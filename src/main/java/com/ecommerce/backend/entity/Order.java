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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"orders", "password", "hibernateLazyInitializer", "handler"})
    private User user;

    // 📦 Liste des articles commandés
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"order", "hibernateLazyInitializer", "handler"})
    private List<OrderItem> items;

    // 💰 Montant total
    @Column(nullable = false)
    private Double totalAmount;

    // 🚚 Frais de port
    @Column(nullable = true)
    private Double fraisPort;

    // 📍 Adresse de livraison
    @Column(nullable = true, columnDefinition = "TEXT")
    private String adresseLivraison;

    // 📞 Téléphone de contact
    @Column(nullable = true)
    private String telephone;

    // 💳 Mode de paiement (Carte, Mobile Money, Google Pay, etc.)
    @Column(nullable = true)
    private String modePaiement;

    // 🏬 Marché concerné (Mode, Alimentation, etc.)
    @Column(nullable = true)
    private String marche;

    // 📅 Date de commande
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 🚚 Statut de la commande
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
}
