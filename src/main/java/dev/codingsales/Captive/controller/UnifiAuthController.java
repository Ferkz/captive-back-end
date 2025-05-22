package dev.codingsales.Captive.controller;

import dev.codingsales.Captive.dto.unfi.UnifiLoginRequest;
// Ensure the UnifiAuthService injected here is the one you intend.
// If it's the one adapted for X-API-KEY, it won't have loginToUnifiController.
import dev.codingsales.Captive.service.impl.UnifiAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth") // This path might conflict if you have another controller for /api/authenticate
public class UnifiAuthController {
    private final UnifiAuthService authService; // This is now the X-API-KEY focused service

    public UnifiAuthController(UnifiAuthService authService){ // Make sure correct bean is injected
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UnifiLoginRequest request) {
        // The injected 'authService' (if it's the one we adapted for X-API-KEY)
        // no longer has 'loginToUnifiController(username, password)'.
        // This endpoint is for the old cookie-based UniFi controller login.
        // If you need this functionality, you need a different service that implements it.

        // To make it compile, I am commenting this out.
        // You need to decide if this controller and its purpose are still valid.
        /*
        try {
            String response = authService.loginToUnifiController( // This method does not exist on the new authService
                    request.getUsername(),
                    request.getPassword()
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro no login: " + e.getMessage());
        }
        */
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .body("This UniFi controller login endpoint is not available with the current API Key authentication method.");
    }
}