package de.pbil.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class Util {
    static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /** Box der Entity, an die Stelle des aktuellen Bildes verschoben (weiche Bewegung). */
    static AABB smoothBox(Entity e, float partial) {
        double dx = Mth.lerp(partial, e.xo, e.getX()) - e.getX();
        double dy = Mth.lerp(partial, e.yo, e.getY()) - e.getY();
        double dz = Mth.lerp(partial, e.zo, e.getZ()) - e.getZ();
        return e.getBoundingBox().move(dx, dy, dz);
    }

    static List<Entity> entities(Predicate<Entity> filter) {
        List<Entity> out = new ArrayList<>();
        Minecraft mc = mc();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e != mc.player && !e.isRemoved() && filter.test(e)) {
                out.add(e);
            }
        }
        return out;
    }

    static Entity nearest(Predicate<Entity> filter, double range) {
        LocalPlayer p = mc().player;
        Entity best = null;
        double bestD = range * range;
        for (Entity e : mc().level.entitiesForRendering()) {
            if (e == p || e.isRemoved() || !filter.test(e)) {
                continue;
            }
            double d = p.getEyePosition().distanceToSqr(e.getBoundingBox().getCenter());
            if (d <= bestD) {
                bestD = d;
                best = e;
            }
        }
        return best;
    }

    /** Dreht den Spieler zum Punkt. */
    static void face(Vec3 target) {
        LocalPlayer p = mc().player;
        Vec3 eye = p.getEyePosition();
        double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
        double flat = Math.sqrt(dx * dx + dz * dz);
        p.setYRot((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        p.setXRot((float) -Math.toDegrees(Math.atan2(dy, flat)));
    }

    /** Hotbar-Slot (0-8) mit diesem Item, sonst -1. */
    static int hotbarSlot(Item item) {
        LocalPlayer p = mc().player;
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getItem(i).is(item)) {
                return i;
            }
        }
        return -1;
    }

    /** Anzahl eines Items im ganzen Inventar plus Nebenhand. */
    static int count(Item item) {
        LocalPlayer p = mc().player;
        int n = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.is(item)) {
                n += s.getCount();
            }
        }
        ItemStack off = p.getOffhandItem();
        if (off.is(item)) {
            n += off.getCount();
        }
        return n;
    }

    /** Alle passenden Bloecke rund um den Spieler (maximal max Treffer, naechste zuerst nicht garantiert). */
    static List<BlockPos> scan(int radius, int vertical, Predicate<BlockState> test, int max) {
        List<BlockPos> out = new ArrayList<>();
        LocalPlayer p = mc().player;
        BlockPos c = p.blockPosition();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -vertical; y <= vertical; y++) {
                    m.set(c.getX() + x, c.getY() + y, c.getZ() + z);
                    if (mc().level.isOutsideBuildHeight(m)) {
                        continue;
                    }
                    if (test.test(mc().level.getBlockState(m))) {
                        out.add(m.immutable());
                        if (out.size() >= max) {
                            return out;
                        }
                    }
                }
            }
        }
        return out;
    }

    private Util() {
    }
}
