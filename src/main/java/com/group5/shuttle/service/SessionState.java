package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;

// Speichert den aktuell eingeloggten Mitarbeiter für die gesamte Sitzung.
// Singleton – wird nach erfolgreichem Login einmalig gesetzt.
public class SessionState {

    private static SessionState instance;

    private Employee currentUser;

    private SessionState() {}

    public static SessionState getInstance() {
        if (instance == null) instance = new SessionState();
        return instance;
    }

    public void setCurrentUser(Employee employee) {
        this.currentUser = employee;
    }

    public Employee getCurrentUser() {
        return currentUser;
    }

    // Gibt die Rolle des eingeloggten Mitarbeiters zurück, z. B. "Security Chief"
    public String getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : "";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
