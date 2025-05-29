package dev.codingsales.Captive.controller;

import dev.codingsales.Captive.dto.item.GuestRegistrationRequestDTO;
import dev.codingsales.Captive.dto.response.ErrorResponseDTO;
import dev.codingsales.Captive.dto.response.SuccessResponseDTO;
import dev.codingsales.Captive.entity.Session;
import dev.codingsales.Captive.include.unifi.dto.GuestLoginRequestDTO;
import dev.codingsales.Captive.service.SessionService;
import dev.codingsales.Captive.service.impl.UnifiAuthService;
import dev.codingsales.Captive.util.UserAgentUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/portal/guest") //endpoint base, publico
@CrossOrigin(origins = "*")
public class GuestPortalController {
    private static final Logger logger = LoggerFactory.getLogger(GuestPortalController.class);

    @Autowired
    private SessionService sessionService; //salvar os dados do convidado

    @Autowired
    private UnifiAuthService unifiAuthService; //interagir com a API
    @Value("${unifi.default.auth.minutes:240}") // Default se não definido
    private int unifiSessionDurationMinutes;

    @Value("${unifiApi.controller.session.hiddenMinutes:0}")
    private long localSessionHiddenMinutes;

    @Value("${unifiApi.controller.session.blockMinutes:0}")
    private long localSessionBlockMinutes;

