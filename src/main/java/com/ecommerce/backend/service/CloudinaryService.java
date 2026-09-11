package com.ecommerce.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier image est vide ou invalide");
        }
        try {
            log.info("☁️ Uploading image to Cloudinary folder: {}", folder);
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder != null ? folder : "ecommerce_products",
                            "resource_type", "auto"
                    )
            );
            String secureUrl = uploadResult.get("secure_url").toString();
            log.info("✅ Image uploaded to Cloudinary successfully: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("❌ Erreur lors du téléversement de l'image vers Cloudinary", e);
            throw new RuntimeException("Erreur lors du téléversement de l'image vers Cloudinary : " + e.getMessage(), e);
        }
    }
}
