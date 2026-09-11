package com.ecommerce.backend.controller;
import com.ecommerce.backend.dto.OrderSummaryDTO;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderStatus;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.service.OrderService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 🛒 Valider le panier et créer une commande
    @PostMapping
    public ResponseEntity<?> placeOrder(
            @AuthenticationPrincipal User user,
            @RequestBody(required = false) com.ecommerce.backend.dto.OrderRequestDTO orderRequest
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Connexion requise pour passer une commande"));
        }

        try {
            Map<String, Object> orderResponse = orderService.placeOrder(user, orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 👤 Voir ses propres commandes
    @GetMapping
    public ResponseEntity<?> getUserOrders(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Connexion requise"));
        }

        List<Order> orders = orderService.getOrdersByUser(user);
        List<OrderSummaryDTO> summaries = orders.stream()
                .map(OrderSummaryDTO::new)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    // 👨‍💼 Voir toutes les commandes (admin)
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderSummaryDTO> summaries = orders.stream()
                .map(OrderSummaryDTO::new)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    // 🔄 Modifier le statut d’une commande (admin)
    @PutMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String statusStr = status;
        if ((statusStr == null || statusStr.isBlank()) && body != null) {
            statusStr = body.getOrDefault("status", body.get("statut"));
        }

        if (statusStr == null || statusStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Paramètre statut requis"));
        }

        try {
            OrderStatus newStatus = OrderStatus.fromFlexibleString(statusStr);
            orderService.updateOrderStatus(orderId, newStatus);
            return ResponseEntity.ok(Map.of(
                    "message", "Statut mis à jour avec succès",
                    "orderId", orderId,
                    "status", newStatus.name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ❌ Annuler une commande (user, < 10 min)
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> cancelOrder(@AuthenticationPrincipal User user,
                                         @PathVariable Long orderId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Connexion requise"));
        }

        try {
            orderService.cancelOrder(user, orderId);
            return ResponseEntity.ok(Map.of("message", "Commande annulée avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
