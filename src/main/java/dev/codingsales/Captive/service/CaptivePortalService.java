package dev.codingsales.Captive.service;

import dev.codingsales.Captive.dto.captivelportal.AuthorizeDeviceRequestDTO;
import dev.codingsales.Captive.dto.captivelportal.SessionInfoDTO;
import dev.codingsales.Captive.exeption.NoContentException;

public interface CaptivePortalService {
    /**
     * Authorize device.
     *
     * @param request the request
     * @return the session info DTO
     * @throws Exception
     * @throws NoContentException the not found exception
     */
    public SessionInfoDTO authorizeDevice(AuthorizeDeviceRequestDTO request) throws Exception;


    /**
     * Gets the session info.
     *
     * @param macAddress the mac address
     * @return the session info
     * @throws NoContentException the not found exception
     */
    public SessionInfoDTO getSessionInfo(String macAddress) throws NoContentException;
}
