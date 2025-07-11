package dev.codingsales.Captive.include.unifi;

import dev.codingsales.Captive.include.unifi.dto.ClientDTO;
import dev.codingsales.Captive.include.unifi.dto.RequestAuthorizeGuestDTO;
import dev.codingsales.Captive.include.unifi.dto.ResponseDTO;
import dev.codingsales.Captive.include.unifi.dto.UnifiDeviceDTO;

import java.util.List;
import java.util.Map;

public interface UnifiApiClient {
    /**
     *
     * @return
     */
    boolean login();
    /**
     * *
     * @param siteId
     * @param macAddress
     * @return
     */
    ClientDTO getClientByMac(String siteId, String macAddress);
    /**
     *
     * @param siteId o ID do site UNIFi
     * @param clientIdUuid UUID do cliente
     * @param payload o corpo da requisicao contendo acao e parametros
     * @return
     */
    ResponseDTO executeClientAction(String siteId, String clientIdUuid, RequestAuthorizeGuestDTO payload);

    String generateUniFiPostAuthRedirectUrl(String siteId);
    List<UnifiDeviceDTO> listDevices(String siteId);
    UnifiDeviceDTO getDeviceDetails(String siteId, String deviceId);
    void restartDevice(String siteId, String deviceId);
    void powerCyclePort(String siteId, String deviceId, int portIndex);
}