package com.ecommerce.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/markets")
public class MarketController {

    @GetMapping
    public ResponseEntity<?> getAvailableMarkets() {
        return ResponseEntity.ok(List.of(
            Map.of(
                "id", "vestimentaire",
                "slug", "vestimentaire",
                "nom", "Mode & Vestimentaire",
                "icone", "🛍️",
                "couleurPrimaire", "#0066FF",
                "couleurAccent", "#FFB800"
            ),
            Map.of(
                "id", "alimentation-generale",
                "slug", "alimentation-generale",
                "nom", "Alimentation Générale",
                "icone", "🌾",
                "couleurPrimaire", "#10B981",
                "couleurAccent", "#F59E0B"
            ),
            Map.of(
                "id", "electronique",
                "slug", "electronique",
                "nom", "High-Tech & Électronique",
                "icone", "⚡",
                "couleurPrimaire", "#6366F1",
                "couleurAccent", "#EC4899"
            ),
            Map.of(
                "id", "maison-deco",
                "slug", "maison-deco",
                "nom", "Maison & Décoration",
                "icone", "🛋️",
                "couleurPrimaire", "#8B5CF6",
                "couleurAccent", "#F59E0B"
            ),
            Map.of(
                "id", "beaute-sante",
                "slug", "beaute-sante",
                "nom", "Beauté & Santé",
                "icone", "✨",
                "couleurPrimaire", "#EC4899",
                "couleurAccent", "#10B981"
            )
        ));
    }
}
