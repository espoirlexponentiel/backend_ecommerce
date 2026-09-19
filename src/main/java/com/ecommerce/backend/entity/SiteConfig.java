package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "site_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteConfig {

    @Id
    private Long id; // Identifiant fixe = 1L

    @Column(nullable = false)
    private String brandName; // e.g. "7 SHOP"

    @Column(nullable = true)
    private String brandBadge; // e.g. "7"

    @Column(nullable = true, length = 1000)
    private String logoUrl; // URL image logo (Cloudinary ou externe)

    @Column(nullable = true)
    private String tagline; // e.g. "Boutique Officielle"
}
