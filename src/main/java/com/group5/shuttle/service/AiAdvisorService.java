package com.group5.shuttle.service;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AiAdvisorService {

    private AiAdvisorService() {}

    // OCP: neue Antwort → Eintrag ergänzen. getResponse() bleibt unverändert.
    private static final Map<String, String> EXACT_RESPONSES = new LinkedHashMap<>();
    private static final Map<String, String> KEYWORD_RESPONSES = new LinkedHashMap<>();

    static {
        EXACT_RESPONSES.put("wer arbeitet am orbiter?",
            "AI: Max Müller (Technician, Team Alpha) ist für den Orbiter eingeplant.\n"
            + "Security Chief: Lena Fischer (Team Alpha).\n"
            + "Routine-Aufgaben: Clean Cabin, Check Fire Suppression, Inspect Landing Gear.");
        EXACT_RESPONSES.put("wie ist der lagerstand?",
            "AI: Lagerstatus:\n"
            + "• Heat Shield Panel (Orbiter) – OUT OF STOCK\n"
            + "• Cryogenic Seal (External Tank) – OUT OF STOCK\n"
            + "• Vibration Damper (SRB) – LOW (1 Stück)\n"
            + "Empfehlung: Sofortige Nachbestellung über Logistics → Order.");
        EXACT_RESPONSES.put("gibt es aktuelle warnungen?",
            "AI: 3 kritische Sensorwerte erkannt:\n"
            + "[REPLACE] coolantPressure (Orbiter): 2.8 – Limit: min 3.5\n"
            + "[REPLACE] stress (External Tank): 22 – Limit: max 20\n"
            + "[WARNING] casingTemperature (SRB): 797 – Limit: max 800\n"
            + "Sofortige Inspektion durch zuständigen Techniker empfohlen.");

        KEYWORD_RESPONSES.put("cost",   "AI: Analyzing database... Current delay costs are 1.2M € per day.");
        KEYWORD_RESPONSES.put("status", "AI: All shuttle systems are currently within nominal parameters.");
    }

    public static String getResponse(String message) {
        String msg = message.toLowerCase().trim();

        String exact = EXACT_RESPONSES.get(msg);
        if (exact != null) return exact;

        for (var entry : KEYWORD_RESPONSES.entrySet())
            if (msg.contains(entry.getKey())) return entry.getValue();

        return "AI: Telemetry analysis in progress for: " + message;
    }
}
