package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.LogisticsOrder;
import com.group5.shuttle.model.OrderStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Single responsibility: performs status transitions on an order.
// Extracted from LogisticsOrder (business logic was previously in the data model).
public final class LogisticsOrderService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private LogisticsOrderService() {}

    public static void approve(LogisticsOrder order, Employee chief) {
        order.setApprovedById(chief.getId());
        order.setApprovedByName(chief.getName());
        order.setApprovalDate(LocalDateTime.now().format(FMT));
        order.setStatus(OrderStatus.APPROVED);
    }

    public static void reject(LogisticsOrder order, Employee chief) {
        order.setApprovedById(chief.getId());
        order.setApprovedByName(chief.getName());
        order.setApprovalDate(LocalDateTime.now().format(FMT));
        order.setStatus(OrderStatus.REJECTED);
    }

    public static void markOrdered(LogisticsOrder order) {
        order.setStatus(OrderStatus.ORDERED);
    }

    public static void markDelivered(LogisticsOrder order) {
        order.setDeliveryDate(LocalDateTime.now().format(FMT));
        order.setStatus(OrderStatus.DELIVERED);
    }
}
