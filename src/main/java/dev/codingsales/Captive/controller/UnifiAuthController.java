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
@RequestMapping("/auth")
public class UnifiAuthController {
    private final UnifiAuthService authService; // This is now the X-API-KEY focused service

    public UnifiAuthController(UnifiAuthService authService){ // Make sure correct bean is injected
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UnifiLoginRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .body("This UniFi controller login endpoint is not available with the current API Key authentication method.");
    }
}