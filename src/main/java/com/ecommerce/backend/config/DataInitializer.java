package com.ecommerce.backend.config;

import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUsers();
        initCatalog();
        initSampleOrders();
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

    private void initCatalog() {
        if (productRepository.count() > 0) {
            log.info("ℹ️ Le catalogue contient déjà {} articles.", productRepository.count());
            return;
        }

        log.info("🌱 Initialisation du catalogue multi-marchés 7 Shop...");

        // Map pour garder les catégories par nom normalisé
        Map<String, Category> catMap = new HashMap<>();
        String[] defaultCategories = {
                "boxers", "chaussettes", "casquettes", "t-shirts", "ensembles",
                "epicerie & riz", "huiles & condiments", "boissons & jus", "produits frais & epices",
                "smartphones", "audio & ecouteurs", "accessoires high-tech",
                "salon & deco", "linge de lit", "soins visage & corps", "parfums"
        };

        for (String catName : defaultCategories) {
            Category cat = categoryRepository.findByNomIgnoreCase(catName)
                    .orElseGet(() -> categoryRepository.save(new Category(null, catName, null)));
            catMap.put(catName, cat);
        }

        // 1. Mode & Vestimentaire
        Product p1 = Product.builder()
                .nom("Pack 3 Boxers Coton Stretch 7 Shop")
                .sousTitre("Bande élastique jacquard SEVEN CHOP & maintien anatomique")
                .category(catMap.get("boxers"))
                .marketId("vestimentaire")
                .badge("BEST-SELLER")
                .prix(34.9)
                .ancienPrix(42.0)
                .rating(4.92)
                .reviewCount(168)
                .stock(50)
                .imageUrl("https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?auto=format&fit=crop&w=800&q=80")
                .tailles("S, M, L, XL, XXL")
                .couleurs("Noir, Blanc, Bleu, Jaune")
                .description("Le basique indispensable du vestiaire masculin Seven Chop. Confectionné en coton peigné stretch ultra-doux (95% coton, 5% élasthanne). Coutures plates anti-frottements.")
                .composition("95% Coton peigné biologique, 5% Élasthanne.")
                .pointsForts("Coton peigné respirant 210 GSM hypoallergénique\nCeinture élastique ultra-souple sans marquage sur la peau\nPack de 3 pièces assorties")
                .build();

        Product p2 = Product.builder()
                .nom("Lot 5 Paires Chaussettes Hautes Côtelées")
                .sousTitre("Coton peigné côtelé & renforts talon-pointe")
                .category(catMap.get("chaussettes"))
                .marketId("vestimentaire")
                .badge("ESSENTIEL 7")
                .prix(24.9)
                .ancienPrix(29.9)
                .rating(4.95)
                .reviewCount(215)
                .stock(40)
                .imageUrl("https://images.unsplash.com/photo-1586350977771-b3b0abd50c82?auto=format&fit=crop&w=800&q=80")
                .tailles("39-42, 43-46")
                .couleurs("Blanc, Bleu, Jaune, Noir")
                .description("Les chaussettes hautes 7 Shop allient maintien parfait et style minimaliste. Maille côtelée souple et coutures invisibles aux orteils.")
                .composition("80% Coton peigné, 17% Polyamide, 3% Élasthanne.")
                .pointsForts("Maille côtelée qui ne glisse pas\nRenforts amortissants talon et pointe\nTeinture certifiée Oeko-Tex")
                .build();

        Product p3 = Product.builder()
                .nom("Casquette Signature 7 Shop Broderie 3D")
                .sousTitre("Sergé de coton épais, boucle métal gravée & visière pré-courbée")
                .category(catMap.get("casquettes"))
                .marketId("vestimentaire")
                .badge("COLLECTION 7")
                .prix(28.0)
                .ancienPrix(35.0)
                .rating(4.97)
                .reviewCount(142)
                .stock(30)
                .imageUrl("https://images.unsplash.com/photo-1588850561407-ed78c282e89b?auto=format&fit=crop&w=800&q=80")
                .tailles("Taille Unique")
                .couleurs("Noir Intense, Jaune 7, Bleu Marine, Blanc Craie")
                .description("Casquette structurée 6 panneaux emblématique Seven Chop. Broderie en relief haute précision à l'avant.")
                .composition("100% Coton sergé lourd 280 GSM. Boucle en acier brossé.")
                .pointsForts("Broderie signature Seven Chop haute précision\nBandeau intérieur éponge anti-transpiration\nAjustement millimétrique par sangle arrière")
                .build();

        Product p4 = Product.builder()
                .nom("T-Shirt Oversize Coton Lourd 240g 7 Shop")
                .sousTitre("Col rond renforcé 3cm & tombé tombant impeccable")
                .category(catMap.get("t-shirts"))
                .marketId("vestimentaire")
                .badge("NOUVEAU")
                .prix(45.0)
                .ancienPrix(null)
                .rating(4.89)
                .reviewCount(76)
                .stock(35)
                .imageUrl("https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=800&q=80")
                .tailles("S, M, L, XL")
                .couleurs("Noir, Blanc, Bleu Seven, Jaune")
                .description("Le t-shirt oversize iconique 7 Shop. Jersey 100% coton lourd ultra-durable au tombé droit et moderne.")
                .composition("100% Coton biologique peigné 240 GSM.")
                .pointsForts("Jersey de coton ultra-lourd 240 GSM\nCol montant 3cm double surpiqûre\nCoupe boxy moderne")
                .build();

        // 2. Alimentation Générale
        Product p5 = Product.builder()
                .nom("Riz Parfumé Jasmin 7 Shop Superbe Qualité 5kg")
                .sousTitre("Grain long parfumé & sélection rigoureuse des meilleures récoltes")
                .category(catMap.get("epicerie & riz"))
                .marketId("alimentation-generale")
                .badge("BEST-SELLER")
                .prix(14.5)
                .ancienPrix(17.0)
                .rating(4.96)
                .reviewCount(340)
                .stock(85)
                .imageUrl("https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=800&q=80")
                .tailles("5 kg, 10 kg, 25 kg")
                .couleurs("Sac 5kg, Sac 10kg, Sac 25kg")
                .description("Le riz parfumé au jasmin 7 Shop est reconnu pour sa texture tendre, son arôme délicat et sa tenue de cuisson parfaite.")
                .composition("100% Riz jasmin long grain blanc brisures 5%.")
                .pointsForts("Parfum naturel intense et cuisson sans coller\nRiche en énergie et nutriments essentiels\nSac tissé ultra-résistant")
                .build();

        Product p6 = Product.builder()
                .nom("Huile de Tournesol Pure 7 Shop 5 Litres")
                .sousTitre("100% Pure, sans cholestérol & enrichie en Vitamine E")
                .category(catMap.get("huiles & condiments"))
                .marketId("alimentation-generale")
                .badge("ESSENTIEL")
                .prix(12.9)
                .ancienPrix(15.5)
                .rating(4.92)
                .reviewCount(210)
                .stock(60)
                .imageUrl("https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=800&q=80")
                .tailles("1 L, 5 L")
                .couleurs("Bidon 5L, Bouteille 1L")
                .description("Huile de tournesol raffinée de qualité supérieure, idéale pour la friture, la cuisson et l'assaisonnement.")
                .composition("100% Huile de tournesol raffinée, antioxydant (Vitamine E).")
                .pointsForts("Riche en acides gras insaturés et oméga-6\nPoint de fumée élevé\nBidon ergonomique anti-goutte")
                .build();

        Product p7 = Product.builder()
                .nom("Pack 6 Bouteilles Pur Jus de Mangue & Passion 1L")
                .sousTitre("Fruits tropicaux gorgés de soleil, sans conservateurs")
                .category(catMap.get("boissons & jus"))
                .marketId("alimentation-generale")
                .badge("PUR FRUIT")
                .prix(16.8)
                .ancienPrix(19.5)
                .rating(4.95)
                .reviewCount(185)
                .stock(40)
                .imageUrl("https://images.unsplash.com/photo-1613478223719-2ab802602423?auto=format&fit=crop&w=800&q=80")
                .tailles("Pack 6x1L, Bouteille 1L")
                .couleurs("Mangue-Passion, Bissap-Menthe, Gingembre-Ananas")
                .description("Un cocktail tropical riche et onctueux mariant la douceur de la mangue mûre et le peps du fruit de la passion.")
                .composition("Jus de mangue (60%), jus de fruit de la passion (40%), sans sucre ajouté.")
                .pointsForts("Riche en vitamine C naturelle\nSans colorants ni conservateurs\nPack familial économique")
                .build();

        productRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6, p7));
        log.info("✅ 7 Produits phares initialisés avec succès dans la base.");
    }

    private void initSampleOrders() {
        if (orderRepository.count() > 0) {
            return;
        }

        User client = userRepository.findByEmail("client@7shop.com").orElse(null);
        if (client == null) return;

        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) return;

        Product p1 = products.get(0);
        Product p2 = products.size() > 1 ? products.get(1) : p1;

        Order sampleOrder = Order.builder()
                .user(client)
                .createdAt(LocalDateTime.now().minusHours(3))
                .status(OrderStatus.EN_ATTENTE)
                .totalAmount(p1.getPrix() * 2 + 4.9)
                .fraisPort(4.9)
                .adresseLivraison("14 Rue des Jardins, Dakar, Sénégal")
                .telephone("+221 77 654 32 10")
                .modePaiement("Carte Bancaire")
                .marche("Mode & Vestimentaire")
                .build();

        sampleOrder = orderRepository.save(sampleOrder);

        OrderItem item1 = OrderItem.builder()
                .order(sampleOrder)
                .product(p1)
                .quantity(2)
                .unitPrice(p1.getPrix())
                .taille("L")
                .couleur("Noir")
                .build();

        orderItemRepository.save(item1);
        log.info("✅ Commande d'exemple créée avec succès.");
    }
}
