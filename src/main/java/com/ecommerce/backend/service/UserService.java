package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 🔐 Inscription classique avec encodage du mot de passe
    public User registerUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("L'adresse email est obligatoire");
        }

        String normalizedEmail = user.getEmail().trim().toLowerCase();
        user.setEmail(normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Un compte existe déjà avec cette adresse email.");
        }

        if (user.getNom() != null && !user.getNom().isBlank()) {
            user.setUsername(user.getNom().trim());
            user.setNom(user.getNom().trim());
        } else if (user.getUsername() != null && !user.getUsername().isBlank()) {
            user.setNom(user.getUsername().trim());
            user.setUsername(user.getUsername().trim());
        } else {
            String fallbackName = normalizedEmail.split("@")[0];
            user.setNom(fallbackName);
            user.setUsername(fallbackName);
        }

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        if (user.getProvider() == null || user.getProvider().isBlank()) {
            user.setProvider("local");
        }
        return userRepository.save(user);
    }

    // 🔍 Recherche par email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 🔍 Recherche par nom d'utilisateur
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 🔐 Vérification du mot de passe
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // ✅ Enregistrement direct (sans encodage) — utile pour OAuth2
    public User save(User user) {
        return userRepository.save(user);
    }
}
