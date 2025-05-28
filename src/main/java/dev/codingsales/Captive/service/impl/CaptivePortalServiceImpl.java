package dev.codingsales.Captive.service.impl;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import dev.codingsales.Captive.include.unifi.dto.RequestAuthorizeGuestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.codingsales.Captive.dto.captivelportal.AuthorizeDeviceRequestDTO; // Seu DTO agora com fullName e phoneNumber
import dev.codingsales.Captive.dto.captivelportal.SessionInfoDTO;
import dev.codingsales.Captive.entity.Session;
import dev.codingsales.Captive.exeption.NoContentException;
import dev.codingsales.Captive.include.unifi.UnifiApiClient;
import dev.codingsales.Captive.include.unifi.dto.ClientDTO;
import dev.codingsales.Captive.include.unifi.dto.ResponseDTO;
import dev.codingsales.Captive.service.CaptivePortalService;
import dev.codingsales.Captive.service.SessionService;
import dev.codingsales.Captive.util.LoggerConstants;
// dev.codingsales.Captive.util.UserAgentUtils; // Não é mais necessário aqui, pois browser/OS vêm do request

@Service
public class CaptivePortalServiceImpl implements CaptivePortalService {
    private static final Logger logger = LoggerFactory.getLogger(CaptivePortalServiceImpl.class);

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UnifiApiClient unifiApiClient;

    @Value("${unifi.default.auth.minutes}")
    private Integer sessionDurationMinutes;

    @Value("${unifi.default.auth.download.kbps:#{null}}")
    private Integer downloadSpeedKbps;

    @Value("${unifi.default.auth.upload.kbps:#{null}}")
    private Integer uploadSpeedKbps;

    @Value("${unifi.default.auth.data.limit.mb:#{null}}")
    private Long dataUsageLimitMBytes;

    @Value("${unifiApi.controller.session.hiddenMinutes:0}")
    private long sessionHiddenMinutes;

    @Value("${unifiApi.controller.session.blockMinutes:0}")
    private long sessionBlockMinutes;


    @Override
    public SessionInfoDTO authorizeDevice(AuthorizeDeviceRequestDTO request) throws Exception {
        logger.info("Authorizing device: MAC={}, IP={}, AP_MAC={}, Email={}, FullName={}, PhoneNumber={}",
                request.getMacAddress(), request.getIpAddress(), request.getAccessPointMacAddress(),
                request.getEmail(), request.getFullName(), request.getPhoneNumber()); // Log adicionado para os novos campos

        // 1. Tentar encontrar uma sessão existente para este MAC.
        Optional<Session> existingSessionOpt = sessionService.findByDeviceMac(request.getMacAddress());

        // Se uma sessão ativa for encontrada para o MAC, apenas retorna sua informação.
        if (existingSessionOpt.isPresent() && existingSessionOpt.get().getExpireLoginOn().after(new Timestamp(System.currentTimeMillis()))) {
            logger.info("Dispositivo {} já possui uma sessão ativa. Retornando informações da sessão existente.", request.getMacAddress());
            return this.getSessionInfo(request.getMacAddress());
        }

        // Variável para armazenar a sessão que será salva/atualizada
        Session sessionToProcess;

        // Se uma sessão existente foi encontrada (mesmo que expirada), usaremos ela para atualização.
        // Caso contrário, criaremos uma nova.
        if (existingSessionOpt.isPresent()) {
            sessionToProcess = existingSessionOpt.get(); // Reutiliza a sessão existente
            logger.info("Sessão existente para MAC {} encontrada (expirada). Preparando para re-autorização e atualização.", request.getMacAddress());
        } else {
            sessionToProcess = new Session(); // Cria uma nova sessão
            logger.info("Nenhuma sessão existente para MAC {}. Criando nova sessão para autorização.", request.getMacAddress());
        }

        // --- Lógica de Autorização no UniFi ---
        ClientDTO unifiClient = unifiApiClient.getClientByMac(
                "default", // siteId, ou use uma propriedade injetada
                request.getMacAddress()
        );

        if (unifiClient == null || unifiClient.getId() == null) {
            logger.error("Não foi possível recuperar o ID do cliente UniFi (UUID) para o MAC: {}. A autorização não pode prosseguir.", request.getMacAddress());
            throw new RuntimeException("Falha ao encontrar o dispositivo " + request.getMacAddress() + " no controlador UniFi. Garanta que o dispositivo esteja conectado.");
        }
        String clientIdUuid = unifiClient.getId();

        RequestAuthorizeGuestDTO unifiPayload = RequestAuthorizeGuestDTO.builder()
                .action("AUTHORIZE_GUEST_ACCESS")
                .timeLimitMinutes(this.sessionDurationMinutes)
                .rxRateLimitKbps(this.downloadSpeedKbps)
                .txRateLimitKbps(this.uploadSpeedKbps)
                .dataUsageLimitMBytes(this.dataUsageLimitMBytes)
                .build();

        logger.info("Tentando autorizar o cliente UniFi UUID: {} com payload: {}", clientIdUuid, unifiPayload);
        ResponseDTO unifiResponse = unifiApiClient.executeClientAction(
                "default", // siteId
                clientIdUuid,
                unifiPayload
        );

        if (unifiResponse != null && unifiResponse.getMeta() != null && "ok".equalsIgnoreCase(unifiResponse.getMeta().getRc())) {
            logger.info("Controlador UniFi autorizou o cliente UUID: {} com sucesso. Salvando/Atualizando sessão local.", clientIdUuid);

            sessionToProcess.setFullName(request.getFullName());
            sessionToProcess.setEmail(request.getEmail());
            sessionToProcess.setPhoneNumber(request.getPhoneNumber());
            sessionToProcess.setDeviceMac(request.getMacAddress()); // Deve ser o mesmo do request
            sessionToProcess.setDeviceIp(request.getIpAddress()); // IP atual do request
            sessionToProcess.setAccesspointMac(request.getAccessPointMacAddress() != null ? request.getAccessPointMacAddress() : "N/A");
            sessionToProcess.setBrowser(request.getBrowser()); // Browser atual do request
            sessionToProcess.setOperatingSystem(request.getOperatingSystem()); // OS atual do request
            sessionToProcess.setAcceptedTou(request.getAcceptTou());

            Timestamp lastLogin = new Timestamp(System.currentTimeMillis());
            Timestamp expireDate = new Timestamp(lastLogin.getTime());

            sessionToProcess.setLastLoginOn(lastLogin);
            sessionToProcess.setExpireLoginOn(expireDate);


            if (existingSessionOpt.isPresent()) {
                sessionService.updateSession(sessionToProcess.getId(), sessionToProcess);
                logger.info("Sessão existente para MAC {} (ID: {}) atualizada após autorização UniFi.", request.getMacAddress(), sessionToProcess.getId());
            } else {
                sessionService.addSession(sessionToProcess);
                logger.info("Nova sessão salva para MAC {} após autorização UniFi.", sessionToProcess.getDeviceMac());
            }

            return generateSessionInfo(sessionToProcess); // Retorna a informação da sessão processada
        } else {
            String errMsg = (unifiResponse != null && unifiResponse.getMeta() != null) ? unifiResponse.getMeta().getMsg() : "Erro desconhecido";
            logger.error("Controlador UniFi falhou ao autorizar convidado UUID: {}. Razão: {}. Dados da Resposta UniFi: {}",
                    clientIdUuid, errMsg, unifiResponse != null ? unifiResponse.getData() : "N/A");
            throw new RuntimeException("Controlador UniFi não pôde autorizar o convidado. Detalhes: " + errMsg);
        }
    }

