package de.pbil.client;

import java.util.List;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

final class MovementModules {
    static void register(List<Module> list) {
        list.add(new Sprint());
        list.add(new Speed());
        list.add(new Fly());
        list.add(new Step());
        list.add(new NoFall());
        list.add(new AutoWalk());
        list.add(new AutoJump());
    }

    static boolean moving() {
        var o = Util.mc().options;
        return o.keyUp.isDown() || o.keyDown.isDown() || o.keyLeft.isDown() || o.keyRight.isDown();
    }

    static final class Sprint extends Module {
        Sprint() {
            super("Sprint", Category.MOVEMENT, "Rennt immer, wenn du vorwaerts gehst");
        }

        @Override
        public void onTick() {
            if (mc.options.keyUp.isDown() && !mc.player.horizontalCollision && mc.player.getFoodData().getFoodLevel() > 6) {
                mc.player.setSprinting(true);
            }
        }
    }

    static final class Speed extends Module {
        private final Setting factor = num("Faktor", 1.3, 1.0, 3.0, 0.05);

        Speed() {
            super("Speed", Category.MOVEMENT, "Schneller am Boden");
        }

        @Override
        public void onTick() {
            if (moving() && mc.player.onGround()) {
                Vec3 d = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(d.x * factor.get(), d.y, d.z * factor.get());
            }
        }
    }

    static final class Fly extends Module {
        private final Setting speed = num("Tempo", 0.05, 0.02, 0.5, 0.01);

        Fly() {
            super("Fly", Category.MOVEMENT, "Fliegen wie im Creative (Server koennen es zuruecksetzen)");
        }

        @Override
        public void onTick() {
            mc.player.getAbilities().mayfly = true;
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().setFlyingSpeed((float) speed.get());
        }

        @Override
        public void onDisable() {
            if (mc.player != null && !mc.player.isCreative() && !mc.player.isSpectator()) {
                mc.player.getAbilities().mayfly = false;
                mc.player.getAbilities().flying = false;
                mc.player.getAbilities().setFlyingSpeed(0.05f);
            }
        }
    }

    static final class Step extends Module {
        private final Setting height = num("Hoehe", 2.0, 1.0, 5.0, 0.5);

        Step() {
            super("Step", Category.MOVEMENT, "Steigt Bloecke ohne Springen hoch");
        }

        @Override
        public void onTick() {
            var attr = mc.player.getAttribute(Attributes.STEP_HEIGHT);
            if (attr != null) {
                attr.setBaseValue(height.get());
            }
        }

        @Override
        public void onDisable() {
            if (mc.player != null) {
                var attr = mc.player.getAttribute(Attributes.STEP_HEIGHT);
                if (attr != null) {
                    attr.setBaseValue(0.6);
                }
            }
        }
    }

    static final class NoFall extends Module {
        NoFall() {
            super("NoFall", Category.MOVEMENT, "Kein Fallschaden (meldet dem Server Bodenkontakt)");
        }

        @Override
        public void onTick() {
            if (mc.player.fallDistance > 2.5 && !mc.player.isFallFlying()) {
                mc.player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision));
            }
        }
    }

    static final class AutoWalk extends Module {
        AutoWalk() {
            super("AutoWalk", Category.MOVEMENT, "Geht dauerhaft vorwaerts");
        }

        @Override
        public void onTick() {
            mc.options.keyUp.setDown(true);
        }

        @Override
        public void onDisable() {
            mc.options.keyUp.setDown(false);
        }
    }

    static final class AutoJump extends Module {
        AutoJump() {
            super("AutoJump", Category.MOVEMENT, "Springt automatisch beim Laufen");
        }

        @Override
        public void onTick() {
            mc.options.keyJump.setDown(moving() && mc.player.onGround());
        }

        @Override
        public void onDisable() {
            mc.options.keyJump.setDown(false);
        }
    }

    private MovementModules() {
    }
}
