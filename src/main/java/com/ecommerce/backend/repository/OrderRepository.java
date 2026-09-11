package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderStatus;
import com.ecommerce.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 👤 Commandes d’un utilisateur (triées par date décroissante)
    List<Order> findByUser(User user);
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    // 👨‍💼 Toutes les commandes (triées par date décroissante)
    List<Order> findAllByOrderByCreatedAtDesc();

    // 👨‍💼 Commandes par statut (pour filtrage admin)
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
}
