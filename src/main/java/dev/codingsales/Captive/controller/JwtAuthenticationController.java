package dev.codingsales.Captive.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dev.codingsales.Captive.security.JwtRequest;
import dev.codingsales.Captive.security.JwtResponse;
import dev.codingsales.Captive.security.JwtTokenUtil;
import dev.codingsales.Captive.security.JwtUserDetailsService;
@RestController
@CrossOrigin
public class JwtAuthenticationController {

    /** The authentication manager. */
    @Autowired
    private AuthenticationManager authenticationManager;

    /** The jwt token util. */
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /** The user details service. */
    @Autowired
    private JwtUserDetailsService userDetailsService;

    /**
     * Creates the authentication token.
     *
     * @param authenticationRequest the authentication request
     * @return the response entity
     * @throws Exception the exception
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationController.class);

    @RequestMapping(value = "/api/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        System.out.println("Token gerado com sucesso para: " + userDetails.getUsername());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    /**
     * Authenticate.
     *
     * @param username the username
     * @param password the password
     * @throws Exception the exception
     */
    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
