package com.group5.shuttle.model;

public enum StockStatus {
    // OCP: neuer Status → nur hier eintragen. Felder: label, color, showsButton, skipsStatusInit
    NONE        ("–",              "#555555", false, false),
    IN_STOCK    ("✓ In Stock",     "#66ff66", false, false),
    LOW         ("▲ Low",          "#ffcc00", false, false),
    OUT_OF_STOCK("✗ Out of Stock", "#ff4444", true,  false),
    ORDERED     ("⏳ Ordered...",  "#ffcc00", false, true),
    ARRIVED     ("✓ Arrived",      "#66ff66", false, true);

    private final String  label;
    private final String  color;
    private final boolean showsButton;
    private final boolean skipsInit;

    StockStatus(String label, String color, boolean showsButton, boolean skipsInit) {
        this.label       = label;
        this.color       = color;
        this.showsButton = showsButton;
        this.skipsInit   = skipsInit;
    }

    public String  getLabel()          { return label; }
    public String  getColor()          { return color; }
    public boolean showsButton()       { return showsButton; }
    /** True → RepairInventoryService skips this status during initialization. */
    public boolean skipsStatusInit()   { return skipsInit; }
}
