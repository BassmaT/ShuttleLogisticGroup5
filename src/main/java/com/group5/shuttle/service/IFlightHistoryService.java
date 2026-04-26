package com.group5.shuttle.service;

import com.group5.shuttle.model.FlightHistory;

// DIP: PredictiveAnalysisService hängt von dieser Abstraktion ab, nicht von FlightHistoryService direkt.
public interface IFlightHistoryService {
    FlightHistory loadFlightHistory();
}
