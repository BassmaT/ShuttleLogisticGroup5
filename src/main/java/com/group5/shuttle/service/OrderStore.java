package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.LogisticsOrder;
import java.util.List;

// Singleton: holds all logistics orders for the current session in memory.
public class OrderStore extends AbstractStore<LogisticsOrder> {

    private static final class Holder {
        static final OrderStore INSTANCE = new OrderStore();
    }

    // Counter for incrementing order numbers (ORD-001, ORD-002 …)
    private int nextOrderNumber = 1;

    private OrderStore() {}

    public static OrderStore getInstance() {
        return Holder.INSTANCE;
    }

    public List<LogisticsOrder> getOrders() {
        return getAll();
    }

    // Creates a new order and returns it
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
