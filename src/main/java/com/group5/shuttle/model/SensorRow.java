package com.group5.shuttle.model;

import javafx.beans.property.SimpleStringProperty;

// A row in the sensor table of the technician panel.
// Each row shows: which part, which sensor, which measurement value, which status.
// SimpleStringProperty is a JavaFX type required so that the table
// automatically reflects changes (so-called "Observable Properties").
public class SensorRow {

    // JavaFX properties for each column value – the table binds to them.
    private final SimpleStringProperty part;    // e.g. "Orbiter"
    private final SimpleStringProperty sensor;  // e.g. "hullTemperature"
    private final SimpleStringProperty value;   // e.g. "520.00"
    private final SimpleStringProperty status;  // e.g. "OK", "WARNING", "REPLACE"

    // Constructor – called when a new table row is created.
    public SensorRow(String part, String sensor, String value, String status) {
        this.part   = new SimpleStringProperty(part);
        this.sensor = new SimpleStringProperty(sensor);
        this.value  = new SimpleStringProperty(value);
        this.status = new SimpleStringProperty(status);
    }

    // Getter methods – return the plain string value.
    // PropertyValueFactory calls exactly these methods via reflection.
    public String getPart()   { return part.get(); }
    public String getSensor() { return sensor.get(); }
    public String getValue()  { return value.get(); }
    public String getStatus() { return status.get(); }

    // Property methods – return the observable object.
    // Required when the table should update automatically.
    public SimpleStringProperty partProperty()   { return part; }
    public SimpleStringProperty sensorProperty() { return sensor; }
    public SimpleStringProperty valueProperty()  { return value; }
    public SimpleStringProperty statusProperty() { return status; }
}
