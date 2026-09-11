package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // 🔹 Récupérer toutes les catégories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 🔹 Récupérer une catégorie par nom (insensible à la casse)
    public Optional<Category> getCategoryByNom(String nom) {
        if (nom == null || nom.isBlank()) return Optional.empty();
        String normalized = nom.trim().toLowerCase();
        return categoryRepository.findByNomIgnoreCase(normalized);
    }

    // 🔹 Créer une catégorie (évite les doublons)
    public Category createCategory(Category category) {
        if (category == null || category.getNom() == null || category.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de la catégorie est requis.");
        }

        String normalized = category.getNom().trim().toLowerCase();
        return categoryRepository.findByNomIgnoreCase(normalized)
                .orElseGet(() -> {
                    category.setNom(normalized);
                    return categoryRepository.save(category);
                });
    }

    // 🔹 Supprimer une catégorie par ID (si elle existe)
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Catégorie introuvable avec l'ID : " + id);
        }
        categoryRepository.deleteById(id);
    }
}
