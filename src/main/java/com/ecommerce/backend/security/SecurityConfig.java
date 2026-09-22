package com.ecommerce.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @org.springframework.beans.factory.annotation.Value("${frontend.url:https://polyshop-interface.onrender.com}")
    private String frontendUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults()) // ✅ Autorise les requêtes cross-origin (CORS)
            .csrf(csrf -> csrf.disable()) // ✅ Désactive CSRF pour les API REST
            .authorizeHttpRequests(auth -> auth
                // 🔓 Autoriser les pré-requêtes CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 🔓 Routes publiques d'authentification
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/users/login").permitAll()
                .requestMatchers("/api/users/oauth2/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/oauth2/**").permitAll()

                // 🔓 Accès libre aux produits, catégories, marchés et paramètres du site
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/markets/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/settings/**").permitAll()

                // 🔓 Images accessibles sans authentification
                .requestMatchers("/uploads/**").permitAll()

                // 🔐 Routes protégées
                .requestMatchers("/api/orders/**").authenticated()
                .requestMatchers("/api/cart/**").authenticated()

                // 🔐 Toute autre requête nécessite une authentification
                .anyRequest().authenticated()
            )
            // 🔐 Configuration OAuth2
            .oauth2Login(oauth -> oauth
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    System.err.println("⚠️ [OAuth2] Échec authentification Google: " + exception.getMessage());
                    String msg = exception.getMessage() != null
                            ? java.net.URLEncoder.encode(exception.getMessage(), java.nio.charset.StandardCharsets.UTF_8)
                            : "oauth2_failed";
                    response.sendRedirect(frontendUrl + "/login?error=" + msg);
                })
            )
            // 🛡️ Réponse JSON propre en cas de non-authentification sur les API REST
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Connexion requise\"}");
                })
            )
            // 🔐 Intégration du filtre JWT
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("http://localhost:[*]"); // ✅ Supporte tous les ports localhost (3000, 5173, etc.)
        config.addAllowedOriginPattern("https://*.onrender.com"); // ✅ Supporte tous les sous-domaines Render
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("https://polyshop-interface.onrender.com");
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            config.addAllowedOrigin(frontendUrl.trim());
        }
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true); // ✅ autorise les cookies/token

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
}
