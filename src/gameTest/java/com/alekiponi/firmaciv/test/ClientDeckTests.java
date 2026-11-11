package com.alekiponi.firmaciv.test;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.slf4j.LoggerFactory;

/** Observe a real network-tracked sloop while a player stands freely on its deck. */
public final class ClientDeckTests {
    private static Vec3 reference, walkStart;
    private static float previousYaw;
    private static double maxDrift, maxYawStep, totalTurn;
    private static int idle, longestIdle;

    public static void tick(Minecraft mc, int tick, AbstractVehicle serverBoat) {
        if (tick == 820) mc.getSingleplayerServer().execute(() -> {
            if (ClientSmokeTest.mooringFence != null) mc.getSingleplayerServer().overworld().removeBlock(ClientSmokeTest.mooringFence, false);
            serverBoat.setYRot(90);
            serverBoat.setDeltaRotation(0);
            var player = mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
            player.closeContainer();
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            player.connection.teleport(serverBoat.getX() + 0.8, serverBoat.getBoundingBox().maxY + 0.05, serverBoat.getZ(), 0, 20);
            player.setDeltaMovement(Vec3.ZERO);
        });
        if (tick == 1470) {
            var boat = mc.level.getEntity(serverBoat.getId());
            walkStart = mc.player.position().subtract(boat.position()).yRot(boat.getYRot() * Mth.DEG_TO_RAD);
            mc.options.keyUp.setDown(true);
        }
        if (tick == 1474) mc.options.keyUp.setDown(false);
        if (tick == 1490) {
            var boat = mc.level.getEntity(serverBoat.getId());
            var local = mc.player.position().subtract(boat.position()).yRot(boat.getYRot() * Mth.DEG_TO_RAD);
            if (local.subtract(walkStart).horizontalDistance() < 0.05) throw new IllegalStateException("Deck transport prevented walking");
            LoggerFactory.getLogger(ClientDeckTests.class).info("CLIENT_DECK_WALK_OK displacement={}", local.subtract(walkStart).horizontalDistance());
        }
        if (tick < 850 || tick > 1450) return;
        var boat = mc.level.getEntity(serverBoat.getId());
        if (boat == null) throw new IllegalStateException("Deck test sloop did not synchronize");
        var local = mc.player.position().subtract(boat.position()).yRot(boat.getYRot() * Mth.DEG_TO_RAD);
        if (tick == 850) {
            if (!mc.player.onGround() || mc.player.isPassenger()) throw new IllegalStateException("Deck test player is not standing freely on the ship");
            reference = local;
            previousYaw = boat.getYRot();
            return;
        }
        double yawStep = Math.abs(Mth.wrapDegrees(boat.getYRot() - previousYaw));
        previousYaw = boat.getYRot();
        totalTurn += yawStep;
        maxYawStep = Math.max(maxYawStep, yawStep);
        if (yawStep < 1e-5) idle++; else idle = 0;
        longestIdle = Math.max(longestIdle, idle);
        maxDrift = Math.max(maxDrift, local.subtract(reference).horizontalDistance());
        if (tick == 1450) {
            LoggerFactory.getLogger(ClientDeckTests.class).info("DECK_MOTION turn={} maxYawStep={} longestIdle={} maxLocalDrift={}", totalTurn, maxYawStep, longestIdle, maxDrift);
            if (totalTurn < 5) throw new IllegalStateException("Deck test did not exercise a turning ship");
            if (longestIdle > 4 || maxYawStep > 0.3) throw new IllegalStateException("Sloop rotation stutters: idle=" + longestIdle + " step=" + maxYawStep);
            if (maxDrift > 0.005) throw new IllegalStateException("Sloop moved underneath standing player: drift=" + maxDrift);
            LoggerFactory.getLogger(ClientDeckTests.class).info("CLIENT_DECK_MOTION_OK");
        }
    }
}
