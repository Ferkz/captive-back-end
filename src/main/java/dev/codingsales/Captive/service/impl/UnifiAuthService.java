package dev.codingsales.Captive.service.impl;

import dev.codingsales.Captive.include.unifi.UnifiApiClient;
import dev.codingsales.Captive.include.unifi.dto.ClientDTO;
import dev.codingsales.Captive.include.unifi.dto.RequestAuthorizeGuestDTO;
import dev.codingsales.Captive.include.unifi.dto.ResponseDTO;
import dev.codingsales.Captive.include.unifi.dto.UnifiAuthServiceResponseDTO; // Importar o DTO correto

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UnifiAuthService {

    private static final Logger logger = LoggerFactory.getLogger(UnifiAuthService.class);

    private final UnifiApiClient unifiApiClient;
    private final String defaultSiteIdInjected;
    private final int defaultAuthMinutesInjected;
    private final Long defaultDataLimitMbInjected;
    private final Integer defaultDownloadKbpsInjected;
    private final Integer defaultUploadKbpsInjected;

    public UnifiAuthService(
            UnifiApiClient unifiApiClient,
            @Value("${unifi.default.site.id}") String defaultSiteIdFromProperties,
            @Value("${unifi.default.auth.minutes}") int defaultAuthMinutesFromProperties,
            @Value("${unifi.default.auth.data.limit.mb:#{null}}") Long defaultDataLimitMbFromProperties,
            @Value("${unifi.default.auth.download.kbps:#{null}}") Integer defaultDownloadKbpsFromProperties,
            @Value("${unifi.default.auth.upload.kbps:#{null}}") Integer defaultUploadKbpsFromProperties) {

        this.unifiApiClient = unifiApiClient;
        this.defaultSiteIdInjected = defaultSiteIdFromProperties;
        this.defaultAuthMinutesInjected = defaultAuthMinutesFromProperties;
        this.defaultDataLimitMbInjected = defaultDataLimitMbFromProperties;
        this.defaultDownloadKbpsInjected = defaultDownloadKbpsFromProperties;
        this.defaultUploadKbpsInjected = defaultUploadKbpsFromProperties;

        logger.info("UnifiAuthService inicializado.");
        logger.info("Valor de defaultSiteIdInjected (de '${unifi.default.site.id}'): '{}'", this.defaultSiteIdInjected);
    }

    private boolean isInvalidSiteId(String siteId) {
        return siteId == null ||
                siteId.trim().isEmpty() ||
                "null".equalsIgnoreCase(siteId.trim()) ||
                "${unifi.default.site.id}".equals(siteId);
    }

    public UnifiAuthServiceResponseDTO authorizeDevice(
            String clientMac,
            String siteIdFromParam,
            Integer minutesFromParam,
            Long dataLimitMbFromParam,
            Integer downloadSpeedKbpsFromParam,
            Integer uploadSpeedKbpsFromParam) {

        if (clientMac == null || clientMac.trim().isEmpty()) {
            logger.error("MAC do cliente não pode ser nulo ou vazio para autorização.");
            // CORRIGIDO: Use UnifiAuthServiceResponse.builder().build()
            return UnifiAuthServiceResponseDTO.builder()
                    .authorized(false)
                    .message("MAC do cliente não fornecido.")
                    .build();
        }

        String siteToUse;
        if (!isInvalidSiteId(siteIdFromParam)) {
            siteToUse = siteIdFromParam.trim();
            logger.info("Usando siteId fornecido como parâmetro para autorização: '{}'", siteToUse);
        } else {
            siteToUse = this.defaultSiteIdInjected;
            logger.info("Parâmetro siteId ('{}') não fornecido ou inválido, usando defaultSiteIdInjected: '{}'", siteIdFromParam, siteToUse);
        }

        if (isInvalidSiteId(siteToUse)) {
            logger.error("FALHA NA AUTORIZAÇÃO: O siteId final ('{}') a ser usado na chamada da API UniFi é NULO ou inválido. MAC do cliente: {}", siteToUse, clientMac);
            // CORRIGIDO: Use UnifiAuthServiceResponse.builder().build()
            return UnifiAuthServiceResponseDTO.builder()
                    .authorized(false)
                    .message("Site ID do UniFi não configurado ou inválido.")
                    .build();
        }

        Integer minutesToUse = (minutesFromParam != null) ? minutesFromParam : this.defaultAuthMinutesInjected;
        Long dataLimitToUse = (dataLimitMbFromParam != null) ? dataLimitMbFromParam : this.defaultDataLimitMbInjected;
        Integer downloadSpeedToUse = (downloadSpeedKbpsFromParam != null) ? downloadSpeedKbpsFromParam : this.defaultDownloadKbpsInjected;
        Integer uploadSpeedToUse = (uploadSpeedKbpsFromParam != null) ? uploadSpeedKbpsFromParam : this.defaultUploadKbpsInjected;

        logger.info("Tentando autorizar MAC '{}' no site UniFi '{}' por {} minutos.", clientMac, siteToUse, minutesToUse);

        ClientDTO client = unifiApiClient.getClientByMac(siteToUse, clientMac);

        // Inicia o Builder
        UnifiAuthServiceResponseDTO.UnifiAuthServiceResponseDTOBuilder serviceResponseBuilder = UnifiAuthServiceResponseDTO.builder()
                .authorized(false) // Default para false
                .message("Falha desconhecida na autorização.");


        if (client == null || client.getId() == null || client.getId().trim().isEmpty()) {
            logger.error("Não foi possível encontrar clientId (UUID) para MAC {} no site {}. O dispositivo pode não estar conectado ou visível para o controller UniFi ainda.", clientMac, siteToUse);
            serviceResponseBuilder.message("Dispositivo não encontrado ou não conectado ao UniFi.");
            return serviceResponseBuilder.build(); // Aqui já está correto
        }

        serviceResponseBuilder
                .deviceName(client.getName() != null ? client.getName() : client.getHostname())
                .deviceHostname(client.getHostname())
                .deviceOsName(client.getOsName())
                .deviceIpAddress(client.getIpAddress());

        String clientIdUuid = client.getId();

        RequestAuthorizeGuestDTO payloadParaApiV1 = RequestAuthorizeGuestDTO.builder()
                .action("AUTHORIZE_GUEST_ACCESS")
                .timeLimitMinutes(minutesToUse)
                .dataUsageLimitMBytes(dataLimitToUse)
                .rxRateLimitKbps(downloadSpeedToUse)
                .txRateLimitKbps(uploadSpeedToUse)
                .build();

        logger.info("Chamando UnifiApiClient.executeClientAction para UUID: '{}' no site: '{}' com payload (para API v1): {}", clientIdUuid, siteToUse, payloadParaApiV1);

        try {
            ResponseDTO unifiResponse = unifiApiClient.executeClientAction(
                    siteToUse,
                    clientIdUuid,
                    payloadParaApiV1
            );

            if (unifiResponse != null && unifiResponse.getMeta() != null && "ok".equalsIgnoreCase(unifiResponse.getMeta().getRc())) {
                logger.info("Dispositivo {} (UUID: {}) autorizado com sucesso no UniFi site {}.", clientMac, clientIdUuid, siteToUse);
                serviceResponseBuilder
                        .authorized(true)
                        .message(unifiResponse.getMeta().getMsg());
            } else {
                String errorMsg = (unifiResponse != null && unifiResponse.getMeta() != null) ? unifiResponse.getMeta().getMsg() : "Erro desconhecido da API UniFi";
                logger.error("Falha ao autorizar dispositivo {} (UUID: {}) no UniFi site {}. Razão do UnifiAuthService: {}. Dados da Resposta UniFi: {}", clientMac, clientIdUuid, siteToUse, errorMsg, unifiResponse != null ? unifiResponse.getData() : "N/A");
                serviceResponseBuilder.message("Falha na autorização UniFi: " + errorMsg);
            }
        } catch (Exception e) {
            logger.error("Exceção ao chamar UnifiApiClient.executeClientAction para MAC {}: {}", clientMac, e.getMessage(), e);
            serviceResponseBuilder.message("Erro interno ao se comunicar com o UniFi Controller: " + e.getMessage());
        }

        return serviceResponseBuilder.build();
    }

    public UnifiAuthServiceResponseDTO authorizeDevice(String clientMac) {
        logger.debug("Chamando authorizeDevice (método de conveniência com 1 arg) para MAC: {}. Usará valores padrão para site e limites.", clientMac);
        return authorizeDevice(clientMac, null, null, null, null, null);
    }

    public boolean unauthorizeDevice(String clientMac, String siteIdFromParam) {
        String siteToUse;
        if (!isInvalidSiteId(siteIdFromParam)) {
            siteToUse = siteIdFromParam.trim();
        } else {
            siteToUse = this.defaultSiteIdInjected;
        }
        if (isInvalidSiteId(siteToUse)) {
            logger.error("FALHA NA DESAUTORIZAÇÃO: siteId ('{}') para a API UniFi é NULO ou inválido. MAC: {}", siteToUse, clientMac);
            return false;
        }

        ClientDTO client = unifiApiClient.getClientByMac(siteToUse, clientMac);
        if (client == null || client.getId() == null || client.getId().trim().isEmpty()) {
            logger.error("Não foi possível encontrar clientId (UUID) para MAC {} no site {} para desautorização.", clientMac, siteToUse);
            return false;
        }
        String clientIdUuid = client.getId();

        RequestAuthorizeGuestDTO payloadParaApiV1 = RequestAuthorizeGuestDTO.builder()
                .action("UNAUTHORIZE_GUEST_ACCESS")
                .build();

        logger.info("Chamando UnifiApiClient.executeClientAction (unauthorize) para UUID: {} no site: {} com payload: {}", clientIdUuid, siteToUse, payloadParaApiV1);
        ResponseDTO unifiResponse = unifiApiClient.executeClientAction(siteToUse, clientIdUuid, payloadParaApiV1);

        if (unifiResponse != null && unifiResponse.getMeta() != null && "ok".equalsIgnoreCase(unifiResponse.getMeta().getRc())) {
            logger.info("Dispositivo {} (UUID: {}) desautorizado com sucesso no UniFi site {}.", clientMac, clientIdUuid, siteToUse);
            return true;
        } else {
            String errorMsg = (unifiResponse != null && unifiResponse.getMeta() != null) ? unifiResponse.getMeta().getMsg() : "Erro desconhecido do UnifiApiClient";
            logger.error("Falha ao desautorizar dispositivo {} (UUID: {}) no UniFi site {}. Razão: {}", clientMac, clientIdUuid, siteToUse, errorMsg);
            return false;
        }
    }

    public boolean unauthorizeDevice(String clientMac) {
        return unauthorizeDevice(clientMac, null);
    }
}