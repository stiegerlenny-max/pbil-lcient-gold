package de.pbil.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Liste aller Module + Tick/Render-Verteilung. Ein Modul, das einen Fehler wirft, wird abgeschaltet statt das Spiel zu beenden. */
public final class Modules {
    private static final List<Module> ALL = new ArrayList<>();

    public static void init() {
        CombatModules.register(ALL);
        MovementModules.register(ALL);
        PlayerModules.register(ALL);
        RenderModules.register(ALL);
        HudModules.register(ALL);
        Config.load();
    }

    public static List<Module> all() {
        return ALL;
    }

    public static List<Module> in(Category c) {
        List<Module> l = new ArrayList<>();
        for (Module m : ALL) {
            if (m.category == c) {
                l.add(m);
            }
        }
        return l;
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        for (Module m : ALL) {
            if (m.enabled) {
                try {
                    m.onTick();
                } catch (Throwable t) {
                    fail(m, t);
                }
            }
        }
    }

    public static void render(GuiGraphics g, float partial) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) {
            return;
        }
        for (Module m : ALL) {
            if (m.enabled) {
                try {
                    m.onRender(g, partial);
                } catch (Throwable t) {
                    fail(m, t);
                }
            }
        }
    }

    private static void fail(Module m, Throwable t) {
        PbilClient.LOGGER.error("Modul " + m.name + " wurde wegen eines Fehlers abgeschaltet", t);
        m.enabled = false;
        try {
            m.onDisable();
        } catch (Throwable ignored) {
            // nichts weiter tun
        }
    }

    private Modules() {
    }
}
