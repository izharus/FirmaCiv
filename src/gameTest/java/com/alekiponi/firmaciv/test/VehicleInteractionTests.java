package com.alekiponi.firmaciv.test;

import com.alekiponi.alekiships.common.entity.compartment.EmptyCompartmentEntity;
import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import com.alekiponi.alekiships.common.entity.vehiclecapability.IHaveCleats;
import com.alekiponi.firmaciv.common.entity.FirmacivEntities;
import com.alekiponi.firmaciv.common.entity.compartment.*;
import com.alekiponi.firmaciv.util.TFCWood;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("firmaciv")
@PrefixGameTestTemplate(false)
public final class VehicleInteractionTests {
    @GameTest(template = "empty")
    public static void installChestsAndBarrelsInCargoSlots(GameTestHelper helper) {
        var boats = spawnBoats(helper);
        helper.runAfterDelay(35, () -> {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            for (var boat : boats) {
                int tested = 0;
                for (var compartment : boat.getCompartments()) {
                    if (compartment == boat.getPilotCompartment()) continue;
                    var part = compartment.getVehicle();
                    var empty = (EmptyCompartmentEntity) compartment;
                    for (var blockType : new Wood.BlockType[]{Wood.BlockType.CHEST, Wood.BlockType.BARREL}) {
                        var stack = new ItemStack(Wood.OAK.getBlock(blockType).get(), 2);
                        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                        var result = player.interactOn(empty, InteractionHand.MAIN_HAND);
                        var label = boat.getType().toShortString() + " " + blockType + " slot=" + tested;
                        helper.assertTrue(result.consumesAction(), "Container interaction rejected: " + label);
                        var installed = part.getFirstPassenger();
                        helper.assertTrue(blockType == Wood.BlockType.CHEST ? installed instanceof TFCChestCompartmentEntity : installed instanceof TFCBarrelCompartmentEntity, "Wrong installed container: " + label);
                        helper.assertTrue(installed.getRootVehicle() == boat && empty.isRemoved(), "Container did not replace cargo seat: " + label);
                        helper.assertTrue(stack.getCount() == 1, "Wrong item consumption: " + label);
                        installed.stopRiding(); installed.discard();
                        empty = com.alekiponi.alekiships.common.entity.AlekiShipsEntities.EMPTY_COMPARTMENT_ENTITY.get().create(helper.getLevel());
                        empty.moveTo(part.position());
                        empty.startRiding(part, true);
                        helper.getLevel().addFreshEntity(empty);
                        empty.tick();
                    }
                    tested++;
                }
                helper.assertTrue(tested == boat.getCompartmentIndices().length - 1, "Missing cargo slots on " + boat.getType().toShortString());
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Wood.OAK.getBlock(Wood.BlockType.CHEST).get()));
                helper.assertTrue(!player.interactOn(boat.getPilotCompartment(), InteractionHand.MAIN_HAND).consumesAction(), "Container replaced pilot seat");
                helper.assertTrue(player.getMainHandItem().getCount() == 1, "Rejected pilot placement consumed chest");
                boat.getIndirectPassengers().forEach(Entity::discard); boat.discard();
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void leadAttachesDetachesAndSavesOnShips(GameTestHelper helper) {
        var boats = spawnBoats(helper);
        helper.runAfterDelay(35, () -> {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            for (var boat : boats) {
                var cleats = ((IHaveCleats) boat).getCleats();
                helper.assertTrue(!cleats.isEmpty(), "No mooring cleats on " + boat.getType().toShortString());
                for (var cleat : cleats) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LEAD, 2));
                    helper.assertTrue(player.interactOn(cleat, InteractionHand.MAIN_HAND).consumesAction(), "Lead interaction rejected");
                    helper.assertTrue(cleat.getLeashHolder() == player, "Lead did not attach to " + boat.getType().toShortString());
                    helper.assertTrue(cleat.getRootVehicle() == boat, "Attaching a lead unmounted the mooring cleat");
                    player.setPos(boat.position().add(7, 0, 0));
                    player.setOldPosAndRot();
                    boat.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    ((IHaveCleats) boat).tickCleatInput();
                    helper.assertTrue(boat.getDeltaMovement().x > 0, "Lead did not pull " + boat.getType().toShortString() + " towards player, distance=" + boat.distanceTo(player));
                    boat.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    helper.assertTrue(player.getMainHandItem().getCount() == 1, "Lead consumed incorrectly");
                    var tag = new CompoundTag(); cleat.saveWithoutId(tag);
                    helper.assertTrue(tag.contains("leash"), "Mooring leash missing from saved entity");
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    helper.assertTrue(player.interactOn(cleat, InteractionHand.MAIN_HAND).consumesAction(), "Lead removal rejected");
                    helper.assertTrue(!cleat.isLeashed(), "Lead did not detach");
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LEAD));
                    player.interactOn(cleat, InteractionHand.MAIN_HAND);
                    var fence = cleat.blockPosition().offset(2, 0, 0);
                    helper.getLevel().setBlockAndUpdate(fence, net.minecraft.world.level.block.Blocks.OAK_FENCE.defaultBlockState());
                    helper.assertTrue(LeadItem.bindPlayerMobs(player, helper.getLevel(), fence).consumesAction(), "Lead did not attach to fence");
                    helper.assertTrue(cleat.getLeashHolder() instanceof net.minecraft.world.entity.decoration.LeashFenceKnotEntity && cleat.getRootVehicle() == boat, "Fence mooring unmounted cleat");
                    var moored = new CompoundTag(); cleat.saveWithoutId(moored);
                    var part = cleat.getVehicle();
                    cleat.stopRiding(); cleat.discard();
                    var loaded = com.alekiponi.alekiships.common.entity.AlekiShipsEntities.VEHICLE_CLEAT_ENTITY.get().create(helper.getLevel());
                    loaded.load(moored); loaded.setUUID(java.util.UUID.randomUUID());
                    loaded.startRiding(part, true); helper.getLevel().addFreshEntity(loaded);
                    loaded.tick();
                    helper.assertTrue(loaded.isLeashed() && loaded.getRootVehicle() == boat, "Saved mooring did not restore on boat");
                    var knot = loaded.getLeashHolder();
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    player.interactOn(knot, InteractionHand.MAIN_HAND);
                    loaded.tick(); // Survival fence removal releases its leads on the next leash tick.
                    helper.assertTrue(!loaded.isLeashed() && loaded.getRootVehicle() == boat, "Fence mooring did not release");
                }
                boat.getIndirectPassengers().forEach(Entity::discard); boat.discard();
            }
            helper.succeed();
        });
    }

    private static AbstractVehicle[] spawnBoats(GameTestHelper helper) {
        var boats = new AbstractVehicle[]{
            FirmacivEntities.TFC_ROWBOATS.get(TFCWood.OAK).get().create(helper.getLevel()),
            FirmacivEntities.TFC_SLOOPS.get(TFCWood.OAK).get().create(helper.getLevel())
        };
        for (int i = 0; i < boats.length; i++) {
            boats[i].setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(5 + i * 6, 4, 8)));
            boats[i].setNoGravity(true);
            helper.getLevel().addFreshEntity(boats[i]);
        }
        return boats;
    }
    @GameTest(template = "empty")
    public static void holdSlotsCanBeTargetedThroughOpenHatch(GameTestHelper helper) {
        var boat = FirmacivEntities.TFC_SLOOPS.get(TFCWood.OAK).get().create(helper.getLevel());
        boat.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(8, 4, 8)));
        boat.setNoGravity(true);
        helper.getLevel().addFreshEntity(boat);
        helper.runAfterDelay(35, () -> {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            for (int index = 1; index <= 6; index++) {
                var slot = boat.getPassengers().get(index).getFirstPassenger();
                helper.assertTrue(slot instanceof EmptyCompartmentEntity, "Hold slot is missing: " + index);
                var to = slot.getBoundingBox().getCenter();
                var from = to.add(0, 3, 0);
                var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(player, from, to,
                    new net.minecraft.world.phys.AABB(from, to).inflate(0.1), e -> !e.isSpectator() && e.isPickable(), from.distanceToSqr(to));
                helper.assertTrue(hit != null && hit.getEntity() == slot, "Hull intercepts hold slot " + index + ": " + (hit == null ? "miss" : hit.getEntity().getType().toShortString()));
            }
            boat.getIndirectPassengers().forEach(Entity::discard); boat.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void chestSoundsFollowFirstAndLastViewer(GameTestHelper helper) {
        var boats = spawnBoats(helper);
        helper.runAfterDelay(35, () -> {
            var first = helper.makeMockPlayer(GameType.SURVIVAL);
            var second = helper.makeMockPlayer(GameType.SURVIVAL);
            var spectator = helper.makeMockPlayer(GameType.SPECTATOR);
            for (var boat : boats) {
                var cargo = (EmptyCompartmentEntity) boat.getCompartments().stream()
                    .filter(c -> c != boat.getPilotCompartment()).findFirst().orElseThrow();
                first.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Wood.OAK.getBlock(Wood.BlockType.CHEST).get()));
                var part = cargo.getVehicle();
                first.interactOn(cargo, InteractionHand.MAIN_HAND);
                var chest = (TFCChestCompartmentEntity) part.getFirstPassenger();
                int[] sounds = {0, 0};
                java.util.function.Consumer<net.neoforged.neoforge.event.PlayLevelSoundEvent> listener = event -> {
                    if (event.getLevel() != helper.getLevel() || event.getSound() == null) return;
                    var sound = event.getSound().value();
                    if (sound != net.minecraft.sounds.SoundEvents.CHEST_OPEN && sound != net.minecraft.sounds.SoundEvents.CHEST_CLOSE) return;
                    helper.assertTrue(event.getSource() == net.minecraft.sounds.SoundSource.BLOCKS && event.getNewVolume() > 0, "Chest sound has wrong category or volume");
                    sounds[sound == net.minecraft.sounds.SoundEvents.CHEST_OPEN ? 0 : 1]++;
                };
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
                try {
                    var firstMenu = chest.createMenu(1, first.getInventory(), first);
                    helper.assertTrue(sounds[0] == 1 && sounds[1] == 0, "First viewer did not play chest opening sound on " + boat.getType().toShortString());
                    var secondMenu = chest.createMenu(2, second.getInventory(), second);
                    helper.assertTrue(sounds[0] == 1, "Additional viewer repeated opening sound");
                    firstMenu.removed(first);
                    helper.assertTrue(sounds[1] == 0, "Chest closed while another viewer remained");
                    secondMenu.removed(second);
                    helper.assertTrue(sounds[1] == 1, "Last viewer did not play closing sound");
                    chest.stopOpen(second);
                    chest.startOpen(spectator); chest.stopOpen(spectator);
                    helper.assertTrue(sounds[0] == 1 && sounds[1] == 1, "Spectator or unmatched close replayed chest sounds");
                } finally {
                    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);
                }
                boat.getIndirectPassengers().forEach(Entity::discard); boat.discard();
            }
            helper.succeed();
        });
    }

}
