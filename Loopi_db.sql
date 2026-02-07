-- ===========================================
-- BASE DE DONNÉES : LOOPI (MOTS DE PASSE EN CLAIR)
-- Version corrigée pour MySQL
-- ===========================================
CREATE DATABASE IF NOT EXISTS loopi_db;
USE loopi_db;

-- ===========================================
-- TABLES DE BASE
-- ===========================================

-- Table Genre
CREATE TABLE genre (
                       id_genre INT PRIMARY KEY AUTO_INCREMENT,
                       sexe VARCHAR(50) NOT NULL UNIQUE
);

-- Table users (évite le mot réservé User)
CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       nom VARCHAR(100) NOT NULL,
                       prenom VARCHAR(100) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE, -- Réduit pour éviter l'erreur de clé
                       password VARCHAR(100) NOT NULL, -- EN CLAIR
                       photo VARCHAR(255) DEFAULT 'default.jpg',
                       role ENUM('admin', 'organisateur', 'participant') DEFAULT 'participant',
                       id_genre INT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (id_genre) REFERENCES genre(id_genre) ON DELETE SET NULL,
                       INDEX idx_email (email),
                       INDEX idx_role (role)
);

-- ===========================================
-- MODULE PRODUITS ET BOUTIQUE
-- ===========================================

