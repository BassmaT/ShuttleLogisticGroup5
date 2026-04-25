package com.group5.shuttle.service;

import java.util.ArrayList;
import java.util.List;

// Basisklasse für alle In-Memory-Stores dieser Sitzung.
// Kapselt die gemeinsame Listenverwaltung (holen, alle löschen).
// Konkrete Stores erben und ergänzen domänenspezifische Methoden.
abstract class AbstractStore<T> {

    protected final List<T> items = new ArrayList<>();

    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    public void clear() {
        items.clear();
    }
}
