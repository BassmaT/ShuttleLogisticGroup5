package com.group5.shuttle.service;

import java.util.ArrayList;
import java.util.List;

// Base class for all in-memory stores in this session.
// Encapsulates common list management (retrieve, clear all).
// Concrete stores inherit and add domain-specific methods.
abstract class AbstractStore<T> {

    protected final List<T> items = new ArrayList<>();

    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    public void clear() {
        items.clear();
    }
}
