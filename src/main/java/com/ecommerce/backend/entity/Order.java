package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Utilisateur ayant passé la commande
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"orders", "password"})
    private User user;

    // 📦 Liste des articles commandés
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    // 💰 Montant total
    @Column(nullable = false)
    private Double totalAmount;

    // 📅 Date de commande
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 🚚 Statut de la commande
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
}
