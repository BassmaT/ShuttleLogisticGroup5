package com.group5.shuttle.service;

import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;
import com.group5.shuttle.model.ShuttlePart;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Diese Klasse verwaltet den gesamten Zustand des Übergabe-Workflows als Singleton.
// Sie speichert, welche Techniker und Security Chiefs eingetragen sind,
// welche Reparaturaufgaben für jeden Shuttle-Teil anfallen,
// und ob der jeweilige Teil bereits freigegeben wurde.
public class TakeoverState {

    // Die eine einzige Instanz dieser Klasse – beim ersten Aufruf erstellt, danach wiederverwendet.
    private static TakeoverState instance;

    // partKey → true, wenn der Security Chief für diesen Teil die Freigabe erteilt hat.
    private final Map<String, Boolean> securityApprovals = new HashMap<>();

    // partKey → Liste der Reparaturaufgaben, die für diesen Teil ermittelt wurden.
    private final Map<String, List<RepairTask>> repairs = new HashMap<>();

    // partKey → Name des eingetragenen Technikers für diesen Teil.
    private final Map<String, String> activeTechnicians = new HashMap<>();

    // partKey → Name des eingetragenen Security Chiefs für diesen Teil.
    private final Map<String, String> activeSecurityChiefs = new HashMap<>();

    // partKey → true, wenn der Techniker auf "Fertig" geklickt hat.
    private final Map<String, Boolean> technicianDone = new HashMap<>();

    // Letzte protokollierte Aktivität – wird im Dashboard angezeigt.
    private String lastActivity = "";

    // ── App-Phase ─────────────────────────────────────────────────────────────

    // Drei Phasen: Landung (15 Sek.) → Sensordaten laden (10 Sek.) → Betrieb
    public enum AppPhase { LANDING, SENSOR_LOADING, OPERATIONAL }

    // Aktuelle Phase der App
    private AppPhase appPhase = AppPhase.LANDING;

    // Zeitstempel für den Start der Landephase und der Sensordaten-Ladephase
    private Instant landingStartedAt   = null;
    private Instant sensorLoadingStartedAt = null;

    // Dauer der Simulationen in Sekunden
    public static final int LANDING_SECONDS       = 15; // simuliert 15 Minuten
    public static final int SENSOR_LOADING_SECONDS = 10; // simuliert 30 Minuten

    // Gibt die aktuelle Phase zurück
    public AppPhase getAppPhase() { return appPhase; }

    // Startet die Landephase und merkt sich den Zeitstempel
    public void beginLanding() {
        this.appPhase = AppPhase.LANDING;
        this.landingStartedAt = Instant.now();
    }

    // Startet die Sensordaten-Ladephase und merkt sich den Zeitstempel
    public void beginSensorLoading() {
        this.appPhase = AppPhase.SENSOR_LOADING;
        this.sensorLoadingStartedAt = Instant.now();
    }

    // Wechselt in den Betriebsmodus (alle Funktionen freigeschaltet)
    public void setOperational() {
        this.appPhase = AppPhase.OPERATIONAL;
    }

    // Verbleibende Sekunden in der aktuellen Phase (0 wenn Phase bereits abgelaufen)
    public int getRemainingSeconds() {
        if (appPhase == AppPhase.LANDING && landingStartedAt != null) {
            long elapsed = java.time.Duration.between(landingStartedAt, Instant.now()).getSeconds();
            return (int) Math.max(0, LANDING_SECONDS - elapsed);
        }
        if (appPhase == AppPhase.SENSOR_LOADING && sensorLoadingStartedAt != null) {
            long elapsed = java.time.Duration.between(sensorLoadingStartedAt, Instant.now()).getSeconds();
            return (int) Math.max(0, SENSOR_LOADING_SECONDS - elapsed);
        }
        return 0;
    }

    // Interne Schlüssel der drei Shuttle-Teile (werden für Maps verwendet).
    public static final String[] PART_KEYS    = {"orbiter", "srb", "externalTank"};

    // Anzeigenamen der drei Shuttle-Teile (werden für die Benutzeroberfläche verwendet).
    public static final String[] PART_DISPLAY = {"Orbiter", "SRB", "External Tank"};

    // Rollenbezeichnung für den Techniker.
    public static final String ROLE_TECHNICIAN    = "Technician";

    // Rollenbezeichnung für den Security Chief.
    public static final String ROLE_SECURITY_CHIEF = "Security Chief";

    // Privater Konstruktor – initialisiert alle Maps mit Standardwerten für jeden Teil.
    private TakeoverState() {
        for (String key : PART_KEYS) {
            securityApprovals.put(key, false);  // kein Teil ist zu Beginn freigegeben
            technicianDone.put(key, false);      // kein Techniker hat zu Beginn fertig gemeldet
            repairs.put(key, new ArrayList<>()); // leere Reparaturliste für jeden Teil
        }
        // Landephase sofort beim Start beginnen
        beginLanding();
    }

    // Gibt die einzige Instanz zurück. Falls noch keine existiert, wird sie hier erstellt.
    public static TakeoverState getInstance() {
        if (instance == null) instance = new TakeoverState();
        return instance;
    }

