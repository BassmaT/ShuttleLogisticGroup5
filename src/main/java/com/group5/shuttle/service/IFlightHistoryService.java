package com.group5.shuttle.service;

import com.group5.shuttle.model.FlightHistory;

// DIP: PredictiveAnalysisService depends on this abstraction, not on FlightHistoryService directly.
public interface IFlightHistoryService {
    FlightHistory loadFlightHistory();
}
