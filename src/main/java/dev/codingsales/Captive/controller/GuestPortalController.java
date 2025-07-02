package dev.codingsales.Captive.controller;

import dev.codingsales.Captive.dto.item.GuestRegistrationRequestDTO;
import dev.codingsales.Captive.dto.response.ErrorResponseDTO;
import dev.codingsales.Captive.dto.response.SuccessResponseDTO;
import dev.codingsales.Captive.entity.Session;
import dev.codingsales.Captive.include.unifi.dto.GuestLoginRequestDTO;
import dev.codingsales.Captive.include.unifi.dto.UnifiAuthServiceResponseDTO;
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

        logger.info("Recebida requisição de cadastro e autorização: {}", registrationRequest);

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

        String clientMac = registrationRequest.getDeviceMac();
        String clientIp = httpRequest.getRemoteAddr();
        String apMac = registrationRequest.getAccessPointMac();
        if (clientMac == null || clientMac.trim().isEmpty()) {
            logger.error("Mac address do cliente não foi fornecido na requisição.");
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                    HttpStatus.BAD_REQUEST.value(), "Mac não encontrado.", "Cliente mac não encontrado na requisição"
            ));
        }

        UnifiAuthServiceResponseDTO unifiAuthResponse = unifiAuthService.authorizeDevice(
                registrationRequest.getDeviceMac(),
                null, // siteId (UnifiAuthService usa o defaultSiteId)
                unifiSessionDurationMinutes, // minutes
                null, // dataLimitMb
                null, // downloadSpeedKbps
                null  // uploadSpeedKbps
        );

        try {
            if (sessionService.existsByDeviceMac(registrationRequest.getDeviceMac())) {
                Optional<Session> existingSessionOpt = Optional.ofNullable(sessionService.findByDeviceMac(registrationRequest.getDeviceMac()));
                if(existingSessionOpt.isPresent()){
                    Session existingSession = existingSessionOpt.get();
                    if (existingSession.getExpireLoginOn().after(new Timestamp(System.currentTimeMillis()))) {
                        logger.info("Dispositivo {} já possui uma sessão ativa. Renovando/Retornando.", registrationRequest.getDeviceMac());

                        return ResponseEntity.ok(new SuccessResponseDTO(
                                HttpStatus.OK.value(),
                                "already active",
                                unifiAuthResponse.getRedirectUrl(),
                                "Seu dispositivo já está autorizado e com uma sessão ativa."));
                    } else {
                        sessionService.deleteSession(existingSession.getId());
                    }
                }
            }
            if (sessionService.findByCpf(registrationRequest.getCpf()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDTO(
                        HttpStatus.CONFLICT.value(),
                        "Cpf Already Registered",
                        "Este cpf já possui um cadastro. Por favor, use a opção 'Login'."));
            }

            if (unifiAuthResponse.isAuthorized()) {
                logger.info("Dispositivo MAC {} autorizado com sucesso no UniFi.",
                        registrationRequest.getDeviceMac());

                Session newSession = new Session();
                newSession.setFullName(registrationRequest.getFullName());
                newSession.setCpf(registrationRequest.getCpf());
                newSession.setDeviceName(unifiAuthResponse.getDeviceName());
                newSession.setDeviceHostName(unifiAuthResponse.getDeviceHostname());
                newSession.setEmail(registrationRequest.getEmail());
                newSession.setPhoneNumber(registrationRequest.getPhoneNumber());
                newSession.setDeviceMac(registrationRequest.getDeviceMac());
                newSession.setDeviceIp(clientIp);
                newSession.setAccesspointMac(apMac != null ? apMac : "N/A");
                newSession.setBrowser(UserAgentUtils.getBrowser(httpRequest));
                newSession.setOperatingSystem(UserAgentUtils.getOperatingSystem(httpRequest));
                newSession.setAcceptedTou(registrationRequest.getAcceptTou());

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
                        unifiAuthResponse.getRedirectUrl(),
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
        String apMac = loginRequest.getAccessPointMac();

        if (clientMac == null || clientMac.trim().isEmpty()) {
            logger.warn("MAC do dispositivo não fornecido na requisição de login de convidado para cpf: {}. Tentando buscar no cabeçalho X-Forwarded-For.", loginRequest.getCpf());
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                    HttpStatus.BAD_REQUEST.value(), "MAC Missing", "MAC do dispositivo não encontrado na requisição."
            ));
        }

        try {
                Optional<Session> existingRegistrationByCpf = sessionService.findByCpf(loginRequest.getCpf());
                if (existingRegistrationByCpf.isPresent()) {
                    logger.info("CPF {} encontrado, mas sem sessão ativa para MAC {}. Tratando como novo login para MAC.", loginRequest.getCpf(), clientMac);

                    UnifiAuthServiceResponseDTO unifiAuthResponse = unifiAuthService.authorizeDevice(
                            clientMac, null, unifiSessionDurationMinutes, null, null, null
                    );

                    if (unifiAuthResponse.isAuthorized()) {
                        Session newSessionForExistingUser = new Session();
                        newSessionForExistingUser.setFullName(existingRegistrationByCpf.get().getFullName());
                        newSessionForExistingUser.setCpf(loginRequest.getCpf());
                        newSessionForExistingUser.setPhoneNumber(existingRegistrationByCpf.get().getPhoneNumber());
                        newSessionForExistingUser.setDeviceMac(clientMac);
                        newSessionForExistingUser.setDeviceIp(clientIp);
                        newSessionForExistingUser.setAccesspointMac(apMac != null ? apMac : "N/A");
                        newSessionForExistingUser.setBrowser(UserAgentUtils.getBrowser(httpRequest));
                        newSessionForExistingUser.setDeviceName(unifiAuthResponse.getDeviceName());
                        newSessionForExistingUser.setDeviceHostName(UserAgentUtils.getOperatingSystem(httpRequest));
                        newSessionForExistingUser.setOperatingSystem(unifiAuthResponse.getDeviceOsName());
                        newSessionForExistingUser.setAcceptedTou(existingRegistrationByCpf.get().getAcceptedTou());

                        Timestamp lastLogin = new Timestamp(System.currentTimeMillis());
                        Timestamp expireDate = new Timestamp(lastLogin.getTime() + TimeUnit.MINUTES.toMillis(unifiSessionDurationMinutes - localSessionHiddenMinutes));
                        Timestamp removeDate = new Timestamp(expireDate.getTime() + TimeUnit.MINUTES.toMillis(localSessionBlockMinutes));

                        newSessionForExistingUser.setLastLoginOn(lastLogin);
                        newSessionForExistingUser.setExpireLoginOn(expireDate);
                        newSessionForExistingUser.setRemoveSessionOn(removeDate);

                        sessionService.addSession(newSessionForExistingUser);
                        logger.info("Nova sessão salva para CPF {} no MAC: {}.", loginRequest.getCpf(), newSessionForExistingUser.getDeviceMac());

                        return ResponseEntity.ok(new SuccessResponseDTO(
                                HttpStatus.OK.value(),
                                "Login Successful",
                                unifiAuthResponse.getRedirectUrl(),
                                "Seu acesso à internet foi reativado para este dispositivo!"));
                    } else {
                        logger.error("Falha ao autorizar NOVO dispositivo MAC {} no UniFi para cpf {}.", clientMac, loginRequest.getCpf());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "UniFi Authorization Failed",
                                "Não foi possível liberar o acesso à internet para este dispositivo no momento."));
                    }

                } else {
                    logger.warn("Tentativa de login de convidado com cpf {} e MAC {} sem cadastro encontrado.", loginRequest.getCpf(), clientMac);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseDTO(
                            HttpStatus.NOT_FOUND.value(),
                            "Registration Not Found",
                            "CPF não encontrado. Por favor, registre-se primeiro."));
                }

        } catch (Exception e) {
            logger.error("Erro durante o processo de login de convidado para cpf {} e MAC {}: {}", loginRequest.getCpf(), clientMac, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Server Error",
                    "Ocorreu um erro interno: " + e.getMessage()));
        }
    }
}
