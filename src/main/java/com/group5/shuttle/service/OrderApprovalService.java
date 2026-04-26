package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.LogisticsOrder;

import java.util.List;

// Einzel-Verantwortung: genehmigt oder lehnt Bestellungen ab und löst die Liefersimulation aus.
// Extrahiert aus LogisticsController (war dort Geschäftslogik in einem UI-Event-Handler).
public final class OrderApprovalService {

    private OrderApprovalService() {}

    // Genehmigt die Bestellung, markiert sie als bestellt und startet die 10-Sekunden-Liefersimulation.
    // onDelivered wird nach Eingang aufgerufen – UI-Refresh und Dialog obliegen dem Controller.
    public static void approve(LogisticsOrder order, IInventoryService inventoryService,
                                IEmployeeService employeeService, Runnable onDelivered) {
        List<Employee> chiefs = employeeService.getByRole("Security Chief");
        Employee approver = chiefs.isEmpty() ? null : chiefs.get(0);
        if (approver == null) return;

        LogisticsOrderService.approve(order, approver);
        LogisticsOrderService.markOrdered(order);

        OrderDeliveryService.scheduleDelivery(() -> {
            LogisticsOrderService.markDelivered(order);

            List<com.group5.shuttle.model.InventoryItem> inventory = inventoryService.loadInventory();
            inventory.stream()
                .filter(i -> i.getName().equals(order.getPartName()))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(item.getQuantity() + order.getQuantity());
                    inventoryService.saveInventory(inventory);
                });

            onDelivered.run();
        });
    }

    // Lehnt die Bestellung ab.
    public static void reject(LogisticsOrder order, IEmployeeService employeeService) {
        List<Employee> chiefs = employeeService.getByRole("Security Chief");
        Employee approver = chiefs.isEmpty() ? null : chiefs.get(0);
        if (approver == null) return;
        LogisticsOrderService.reject(order, approver);
    }
}
