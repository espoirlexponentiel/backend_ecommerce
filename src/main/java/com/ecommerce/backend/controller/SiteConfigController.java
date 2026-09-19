package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.SiteConfig;
import com.ecommerce.backend.repository.SiteConfigRepository;
import com.ecommerce.backend.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SiteConfigController {

    @Autowired
    private SiteConfigRepository siteConfigRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // 🔓 Récupérer la configuration de marque & logo du site (Public)
    @GetMapping
    public ResponseEntity<?> getSiteConfig() {
        SiteConfig config = siteConfigRepository.findById(1L).orElseGet(() -> {
            SiteConfig defaultConfig = SiteConfig.builder()
                    .id(1L)
                    .brandName("PolyShop")
                    .brandBadge("P")
                    .tagline("Boutique Officielle")
                    .logoUrl("")
                    .build();
            return siteConfigRepository.save(defaultConfig);
        });
        return ResponseEntity.ok(config);
    }

    // 🔐 Mettre à jour les textes & logo (Admin)
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSiteConfig(@RequestBody SiteConfig updatedConfig) {
        SiteConfig config = siteConfigRepository.findById(1L).orElseGet(() -> 
            SiteConfig.builder().id(1L).brandName("PolyShop").brandBadge("P").build()
        );

        if (updatedConfig.getBrandName() != null && !updatedConfig.getBrandName().isBlank()) {
            config.setBrandName(updatedConfig.getBrandName().trim());
        }
        if (updatedConfig.getBrandBadge() != null) {
            config.setBrandBadge(updatedConfig.getBrandBadge().trim());
        }
        if (updatedConfig.getLogoUrl() != null) {
            config.setLogoUrl(updatedConfig.getLogoUrl().trim());
        }
        if (updatedConfig.getTagline() != null) {
            config.setTagline(updatedConfig.getTagline().trim());
        }

        SiteConfig saved = siteConfigRepository.save(config);
        return ResponseEntity.ok(Map.of(
                "message", "✅ Identité du site mise à jour avec succès",
                "config", saved
        ));
    }

    // 🔐 Téléverser une image de logo vers Cloudinary (Admin)
    @PostMapping(value = "/upload-logo", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadLogo(@RequestParam("file") MultipartFile file) {
        try {
            String secureUrl = cloudinaryService.uploadImage(file, "ecommerce_branding");

            SiteConfig config = siteConfigRepository.findById(1L).orElseGet(() -> 
                SiteConfig.builder().id(1L).brandName("PolyShop").brandBadge("P").build()
            );
            config.setLogoUrl(secureUrl);
            SiteConfig saved = siteConfigRepository.save(config);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Logo téléversé et appliqué avec succès !",
                    "logoUrl", secureUrl,
                    "config", saved
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Échec du téléversement du logo : " + e.getMessage()));
        }
    }
}
