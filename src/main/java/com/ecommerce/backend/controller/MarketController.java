package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Market;
import com.ecommerce.backend.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/markets")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;
    private final com.ecommerce.backend.service.CloudinaryService cloudinaryService;

    // 🔓 Accessible à tous : liste de tous les marchés enregistrés en BDD
    @GetMapping
    public ResponseEntity<List<Market>> getAvailableMarkets() {
        return ResponseEntity.ok(marketService.getAllMarkets());
    }

    // 🔓 Accessible à tous : récupérer un marché par ID / Slug
    @GetMapping("/{id}")
    public ResponseEntity<Market> getMarketById(@PathVariable String id) {
        return marketService.getMarketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔐 Upload image bannière marché vers Cloudinary
    @PostMapping(value = "/upload-image", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadMarketImage(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Veuillez sélectionner un fichier image"));
        }
        try {
            String secureUrl = cloudinaryService.uploadImage(file, "ecommerce_markets");
            return ResponseEntity.ok(Map.of(
                    "url", secureUrl,
                    "message", "✅ Image uploadée avec succès sur Cloudinary"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Échec de l'upload Cloudinary: " + e.getMessage()));
        }
    }

    // 🔐 Accessible aux admins uniquement : créer un nouveau marché
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createMarket(@RequestBody Market market) {
        if (market.getNom() == null || market.getNom().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le nom du marché est obligatoire"));
        }
        Market created = marketService.createMarket(market);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "✅ Marché créé avec succès", "market", created)
        );
    }

    // 🔐 Accessible aux admins uniquement : modifier un marché existant
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateMarket(@PathVariable String id, @RequestBody Market market) {
        try {
            Market updated = marketService.updateMarket(id, market);
            return ResponseEntity.ok(Map.of("message", "✅ Marché mis à jour avec succès", "market", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // 🔐 Accessible aux admins uniquement : basculer la visibilité (Afficher / Masquer)
    @PutMapping("/{id}/toggle-visibility")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleMarketVisibility(@PathVariable String id) {
        try {
            Market updated = marketService.toggleMarketVisibility(id);
            return ResponseEntity.ok(Map.of(
                    "message", updated.getIsActive() ? "✅ Marché rendu visible" : "👁️ Marché masqué aux clients",
                    "market", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // 🔐 Accessible aux admins uniquement : supprimer un marché
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMarket(@PathVariable String id) {
        try {
            marketService.deleteMarket(id);
            return ResponseEntity.ok(Map.of("message", "✅ Marché et ses éléments associés supprimés avec succès"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