    // ── Mitarbeiter-Registrierung ────────────────────────────────────────────

    // Trägt einen Mitarbeiter (Techniker oder Security Chief) für einen bestimmten Teil ein.
    // Anhand der Rolle wird entschieden, in welche Map der Name gespeichert wird.
    public void registerWorker(String partKey, String name, String role) {
        if (ROLE_TECHNICIAN.equals(role)) {
            activeTechnicians.put(partKey, name);       // Techniker eintragen
        } else {
            activeSecurityChiefs.put(partKey, name);    // Security Chief eintragen
        }
    }

    // Gibt eine zusammengefasste Anzeige aller eingetragenen Mitarbeiter für einen Teil zurück.
    // Mögliche Rückgaben: "Name (Tech) / Name (Chief)", "Name (Technician)", "Name (Security Chief)" oder null.
    public String getWorkerInfo(String partKey) {
        String tech  = activeTechnicians.get(partKey);
        String chief = activeSecurityChiefs.get(partKey);
        if (tech != null && chief != null)
            return tech + " (Tech) / " + chief + " (Chief)";   // beide eingetragen
        if (tech  != null) return tech  + " (Technician)";     // nur Techniker eingetragen
        if (chief != null) return chief + " (Security Chief)"; // nur Security Chief eingetragen
        return null; // noch niemand eingetragen
    }

    // Gibt den Namen des Technikers für einen bestimmten Teil zurück (oder null).
    public String getTechnicianName(String partKey) {
        return activeTechnicians.get(partKey);
    }

    // Gibt den Namen des Security Chiefs für einen bestimmten Teil zurück (oder null).
    public String getSecurityChiefName(String partKey) {
        return activeSecurityChiefs.get(partKey);
    }

    // ── Techniker-Fertigmeldung ──────────────────────────────────────────────

    // Markiert einen Teil als vom Techniker abgeschlossen.
    // Die letzte Aktivität wird mit Name und Teil-Anzeigename protokolliert.
    public void markTechnicianDone(String partKey, String techName) {
        technicianDone.put(partKey, true);
        lastActivity = techName + " hat Reparaturen an " + getDisplayName(partKey) + " abgeschlossen";
    }

    // Gibt zurück, ob der Techniker diesen Teil als fertig gemeldet hat.
    public boolean isTechnicianDone(String partKey) {
        return technicianDone.getOrDefault(partKey, false);
    }

    // ── Reparaturaufgaben ────────────────────────────────────────────────────

    // Ermittelt alle Reparaturaufgaben aus den aktuellen Sensordaten und Grenzwerten.
    // Jedem auffälligen Sensor (WARNING oder REPLACE) wird per Round-Robin ein
    // passendes Lagerteil aus dem Inventar zugewiesen.
    public void generateRepairs(ShuttleData data, Map<String, Map<String, SensorThreshold>> thresholds,
                                SensorService sensorService) {
        // Bestehende Reparaturlisten für alle Teile leeren
        for (String key : PART_KEYS) repairs.get(key).clear();

        // Ohne gültige Sensordaten oder Grenzwerte können keine Aufgaben erzeugt werden
        if (data == null || thresholds == null) return;

        // Inventar nach Teil-Anzeigename gruppieren: Anzeigename → Liste der Lagerartikel
        Map<String, List<InventoryItem>> inventoryByPart = new HashMap<>();
        for (InventoryItem item : sensorService.loadInventory()) {
            inventoryByPart.computeIfAbsent(item.getPart(), k -> new ArrayList<>()).add(item);
        }

        // Shuttle-Teile den internen Schlüsseln zuordnen
        Map<String, ShuttlePart> parts = new HashMap<>();
        parts.put("orbiter",      data.orbiter);
        parts.put("srb",          data.srb);
        parts.put("externalTank", data.externalTank);

        // Round-Robin-Zähler pro Teil: sorgt dafür, dass aufeinanderfolgende Aufgaben
        // unterschiedliche Lagerartikel desselben Teils erhalten.
        Map<String, Integer> partIndex = new HashMap<>();

        for (String partKey : PART_KEYS) {
            ShuttlePart part = parts.get(partKey);
            if (part == null || part.getSensors() == null) continue; // überspringe leere Teile

            Map<String, SensorThreshold> pt = thresholds.get(partKey);
            String displayName = getDisplayName(partKey);

            // Alle Lagerartikel, die zu diesem Teil gehören
            List<InventoryItem> invItems = inventoryByPart.getOrDefault(displayName, List.of());

            // Jeden Sensor des Teils auswerten
            for (var entry : part.getSensors().entrySet()) {
                if (pt == null) continue; // keine Grenzwerte für diesen Teil vorhanden

                SensorThreshold t = pt.get(entry.getKey());
                if (t == null) continue; // kein Grenzwert für diesen Sensor vorhanden

                String result = sensorService.evaluate(entry.getValue(), t);

                // Nur auffällige Sensoren (WARNING oder REPLACE) erzeugen Reparaturaufgaben
                if (result.equals("WARNING") || result.equals("REPLACE")) {
                    RepairTask task = new RepairTask(partKey, entry.getKey(), result);

                    // Lagerartikel per Round-Robin zuweisen, falls welche vorhanden sind
                    if (!invItems.isEmpty()) {
                        int idx = partIndex.getOrDefault(partKey, 0) % invItems.size(); // zyklischer Index
                        task.setRequiredItemName(invItems.get(idx).getName());            // Artikel zuweisen
                        partIndex.put(partKey, idx + 1);                                  // Zähler erhöhen
                    }

                    repairs.get(partKey).add(task); // Aufgabe zur Liste des Teils hinzufügen
                }
            }
        }
    }

