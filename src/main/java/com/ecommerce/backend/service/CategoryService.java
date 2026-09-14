package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Market;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.MarketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MarketRepository marketRepository;

    // 🔹 Récupérer toutes les catégories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 🔹 Récupérer les catégories par marché
    public List<Category> getCategoriesByMarket(String marketId) {
        if (marketId == null || marketId.isBlank() || marketId.equalsIgnoreCase("all")) {
            return categoryRepository.findAll();
        }
        return categoryRepository.findByMarketId(marketId.trim());
    }

    // 🔹 Récupérer une catégorie par ID
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // 🔹 Récupérer une catégorie par marché et nom
    public Optional<Category> getCategoryByMarketAndNom(String marketId, String nom) {
        if (nom == null || nom.isBlank()) return Optional.empty();
        String normalized = nom.trim();
        if (marketId != null && !marketId.isBlank()) {
            return categoryRepository.findByMarketIdAndNomIgnoreCase(marketId.trim(), normalized);
        }
        return categoryRepository.findByNomIgnoreCase(normalized);
    }

    // 🔹 Récupérer une catégorie par nom
    public Optional<Category> getCategoryByNom(String nom) {
        if (nom == null || nom.isBlank()) return Optional.empty();
        return categoryRepository.findByNomIgnoreCase(nom.trim());
    }

    // 🔹 Créer une catégorie rattachée à un marché
    @Transactional
    public Category createCategory(Category category) {
        if (category == null || category.getNom() == null || category.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de la catégorie est requis.");
        }

        Market market = category.getMarket();
        if (market == null || market.getId() == null || market.getId().isBlank()) {
            throw new IllegalArgumentException("Le marché parent est requis pour créer une catégorie.");
        }

        // Vérifier que le marché existe ou le créer
        Market persistentMarket = marketRepository.findById(market.getId())
                .orElseGet(() -> marketRepository.save(
                        Market.builder()
                                .id(market.getId())
                                .nom(market.getNom() != null ? market.getNom() : market.getId())
                                .slug(market.getId())
                                .build()
                ));

        category.setMarket(persistentMarket);
        category.setNom(category.getNom().trim());

        // Éviter les doublons dans le même marché
        return categoryRepository.findByMarketIdAndNomIgnoreCase(persistentMarket.getId(), category.getNom())
                .orElseGet(() -> categoryRepository.save(category));
    }

    // 🔹 Mettre à jour une catégorie existante
    @Transactional
    public Category updateCategory(Long id, Category updated) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    if (updated.getNom() != null && !updated.getNom().isBlank()) {
                        existing.setNom(updated.getNom().trim());
                    }
                    if (updated.getDescription() != null) {
                        existing.setDescription(updated.getDescription());
                    }
                    if (updated.getMarket() != null && updated.getMarket().getId() != null) {
                        marketRepository.findById(updated.getMarket().getId()).ifPresent(existing::setMarket);
                    }
                    return categoryRepository.save(existing);
                })
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable avec l'ID : " + id));
    }

    // 🔹 Supprimer une catégorie par ID
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Catégorie introuvable avec l'ID : " + id);
        }
        categoryRepository.deleteById(id);
    }
}
