package dev.codingsales.Captive.security;

import java.io.IOException;
import java.io.Serializable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7858869558953243875L;

    /**
     * Commence.
     *
     * @param request the request
     * @param response the response
     * @param authException the auth exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

}
