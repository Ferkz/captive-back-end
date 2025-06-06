/**
package dev.codingsales.Captive.controller;

import dev.codingsales.Captive.dto.response.ErrorResponseDTO;
import dev.codingsales.Captive.dto.response.SuccessResponseDTO;
import dev.codingsales.Captive.mapper.AccessLogMapper;
import dev.codingsales.Captive.security.JwtTokenUtil;
import dev.codingsales.Captive.security.JwtUserDetailsService;
import dev.codingsales.Captive.service.AccessLogService;
import dev.codingsales.Captive.dto.item.AccessLogDTO; // E este DTO

import dev.codingsales.Captive.service.impl.UnifiAuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest; // Para jakarta
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/captive/portal")
@CrossOrigin(origins = "*") // Considere restringir em produção
public class CaptivePortalController {

    private static final Logger logger = LoggerFactory.getLogger(CaptivePortalController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private UnifiAuthService unifiAuthService;

    @Autowired
    private AccessLogService accessLogService;

    @PostMapping("/login")
    public ResponseEntity<?> captivePortalLogin(@RequestParam String username,
                                                @RequestParam String password,
                                                @RequestParam(name = "mac") String clientMac,
                                                @RequestParam(name = "ap", required = false) String apMac,
                                                @RequestParam(name = "ssid", required = false) String ssid,
                                                HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("Captive portal login request for user: {}, client MAC: {}, AP: {}, SSID: {}",
                username, clientMac, apMac, ssid);

        if (clientMac == null || clientMac.trim().isEmpty()) {
            logger.warn("Client MAC address not provided.");
            // Usando seus DTOs de resposta
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                    HttpStatus.BAD_REQUEST.value(),
                    "MAC Address Missing",
                    "Client MAC address is required."));
        }

        try {
            // 1. Autenticar usuário do portal (username/password)
            authenticate(username, password);
            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 2. Autorizar o MAC address no UniFi
            boolean unifiAuthorized = unifiAuthService.authorizeDevice(clientMac);

            if (unifiAuthorized) {
                final String token = jwtTokenUtil.generateToken(userDetails); // Gerar token JWT

                // 3. Registrar log de acesso (adaptar para seu AccessLogDTO e serviço)
                try {
                    // Supondo que seu AccessLogDTO e serviço aceitem estes campos
                    AccessLogDTO logDto = new AccessLogDTO();
                     logDto.setDeviceMac(clientMac);
                     logDto.setDeviceIp(request.getRemoteAddr());
                     logDto.setAccesspointMac(apMac);
                     logDto.setLastLoginOn(new Date());
                     //logDto.setExpireLoginOn();
                    //logDto.setRemoveSessionOn(...); Calcule
                     logDto.setBrowser(dev.codingsales.Captive.util.UserAgentUtils.getBrowser(request));
                     logDto.setOperatingSystem(dev.codingsales.Captive.util.UserAgentUtils.getOperatingSystem(request));
                     accessLogService.addAccessLog(AccessLogMapper.INSTANCE.accessLogDTOToAccessLog(logDto));
                    logger.info("Access log would be created here for MAC: " + clientMac);
                } catch (Exception e) {
                    logger.error("Error recording access log for captive portal: " + e.getMessage(), e);
                }

                Map<String, Object> responsePayload = new HashMap<>();
                responsePayload.put("message", "Access granted!");
                responsePayload.put("mac_address", clientMac);
                responsePayload.put("username", username);
                responsePayload.put("token", token); // Enviar token para o frontend

                long endTime = System.currentTimeMillis();
                logger.info("Login for {} (MAC: {}) successful. UniFi authorized. Time: {} ms", username, clientMac, (endTime - startTime));
                return ResponseEntity.ok(new SuccessResponseDTO(HttpStatus.OK.value(), "Login Successful", responsePayload));
            } else {
                logger.warn("Failed to authorize device MAC {} in UniFi for user {}.", clientMac, username);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponseDTO(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "UniFi Authorization Failed",
                                "Could not grant internet access via UniFi controller."));
            }

        } catch (BadCredentialsException e) {
            logger.warn("Invalid credentials for user {} in captive portal login.", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDTO(
                            HttpStatus.UNAUTHORIZED.value(),
                            "Authentication Failed",
                            "Invalid username or password."));
        } catch (DisabledException e) {
            logger.warn("User {} is disabled.", username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponseDTO(
                            HttpStatus.FORBIDDEN.value(),
                            "Account Disabled",
                            "This user account is disabled."));
        } catch (Exception e) { // Captura outras exceções da autenticação ou do UserDetailsService
            logger.error("Authentication error during captive portal login for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDTO(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Server Error",
                            "An internal error occurred during authentication: " + e.getMessage()));
        }
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            logger.warn("Authentication failed for {}: USER_DISABLED", username);
            throw e; // Re-throw para ser capturado pelo handler do controller
        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for {}: INVALID_CREDENTIALS", username);
            throw e; // Re-throw
        }
    }

    // Endpoint de Logout (opcional, mas recomendado)
    @PostMapping("/logout")
    public ResponseEntity<?> captivePortalLogout(@RequestParam(name = "mac") String clientMac,
                                                 HttpServletRequest request) {
        logger.info("Captive portal logout request for client MAC: {}", clientMac);
        if (clientMac == null || clientMac.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                    HttpStatus.BAD_REQUEST.value(),
                    "MAC Address Missing",
                    "Client MAC address is required for logout."));
        }

        boolean unifiUnAuthorized = unifiAuthService.unauthorizeDevice(clientMac);

        if (unifiUnAuthorized) {
            // Registrar log de logout, se necessário
            logger.info("Device MAC {} successfully unauthorized from UniFi.", clientMac);
            return ResponseEntity.ok(new SuccessResponseDTO(HttpStatus.OK.value(), "Logout Successful", "Device has been deauthorized."));
        } else {
            logger.warn("Failed to unauthorize device MAC {} in UniFi.", clientMac);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDTO(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "UniFi Deauthorization Failed",
                            "Could not deauthorize the device via UniFi controller."));
        }
    }
}

 */