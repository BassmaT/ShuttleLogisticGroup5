package com.group5.shuttle.service;

import com.group5.shuttle.model.MaintenanceTicket;
import com.group5.shuttle.model.RepairTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Single responsibility: writes completed repairs as maintenance tickets to the history.
// Extracted from MissionControlController (business logic was previously in a UI controller).
public final class MaintenanceHistoryService {

    private static final DateTimeFormatter TIMESTAMP_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private MaintenanceHistoryService() {}

    public static void logRepairs(String partKey,
                                   IRepairAccess repairAccess,
                                   IWorkerRegistry workerRegistry) {
        String techName = workerRegistry.getTechnicianName(partKey);
        if (techName == null) techName = "Unknown";

        String displayName = TakeoverState.getDisplayName(partKey);
        String timestamp   = LocalDateTime.now().format(TIMESTAMP_FMT);

        List<MaintenanceTicket> tickets = TicketStore.getInstance().getTickets();
        int nextId = tickets.size() + 1;

        for (RepairTask task : repairAccess.getRepairs(partKey)) {
            if (!task.isDone()) continue;
            String action = task.getAction()
                + (task.getRequiredItemName() != null
                   ? " – used: " + task.getRequiredItemName() : "");
            tickets.add(new MaintenanceTicket(
                String.format("TKT-%03d", nextId++),
                timestamp, displayName,
                task.getSensorName(), task.getStatus(),
                action, techName));
        }
        TicketStore.getInstance().saveTickets(tickets);
    }
}
