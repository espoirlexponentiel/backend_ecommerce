package com.ecommerce.backend.dto;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSummaryDTO {
    private Long id;
    private String userEmail;
    private Double totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public OrderSummaryDTO(Order order) {
        this.id = order.getId();
        this.userEmail = order.getUser().getEmail();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
    }
}
