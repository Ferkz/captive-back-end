package dev.codingsales.Captive.mock;

import dev.codingsales.Captive.include.unifi.UnifiApiClient;
import dev.codingsales.Captive.include.unifi.dto.ClientDTO;
import dev.codingsales.Captive.include.unifi.dto.MetaDTO;
// Usando SEU RequestAuthorizeGuestDTO que você adaptou
import dev.codingsales.Captive.include.unifi.dto.RequestAuthorizeGuestDTO;
import dev.codingsales.Captive.include.unifi.dto.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Certifique-se que esta condição corresponde à sua propriedade em application.properties para ativar o mock
@ConditionalOnProperty(name = "unifiApi.controller.mock", havingValue = "true")
@Service
public class UnifiApiClientMock implements UnifiApiClient {
    private static final Logger logger = LoggerFactory.getLogger(UnifiApiClientMock.class);

    @Override
    public boolean login() {
        logger.info("MOCK UnifiApiClient: login() called (API key readiness check)");
        return true;
    }

    @Override
    public ClientDTO getClientByMac(String siteId, String macAddress) {
        logger.info("MOCK UnifiApiClient: getClientByMac() called for siteId: {}, macAddress: {}", siteId, macAddress);
        if (macAddress == null || macAddress.trim().isEmpty()) {
            return null;
        }
        ClientDTO mockClient = new ClientDTO();
        mockClient.setId(UUID.randomUUID().toString());
        mockClient.setMacAddress(macAddress.toLowerCase());
        mockClient.setName("Mock Device " + macAddress.substring(Math.max(0, macAddress.length() - 5)));
        mockClient.setIpAddress("192.168.1.123");
        mockClient.setType("WIRELESS");
        mockClient.setConnectedAt(java.time.Instant.now().toString());
        return mockClient;
    }

    @Override
    public ResponseDTO executeClientAction(String siteId, String clientIdUuid, RequestAuthorizeGuestDTO payload) {
        // Usando getCmd() do seu DTO existente
        String actionString = payload.getAction();

        logger.info("MOCK UnifiApiClient: executeClientAction() called for siteId: {}, clientIdUuid: {}, action: {}",
                siteId, clientIdUuid, actionString);

        ResponseDTO mockResponse = new ResponseDTO();
        MetaDTO meta = new MetaDTO();
        meta.setRc("ok");
        meta.setMsg("Mock action '" + actionString + "' executed successfully for client " + clientIdUuid);
        mockResponse.setMeta(meta);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("action", actionString); // Usando a string da ação original
        responseData.put("status", "success_mock");

        // Usando getMinutes() do seu DTO existente
        if ("authorize-guest".equalsIgnoreCase(actionString)) {
            responseData.put("timeLimitMinutes", payload.getTimeLimitMinutes());
        }
        mockResponse.setData(Collections.singletonList(responseData)); // Corrigido para setData
        return mockResponse;
    }
}