package edu.Loopi.interfaces;

import edu.Loopi.entities.Event;
import edu.Loopi.entities.User;
import java.util.List;

public interface IEvenementService {
    // ============ CRUD OPERATIONS ============
    boolean addEvent(Event event);
    boolean updateEvent(Event event);
    boolean deleteEvent(int idEvent);
    Event getEventById(int idEvent);
    List<Event> getAllEvents();

    // ============ GESTION PAR ORGANISATEUR ============

    List<Event> getEventsByOrganisateur(int organisateurId);
    int countEventsByOrganisateur(int organisateurId);
    // ============ GESTION DES PARTICIPATIONS ============
    boolean inscrireParticipant(int idEvent, int idUser, String contact, Integer age);
    boolean desinscrireParticipant(int idEvent, int idUser);
    boolean updateStatutParticipant(int idEvent, int idUser, String statut);
    List<User> getParticipantsByEvent(int idEvent);
    boolean isParticipant(int idEvent, int idUser);
    boolean isEventComplet(int idEvent);
    // ============ STATISTIQUES ============
    int countParticipantsByEvent(int idEvent);
    void loadParticipationStats(Event event);
    double getTauxRemplissage(Event event);
    // ============ RECHERCHE ET FILTRES ============
    List<Event> searchEvents(String keyword);
    List<Event> getUpcomingEvents();
    List<Event> getPastEvents();
    List<Event> getOngoingEvents();
    // ============ MÉTHODES UTILITAIRES ============
    String getEventStatut(Event event);
    int getPlacesRestantes(Event event);
}