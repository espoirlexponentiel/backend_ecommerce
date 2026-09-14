package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // 🔹 Récupérer les produits avec filtres
    public List<Product> getProducts(String marketId, String search) {
        if (marketId != null && !marketId.isBlank() && !marketId.equalsIgnoreCase("all")) {
            if (search != null && !search.isBlank()) {
                return productRepository.findByMarketIdAndNomContainingIgnoreCase(marketId.trim(), search.trim());
            }
            return productRepository.findByMarketId(marketId.trim());
        }

        if (search != null && !search.isBlank()) {
            return productRepository.findByNomContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search.trim(), search.trim());
        }

        return productRepository.findAll();
    }

    // 🔹 Récupérer tous les produits
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 🔹 Récupérer les produits par marché
    public List<Product> getProductsByMarket(String marketId) {
        if (marketId == null || marketId.isBlank() || marketId.equalsIgnoreCase("all")) {
            return productRepository.findAll();
        }
        return productRepository.findByMarketId(marketId.trim());
    }

    // 🔹 Récupérer les produits par nom de catégorie
    public List<Product> getProductsByCategory(String nomCategorie) {
        if (nomCategorie == null || nomCategorie.isBlank()) {
            return List.of();
        }
        return productRepository.findByCategoryNomIgnoreCase(nomCategorie.trim());
    }

    // 🔹 Récupérer les produits par ID de catégorie
    public List<Product> getProductsByCategoryId(Long categoryId) {
        if (categoryId == null) return List.of();
        return productRepository.findByCategoryId(categoryId);
    }

    // 🔹 Récupérer un produit par ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // 🔹 Créer un nouveau produit
    @Transactional
    public Product createProduct(Product product) {
        if (product.getStock() == null) product.setStock(20);
        if (product.getRating() == null) product.setRating(5.0);
        if (product.getReviewCount() == null) product.setReviewCount(1);

        // Synchroniser le marketId avec la catégorie parente si disponible
        if (product.getCategory() != null && product.getCategory().getMarket() != null) {
            product.setMarketId(product.getCategory().getMarket().getId());
        }

        return productRepository.save(product);
    }

    // 🔹 Supprimer un produit par ID
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Produit introuvable avec l'ID : " + id);
        }
        productRepository.deleteById(id);
    }

    // 🔹 Mettre à jour un produit existant
    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(existing -> {
                    if (updatedProduct.getNom() != null) existing.setNom(updatedProduct.getNom());
                    if (updatedProduct.getDescription() != null) existing.setDescription(updatedProduct.getDescription());
                    if (updatedProduct.getPrix() != null) existing.setPrix(updatedProduct.getPrix());
                    if (updatedProduct.getAncienPrix() != null) existing.setAncienPrix(updatedProduct.getAncienPrix());
                    if (updatedProduct.getStock() != null) existing.setStock(updatedProduct.getStock());
                    if (updatedProduct.getImageUrl() != null) existing.setImageUrl(updatedProduct.getImageUrl());
                    if (updatedProduct.getSousTitre() != null) existing.setSousTitre(updatedProduct.getSousTitre());
                    if (updatedProduct.getBadge() != null) existing.setBadge(updatedProduct.getBadge());
                    if (updatedProduct.getTailles() != null) existing.setTailles(updatedProduct.getTailles());
                    if (updatedProduct.getCouleurs() != null) existing.setCouleurs(updatedProduct.getCouleurs());
                    if (updatedProduct.getComposition() != null) existing.setComposition(updatedProduct.getComposition());
                    if (updatedProduct.getPointsForts() != null) existing.setPointsForts(updatedProduct.getPointsForts());
                    if (updatedProduct.getRating() != null) existing.setRating(updatedProduct.getRating());
                    if (updatedProduct.getReviewCount() != null) existing.setReviewCount(updatedProduct.getReviewCount());
                    
                    if (updatedProduct.getCategory() != null) {
                        existing.setCategory(updatedProduct.getCategory());
                        if (updatedProduct.getCategory().getMarket() != null) {
                            existing.setMarketId(updatedProduct.getCategory().getMarket().getId());
                        }
                    } else if (updatedProduct.getMarketId() != null) {
                        existing.setMarketId(updatedProduct.getMarketId());
                    }

                    return productRepository.save(existing);
                })
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable avec l'ID : " + id));
    }
}
