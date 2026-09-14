package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Market;
import com.ecommerce.backend.service.CategoryService;
import com.ecommerce.backend.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final MarketService marketService;

    // 🔓 Accessible à tous : liste des catégories (filtrable par ?marketId=...)
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories(
            @RequestParam(required = false) String marketId
    ) {
        if (marketId != null && !marketId.isBlank() && !marketId.equalsIgnoreCase("all")) {
            return ResponseEntity.ok(categoryService.getCategoriesByMarket(marketId.trim()));
        }
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // 🔓 Accessible à tous : récupérer une catégorie par ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔐 Accessible aux admins uniquement : création d'un rayon dans un marché
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, Object> payload) {
        String nom = (String) payload.get("nom");
        String marketId = (String) payload.get("marketId");

        if (nom == null || nom.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le nom de la catégorie est obligatoire"));
        }

        if (marketId == null || marketId.isBlank()) {
            // Si market est passé comme objet
            Object marketObj = payload.get("market");
            if (marketObj instanceof Map<?, ?> map) {
                marketId = (String) map.get("id");
            }
        }

        if (marketId == null || marketId.isBlank()) {
            marketId = "vestimentaire"; // Marché par défaut si non spécifié
        }

        Market market = marketService.getMarketById(marketId)
                .orElseGet(() -> marketService.createMarket(Market.builder().id(payload.get("marketId") != null ? (String) payload.get("marketId") : "vestimentaire").nom("Marché " + payload.get("marketId")).build()));

        Category category = Category.builder()
                .nom(nom.trim())
                .description((String) payload.get("description"))
                .market(market)
                .build();

        Category saved = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "✅ Catégorie créée avec succès", "category", saved)
        );
    }

    // 🔐 Accessible aux admins uniquement : modification d'un rayon
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String nom = (String) payload.get("nom");
        String description = (String) payload.get("description");
        String marketId = (String) payload.get("marketId");

        Category updated = Category.builder()
                .nom(nom)
                .description(description)
                .build();

        if (marketId != null && !marketId.isBlank()) {
            marketService.getMarketById(marketId).ifPresent(updated::setMarket);
        }

        try {
            Category saved = categoryService.updateCategory(id, updated);
            return ResponseEntity.ok(Map.of("message", "✅ Catégorie mise à jour avec succès", "category", saved));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // 🔐 Accessible aux admins uniquement : suppression
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
