package de.pbil.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Einstellungen eines Moduls: Schalter anklicken, Zahlen mit - und + aendern. */
public class SettingsScreen extends Screen {
    private static final int ROW = 22;

    private final Screen parent;
    private final Module module;
    private int panelW;
    private int panelX;
    private int panelY;

    public SettingsScreen(Screen parent, Module module) {
        super(Component.literal(module.name));
        this.parent = parent;
        this.module = module;
    }

    @Override
    protected void init() {
        panelW = Math.min(240, this.width - 20);
        panelX = (this.width - panelW) / 2;
        panelY = Math.max(10, (this.height - (module.settings.size() * ROW + 70)) / 2);
        int y = panelY + 34;
        for (Setting s : module.settings) {
            final Setting st = s;
            if (s.isBool) {
                this.addRenderableWidget(Button.builder(Component.literal(s.display()), b -> {
                    st.change(1);
                    Config.save();
                    this.rebuildWidgets();
                }).bounds(panelX + panelW - 70, y, 60, 18).build());
            } else {
                this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
                    st.change(-1);
                    Config.save();
                }).bounds(panelX + panelW - 100, y, 18, 18).build());
                this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
                    st.change(1);
                    Config.save();
                }).bounds(panelX + panelW - 28, y, 18, 18).build());
            }
            y += ROW;
        }
        this.addRenderableWidget(Button.builder(Component.literal("Zurück"), b -> this.onClose())
            .bounds(panelX + panelW / 2 - 50, y + 6, 100, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        int h = module.settings.size() * ROW + 70;
        g.fill(panelX - 4, panelY - 4, panelX + panelW + 4, panelY + h, 0xE0101828);
        g.fill(panelX - 4, panelY + 22, panelX + panelW + 4, panelY + 23, ClickGui.ACCENT);
        g.drawString(this.font, module.name, panelX + 4, panelY + 6, 0xFFFFFFFF, true);
        int y = panelY + 34;
        for (Setting s : module.settings) {
            g.drawString(this.font, s.name, panelX + 6, y + 5, 0xFFC4C9D2, false);
            if (!s.isBool) {
                String v = s.display();
                g.drawString(this.font, v, panelX + panelW - 64 + (36 - this.font.width(v)) / 2, y + 5, 0xFFFFFFFF, false);
            }
            y += ROW;
        }
    }
}
