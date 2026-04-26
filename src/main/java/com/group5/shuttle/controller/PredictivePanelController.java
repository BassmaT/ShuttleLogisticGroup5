package com.group5.shuttle.controller;

import com.group5.shuttle.model.TrendResult;
import com.group5.shuttle.service.IPredictiveAnalysisService;
import com.group5.shuttle.service.TakeoverState;
import com.group5.shuttle.util.StatusColors;
import com.group5.shuttle.util.Styles;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

// Einzelverantwortung: Aufbau und Aktualisierung der Predictive-AI-Warnungen im Dashboard.
public class PredictivePanelController {

    private final VBox                       container;
    private final IPredictiveAnalysisService predictiveService;

    public PredictivePanelController(VBox container,
                                      IPredictiveAnalysisService predictiveService) {
        this.container         = container;
        this.predictiveService = predictiveService;
    }

    public void update() {
        if (container == null) return;
        container.getChildren().clear();

        Map<String, List<TrendResult>> trends = predictiveService.analyzeAll();
        boolean foundWarning = false;

        for (var partEntry : trends.entrySet()) {
            for (TrendResult trend : partEntry.getValue()) {
                if ("STABLE".equals(trend.getDirection())) continue;
                if (trend.getFlightsUntilLimit() < 0 || trend.getFlightsUntilLimit() > 5) continue;

                foundWarning = true;
                String color = StatusColors.forTrendUrgency(trend.getFlightsUntilLimit());
                String part  = TakeoverState.getDisplayName(trend.getPartKey());
                Label  lbl   = new Label("  [" + part + "] " + trend.getRecommendation());
                lbl.setStyle(Styles.label12(color));
                lbl.setWrapText(true);
                container.getChildren().add(lbl);
            }
        }

        if (!foundWarning) {
            Label ok = new Label("  No critical trends detected");
            ok.setStyle(Styles.label12("#66ff66"));
            container.getChildren().add(ok);
        }
    }
}
