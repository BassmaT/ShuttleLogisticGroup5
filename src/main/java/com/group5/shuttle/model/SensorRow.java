package com.group5.shuttle.model;

import javafx.beans.property.SimpleStringProperty;

// Eine Zeile in der Sensor-Tabelle des Techniker-Panels.
// Jede Zeile zeigt: welcher Teil, welcher Sensor, welcher Messwert, welcher Status.
// SimpleStringProperty ist ein JavaFX-Typ, der notwendig ist, damit die Tabelle
// Änderungen automatisch anzeigt (sogenannte "Observable Properties").
public class SensorRow {

    // JavaFX-Properties für jeden Spaltenwert – die Tabelle bindet sich daran.
    private final SimpleStringProperty part;    // z. B. "Orbiter"
    private final SimpleStringProperty sensor;  // z. B. "hullTemperature"
    private final SimpleStringProperty value;   // z. B. "520.00"
    private final SimpleStringProperty status;  // z. B. "OK", "WARNING", "REPLACE"

    // Konstruktor – wird aufgerufen, wenn eine neue Tabellenzeile erstellt wird.
    public SensorRow(String part, String sensor, String value, String status) {
        this.part   = new SimpleStringProperty(part);
        this.sensor = new SimpleStringProperty(sensor);
        this.value  = new SimpleStringProperty(value);
        this.status = new SimpleStringProperty(status);
    }

    // Getter-Methoden – geben den reinen String-Wert zurück.
    // PropertyValueFactory ruft genau diese Methoden per Reflection auf.
    public String getPart()   { return part.get(); }
    public String getSensor() { return sensor.get(); }
    public String getValue()  { return value.get(); }
    public String getStatus() { return status.get(); }

    // Property-Methoden – geben das Observable-Objekt zurück.
    // Werden benötigt, wenn sich die Tabelle automatisch aktualisieren soll.
    public SimpleStringProperty partProperty()   { return part; }
    public SimpleStringProperty sensorProperty() { return sensor; }
    public SimpleStringProperty valueProperty()  { return value; }
    public SimpleStringProperty statusProperty() { return status; }
}
