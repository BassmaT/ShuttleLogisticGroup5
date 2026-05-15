package com.group5.shuttle.service;

import com.group5.shuttle.model.MaintenanceTicket;
import java.util.List;

// Singleton: holds all maintenance tickets for the current session in memory.
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

    // Replaces the entire ticket list with a new list.
    public void saveTickets(List<MaintenanceTicket> list) {
        items.clear();
        items.addAll(list);
    }
}
