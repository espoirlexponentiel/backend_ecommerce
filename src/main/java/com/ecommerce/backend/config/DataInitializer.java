package com.ecommerce.backend.config;

import com.ecommerce.backend.entity.Market;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.MarketRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MarketRepository marketRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUsers();
        initDefaultMarkets();
        log.info("🚀 Base de données prête : Marché principal configuré.");
    }

    private void initDefaultMarkets() {
        // 1. Marché Mode & Vestimentaire (Bleu #0066FF / Accent Jaune #FFB800)
        if (!marketRepository.existsById("vestimentaire")) {
            Market vestimentaire = Market.builder()
                    .id("vestimentaire")
                    .nom("Mode & Vestimentaire")
                    .slug("vestimentaire")
                    .icone("🛍️")
                    .couleurPrimaire("#0066FF")
                    .couleurPrimaireHover("#0052cc")
                    .couleurAccent("#FFB800")
                    .couleurHeroBg("linear-gradient(135deg, #ffffff 0%, #f4f8ff 50%, #fffbf0 100%)")
                    .heroTitre("Le style pur.\nBlanc, Bleu & Jaune.")
                    .heroSousTitre("Découvrez l'univers 7 Shop : sous-vêtements, chaussettes, débardeurs, pull-overs, ceintures, pantalons, coupes oversize et casquettes.")
                    .heroImageUrl("/images/hero-model.png?v=5")
                    .heroImageAlt("Modèle 7 Shop - Collection Mode Urbaine")
                    .heroImageWidth("480px")
                    .heroImageHeight("500px")
                    .heroImageObjectFit("contain")
                    .build();
            marketRepository.save(vestimentaire);
            log.info("✅ Marché initialisé en BDD : Mode & Vestimentaire (vestimentaire)");
        }

        // Nettoyage : suppression de 'alimentation-generale' si existant
        if (marketRepository.existsById("alimentation-generale")) {
            marketRepository.deleteById("alimentation-generale");
            log.info("🗑️ Marché supprimé de la BDD : alimentation-generale");
        }
    }

    private void initUsers() {
        // Compte Administrateur par défaut
        if (!userRepository.existsByEmail("admin@7shop.com")) {
            User admin = User.builder()
                    .email("admin@7shop.com")
                    .nom("Directeur 7 Shop")
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .provider("local")
                    .telephone("+221 77 000 00 01")
                    .adresse("Siège 7 Shop, Dakar")
                    .build();
            userRepository.save(admin);
            log.info("✅ Administrateur créé : admin@7shop.com / admin123");
        }

        // Compte Client Démo
        if (!userRepository.existsByEmail("client@7shop.com")) {
            User client = User.builder()
                    .email("client@7shop.com")
                    .nom("Alexandre Dupont")
                    .username("alexandre")
                    .password(passwordEncoder.encode("user123"))
                    .role("USER")
                    .provider("local")
                    .telephone("+221 77 654 32 10")
                    .adresse("14 Rue des Jardins, Dakar")
                    .build();
            userRepository.save(client);
            log.info("✅ Client Démo créé : client@7shop.com / user123");
        }
    }
}
