package de.pbil.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

final class RenderModules {
    static void register(List<Module> list) {
        list.add(new PlayerEsp());
        list.add(new MobEsp());
        list.add(new ItemEsp());
        list.add(new CrystalEsp());
        list.add(new StorageEsp());
        list.add(new SpawnerEsp());
        list.add(new BlockEsp());
        list.add(new Tracers());
        list.add(new Nametags());
        list.add(new Fullbright());
        list.add(new Zoom());
    }

    /** Kasten um jede passende Entity, optional mit Lebensbalken. */
    static void entityBoxes(GuiGraphics g, float pt, Predicate<Entity> filter, int color, boolean health, int max) {
        Proj pr = Draw.proj(pt);
        if (pr == null) {
            return;
        }
        int n = 0;
        for (Entity e : Util.entities(filter)) {
            if (n++ >= max) {
                break;
            }
            double[] r = Draw.box(g, pr, Util.smoothBox(e, pt), color);
            if (r != null && health && e instanceof LivingEntity le) {
                double frac = Math.max(0, Math.min(1, le.getHealth() / Math.max(1f, le.getMaxHealth())));
                int hc = frac > 0.6 ? 0xFF3CE05A : frac > 0.3 ? 0xFFFFC832 : 0xFFFF3C3C;
                int x = (int) r[0] - 4;
                int top = (int) r[1], bottom = (int) r[3];
                g.fill(x, top, x + 2, bottom, 0xA0000000);
                g.fill(x, bottom - (int) ((bottom - top) * frac), x + 2, bottom, hc);
            }
        }
    }

    static final class PlayerEsp extends Module {
        private final Setting health = bool("Lebensbalken", true);

        PlayerEsp() {
            super("PlayerESP", Category.RENDER, "Kasten um alle Spieler");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            entityBoxes(g, pt, e -> e instanceof Player && e.isAlive(), 0xFF3BD1FF, health.on(), 60);
        }
    }

    static final class MobEsp extends Module {
        private final Setting hostile = bool("Nur Monster", false);

        MobEsp() {
            super("MobESP", Category.RENDER, "Kasten um Mobs (rot = Monster, gruen = friedlich)");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            entityBoxes(g, pt, e -> e instanceof Monster && e.isAlive(), 0xFFFF4B4B, false, 40);
            if (!hostile.on()) {
                entityBoxes(g, pt, e -> e instanceof Mob && !(e instanceof Monster) && e.isAlive(), 0xFF5BE06A, false, 40);
            }
        }
    }

    static final class ItemEsp extends Module {
        ItemEsp() {
            super("ItemESP", Category.RENDER, "Kasten um herumliegende Items");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            entityBoxes(g, pt, e -> e instanceof ItemEntity, 0xFFFFE04B, false, 60);
        }
    }

    static final class CrystalEsp extends Module {
        CrystalEsp() {
            super("CrystalESP", Category.RENDER, "Kasten um End Crystals");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            entityBoxes(g, pt, e -> e instanceof EndCrystal, 0xFFFF5BF0, false, 40);
        }
    }

    /** Gemeinsame Logik: alle paar Ticks Bloecke suchen, jedes Bild einrahmen. */
    abstract static class BlockScanner extends Module {
        final Setting radius = num("Radius", 20, 8, 40, 2);
        private List<BlockPos> found = new ArrayList<>();
        private int ticks;

        BlockScanner(String name, String desc) {
            super(name, Category.RENDER, desc);
        }

        abstract boolean matches(BlockState s);

        abstract int color(BlockState s);

        @Override
        public void onTick() {
            if (ticks++ % 15 == 0) {
                int r = radius.asInt();
                found = Util.scan(r, Math.min(r, 16), this::matches, 250);
            }
        }

        @Override
        public void onDisable() {
            found = new ArrayList<>();
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            Proj pr = Draw.proj(pt);
            if (pr == null) {
                return;
            }
            for (BlockPos p : found) {
                BlockState s = mc.level.getBlockState(p);
                if (!matches(s)) {
                    continue;
                }
                Draw.box(g, pr, new AABB(p), color(s));
            }
        }
    }

    static final class StorageEsp extends BlockScanner {
        StorageEsp() {
            super("StorageESP", "Kasten um Kisten, Fasser, Shulker, Ender Chests, Trichter und Ofen");
        }

        @Override
        boolean matches(BlockState s) {
            Block b = s.getBlock();
            return b instanceof ChestBlock || b instanceof BarrelBlock || b instanceof ShulkerBoxBlock
                || b instanceof EnderChestBlock || b instanceof HopperBlock || b instanceof AbstractFurnaceBlock
                || b instanceof DispenserBlock;
        }

        @Override
        int color(BlockState s) {
            Block b = s.getBlock();
            if (b instanceof EnderChestBlock) {
                return 0xFFB45BFF;
            }
            if (b instanceof ShulkerBoxBlock) {
                return 0xFFFF7BE0;
            }
            if (b instanceof ChestBlock) {
                return 0xFFFFB347;
            }
            return 0xFF8FD0FF;
        }
    }

