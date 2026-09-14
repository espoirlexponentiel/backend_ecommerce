package com.ecommerce.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double prix;

    @Column(nullable = false)
    private Integer stock;

    // ✅ Image URL du produit
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    // 🏬 Identifiant du marché (synchro ou hérité de la catégorie)
    @Column(name = "market_id", nullable = true)
    private String marketId;

    // 🏷️ Sous-titre descriptif
    @Column(name = "sous_titre", nullable = true)
    private String sousTitre;

    // 🔖 Badge promo/nouveau
    @Column(nullable = true)
    private String badge;

    // 💲 Ancien prix barré
    @Column(name = "ancien_prix", nullable = true)
    private Double ancienPrix;

    // 📏 Tailles disponibles (séparées par virgule ou JSON)
    @Column(nullable = true, columnDefinition = "TEXT")
    private String tailles;

    // 🎨 Couleurs disponibles (séparées par virgule ou JSON)
    @Column(nullable = true, columnDefinition = "TEXT")
    private String couleurs;

    // 🧵 Composition des matières
    @Column(nullable = true, columnDefinition = "TEXT")
    private String composition;

    // ✨ Points forts de l'article (séparés par saut de ligne)
    @Column(name = "points_forts", nullable = true, columnDefinition = "TEXT")
    private String pointsForts;

    // ⭐ Note moyenne
    @Builder.Default
    @Column(nullable = true)
    private Double rating = 5.0;

    // 💬 Nombre d'avis
    @Builder.Default
    @Column(name = "review_count", nullable = true)
    private Integer reviewCount = 1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = true)
    @JsonIgnoreProperties({"products"})
    private Category category;

    public String getMarketId() {
        if (this.marketId != null && !this.marketId.isBlank()) {
            return this.marketId;
        }
        if (this.category != null && this.category.getMarket() != null) {
            return this.category.getMarket().getId();
        }
        return null;
    }
}
