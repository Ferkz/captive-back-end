package dev.codingsales.Captive.mock;

import dev.codingsales.Captive.include.unifi.UnifiApiClient;
import dev.codingsales.Captive.include.unifi.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


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

        // ===========================================
        // ===== LINHA ADICIONADA PARA A CORREÇÃO ====
        // ===========================================
        mockClient.setOsName("MockOS"); // Adiciona um valor de teste para o SO
        // ===========================================

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

    @Override
    public List<UnifiDeviceDTO> listDevices(String siteId) {
        logger.info("MOCK UnifiApiClient: listDevices() called for siteId: {}", siteId);
        List<UnifiDeviceDTO> mockDevices = new ArrayList<>();

        UnifiDeviceDTO device1 = new UnifiDeviceDTO();
        device1.setId(UUID.randomUUID().toString());
        device1.setName("Mock AP Sala");
        device1.setModel("UAP-AC-PRO");
        device1.setMacAddress("AA:BB:CC:DD:EE:01");
        device1.setIpAddress("192.168.1.10");
        device1.setState("ONLINE");
        device1.setFirmwareVersion("6.5.28");
        device1.setAdoptedAt(new Date());

        UnifiDeviceDTO device2 = new UnifiDeviceDTO();
        device2.setId(UUID.randomUUID().toString());
        device2.setName("Mock Switch Corredor");
        device2.setModel("USW-8-POE");
        device2.setMacAddress("AA:BB:CC:DD:EE:02");
        device2.setIpAddress("192.168.1.11");
        device2.setState("OFFLINE");
        device2.setFirmwareVersion("6.4.18");
        device2.setAdoptedAt(new Date());

        mockDevices.add(device1);
        mockDevices.add(device2);

        return mockDevices;
    }

    @Override
    public UnifiDeviceDTO getDeviceDetails(String siteId, String deviceId) {
        logger.info("MOCK UnifiApiClient: getDeviceDetails() called for siteId: {}, deviceId: {}", siteId, deviceId);
        UnifiDeviceDTO device = new UnifiDeviceDTO();
        device.setId(deviceId);
        device.setName("Mock AP Detalhado");
        device.setModel("U6-LR");
        device.setMacAddress("AA:BB:CC:DD:EE:03");
        device.setIpAddress("192.168.1.12");
        device.setState("ONLINE");
        device.setFirmwareVersion("6.6.55");
        device.setAdoptedAt(new Date());
        return device;
    }

    @Override
    public void restartDevice(String siteId, String deviceId) {
        logger.info("MOCK UnifiApiClient: restartDevice() called for siteId: {}, deviceId: {}", siteId, deviceId);
        // Em um mock, não fazemos nada além de logar a ação.
    }

    @Override
    public void powerCyclePort(String siteId, String deviceId, int portIndex) {
        logger.info("MOCK UnifiApiClient: powerCyclePort() called for siteId: {}, deviceId: {}, portIndex: {}", siteId, deviceId, portIndex);
        // Em um mock, não fazemos nada além de logar a ação.
    }
}
