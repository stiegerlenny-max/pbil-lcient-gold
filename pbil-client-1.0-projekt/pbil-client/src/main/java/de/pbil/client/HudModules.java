package de.pbil.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class HudModules {
    static void register(List<Module> list) {
        list.add(new Watermark());
        list.add(new ModuleList());
        list.add(new Coordinates());
        list.add(new ArmorHud());
        list.add(new PvpInfo());
        list.add(new SpeedHud());
    }

    static void text(GuiGraphics g, String s, int x, int y, int color) {
        g.drawString(Util.mc().font, s, x, y, color, true);
    }

    static final class Watermark extends Module {
        Watermark() {
            super("Watermark", Category.HUD, "Zeigt \"Pbil Client\" oben links");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            text(g, "Pbil Client", 4, 4, ClickGui.ACCENT);
        }
    }

    static final class ModuleList extends Module {
        ModuleList() {
            super("ModuleList", Category.HUD, "Liste der aktiven Module oben rechts");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            List<String> names = new ArrayList<>();
            for (Module m : Modules.all()) {
                if (m.enabled && m.category != Category.HUD) {
                    names.add(m.name);
                }
            }
            names.sort(Comparator.comparingInt((String s) -> -mc.font.width(s)));
            int sw = mc.getWindow().getGuiScaledWidth();
            int y = 4;
            for (String n : names) {
                int w = mc.font.width(n);
                g.fill(sw - w - 7, y - 1, sw - 3, y + 9, 0x80000000);
                g.fill(sw - 3, y - 1, sw - 1, y + 9, ClickGui.ACCENT);
                text(g, n, sw - w - 5, y, 0xFFFFFFFF);
                y += 10;
            }
        }
    }

    static final class Coordinates extends Module {
        Coordinates() {
            super("Coordinates", Category.HUD, "Koordinaten und Blickrichtung unten rechts");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            String s = String.format(java.util.Locale.ROOT, "XYZ %.1f / %.1f / %.1f  %s",
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getDirection().getName());
            int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
            text(g, s, sw - mc.font.width(s) - 4, sh - 12, 0xFFFFFFFF);
        }
    }

    static final class ArmorHud extends Module {
        ArmorHud() {
            super("ArmorHUD", Category.HUD, "Ruestung mit Haltbarkeit links in der Mitte");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            int y = mc.getWindow().getGuiScaledHeight() / 2 - 38;
            for (EquipmentSlot s : slots) {
                ItemStack st = mc.player.getItemBySlot(s);
                if (!st.isEmpty()) {
                    g.renderItem(st, 4, y);
                    if (st.getMaxDamage() > 0) {
                        int pct = 100 * (st.getMaxDamage() - st.getDamageValue()) / st.getMaxDamage();
                        int c = pct > 60 ? 0xFF3CE05A : pct > 30 ? 0xFFFFC832 : 0xFFFF3C3C;
                        text(g, pct + "%", 24, y + 4, c);
                    }
                }
                y += 20;
            }
        }
    }

    static final class PvpInfo extends Module {
        PvpInfo() {
            super("PvPInfo", Category.HUD, "Anzahl Totems, Crystals, Obsidian und Goldaepfel");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            int y = 16;
            String[] names = {"Totems", "Crystals", "Obsidian", "Gapples"};
            int[] counts = {Util.count(Items.TOTEM_OF_UNDYING), Util.count(Items.END_CRYSTAL),
                Util.count(Items.OBSIDIAN), Util.count(Items.GOLDEN_APPLE) + Util.count(Items.ENCHANTED_GOLDEN_APPLE)};
            for (int i = 0; i < names.length; i++) {
                text(g, names[i] + ": " + counts[i], 4, y, counts[i] == 0 ? 0xFFFF6B6B : 0xFFFFFFFF);
                y += 10;
            }
        }
    }

    static final class SpeedHud extends Module {
        private double lastX, lastZ;
        private double bps;

        SpeedHud() {
            super("Speed (BPS)", Category.HUD, "Geschwindigkeit in Bloecken pro Sekunde");
        }

        @Override
        public void onTick() {
            double dx = mc.player.getX() - lastX, dz = mc.player.getZ() - lastZ;
            bps = bps * 0.7 + Math.sqrt(dx * dx + dz * dz) * 20 * 0.3;
            lastX = mc.player.getX();
            lastZ = mc.player.getZ();
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            String s = String.format(java.util.Locale.ROOT, "%.1f b/s", bps);
            int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
            text(g, s, sw - mc.font.width(s) - 4, sh - 22, 0xFFC4C9D2);
        }
    }

    private HudModules() {
    }
}
