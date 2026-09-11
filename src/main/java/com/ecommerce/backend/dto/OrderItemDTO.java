package com.ecommerce.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {

    private Long productId;

    @JsonAlias({"quantity", "quantite", "qty"})
    private Integer quantite;

    @JsonAlias({"taille", "size"})
    private String taille;

    @JsonAlias({"couleur", "color"})
    private String couleur;

    @JsonAlias({"prixUnitaire", "unitPrice", "prix", "price"})
    private Double prixUnitaire;

    private String nom;
    private String imageUrl;

    public Integer getEffectiveQuantity() {
        return (quantite != null && quantite > 0) ? quantite : 1;
    }
}
