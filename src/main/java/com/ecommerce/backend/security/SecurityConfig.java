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
// import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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

                // 🔓 Routes publiques
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/users/login").permitAll()
                .requestMatchers("/oauth2/**").permitAll()

                // 🔓 Accès libre aux produits, catégories et marchés
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/markets/**").permitAll()

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
                .defaultSuccessUrl("/api/users/oauth2/success", true)
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
        config.addAllowedOrigin("http://localhost:3000");
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
