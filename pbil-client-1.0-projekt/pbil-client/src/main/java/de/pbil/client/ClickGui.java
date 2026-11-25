package de.pbil.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Hauptmenue: pro Kategorie ein Panel, Klick auf ein Modul schaltet es um, ">" oeffnet die Einstellungen. */
public class ClickGui extends Screen {
    static final int ACCENT = 0xFF3B82F6;
    private static final int ROW = 16;
    private static final int GAP = 6;
    private static final int TOP = 20;

    private int panelW;
    private int x0;

    public ClickGui() {
        super(Component.literal("Pbil Client"));
    }

    static Button hit(int x, int y, int w, int h, Runnable action) {
        Button b = Button.builder(Component.empty(), btn -> action.run()).bounds(x, y, w, h).build();
        b.setAlpha(0f);
        return b;
    }

    @Override
    protected void init() {
        Category[] cats = Category.values();
        panelW = Math.max(60, Math.min(112, (this.width - 12) / cats.length - GAP));
        int total = cats.length * (panelW + GAP) - GAP;
        x0 = (this.width - total) / 2;
        for (int i = 0; i < cats.length; i++) {
            int x = x0 + i * (panelW + GAP);
            int y = TOP + ROW + 2;
            for (Module m : Modules.in(cats[i])) {
                final Module mod = m;
                int w = m.settings.isEmpty() ? panelW : panelW - 14;
                this.addRenderableWidget(hit(x, y, w, ROW, mod::toggle));
                if (!m.settings.isEmpty()) {
                    this.addRenderableWidget(hit(x + panelW - 14, y, 14, ROW,
                        () -> this.minecraft.setScreen(new SettingsScreen(this, mod))));
                }
                y += ROW;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        Category[] cats = Category.values();
        String hint = "Linksklick: an/aus   >: Einstellungen   ESC: schliessen";
        g.drawString(this.font, hint, (this.width - this.font.width(hint)) / 2, 6, 0xFF9AA0AA, false);
        String tip = null;
        for (int i = 0; i < cats.length; i++) {
            int x = x0 + i * (panelW + GAP);
            int y = TOP;
            g.fill(x, y, x + panelW, y + ROW, 0xF0101828);
            g.fill(x, y + ROW - 1, x + panelW, y + ROW, ACCENT);
            g.drawString(this.font, cats[i].label, x + 5, y + 4, 0xFFFFFFFF, true);
            y += ROW + 2;
            for (Module m : Modules.in(cats[i])) {
                boolean hover = mouseX >= x && mouseX < x + panelW && mouseY >= y && mouseY < y + ROW;
                int bg = m.enabled ? ACCENT : 0xC0141820;
                if (hover) {
                    bg = m.enabled ? 0xFF5B9BFF : 0xE0262C38;
                    tip = m.desc;
                }
                g.fill(x, y, x + panelW, y + ROW, bg);
                g.drawString(this.font, m.name, x + 5, y + 4, m.enabled ? 0xFFFFFFFF : 0xFFC4C9D2, false);
                if (!m.settings.isEmpty()) {
                    g.drawString(this.font, ">", x + panelW - 10, y + 4, 0xFFFFFFFF, false);
                }
                y += ROW;
            }
        }
        if (tip != null) {
            int w = this.font.width(tip);
            int tx = Math.min(mouseX + 8, this.width - w - 6);
            g.fill(tx - 3, mouseY + 8, tx + w + 3, mouseY + 20, 0xF0000000);
            g.drawString(this.font, tip, tx, mouseY + 10, 0xFFFFFFFF, false);
        }
    }
}
