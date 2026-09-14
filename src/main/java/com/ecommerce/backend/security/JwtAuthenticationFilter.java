package com.ecommerce.backend.security;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    /**
     * ✅ Exclure certaines routes du filtre JWT
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/uploads/")              // Images publiques
            || path.startsWith("/api/users/login")       // Login
            || path.startsWith("/api/users/register")    // Register
            || path.startsWith("/oauth2/");              // OAuth2
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // ✅ Si pas de token, continuer sans bloquer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            if (!jwtUtil.isTokenExpired(token)) {
                final String email = jwtUtil.extractUsername(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = userRepository.findByEmail(email).orElse(null);

                    if (user != null && jwtUtil.isTokenValid(token, user)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        System.out.println("⚠️ [JWT] User not found or token invalid for email: " + email + ", user in DB: " + (user != null));
                    }
                }
            } else {
                System.out.println("⚠️ [JWT] Token is expired");
            }
        } catch (Exception e) {
            System.err.println("⚠️ [JWT] Filter Exception: " + e.getMessage());
        }

        // ✅ Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}