    static final class SpawnerEsp extends BlockScanner {
        SpawnerEsp() {
            super("SpawnerESP", "Kasten um Spawner");
        }

        @Override
        boolean matches(BlockState s) {
            return s.is(Blocks.SPAWNER);
        }

        @Override
        int color(BlockState s) {
            return 0xFFFF3B3B;
        }
    }

    static final class BlockEsp extends BlockScanner {
        private final Setting diamond = bool("Diamant", true);
        private final Setting debris = bool("Netherit", true);
        private final Setting gold = bool("Gold", false);
        private final Setting emerald = bool("Smaragd", false);
        private final Setting ender = bool("Ender-Portal", false);

        BlockEsp() {
            super("BlockESP", "Kasten um wertvolle Bloecke (Auswahl in den Einstellungen)");
        }

        @Override
        boolean matches(BlockState s) {
            return (diamond.on() && s.is(BlockTags.DIAMOND_ORES)) || (debris.on() && s.is(Blocks.ANCIENT_DEBRIS))
                || (gold.on() && s.is(BlockTags.GOLD_ORES)) || (emerald.on() && s.is(BlockTags.EMERALD_ORES))
                || (ender.on() && (s.is(Blocks.END_PORTAL_FRAME) || s.is(Blocks.END_PORTAL)));
        }

        @Override
        int color(BlockState s) {
            if (s.is(BlockTags.DIAMOND_ORES)) {
                return 0xFF4BE3FF;
            }
            if (s.is(Blocks.ANCIENT_DEBRIS)) {
                return 0xFFB5651D;
            }
            if (s.is(BlockTags.GOLD_ORES)) {
                return 0xFFFFD83B;
            }
            if (s.is(BlockTags.EMERALD_ORES)) {
                return 0xFF3BFF6A;
            }
            return 0xFFC77BFF;
        }
    }

    static final class Tracers extends Module {
        private final Setting players = bool("Spieler", true);
        private final Setting mobs = bool("Monster", false);

        Tracers() {
            super("Tracers", Category.RENDER, "Linien vom Bildschirmrand zu Zielen");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            Proj pr = Draw.proj(pt);
            if (pr == null) {
                return;
            }
            int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
            int n = 0;
            for (Entity e : Util.entities(x -> x.isAlive() && ((players.on() && x instanceof Player) || (mobs.on() && x instanceof Monster)))) {
                if (n++ >= 25) {
                    break;
                }
                AABB bb = Util.smoothBox(e, pt);
                double[] p = pr.project(bb.getCenter().x, bb.getCenter().y, bb.getCenter().z);
                if (p != null) {
                    Draw.line(g, sw / 2.0, sh - 1, p[0], p[1], e instanceof Player ? 0xCC3BD1FF : 0xCCFF4B4B);
                }
            }
        }
    }

    static final class Nametags extends Module {
        Nametags() {
            super("Nametags", Category.RENDER, "Name, Leben und Abstand ueber Spielern");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            Proj pr = Draw.proj(pt);
            if (pr == null) {
                return;
            }
            int n = 0;
            for (Entity e : Util.entities(x -> x instanceof Player && x.isAlive())) {
                if (n++ >= 40) {
                    break;
                }
                AABB bb = Util.smoothBox(e, pt);
                double[] p = pr.project((bb.minX + bb.maxX) / 2, bb.maxY + 0.35, (bb.minZ + bb.maxZ) / 2);
                if (p == null) {
                    continue;
                }
                LivingEntity le = (LivingEntity) e;
                int hp = (int) Math.ceil(le.getHealth() + le.getAbsorptionAmount());
                int color = hp > 14 ? 0xFF3CE05A : hp > 7 ? 0xFFFFC832 : 0xFFFF3C3C;
                String text = e.getName().getString() + " " + hp + "HP " + (int) mc.player.distanceTo(e) + "m";
                Draw.centered(g, text, p[0], p[1] - 10, color);
            }
        }
    }

    static final class Fullbright extends Module {
        Fullbright() {
            super("Fullbright", Category.RENDER, "Alles hell (Nachtsicht nur auf deinem Bildschirm)");
        }

        @Override
        public void onTick() {
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, false));
        }

        @Override
        public void onDisable() {
            if (mc.player != null) {
                mc.player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
    }

    static final class Zoom extends Module {
        private int old = -1;

        Zoom() {
            super("Zoom", Category.RENDER, "Heranzoomen (Sichtfeld 30)");
        }

        @Override
        public void onEnable() {
            old = mc.options.fov().get();
            mc.options.fov().set(30);
        }

        @Override
        public void onDisable() {
            if (old > 0) {
                mc.options.fov().set(old);
            }
        }
    }

    private RenderModules() {
    }
}
