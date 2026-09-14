package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 🔍 Recherche par ID de Catégorie
    List<Product> findByCategoryId(Long categoryId);

    // 🔍 Recherche par Nom de Catégorie
    List<Product> findByCategoryNomIgnoreCase(String nom);

    // 🏬 Recherche par identifiant de marché
    List<Product> findByMarketId(String marketId);

    // 🏬 Recherche par marché via la catégorie
    List<Product> findByCategoryMarketId(String marketId);

    // 🏬 Recherche par marché et nom de catégorie
    List<Product> findByMarketIdAndCategoryNomIgnoreCase(String marketId, String nom);

    // 🔎 Recherche par marché et nom de produit
    List<Product> findByMarketIdAndNomContainingIgnoreCase(String marketId, String nom);

    // 🔎 Recherche globale par mot-clé dans le nom ou la description
    List<Product> findByNomContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nom, String description);

    // 🗑️ Suppression des produits d'un marché
    void deleteByMarketId(String marketId);
}