    // Gibt die Liste aller Reparaturaufgaben für einen bestimmten Teil zurück.
    public List<RepairTask> getRepairs(String partKey) {
        return repairs.getOrDefault(partKey, new ArrayList<>());
    }

    // Gibt zurück, ob alle Reparaturaufgaben eines Teils als erledigt markiert sind.
    // Eine leere Aufgabenliste gilt ebenfalls als vollständig abgeschlossen.
    public boolean allRepairsDone(String partKey) {
        List<RepairTask> tasks = repairs.get(partKey);
        if (tasks == null || tasks.isEmpty()) return true; // keine Aufgaben → alles erledigt
        return tasks.stream().allMatch(RepairTask::isDone); // alle Aufgaben müssen erledigt sein
    }

    // ── Freigabe ─────────────────────────────────────────────────────────────

    // Prüft, ob der Security Chief die Freigabe für einen Teil erteilen darf.
    // Voraussetzungen:
    //   1. Der Techniker hat "Fertig" gemeldet.
    //   2. Der Teil wurde noch nicht freigegeben.
    public boolean canApprove(String partKey) {
        return isTechnicianDone(partKey) && !isPartApproved(partKey);
    }

    // Erteilt die Freigabe für einen Teil durch den angegebenen Security Chief.
    // Die letzte Aktivität wird mit Name und Teil-Anzeigename protokolliert.
    public void approve(String partKey, String chiefName) {
        securityApprovals.put(partKey, true);
        lastActivity = chiefName + " hat " + getDisplayName(partKey) + " freigegeben";
    }

    // Gibt zurück, ob ein bestimmter Teil bereits vom Security Chief freigegeben wurde.
    public boolean isPartApproved(String partKey) {
        return securityApprovals.getOrDefault(partKey, false);
    }

    // ── Fortschritt ──────────────────────────────────────────────────────────

    // Berechnet den Gesamtfortschritt der Übergabe als Wert zwischen 0.0 und 1.0.
    // Jeder freigegebene Teil zählt als ein Drittel des Gesamtfortschritts.
    public double getProgress() {
        long approved = securityApprovals.values().stream().filter(v -> v).count(); // Anzahl freigegebener Teile
        return approved / 3.0; // drei Teile insgesamt → jeder zählt 1/3
    }

    // Gibt true zurück, wenn alle drei Teile freigegeben wurden (Übergabe abgeschlossen).
    public boolean isTakeoverComplete() {
        return getProgress() >= 1.0;
    }

    // ── Letzte Aktivität ─────────────────────────────────────────────────────

    // Gibt die zuletzt protokollierte Aktivität zurück (wird im Dashboard angezeigt).
    public String getLastActivity() {
        return lastActivity;
    }

    // ── Zurücksetzen ─────────────────────────────────────────────────────────

    // Setzt den gesamten Workflow zurück in den Ausgangszustand.
    // Alle Mitarbeiter, Freigaben, Fertigmeldungen und Reparaturlisten werden geleert.
    // Auch Routineaufgaben und Bestellungen werden zurückgesetzt.
    public void reset() {
        activeTechnicians.clear();       // alle Techniker-Einträge löschen
        activeSecurityChiefs.clear();    // alle Security-Chief-Einträge löschen
        lastActivity = "";               // letzte Aktivität zurücksetzen
        for (String key : PART_KEYS) {
            securityApprovals.put(key, false);  // Freigabe-Status zurücksetzen
            technicianDone.put(key, false);      // Fertigmeldung zurücksetzen
            repairs.get(key).clear();            // Reparaturliste leeren
        }
        // Routineaufgaben-Status zurücksetzen (Erledigt-Flags + Zeitstempel löschen)
        RoutineTaskStore.getInstance().reset();
        // Alle Logistik-Bestellungen der Sitzung löschen
        OrderStore.getInstance().clear();
    }

    // ── Hilfsmethoden ────────────────────────────────────────────────────────

    // Gibt den Anzeigenamen eines Teils anhand seines internen Schlüssels zurück.
    // Beispiel: "orbiter" → "Orbiter", "srb" → "SRB", "externalTank" → "External Tank".
    // Falls der Schlüssel unbekannt ist, wird er unverändert zurückgegeben.
    public static String getDisplayName(String partKey) {
        for (int i = 0; i < PART_KEYS.length; i++)
            if (PART_KEYS[i].equals(partKey)) return PART_DISPLAY[i];
        return partKey; // unbekannter Schlüssel: Rohwert zurückgeben
    }
}
