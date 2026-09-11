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

    // 📂 Dossier d’upload (dans le projet)
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // 🔓 Récupérer tous les produits
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
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

    // 🔐 Créer un nouveau produit (image facultative)
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(
            @RequestParam("nom") String productNom,
            @RequestParam("description") String productDescription,
            @RequestParam("prix") Double productPrix,
            @RequestParam("stock") Integer productStock,
            @RequestParam("category") String categoryNom,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            HttpServletRequest request
    ) throws IOException {

        // 🔄 Normaliser le nom de catégorie
        String normalizedCategoryName = categoryNom.trim().toLowerCase();

        Category category = categoryService.getCategoryByNom(normalizedCategoryName)
                .orElseGet(() -> categoryService.createCategory(
                        new Category(null, normalizedCategoryName, null)
                ));

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String originalName = imageFile.getOriginalFilename();
            if (originalName != null && !originalName.isBlank()) {
                // 📂 Créer le dossier si nécessaire
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // 📂 Sauvegarder le fichier via InputStream (pas de tmpdir)
                File destination = new File(UPLOAD_DIR, originalName);
                try (InputStream inputStream = imageFile.getInputStream()) {
                    Files.copy(inputStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                // 🌐 Construire l’URL complète
                String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                imageUrl = baseUrl + "/uploads/" + originalName;
                System.out.println("Upload path: " + destination.getAbsolutePath());
            }
        }

        Product product = new Product();
        product.setNom(productNom);
        product.setDescription(productDescription);
        product.setPrix(productPrix);
        product.setStock(productStock);
        product.setImageUrl(imageUrl);
        product.setCategory(category);

        Product saved = productService.createProduct(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "✅ Produit créé avec succès", "product", saved)
        );
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
        Product saved = productService.updateProduct(id, updatedProduct);
        return ResponseEntity.ok(saved);
    }
}
