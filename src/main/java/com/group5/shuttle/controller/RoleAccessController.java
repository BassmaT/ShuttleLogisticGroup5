package com.group5.shuttle.controller;

import com.group5.shuttle.service.SessionState;
import javafx.scene.control.Button;

import java.util.List;
import java.util.Map;
import java.util.Objects;

// Single responsibility: enables or disables navigation buttons
// based on the role of the logged-in employee.
// OCP: role configuration comes from RoleConfig – this class remains unchanged.
final class RoleAccessController {

    private final Map<String, List<String>> restrictionIds;
    private final Map<String, Button>       buttonLookup;

    RoleAccessController(Map<String, List<String>> restrictionIds,
                         Map<String, Button> buttonLookup) {
        this.restrictionIds = restrictionIds;
        this.buttonLookup   = buttonLookup;
    }

    void applyRestrictions(List<Button> allNavButtons) {
        allNavButtons.forEach(b -> b.setDisable(false));
        String role = SessionState.getInstance().getCurrentRole();
        restrictionIds.getOrDefault(role, List.of()).stream()
            .map(buttonLookup::get)
            .filter(Objects::nonNull)
            .forEach(b -> b.setDisable(true));
    }
}
