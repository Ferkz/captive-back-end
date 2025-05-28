package dev.codingsales.Captive.service.impl;



import java.sql.Timestamp;

import java.util.Map;

import java.util.concurrent.TimeUnit;



import dev.codingsales.Captive.include.unifi.dto.RequestAuthorizeGuestDTO;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;



import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;



import dev.codingsales.Captive.dto.captivelportal.AuthorizeDeviceRequestDTO; // This is your existing DTO from the controller

import dev.codingsales.Captive.dto.captivelportal.SessionInfoDTO;

import dev.codingsales.Captive.entity.Session;

import dev.codingsales.Captive.exeption.NoContentException;

import dev.codingsales.Captive.include.unifi.UnifiApiClient;

import dev.codingsales.Captive.include.unifi.dto.ClientDTO; // To get the UUID

import dev.codingsales.Captive.include.unifi.dto.ResponseDTO; // UniFi API client response

import dev.codingsales.Captive.service.CaptivePortalService;

import dev.codingsales.Captive.service.SessionService;

import dev.codingsales.Captive.util.LoggerConstants; // Assuming this has NOT_FOUND_EXCEPTION



@Service

public class CaptivePortalServiceImpl implements CaptivePortalService {

    private static final Logger logger = LoggerFactory.getLogger(CaptivePortalServiceImpl.class); // SLF4J Logger



    @Autowired

    private SessionService sessionService;



    @Autowired

    private UnifiApiClient unifiApiClient; // This will be the new X-API-KEY implementation



// Values from your application.properties, ensure they are correctly prefixed if needed

// e.g. unifi.default.auth.minutes VS unifiApi.controller.session.duration

// I'll use the ones that seem to match the /integration/v1/ payload best.

    @Value("${unifi.default.auth.minutes}") // Using this for consistency with my UnifiAuthService

    private Integer sessionDurationMinutes; // Changed to Integer to match RequestAuthorizeGuestDTOIntegrationV1



    @Value("${unifi.default.auth.download.kbps:#{null}}") // Allow null if not set

    private Integer downloadSpeedKbps;



    @Value("${unifi.default.auth.upload.kbps:#{null}}") // Allow null if not set

    private Integer uploadSpeedKbps;



    @Value("${unifi.default.auth.data.limit.mb:#{null}}") // Allow null if not set

    private Long dataUsageLimitMBytes; // Renamed for clarity



// These seem to be for calculating local session expiry, not directly for UniFi payload

    @Value("${unifiApi.controller.session.hiddenMinutes:0}") // Default to 0 if not present

    private Long sessionHiddenMinutes;



    @Value("${unifiApi.controller.session.blockMinutes:0}") // Default to 0 if not present

    private Long sessionBlockMinutes;





    @Override

