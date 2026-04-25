package com.group5.shuttle.service;

import com.group5.shuttle.model.MaintenanceTicket;
import java.util.List;

// Singleton: hält alle Wartungstickets der aktuellen Sitzung im Arbeitsspeicher.
public class TicketStore extends AbstractStore<MaintenanceTicket> {

    private static final class Holder {
        static final TicketStore INSTANCE = new TicketStore();
    }

    private TicketStore() {}

    public static TicketStore getInstance() {
        return Holder.INSTANCE;
    }

    public List<MaintenanceTicket> getTickets() {
        return getAll();
    }

    // Ersetzt die gesamte Ticket-Liste durch eine neue Liste.
    public void saveTickets(List<MaintenanceTicket> list) {
        items.clear();
        items.addAll(list);
    }
}
