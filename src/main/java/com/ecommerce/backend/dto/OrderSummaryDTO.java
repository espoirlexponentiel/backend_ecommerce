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
    private List<OrderItemDetailDTO> items;

    @Data
    @NoArgsConstructor
    public static class OrderItemDetailDTO {
        private Long id;
        private Long productId;
        private String nom;
        private String imageUrl;
        private Integer quantity;
        private Integer quantite;
        private Double unitPrice;
        private Double prix;
        private String taille;
        private String couleur;
        private Map<String, Object> product;
    }

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

        if (order.getItems() != null) {
            this.items = order.getItems().stream().map(item -> {
                OrderItemDetailDTO dto = new OrderItemDetailDTO();
                dto.setId(item.getId());
                dto.setQuantity(item.getQuantity());
                dto.setQuantite(item.getQuantity());
                dto.setUnitPrice(item.getUnitPrice());
                dto.setPrix(item.getUnitPrice());
                dto.setTaille(item.getTaille());
                dto.setCouleur(item.getCouleur());

                Map<String, Object> prodMap = new java.util.HashMap<>();
                if (item.getProduct() != null) {
                    dto.setProductId(item.getProduct().getId());
                    dto.setNom(item.getProduct().getNom());
                    dto.setImageUrl(item.getProduct().getImageUrl());

                    prodMap.put("id", item.getProduct().getId());
                    prodMap.put("nom", item.getProduct().getNom());
                    prodMap.put("imageUrl", item.getProduct().getImageUrl());
                    prodMap.put("prix", item.getProduct().getPrix());
                } else {
                    dto.setNom("Article de commande");
                    dto.setImageUrl("");
                    prodMap.put("nom", "Article de commande");
                    prodMap.put("imageUrl", "");
                }
                dto.setProduct(prodMap);
                return dto;
            }).collect(java.util.stream.Collectors.toList());
        } else {
            this.items = java.util.Collections.emptyList();
        }

        java.util.Map<String, Object> clientMap = new java.util.HashMap<>();
        if (order.getUser() != null) {
            this.userEmail = order.getUser().getEmail();
            this.userNom = order.getUser().getDisplayName();
            clientMap.put("nom", order.getUser().getDisplayName() != null ? order.getUser().getDisplayName() : "Client");
            clientMap.put("email", order.getUser().getEmail() != null ? order.getUser().getEmail() : "");
            clientMap.put("telephone", order.getTelephone() != null ? order.getTelephone() : (order.getUser().getTelephone() != null ? order.getUser().getTelephone() : ""));
            clientMap.put("adresse", order.getAdresseLivraison() != null ? order.getAdresseLivraison() : (order.getUser().getAdresse() != null ? order.getUser().getAdresse() : ""));
        } else {
            this.userEmail = "Client invité";
            this.userNom = "Client";
            clientMap.put("nom", "Client");
            clientMap.put("email", "");
            clientMap.put("telephone", order.getTelephone() != null ? order.getTelephone() : "");
            clientMap.put("adresse", order.getAdresseLivraison() != null ? order.getAdresseLivraison() : "");
        }
        this.client = clientMap;
    }
}
