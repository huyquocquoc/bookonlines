package com.bookstore.config;

import com.bookstore.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for book-service
 * Configures JWT-based authentication and role-based authorization
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF as we're using JWT tokens
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - allow all
                        .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        
                        // POST /api/books - allow all (can be restricted later if needed)
                        .requestMatchers(HttpMethod.POST, "/api/books").permitAll()
                        
                        // PUT and DELETE require authentication (role check in controller)
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").authenticated()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                
                // Stateless session management
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    
}

// Made with Bob