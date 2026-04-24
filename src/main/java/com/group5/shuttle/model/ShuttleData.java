package com.group5.shuttle.model;

// Dieses Datenmodell stellt den gesamten Shuttle dar.
// Die drei Felder repräsentieren die Hauptkomponenten des Shuttles.
public class ShuttleData {

    // Der Orbiter – das eigentliche Raumschiff (Rumpf, Kabine, Triebwerke).
    private ShuttlePart orbiter;

    // Die Feststoffraketen (Solid Rocket Boosters) – starten den Shuttle.
    private ShuttlePart srb;

    // Der externe Tank – enthält den Treibstoff für den Hauptantrieb.
    private ShuttlePart externalTank;

    public ShuttlePart getOrbiter()      { return orbiter; }
    public ShuttlePart getSrb()          { return srb; }
    public ShuttlePart getExternalTank() { return externalTank; }

    public void setOrbiter(ShuttlePart orbiter)           { this.orbiter = orbiter; }
    public void setSrb(ShuttlePart srb)                   { this.srb = srb; }
    public void setExternalTank(ShuttlePart externalTank) { this.externalTank = externalTank; }
}
