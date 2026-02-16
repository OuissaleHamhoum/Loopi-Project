package edu.Loopi.services;

// ===== IMPORTS NÉCESSAIRES =====
// Ces imports permettent d'utiliser JUnit et les assertions
import edu.Loopi.entities.Event;
import edu.Loopi.entities.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import java.time.LocalDateTime;
import java.util.List;

// Import static pour utiliser assertTrue, assertFalse, etc. sans écrire Assertions. devant
import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de test pour EventService
 *
 * RÔLE : Tester automatiquement toutes les méthodes de EventService
 *
 * ANNOTATIONS JUnit expliquées :
 * @Test : Indique que la méthode est un test
 * @BeforeAll : S'exécute UNE SEULE fois avant tous les tests
 * @AfterAll : S'exécute UNE SEULE fois après tous les tests
 * @BeforeEach : S'exécute AVANT chaque test
 * @AfterEach : S'exécute APRÈS chaque test
 * @Order : Définit l'ordre d'exécution des tests
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Permet d'ordonner les tests avec @Order
public class EventServiceTest {

    // ===== ATTRIBUTS =====
    private static EventService eventService; // Le service à tester
    private static int testEventId;           // Pour stocker l'ID de l'événement créé
    private static int testOrganisateurId = 1; // ID d'un organisateur existant (À MODIFIER)
    private static int testUserId = 2;         // ID d'un utilisateur existant (À MODIFIER)

    /**
     * S'exécute UNE SEULE fois avant TOUS les tests
     * Initialise le service
     */
    @BeforeAll
    static void setup() {
        eventService = new EventService();
        System.out.println("=== DÉBUT DES TESTS ===");
    }

    /**
     * S'exécute UNE SEULE fois après TOUS les tests
     */
    @AfterAll
    static void tearDown() {
        System.out.println("=== FIN DES TESTS ===");
    }

    /**
     * S'exécute AVANT chaque test
     */
    @BeforeEach
    void setUp() {
        System.out.println("\n--- Nouveau test ---");
    }

    /**
     * S'exécute APRÈS chaque test
     */
    @AfterEach
    void cleanUp() {
        System.out.println("--- Test terminé ---\n");
    }

    // ============ TEST 1 : AJOUTER UN ÉVÉNEMENT ============
    /**
     * Test de la méthode addEvent()
     * Vérifie qu'on peut ajouter un événement dans la base de données
     */
    @Test
    @Order(1)
    void testAjouterEvenement() {
        System.out.println("TEST 1 : Ajouter un événement");

        // 1. CRÉER un événement
        Event event = new Event();
        event.setTitre("Conférence Java");
        event.setDescription("Apprendre JUnit");
        event.setDate_evenement(LocalDateTime.now().plusDays(5));
        event.setLieu("Esprit Tunis");
        event.setId_organisateur(testOrganisateurId);
        event.setCapacite_max(50);
        event.setImage_evenement("image.jpg");

        // 2. EXÉCUTER la méthode à tester
        boolean resultat = eventService.addEvent(event);

        // 3. VÉRIFIER le résultat avec des ASSERTIONS
        assertTrue(resultat, "L'ajout devrait réussir");
        assertNotNull(event.getId_evenement(), "L'ID ne devrait pas être null");

        // Sauvegarder l'ID pour les tests suivants
        testEventId = event.getId_evenement();

        // 4. VÉRIFIER que l'événement existe en base
        Event eventRecupere = eventService.getEventById(testEventId);
        assertNotNull(eventRecupere, "L'événement devrait exister en base");

        System.out.println("✅ Ajout réussi - ID généré : " + testEventId);
    }

    // ============ TEST 2 : RÉCUPÉRER UN ÉVÉNEMENT ============
    /**
     * Test de la méthode getEventById()
     * Vérifie qu'on peut récupérer un événement par son ID
     */
    @Test
    @Order(2)
    void testRecupererEvenement() {
        System.out.println("TEST 2 : Récupérer un événement par ID");

        // Vérifier que l'ID de test est valide
        assertTrue(testEventId > 0, "L'ID de test doit être valide");

        // Récupérer l'événement
        Event event = eventService.getEventById(testEventId);

        // Vérifications
        assertNotNull(event, "L'événement ne devrait pas être null");
        assertEquals(testEventId, event.getId_evenement(), "L'ID devrait correspondre");
        assertNotNull(event.getTitre(), "Le titre ne devrait pas être null");

        System.out.println("✅ Récupération réussie - Titre : " + event.getTitre());
    }

