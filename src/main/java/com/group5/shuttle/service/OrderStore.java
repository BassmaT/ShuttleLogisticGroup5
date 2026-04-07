package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.LogisticsOrder;
import java.util.ArrayList;
import java.util.List;

// Singleton: hält alle Logistik-Bestellungen der aktuellen Sitzung im Arbeitsspeicher.
// Identisches Muster wie TicketStore – Daten werden beim Schließen der App gelöscht.
public class OrderStore {

    // Die einzige Instanz dieser Klasse
    private static OrderStore instance;

    // In-Memory-Liste aller Bestellungen dieser Sitzung
    private final List<LogisticsOrder> orders = new ArrayList<>();

    // Zähler für aufsteigende Bestellnummern (ORD-001, ORD-002 …)
    private int nextOrderNumber = 1;

    // Privater Konstruktor – keine Initialisierung nötig
    private OrderStore() {}

    // Gibt die einzige Instanz zurück – erstellt sie beim ersten Aufruf
    public static OrderStore getInstance() {
        if (instance == null) instance = new OrderStore();
        return instance;
    }

    // Gibt eine Kopie aller Bestellungen zurück (defensive copy)
    public List<LogisticsOrder> getOrders() {
        return new ArrayList<>(orders);
    }

    // Legt eine neue Bestellung an, fügt sie zur Liste hinzu und gibt sie zurück
    public LogisticsOrder createOrder(String partName, int quantity,
                                      Employee orderedBy, String reason) {
        // Bestellnummer hochzählen und formatieren, z. B. "ORD-003"
        String num = String.format("ORD-%03d", nextOrderNumber++);
        LogisticsOrder order = new LogisticsOrder(num, partName, quantity, orderedBy, reason);
        orders.add(order);
        return order;
    }

    // Löscht alle Bestellungen und setzt den Nummernzähler zurück
    // Wird beim Takeover-Reset aufgerufen
    public void clear() {
        orders.clear();
        nextOrderNumber = 1;
    }
}
