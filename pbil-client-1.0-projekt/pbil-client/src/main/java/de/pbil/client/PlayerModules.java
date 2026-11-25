package de.pbil.client;

import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.client.gui.screens.DeathScreen;

final class PlayerModules {
    static void register(List<Module> list) {
        list.add(new AutoRespawn());
        list.add(new AutoEat());
        list.add(new AntiAfk());
    }

    static final class AutoRespawn extends Module {
        AutoRespawn() {
            super("AutoRespawn", Category.PLAYER, "Klickt nach dem Tod sofort auf Respawn");
        }

        @Override
        public void onTick() {
            if (mc.screen instanceof DeathScreen) {
                mc.player.respawn();
            }
        }
    }

    static final class AutoEat extends Module {
        private final Setting hunger = num("Essen ab", 14, 2, 19, 1);
        private final Setting gapples = bool("Goldaepfel", false);
        private boolean eating;
        private int oldSlot = -1;

        AutoEat() {
            super("AutoEat", Category.PLAYER, "Isst Essen aus der Hotbar bei Hunger");
        }

        private boolean good(ItemStack s) {
            if (s.isEmpty() || !s.has(DataComponents.FOOD)) {
                return false;
            }
            if (s.is(Items.ROTTEN_FLESH) || s.is(Items.SPIDER_EYE) || s.is(Items.POISONOUS_POTATO)
                || s.is(Items.PUFFERFISH) || s.is(Items.CHORUS_FRUIT)) {
                return false;
            }
            return gapples.on() || (!s.is(Items.GOLDEN_APPLE) && !s.is(Items.ENCHANTED_GOLDEN_APPLE));
        }

        @Override
        public void onTick() {
            int food = mc.player.getFoodData().getFoodLevel();
            if (eating) {
                if (food >= 20 || !mc.player.isUsingItem() && food > hunger.get()) {
                    stop();
                }
                return;
            }
            if (food > hunger.get() || mc.screen != null) {
                return;
            }
            for (int i = 0; i < 9; i++) {
                if (good(mc.player.getInventory().getItem(i))) {
                    oldSlot = mc.player.getInventory().getSelectedSlot();
                    mc.player.getInventory().setSelectedSlot(i);
                    mc.options.keyUse.setDown(true);
                    eating = true;
                    return;
                }
            }
        }

        private void stop() {
            mc.options.keyUse.setDown(false);
            if (oldSlot >= 0) {
                mc.player.getInventory().setSelectedSlot(oldSlot);
            }
            oldSlot = -1;
            eating = false;
        }

        @Override
        public void onDisable() {
            if (eating && mc.player != null) {
                stop();
            }
        }
    }

    static final class AntiAfk extends Module {
        private int ticks;

        AntiAfk() {
            super("AntiAFK", Category.PLAYER, "Springt kurz alle 15 Sekunden, damit du nicht gekickt wirst");
        }

        @Override
        public void onTick() {
            ticks++;
            mc.options.keyJump.setDown(ticks % 300 < 2);
        }

        @Override
        public void onDisable() {
            mc.options.keyJump.setDown(false);
        }
    }

    private PlayerModules() {
    }
}
