package com.alekiponi.firmaciv.test;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import com.alekiponi.firmaciv.common.block.FirmacivBlocks;
import com.alekiponi.firmaciv.common.entity.FirmacivEntities;
import com.alekiponi.firmaciv.common.entity.compartment.*;
import com.alekiponi.firmaciv.util.TFCWood;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in rendering/registry-sync smoke test. This source set is excluded from the release JAR. */
@EventBusSubscriber(modid = "firmaciv", value = Dist.CLIENT)
public final class ClientSmokeTest {
    private static int ticks;
    private static AbstractVehicle rowboat;
    private static AbstractVehicle sloop;
    private static volatile int placementTarget;
    private static int placementPart;
    private static int chestOpenSounds, chestCloseSounds, opensBeforeMenu, closesBeforeMenu;

    @SubscribeEvent
    public static void chestSound(net.neoforged.neoforge.client.event.sound.PlaySoundEvent event) {
        if (!Boolean.getBoolean("firmaciv.smokeTest")) return;
        var sound = event.getSound();
        if (sound == null) return;
        if (sound.getLocation().equals(net.minecraft.sounds.SoundEvents.CHEST_OPEN.getLocation())) chestOpenSounds++;
        if (sound.getLocation().equals(net.minecraft.sounds.SoundEvents.CHEST_CLOSE.getLocation())) chestCloseSounds++;
    }

