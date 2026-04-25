package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.LogisticsOrder;
import java.util.List;

// Singleton: hält alle Logistik-Bestellungen der aktuellen Sitzung im Arbeitsspeicher.
public class OrderStore extends AbstractStore<LogisticsOrder> {

    private static final class Holder {
        static final OrderStore INSTANCE = new OrderStore();
    }

    // Zähler für aufsteigende Bestellnummern (ORD-001, ORD-002 …)
    private int nextOrderNumber = 1;

    private OrderStore() {}

    public static OrderStore getInstance() {
        return Holder.INSTANCE;
    }

    public List<LogisticsOrder> getOrders() {
        return getAll();
    }

    // Legt eine neue Bestellung an und gibt sie zurück
    public LogisticsOrder createOrder(String partName, int quantity,
                                      Employee orderedBy, String reason) {
        String num = String.format("ORD-%03d", nextOrderNumber++);
        LogisticsOrder order = new LogisticsOrder(num, partName, quantity, orderedBy, reason);
        items.add(order);
        return order;
    }

    @Override
    public void clear() {
        super.clear();
        nextOrderNumber = 1;
    }
}
