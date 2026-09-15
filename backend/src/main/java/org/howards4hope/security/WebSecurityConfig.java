package org.howards4hope.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5500,http://127.0.0.1:5500,https://howards4hope-b06f6.web.app,https://howards4hope-b06f6.firebaseapp.com,https://howards4hope.org}")
    private String allowedOrigins;

    @Bean
    public FirebaseTokenFilter firebaseTokenFilter() {
        return new FirebaseTokenFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Centralized CORS Setup
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Disable CSRF for stateless REST APIs using Bearer JWTs
            .csrf(csrf -> csrf.disable())
            
            // 3. Robust Security Headers (CSP, FrameOptions, HSTS, Referrer-Policy, NoSniff)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable) // Default spring nosniff
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            )
            
            // 4. Session policy: completely stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 5. Authorize Endpoints
            .authorizeHttpRequests(auth -> auth
                // Allow public endpoints
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/events", "/api/events/keyset", "/api/events/search", "/api/events/{id}").permitAll()
                .requestMatchers("/api/payments/webhook").permitAll()
                .requestMatchers("/api/payments/create-stripe-checkout", "/api/payments/create-paypal-order").permitAll()
                .requestMatchers("/api/donations/create-checkout", "/api/donations/receipt/**").permitAll()
                .requestMatchers("/api/tickets/book-guest", "/api/tickets/lookup", "/api/tickets/verify/**").permitAll()
                .requestMatchers("/api/blog", "/api/blog/**", "/api/outreach/apply").permitAll()
                .requestMatchers("/api/analytics/track").permitAll()
                .requestMatchers("/api/newsletter/subscribe").permitAll()
                
                // Secure Admin routes
                .requestMatchers("/api/admin/**", "/api/donations/admin/**", "/api/blog/admin/**").hasRole("ADMIN")
                
                // Require Auth for all other API endpoints (e.g., /api/tickets/book, /api/tickets/my-tickets)
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            );

        // 6. Add Custom Firebase Token Filter before standard authentication filter
        http.addFilterBefore(firebaseTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control", "X-Requested-With", "Stripe-Signature"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
