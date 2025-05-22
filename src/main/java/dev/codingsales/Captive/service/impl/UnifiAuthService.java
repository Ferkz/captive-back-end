package dev.codingsales.Captive.service.impl;

import dev.codingsales.Captive.include.unifi.UnifiApiClient;
import dev.codingsales.Captive.include.unifi.dto.ClientDTO;
import dev.codingsales.Captive.include.unifi.dto.RequestAuthorizeGuestDTO;
import dev.codingsales.Captive.include.unifi.dto.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UnifiAuthService { // Renomeado ou adaptado do seu UnifiAuthService existente

    private static final Logger logger = LoggerFactory.getLogger(UnifiAuthService.class);

    private final UnifiApiClient unifiApiClient; // Este será o nosso novo Impl com X-API-KEY
    private final String defaultSiteId;
    private final int defaultAuthMinutes;
    private final Long defaultDataLimitMb;
    private final Integer defaultDownloadKbps;
    private final Integer defaultUploadKbps;


    public UnifiAuthService(
            UnifiApiClient unifiApiClient, // Injeta nossa nova implementação
            @Value("${unifi.default.site.id}") String defaultSiteId,
            @Value("${unifi.default.auth.minutes}") int defaultAuthMinutes,
            @Value("${unifi.default.auth.data.limit.mb:#{null}}") Long defaultDataLimitMb,
            @Value("${unifi.default.auth.download.kbps:#{null}}") Integer defaultDownloadKbps,
            @Value("${unifi.default.auth.upload.kbps:#{null}}") Integer defaultUploadKbps) {
        this.unifiApiClient = unifiApiClient;
        this.defaultSiteId = defaultSiteId;
        this.defaultAuthMinutes = defaultAuthMinutes;
        this.defaultDataLimitMb = defaultDataLimitMb;
        this.defaultDownloadKbps = defaultDownloadKbps;
        this.defaultUploadKbps = defaultUploadKbps;
    }

    // Removido loginToUnifiController, pois a autenticação é via X-API-KEY em UnifiApiClientImpl

    public boolean authorizeDevice(String clientMac) {
        return authorizeDevice(clientMac, defaultSiteId, defaultAuthMinutes, defaultDataLimitMb, defaultDownloadKbps, defaultUploadKbps);
    }

    public boolean authorizeDevice(String clientMac, String siteId, Integer minutes, Long dataLimitMb, Integer downloadSpeedKbps, Integer uploadSpeedKbps) {
        if (clientMac == null || clientMac.trim().isEmpty()) {
            logger.error("Client MAC address cannot be null or empty for authorization.");
            return false;
        }

        ClientDTO client = unifiApiClient.getClientByMac(siteId, clientMac);
        if (client == null || client.getId() == null || client.getId().trim().isEmpty()) {
            logger.error("Could not find clientId (UUID) for MAC {} in site {}. Device might not be connected or visible to the controller yet.", clientMac, siteId);
            return false;
        }
        String clientIdUuid = client.getId();

        RequestAuthorizeGuestDTO authRequestPayload = RequestAuthorizeGuestDTO.builder()
                .action("AUTHORIZE_GUEST_ACCESS")
                .timeLimitMinutes(minutes != null ? minutes : defaultAuthMinutes)
                .dataUsageLimitMBytes(dataLimitMb)
                .rxRateLimitKbps(downloadSpeedKbps)
                .txRateLimitKbps(uploadSpeedKbps)
                .build();

        logger.info("Attempting to authorize device MAC: {}, ClientID (UUID): {}, Site: {}, Action: {}, Duration: {} min",
                clientMac, clientIdUuid, siteId, authRequestPayload.getAction(), authRequestPayload.getTimeLimitMinutes());

        try {
            ResponseDTO response = unifiApiClient.executeClientAction(siteId, clientIdUuid, authRequestPayload);
            if (response != null && response.getMeta() != null && "ok".equalsIgnoreCase(response.getMeta().getRc())) {
                logger.info("Device {} (UUID: {}) authorized successfully in UniFi. UniFi Response: {}", clientMac, clientIdUuid, response.getData());
                return true;
            } else {
                String errorMsg = (response != null && response.getMeta() != null) ? response.getMeta().getMsg() : "Unknown error from UniFi API client";
                logger.error("Failed to authorize device {} (UUID: {}) in UniFi. Reason: {}. UniFi Raw Data: {}", clientMac, clientIdUuid, errorMsg, response != null ? response.getData() : "N/A");
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception while authorizing device {} (UUID: {}) in UniFi: {}", clientMac, clientIdUuid, e.getMessage(), e);
            return false;
        }
    }

    public boolean unauthorizeDevice(String clientMac) {
        return unauthorizeDevice(clientMac, defaultSiteId);
    }

    public boolean unauthorizeDevice(String clientMac, String siteId) {
        if (clientMac == null || clientMac.trim().isEmpty()) {
            logger.error("Client MAC address cannot be null or empty for unauthorization.");
            return false;
        }

        ClientDTO client = unifiApiClient.getClientByMac(siteId, clientMac);
        if (client == null || client.getId() == null || client.getId().trim().isEmpty()) {
            logger.error("Could not find clientId (UUID) for MAC {} for unauthorization in site {}.", clientMac, siteId);
            return false;
        }
        String clientIdUuid = client.getId();

        RequestAuthorizeGuestDTO unauthRequestPayload = RequestAuthorizeGuestDTO.builder()
                .action("UNAUTHORIZE_GUEST_ACCESS")
                .build();

        logger.info("Attempting to unauthorize device MAC: {}, ClientID (UUID): {}, Site: {}, Action: {}",
                clientMac, clientIdUuid, siteId, unauthRequestPayload.getAction());
        try {
            ResponseDTO response = unifiApiClient.executeClientAction(siteId, clientIdUuid, unauthRequestPayload);
            if (response != null && response.getMeta() != null && "ok".equalsIgnoreCase(response.getMeta().getRc())) {
                logger.info("Device {} (UUID: {}) unauthorized successfully in UniFi. UniFi Response: {}", clientMac, clientIdUuid, response.getData());
                return true;
            } else {
                String errorMsg = (response != null && response.getMeta() != null) ? response.getMeta().getMsg() : "Unknown error from UniFi API client";
                logger.error("Failed to unauthorize device {} (UUID: {}) in UniFi. Reason: {}", clientMac, clientIdUuid, errorMsg);
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception while unauthorizing device {} (UUID: {}) in UniFi: {}", clientMac, clientIdUuid, e.getMessage(), e);
            return false;
        }
    }
}