-- Table CategoryProduit
CREATE TABLE category_produit (
                                  id_cat INT PRIMARY KEY AUTO_INCREMENT,
                                  nom_cat VARCHAR(100) NOT NULL UNIQUE,
                                  description TEXT,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table Produit
CREATE TABLE produit (
                         id_produit INT PRIMARY KEY AUTO_INCREMENT,
                         nom_produit VARCHAR(200) NOT NULL,
                         description TEXT,
                         prix DECIMAL(10,2) NOT NULL,
                         image_produit VARCHAR(255),
                         id_cat INT NOT NULL,
                         id_user INT,
                         quantite_stock INT DEFAULT 0,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         FOREIGN KEY (id_cat) REFERENCES category_produit(id_cat) ON DELETE CASCADE,
                         FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE SET NULL,
                         INDEX idx_categorie (id_cat)
);

-- ===========================================
-- MODULE COMMANDES
-- ===========================================

-- Table Commande
CREATE TABLE commande (
                          id_commande INT PRIMARY KEY AUTO_INCREMENT,
                          id_user INT NOT NULL,
                          quantite INT NOT NULL DEFAULT 0,
                          total_avant_reduction DECIMAL(10,2) NOT NULL,
                          applique_code VARCHAR(50),
                          total DECIMAL(10,2) NOT NULL,
                          statut ENUM('en_attente', 'confirmee', 'en_preparation', 'expediee', 'livree', 'annulee') DEFAULT 'en_attente',
                          adresse_livraison TEXT,
                          telephone VARCHAR(20),
                          date_commande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          date_livraison DATE,
                          FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE,
                          INDEX idx_user (id_user),
                          INDEX idx_date (date_commande)
);

-- Table Content
CREATE TABLE content (
                         id_content INT PRIMARY KEY AUTO_INCREMENT,
                         id_commande INT NOT NULL,
                         id_produit INT NOT NULL,
                         quantite INT NOT NULL CHECK (quantite > 0),
                         prix_unitaire DECIMAL(10,2) NOT NULL,
                         FOREIGN KEY (id_commande) REFERENCES commande(id_commande) ON DELETE CASCADE,
                         FOREIGN KEY (id_produit) REFERENCES produit(id_produit) ON DELETE CASCADE,
                         UNIQUE KEY unique_commande_produit (id_commande, id_produit)
);

-- ===========================================
-- MODULE DONS ET COUPONS
-- ===========================================

-- Table Donation
CREATE TABLE donation (
                          id_donation INT PRIMARY KEY AUTO_INCREMENT,
                          id_user INT NOT NULL,
                          amount DECIMAL(10,2) NOT NULL,
                          donation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          statut ENUM('en_attente', 'confirme', 'annule') DEFAULT 'en_attente',
                          mode_paiement VARCHAR(50),
                          message TEXT,
                          FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE,
                          INDEX idx_user (id_user),
                          INDEX idx_date (donation_date)
);

-- Table Coupon
CREATE TABLE coupon (
                        id_coupon INT PRIMARY KEY AUTO_INCREMENT,
                        code VARCHAR(50) NOT NULL UNIQUE,
                        discount_percent DECIMAL(5,2) NOT NULL,
                        donation_date DATE,
                        used BOOLEAN DEFAULT FALSE,
                        id_user INT,
                        id_donation INT,
                        expiration_date DATE,
                        min_amount DECIMAL(10,2) DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE SET NULL,
                        FOREIGN KEY (id_donation) REFERENCES donation(id_donation) ON DELETE SET NULL,
                        INDEX idx_code (code),
                        INDEX idx_used (used)
);

-- ===========================================
-- MODULE ÉVÉNEMENTS
-- ===========================================

-- Table Evenement
CREATE TABLE evenement (
                           id_evenement INT PRIMARY KEY AUTO_INCREMENT,
                           titre VARCHAR(200) NOT NULL,
                           description TEXT,
                           date_evenement DATETIME NOT NULL,
                           lieu VARCHAR(200),
                           id_organisateur INT NOT NULL,
                           capacite_max INT,
                           image_evenement VARCHAR(255),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (id_organisateur) REFERENCES users(id) ON DELETE CASCADE
);

-- Table Participation
CREATE TABLE participation (
                               id INT PRIMARY KEY AUTO_INCREMENT,
                               id_user INT NOT NULL,
                               id_evenement INT NOT NULL,
                               contact VARCHAR(100) NOT NULL,
                               age INT CHECK (age >= 0),
                               date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               statut ENUM('inscrit', 'present', 'absent') DEFAULT 'inscrit',
                               FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE,
                               FOREIGN KEY (id_evenement) REFERENCES evenement(id_evenement) ON DELETE CASCADE,
                               UNIQUE KEY unique_participation (id_user, id_evenement)
);

-- ===========================================
-- DONNÉES D'EXEMPLE (MOTS DE PASSE EN CLAIR)
-- ===========================================

-- Insertion des genres
INSERT INTO genre (sexe) VALUES
                             ('Homme'),
                             ('Femme'),
                             ('Non spécifié');

-- Insertion des utilisateurs avec mots de passe en clair
INSERT INTO users (nom, prenom, email, password, role, id_genre) VALUES
                                                                     ('Admin', 'System', 'admin@loopi.tn', 'admin123', 'admin', 1),
                                                                     ('Organisateur', 'Eco', 'organisateur@loopi.tn', 'org123', 'organisateur', 1),
                                                                     ('Participant', 'Test', 'participant@loopi.tn', 'part123', 'participant', 2),
                                                                     ('Ben', 'Ali', 'ben.ali@email.com', 'ben123', 'participant', 1),
                                                                     ('Dupont', 'Marie', 'marie.dupont@email.com', 'marie123', 'participant', 2),
                                                                     ('Martin', 'Pierre', 'pierre@email.com', 'pierre123', 'participant', 1);

-- Insertion des catégories
INSERT INTO category_produit (nom_cat, description) VALUES
                                                        ('Mobilier recyclé', 'Meubles fabriqués à partir de matériaux recyclés'),
                                                        ('Décorations écologiques', 'Objets décoratifs respectueux de l''environnement'),
                                                        ('Accessoires durables', 'Accessoires mode et utilitaires écologiques'),
                                                        ('Jouets éducatifs', 'Jouets fabriqués à partir de matériaux recyclés');

-- Insertion de produits
INSERT INTO produit (nom_produit, description, prix, image_produit, id_cat, id_user, quantite_stock) VALUES
                                                                                                         ('Table basse en palette', 'Table basse design en palettes recyclées', 120.50, 'table.jpg', 1, 2, 10),
                                                                                                         ('Lampes en bouteilles', 'Lampes créatives en bouteilles recyclées', 45.99, 'lampe.jpg', 2, 2, 25),
                                                                                                         ('Sac en pneus recyclés', 'Sac à main en pneus recyclés', 65.00, 'sac.jpg', 3, 2, 15),
                                                                                                         ('Jouets en bois', 'Jouets éducatifs en bois recyclé', 22.99, 'jouet.jpg', 4, 2, 30);

-- Insertion d'événements
INSERT INTO evenement (titre, description, date_evenement, lieu, id_organisateur, capacite_max) VALUES
                                                                                                    ('Nettoyage de plage', 'Journée de nettoyage', '2024-06-15 09:00:00', 'Plage Sousse', 2, 50),
                                                                                                    ('Atelier recyclage', 'Apprenez à recycler', '2024-06-20 14:00:00', 'Centre Tunis', 2, 30);

-- Insertion de participations
INSERT INTO participation (id_user, id_evenement, contact, age) VALUES
                                                                    (3, 1, 'participant@loopi.tn', 28),
                                                                    (4, 1, 'ben.ali@email.com', 35),
                                                                    (5, 2, 'marie.dupont@email.com', 32);

-- Insertion de dons
INSERT INTO donation (id_user, amount, statut, mode_paiement) VALUES
                                                                  (3, 50.00, 'confirme', 'carte'),
                                                                  (4, 100.00, 'confirme', 'paypal'),
                                                                  (5, 25.00, 'en_attente', 'carte');

-- Insertion de coupons
INSERT INTO coupon (code, discount_percent, donation_date, id_user, id_donation, expiration_date) VALUES
                                                                                                      ('ECO10', 10.00, CURDATE(), 3, 1, DATE_ADD(CURDATE(), INTERVAL 30 DAY)),
                                                                                                      ('GREEN15', 15.00, CURDATE(), 4, 2, DATE_ADD(CURDATE(), INTERVAL 60 DAY));

-- Insertion d'une commande
INSERT INTO commande (id_user, quantite, total_avant_reduction, applique_code, total, statut, adresse_livraison, telephone) VALUES
                                                                                                                                (3, 2, 166.50, 'ECO10', 149.85, 'livree', '123 Rue Tunis', '12345678'),
                                                                                                                                (4, 1, 45.99, NULL, 45.99, 'en_attente', '456 Avenue Sousse', '87654321');

-- Insertion du contenu des commandes
INSERT INTO content (id_commande, id_produit, quantite, prix_unitaire) VALUES
                                                                           (1, 1, 1, 120.50),
                                                                           (1, 3, 1, 46.00),
                                                                           (2, 2, 1, 45.99);

-- ===========================================
-- VUE POUR VOIR LES UTILISATEURS
-- ===========================================

CREATE OR REPLACE VIEW v_users_passwords AS
SELECT
    id,
    nom,
    prenom,
    email,
    password, -- MOT DE PASSE EN CLAIR
    role,
    photo,
    created_at
FROM users;

-- ===========================================
-- REQUÊTES DE TEST
-- ===========================================

-- Voir tous les utilisateurs avec leurs mots de passe
SELECT * FROM v_users_passwords;

-- Tester la connexion (version simple)
SELECT * FROM users WHERE email = 'admin@loopi.tn' AND password = 'admin123';

-- Voir tous les produits
SELECT
    p.id_produit,
    p.nom_produit,
    p.prix,
    c.nom_cat,
    CONCAT(u.prenom, ' ', u.nom) AS vendeur
FROM produit p
         JOIN category_produit c ON p.id_cat = c.id_cat
         LEFT JOIN users u ON p.id_user = u.id;

-- Statistiques simples
SELECT
    role,
    COUNT(*) as nombre_utilisateurs,
    GROUP_CONCAT(email SEPARATOR ', ') as emails
FROM users
GROUP BY role;

-- Voir les commandes avec détails clients
SELECT
    c.id_commande,
    c.date_commande,
    c.total,
    c.statut,
    CONCAT(u.prenom, ' ', u.nom) as client,
    u.email,
    u.password as mot_de_passe -- EN CLAIR
FROM commande c
         JOIN users u ON c.id_user = u.id;

-- ===========================================
-- FIN DU SCRIPT
-- ===========================================

SELECT 'Base de données LOOPI créée avec succès !' as message;
SELECT 'MOTS DE PASSE EN CLAIR - POUR DÉVELOPPEMENT SEULEMENT' as avertissement;
SELECT NOW() as date_creation;

-- Afficher les tables créées
SHOW TABLES;

-- Afficher quelques utilisateurs avec leurs mots de passe
SELECT
    id,
    CONCAT(prenom, ' ', nom) as nom_complet,
    email,
    password as mot_de_passe,
    role
FROM users
ORDER BY id;



-- Ajouter une colonne pour le sexe dans users
ALTER TABLE users ADD COLUMN id_genre INT NULL;
ALTER TABLE users ADD FOREIGN KEY (id_genre) REFERENCES genre(id_genre);

-- Ajouter les colonnes manquantes selon votre diagramme
ALTER TABLE users
    ADD COLUMN photo VARCHAR(255) DEFAULT 'default.jpg',
ADD COLUMN role ENUM('admin', 'organisateur', 'participant') DEFAULT 'participant';

-- Créer une vue pour les utilisateurs avec leurs genres
CREATE OR REPLACE VIEW v_users_complete AS
SELECT
    u.id,
    u.nom,
    u.prenom,
    u.email,
    u.password,
    u.photo,
    u.role,
    g.sexe,
    u.created_at,
    u.updated_at
FROM users u
         LEFT JOIN genre g ON u.id_genre = g.id_genre;

-- Table pour stocker les tokens de réinitialisation
CREATE TABLE password_reset_tokens (
                                       id INT PRIMARY KEY AUTO_INCREMENT,
                                       email VARCHAR(100) NOT NULL,
                                       token VARCHAR(100) NOT NULL,
                                       expires_at TIMESTAMP NOT NULL,
                                       used BOOLEAN DEFAULT FALSE,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       INDEX idx_email (email),
                                       INDEX idx_token (token)
);

-- Procédure pour nettoyer les tokens expirés
DELIMITER //
CREATE PROCEDURE CleanExpiredTokens()
BEGIN
DELETE FROM password_reset_tokens WHERE expires_at < NOW();
END //
DELIMITER ;

-- Événement pour nettoyer automatiquement
CREATE EVENT CleanTokensEvent
ON SCHEDULE EVERY 1 HOUR
DO CALL CleanExpiredTokens();