package com.group5.shuttle.service;

import com.group5.shuttle.model.MaintenanceTicket;

import java.util.ArrayList;
import java.util.List;

// Diese Klasse speichert alle Wartungstickets (History-Einträge) im Arbeitsspeicher.
// Es wird KEIN File geschrieben – beim Schließen der App sind alle Einträge weg.
// Das Singleton-Muster stellt sicher, dass es nur eine einzige Instanz gibt,
// auf die alle Controller zugreifen.
public class TicketStore {

    // Die eine einzige Instanz dieser Klasse – beim ersten Aufruf erstellt, danach wiederverwendet.
    private static TicketStore instance;

    // Die Liste aller Tickets, die in dieser Sitzung erstellt wurden.
    private final List<MaintenanceTicket> tickets = new ArrayList<>();

    // Privater Konstruktor – verhindert, dass jemand "new TicketStore()" schreibt.
    private TicketStore() {}

    // Gibt die einzige Instanz zurück. Falls noch keine existiert, wird sie hier erstellt.
    public static TicketStore getInstance() {
        if (instance == null) instance = new TicketStore();
        return instance;
    }

    // Gibt eine Kopie der aktuellen Ticket-Liste zurück.
    // Eine Kopie (new ArrayList) wird verwendet, damit die interne Liste nicht von außen verändert werden kann.
    public List<MaintenanceTicket> getTickets() {
        return new ArrayList<>(tickets);
    }

    // Ersetzt die gesamte Ticket-Liste durch eine neue Liste.
    // Wird aufgerufen, wenn nach einer Reparatur alle Tickets neu gespeichert werden.
    public void saveTickets(List<MaintenanceTicket> list) {
        tickets.clear();        // alte Liste leeren
        tickets.addAll(list);   // neue Einträge hinzufügen
    }

    // Löscht alle Tickets – kann z. B. nach einem Reset aufgerufen werden.
    public void clear() {
        tickets.clear();
    }
}
