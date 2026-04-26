package com.group5.shuttle.service;

import com.group5.shuttle.model.InventoryItem;

import java.util.List;

// Abstraktion für den Lagerbestand-Service (D – Dependency Inversion).
public interface IInventoryService {
    List<InventoryItem> loadInventory();
    void saveInventory(List<InventoryItem> items);
}
