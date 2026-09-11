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

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Utilisateur déjà existant avec cet email : " + user.getEmail());
        }

        if (user.getNom() == null && user.getUsername() != null) {
            user.setNom(user.getUsername());
        } else if (user.getUsername() == null && user.getNom() != null) {
            user.setUsername(user.getNom());
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
