package com.ecommerce.backend;

import com.ecommerce.backend.entity.Market;
import com.ecommerce.backend.repository.MarketRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// Charger automatiquement le fichier .env en local s'il existe
		try {
			java.io.File envFile = new java.io.File(".env");
			if (envFile.exists()) {
				java.nio.file.Files.lines(envFile.toPath())
					.map(String::trim)
					.filter(line -> !line.isEmpty() && !line.startsWith("#") && line.contains("="))
					.forEach(line -> {
						int idx = line.indexOf("=");
						String key = line.substring(0, idx).trim();
						String val = line.substring(idx + 1).trim();
						if (System.getProperty(key) == null && System.getenv(key) == null) {
							System.setProperty(key, val);
						}
					});
			}
		} catch (Exception ignored) {}

		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner initCleanMarkets(MarketRepository marketRepository) {
		return args -> {
			try {
				// Marché 1 : Mode & Vestimentaire
				Market vestimentaire = marketRepository.findById("vestimentaire").orElse(new Market());
				vestimentaire.setId("vestimentaire");
				vestimentaire.setSlug("vestimentaire");
				vestimentaire.setNom("Mode & Vestimentaire");
				vestimentaire.setIcone("🛍️");
				vestimentaire.setCouleurPrimaire("#0066FF");
				vestimentaire.setCouleurPrimaireHover("#0052cc");
				vestimentaire.setCouleurAccent("#FFB800");
				vestimentaire.setCouleurHeroBg("linear-gradient(135deg, #ffffff 0%, #f4f8ff 50%, #fffbf0 100%)");
				vestimentaire.setHeroTitre("Le style pur.\nBlanc, Bleu & Jaune.");
				vestimentaire.setHeroSousTitre("Découvrez l'univers 7 Shop : sous-vêtements (boxers, chaussettes, débardeurs), tapettes, pull-overs, ceintures, pantalons, coupes oversize et casquettes. Des matières sélectionnées pour une tenue impeccable au quotidien.");
				vestimentaire.setHeroImageUrl("/images/hero-model.png?v=5");
				vestimentaire.setHeroImageAlt("Modèle 7 Shop - Collection Mode Urbaine");
				vestimentaire.setIsActive(true);
				marketRepository.save(vestimentaire);

				// Marché 2 : Alimentation Générale
				Market alimentation = marketRepository.findById("alimentation-generale").orElse(new Market());
				alimentation.setId("alimentation-generale");
				alimentation.setSlug("alimentation-generale");
				alimentation.setNom("Alimentation Générale");
				alimentation.setIcone("🌾");
				alimentation.setCouleurPrimaire("#EAB308");
				alimentation.setCouleurPrimaireHover("#CA8A04");
				alimentation.setCouleurAccent("#0066FF");
				alimentation.setCouleurHeroBg("linear-gradient(135deg, #ffffff 0%, #fffbeb 50%, #fef3c7 100%)");
				alimentation.setHeroTitre("Le Goût & La Fraîcheur.\nVos Essentiels au Quotidien.");
				alimentation.setHeroSousTitre("Épicerie de qualité, riz parfumé de premier choix, huiles végétales pures, boissons rafraîchissantes, condiments et produits du terroir sélectionnés pour nourrir et régaler toute la famille.");
				alimentation.setHeroImageUrl("/images/hero-food.png");
				alimentation.setHeroImageAlt("Panier Alimentation Générale 7 Shop - Épicerie Fine & Terroir");
				alimentation.setIsActive(true);
				marketRepository.save(alimentation);

				System.out.println("✅ [7 SHOP] Marchés fondateurs synchronisés avec encodage UTF-8 et icônes certifiées !");
			} catch (Exception e) {
				System.err.println("⚠️ [7 SHOP] Erreur synchronisation marchés : " + e.getMessage());
			}
		};
	}
}
