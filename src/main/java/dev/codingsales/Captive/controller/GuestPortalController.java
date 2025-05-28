package dev.codingsales.Captive.controller;

import dev.codingsales.Captive.dto.item.GuestRegistrationRequestDTO;
import dev.codingsales.Captive.dto.response.ErrorResponseDTO;
import dev.codingsales.Captive.dto.response.SuccessResponseDTO;
import dev.codingsales.Captive.entity.Session;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/portal/guest")
@CrossOrigin(origins = "*")
public class GuestPortalController {
    private static final Logger logger = LoggerFactory.getLogger(GuestPortalController.class);

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UnifiAuthService unifiAuthService;
    @Value("${unifi.default.auth.minutes:240}")
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
            logger.error("Dados de cadastro inválidos: {}", errors);
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
            // 1. Verificar se já existe uma sessão para este MAC (ativa ou expirada)
            Optional<Session> existingSessionOpt = sessionService.findByDeviceMac(registrationRequest.getDeviceMac()); // <-- MUDANÇA AQUI: Usa Optional

            if (existingSessionOpt.isPresent()) {
                Session existingSession = existingSessionOpt.get();

                if (existingSession.getExpireLoginOn().after(new Timestamp(System.currentTimeMillis()))) {
                    // Sessão ATIVA existente para este MAC
                    logger.info("Dispositivo {} já possui uma sessão ativa. Retornando 'Already Active'.", registrationRequest.getDeviceMac());
                    return ResponseEntity.ok(new SuccessResponseDTO(
                            HttpStatus.OK.value(),
                            "Already Active",
                            "Seu dispositivo já está autorizado e com uma sessão ativa."));
                } else {
                    // Sessão EXPIRADA existente para este MAC - ATUALIZAR
                    logger.info("Sessão expirada encontrada para MAC {}. Atualizando com novos dados de registro.", registrationRequest.getDeviceMac());

                    // Preencher existingSession com os novos dados do registrationRequest
                    existingSession.setFullName(registrationRequest.getFullName());
                    existingSession.setEmail(registrationRequest.getEmail());
                    existingSession.setPhoneNumber(registrationRequest.getPhoneNumber());
                    // MAC, IP, AP MAC, Browser, OS já devem ser do request atual, mas setar novamente para clareza
                    existingSession.setDeviceMac(registrationRequest.getDeviceMac());
                    existingSession.setDeviceIp(clientIp);
                    existingSession.setAccesspointMac(apMac != null ? apMac : "N/A");
                    existingSession.setBrowser(registrationRequest.getBrowser());
                    existingSession.setOperatingSystem(registrationRequest.getOperatingSystem());
                    existingSession.setAcceptedTou(registrationRequest.getAcceptTou());

                    Timestamp lastLogin = new Timestamp(System.currentTimeMillis());
                    Timestamp expireDate = new Timestamp(lastLogin.getTime() + TimeUnit.MINUTES.toMillis(unifiSessionDurationMinutes - localSessionHiddenMinutes));
                    Timestamp removeDate = new Timestamp(expireDate.getTime() + TimeUnit.MINUTES.toMillis(localSessionBlockMinutes));

                    existingSession.setLastLoginOn(lastLogin);
                    existingSession.setExpireLoginOn(expireDate);
                    existingSession.setRemoveSessionOn(removeDate);

                    boolean unifiAuthorized = unifiAuthService.authorizeDevice(
                            registrationRequest.getDeviceMac(),
                            null, // siteId
                            unifiSessionDurationMinutes, // minutes
                            null, // dataLimitMb
                            null, // downloadSpeedKbps
                            null  // uploadSpeedKbps
                    );

                    if (unifiAuthorized) {
                        sessionService.updateSession(existingSession.getId(), existingSession); // <<< ATUALIZA A SESSÃO EXISTENTE
                        logger.info("Sessão de convidado existente atualizada para MAC: {}.", existingSession.getDeviceMac());
                        return ResponseEntity.ok(new SuccessResponseDTO(
                                HttpStatus.OK.value(),
                                "Registration Updated",
                                "Cadastro atualizado e acesso à internet liberado!"));
                    } else {
                        logger.info("Falha ao autorizar dispositivo MAC {} no UniFi durante atualização.", registrationRequest.getDeviceMac());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "UniFi Authorization Failed",
                                "Não foi possível liberar o acesso à internet no momento da atualização. Tente novamente mais tarde."));
                    }
                }
            }
            if (sessionService.findByEmail(registrationRequest.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDTO(
                        HttpStatus.CONFLICT.value(),
                        "Email Already Registered",
                        "Este email já possui um cadastro. Por favor, use a opção 'Login'."));
            }
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

                Session newSession = new Session();
                newSession.setFullName(registrationRequest.getFullName());
                newSession.setEmail(registrationRequest.getEmail());
                newSession.setPhoneNumber(registrationRequest.getPhoneNumber());
                newSession.setDeviceMac(registrationRequest.getDeviceMac());
                newSession.setDeviceIp(clientIp);
                newSession.setAccesspointMac(apMac != null ? apMac : "N/A");
                newSession.setBrowser(registrationRequest.getBrowser());
                newSession.setOperatingSystem(registrationRequest.getOperatingSystem());
                newSession.setAcceptedTou(registrationRequest.getAcceptTou());

                Timestamp lastLogin = new Timestamp(System.currentTimeMillis());
                Timestamp expireDate = new Timestamp(lastLogin.getTime() + TimeUnit.MINUTES.toMillis(unifiSessionDurationMinutes - localSessionHiddenMinutes));
                Timestamp removeDate = new Timestamp(expireDate.getTime() + TimeUnit.MINUTES.toMillis(localSessionBlockMinutes));

                newSession.setLastLoginOn(lastLogin);
                newSession.setExpireLoginOn(expireDate);
                newSession.setRemoveSessionOn(removeDate);

                sessionService.addSession(newSession);
                logger.info("Nova sessão de convidado salva para MAC: {}.", newSession.getDeviceMac());

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
}