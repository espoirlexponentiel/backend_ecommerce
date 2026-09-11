package com.ecommerce.backend.security;

import com.ecommerce.backend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24h

    // 🔐 Générer un token avec username (email) et rôle
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔍 Extraire l'email (username)
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // 🔍 Extraire le rôle
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // 🔍 Vérifier si le token est expiré
    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // ✅ Vérifier si le token est valide pour un utilisateur donné
    public boolean isTokenValid(String token, User user) {
        final String email = extractUsername(token);
        return (email != null && email.equals(user.getEmail()) && !isTokenExpired(token));
    }

    // 🔐 Clé de signature
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 🔍 Extraire les claims
    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new RuntimeException("Token invalide ou corrompu", e);
        }
    }
}
