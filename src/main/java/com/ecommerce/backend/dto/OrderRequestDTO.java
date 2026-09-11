package com.ecommerce.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {

    @JsonAlias({"adresseLivraison", "shippingAddress", "adresse", "address"})
    private String adresseLivraison;

    @JsonAlias({"telephone", "phone"})
    private String telephone;

    @JsonAlias({"modePaiement", "paymentMethod", "paymentMode"})
    private String modePaiement;

    @JsonAlias({"marche", "market", "marketId"})
    private String marche;

    @JsonAlias({"fraisPort", "shippingFee", "fraisLivraison"})
    private Double fraisPort;

    @Builder.Default
    @JsonAlias({"items", "articles", "cartItems"})
    private List<OrderItemDTO> items = new ArrayList<>();
}