    @Override
    public SessionInfoDTO getSessionInfo(String macAddress) throws NoContentException {
        Optional<Session> sessionOpt = sessionService.findByDeviceMac(macAddress);
        if (sessionOpt.isPresent()) {
            return generateSessionInfo(sessionOpt.get());
        } else {
            String error = String.format(LoggerConstants.NOT_FOUND_EXCEPTION, "CaptivePortalServiceImpl",
                    "getSessionInfo", "session for MAC: " + macAddress, "");
            logger.error(error);
            throw new NoContentException(error);
        }
    }

    private SessionInfoDTO generateSessionInfo(Session session) {
        Timestamp loginDate = session.getLastLoginOn();
        Timestamp expireDate = session.getExpireLoginOn();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        long diff = expireDate.getTime() - now.getTime();
        if (diff <= 0) {
            return new SessionInfoDTO(session.getDeviceMac(), 0L, 0L, expireDate, loginDate);
        }
        long diffSeconds = (diff / 1000) % 60;
        long diffMinutes = (diff / (60 * 1000));
        return new SessionInfoDTO(session.getDeviceMac(), diffMinutes, diffSeconds, expireDate, loginDate);
    }

    // O método generateSession original não é mais diretamente necessário.
    // Mantenho-o aqui por compatibilidade ou para outros usos, se houver.
    private Session generateSession(String macAddress, String ipAddress, String accessPointMac, String browser,
                                    String operatingSystem) {
        Session session = new Session();
        session.setBrowser(browser);
        session.setOperatingSystem(operatingSystem);
        session.setDeviceMac(macAddress);
        session.setAccesspointMac(accessPointMac != null ? accessPointMac : "N/A");
        session.setDeviceIp(ipAddress);

        int durationMinutesActual = (this.sessionDurationMinutes != null) ? this.sessionDurationMinutes : 0;

        Timestamp lastLogin = new Timestamp(System.currentTimeMillis());
        Timestamp expireDate = new Timestamp(lastLogin.getTime());

        long visibleDurationMillis = TimeUnit.MINUTES.toMillis(durationMinutesActual);
        if (this.sessionHiddenMinutes > 0) {
            visibleDurationMillis -= TimeUnit.MINUTES.toMillis(this.sessionHiddenMinutes);
        }
        expireDate.setTime(lastLogin.getTime() + Math.max(0, visibleDurationMillis));

        Timestamp removeDate = new Timestamp(lastLogin.getTime() + TimeUnit.MINUTES.toMillis(durationMinutesActual));
        if (this.sessionBlockMinutes > 0) {
            removeDate.setTime(removeDate.getTime() + TimeUnit.MINUTES.toMillis(this.sessionBlockMinutes));
        } else {
            removeDate.setTime(expireDate.getTime() + TimeUnit.MINUTES.toMillis(1));
        }

        session.setLastLoginOn(lastLogin);
        session.setExpireLoginOn(expireDate);
        session.setRemoveSessionOn(removeDate);
        return session;
    }
}