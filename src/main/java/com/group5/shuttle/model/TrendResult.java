package com.group5.shuttle.model;

// Ergebnis der Trendanalyse für einen einzelnen Sensor über die letzten 5 Flüge
public class TrendResult {

    // Schlüssel des Shuttle-Teils, z. B. "srb"
    private final String partKey;

    // Name des Sensors, z. B. "casingTemperature"
    private final String sensorName;

    // Durchschnittliche Änderung pro Flug (positiv = steigend, negativ = fallend)
    private final double trendPerFlight;

    // Richtung: "RISING" (steigend), "FALLING" (fallend) oder "STABLE" (stabil)
    private final String direction;

    // Lesbarer Empfehlungstext für die Anzeige im Dashboard
    private final String recommendation;

    // Geschätzte Anzahl Flüge bis zum Erreichen des Grenzwerts (-1 = nicht berechenbar)
    private final int flightsUntilLimit;

    // Konstruktor – setzt alle Felder auf einmal
    public TrendResult(String partKey, String sensorName, double trendPerFlight,
                       String direction, String recommendation, int flightsUntilLimit) {
        this.partKey          = partKey;
        this.sensorName       = sensorName;
        this.trendPerFlight   = trendPerFlight;
        this.direction        = direction;
        this.recommendation   = recommendation;
        this.flightsUntilLimit = flightsUntilLimit;
    }

    // Getter-Methoden
    public String getPartKey()         { return partKey; }
    public String getSensorName()      { return sensorName; }
    public double getTrendPerFlight()  { return trendPerFlight; }
    public String getDirection()       { return direction; }
    public String getRecommendation()  { return recommendation; }
    public int getFlightsUntilLimit()  { return flightsUntilLimit; }
}
