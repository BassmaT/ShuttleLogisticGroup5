package com.group5.shuttle.util;

import javafx.scene.control.TableCell;

import java.util.function.Function;

/**
 * Wiederverwendbare TableCell-Implementierung mit Farbkodierung (DRY-Prinzip).
 * Ersetzt das identische updateItem()-Boilerplate in allen Controllern.
 *
 * Verwendung:
 *   colStatus.setCellFactory(col -> new ColoredTableCell<>(StatusColors::forSensorStatus));
 */
public class ColoredTableCell<T> extends TableCell<T, String> {

    private final Function<String, String> colorMapper;

    public ColoredTableCell(Function<String, String> colorMapper) {
        this.colorMapper = colorMapper;
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setStyle("");
        } else {
            setText(item);
            setStyle("-fx-text-fill: " + colorMapper.apply(item) + "; -fx-font-weight: bold;");
        }
    }
}
