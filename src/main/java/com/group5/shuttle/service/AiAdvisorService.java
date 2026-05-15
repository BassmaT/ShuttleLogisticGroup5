package com.group5.shuttle.service;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AiAdvisorService {

    private AiAdvisorService() {}

    // OCP: new response → add an entry. getResponse() remains unchanged.
    private static final Map<String, String> EXACT_RESPONSES = new LinkedHashMap<>();
    private static final Map<String, String> KEYWORD_RESPONSES = new LinkedHashMap<>();

    static {
        EXACT_RESPONSES.put("who works on the orbiter?",
            "AI: Max Müller (Technician, Team Alpha) is assigned to the Orbiter.\n"
            + "Security Chief: Lena Fischer (Team Alpha).\n"
            + "Routine tasks: Clean Cabin, Check Fire Suppression, Inspect Landing Gear.");
        EXACT_RESPONSES.put("what is the inventory status?",
            "AI: Inventory status:\n"
            + "• Heat Shield Panel (Orbiter) – OUT OF STOCK\n"
            + "• Cryogenic Seal (External Tank) – OUT OF STOCK\n"
            + "• Vibration Damper (SRB) – LOW (1 unit)\n"
            + "Recommendation: Immediate reorder via Logistics → Order.");
        EXACT_RESPONSES.put("are there any current warnings?",
            "AI: 3 critical sensor values detected:\n"
            + "[REPLACE] coolantPressure (Orbiter): 2.8 – Limit: min 3.5\n"
            + "[REPLACE] stress (External Tank): 22 – Limit: max 20\n"
            + "[WARNING] casingTemperature (SRB): 797 – Limit: max 800\n"
            + "Immediate inspection by the responsible technician recommended.");

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
