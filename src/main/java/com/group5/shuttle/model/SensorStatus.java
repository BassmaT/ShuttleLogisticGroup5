package com.group5.shuttle.model;

public enum SensorStatus {
    OK     ("#66ff66"),
    WARNING("#ffcc00"),
    REPLACE("#ff4444");

    private final String color;
    SensorStatus(String color) { this.color = color; }
    public String getColor()   { return color; }
}
