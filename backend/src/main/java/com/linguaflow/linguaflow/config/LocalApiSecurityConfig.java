
package com.linguaflow.linguaflow.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("local")
@ConditionalOnProperty(
        name = "app.dev.allow-seed-creator",
        havingValue = "true"
)
public class LocalApiSecurityConfig {

    @Bean
    public SecurityFilterChain localApiSecurityFilterChain(
            HttpSecurity http) throws Exception {

        http
            // CSRF configuration for local development
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                request -> {

                    String method = request.getMethod();
                    String path = request.getServletPath();

                    // POST - Create tourist site
                    boolean createSite =
                            "POST".equals(method)
                            && "/api/tourist-sites".equals(path);

                    // PUT - Update tourist site
                    boolean updateSite =
                            "PUT".equals(method)
                            && path.matches("/api/tourist-sites/[0-9]+");

                    // DELETE - Delete tourist site
                    boolean deleteSite =
                            "DELETE".equals(method)
                            && path.matches("/api/tourist-sites/[0-9]+");

                    // POST - Create QR Code
                    boolean createQrCode =
                            "POST".equals(method)
                            && "/api/qr-codes".equals(path);

                    // PUT - Update QR Code status
                    boolean updateQrStatus =
                            "PUT".equals(method)
                            && path.matches("/api/qr-codes/[0-9]+/status");

                    // DELETE - Delete QR Code
                    boolean deleteQrCode =
                            "DELETE".equals(method)
                            && path.matches("/api/qr-codes/[0-9]+");

                    // POST - Record QR Code scan history
                    boolean recordQrScan =
                            "POST".equals(method)
                            && path.matches("/api/qr-codes/[0-9]+/scan");

                    return createSite
                            || updateSite
                            || deleteSite
                            || createQrCode
                            || updateQrStatus
                            || deleteQrCode
                            || recordQrScan;
                }
            ))

            // API access permissions
            .authorizeHttpRequests(auth -> auth

                // Public - Resolve QR Code
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/qr-codes/*/resolve"
                ).permitAll()

                // Public - Record QR Code scan
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/qr-codes/*/scan"
                ).permitAll()

                // All other APIs require authentication
                .anyRequest().authenticated()
            )

            // HTTP Basic for local development
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
