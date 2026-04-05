# Shuttle Dashboard – Gruppe 5 KI GENERIERTE READ ME FILE

Eine JavaFX-Desktop-Anwendung zur Verwaltung des Pre-Launch-Takeover-Prozesses für Raumfähren. Das System unterstützt Techniker und Security Chiefs dabei, alle Shuttle-Komponenten zu überprüfen, Reparaturen durchzuführen und die finale Freigabe zu erteilen.

---

## Features

- **Haupt-Dashboard** – Überblick über den Takeover-Fortschritt (0–100%), Sensorwarnungen, aktive Arbeiter und letzte Aktivität
- **Mission Control** – Zweistufiger Workflow: Zuerst Shuttle-Teil wählen, dann Name und Rolle eingeben
- **Techniker-Panel** – Reparaturaufgaben in Tabellenform; Checkboxen mit automatischer Ersatzteil-Prüfung
- **Bestell-Simulation** – Fehlende Teile können bestellt werden; nach 10 Sekunden erscheint eine Benachrichtigung „Teil eingetroffen"
- **Inventar** – Lagerbestand der Ersatzteile; wird bei abgeschlossenen Reparaturen automatisch abgebucht
- **History** – Alle durchgeführten Reparaturen mit Datum, Uhrzeit, Techniker und Maßnahme (nur für aktuelle Sitzung, wird beim Schließen gelöscht)
- **Fortschrittsbalken** – Zeigt den Genehmigungsfortschritt; Hover-Tooltip zeigt Prozentzahl
- **Sensorüberwachung** – Farbcodierte Statusanzeige: Grün (OK), Gelb (WARNING), Rot (REPLACE)
- **Zweistufige Freigabe** – Erst wenn der Techniker „Mark All Done" gedrückt hat, kann der Security Chief seinen finalen OK geben
- **Dunkel-Theme** – Durchgängig dunkle Benutzeroberfläche (#1e1e1e)

---

## Technologie-Stack

| Technologie      | Version  | Zweck                                      |
|------------------|----------|--------------------------------------------|
| Java             | 17       | Programmiersprache                         |
| JavaFX           | 21.0.2   | UI-Framework (FXML, Controls, Properties)  |
| Jackson          | 2.17.0   | JSON-Serialisierung / Deserialisierung      |
| Maven            | –        | Build-Tool und Abhängigkeitsverwaltung     |
| javafx-maven-plugin | 0.0.8 | App starten via `mvn javafx:run`           |

---

## Projektstruktur

```
shuttle-dashboard/
├── pom.xml                          ← Maven Build-Konfiguration
└── src/
    └── main/
        ├── java/com/group5/shuttle/
        │   ├── App.java             ← Einstiegspunkt der Anwendung
        │   ├── Main.java            ← JavaFX Launch-Wrapper
        │   ├── controller/          ← UI-Controller (MVC)
        │   │   ├── BaseController.java
        │   │   ├── MainController.java
        │   │   ├── MissionControlController.java
        │   │   ├── TechnicianController.java
        │   │   ├── InventoryController.java
        │   │   ├── HistoryController.java
        │   │   └── RoleSelectionController.java
        │   ├── model/               ← Datenmodelle
        │   │   ├── ShuttleData.java
        │   │   ├── ShuttlePart.java
        │   │   ├── SensorThreshold.java
        │   │   ├── SensorRow.java
        │   │   ├── RepairTask.java
        │   │   ├── InventoryItem.java
        │   │   └── MaintenanceTicket.java
        │   └── service/             ← Business-Logik
        │       ├── SensorService.java
        │       ├── TakeoverState.java
        │       └── TicketStore.java
        └── resources/view/
            ├── main_view.fxml       ← Haupt-Dashboard
            ├── mission_control.fxml ← Mission Control
            ├── technician.fxml      ← Techniker-Panel
            ├── inventory.fxml       ← Lagerübersicht
            ├── history.fxml         ← Wartungshistorie
            ├── role_selection.fxml  ← Rollen-Auswahl
            └── data/
                ├── sensors.json     ← Aktuelle Sensorwerte
                ├── thresholds.json  ← Grenzwerte pro Sensor
                └── inventory.json   ← Lagerbestand (Vorlage)
```

---

## Architektur

### MVC-Pattern
Die App folgt dem Model-View-Controller-Muster:
- **Model** – Datenklassen (z.B. `ShuttlePart`, `RepairTask`, `InventoryItem`)
- **View** – FXML-Dateien definieren das Layout; CSS-Styles direkt im FXML
- **Controller** – Java-Klassen mit `@FXML`-Injektionen steuern die UI-Logik

### Singleton-Pattern
Zwei zentrale Singletons teilen den Zustand über alle Controller:

- **`TakeoverState`** – Speichert den gesamten Takeover-Fortschritt: welche Teile genehmigt sind, welche Techniker eingetragen sind, alle Reparaturaufgaben und die letzte Aktivität
- **`TicketStore`** – Speichert Wartungstickets nur im Arbeitsspeicher (kein File-I/O); Daten werden beim Schließen der App gelöscht

### BaseController
Alle View-Controller erben von `BaseController`, der die `loadView(String fxml)`-Methode für Navigation bereitstellt.

### Jackson-Integration
`ShuttlePart` nutzt `@JsonAnySetter`, damit Jackson alle Sensor-Schlüssel aus der JSON-Datei direkt in die interne `sensors`-Map einträgt – ohne dass jeder Sensor ein eigenes Feld braucht.

---

## Takeover-Workflow

Der Takeover-Prozess umfasst drei Shuttle-Teile: **Orbiter**, **SRB** und **External Tank**. Für jeden Teil gilt folgender Ablauf:

### Schritt 1 – Shuttle-Teil wählen (Mission Control)
Im Mission-Control-Bereich auf einen der drei Part-Buttons klicken:
`[Orbiter]` `[SRB]` `[External Tank]`

### Schritt 2 – Rolle und Name eingeben
Nach der Part-Auswahl erscheint das Eingabefeld:
- **Name** eingeben (wird in der History und im Dashboard angezeigt)
- **Rolle** wählen: `Technician` oder `Security Chief`
- `Confirm` drücken

### Schritt 3 – Reparaturen durchführen (Technician)
Als Technician sieht man die Reparaturtabelle für den gewählten Part:
- Alle Reparaturaufgaben mit Sensor, Status (WARNING/REPLACE) und Maßnahme
- Ersatzteile-Spalte: zeigt ob Teil auf Lager ist oder bestellt werden muss
- Bei `OUT_OF_STOCK`: **Order Part**-Button drücken → nach 10 Sekunden erscheint eine Pop-up-Benachrichtigung, dass das Teil eingetroffen ist
- Done-Checkbox ist erst aktivierbar, wenn das Teil verfügbar ist
- Wenn alle Aufgaben erledigt: `Mark All Done` drücken

### Schritt 4 – Finale Freigabe (Security Chief)
Als Security Chief denselben Part wählen:
- Der `Give OK`-Button ist nur aktiv, wenn der Technician bereits `Mark All Done` gedrückt hat
- Nach `Give OK` gilt der Part als freigegeben (`Approved ✓`)

### Schritt 5 – Takeover abschließen
Sobald alle drei Teile freigegeben sind (100% Fortschritt), erscheint der Button `Takeover Complete` im Dashboard. Ein Klick setzt alles zurück und generiert neue Reparaturaufgaben.

---

## Setup & Ausführen

### Voraussetzungen
- Java 17 oder höher installiert
- Maven installiert (oder Maven Wrapper `./mvnw` nutzen)

### App starten

```bash
cd shuttle-dashboard
mvn javafx:run
```

### Ersten Start / Inventar zurücksetzen

Beim ersten Start wird `~/.shuttle-dashboard/inventory.json` automatisch aus den classpath-Ressourcen kopiert. Um die Inventardaten zurückzusetzen (z.B. nach Tests):

```bash
rm ~/.shuttle-dashboard/inventory.json
```

Beim nächsten Start wird die Datei automatisch neu angelegt.

---

## Datenpersistenz

| Daten              | Speicherort                              | Verhalten                                   |
|--------------------|------------------------------------------|---------------------------------------------|
| Inventar           | `~/.shuttle-dashboard/inventory.json`    | Wird dauerhaft gespeichert; überlebt Neustart |
| Sensorwerte        | `src/main/resources/view/data/sensors.json` | Schreibgeschützt (classpath)              |
| Grenzwerte         | `src/main/resources/view/data/thresholds.json` | Schreibgeschützt (classpath)           |
| Wartungshistorie   | Arbeitsspeicher (TicketStore)            | Wird beim Schließen der App gelöscht        |
| Takeover-Zustand   | Arbeitsspeicher (TakeoverState)          | Wird beim Schließen der App gelöscht        |

---

## Testdaten

### Sensoren (sensors.json)

| Part          | Sensor              | Wert  | Status  |
|---------------|---------------------|-------|---------|
| Orbiter       | fuelPressure        | 350.5 | OK      |
| Orbiter       | coolantPressure     | 2.8   | REPLACE |
| Orbiter       | engineTemp          | 520.0 | WARNING |
| SRB           | thrustLevel         | 88.5  | OK      |
| SRB           | casingTemp          | 430.0 | WARNING |
| External Tank | liquidOxygenLevel   | 95.2  | OK      |
| External Tank | hydrogenPressure    | 42.0  | OK      |

### Grenzwerte (thresholds.json)

Jeder Sensor hat einen `min`- und/oder `max`-Grenzwert:
- Liegt der Wert im normalen Bereich → **OK** (grün)
- Liegt er knapp außerhalb → **WARNING** (gelb) → `Inspect and adjust`
- Liegt er weit außerhalb → **REPLACE** (rot) → `Replace component`

### Inventar

- **Heat Shield Panel** (INV-001) – Anfangsbestand: 0 → `OUT_OF_STOCK` → muss bestellt werden
- Weitere Teile sind auf Lager und können direkt verwendet werden

---

## Projektteam

Gruppe 5 – Universitätsprojekt
