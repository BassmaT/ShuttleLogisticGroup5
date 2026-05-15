package com.group5.shuttle.controller;

import java.util.List;
import java.util.Map;

// OCP: new role → add one entry here.
// RoleAccessController and MainController remain unchanged.
final class RoleConfig {

    private RoleConfig() {}

    static final Map<String, List<String>> RESTRICTED_BUTTONS = Map.of(
        "Technician", List.of("btnInventory",   "btnHistory",   "btnStaff",  "btnLogistics", "btnSchedule"),
        "Planner",    List.of("btnMission",    "btnTechnician", "btnInventory", "btnHistory",  "btnLogistics"),
        "Logistics",  List.of("btnMission",    "btnTechnician", "btnHistory",   "btnStaff",    "btnSchedule")
    );
}
