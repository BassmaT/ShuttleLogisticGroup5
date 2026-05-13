package com.group5.shuttle.controller;

import java.util.List;
import java.util.Map;

// OCP: neue Rolle → einen Eintrag hier ergänzen.
// RoleAccessController und MainController bleiben unverändert.
final class RoleConfig {

    private RoleConfig() {}

    static final Map<String, List<String>> RESTRICTED_BUTTONS = Map.of(
        "Technician", List.of("btnInventory",   "btnHistory",   "btnStaff",  "btnLogistics", "btnSchedule"),
        "Planner",    List.of("btnMission",    "btnTechnician", "btnInventory", "btnHistory",  "btnLogistics"),
        "Logistics",  List.of("btnMission",    "btnTechnician", "btnHistory",   "btnStaff",    "btnSchedule")
    );
}
