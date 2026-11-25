package de.pbil.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbilClient implements ClientModInitializer {
    public static final String ID = "pbil_client";
    public static final Logger LOGGER = LoggerFactory.getLogger("Pbil Client");

    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        Modules.init();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ID, "main"));
        openKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.pbil_client.gui", InputConstants.Type.KEYSYM, InputConstants.KEY_RSHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ClickGui());
                }
            }
            Modules.tick();
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath(ID, "overlay"),
            (graphics, tickCounter) -> Modules.render(graphics, tickCounter.getGameTimeDeltaPartialTick(false)));
    }
}
