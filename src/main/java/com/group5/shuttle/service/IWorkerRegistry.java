package com.group5.shuttle.service;

// Focused interface for employee registration on a shuttle part (I – Interface Segregation).
// Only controllers that register or query employees depend on this interface.
public interface IWorkerRegistry {
    void registerWorker(String partKey, String name, String role);
    String getWorkerInfo(String partKey);
    String getTechnicianName(String partKey);
    String getSecurityChiefName(String partKey);
}
