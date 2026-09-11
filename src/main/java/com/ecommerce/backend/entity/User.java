package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String nom;

    @Column(nullable = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // "USER" ou "ADMIN"

    @Column(nullable = false)
    private String provider; // "local" ou "google"

    @Column(nullable = true)
    private String telephone;

    @Column(nullable = true)
    private String adresse;

    // ✅ Autorités pour Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final String roleName = (role != null && !role.isBlank())
                ? (role.toUpperCase().startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase())
                : "ROLE_USER";
        return Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority(roleName));
    }

    // ✅ Utilisé par Spring Security pour l'identifiant principal
    @Override
    public String getUsername() {
        return email != null ? email : username;
    }

    public String getDisplayName() {
        if (nom != null && !nom.isBlank()) return nom;
        if (username != null && !username.isBlank()) return username;
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
