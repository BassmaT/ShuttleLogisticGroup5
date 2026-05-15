package com.group5.shuttle.controller;

import com.group5.shuttle.model.SensorStatus;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;
import com.group5.shuttle.model.ShuttlePart;
import com.group5.shuttle.service.IPartApproval;
import com.group5.shuttle.service.SensorEvaluator;
import com.group5.shuttle.service.TakeoverState;
import com.group5.shuttle.util.ShuttleDataHelper;
import com.group5.shuttle.util.StatusColors;
import com.group5.shuttle.util.Styles;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Map;

// Single responsibility: building and updating the sensor display in the dashboard.
// Decouples the sensor rendering logic from the MainController.
public class SensorPanelController {

    private final VBox            container;
    private final SensorEvaluator sensorService;
    private final IPartApproval   partApproval;

    public SensorPanelController(VBox container,
                                  SensorEvaluator sensorService,
                                  IPartApproval partApproval) {
        this.container     = container;
        this.sensorService = sensorService;
        this.partApproval  = partApproval;
    }

    public void update(ShuttleData data,
                       Map<String, Map<String, SensorThreshold>> thresholds) {
        container.getChildren().clear();

        for (var partEntry : data.getAllParts().entrySet()) {
            String      partName = partEntry.getKey();
            ShuttlePart part     = partEntry.getValue();
            if (part == null || part.getSensors() == null) continue;

            boolean approved   = partApproval.isPartApproved(partName);
            String  headerText = TakeoverState.getDisplayName(partName).toUpperCase()
                               + (approved ? "  [APPROVED]" : "");
            Label partHeader = new Label(headerText);
            partHeader.setStyle("-fx-text-fill: " + (approved ? "#66ff66" : "#aaaaaa")
                + "; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 0 2 0;");
            container.getChildren().add(partHeader);

            Map<String, SensorThreshold> pt = thresholds != null ? thresholds.get(partName) : null;
            for (var sensorEntry : part.getSensors().entrySet()) {
                String sensorName = sensorEntry.getKey();
                double value      = sensorEntry.getValue();
                SensorStatus result = ShuttleDataHelper.evaluateSensor(sensorName, value, pt, sensorService);
                Label lbl = new Label(String.format("  %s: %.2f  [%s]", sensorName, value, result));
                lbl.setStyle(Styles.label13(StatusColors.forSensorStatus(result)));
                container.getChildren().add(lbl);
            }
        }
    }
}
