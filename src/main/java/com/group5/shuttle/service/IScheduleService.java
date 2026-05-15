package com.group5.shuttle.service;

import com.group5.shuttle.model.TakeoverSchedule;

// Abstraction for the schedule service (D – Dependency Inversion).
public interface IScheduleService {
    TakeoverSchedule loadSchedule();
}
