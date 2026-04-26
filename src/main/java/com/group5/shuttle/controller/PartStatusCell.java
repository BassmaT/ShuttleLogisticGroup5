package com.group5.shuttle.controller;

import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.StockStatus;
import com.group5.shuttle.util.Styles;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

import java.util.function.Consumer;

// Rendert den Teile-Verfügbarkeitsstatus in einer Tabellenzelle.
// OCP: Neuer StockStatus-Wert → nur das Enum erweitern, diese Klasse bleibt unverändert.
class PartStatusCell extends TableCell<RepairTask, String> {

    private final Button btnOrder = new Button("Order Part");
    private final Label  lblPart  = new Label();

    PartStatusCell(Consumer<RepairTask> onOrder) {
        btnOrder.setStyle(
            "-fx-font-size: 11px; -fx-background-color: #ff8800; " +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8;");
        btnOrder.setOnAction(e -> {
            RepairTask task = getTableRow() != null ? getTableRow().getItem() : null;
            if (task != null) onOrder.accept(task);
        });
    }

    @Override
    protected void updateItem(String status, boolean empty) {
        super.updateItem(status, empty);
        if (empty || status == null) { setGraphic(null); return; }
        StockStatus s = StockStatus.valueOf(status);
        if (s.showsButton()) {
            setGraphic(btnOrder);
        } else {
            lblPart.setText(s.getLabel());
            lblPart.setStyle(Styles.label12(s.getColor()));
            setGraphic(lblPart);
        }
    }
}
