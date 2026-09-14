-- Conversion de la base et de toutes les tables vers utf8mb4 (support complet des accents et des emojis 4-bytes)
ALTER DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE markets CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE categories CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE products CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE orders CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE order_items CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE carts CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE cart_items CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Mise à jour propre des deux marchés fondateurs
UPDATE markets 
SET 
  nom = 'Mode & Vestimentaire',
  icone = '🛍️',
  couleur_primaire = '#0066FF',
  couleur_primaire_hover = '#0052cc',
  couleur_accent = '#FFB800',
  couleur_hero_bg = 'linear-gradient(135deg, #ffffff 0%, #f4f8ff 50%, #fffbf0 100%)',
  hero_titre = 'Le style pur.\nBlanc, Bleu & Jaune.',
  hero_sous_titre = 'Découvrez l''univers 7 Shop : sous-vêtements (boxers, chaussettes, débardeurs), tapettes, pull-overs, ceintures, pantalons, coupes oversize et casquettes. Des matières sélectionnées pour une tenue impeccable au quotidien.',
  hero_image_url = '/images/hero-model.png?v=5',
  hero_image_alt = 'Modèle 7 Shop - Collection Mode Urbaine',
  is_active = 1
WHERE id = 'vestimentaire';

UPDATE markets 
SET 
  nom = 'Alimentation Générale',
  icone = '🌾',
  couleur_primaire = '#EAB308',
  couleur_primaire_hover = '#CA8A04',
  couleur_accent = '#0066FF',
  couleur_hero_bg = 'linear-gradient(135deg, #ffffff 0%, #fffbeb 50%, #fef3c7 100%)',
  hero_titre = 'Le Goût & La Fraîcheur.\nVos Essentiels au Quotidien.',
  hero_sous_titre = 'Épicerie de qualité, riz parfumé de premier choix, huiles végétales pures, boissons rafraîchissantes, condiments et produits du terroir sélectionnés pour nourrir et régaler toute la famille.',
  hero_image_url = '/images/hero-food.png',
  hero_image_alt = 'Panier Alimentation Générale 7 Shop - Épicerie Fine & Terroir',
  is_active = 1
WHERE id = 'alimentation-generale';
