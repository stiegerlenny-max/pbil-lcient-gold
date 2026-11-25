package de.pbil.client;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    HUD("HUD");

    public final String label;

    Category(String label) {
        this.label = label;
    }
}
