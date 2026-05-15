package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.LogisticsOrder;

import java.util.List;

// Single responsibility: approves or rejects orders and triggers the delivery simulation.
// Extracted from LogisticsController (business logic was previously in a UI event handler).
public final class OrderApprovalService {

    private OrderApprovalService() {}

    // Approves the order, marks it as ordered, and starts the 10-second delivery simulation.
    // onDelivered is called after receipt – UI refresh and dialog are the controller's responsibility.
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

    // Rejects the order.
    public static void reject(LogisticsOrder order, IEmployeeService employeeService) {
        List<Employee> chiefs = employeeService.getByRole("Security Chief");
        Employee approver = chiefs.isEmpty() ? null : chiefs.get(0);
        if (approver == null) return;
        LogisticsOrderService.reject(order, approver);
    }
}
