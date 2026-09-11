package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Produit commandé
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 🔗 Commande associée
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"items", "hibernateLazyInitializer", "handler"})
    private Order order;

    // 📦 Quantité commandée
    @Column(nullable = false)
    private Integer quantity;

    // 💰 Prix unitaire au moment de la commande
    @Column(nullable = false)
    private Double unitPrice;

    // 📏 Taille ou format choisi
    @Column(nullable = true)
    private String taille;

    // 🎨 Couleur ou variante choisie
    @Column(nullable = true)
    private String couleur;
}
