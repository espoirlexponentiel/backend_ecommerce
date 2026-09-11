package com.ecommerce.backend.dto;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class OrderSummaryDTO {
    private Long id;
    private String userEmail;
    private String userNom;
    private Double totalAmount;
    private Double total;
    private Double fraisPort;
    private OrderStatus status;
    private String statut;
    private String adresseLivraison;
    private String telephone;
    private String modePaiement;
    private String marche;
    private LocalDateTime createdAt;
    private String date;
    private Map<String, Object> client;
    private List<OrderItem> items;

    public OrderSummaryDTO(Order order) {
        this.id = order.getId();
        this.totalAmount = order.getTotalAmount();
        this.total = order.getTotalAmount();
        this.fraisPort = order.getFraisPort() != null ? order.getFraisPort() : 0.0;
        this.status = order.getStatus();
        this.statut = order.getStatus() != null ? order.getStatus().name() : "EN_ATTENTE";
        this.adresseLivraison = order.getAdresseLivraison();
        this.telephone = order.getTelephone();
        this.modePaiement = order.getModePaiement();
        this.marche = order.getMarche() != null ? order.getMarche() : "Général";
        this.createdAt = order.getCreatedAt();
        this.date = order.getCreatedAt() != null ? order.getCreatedAt().toString() : "";
        this.items = order.getItems();

        if (order.getUser() != null) {
            this.userEmail = order.getUser().getEmail();
            this.userNom = order.getUser().getDisplayName();
            this.client = Map.of(
                "nom", order.getUser().getDisplayName(),
                "email", order.getUser().getEmail(),
                "telephone", order.getTelephone() != null ? order.getTelephone() : (order.getUser().getTelephone() != null ? order.getUser().getTelephone() : ""),
                "adresse", order.getAdresseLivraison() != null ? order.getAdresseLivraison() : (order.getUser().getAdresse() != null ? order.getUser().getAdresse() : "")
            );
        } else {
            this.userEmail = "Client invité";
            this.userNom = "Client";
            this.client = Map.of(
                "nom", "Client",
                "email", "",
                "telephone", order.getTelephone() != null ? order.getTelephone() : "",
                "adresse", order.getAdresseLivraison() != null ? order.getAdresseLivraison() : ""
            );
        }
    }
}
