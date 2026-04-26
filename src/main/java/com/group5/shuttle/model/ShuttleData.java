package com.group5.shuttle.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// Dieses Datenmodell stellt den gesamten Shuttle dar.
// OCP: getAllParts() gibt direkt die interne Map zurück – neuer Part → nur neuen Setter hinzufügen.
public class ShuttleData {

    // Einzige Quelle der Wahrheit für alle Shuttle-Teile (Reihenfolge: Orbiter → SRB → Tank).
    private final Map<String, ShuttlePart> parts = new LinkedHashMap<>();

    public ShuttlePart getOrbiter()      { return parts.get("orbiter"); }
    public ShuttlePart getSrb()          { return parts.get("srb"); }
    public ShuttlePart getExternalTank() { return parts.get("externalTank"); }

    public void setOrbiter(ShuttlePart p)      { parts.put("orbiter",      p); }
    public void setSrb(ShuttlePart p)          { parts.put("srb",          p); }
    public void setExternalTank(ShuttlePart p) { parts.put("externalTank", p); }

    public Map<String, ShuttlePart> getAllParts() {
        return Collections.unmodifiableMap(parts);
    }
}
