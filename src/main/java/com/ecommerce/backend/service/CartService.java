package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ Spring Transactional

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                            .user(user)
                            .items(new java.util.ArrayList<>())
                            .build()
                ));
    }

    @Transactional
    public void addToCart(User user, Long productId, int quantity) {
        if (quantity <= 0) throw new RuntimeException("La quantité doit être supérieure à zéro");

        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.getItems().add(item);
        }

        cartItemRepository.saveAndFlush(item);
        cartRepository.saveAndFlush(cart);
    }

    @Transactional
    public void updateQuantity(User user, Long productId, int quantity) {
        if (quantity <= 0) throw new RuntimeException("La quantité doit être supérieure à zéro");

        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Article non trouvé dans le panier"));

        item.setQuantity(quantity);
        cartItemRepository.saveAndFlush(item);
    }

    @Transactional
    public void removeFromCart(User user, Long productId) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Article non trouvé dans le panier"));

        cartItemRepository.delete(item);
        cartRepository.saveAndFlush(cart);
    }

    public List<CartItem> getCartItems(User user) {
        Cart cart = getOrCreateCart(user);
        return cartItemRepository.findByCart(cart);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteAllByCart(cart);
        cart.getItems().clear();
        cartRepository.saveAndFlush(cart);
    }
}
