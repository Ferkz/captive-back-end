package dev.codingsales.Captive.config;

import dev.codingsales.Captive.security.JwtAuthenticationEntryPoint;
import dev.codingsales.Captive.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import java.util.Arrays; // Adicionado para Arrays.asList se necessário

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    /**


    private static final String[] SWAGGER_PATTERNS = {
            "/v2/api-docs", // Endpoint exato
            "/v3/api-docs", // Endpoint exato
            "/v3/api-docs/**", // Sub-paths para v3, como swagger-config
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html", // A página HTML principal
            "/swagger-ui/**",   // Recursos da UI do Swagger
            "/webjars/**"
    };

    private static final String[] APP_PUBLIC_PATTERNS = {
            "/api/authenticate",
            "/captive/portal/**",
            "/portal/guest/**",
            "/api/config/vars/**",
            "/error"
    };

    private static final String[] H2_CONSOLE_PATTERNS = {
            "/h2-console/**"
    };

    private static final String[] RESOURCE_PATTERNS = {
            // Se "/resource" é um endpoint exato:
            // "/resource",
            // Se "/resource" e tudo abaixo dele:
            "/resource/**"
    };

     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /**String[] publicEndpoints = Stream.of(
                        STATIC_RESOURCES_PATTERNS,
                        SWAGGER_PATTERNS,
                        APP_PUBLIC_PATTERNS,
                        H2_CONSOLE_PATTERNS,
                        RESOURCE_PATTERNS // Certifique-se que este está correto para seu uso
                ).flatMap(Stream::of)
                .toArray(String[]::new);
         */
        // Teste 1: Apenas APP_PUBLIC_PATTERNS (além do /api/authenticate e /error)
        String[] appPublic = {
                "/captive/portal/**",
                "/portal/guest/**",
                "/api/config/vars/**",
        };
        String[] basePublic = {"/api/authenticate", "/error"};
        String[] currentTestPublicEndpoints = Stream.concat(Arrays.stream(basePublic), Arrays.stream(appPublic))
                .toArray(String[]::new);
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(currentTestPublicEndpoints).permitAll()
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:4200/"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization", "Cache-Control", "Content-Type", "X-API-KEY",
                "X-Requested-With", "Origin", "Accept", "Access-Control-Allow-Origin",
                "Access-Control-Allow-Headers", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}