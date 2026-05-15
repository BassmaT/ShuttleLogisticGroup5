package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.UserRole;

// Stores the currently logged-in employee for the entire session.
// Singleton – set once after a successful login.
public class SessionState {

    private static final class Holder {
        static final SessionState INSTANCE = new SessionState();
    }

    private Employee currentUser;
    private UserRole currentUserRole;

    private SessionState() {}

    private boolean pendingNotification = false;

    public boolean hasPendingNotification() { return pendingNotification; }
    public void setPendingNotification(boolean value) { this.pendingNotification = value; }

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

    // Returns the role of the logged-in employee, e.g. "Security Chief"
    public String getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : "";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
