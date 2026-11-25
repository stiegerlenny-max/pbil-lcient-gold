package de.pbil.client;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

final class CombatModules {
    static void register(List<Module> list) {
        list.add(new KillAura());
        list.add(new TriggerBot());
        list.add(new CrystalAura());
        list.add(new AutoTotem());
        list.add(new Surround());
    }

    static boolean ready(float min) {
        return mcPlayerReady(min);
    }

    private static boolean mcPlayerReady(float min) {
        return Util.mc().player.getAttackStrengthScale(0.5f) >= min;
    }

    static void hit(Entity e) {
        Util.mc().gameMode.attack(Util.mc().player, e);
        Util.mc().player.swing(InteractionHand.MAIN_HAND);
    }

    static final class KillAura extends Module {
        private final Setting range = num("Reichweite", 4.0, 2.0, 6.0, 0.1);
        private final Setting players = bool("Spieler", true);
        private final Setting mobs = bool("Monster", false);
        private final Setting rotate = bool("Drehen", true);

        KillAura() {
            super("KillAura", Category.COMBAT, "Greift Ziele in Reichweite automatisch an");
        }

        @Override
        public void onTick() {
            Entity t = Util.nearest(e -> e instanceof LivingEntity le && le.isAlive()
                && ((players.on() && e instanceof Player) || (mobs.on() && e instanceof Monster)), range.get());
            if (t == null) {
                return;
            }
            if (rotate.on()) {
                Util.face(t.getBoundingBox().getCenter());
            }
            if (ready(0.95f)) {
                hit(t);
            }
        }
    }

    static final class TriggerBot extends Module {
        TriggerBot() {
            super("TriggerBot", Category.COMBAT, "Schlaegt zu, sobald das Fadenkreuz auf ein Lebewesen zeigt");
        }

        @Override
        public void onTick() {
            if (mc.hitResult instanceof EntityHitResult r && r.getEntity() instanceof LivingEntity le && le.isAlive() && ready(0.95f)) {
                hit(le);
            }
        }
    }

    static final class CrystalAura extends Module {
        private final Setting range = num("Reichweite", 4.5, 2.0, 6.0, 0.1);
        private final Setting delay = num("Pause (Ticks)", 1, 0, 10, 1);
        private final Setting place = bool("Platzieren", true);
        private final Setting target = num("Zielabstand", 8, 3, 16, 1);
        private int wait;

        CrystalAura() {
            super("CrystalAura", Category.COMBAT, "Zerstoert Crystals und setzt neue neben Gegner (Crystal in die Hand nehmen)");
        }

        @Override
        public void onTick() {
            if (wait > 0) {
                wait--;
                return;
            }
            Entity crystal = Util.nearest(e -> e instanceof EndCrystal, range.get());
            if (crystal != null) {
                hit(crystal);
                wait = delay.asInt();
                return;
            }
            if (!place.on()) {
                return;
            }
            InteractionHand hand = mc.player.getMainHandItem().is(Items.END_CRYSTAL) ? InteractionHand.MAIN_HAND
                : mc.player.getOffhandItem().is(Items.END_CRYSTAL) ? InteractionHand.OFF_HAND : null;
            if (hand == null) {
                return;
            }
            Entity enemy = Util.nearest(e -> e instanceof Player && e.isAlive(), target.get());
            if (enemy == null) {
                return;
            }
            BlockPos best = null;
            double bestD = Double.MAX_VALUE;
            BlockPos feet = enemy.blockPosition();
            int r = 4;
            for (int x = -r; x <= r; x++) {
                for (int y = -2; y <= 1; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos p = feet.offset(x, y, z);
                        if (!canPlace(p)) {
                            continue;
                        }
                        double dPlayer = mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(p));
                        double dEnemy = enemy.position().distanceTo(Vec3.atCenterOf(p.above()));
                        if (dPlayer > range.get() || dPlayer < 2.5 || dEnemy < 1.2) {
                            continue;
                        }
                        if (dEnemy < bestD) {
                            bestD = dEnemy;
                            best = p;
                        }
                    }
                }
            }
            if (best != null) {
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(best).add(0, 0.5, 0), Direction.UP, best, false);
                mc.gameMode.useItemOn(mc.player, hand, hit);
                mc.player.swing(hand);
                wait = delay.asInt();
            }
        }

        private boolean canPlace(BlockPos p) {
            BlockState base = mc.level.getBlockState(p);
            if (!base.is(Blocks.OBSIDIAN) && !base.is(Blocks.BEDROCK)) {
                return false;
            }
            if (!mc.level.getBlockState(p.above()).isAir() || !mc.level.getBlockState(p.above(2)).isAir()) {
                return false;
            }
            AABB space = new AABB(p.above()).expandTowards(0, 1, 0);
            return mc.level.getEntities((Entity) null, space).isEmpty();
        }
    }

    static final class AutoTotem extends Module {
        private final Setting health = num("Leben unter", 20, 1, 20, 1);
        private int wait;

        AutoTotem() {
            super("AutoTotem", Category.COMBAT, "Haelt immer einen Totem in der Nebenhand");
        }

        @Override
        public void onTick() {
            if (wait > 0) {
                wait--;
                return;
            }
            if (mc.player.getHealth() > health.get() || mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
                return;
            }
            if (mc.player.containerMenu != mc.player.inventoryMenu) {
                return;
            }
            for (int i = 0; i < 36; i++) {
                if (mc.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                    int slot = i < 9 ? 36 + i : i;
                    mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, slot, 40, ClickType.SWAP, mc.player);
                    wait = 3;
                    return;
                }
            }
        }
    }

    static final class Surround extends Module {
        private int wait;

        Surround() {
            super("Surround", Category.COMBAT, "Setzt Obsidian um deine Fuesse (Obsidian in die Hotbar legen)");
        }

        @Override
        public void onTick() {
            if (wait > 0) {
                wait--;
                return;
            }
            int slot = Util.hotbarSlot(Items.OBSIDIAN);
            if (slot < 0 || !mc.player.onGround()) {
                return;
            }
            BlockPos feet = mc.player.blockPosition();
            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos p = feet.relative(d);
                BlockPos below = p.below();
                if (!mc.level.getBlockState(p).canBeReplaced() || mc.level.getBlockState(below).isAir()) {
                    continue;
                }
                if (!mc.level.getEntities((Entity) null, new AABB(p)).isEmpty()) {
                    continue;
                }
                int old = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot(slot);
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(below).add(0, 0.5, 0), Direction.UP, below, false);
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                mc.player.swing(InteractionHand.MAIN_HAND);
                mc.player.getInventory().setSelectedSlot(old);
                wait = 1;
                return;
            }
        }
    }

    private CombatModules() {
    }
}
