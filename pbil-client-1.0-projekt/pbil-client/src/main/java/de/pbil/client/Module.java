package de.pbil.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public abstract class Module {
    protected static final Minecraft mc = Minecraft.getInstance();

    public final String name;
    public final String desc;
    public final Category category;
    public boolean enabled;
    public final List<Setting> settings = new ArrayList<>();

    protected Module(String name, Category category, String desc) {
        this.name = name;
        this.category = category;
        this.desc = desc;
    }

    protected Setting bool(String n, boolean def) {
        Setting s = Setting.bool(n, def);
        settings.add(s);
        return s;
    }

    protected Setting num(String n, double def, double min, double max, double step) {
        Setting s = Setting.num(n, def, min, max, step);
        settings.add(s);
        return s;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onTick() {
    }

    /** Wird jedes Bild im HUD-Layer aufgerufen (Overlay-Zeichnen). */
    public void onRender(GuiGraphics g, float partial) {
    }

    public void setEnabled(boolean on) {
        if (on == enabled) {
            return;
        }
        enabled = on;
        try {
            if (on) {
                onEnable();
            } else {
                onDisable();
            }
        } catch (Throwable t) {
            PbilClient.LOGGER.error("Fehler in Modul " + name, t);
        }
        Config.save();
    }

    public void toggle() {
        setEnabled(!enabled);
    }
}
