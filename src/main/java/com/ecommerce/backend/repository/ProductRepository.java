package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 🔍 Recherche par nom de catégorie (insensible à la casse)
    List<Product> findByCategoryNomIgnoreCase(String nom);

    // 🏬 Recherche par identifiant de marché
    List<Product> findByMarketId(String marketId);

    // 🏬 Recherche par marché et catégorie
    List<Product> findByMarketIdAndCategoryNomIgnoreCase(String marketId, String nom);

    // 🔎 Recherche globale par mot-clé dans le nom ou la description
    List<Product> findByNomContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nom, String description);

    // 🔎 Recherche par marché et nom
    List<Product> findByMarketIdAndNomContainingIgnoreCase(String marketId, String nom);
}
