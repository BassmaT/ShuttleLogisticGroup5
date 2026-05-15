package com.group5.shuttle.service;

import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;
import com.group5.shuttle.model.SensorStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for: sensor data, thresholds, sensor evaluation.
 * Singleton – instantiated once per session.
 */
public class SensorDataService implements ISensorDataService {

    private static final class Holder {
        static final SensorDataService INSTANCE = new SensorDataService();
    }

    private SensorDataService() {}

    public static SensorDataService getInstance() {
        return Holder.INSTANCE;
    }

    // ── Sensordaten ───────────────────────────────────────────────────────────

    public ShuttleData loadSensorData() {
        ShuttleData data = new ShuttleData();

        data.setOrbiter(new com.group5.shuttle.model.ShuttlePart());
        data.getOrbiter().setSensor("hullTemperature", 520.0);
        data.getOrbiter().setSensor("cabinPressure",   101.0);
        data.getOrbiter().setSensor("oxygenLevel",      96.0);
        data.getOrbiter().setSensor("coolantPressure",   2.8);

        data.setSrb(new com.group5.shuttle.model.ShuttlePart());
        data.getSrb().setSensor("thrust",            1800.0);
        data.getSrb().setSensor("casingTemperature",  797.0);
        data.getSrb().setSensor("vibration",            0.22);

        data.setExternalTank(new com.group5.shuttle.model.ShuttlePart());
        data.getExternalTank().setSensor("fuelTemperature", -150.0);
        data.getExternalTank().setSensor("fuelPressure",       4.8);
        data.getExternalTank().setSensor("stress",            22.0);

        return data;
    }

    // ── Schwellwerte ──────────────────────────────────────────────────────────

    public Map<String, Map<String, SensorThreshold>> loadThresholds() {
        Map<String, Map<String, SensorThreshold>> result = new HashMap<>();

        Map<String, SensorThreshold> orbiter = new HashMap<>();
        orbiter.put("hullTemperature", threshold(null,  650.0, 5.0));
        orbiter.put("cabinPressure",   threshold(95.0,  110.0, 2.0));
        orbiter.put("oxygenLevel",     threshold(90.0,  null,  1.5));
        orbiter.put("coolantPressure", threshold(3.5,   null,  0.3));
        result.put("orbiter", orbiter);

        Map<String, SensorThreshold> srb = new HashMap<>();
        srb.put("thrust",            threshold(1500.0, null,  50.0));
        srb.put("casingTemperature", threshold(null,   800.0,  3.0));
        srb.put("vibration",         threshold(null,     0.40, 0.03));
        result.put("srb", srb);

        Map<String, SensorThreshold> tank = new HashMap<>();
        tank.put("fuelTemperature", threshold(-170.0, -120.0, 3.0));
        tank.put("fuelPressure",    threshold(4.0,    null,   0.2));
        tank.put("stress",          threshold(null,   20.0,   1.5));
        result.put("externalTank", tank);

        return result;
    }

    private SensorThreshold threshold(Double min, Double max, double buf) {
        return new SensorThreshold(min, max, buf);
    }

    // ── Sensorauswertung ──────────────────────────────────────────────────────

    public SensorStatus evaluate(double value, SensorThreshold t)  {
        double buf = t.getWarningBuffer();
        if (t.getMin() != null && value < t.getMin())       return SensorStatus.REPLACE;
        if (t.getMax() != null && value > t.getMax())       return SensorStatus.REPLACE;
        if (t.getMin() != null && value < t.getMin() + buf) return SensorStatus.WARNING;
        if (t.getMax() != null && value > t.getMax() - buf) return SensorStatus.WARNING;
        return SensorStatus.OK;
    }
}
