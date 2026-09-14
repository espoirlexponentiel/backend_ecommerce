package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Market;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.service.CategoryService;
import com.ecommerce.backend.service.CloudinaryService;
import com.ecommerce.backend.service.MarketService;
import com.ecommerce.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final MarketService marketService;
    private final CloudinaryService cloudinaryService;

    // 🔓 Récupérer les produits (supporte ?marketId=... &search=...)
    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String marketId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String q
    ) {
        String query = (search != null && !search.isBlank()) ? search : q;
        return ResponseEntity.ok(productService.getProducts(marketId, query));
    }

    // 🔓 Récupérer les produits par marché
    @GetMapping("/market/{marketId}")
    public ResponseEntity<List<Product>> getProductsByMarket(@PathVariable String marketId) {
        return ResponseEntity.ok(productService.getProductsByMarket(marketId));
    }

    // 🔓 Récupérer les produits par nom de catégorie
    @GetMapping("/category/{nom}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String nom) {
        return ResponseEntity.ok(productService.getProductsByCategory(nom));
    }

    // 🔓 Récupérer un produit par ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔐 Créer un nouveau produit via JSON (application/json)
    @PostMapping(consumes = {"application/json"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProductJson(@RequestBody Product product) {
        if (product.getNom() == null || product.getNom().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le nom du produit est obligatoire"));
        }

        String targetMarketId = product.getMarketId() != null && !product.getMarketId().isBlank() 
                ? product.getMarketId() : "vestimentaire";

        Category resolvedCategory = null;

        if (product.getCategory() != null) {
            if (product.getCategory().getId() != null) {
                resolvedCategory = categoryService.getCategoryById(product.getCategory().getId()).orElse(null);
            } else if (product.getCategory().getNom() != null && !product.getCategory().getNom().isBlank()) {
                String catNom = product.getCategory().getNom().trim();
                resolvedCategory = categoryService.getCategoryByMarketAndNom(targetMarketId, catNom)
                        .orElseGet(() -> {
                            Market m = marketService.getMarketById(targetMarketId)
                                    .orElseGet(() -> marketService.createMarket(Market.builder().id(targetMarketId).nom(targetMarketId).build()));
                            return categoryService.createCategory(Category.builder().nom(catNom).market(m).build());
                        });
            }
        }

        if (resolvedCategory != null) {
            product.setCategory(resolvedCategory);
            product.setMarketId(resolvedCategory.getMarket() != null ? resolvedCategory.getMarket().getId() : targetMarketId);
        }

        Product saved = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "✅ Produit créé avec succès", "product", saved)
        );
    }

    // 🔐 Créer un nouveau produit avec upload d'image (multipart/form-data)
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProductMultipart(
            @RequestParam("nom") String productNom,
            @RequestParam(value = "description", required = false) String productDescription,
            @RequestParam("prix") Double productPrix,
            @RequestParam(value = "ancienPrix", required = false) Double ancienPrix,
            @RequestParam(value = "stock", required = false, defaultValue = "20") Integer productStock,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "category", required = false) String categoryNom,
            @RequestParam(value = "marketId", required = false) String marketId,
            @RequestParam(value = "sousTitre", required = false) String sousTitre,
            @RequestParam(value = "badge", required = false) String badge,
            @RequestParam(value = "tailles", required = false) String tailles,
            @RequestParam(value = "couleurs", required = false) String couleurs,
            @RequestParam(value = "composition", required = false) String composition,
            @RequestParam(value = "pointsForts", required = false) String pointsForts,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            HttpServletRequest request
    ) throws IOException {

        String targetMarketId = (marketId != null && !marketId.isBlank()) ? marketId : "vestimentaire";
        Category resolvedCategory = null;

        if (categoryId != null) {
            resolvedCategory = categoryService.getCategoryById(categoryId).orElse(null);
        } else if (categoryNom != null && !categoryNom.isBlank()) {
            String normCat = categoryNom.trim();
            resolvedCategory = categoryService.getCategoryByMarketAndNom(targetMarketId, normCat)
                    .orElseGet(() -> {
                        Market m = marketService.getMarketById(targetMarketId)
                                .orElseGet(() -> marketService.createMarket(Market.builder().id(targetMarketId).nom(targetMarketId).build()));
                        return categoryService.createCategory(Category.builder().nom(normCat).market(m).build());
                    });
        }

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(imageFile, "ecommerce_products");
        }

        Product product = Product.builder()
                .nom(productNom)
                .description(productDescription != null ? productDescription : "")
                .prix(productPrix)
                .ancienPrix(ancienPrix)
                .stock(productStock != null ? productStock : 20)
                .marketId(resolvedCategory != null && resolvedCategory.getMarket() != null ? resolvedCategory.getMarket().getId() : targetMarketId)
                .sousTitre(sousTitre)
                .badge(badge)
                .tailles(tailles)
                .couleurs(couleurs)
                .composition(composition)
                .pointsForts(pointsForts)
                .imageUrl(imageUrl)
                .category(resolvedCategory)
                .rating(5.0)
                .reviewCount(1)
                .build();

        Product saved = productService.createProduct(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "✅ Produit créé avec succès", "product", saved)
        );
    }

    // 🔐 Endpoint d'upload direct d'image vers Cloudinary (Admin)
    @PostMapping(value = "/upload-image", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Veuillez sélectionner un fichier image"));
        }
        try {
            String secureUrl = cloudinaryService.uploadImage(file, "ecommerce_products");
            return ResponseEntity.ok(Map.of(
                    "url", secureUrl,
                    "message", "✅ Image uploadée avec succès sur Cloudinary"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Échec de l'upload Cloudinary: " + e.getMessage()));
        }
    }

    // 🔐 Supprimer un produit par ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // 🔐 Mettre à jour un produit existant
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product updatedProduct) {
        if (updatedProduct.getCategory() != null) {
            if (updatedProduct.getCategory().getId() != null) {
                Category cat = categoryService.getCategoryById(updatedProduct.getCategory().getId()).orElse(null);
                if (cat != null) updatedProduct.setCategory(cat);
            } else if (updatedProduct.getCategory().getNom() != null && !updatedProduct.getCategory().getNom().isBlank()) {
                String targetMarket = updatedProduct.getMarketId() != null ? updatedProduct.getMarketId() : "vestimentaire";
                Category cat = categoryService.getCategoryByMarketAndNom(targetMarket, updatedProduct.getCategory().getNom())
                        .orElseGet(() -> {
                            Market m = marketService.getMarketById(targetMarket)
                                    .orElseGet(() -> marketService.createMarket(Market.builder().id(targetMarket).nom(targetMarket).build()));
                            return categoryService.createCategory(Category.builder().nom(updatedProduct.getCategory().getNom().trim()).market(m).build());
                        });
                updatedProduct.setCategory(cat);
            }
        }
        Product saved = productService.updateProduct(id, updatedProduct);
        return ResponseEntity.ok(saved);
    }
}
