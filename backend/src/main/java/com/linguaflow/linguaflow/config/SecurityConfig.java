package com.linguaflow.linguaflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .formLogin(formLogin -> formLogin.disable())
                                .httpBasic(Customizer.withDefaults())
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(
                                                                "/api/v1/public/**",
                                                                "/api/v1/sync/poi",
                                                                "/api/v1/tourist-sites/qr/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .build();
        }

        @Bean
        public UserDetailsService userDetailsService() {
                UserDetails admin = User.withUsername("admin")
                                .password("{noop}123456")
                                .roles("ADMIN")
                                .build();

                return new InMemoryUserDetailsManager(admin);
        }
}