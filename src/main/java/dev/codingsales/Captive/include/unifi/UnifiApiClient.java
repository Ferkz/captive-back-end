package dev.codingsales.Captive.include.unifi;

import dev.codingsales.Captive.include.unifi.dto.ClientDTO;
import dev.codingsales.Captive.include.unifi.dto.RequestAuthorizeGuestDTO;
import dev.codingsales.Captive.include.unifi.dto.ResponseDTO;

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
}