    // ============ TEST 3 : MODIFIER UN ÉVÉNEMENT ============
    /**
     * Test de la méthode updateEvent()
     * Vérifie qu'on peut modifier un événement existant
     */
    @Test
    @Order(3)
    void testModifierEvenement() {
        System.out.println("TEST 3 : Modifier un événement");

        // Récupérer l'événement
        Event event = eventService.getEventById(testEventId);
        assertNotNull(event, "L'événement à modifier doit exister");

        // Modifier les données
        String nouveauTitre = "Conférence Java Avancée";
        event.setTitre(nouveauTitre);
        event.setCapacite_max(100);

        // Exécuter la modification
        boolean resultat = eventService.updateEvent(event);

        // Vérifier que la modification a réussi
        assertTrue(resultat, "La modification devrait réussir");

        // Vérifier que les changements sont en base
        Event eventModifie = eventService.getEventById(testEventId);
        assertEquals(nouveauTitre, eventModifie.getTitre(), "Le titre devrait être modifié");
        assertEquals(100, eventModifie.getCapacite_max(), "La capacité devrait être modifiée");

        System.out.println("✅ Modification réussie - Nouveau titre : " + nouveauTitre);
    }

    // ============ TEST 4 : LISTER TOUS LES ÉVÉNEMENTS ============
    /**
     * Test de la méthode getAllEvents()
     * Vérifie qu'on peut récupérer tous les événements
     */
    @Test
    @Order(4)
    void testListerTousEvenements() {
        System.out.println("TEST 4 : Lister tous les événements");

        List<Event> evenements = eventService.getAllEvents();

        assertNotNull(evenements, "La liste ne devrait pas être null");
        assertFalse(evenements.isEmpty(), "La liste ne devrait pas être vide");

        System.out.println("✅ Liste récupérée - Nombre d'événements : " + evenements.size());
    }

    // ============ TEST 5 : INSCRIRE UN PARTICIPANT ============
    /**
     * Test de la méthode inscrireParticipant()
     * Vérifie qu'on peut inscrire un utilisateur à un événement
     */
    @Test
    @Order(5)
    void testInscrireParticipant() {
        System.out.println("TEST 5 : Inscrire un participant");

        // Inscrire l'utilisateur
        boolean resultat = eventService.inscrireParticipant(
                testEventId,
                testUserId,
                "etudiant@esprit.tn",
                22
        );

        assertTrue(resultat, "L'inscription devrait réussir");

        // Vérifier qu'il est bien inscrit
        boolean estInscrit = eventService.isParticipant(testEventId, testUserId);
        assertTrue(estInscrit, "L'utilisateur devrait être inscrit");

        System.out.println("✅ Inscription réussie");
    }

    // ============ TEST 6 : LISTER LES PARTICIPANTS ============
    /**
     * Test de la méthode getParticipantsByEvent()
     * Vérifie qu'on peut lister les participants d'un événement
     */
    @Test
    @Order(6)
    void testListerParticipants() {
        System.out.println("TEST 6 : Lister les participants");

        List<User> participants = eventService.getParticipantsByEvent(testEventId);

        assertNotNull(participants, "La liste ne devrait pas être null");
        assertFalse(participants.isEmpty(), "La liste ne devrait pas être vide");

        // Vérifier que notre utilisateur est dans la liste
        boolean trouve = false;
        for (User user : participants) {
            if (user.getId() == testUserId) {
                trouve = true;
                break;
            }
        }
        assertTrue(trouve, "L'utilisateur devrait être dans la liste");

        System.out.println("✅ Liste participants récupérée - Nombre : " + participants.size());
    }

    // ============ TEST 7 : COMPTER LES PARTICIPANTS ============
    /**
     * Test de la méthode countParticipantsByEvent()
     * Vérifie le nombre de participants
     */
    @Test
    @Order(7)
    void testCompterParticipants() {
        System.out.println("TEST 7 : Compter les participants");

        int nombre = eventService.countParticipantsByEvent(testEventId);

        assertTrue(nombre >= 1, "Le nombre devrait être au moins 1");

        System.out.println("✅ Nombre de participants : " + nombre);
    }

