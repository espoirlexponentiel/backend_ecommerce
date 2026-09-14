package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketRepository extends JpaRepository<Market, String> {

    Optional<Market> findBySlug(String slug);

    boolean existsByNomIgnoreCase(String nom);

    java.util.List<Market> findByIsActiveTrue();
}
