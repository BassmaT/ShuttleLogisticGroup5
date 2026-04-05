package com.group5.shuttle.model;

// Dieses Datenmodell speichert die Grenzwerte für einen einzelnen Sensor.
// Es gibt einen Mindestwert (min) und einen Höchstwert (max).
// Wenn ein Sensorwert außerhalb dieser Grenzen liegt, wird eine Warnung ausgelöst.
public class SensorThreshold {

    // Untere Grenze – liegt der Sensorwert darunter, ist etwas nicht in Ordnung.
    // Double (groß) statt double (klein), damit der Wert auch leer (null) sein kann.
    public Double min;

    // Obere Grenze – liegt der Sensorwert darüber, ist etwas nicht in Ordnung.
    public Double max;
}
