package com.group5.shuttle.controller;

import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.SensorStatus;
import com.group5.shuttle.util.StatusColors;
import javafx.scene.control.TableCell;

class SensorStatusCell extends TableCell<RepairTask, String> {

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) { setText(null); setStyle(""); return; }
        setText(item);
        setStyle("-fx-text-fill: " + StatusColors.forSensorStatus(SensorStatus.valueOf(item))
                + "; -fx-font-weight: bold;");
    }
}
