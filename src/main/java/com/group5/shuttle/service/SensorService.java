package com.group5.shuttle.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.group5.shuttle.model.EmployeeRoster;
import com.group5.shuttle.model.FlightHistory;
import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.model.MaintenanceTicket;
import com.group5.shuttle.model.RoutineTask;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;
import com.group5.shuttle.model.TakeoverSchedule;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// Diese Klasse ist für das Laden und Speichern aller Daten zuständig:
// Sensordaten, Grenzwerte, Lagerhaltung und Wartungshistorie.
// Sie wird von den Controllern benutzt, um auf die Daten zuzugreifen.
public class SensorService {

    // ObjectMapper ist die Jackson-Klasse, die JSON liest und schreibt.
    // INDENT_OUTPUT sorgt dafür, dass gespeicherte JSON-Dateien lesbar formatiert sind.
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    // Pfad zum beschreibbaren Datenordner im Home-Verzeichnis des Benutzers: ~/.shuttle-dashboard/
    // System.getProperty("user.home") gibt z. B. "/Users/Max" zurück.
    private static final Path DATA_DIR      = Path.of(System.getProperty("user.home"), ".shuttle-dashboard");

    // Vollständiger Dateipfad zur Lagerdatei, z. B. "/Users/Max/.shuttle-dashboard/inventory.json"
    private static final File INVENTORY_FILE = DATA_DIR.resolve("inventory.json").toFile();

    // Stellt sicher, dass der Datenordner und die Lagerdatei existieren.
    // Beim ersten Start wird die Lagerdatei aus den Projektdaten kopiert (Seed).
    private void ensureDataDir() {
        try {
            Files.createDirectories(DATA_DIR); // Ordner anlegen, falls er noch nicht existiert
            if (!INVENTORY_FILE.exists()) {
                // Erste Ausführung: Initialdaten aus dem Classpath (resources-Ordner) kopieren
                try (InputStream is = getClass().getClassLoader()
                        .getResourceAsStream("view/data/inventory.json")) {
                    if (is != null) Files.copy(is, INVENTORY_FILE.toPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Fehler ausgeben, Programm läuft aber weiter
        }
    }

    // ── Sensordaten und Grenzwerte ──────────────────────────────────────────

    // Lädt die aktuellen Sensorwerte aus der sensors.json-Datei im Classpath.
    // Gibt null zurück, wenn die Datei nicht gelesen werden kann.
    public ShuttleData loadSensorData() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("view/data/sensors.json");
            return mapper.readValue(is, ShuttleData.class); // JSON → Java-Objekt
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Lädt die Grenzwerte aller Sensoren aus der thresholds.json-Datei im Classpath.
    // Gibt eine verschachtelte Map zurück: Teil → Sensor → Grenzwert-Objekt.
    public Map<String, Map<String, SensorThreshold>> loadThresholds() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("view/data/thresholds.json");
            return mapper.readValue(is, new TypeReference<>() {}); // JSON → verschachtelte Map
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Lagerhaltung ────────────────────────────────────────────────────────

    // Lädt die aktuelle Lagerliste aus der beschreibbaren inventory.json-Datei.
    // Beim ersten Start wird diese Datei automatisch aus den Projektdaten erstellt.
    public List<InventoryItem> loadInventory() {
        ensureDataDir(); // sicherstellen, dass die Datei existiert
        try {
            return mapper.readValue(INVENTORY_FILE, new TypeReference<>() {}); // JSON → Liste
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList(); // leere Liste zurückgeben, damit das Programm nicht abstürzt
        }
    }

    // Speichert die aktuelle Lagerliste dauerhaft in die inventory.json-Datei.
    // Wird aufgerufen, wenn Teile verbraucht oder bestellt werden.
    public void saveInventory(List<InventoryItem> items) {
        ensureDataDir();
        try {
            mapper.writeValue(INVENTORY_FILE, items); // Liste → JSON-Datei
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Wartungshistorie (nur im Arbeitsspeicher) ──────────────────────────

    // Gibt alle Tickets der aktuellen Sitzung zurück.
    // Die Daten liegen nur im Arbeitsspeicher – beim Schließen der App sind sie weg.
    public List<MaintenanceTicket> loadTickets() {
        return TicketStore.getInstance().getTickets();
    }

    // Fügt ein einzelnes Ticket zur Sitzungs-History hinzu.
    public void appendTicket(MaintenanceTicket ticket) {
        List<MaintenanceTicket> list = TicketStore.getInstance().getTickets(); // aktuelle Liste holen
        list.add(ticket);                                                      // neues Ticket anhängen
        TicketStore.getInstance().saveTickets(list);                           // zurückspeichern
    }

    // Ersetzt die gesamte Ticket-Liste durch eine neue Liste.
    // Wird aufgerufen, wenn nach einer Reparatur alle Tickets auf einmal gespeichert werden.
    public void saveTickets(List<MaintenanceTicket> tickets) {
        TicketStore.getInstance().saveTickets(tickets);
    }

    // ── Sensorauswertung ──────────────────────────────────────────────────

    // Vergleicht einen Messwert mit den Grenzwerten und gibt das Ergebnis zurück:
    // "REPLACE"  → Wert liegt klar außerhalb des erlaubten Bereichs (sofort tauschen)
    // "WARNING"  → Wert nähert sich dem Grenzwert (Warnung, prüfen)
    // "OK"       → Wert liegt im normalen Bereich
    public String evaluate(double value, SensorThreshold t) {
        // Prüfen, ob der Wert klar unter dem Minimum oder über dem Maximum liegt → REPLACE
        if (t.min != null && value < t.min) return "REPLACE";
        if (t.max != null && value > t.max) return "REPLACE";

        // Prüfen, ob der Wert sich dem Minimum oder Maximum auf 5 Einheiten nähert → WARNING
        if (t.min != null && value < t.min + 5) return "WARNING";
        if (t.max != null && value > t.max - 5) return "WARNING";

        // Alles in Ordnung
        return "OK";
    }

    // ── Mitarbeiter ──────────────────────────────────────────────────────────

    // Lädt alle Mitarbeiter aus der employees.json-Datei im Classpath.
    // Gibt ein EmployeeRoster-Objekt zurück, das alle Teams enthält.
    public EmployeeRoster loadEmployees() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("view/data/employees.json");
            return mapper.readValue(is, EmployeeRoster.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Routineaufgaben ──────────────────────────────────────────────────────

    // Lädt die Routineaufgaben aus der routine_tasks.json-Datei im Classpath.
    // Diese Aufgaben sind bei jeder Übergabe durchzuführen, unabhängig von Sensordaten.
    public List<RoutineTask> loadRoutineTasks() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("view/data/routine_tasks.json");
            return mapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ── Zeitplan ─────────────────────────────────────────────────────────────

    // Lädt den 3-Tage-Zeitplan aus der schedule.json-Datei im Classpath.
    public TakeoverSchedule loadSchedule() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("view/data/schedule.json");
            return mapper.readValue(is, TakeoverSchedule.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Flughistorie (Trendanalyse) ───────────────────────────────────────────

    // Lädt die simulierten Flugdaten der letzten 5 Flüge aus flight_history.json.
    // Wird von PredictiveAnalysisService für die Trendberechnung verwendet.
    public FlightHistory loadFlightHistory() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("view/data/flight_history.json");
            return mapper.readValue(is, FlightHistory.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
