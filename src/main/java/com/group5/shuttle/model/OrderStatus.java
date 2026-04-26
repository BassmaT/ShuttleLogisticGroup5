package com.group5.shuttle.model;

public enum OrderStatus {
    PENDING_APPROVAL("#aaaaaa"),
    APPROVED        ("#ffcc00"),
    REJECTED        ("#ff4444"),
    ORDERED         ("#66aaff"),
    DELIVERED       ("#66ff66");

    private final String color;
    OrderStatus(String color) { this.color = color; }
    public String getColor()  { return color; }
}
