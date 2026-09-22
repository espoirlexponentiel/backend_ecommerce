package com.ecommerce.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "markets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Market {

    @Id
    @Column(nullable = false, unique = true)
    private String id; // ex: "vestimentaire", "alimentation-generale", "electronique"

    @Column(nullable = false)
    private String nom;

    @Column(nullable = true)
    private String slug;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String description;

    @Column(nullable = true)
    @Builder.Default
    private String icone = "🏬";

    @Column(name = "couleur_primaire", nullable = true)
    @Builder.Default
    private String couleurPrimaire = "#0066FF";

    @Column(name = "couleur_primaire_hover", nullable = true)
    @Builder.Default
    private String couleurPrimaireHover = "#0052cc";

    @Column(name = "couleur_accent", nullable = true)
    @Builder.Default
    private String couleurAccent = "#FFB800";

    @Column(name = "couleur_hero_bg", columnDefinition = "TEXT", nullable = true)
    @Builder.Default
    private String couleurHeroBg = "linear-gradient(135deg, #ffffff 0%, #f4f8ff 50%, #fffbf0 100%)";

    @Column(name = "hero_titre", columnDefinition = "TEXT", nullable = true)
    private String heroTitre;

    @Column(name = "hero_sous_titre", columnDefinition = "TEXT", nullable = true)
    private String heroSousTitre;

    @Column(name = "hero_image_url", columnDefinition = "TEXT", nullable = true)
    private String heroImageUrl;

    @Column(name = "hero_image_alt", nullable = true)
    private String heroImageAlt;

    @Column(name = "hero_image_width", nullable = true)
    private String heroImageWidth;

    @Column(name = "hero_image_height", nullable = true)
    private String heroImageHeight;

    @Column(name = "hero_image_object_fit", nullable = true)
    @Builder.Default
    private String heroImageObjectFit = "contain";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "market", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"market", "products"})
    @Builder.Default
    private List<Category> categories = new ArrayList<>();
}
