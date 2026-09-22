package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.security.JwtUtil;
import com.ecommerce.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @org.springframework.beans.factory.annotation.Value("${frontend.url:https://polyshop-interface.onrender.com}")
    private String frontendUrl;

    // 🔓 Inscription classique avec gestion d'erreur
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            System.out.println("Inscription reçue pour : " + user.getEmail());
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok(Map.of(
                    "message", "Utilisateur créé avec succès",
                    "id", savedUser.getId(),
                    "email", savedUser.getEmail(),
                    "nom", savedUser.getDisplayName(),
                    "role", savedUser.getRole()
            ));
        } catch (RuntimeException e) {
            System.out.println("Erreur inscription : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Erreur lors de l'inscription : " + e.getMessage()));
        }
    }

    // 🔓 Connexion classique + génération du token JWT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        if (user.getEmail() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email et mot de passe requis"));
        }

        Optional<User> optionalUser = userService.findByEmail(user.getEmail().trim().toLowerCase());

        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();

            if ("google".equalsIgnoreCase(existingUser.getProvider())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Ce compte est associé à Google. Veuillez vous connecter avec Google."));
            }

            if (userService.checkPassword(user.getPassword(), existingUser.getPassword())) {
                // ✅ subject = email
                String token = jwtUtil.generateToken(existingUser.getEmail(), existingUser.getRole());

                Map<String, Object> userData = new HashMap<>();
                userData.put("id", existingUser.getId());
                userData.put("email", existingUser.getEmail());
                userData.put("nom", existingUser.getDisplayName());
                userData.put("username", existingUser.getDisplayName());
                userData.put("role", existingUser.getRole());
                userData.put("telephone", existingUser.getTelephone() != null ? existingUser.getTelephone() : "");
                userData.put("adresse", existingUser.getAdresse() != null ? existingUser.getAdresse() : "");

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("email", existingUser.getEmail());
                response.put("username", existingUser.getDisplayName());
                response.put("nom", existingUser.getDisplayName());
                response.put("role", existingUser.getRole());
                response.put("user", userData);

                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Email ou mot de passe incorrect"));
    }

    // 🔐 Récupérer l'utilisateur connecté
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Object principal) {
        String email = null;
        if (principal instanceof User u) {
            email = u.getEmail();
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            email = ud.getUsername();
        } else if (principal instanceof String str) {
            email = str;
        }

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Non authentifié"));
        }

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Utilisateur introuvable"));
        }

        User u = userOpt.get();
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", u.getId());
        userData.put("email", u.getEmail());
        userData.put("nom", u.getDisplayName());
        userData.put("username", u.getDisplayName());
        userData.put("role", u.getRole());
        userData.put("telephone", u.getTelephone() != null ? u.getTelephone() : "");
        userData.put("adresse", u.getAdresse() != null ? u.getAdresse() : "");

        return ResponseEntity.ok(Map.of(
                "email", u.getEmail(),
                "nom", u.getDisplayName(),
                "username", u.getDisplayName(),
                "role", u.getRole(),
                "user", userData
        ));
    }

    // 📝 Mettre à jour le profil de l'utilisateur connecté (téléphone, adresse, nom)
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal Object principal,
            @RequestBody Map<String, String> profileData
    ) {
        String email = null;
        if (principal instanceof User u) {
            email = u.getEmail();
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            email = ud.getUsername();
        } else if (principal instanceof String str) {
            email = str;
        }

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Non authentifié"));
        }

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Utilisateur introuvable"));
        }

        User u = userOpt.get();
        if (profileData != null) {
            if (profileData.containsKey("telephone")) {
                u.setTelephone(profileData.get("telephone"));
            }
            if (profileData.containsKey("adresse")) {
                u.setAdresse(profileData.get("adresse"));
            }
            if (profileData.containsKey("nom") && profileData.get("nom") != null && !profileData.get("nom").isBlank()) {
                u.setNom(profileData.get("nom"));
                u.setUsername(profileData.get("nom"));
            }
        }

        User saved = userService.save(u);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", saved.getId());
        userData.put("email", saved.getEmail());
        userData.put("nom", saved.getDisplayName());
        userData.put("username", saved.getDisplayName());
        userData.put("role", saved.getRole());
        userData.put("telephone", saved.getTelephone() != null ? saved.getTelephone() : "");
        userData.put("adresse", saved.getAdresse() != null ? saved.getAdresse() : "");

        return ResponseEntity.ok(Map.of(
                "message", "Profil mis à jour avec succès",
                "user", userData
        ));
    }

    // 🔓 Callback OAuth2 Google -> Redirection vers le frontend React avec le JWT Token
    @GetMapping("/oauth2/success")
    public ResponseEntity<?> oauth2Success(
            @AuthenticationPrincipal OAuth2User principal,
            jakarta.servlet.http.HttpServletResponse response
    ) throws java.io.IOException {
        if (principal == null) {
            response.sendRedirect(frontendUrl + "/login?error=oauth2_failed");
            return null;
        }

        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        if (name == null || name.isBlank()) {
            name = email != null ? email.split("@")[0] : "Utilisateur";
        }

        Optional<User> existingUser = userService.findByEmail(email);
        User user;

        if (existingUser.isEmpty()) {
            user = new User();
            user.setEmail(email);
            user.setNom(name);
            user.setUsername(name);
            user.setRole("USER");
            user.setProvider("google");
            user.setPassword("");
            user = userService.save(user);
        } else {
            user = existingUser.get();
            // Toute connexion via Google attribue le rôle USER
            user.setRole("USER");
            user = userService.save(user);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String encodedName = java.net.URLEncoder.encode(user.getDisplayName() != null ? user.getDisplayName() : name, java.nio.charset.StandardCharsets.UTF_8);

        String redirectUrl = String.format(
                "%s/login?token=%s&email=%s&role=%s&nom=%s",
                frontendUrl,
                token,
                user.getEmail(),
                user.getRole(),
                encodedName
        );

        response.sendRedirect(redirectUrl);
        return null;
    }
}
