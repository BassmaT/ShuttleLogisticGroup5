package com.group5.shuttle.service;

import com.group5.shuttle.model.InventoryItem;

import java.util.List;

// Abstraction for the inventory service (D – Dependency Inversion).
public interface IInventoryService {
    List<InventoryItem> loadInventory();
    void saveInventory(List<InventoryItem> items);
}
