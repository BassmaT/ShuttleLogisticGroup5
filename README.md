# Shuttle Dashboard – Gruppe 5
KI generiert: 

Eine JavaFX-Desktop-Anwendung zur Verwaltung des Pre-Launch-Takeover-Prozesses für Raumfähren. Das System unterstützt Techniker und Security Chiefs dabei, alle Shuttle-Komponenten zu überprüfen, Reparaturen durchzuführen und die finale Freigabe zu erteilen.

---

## Features

- **Landing-Phase** – Beim Start landet das Shuttle simuliert (15 Sek. = 15 Min.). Nur der Logistics-Tab ist aktiv; alle anderen Tabs sind ausgegraut.
- **Sensor-Ladepha­se** – Nach der Landung werden die Sensoren kalibriert (10 Sek. = 30 Min.). Fortschrittsbalken zeigt den Status.
- **Haupt-Dashboard** – Überblick über den Takeover-Fortschritt (0–100%), Sensorwarnungen, aktive Arbeiter, 3-Tage-Zeitplan und Predictive Maintenance.
- **Mission Control** – Mitarbeiter einem Shuttle-Teil zuweisen; Reparatur- und Routineaufgaben pro Teil sichtbar; zweistufige Freigabe (Techniker → Security Chief).
- **Technician-Panel** – Reparaturaufgaben als Tabelle; Checkboxen mit automatischer Ersatzteil-Prüfung.
- **Inventory** – Lagerbestand der Ersatzteile mit Name, Menge, Status und Beschreibung.
- **History** – Wartungshistorie aus den letzten Flügen.
- **Staff** – Routineaufgaben als Checkliste; erledigte Aufgaben werden durchgestrichen.
- **Logistics** – Teile per ComboBox auswählen und bestellen; Lagerbestand wird nach Lieferung automatisch erhöht; aktueller Lagerbestand sichtbar.
- **Schedule** – 3-Tage-Zeitplan interaktiv; Mitarbeiter können per ComboBox umgeplant werden (z. B. bei Krankheit oder Urlaub).
- **Sensorüberwachung** – Farbcodiert: Grün (OK), Gelb (WARNING), Rot (REPLACE).
- **Predictive Maintenance** – Trendanalyse aus den letzten 5 Flügen; Warnungen, wenn ein Sensor in ≤5 Flügen den Grenzwert erreicht.
- **Dunkel-Theme** – Durchgängig dunkle Benutzeroberfläche (#1e1e1e).

---

## Technologie-Stack

| Technologie         | Version  | Zweck                                     |
|---------------------|----------|-------------------------------------------|
| Java                | 17       | Programmiersprache                        |
| JavaFX              | 21.0.2   | UI-Framework (FXML, Controls, Properties) |
| Jackson             | 2.17.0   | JSON-Serialisierung / Deserialisierung    |
| Maven               | –        | Build-Tool und Abhängigkeitsverwaltung    |
| javafx-maven-plugin | 0.0.8    | App starten via `mvn javafx:run`          |

---

## Projektstruktur

```
shuttle-dashboard/
├── pom.xml
└── src/main/
    ├── java/com/group5/shuttle/
    │   ├── Main.java                        ← JavaFX Launch-Wrapper
    │   ├── App.java                         ← Fenster aufbauen, main_view.fxml laden
    │   ├── controller/
    │   │   ├── BaseController.java          ← Abstrakte Basisklasse für alle Controller
    │   │   ├── MainController.java          ← Haupt-Dashboard + Landing/Sensor-Timer
    │   │   ├── MissionControlController.java← Mitarbeiter zuweisen, Teile freigeben
    │   │   ├── TechnicianController.java    ← Reparaturaufgaben einsehen
    │   │   ├── InventoryController.java     ← Lagerbestand anzeigen
    │   │   ├── HistoryController.java       ← Wartungshistorie anzeigen
    │   │   ├── StaffController.java         ← Routineaufgaben Checkliste
    │   │   ├── LogisticsController.java     ← Teile bestellen, Lagerbestand verwalten
    │   │   ├── ScheduleController.java      ← Interaktiver 3-Tage-Zeitplan
    │   │   └── LandingController.java       ← (veraltet, Landing in MainController integriert)
    │   ├── model/
    │   │   ├── ShuttleData.java             ← Wurzelobjekt der Sensordaten (3 Teile)
    │   │   ├── ShuttlePart.java             ← Ein Shuttle-Teil mit Sensor-Map
    │   │   ├── SensorThreshold.java         ← Grenzwerte (min/max, warn/replace)
    │   │   ├── SensorRow.java               ← Tabellenzeile für Sensoranzeige
    │   │   ├── RepairTask.java              ← Eine Reparaturaufgabe
    │   │   ├── InventoryItem.java           ← Ein Lagerteil (Name, Menge, Status)
    │   │   ├── RoutineTask.java             ← Eine Routineaufgabe mit done-Property
    │   │   ├── Employee.java                ← Mitarbeiter (ID, Name, Rolle, Team)
    │   │   ├── EmployeeRoster.java          ← Wrapper für employees.json
    │   │   ├── EmployeeTeam.java            ← Team mit Mitarbeiterliste
    │   │   ├── ScheduleEntry.java           ← Ein Eintrag im Zeitplan
    │   │   ├── ScheduleDay.java             ← Ein Tag mit mehreren Einträgen
    │   │   ├── TakeoverSchedule.java        ← Wrapper für schedule.json
    │   │   ├── LogisticsOrder.java          ← Eine Bestellung im Logistics-System
    │   │   ├── FlightRecord.java            ← Sensordaten eines vergangenen Fluges
    │   │   ├── FlightHistory.java           ← Wrapper für flight_history.json
    │   │   ├── TrendResult.java             ← Ergebnis der Trendanalyse
    │   │   └── MaintenanceTicket.java       ← Wartungsticket (History)
    │   └── service/
    │       ├── SensorService.java           ← Alle JSON-Daten lesen/schreiben
    │       ├── TakeoverState.java           ← Zentraler Singleton: Phase, Fortschritt, Mitarbeiter
    │       ├── EmployeeService.java         ← Singleton: Mitarbeiterliste laden und abrufen
    │       ├── RoutineTaskStore.java        ← Singleton: Routineaufgaben im Speicher
    │       ├── OrderStore.java              ← Singleton: Logistik-Bestellungen im Speicher
    │       ├── PredictiveAnalysisService.java ← Trendanalyse aus Flughistorie
    │       └── TicketStore.java             ← Wartungstickets im Speicher
    └── resources/view/
        ├── main_view.fxml
        ├── mission_control.fxml
        ├── technician.fxml
        ├── inventory.fxml
        ├── history.fxml
        ├── staff.fxml
        ├── logistics.fxml
        ├── schedule_view.fxml
        └── data/
            ├── sensors.json         ← Aktuelle Sensorwerte der 3 Shuttle-Teile
            ├── thresholds.json      ← Grenzwerte pro Sensor (warn/replace)
            ├── inventory.json       ← Lagerbestand der Ersatzteile
            ├── employees.json       ← Mitarbeiterliste mit Rollen und Teams
            ├── schedule.json        ← 3-Tage-Zeitplan mit Aufgaben und Mitarbeitern
            ├── routine_tasks.json   ← Routineaufgaben pro Shuttle-Teil
            ├── flight_history.json  ← Sensordaten der letzten 5 Flüge (Trendanalyse)
            └── tickets.json         ← Wartungstickets (Beispieldaten)
```

---

## Architektur

### MVC-Pattern

| Schicht       | Inhalt                                                             |
|---------------|--------------------------------------------------------------------|
| **Model**     | Datenklassen: `ShuttlePart`, `RepairTask`, `InventoryItem`, ...    |
| **View**      | FXML-Dateien definieren das Layout; Styles direkt im FXML          |
| **Controller**| Java-Klassen mit `@FXML`-Injektion steuern die UI-Logik           |

### Singleton-Pattern

Vier Singletons teilen den Zustand über alle Controller:

| Singleton              | Inhalt                                                              |
|------------------------|---------------------------------------------------------------------|
| `TakeoverState`        | App-Phase, Fortschritt, Mitarbeiter, Freigaben, Reparaturen        |
| `EmployeeService`      | Mitarbeiterliste (aus employees.json, einmal geladen)              |
| `RoutineTaskStore`     | Routineaufgaben mit done-Status (Speicher, kein File-I/O)          |
| `OrderStore`           | Aktive Logistik-Bestellungen der Sitzung                           |

### App-Phasen (State Machine)

```
LANDING (15 Sek.)
    └── Countdown läuft; nur Logistics aktiv
    └── Nach Ablauf: 3 Sek. Pause → SENSOR_LOADING

SENSOR_LOADING (10 Sek.)
    └── Alle Tabs gesperrt; Lade-Animation
    └── Nach Ablauf → OPERATIONAL

OPERATIONAL
    └── Alle Tabs aktiv; Sensordaten und Zeitplan sichtbar
    └── Takeover-Workflow läuft
```

Der Zustand überlebt Navigation (z.B. zu Logistics und zurück), weil `Instant landingStartedAt` im Singleton gespeichert wird und `getRemainingSeconds()` die Restzeit berechnet.

---

## Takeover-Workflow

Für jeden der drei Teile (**Orbiter**, **SRB**, **External Tank**) gilt:

```
1. Mission Control → Teil wählen → Techniker eintragen
2. Techniker sieht Reparatur- und Routineaufgaben für diesen Teil
3. Aufgaben erledigen → "Mark Done" klicken
4. Security Chief für das gleiche Teil eintragen
5. Security Chief klickt "Approve" (nur aktiv, wenn Techniker fertig)
6. Teil gilt als freigegeben (✓)
```

Sobald alle drei Teile freigegeben sind → Fortschrittsbalken 100% → Button **Takeover Complete** erscheint → Klick setzt alles zurück.

---

## Setup & Ausführen

### Voraussetzungen

- Java 17 oder höher
- Maven installiert

### App starten

```bash
mvn javafx:run
```

---

## Datenpersistenz

| Daten                | Speicherort                             | Verhalten                                  |
|----------------------|-----------------------------------------|--------------------------------------------|
| Inventar             | `resources/view/data/inventory.json`    | Wird dauerhaft gespeichert                 |
| Sensorwerte          | `resources/view/data/sensors.json`      | Schreibgeschützt                           |
| Grenzwerte           | `resources/view/data/thresholds.json`   | Schreibgeschützt                           |
| Routineaufgaben      | `RoutineTaskStore` (Speicher)           | Wird beim Schließen gelöscht               |
| Bestellungen         | `OrderStore` (Speicher)                 | Wird beim Schließen gelöscht               |
| Takeover-Zustand     | `TakeoverState` (Speicher)              | Wird beim Schließen gelöscht               |
| Schedule-Änderungen  | `TakeoverSchedule` (Speicher)           | Wird beim Schließen gelöscht               |

---

## Projektteam

Gruppe 5 – Universitätsprojekt
