package com.group5.shuttle.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

// A repair task for a faulty or worn sensor.
// Created automatically when a sensor exceeds or falls below a threshold.
// JavaFX properties are used so that the table reflects changes live.
public class RepairTask {

    // Name of the affected sensor, e.g. "coolantPressure".
    private final SimpleStringProperty sensorName;

    // Fault status of the sensor: "WARNING" (warning) or "REPLACE" (replace immediately).
    private final SimpleStringProperty status;

    // Description of the action, e.g. "Replace component" or "Inspect and adjust".
    private final SimpleStringProperty action;

    // Indicates whether the technician has marked this task as done.
    private final SimpleBooleanProperty done;

    // The key of the shuttle part this task belongs to, e.g. "orbiter".
    private final String partKey;

    // Name of the inventory item required for this repair.
    // Can be null if no specific part is needed.
    private String requiredItemName;

    // Current state of the required inventory item:
    // "NONE"         → no part needed
    // "IN_STOCK"     → part is available
    // "OUT_OF_STOCK" → part is not in stock, must be ordered
    // "ORDERED"      → part has been ordered, arriving soon
    // "ARRIVED"      → part has arrived, repair can begin
    private final SimpleStringProperty partStatus = new SimpleStringProperty(StockStatus.NONE.name());

    // Indicates whether the technician is allowed to tick the "Done" checkbox.
    // True only when the part is available or has arrived.
    private final SimpleBooleanProperty partAvailable = new SimpleBooleanProperty(true);

    // Constructor – called when a new repair task is created.
    public RepairTask(String partKey, String sensorName, SensorStatus status, String action) {
        this.partKey    = partKey;
        this.sensorName = new SimpleStringProperty(sensorName);
        this.status     = new SimpleStringProperty(status.name());
        this.action     = new SimpleStringProperty(action);
        this.done       = new SimpleBooleanProperty(false);
    }

    // Returns the key of the shuttle part, e.g. "orbiter".
    public String getPartKey() { return partKey; }

    // Sensor name – getter and property accessor.
    public String getSensorName()                    { return sensorName.get(); }
    public SimpleStringProperty sensorNameProperty() { return sensorName; }

    // Fault status – getter and property accessor.
    public String getStatus()                    { return status.get(); }
    public SimpleStringProperty statusProperty() { return status; }

    // Action – getter and property accessor.
    public String getAction()                    { return action.get(); }
    public SimpleStringProperty actionProperty() { return action; }

    // Done flag – getter, setter and property accessor.
    public boolean isDone()                       { return done.get(); }
    public void setDone(boolean value)            { done.set(value); }
    public SimpleBooleanProperty doneProperty()   { return done; }

    // Name of the required inventory item – can be null.
    public String getRequiredItemName()                        { return requiredItemName; }
    public void   setRequiredItemName(String requiredItemName) { this.requiredItemName = requiredItemName; }

    // Stock status of the required part – getter, setter and property accessor.
    public String getPartStatus()                         { return partStatus.get(); }
    public void   setPartStatus(StockStatus s)            { partStatus.set(s.name()); }
    public SimpleStringProperty partStatusProperty()      { return partStatus; }

    // Indicates whether the Done checkbox is enabled – getter, setter and property accessor.
    public boolean isPartAvailable()                     { return partAvailable.get(); }
    public void    setPartAvailable(boolean b)           { partAvailable.set(b); }
    public SimpleBooleanProperty partAvailableProperty() { return partAvailable; }
}
