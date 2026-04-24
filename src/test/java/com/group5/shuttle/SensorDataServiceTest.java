package com.group5.shuttle;

import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.service.SensorDataService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SensorDataServiceTest {

    private final SensorDataService svc = SensorDataService.getInstance();

    // ── REPLACE (Grenzwert überschritten) ─────────────────────────────────────

    @Test
    void evaluate_belowMin_returnsReplace() {
        SensorThreshold t = new SensorThreshold(3.5, null);
        assertEquals("REPLACE", svc.evaluate(3.0, t));
    }

    @Test
    void evaluate_aboveMax_returnsReplace() {
        SensorThreshold t = new SensorThreshold(null, 800.0);
        assertEquals("REPLACE", svc.evaluate(850.0, t));
    }

    @Test
    void evaluate_exactlyAtMin_returnsWarning() {
        // value == min ist nicht strikt unter min → kein REPLACE, aber < min+5 → WARNING
        SensorThreshold t = new SensorThreshold(3.5, null);
        assertEquals("WARNING", svc.evaluate(3.5, t));
    }

    // ── WARNING (nahe am Grenzwert) ───────────────────────────────────────────

    @Test
    void evaluate_nearMin_returnsWarning() {
        // min=3.5 → Warnung wenn < 3.5+5=8.5
        SensorThreshold t = new SensorThreshold(3.5, null);
        assertEquals("WARNING", svc.evaluate(5.0, t));
    }

    @Test
    void evaluate_nearMax_returnsWarning() {
        // max=800 → Warnung wenn > 800-5=795
        SensorThreshold t = new SensorThreshold(null, 800.0);
        assertEquals("WARNING", svc.evaluate(798.0, t));
    }

    // ── OK (innerhalb des sicheren Bereichs) ──────────────────────────────────

    @Test
    void evaluate_withinBounds_returnsOk() {
        SensorThreshold t = new SensorThreshold(3.5, 800.0);
        assertEquals("OK", svc.evaluate(100.0, t));
    }

    @Test
    void evaluate_noThresholds_returnsOk() {
        SensorThreshold t = new SensorThreshold(null, null);
        assertEquals("OK", svc.evaluate(9999.0, t));
    }

    @Test
    void evaluate_exactlyAtSafeMin_returnsOk() {
        // Genau 5 über dem Minimum → kein Warning mehr
        SensorThreshold t = new SensorThreshold(3.5, null);
        assertEquals("OK", svc.evaluate(8.5, t)); // 8.5 ist nicht < 8.5
    }
}
