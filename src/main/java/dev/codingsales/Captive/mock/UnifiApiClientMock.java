package dev.codingsales.Captive.mock;

import dev.codingsales.Captive.include.unifi.UnifiApiClient;
import dev.codingsales.Captive.include.unifi.dto.ClientDTO;
import dev.codingsales.Captive.include.unifi.dto.MetaDTO;
import dev.codingsales.Captive.include.unifi.dto.RequestAuthorizeGuestDTO;
import dev.codingsales.Captive.include.unifi.dto.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@ConditionalOnProperty(name = "unifiApi.controller.mock", havingValue = "true")
@Service
@Primary
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

        if ("authorize-guest".equalsIgnoreCase(actionString)) {
            responseData.put("timeLimitMinutes", payload.getTimeLimitMinutes());
        }
        mockResponse.setData(Collections.singletonList(responseData)); // Corrigido para setData
        return mockResponse;
    }
    @Override
    public String generateUniFiPostAuthRedirectUrl(String siteId) {
        // Para um mock, podemos retornar uma URL fixa que simule o redirecionamento para o Google.
        String encodedGoogleUrl = URLEncoder.encode("http://www.google.com", StandardCharsets.UTF_8);
        String mockRedirectUrl = String.format("http://mock-unifi-controller.com/guest/s/%s/status?authed=true&url=%s",
                siteId, encodedGoogleUrl);
        logger.info("MOCK UnifiApiClient: generateUniFiPostAuthRedirectUrl() called for siteId: {}. Returning mock URL: {}", siteId, mockRedirectUrl);
        return mockRedirectUrl;
    }
}