    static volatile BlockPos mooringFence;
    private static final java.util.Map<Integer, double[]> heights = new java.util.HashMap<>();
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("firmaciv.smokeTest")) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        ticks++;
        var server = mc.getSingleplayerServer();
        if (ticks == 1) {
            mc.options.pauseOnLostFocus = false;
            server.execute(() -> {
                var level = server.overworld();
                level.setDayTime(6000);
                var player = server.getPlayerList().getPlayers().getFirst();
                player.setGameMode(GameType.CREATIVE);
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
                player.teleportTo(0, 76, -12);
                player.connection.teleport(0, 76, -12, 0, 17);
                for (var pos : BlockPos.betweenClosed(-22, 70, -2, 22, 70, 24)) {
                    level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
                    level.setBlock(pos.above(), net.dries007.tfc.common.fluids.TFCFluids.SALT_WATER.getSource().defaultFluidState().createLegacyBlock(), 2);
                    level.setBlock(pos.above(2), net.dries007.tfc.common.fluids.TFCFluids.SALT_WATER.getSource().defaultFluidState().createLegacyBlock(), 2);
                }
                Entity[] entities = {
                    FirmacivEntities.TFC_ROWBOATS.get(TFCWood.OAK).get().create(level),
                    FirmacivEntities.TFC_CANOES.get(TFCWood.OAK).get().create(level),
                    FirmacivEntities.KAYAK_ENTITY.get().create(level),
                    FirmacivEntities.TFC_SLOOPS.get(TFCWood.OAK).get().create(level),
                    FirmacivEntities.TFC_SLOOPS_UNDER_CONSTRUCTION.get(TFCWood.OAK).get().create(level)
                };
                double[][] positions = {{-7, 3}, {0, 3}, {7, 3}, {-7, 14}, {7, 14}};
                for (int i = 0; i < entities.length; i++) {
                    entities[i].moveTo(positions[i][0], 73, positions[i][1], 90, 0);
                    level.addFreshEntity(entities[i]);
                }
                rowboat = (AbstractVehicle) entities[0];
                sloop = (AbstractVehicle) entities[3];
                for (int x = -3; x <= 3; x++) level.setBlock(new BlockPos(x, 73, -1), FirmacivBlocks.WOOD_ROOFING.get(Wood.OAK).get().defaultBlockState(), 3);
            });
        }
        if (ticks == 130) verifyRemoteInterpolation(mc);
        if (ticks == 100) server.execute(() -> {
            if (rowboat == null || rowboat.getCompartments().isEmpty()) throw new IllegalStateException("Boat compartments missing");
            var old = rowboat.getCompartments().stream().filter(c -> c != rowboat.getPilotCompartment()).findFirst().orElseThrow();
            var parent = old.getVehicle();
            var barrel = new TFCBarrelCompartmentEntity(FirmacivEntities.TFC_BARREL_COMPARTMENT_ENTITY.get(), server.overworld(), new ItemStack(Wood.OAK.getBlock(Wood.BlockType.BARREL).get()));
            old.stopRiding(); old.discard();
            barrel.moveTo(parent.position());
            barrel.startRiding(parent, true);
            server.overworld().addFreshEntity(barrel);
        });
        if (ticks >= 150 && ticks <= 350) {
            for (var entity : mc.level.entitiesForRendering()) {
                if (entity instanceof com.alekiponi.alekiships.common.entity.vehicle.AbstractAlekiBoatEntity<?>) {
                    var range = heights.computeIfAbsent(entity.getId(), ignored -> new double[]{Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0});
                    range[0] = Math.min(range[0], entity.getY()); range[1] = Math.max(range[1], entity.getY());
                    range[2] = Math.max(range[2], Math.abs(entity.getY() - entity.yOld));
                }
            }
        }
        if (ticks == 350) {
            for (var entity : mc.level.entitiesForRendering()) {
                var range = heights.get(entity.getId());
                if (range != null) {
                    org.slf4j.LoggerFactory.getLogger(ClientSmokeTest.class).info("CLIENT_BUOYANCY {} range={} step={} min={} max={}", entity.getType().toShortString(), range[1] - range[0], range[2], range[0], range[1]);
                    if (range[1] - range[0] > 0.05) throw new IllegalStateException("Client buoyancy oscillates: " + entity.getType().toShortString() + " range=" + (range[1] - range[0]));
                }
            }
        }
        if (ticks == 360) {
            long visible = java.util.stream.StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false)
                .filter(e -> net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals("firmaciv")).count();
            if (visible < 6) throw new IllegalStateException("Client did not receive all port entities: " + visible);
            for (var entity : mc.level.entitiesForRendering()) {
                if (entity instanceof com.alekiponi.alekiships.common.entity.vehicle.AbstractAlekiBoatEntity<?> && entity.getY() < 72) {
                    throw new IllegalStateException("Boat sank in TFC salt water: " + entity.getType().toShortString());
                }
            }
            Screenshot.grab(mc.gameDirectory, "firmaciv-port-smoke.png", mc.getMainRenderTarget(), message ->
                org.slf4j.LoggerFactory.getLogger(ClientSmokeTest.class).info("PORT_CLIENT_SMOKE_OK: {}", message.getString()));
        }
        if (ticks >= 400 && ticks <= 630) verifyContainerPlacement(mc, ticks);
        if (ticks >= 660) verifyMooring(mc, ticks);
        if (ticks >= 820) ClientDeckTests.tick(mc, ticks, sloop);
        if (ticks == 1530) mc.stop();
    }
    private static void verifyRemoteInterpolation(Minecraft mc) {
        var boats = new com.alekiponi.alekiships.common.entity.vehicle.AbstractAlekiBoatEntity<?>[]{
            FirmacivEntities.KAYAK_ENTITY.get().create(mc.level),
            FirmacivEntities.TFC_CANOES.get(TFCWood.OAK).get().create(mc.level),
            FirmacivEntities.TFC_ROWBOATS.get(TFCWood.OAK).get().create(mc.level),
            FirmacivEntities.TFC_SLOOPS.get(TFCWood.OAK).get().create(mc.level)
        };
        for (var boat : boats) {
            String label = boat.getType().toShortString();
            boat.setPos(0, 73, 3);
            if (boat.isControlledByLocalInstance()) throw new IllegalStateException("Test boat is not remote");
            // Same callbacks used by incoming server position/velocity packets.
            boat.lerpTo(0, 72.55, 3, 0, 0, 3);
            if (boat.getY() != 73) throw new IllegalStateException("Server position snapped instead of interpolating: " + label);
            boat.lerpMotion(0, -0.25, 0);
            for (int step = 1; step <= 3; step++) {
                boat.tickCount++;
                boat.tick();
                double expected = 73 + (72.55 - 73) * step / 3;
                if (Math.abs(boat.getY() - expected) > 1e-6) throw new IllegalStateException("Remote physics overrides server interpolation: " + label + " step=" + step + " expected=" + expected + " actual=" + boat.getY());
            }
            // No fresh server packet: a remote boat must not start sinking locally.
            for (int step = 0; step < 20; step++) {
                boat.tickCount++; boat.tick();
                if (Math.abs(boat.getY() - 72.55) > 1e-6) throw new IllegalStateException("Remote boat drifted between packets: " + label);
            }
            boat.discard();
        }
        org.slf4j.LoggerFactory.getLogger(ClientSmokeTest.class).info("REMOTE_BOAT_INTERPOLATION_OK");
    }

    private static void verifyContainerPlacement(Minecraft mc, int tick) {
        int step = (tick - 400) / 60;
        int phase = (tick - 400) % 60;
        boolean chest = step % 2 == 0;
        if (phase == 0) mc.getSingleplayerServer().execute(() -> {
            var boat = step < 2 ? rowboat : sloop;
            var cargo = boat.getCompartments().stream()
                .filter(c -> c instanceof com.alekiponi.alekiships.common.entity.compartment.EmptyCompartmentEntity && c != boat.getPilotCompartment()).findFirst().orElseThrow();
            var player = mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
            player.connection.teleport(cargo.getX(), cargo.getY() + 1, cargo.getZ(), 0, 90);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Wood.OAK.getBlock(chest ? Wood.BlockType.CHEST : Wood.BlockType.BARREL).get()));
            placementTarget = cargo.getId();
        });
        if (phase == 20) {
            var cargo = mc.level.getEntity(placementTarget);
            if (cargo == null) throw new IllegalStateException("Cargo target did not synchronize");
            placementPart = cargo.getVehicle().getId();
            var picked = pickContainer(mc, cargo);
            var result = mc.gameMode.interact(mc.player, picked, net.minecraft.world.InteractionHand.MAIN_HAND);
            if (!result.consumesAction()) throw new IllegalStateException("Client rejected container placement");
            if (cargo.isRemoved()) throw new IllegalStateException("Client replaced cargo before the server: " + (chest ? "chest" : "barrel"));
        }
        if (phase == 40) {
            var part = mc.level.getEntity(placementPart);
            var installed = part == null ? null : part.getFirstPassenger();
            if (!(chest ? installed instanceof TFCChestCompartmentEntity : installed instanceof TFCBarrelCompartmentEntity)) throw new IllegalStateException("Installed container did not synchronize, case " + step);
            if (mc.player.getMainHandItem().getCount() != 1) throw new IllegalStateException("Creative placement consumed container, case " + step);
            org.slf4j.LoggerFactory.getLogger(ClientSmokeTest.class).info("CLIENT_CONTAINER_PLACEMENT_OK case={}", step);
            if (chest) { opensBeforeMenu = chestOpenSounds; closesBeforeMenu = chestCloseSounds; }
            mc.gameMode.interact(mc.player, pickContainer(mc, installed), net.minecraft.world.InteractionHand.MAIN_HAND);
        }
        if (phase == 50) {
            if (!(mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>)) throw new IllegalStateException("Placed container menu did not open, case " + step);
            if (chest && (chestOpenSounds != opensBeforeMenu + 1 || chestCloseSounds != closesBeforeMenu)) throw new IllegalStateException("Client did not play exactly one chest opening sound, case " + step);
            mc.player.closeContainer();
            org.slf4j.LoggerFactory.getLogger(ClientSmokeTest.class).info("CLIENT_CONTAINER_MENU_OK case={}", step);
        }
        if (phase == 55 && chest) {
            if (chestCloseSounds != closesBeforeMenu + 1) throw new IllegalStateException("Client did not play exactly one chest closing sound, case " + step);
            org.slf4j.LoggerFactory.getLogger(ClientSmokeTest.class).info("CLIENT_CHEST_AUDIO_OK case={}", step);
        }
    }

    private static void verifyMooring(Minecraft mc, int tick) {
        if (tick == 660) mc.getSingleplayerServer().execute(() -> {
            var cleat = ((com.alekiponi.alekiships.common.entity.vehiclecapability.IHaveCleats) sloop).getCleats().getFirst();
            var player = mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
            player.connection.teleport(cleat.getX(), cleat.getY() + 1, cleat.getZ() - 2, 0, 20);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(net.minecraft.world.item.Items.LEAD));
            placementTarget = cleat.getId();
        });
        if (tick == 680) {
            var cleat = mc.level.getEntity(placementTarget);
            if (!mc.gameMode.interact(mc.player, cleat, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction()) throw new IllegalStateException("Client refused lead attachment");
        }
        if (tick == 700) {
            var cleat = (com.alekiponi.alekiships.common.entity.vehiclehelper.CleatEntity) mc.level.getEntity(placementTarget);
            if (cleat.getLeashHolder() != mc.player || cleat.getRootVehicle().getId() != sloop.getId()) throw new IllegalStateException("Sloop leash did not synchronize or unmounted its cleat");
            mc.getSingleplayerServer().execute(() -> {
                var serverCleat = mc.getSingleplayerServer().overworld().getEntity(placementTarget);
                mooringFence = serverCleat.blockPosition().offset(2, 0, 0);
                mc.getSingleplayerServer().overworld().setBlockAndUpdate(mooringFence, Blocks.OAK_FENCE.defaultBlockState());
            });
        }
        if (tick == 730) {
            var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(mooringFence), net.minecraft.core.Direction.UP, mooringFence, false);
            if (!mc.gameMode.useItemOn(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND, hit).consumesAction()) throw new IllegalStateException("Client refused fence mooring");
        }
        if (tick == 750) {
            var cleat = (com.alekiponi.alekiships.common.entity.vehiclehelper.CleatEntity) mc.level.getEntity(placementTarget);
            if (!(cleat.getLeashHolder() instanceof net.minecraft.world.entity.decoration.LeashFenceKnotEntity) || cleat.getRootVehicle().getId() != sloop.getId()) throw new IllegalStateException("Fence mooring did not synchronize");
            Screenshot.grab(mc.gameDirectory, "firmaciv-mooring-smoke.png", mc.getMainRenderTarget(), message ->
                org.slf4j.LoggerFactory.getLogger(ClientSmokeTest.class).info("MOORING_SCREENSHOT: {}", message.getString()));
            mc.gameMode.interact(mc.player, cleat.getLeashHolder(), net.minecraft.world.InteractionHand.MAIN_HAND);
        }
        if (tick == 780) {
            var cleat = (com.alekiponi.alekiships.common.entity.vehiclehelper.CleatEntity) mc.level.getEntity(placementTarget);
            if (cleat.isLeashed() || cleat.getRootVehicle().getId() != sloop.getId()) throw new IllegalStateException("Fence mooring did not release");
            org.slf4j.LoggerFactory.getLogger(ClientSmokeTest.class).info("CLIENT_SLOOP_MOORING_OK");
        }
    }

    private static Entity pickContainer(Minecraft mc, Entity expected) {
        var delta = expected.getBoundingBox().getCenter().subtract(mc.player.getEyePosition(1));
        mc.player.setYRot((float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90);
        mc.player.setXRot((float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance())));
        mc.gameRenderer.pick(1);
        if (!(mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit) || hit.getEntity() != expected) {
            throw new IllegalStateException("Crosshair cannot target cargo: " + expected.getType().toShortString() + "; hit=" + mc.hitResult);
        }
        return ((net.minecraft.world.phys.EntityHitResult) mc.hitResult).getEntity();
    }

}
