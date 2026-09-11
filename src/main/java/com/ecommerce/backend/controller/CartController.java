package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ➕ Ajouter un produit au panier
    @PostMapping("/items")
    public ResponseEntity<?> addToCart(@AuthenticationPrincipal User user,
                                       @RequestBody Map<String, Object> payload) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non connecté"));
        }

        try {
            Long productId = Long.valueOf(payload.get("productId").toString());
            int quantity = Integer.parseInt(payload.get("quantity").toString());

            cartService.addToCart(user, productId, quantity);
            return ResponseEntity.ok(Map.of("message", "Produit ajouté au panier"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✏️ Modifier la quantité
    @PutMapping("/items")
    public ResponseEntity<?> updateQuantity(@AuthenticationPrincipal User user,
                                            @RequestBody Map<String, Object> payload) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non connecté"));
        }

        try {
            Long productId = Long.valueOf(payload.get("productId").toString());
            int quantity = Integer.parseInt(payload.get("quantity").toString());

            cartService.updateQuantity(user, productId, quantity);
            return ResponseEntity.ok(Map.of("message", "Quantité mise à jour"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ❌ Supprimer un article
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<?> removeFromCart(@AuthenticationPrincipal User user,
                                            @PathVariable Long productId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non connecté"));
        }

        try {
            cartService.removeFromCart(user, productId);
            return ResponseEntity.ok(Map.of("message", "Article supprimé du panier"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 📦 Voir le contenu du panier
    @GetMapping("/items")
    public ResponseEntity<?> getCartItems(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non connecté"));
        }

        List<CartItem> items = cartService.getCartItems(user);

        // ✅ renvoie toujours un objet avec "items"
        return ResponseEntity.ok(Map.of("items", items));
    }

    // 🧹 Vider le panier
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non connecté"));
        }

        cartService.clearCart(user);
        return ResponseEntity.ok(Map.of("message", "Panier vidé"));
    }
}
