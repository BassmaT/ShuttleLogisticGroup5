package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.UserRole;

// Speichert den aktuell eingeloggten Mitarbeiter für die gesamte Sitzung.
// Singleton – wird nach erfolgreichem Login einmalig gesetzt.
public class SessionState {

    private static final class Holder {
        static final SessionState INSTANCE = new SessionState();
    }

    private Employee currentUser;
    private UserRole currentUserRole;

    private SessionState() {}

    public static SessionState getInstance() {
        return Holder.INSTANCE;
    }

    public void setCurrentUser(Employee employee) {
        this.currentUser = employee;
    }

    public void setUserRole(UserRole role) {
        this.currentUserRole = role;
    }

    public UserRole getUserRole() {
        return currentUserRole;
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
