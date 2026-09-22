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
				// Marché Unique par Défaut : Mode & Vestimentaire
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

				// Nettoyage automatique : Suppression de 'alimentation-generale' de la BDD si présent
				if (marketRepository.existsById("alimentation-generale")) {
					marketRepository.deleteById("alimentation-generale");
					System.out.println("🗑️ [7 SHOP] Ancien marché 'Alimentation Générale' supprimé de la BDD.");
				}

				System.out.println("✅ [7 SHOP] Marché Mode & Vestimentaire synchronisé !");
			} catch (Exception e) {
				System.err.println("⚠️ [7 SHOP] Erreur synchronisation marchés : " + e.getMessage());
			}
		};
	}

	@Bean
	public CommandLineRunner initSiteConfig(com.ecommerce.backend.repository.SiteConfigRepository siteConfigRepository) {
		return args -> {
			try {
				if (!siteConfigRepository.existsById(1L)) {
					com.ecommerce.backend.entity.SiteConfig config = com.ecommerce.backend.entity.SiteConfig.builder()
							.id(1L)
							.brandName("PolyShop")
							.brandBadge("P")
							.tagline("Boutique Officielle")
							.logoUrl("")
							.build();
					siteConfigRepository.save(config);
					System.out.println("✅ [PolyShop] Configuration identité & logo initialisée !");
				}
			} catch (Exception e) {
				System.err.println("⚠️ [7 SHOP] Erreur initialisation config site : " + e.getMessage());
			}
		};
	}
}
