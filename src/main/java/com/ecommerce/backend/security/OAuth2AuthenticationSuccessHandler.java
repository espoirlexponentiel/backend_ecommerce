package com.ecommerce.backend.security;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    @Lazy
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            if (!(authentication.getPrincipal() instanceof OAuth2User principal)) {
                System.err.println("⚠️ [OAuth2] Principal n'est pas une instance de OAuth2User");
                response.sendRedirect("http://localhost:3000/login?error=oauth2_failed");
                return;
            }

            String email = principal.getAttribute("email");
            String name = principal.getAttribute("name");

            if (name == null || name.isBlank()) {
                name = email != null ? email.split("@")[0] : "Utilisateur";
            }

            if (email == null || email.isBlank()) {
                System.err.println("⚠️ [OAuth2] Email manquant dans le token Google");
                response.sendRedirect("http://localhost:3000/login?error=oauth2_failed");
                return;
            }

            Optional<User> existingUser = userRepository.findByEmail(email.trim().toLowerCase());
            User user;

            if (existingUser.isEmpty()) {
                user = new User();
                user.setEmail(email.trim().toLowerCase());
                user.setNom(name);
                user.setUsername(name);
                user.setRole("USER");
                user.setProvider("google");
                user.setPassword("");
                user = userRepository.save(user);
                System.out.println("✅ [OAuth2] Nouvel utilisateur Google créé : " + user.getEmail());
            } else {
                user = existingUser.get();
                if (user.getRole() == null || user.getRole().isBlank()) {
                    user.setRole("USER");
                }
                if (user.getProvider() == null || user.getProvider().isBlank()) {
                    user.setProvider("google");
                }
                user = userRepository.save(user);
                System.out.println("✅ [OAuth2] Utilisateur Google connecté : " + user.getEmail());
            }

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            String encodedName = URLEncoder.encode(user.getDisplayName() != null ? user.getDisplayName() : name, StandardCharsets.UTF_8);

            String redirectUrl = String.format(
                    "http://localhost:3000/login?token=%s&email=%s&role=%s&nom=%s",
                    token,
                    user.getEmail(),
                    user.getRole(),
                    encodedName
            );

            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8) : "oauth2_failed";
            response.sendRedirect("http://localhost:3000/login?error=" + msg);
        }
    }
}
