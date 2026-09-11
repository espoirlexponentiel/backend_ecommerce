package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.service.CategoryService;
import com.ecommerce.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final com.ecommerce.backend.service.CloudinaryService cloudinaryService;

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

        if (product.getCategory() != null && product.getCategory().getNom() != null) {
            String norm = product.getCategory().getNom().trim().toLowerCase();
            Category category = categoryService.getCategoryByNom(norm)
                    .orElseGet(() -> categoryService.createCategory(new Category(null, norm, null)));
            product.setCategory(category);
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

        Category category = null;
        if (categoryNom != null && !categoryNom.isBlank()) {
            String normalizedCategoryName = categoryNom.trim().toLowerCase();
            category = categoryService.getCategoryByNom(normalizedCategoryName)
                    .orElseGet(() -> categoryService.createCategory(
                            new Category(null, normalizedCategoryName, null)
                    ));
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
                .marketId(marketId != null ? marketId : "vestimentaire")
                .sousTitre(sousTitre)
                .badge(badge)
                .tailles(tailles)
                .couleurs(couleurs)
                .composition(composition)
                .pointsForts(pointsForts)
                .imageUrl(imageUrl)
                .category(category)
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
        if (updatedProduct.getCategory() != null && updatedProduct.getCategory().getNom() != null) {
            String norm = updatedProduct.getCategory().getNom().trim().toLowerCase();
            Category category = categoryService.getCategoryByNom(norm)
                    .orElseGet(() -> categoryService.createCategory(new Category(null, norm, null)));
            updatedProduct.setCategory(category);
        }
        Product saved = productService.updateProduct(id, updatedProduct);
        return ResponseEntity.ok(saved);
    }
}