    public SessionInfoDTO authorizeDevice(AuthorizeDeviceRequestDTO request) throws Exception {

// AuthorizeDeviceRequestDTO is from your existing controller. It contains macAddress, email etc.

        logger.info("Authorizing device: MAC={}, IP={}, AP_MAC={}, Email={}",

                request.getMacAddress(), request.getIpAddress(), request.getAccessPointMacAddress(), request.getEmail());



        if (this.sessionService.existsByDeviceMac(request.getMacAddress())) {

            logger.info("Device {} already has an active session. Returning existing session info.", request.getMacAddress());

            return this.getSessionInfo(request.getMacAddress());

        }



// Step 1: Get Client UUID from UniFi

        ClientDTO unifiClient = unifiApiClient.getClientByMac(

                "default", // Assuming "default" site, or get from properties: @Value("${unifi.default.site.id}")

                request.getMacAddress()

        );



        if (unifiClient == null || unifiClient.getId() == null) {

            logger.error("Could not retrieve UniFi client ID (UUID) for MAC: {}. Authorization cannot proceed.", request.getMacAddress());

            throw new RuntimeException("Failed to find device " + request.getMacAddress() + " on UniFi controller. Ensure device is connected.");

        }

        String clientIdUuid = unifiClient.getId();



// Step 2: Prepare payload for UniFi client action API

        RequestAuthorizeGuestDTO unifiPayload = RequestAuthorizeGuestDTO.builder()

                .action("AUTHORIZE_GUEST_ACCESS")

                .timeLimitMinutes(this.sessionDurationMinutes)

                .rxRateLimitKbps(this.downloadSpeedKbps)

                .txRateLimitKbps(this.uploadSpeedKbps)

                .dataUsageLimitMBytes(this.dataUsageLimitMBytes)

                .build();



        logger.info("Attempting to authorize UniFi client UUID: {} with payload: {}", clientIdUuid, unifiPayload);

        ResponseDTO unifiResponse = unifiApiClient.executeClientAction(

                "default", // siteId

                clientIdUuid,

                unifiPayload

        );



        if (unifiResponse != null && unifiResponse.getMeta() != null && "ok".equalsIgnoreCase(unifiResponse.getMeta().getRc())) {

            logger.info("UniFi controller successfully authorized client UUID: {}. Creating local session.", clientIdUuid);

            Session session = generateSession(

                    request.getMacAddress(),

                    request.getIpAddress(),

                    request.getAccessPointMacAddress(), // This comes from AuthorizeDeviceRequestDTO

                    request.getBrowser(),

                    request.getOperatingSystem());

            return generateSessionInfo(sessionService.addSession(session));

        } else {

            String errMsg = (unifiResponse != null && unifiResponse.getMeta() != null) ? unifiResponse.getMeta().getMsg() : "Unknown error";

            logger.error("UniFi Controller failed to authorize guest UUID: {}. Reason: {}. UniFi Response Data: {}",

                    clientIdUuid, errMsg, unifiResponse != null ? unifiResponse.getData() : "N/A");

            throw new RuntimeException("UniFi Controller could not authorize guest. Details: " + errMsg);

        }

    }



    @Override

    public SessionInfoDTO getSessionInfo(String macAddress) throws NoContentException {

        try {

            Session session = sessionService.findByDeviceMac(macAddress);

            return generateSessionInfo(session);

        } catch (NoContentException e) {

            String error = String.format(LoggerConstants.NOT_FOUND_EXCEPTION, "CaptivePortalServiceImpl",

                    "getSessionInfo", "session for MAC: " + macAddress, ""); // Adjusted message

            logger.error(error);

            throw e;

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



    private Session generateSession(String macAddress, String ipAddress, String accessPointMac, String browser,

                                    String operatingSystem) {

        Session session = new Session();

        session.setBrowser(browser);

        session.setOperatingSystem(operatingSystem);

        session.setDeviceMac(macAddress);

        session.setAccesspointMac(accessPointMac != null ? accessPointMac : "N/A"); // Handle null AP MAC

        session.setDeviceIp(ipAddress);



// Use sessionDurationMinutes (which should be Integer)

        int durationMinutesActual = (this.sessionDurationMinutes != null) ? this.sessionDurationMinutes : 0;



        Timestamp lastLogin = new Timestamp(System.currentTimeMillis());

        Timestamp expireDate = new Timestamp(lastLogin.getTime());



// Calculate actual visible expiry for user (total duration - hidden minutes)

        long visibleDurationMillis = TimeUnit.MINUTES.toMillis(durationMinutesActual);

        if (this.sessionHiddenMinutes != null && this.sessionHiddenMinutes > 0) {

            visibleDurationMillis -= TimeUnit.MINUTES.toMillis(this.sessionHiddenMinutes);

        }

        expireDate.setTime(lastLogin.getTime() + Math.max(0, visibleDurationMillis)); // Ensure not negative



// Calculate when the session record should be removed (total duration + block minutes)

        Timestamp removeDate = new Timestamp(lastLogin.getTime() + TimeUnit.MINUTES.toMillis(durationMinutesActual));

        if (this.sessionBlockMinutes != null && this.sessionBlockMinutes > 0) {

            removeDate.setTime(removeDate.getTime() + TimeUnit.MINUTES.toMillis(this.sessionBlockMinutes));

        } else { // If no block minutes, set removeDate same as expireDate or slightly after.

            removeDate.setTime(expireDate.getTime() + TimeUnit.MINUTES.toMillis(1)); // Remove 1 min after visible expiry

        }



        session.setLastLoginOn(lastLogin);

        session.setExpireLoginOn(expireDate);

        session.setRemoveSessionOn(removeDate);

        return session;

    }

}