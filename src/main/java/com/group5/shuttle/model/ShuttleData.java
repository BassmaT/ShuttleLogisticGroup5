package com.group5.shuttle.model;

// Dieses Datenmodell stellt den gesamten Shuttle dar.
// Jackson (die JSON-Bibliothek) liest die sensors.json-Datei ein
// und füllt automatisch die drei Felder unten.
public class ShuttleData {

    // Der Orbiter – das eigentliche Raumschiff (Rumpf, Kabine, Triebwerke).
    public ShuttlePart orbiter;

    // Die Feststoffraketen (Solid Rocket Boosters) – starten den Shuttle.
    public ShuttlePart srb;

    // Der externe Tank – enthält den Treibstoff für den Hauptantrieb.
    public ShuttlePart externalTank;
}
