package dev.codingsales.Captive.exeption;

import org.springframework.http.ResponseEntity;
import dev.codingsales.Captive.dto.response.ErrorResponseDTO;

public abstract class HandledException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Gets the response body.
     *
     * @return the response body
     */
    public abstract ResponseEntity<ErrorResponseDTO> getResponseEntity();

    /**
     * Instantiates a new handled exception.
     *
     * @param message the message
     */
    HandledException(String message) {
        super(message);
    }
}
