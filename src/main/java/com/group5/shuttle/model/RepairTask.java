package com.group5.shuttle.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import com.group5.shuttle.model.SensorStatus;
import com.group5.shuttle.model.StockStatus;

// Eine Reparaturaufgabe für einen defekten oder verschlissenen Sensor.
// Wird automatisch erstellt, wenn ein Sensor den Grenzwert über- oder unterschreitet.
// JavaFX-Properties werden verwendet, damit die Tabelle Änderungen live anzeigt.
public class RepairTask {

    // Name des betroffenen Sensors, z. B. "coolantPressure".
    private final SimpleStringProperty sensorName;

    // Fehlerstatus des Sensors: "WARNING" (Warnung) oder "REPLACE" (sofort tauschen).
    private final SimpleStringProperty status;

    // Beschreibung der Maßnahme, z. B. "Replace component" oder "Inspect and adjust".
    private final SimpleStringProperty action;

    // Gibt an, ob der Techniker diese Aufgabe als erledigt markiert hat.
    private final SimpleBooleanProperty done;

    // Der Schlüssel des Shuttle-Teils, zu dem diese Aufgabe gehört, z. B. "orbiter".
    private final String partKey;

    // Name des Lagerartikels, der für diese Reparatur benötigt wird.
    // Kann null sein, wenn kein spezielles Teil nötig ist.
    private String requiredItemName;

    // Aktueller Zustand des benötigten Lagerartikels:
    // "NONE"         → kein Teil nötig
    // "IN_STOCK"     → Teil ist verfügbar
    // "OUT_OF_STOCK" → Teil ist nicht auf Lager, muss bestellt werden
    // "ORDERED"      → Teil wurde bestellt, kommt in Kürze
    // "ARRIVED"      → Teil ist eingetroffen, Reparatur kann beginnen
    private final SimpleStringProperty partStatus = new SimpleStringProperty(StockStatus.NONE.name());

    // Gibt an, ob der Techniker die "Erledigt"-Checkbox anklicken darf.
    // Erst true, wenn das Teil verfügbar oder eingetroffen ist.
    private final SimpleBooleanProperty partAvailable = new SimpleBooleanProperty(true);

    // Konstruktor – wird aufgerufen, wenn eine neue Reparaturaufgabe erstellt wird.
    public RepairTask(String partKey, String sensorName, SensorStatus status, String action) {
        this.partKey    = partKey;
        this.sensorName = new SimpleStringProperty(sensorName);
        this.status     = new SimpleStringProperty(status.name());
        this.action     = new SimpleStringProperty(action);
        this.done       = new SimpleBooleanProperty(false);
    }

    // Gibt den Schlüssel des Shuttle-Teils zurück, z. B. "orbiter".
    public String getPartKey() { return partKey; }

    // Sensorname – Getter und Property-Zugriff.
    public String getSensorName()                    { return sensorName.get(); }
    public SimpleStringProperty sensorNameProperty() { return sensorName; }

    // Fehlerstatus – Getter und Property-Zugriff.
    public String getStatus()                    { return status.get(); }
    public SimpleStringProperty statusProperty() { return status; }

    // Maßnahme – Getter und Property-Zugriff.
    public String getAction()                    { return action.get(); }
    public SimpleStringProperty actionProperty() { return action; }

    // Erledigt-Flag – Getter, Setter und Property-Zugriff.
    public boolean isDone()                       { return done.get(); }
    public void setDone(boolean value)            { done.set(value); }
    public SimpleBooleanProperty doneProperty()   { return done; }

    // Name des benötigten Lagerartikels – kann null sein.
    public String getRequiredItemName()                        { return requiredItemName; }
    public void   setRequiredItemName(String requiredItemName) { this.requiredItemName = requiredItemName; }

    // Lagerstatus des benötigten Teils – Getter, Setter und Property-Zugriff.
    public String getPartStatus()                         { return partStatus.get(); }
    public void   setPartStatus(StockStatus s)            { partStatus.set(s.name()); }
    public SimpleStringProperty partStatusProperty()      { return partStatus; }

    // Gibt an, ob die Erledigt-Checkbox aktiviert ist – Getter, Setter und Property-Zugriff.
    public boolean isPartAvailable()                     { return partAvailable.get(); }
    public void    setPartAvailable(boolean b)           { partAvailable.set(b); }
    public SimpleBooleanProperty partAvailableProperty() { return partAvailable; }
}
