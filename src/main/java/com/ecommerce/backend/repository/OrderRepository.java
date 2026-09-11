package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderStatus;
import com.ecommerce.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 👤 Commandes d’un utilisateur
    List<Order> findByUser(User user);

    // 👨‍💼 Commandes par statut (pour filtrage admin)
    List<Order> findByStatus(OrderStatus status);
}
