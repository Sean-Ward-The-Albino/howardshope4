package org.howards4hope.security;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FirebaseTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            String email = null;
            boolean isAdmin = false;

            // Verify Firebase Admin SDK is initialized
            if (FirebaseApp.getApps().isEmpty()) {
                logger.error("Firebase Admin SDK not initialized — cannot verify JWT token. "
                        + "Ensure firebase-service-account.json is on the classpath.");
                filterChain.doFilter(request, response);
                return;
            }

            // Verify the real Firebase JWT token (RS256 signature verification)
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            email = decodedToken.getEmail();

            // Use Firebase Custom Claims for secure role-based access control
            // Admin claims are set via the set-admin.js script using Firebase Admin SDK
            Object adminClaim = decodedToken.getClaims().get("admin");
            isAdmin = Boolean.TRUE.equals(adminClaim);

            if (email != null) {
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                if (isAdmin) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email, null, authorities);
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            logger.error("Failed to authenticate Firebase Token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