    // ============ TEST 8 : VÉRIFIER SI PARTICIPANT ============
    /**
     * Test de la méthode isParticipant()
     * Vérifie si un utilisateur est participant
     */
    @Test
    @Order(8)
    void testVerifierParticipant() {
        System.out.println("TEST 8 : Vérifier si un utilisateur est participant");

        // Vérifier avec un participant existant
        boolean estParticipant = eventService.isParticipant(testEventId, testUserId);
        assertTrue(estParticipant, "L'utilisateur devrait être participant");

        // Vérifier avec un ID inexistant
        boolean fauxParticipant = eventService.isParticipant(testEventId, 99999);
        assertFalse(fauxParticipant, "Un ID inexistant ne devrait pas être participant");

        System.out.println("✅ Vérification réussie");
    }

    // ============ TEST 9 : STATUT DE L'ÉVÉNEMENT ============
    /**
     * Test de la méthode getEventStatut()
     * Vérifie le statut d'un événement (À venir, En cours, Passé)
     */
    @Test
    @Order(9)
    void testStatutEvenement() {
        System.out.println("TEST 9 : Obtenir le statut de l'événement");

        Event event = eventService.getEventById(testEventId);
        assertNotNull(event);

        String statut = eventService.getEventStatut(event);

        assertNotNull(statut, "Le statut ne devrait pas être null");
        assertFalse(statut.isEmpty(), "Le statut ne devrait pas être vide");

        System.out.println("✅ Statut de l'événement : " + statut);
    }

    // ============ TEST 10 : PLACES RESTANTES ============
    /**
     * Test de la méthode getPlacesRestantes()
     * Vérifie le calcul des places restantes
     */
    @Test
    @Order(10)
    void testPlacesRestantes() {
        System.out.println("TEST 10 : Calculer les places restantes");

        Event event = eventService.getEventById(testEventId);
        assertNotNull(event);

        int placesRestantes = eventService.getPlacesRestantes(event);

        // Vérifier que le calcul est cohérent
        if (event.getCapacite_max() != null) {
            assertEquals(
                    event.getCapacite_max() - event.getParticipantsCount(),
                    placesRestantes,
                    "Le calcul des places restantes est incorrect"
            );
            assertTrue(placesRestantes >= 0, "Les places restantes ne peuvent pas être négatives");
        }

        System.out.println("✅ Places restantes : " + placesRestantes);
    }

    // ============ TEST 11 : DÉSINSCRIRE UN PARTICIPANT ============
    /**
     * Test de la méthode desinscrireParticipant()
     * Vérifie qu'on peut désinscrire un participant
     */
    @Test
    @Order(11)
    void testDesinscrireParticipant() {
        System.out.println("TEST 11 : Désinscrire un participant");

        // Désinscrire
        boolean resultat = eventService.desinscrireParticipant(testEventId, testUserId);
        assertTrue(resultat, "La désinscription devrait réussir");

        // Vérifier qu'il n'est plus inscrit
        boolean estEncoreInscrit = eventService.isParticipant(testEventId, testUserId);
        assertFalse(estEncoreInscrit, "L'utilisateur ne devrait plus être inscrit");

        System.out.println("✅ Désinscription réussie");
    }

    // ============ TEST 12 : SUPPRIMER UN ÉVÉNEMENT ============
    /**
     * Test de la méthode deleteEvent()
     * Vérifie qu'on peut supprimer un événement
     */
    @Test
    @Order(12)
    void testSupprimerEvenement() {
        System.out.println("TEST 12 : Supprimer un événement");

        // Vérifier que l'événement existe avant suppression
        Event avantSuppression = eventService.getEventById(testEventId);
        assertNotNull(avantSuppression, "L'événement devrait exister avant suppression");

        // Supprimer
        boolean resultat = eventService.deleteEvent(testEventId);
        assertTrue(resultat, "La suppression devrait réussir");

        // Vérifier qu'il n'existe plus
        Event apresSuppression = eventService.getEventById(testEventId);
        assertNull(apresSuppression, "L'événement ne devrait plus exister");

        System.out.println("✅ Suppression réussie");
    }

    // ============ TEST 13 : CAS D'ERREUR - ID INEXISTANT ============
    /**
     * Test des cas d'erreur
     * Vérifie le comportement avec un ID inexistant
     */
    @Test
    @Order(13)
    void testEvenementInexistant() {
        System.out.println("TEST 13 : Tester avec un ID inexistant");

        Event event = eventService.getEventById(-999);
        assertNull(event, "Un événement inexistant devrait retourner null");

        boolean suppression = eventService.deleteEvent(-999);
        assertFalse(suppression, "Supprimer un événement inexistant devrait échouer");

        System.out.println("✅ Gestion d'erreur correcte");
    }
}