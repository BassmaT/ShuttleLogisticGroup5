package com.group5.shuttle.service;

import com.group5.shuttle.model.TrendResult;

import java.util.List;
import java.util.Map;

// DIP: PredictivePanelController und MainController hängen von dieser Abstraktion ab.
public interface IPredictiveAnalysisService {
    Map<String, List<TrendResult>> analyzeAll();
}
