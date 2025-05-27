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
            // 1. Verificar se já existe uma sessão ativa para este MAC
            if (sessionService.existsByDeviceMac(registrationRequest.getDeviceMac())) {
                Session existingSession = sessionService.findByDeviceMac(registrationRequest.getDeviceMac());
                if (existingSession.getExpireLoginOn().after(new Timestamp(System.currentTimeMillis()))) {
                    logger.info("Dispositivo {} já possui uma sessão ativa. Renovando/Retornando."+ registrationRequest.getDeviceMac());
                    // Aqui você pode optar por apenas retornar sucesso ou tentar re-autorizar no UniFi

                    return ResponseEntity.ok(new SuccessResponseDTO(
                            HttpStatus.OK.value(),
                            "Already Active",
                            "Seu dispositivo já está autorizado e com uma sessão ativa."));
                } else {
                    // Sessão existe mas expirou, pode deletar ou atualizar
                    sessionService.deleteSession(existingSession.getId());
                }
            }

            // 2. Tentar autorizar o dispositivo no UniFi via UnifiAuthService (que usa X-API-KEY)
            // O UnifiAuthService já lida com getClientByMac e executeClientAction.
            boolean unifiAuthorized = unifiAuthService.authorizeDevice(
                    registrationRequest.getDeviceMac(),
                    null, // siteId (UnifiAuthService usa o defaultSiteId)
                    unifiSessionDurationMinutes, // minutes
                    null, // dataLimitMb (pode vir do DTO ou properties)
                    null, // downloadSpeedKbps
                    null  // uploadSpeedKbps
            );

            if (unifiAuthorized) {
                logger.info("Dispositivo MAC {} autorizado com sucesso no UniFi."+ registrationRequest.getDeviceMac());

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

                sessionService.addSession(newSession); // Seu SessionService.addSession
                logger.info("Sessão de convidado salva para MAC: {}"+ newSession.getDeviceMac());

                return ResponseEntity.ok(new SuccessResponseDTO(
                        HttpStatus.OK.value(),
                        "Registration Successful",
                        "Cadastro realizado e acesso à internet liberado!"));
            } else {
                logger.info("Falha ao autorizar o dispositivo MAC {} no UniFi."+ registrationRequest.getDeviceMac());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "UniFi Authorization Failed",
                        "Não foi possível liberar o acesso à internet no momento. Tente novamente mais tarde."));
            }

        } catch (Exception e) {
            logger.info("Erro durante o processo de cadastro e autorização para MAC {}: {}"+  e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Server Error",
                    "Ocorreu um erro interno: " + e.getMessage()));
        }
    }
}