    @PostMapping("/register-and-authorize")
    public ResponseEntity<?> registerAndAuthorizeDevice(
            @Valid @RequestBody GuestRegistrationRequestDTO registrationRequest,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        logger.info("Recebida requisição de cadastro e autorização: {}"+ registrationRequest);

        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            logger.error("Dados de cadastro inválidos: {}" + errors);
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation Error",
                    errors));
        }
        // Obter o mac e o Ip do cliente. o unifi enviar o mac
        String clientMac = registrationRequest.getDeviceMac();
        String clientIp = httpRequest.getRemoteAddr();
        String apMac = registrationRequest.getAccessPointMac();
        if (clientMac == null || clientMac.trim().isEmpty()) {
            logger.error("Mac address do cliente não foi fornecido na requisição.");
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                    HttpStatus.BAD_REQUEST.value(), "Mac não encontrado.", "Cliente mac não encontrado na requisição"
            ));
        }
        registrationRequest.setDeviceMac(clientMac);
        registrationRequest.setDeviceIp(clientIp);
        registrationRequest.setAccessPointMac(apMac);
        registrationRequest.setBrowser(UserAgentUtils.getBrowser(httpRequest));
        registrationRequest.setOperatingSystem(UserAgentUtils.getOperatingSystem(httpRequest));
        try {
            // Verificar se já existe uma sessão ATIVA para este MAC
            // (Esta parte da lógica já existe e pode ser complementada pelo novo fluxo de login)
            if (sessionService.existsByDeviceMac(registrationRequest.getDeviceMac())) {
                Session existingSession = sessionService.findByDeviceMac(registrationRequest.getDeviceMac());


                if (existingSession.getExpireLoginOn().after(new Timestamp(System.currentTimeMillis()))) {
                    logger.info("Dispositivo {} já possui uma sessão ativa. Renovando/Retornando.", registrationRequest.getDeviceMac());

                    return ResponseEntity.ok(new SuccessResponseDTO(
                            HttpStatus.OK.value(),
                            "already active",
                            "Seu dispositivo já está autorizado e com uma sessão ativa."));
                } else {
                    // Sessão existe mas expirou, pode deletar ou atualizar
                    sessionService.deleteSession(existingSession.getId());
                }
            }

            // **Adição para verificar se o email já está cadastrado**
            // Se você quer que o email seja único para todos os cadastros
            if (sessionService.findByEmail(registrationRequest.getEmail()).isPresent()) {
                // Se um cadastro com este email já existe (mesmo que a sessão tenha expirado),
                // você pode querer direcionar o usuário para a tela de "login"
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDTO(
                        HttpStatus.CONFLICT.value(),
                        "Email Already Registered",
                        "Este email já possui um cadastro. Por favor, use a opção 'Login'."));
            }

            // 2. Tentar autorizar o dispositivo no UniFi via UnifiAuthService
            boolean unifiAuthorized = unifiAuthService.authorizeDevice(
                    registrationRequest.getDeviceMac(),
                    null, // siteId (UnifiAuthService usa o defaultSiteId)
                    unifiSessionDurationMinutes, // minutes
                    null, // dataLimitMb
                    null, // downloadSpeedKbps
                    null  // uploadSpeedKbps
            );

            if (unifiAuthorized) {
                logger.info("Dispositivo MAC {} autorizado com sucesso no UniFi.", registrationRequest.getDeviceMac());

                // 3. Salvar os dados do "convidado" e da sessão no banco de dados
                Session newSession = new Session();
                newSession.setFullName(registrationRequest.getFullName());
                newSession.setEmail(registrationRequest.getEmail());
                newSession.setPhoneNumber(registrationRequest.getPhoneNumber());
                newSession.setDeviceMac(registrationRequest.getDeviceMac());
                newSession.setDeviceIp(registrationRequest.getDeviceIp());
                newSession.setAccesspointMac(registrationRequest.getAccessPointMac() != null ? registrationRequest.getAccessPointMac() : "N/A");
                newSession.setBrowser(registrationRequest.getBrowser());
                newSession.setOperatingSystem(registrationRequest.getOperatingSystem());
                newSession.setAcceptedTou(registrationRequest.getAcceptTou());

                // Calcular tempos de expiração para a sessão local
                Timestamp lastLogin = new Timestamp(System.currentTimeMillis());
                Timestamp expireDate = new Timestamp(lastLogin.getTime() + TimeUnit.MINUTES.toMillis(unifiSessionDurationMinutes - localSessionHiddenMinutes));
                Timestamp removeDate = new Timestamp(expireDate.getTime() + TimeUnit.MINUTES.toMillis(localSessionBlockMinutes));

                newSession.setLastLoginOn(lastLogin);
                newSession.setExpireLoginOn(expireDate);
                newSession.setRemoveSessionOn(removeDate);

                sessionService.addSession(newSession);
                logger.info("Sessão de convidado salva para MAC: {}.", newSession.getDeviceMac());

                return ResponseEntity.ok(new SuccessResponseDTO(
                        HttpStatus.OK.value(),
                        "Registration Successful",
                        "Cadastro realizado e acesso à internet liberado!"));
            } else {
                logger.info("Falha ao autorizar o dispositivo MAC {} no UniFi.", registrationRequest.getDeviceMac());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "UniFi Authorization Failed",
                        "Não foi possível liberar o acesso à internet no momento. Tente novamente mais tarde."));
            }

        } catch (Exception e) {
            logger.error("Erro durante o processo de cadastro e autorização para MAC {}: {}", registrationRequest.getDeviceMac(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Server Error",
                    "Ocorreu um erro interno: " + e.getMessage()));
        }
    }
    //endpoint convidados
    @PostMapping("/login")
    public ResponseEntity<?> guestLogin(
            @Valid @RequestBody GuestLoginRequestDTO loginRequest,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {

        logger.info("Recebida requisição de login de convidado: {}", loginRequest);

        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            logger.error("Dados de login inválidos: {}", errors);
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation Error",
                    errors));
        }

        String clientMac = loginRequest.getDeviceMac();
        String clientIp = httpRequest.getRemoteAddr();
        String apMac = loginRequest.getAccessPointMac(); // Pode vir na requisição do portal ou ser nulo

        // Se o MAC não for fornecido, tente obtê-lo do cabeçalho X-Forwarded-For ou de algum outro lugar
        if (clientMac == null || clientMac.trim().isEmpty()) {
            // Este é um fallback, idealmente o cliente deve enviar o MAC.
            // Para portais cativos, o MAC geralmente é interceptado/enviado pelo AP.
            logger.warn("MAC do dispositivo não fornecido na requisição de login de convidado para email: {}. Tentando buscar no cabeçalho X-Forwarded-For.", loginRequest.getEmail());
            // Lógica para tentar extrair o MAC do request, se disponível.
            // Por exemplo, se o AP injeta um cabeçalho X-Client-Mac:
            // clientMac = httpRequest.getHeader("X-Client-Mac");
            // Se não for encontrado, você pode retornar um erro ou tentar prosseguir sem ele,
            // mas a autorização UniFi precisa do MAC.
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                    HttpStatus.BAD_REQUEST.value(), "MAC Missing", "MAC do dispositivo não encontrado na requisição."
            ));
        }

        try {
            // 1. Tentar encontrar uma sessão válida para este email E MAC
            Optional<Session> validSessionOpt = sessionService.findValidSessionByEmailAndMac(
                    loginRequest.getEmail(),
                    clientMac);

            if (validSessionOpt.isPresent()) {
                Session existingSession = validSessionOpt.get();
                logger.info("Sessão válida encontrada para email {} e MAC {}. Reautorizando no UniFi.", loginRequest.getEmail(), clientMac);

                // Re-autorizar o dispositivo no UniFi (se ainda não estiver autorizado ou para renovar)
                boolean unifiAuthorized = unifiAuthService.authorizeDevice(
                        clientMac,
                        null, // siteId
                        unifiSessionDurationMinutes, // minutes
                        null, // dataLimitMb
                        null, // downloadSpeedKbps
                        null  // uploadSpeedKbps
                );

                if (unifiAuthorized) {
                    // Atualizar o timestamp da sessão local se desejar renovar a contagem de tempo visível
                    existingSession.setLastLoginOn(new Timestamp(System.currentTimeMillis()));
                    // Recalcular expireLoginOn e removeSessionOn se a autorização na UniFi renovar a duração
                    Timestamp newExpireDate = new Timestamp(existingSession.getLastLoginOn().getTime() + TimeUnit.MINUTES.toMillis(unifiSessionDurationMinutes - localSessionHiddenMinutes));
                    Timestamp newRemoveDate = new Timestamp(newExpireDate.getTime() + TimeUnit.MINUTES.toMillis(localSessionBlockMinutes));
                    existingSession.setExpireLoginOn(newExpireDate);
                    existingSession.setRemoveSessionOn(newRemoveDate);
                    sessionService.updateSession(existingSession.getId(), existingSession);

                    return ResponseEntity.ok(new SuccessResponseDTO(
                            HttpStatus.OK.value(),
                            "Login Successful",
                            "Seu acesso à internet foi reativado!"));
                } else {
                    logger.error("Falha ao re-autorizar dispositivo MAC {} no UniFi após login de convidado.", clientMac);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "UniFi Reauthorization Failed",
                            "Não foi possível reativar o acesso à internet no momento. Tente novamente mais tarde."));
                }
            } else {
                // Nenhuma sessão válida encontrada com este email e MAC.
                // Aqui você pode verificar se o email existe em algum cadastro anterior (mesmo que expirado).
                Optional<Session> existingRegistrationByEmail = sessionService.findByEmail(loginRequest.getEmail());

                if (existingRegistrationByEmail.isPresent()) {
                    // O email existe, mas não há sessão ativa para este MAC, ou a sessão expirou.
                    // Isso pode significar que o usuário está tentando fazer login de um NOVO dispositivo
                    // ou que a sessão do dispositivo anterior expirou e precisa ser re-autorizada.

                    // Opção 1: Tratar como um novo dispositivo para um usuário existente
                    // (similar ao registro, mas sem coletar todos os dados novamente)
                    logger.info("Email {} encontrado, mas sem sessão ativa para MAC {}. Tratando como novo login para MAC.", loginRequest.getEmail(), clientMac);

                    // Re-autorizar o dispositivo no UniFi (mesmo que seja um novo MAC para o email)
                    boolean unifiAuthorized = unifiAuthService.authorizeDevice(
                            clientMac,
                            null, // siteId
                            unifiSessionDurationMinutes, // minutes
                            null, // dataLimitMb
                            null, // downloadSpeedKbps
                            null  // uploadSpeedKbps
                    );

                    if (unifiAuthorized) {
                        // Criar uma nova sessão local para o novo dispositivo do usuário existente
                        Session newSessionForExistingUser = new Session();
                        newSessionForExistingUser.setFullName(existingRegistrationByEmail.get().getFullName()); // Reutiliza dados
                        newSessionForExistingUser.setEmail(loginRequest.getEmail());
                        newSessionForExistingUser.setPhoneNumber(existingRegistrationByEmail.get().getPhoneNumber());
                        newSessionForExistingUser.setDeviceMac(clientMac); // NOVO MAC
                        newSessionForExistingUser.setDeviceIp(clientIp);
                        newSessionForExistingUser.setAccesspointMac(apMac != null ? apMac : "N/A");
                        newSessionForExistingUser.setBrowser(UserAgentUtils.getBrowser(httpRequest));
                        newSessionForExistingUser.setOperatingSystem(UserAgentUtils.getOperatingSystem(httpRequest));
                        newSessionForExistingUser.setAcceptedTou(existingRegistrationByEmail.get().getAcceptedTou()); // Reutiliza aceitação do TOU

                        Timestamp lastLogin = new Timestamp(System.currentTimeMillis());
                        Timestamp expireDate = new Timestamp(lastLogin.getTime() + TimeUnit.MINUTES.toMillis(unifiSessionDurationMinutes - localSessionHiddenMinutes));
                        Timestamp removeDate = new Timestamp(expireDate.getTime() + TimeUnit.MINUTES.toMillis(localSessionBlockMinutes));

                        newSessionForExistingUser.setLastLoginOn(lastLogin);
                        newSessionForExistingUser.setExpireLoginOn(expireDate);
                        newSessionForExistingUser.setRemoveSessionOn(removeDate);

                        sessionService.addSession(newSessionForExistingUser); // Adiciona nova sessão
                        logger.info("Nova sessão salva para email {} no MAC: {}.", loginRequest.getEmail(), newSessionForExistingUser.getDeviceMac());

                        return ResponseEntity.ok(new SuccessResponseDTO(
                                HttpStatus.OK.value(),
                                "Login Successful",
                                "Seu acesso à internet foi reativado para este dispositivo!"));
                    } else {
                        logger.error("Falha ao autorizar NOVO dispositivo MAC {} no UniFi para email {}.", clientMac, loginRequest.getEmail());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "UniFi Authorization Failed",
                                "Não foi possível liberar o acesso à internet para este dispositivo no momento. Tente novamente mais tarde."));
                    }

                } else {
                    // Email não encontrado em nenhum cadastro (nem ativo, nem expirado)
                    logger.warn("Tentativa de login de convidado com email {} e MAC {} sem cadastro encontrado.", loginRequest.getEmail(), clientMac);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseDTO(
                            HttpStatus.UNAUTHORIZED.value(),
                            "Registration Not Found",
                            "Email não encontrado. Por favor, registre-se primeiro."));
                }
            }

        } catch (Exception e) {
            logger.error("Erro durante o processo de login de convidado para email {} e MAC {}: {}", loginRequest.getEmail(), clientMac, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Server Error",
                    "Ocorreu um erro interno: " + e.getMessage()));
        }
    }
}