package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 🔍 Recherche par ID de Marché
    List<Category> findByMarketId(String marketId);

    // 🔍 Recherche par Marché et Nom de Catégorie (insensible à la casse)
    Optional<Category> findByMarketIdAndNomIgnoreCase(String marketId, String nom);

    // 🔍 Recherche par Nom seul (insensible à la casse - premier résultat)
    Optional<Category> findByNomIgnoreCase(String nom);

    // 🔍 Vérification d'existence
    boolean existsByMarketIdAndNomIgnoreCase(String marketId, String nom);
}
