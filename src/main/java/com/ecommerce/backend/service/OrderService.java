package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.OrderItemDTO;
import com.ecommerce.backend.dto.OrderRequestDTO;
import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    // 🛒 Transformer le panier en commande (depuis DTO frontend ou depuis base)
    @Transactional
    public Map<String, Object> placeOrder(User user, OrderRequestDTO request) {
        if (user == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        List<OrderItem> savedOrderItems = new ArrayList<>();
        double total = 0.0;
        double fraisPort = (request != null && request.getFraisPort() != null) ? request.getFraisPort() : 0.0;

        // Cas 1 : Le frontend fournit directement les articles du panier
        if (request != null && request.getItems() != null && !request.getItems().isEmpty()) {
            Order order = Order.builder()
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .status(OrderStatus.EN_ATTENTE)
                    .adresseLivraison(request.getAdresseLivraison() != null ? request.getAdresseLivraison() : user.getAdresse())
                    .telephone(request.getTelephone() != null ? request.getTelephone() : user.getTelephone())
                    .modePaiement(request.getModePaiement() != null ? request.getModePaiement() : "Carte Bancaire")
                    .marche(request.getMarche() != null ? request.getMarche() : "Général")
                    .fraisPort(fraisPort)
                    .totalAmount(0.0) // recalculé
                    .build();

            order = orderRepository.save(order);

            for (OrderItemDTO itemDto : request.getItems()) {
                Product product = null;
                if (itemDto.getProductId() != null) {
                    product = productRepository.findById(itemDto.getProductId()).orElse(null);
                }

                if (product == null) {
                    // Si le produit n'est pas encore en base, créons-le automatiquement pour préserver l'historique
                    product = Product.builder()
                            .nom(itemDto.getNom() != null ? itemDto.getNom() : "Article #" + itemDto.getProductId())
                            .description("Article de commande")
                            .prix(itemDto.getPrixUnitaire() != null ? itemDto.getPrixUnitaire() : 0.0)
                            .stock(100)
                            .imageUrl(itemDto.getImageUrl())
                            .marketId(request.getMarche())
                            .build();
                    product = productRepository.save(product);
                }

                int qty = itemDto.getEffectiveQuantity();
                double unitPrice = itemDto.getPrixUnitaire() != null ? itemDto.getPrixUnitaire() : product.getPrix();
                total += unitPrice * qty;

                // Décrémenter le stock si disponible
                if (product.getStock() != null && product.getStock() >= qty) {
                    product.setStock(product.getStock() - qty);
                    productRepository.save(product);
                }

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(qty)
                        .unitPrice(unitPrice)
                        .taille(itemDto.getTaille())
                        .couleur(itemDto.getCouleur())
                        .build();

                savedOrderItems.add(orderItemRepository.save(orderItem));
            }

            order.setTotalAmount(total + fraisPort);
            order.setItems(savedOrderItems);
            order = orderRepository.save(order);

            try {
                cartService.clearCart(user);
            } catch (Exception ignored) {}

            return formatOrderResponse(order, savedOrderItems);
        }

        // Cas 2 : Récupération des articles depuis le panier serveur en base de données
        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Le panier est vide");
        }

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Stock insuffisant pour le produit : " + product.getNom() +
                        ". Stock disponible : " + product.getStock());
            }
            total += product.getPrix() * item.getQuantity();
        }

        Order order = Order.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.EN_ATTENTE)
                .totalAmount(total + fraisPort)
                .fraisPort(fraisPort)
                .adresseLivraison(request != null ? request.getAdresseLivraison() : user.getAdresse())
                .telephone(request != null ? request.getTelephone() : user.getTelephone())
                .modePaiement(request != null ? request.getModePaiement() : "Carte Bancaire")
                .marche(request != null ? request.getMarche() : "Général")
                .build();

        order = orderRepository.save(order);

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(product.getPrix())
                    .build();

            savedOrderItems.add(orderItemRepository.save(orderItem));
        }

        order.setItems(savedOrderItems);
        cartService.clearCart(user);

        return formatOrderResponse(order, savedOrderItems);
    }

    private Map<String, Object> formatOrderResponse(Order order, List<OrderItem> items) {
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("id", order.getId());
        response.put("status", order.getStatus());
        response.put("statut", order.getStatus() != null ? order.getStatus().name() : "EN_ATTENTE");
        response.put("totalAmount", order.getTotalAmount());
        response.put("total", order.getTotalAmount());
        response.put("fraisPort", order.getFraisPort());
        response.put("adresseLivraison", order.getAdresseLivraison());
        response.put("marche", order.getMarche());
        response.put("createdAt", order.getCreatedAt());
        response.put("items", items);
        return response;
    }

    // 👤 Voir les commandes d’un utilisateur
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // 👨‍💼 Voir toutes les commandes (admin)
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    // 🔄 Modifier le statut d’une commande (admin)
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'ID : " + orderId));

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    // ❌ Annuler une commande (user, < 10 min)
    @Transactional
    public void cancelOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'ID : " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Accès refusé");
        }

        Duration duration = Duration.between(order.getCreatedAt(), LocalDateTime.now());
        if (duration.toMinutes() > 10) {
            throw new RuntimeException("Impossible d'annuler après 10 minutes");
        }

        // ✅ Restaurer le stock
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(OrderStatus.ANNULEE);
        orderRepository.save(order);
    }
}
