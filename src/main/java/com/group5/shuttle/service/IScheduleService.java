package com.group5.shuttle.service;

import com.group5.shuttle.model.TakeoverSchedule;

// Abstraktion für den Schedule-Service (D – Dependency Inversion).
public interface IScheduleService {
    TakeoverSchedule loadSchedule();
}
