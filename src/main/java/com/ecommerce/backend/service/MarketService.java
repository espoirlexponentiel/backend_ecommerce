package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Market;
import com.ecommerce.backend.repository.MarketRepository;
import com.ecommerce.backend.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final MarketRepository marketRepository;
    private final ProductRepository productRepository;

    public List<Market> getAllMarkets() {
        return marketRepository.findAll();
    }

    public List<Market> getActiveMarkets() {
        return marketRepository.findByIsActiveTrue();
    }

    public Optional<Market> getMarketById(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return marketRepository.findById(id);
    }

    @Transactional
    public Market createMarket(Market market) {
        if (market == null || market.getNom() == null || market.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom du marché est obligatoire.");
        }

        // Si l'ID n'est pas fourni, générer un ID/slug à partir du nom
        if (market.getId() == null || market.getId().isBlank()) {
            String slug = market.getNom().toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("(^-|-$)", "");
            if (slug.isBlank()) slug = "market-" + System.currentTimeMillis();
            market.setId(slug);
        }

        if (market.getSlug() == null || market.getSlug().isBlank()) {
            market.setSlug(market.getId());
        }

        if (market.getIcone() == null || market.getIcone().isBlank()) {
            market.setIcone("🏬");
        }

        if (market.getCouleurPrimaire() == null || market.getCouleurPrimaire().isBlank()) {
            market.setCouleurPrimaire("#0066FF");
        }

        if (market.getIsActive() == null) {
            market.setIsActive(true);
        }

        return marketRepository.save(market);
    }

    @Transactional
    public Market updateMarket(String id, Market updated) {
        return marketRepository.findById(id)
                .map(existing -> {
                    if (updated.getNom() != null && !updated.getNom().isBlank()) {
                        existing.setNom(updated.getNom());
                    }
                    if (updated.getSlug() != null) existing.setSlug(updated.getSlug());
                    if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
                    if (updated.getIcone() != null) existing.setIcone(updated.getIcone());
                    if (updated.getCouleurPrimaire() != null) existing.setCouleurPrimaire(updated.getCouleurPrimaire());
                    if (updated.getCouleurPrimaireHover() != null) existing.setCouleurPrimaireHover(updated.getCouleurPrimaireHover());
                    if (updated.getCouleurAccent() != null) existing.setCouleurAccent(updated.getCouleurAccent());
                    if (updated.getCouleurHeroBg() != null) existing.setCouleurHeroBg(updated.getCouleurHeroBg());
                    if (updated.getHeroTitre() != null) existing.setHeroTitre(updated.getHeroTitre());
                    if (updated.getHeroSousTitre() != null) existing.setHeroSousTitre(updated.getHeroSousTitre());
                    if (updated.getHeroImageUrl() != null) existing.setHeroImageUrl(updated.getHeroImageUrl());
                    if (updated.getHeroImageAlt() != null) existing.setHeroImageAlt(updated.getHeroImageAlt());
                    if (updated.getIsActive() != null) existing.setIsActive(updated.getIsActive());
                    return marketRepository.save(existing);
                })
                .orElseThrow(() -> new EntityNotFoundException("Marché introuvable avec l'ID : " + id));
    }

    @Transactional
    public Market toggleMarketVisibility(String id) {
        return marketRepository.findById(id)
                .map(existing -> {
                    boolean current = existing.getIsActive() != null ? existing.getIsActive() : true;
                    existing.setIsActive(!current);
                    return marketRepository.save(existing);
                })
                .orElseThrow(() -> new EntityNotFoundException("Marché introuvable avec l'ID : " + id));
    }

    @Transactional
    public void deleteMarket(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Identifiant du marché invalide.");
        }

        // 🛡️ Protection des 2 marchés racines fondamentaux
        if ("vestimentaire".equalsIgnoreCase(id.trim()) || "alimentation-generale".equalsIgnoreCase(id.trim())) {
            throw new IllegalArgumentException("Les 2 marchés de base (Mode & Vestimentaire et Alimentation Générale) ne peuvent pas être supprimés. Vous pouvez en revanche les masquer si nécessaire.");
        }

        if (!marketRepository.existsById(id)) {
            throw new EntityNotFoundException("Marché introuvable avec l'ID : " + id);
        }

        // Supprimer d'abord les produits orphelins rattachés directement à ce marché
        productRepository.deleteByMarketId(id);

        // Supprimer le marché (les catégories sont supprimées en cascade grâce à CascadeType.ALL)
        marketRepository.deleteById(id);
    }
}
