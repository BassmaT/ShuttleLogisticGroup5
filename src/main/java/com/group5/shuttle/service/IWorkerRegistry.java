package com.group5.shuttle.service;

// Fokussierte Schnittstelle für Mitarbeiter-Registrierung am Shuttle-Teil (I – Interface Segregation).
// Nur Controller, die Mitarbeiter registrieren oder abfragen, hängen von dieser ab.
public interface IWorkerRegistry {
    void registerWorker(String partKey, String name, String role);
    String getWorkerInfo(String partKey);
    String getTechnicianName(String partKey);
    String getSecurityChiefName(String partKey);
}
