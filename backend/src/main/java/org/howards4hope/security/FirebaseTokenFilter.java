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

            // If Firebase was initialized successfully, verify the real JWT token
            if (!FirebaseApp.getApps().isEmpty()) {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                email = decodedToken.getEmail();
                // Simple email rule or custom claim check for administrative roles
                isAdmin = email != null && (email.contains("admin") || email.equals("avlorycorp@gmail.com"));
            } else {
                // MOCK AUTHENTICATION FALLBACK (For rapid local developer testing without service-account.json!)
                // Decodes standard mock token formats
                if (token.startsWith("mock-admin:")) {
                    email = token.substring("mock-admin:".length());
                    isAdmin = true;
                } else if (token.startsWith("mock-user:")) {
                    email = token.substring("mock-user:".length());
                    isAdmin = false;
                } else if (token.startsWith("mock-admin")) {
                    email = "admin@howards4hope.org";
                    isAdmin = true;
                } else if (token.startsWith("mock-user")) {
                    email = "user@gmail.com";
                    isAdmin = false;
                }
            }

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
