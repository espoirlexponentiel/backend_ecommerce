package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 🔍 Recherche par nom de catégorie (insensible à la casse)
    List<Product> findByCategoryNomIgnoreCase(String nom);
}
