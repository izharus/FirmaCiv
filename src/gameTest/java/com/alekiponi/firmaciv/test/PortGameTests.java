package com.alekiponi.firmaciv.test;

import com.alekiponi.alekiships.common.AlekiShipsDataMaps;
import com.alekiponi.alekiships.common.AlekiShipsRegistries;
import com.alekiponi.alekiships.common.entity.EntityInput;
import com.alekiponi.alekiships.common.item.AlekiShipsItems;
import com.alekiponi.alekiships.common.recipe.EntityMultiblockRecipe;
import com.alekiponi.firmaciv.common.entity.FirmacivEntities;
import com.alekiponi.firmaciv.common.entity.compartment.*;
import com.alekiponi.firmaciv.common.block.FirmacivBlocks;
import com.alekiponi.firmaciv.util.TFCWood;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("firmaciv")
@PrefixGameTestTemplate(false)
public final class PortGameTests {
    @GameTest(template = "empty")
    public static void vehicleVariantsSurviveSave(GameTestHelper helper) {
        for (var wood : TFCWood.values()) {
            var rowboat = FirmacivEntities.TFC_ROWBOATS.get(wood).get().create(helper.getLevel());
            var sloop = FirmacivEntities.TFC_SLOOPS.get(wood).get().create(helper.getLevel());
            var construction = FirmacivEntities.TFC_SLOOPS_UNDER_CONSTRUCTION.get(wood).get().create(helper.getLevel());
            var canoe = FirmacivEntities.TFC_CANOES.get(wood).get().create(helper.getLevel());
            for (Entity entity : new Entity[]{rowboat, sloop, construction, canoe}) {
                var tag = new CompoundTag();
                entity.saveWithoutId(tag);
                var restored = entity.getType().create(helper.getLevel());
                restored.load(tag);
                helper.assertTrue(restored.getType() == entity.getType(), "Entity type changed: " + wood);
            }
            helper.assertTrue(rowboat.getVariant().unwrapKey().orElseThrow().location().equals(ResourceLocation.fromNamespaceAndPath("firmaciv", wood.getSerializedName())), "Wrong rowboat variant");
            helper.assertTrue(sloop.getVariant().unwrapKey().orElseThrow().location().getNamespace().equals("firmaciv"), "Wrong sloop variant");
        }
        var kayak = FirmacivEntities.KAYAK_ENTITY.get().create(helper.getLevel());
        helper.assertTrue(kayak.getVariant().unwrapKey().orElseThrow().location().getPath().equals("kayak"), "Wrong kayak variant");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sealedBarrelPreservesCargo(GameTestHelper helper) {
        var stack = new ItemStack(Wood.OAK.getBlock(Wood.BlockType.BARREL).get());
        var placement = stack.getItemHolder().getData(AlekiShipsDataMaps.COMPARTMENT_PLACEABLE);
        helper.assertTrue(placement != null, "Barrel placement data missing");
        var barrel = (TFCBarrelCompartmentEntity) placement.createCompartment(helper.getLevel(), stack).orElseThrow();
        barrel.getFluidHandler(null).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        barrel.setItem(BarrelBlockEntity.SLOT_ITEM, new ItemStack(Items.DIAMOND, 3));
        barrel.onSeal();
        helper.assertTrue(barrel.getItemHandler(null).extractItem(BarrelBlockEntity.SLOT_ITEM, 1, false).isEmpty(), "Sealed barrel allowed extraction");
        var stored = barrel.getDropStack();
        var restored = new TFCBarrelCompartmentEntity(FirmacivEntities.TFC_BARREL_COMPARTMENT_ENTITY.get(), helper.getLevel(), stored);
        helper.assertTrue(restored.isSealed(), "Seal lost on item round trip");
        helper.assertTrue(restored.getItem(BarrelBlockEntity.SLOT_ITEM).getCount() == 3, "Barrel items lost");
        helper.assertTrue(restored.getFluidHandler(null).getFluidInTank(0).getAmount() == 1000, "Barrel fluid lost");
        CompoundTag tag = new CompoundTag(); restored.saveWithoutId(tag);
        var loaded = FirmacivEntities.TFC_BARREL_COMPARTMENT_ENTITY.get().create(helper.getLevel()); loaded.load(tag);
        helper.assertTrue(loaded.isSealed() && loaded.getItem(BarrelBlockEntity.SLOT_ITEM).getCount() == 3, "Barrel world save failed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chestAndVesselPlacement(GameTestHelper helper) {
        var chestStack = new ItemStack(Wood.OAK.getBlock(Wood.BlockType.CHEST).get());
        var chestPlacement = chestStack.getItemHolder().getData(AlekiShipsDataMaps.COMPARTMENT_PLACEABLE);
        var chest = (TFCChestCompartmentEntity) chestPlacement.createCompartment(helper.getLevel(), chestStack).orElseThrow();
        helper.assertTrue(chest.getContainerSize() == 18, "Chest must have TFC capacity");
        var vesselStack = new ItemStack(TFCBlocks.LARGE_VESSEL.get());
        var placement = vesselStack.getItemHolder().getData(AlekiShipsDataMaps.COMPARTMENT_PLACEABLE);
        helper.assertTrue(placement != null, "Vessel placement data missing");
        var vessel = (LargeVesselCompartmentEntity) placement.createCompartment(helper.getLevel(), vesselStack).orElseThrow();
        vessel.setItem(0, new ItemStack(Items.DIAMOND, 2)); vessel.onSeal();
        var restored = new LargeVesselCompartmentEntity(FirmacivEntities.LARGE_VESSEL_COMPARTMENT_ENTITY.get(), helper.getLevel(), vessel.getDropStack());
        helper.assertTrue(restored.isSealed() && restored.getItem(0).getCount() == 2, "Vessel contents lost");
        var tag = new CompoundTag(); restored.saveWithoutId(tag);
        var loaded = FirmacivEntities.LARGE_VESSEL_COMPARTMENT_ENTITY.get().create(helper.getLevel()); loaded.load(tag);
        helper.assertTrue(loaded.isSealed() && loaded.getItem(0).getCount() == 2, "Vessel world save failed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cannonRequiresAllThreeInputs(GameTestHelper helper) {
        var cannon = FirmacivEntities.FIRMACIV_CANNON_ENTITY.get().create(helper.getLevel());
        var input = EntityInput.getEntityInput(helper.getLevel().registryAccess(), cannon.getInputState().entityInputKey());
        helper.assertTrue(!input.tryInsert(cannon, new ItemStack(AlekiShipsItems.CANNONBALL.get()), null).getResult().consumesAction(), "Cannon accepted ball before powder");
        for (Item item : new Item[]{Items.GUNPOWDER, TFCItems.UNREFINED_PAPER.get(), AlekiShipsItems.CANNONBALL.get()}) {
            helper.assertTrue(!cannon.isLoaded(), "Cannon loaded too early");
            var remainder = input.tryInsert(cannon, new ItemStack(item, 64), null).getObject();
            helper.assertTrue(remainder.getCount() == 63, "Cannon consumed more than one item");
        }
        helper.assertTrue(cannon.isLoaded(), "Cannon failed to load");
        var tag = new CompoundTag(); cannon.saveWithoutId(tag);
        var restored = FirmacivEntities.FIRMACIV_CANNON_ENTITY.get().create(helper.getLevel()); restored.load(tag);
        helper.assertTrue(restored.isLoaded(), "Cannon load lost on save");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shipRecipesAndRoofShapes(GameTestHelper helper) {
        for (var wood : TFCWood.values()) {
            for (String type : new String[]{"rowboat", "construction_sloop"}) {
                var id = ResourceLocation.fromNamespaceAndPath("firmaciv", "entity_multiblock/" + type + "/" + wood.getSerializedName());
                var recipe = (EntityMultiblockRecipe) helper.getLevel().getRecipeManager().byKey(id).orElseThrow().value();
                helper.assertTrue(recipe.getEntityResult().createEntity(helper.getLevel()).isPresent(), "Ship recipe cannot create entity");
            }
            for (var state : FirmacivBlocks.WOOD_ROOFING.get(wood.wood).get().getStateDefinition().getPossibleStates()) {
                helper.assertTrue(!state.getShape(helper.getLevel(), BlockPos.ZERO).isEmpty(), "Empty roof shape");
            }
        }
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void assembleAllShipFrames(GameTestHelper helper) {
        for (var wood : TFCWood.values()) {
            for (String type : new String[]{"rowboat", "construction_sloop"}) {
                var id = ResourceLocation.fromNamespaceAndPath("firmaciv", "entity_multiblock/" + type + "/" + wood.getSerializedName());
                var recipe = (EntityMultiblockRecipe) helper.getLevel().getRecipeManager().byKey(id).orElseThrow().value();
                var origin = helper.absolutePos(new BlockPos(3, 2, 3));
                recipe.getPattern().place(helper.getLevel(), origin, 0, Rotation.NONE);
                var root = origin;
                for (var element : recipe.getPattern().shape()) {
                    var candidate = origin.offset(element.position());
                    if (!helper.getLevel().getBlockState(candidate).isAir()) { root = candidate; break; }
                }
                var match = EntityMultiblockRecipe.getBestMatch(helper.getLevel(), root).orElseThrow();
                helper.assertTrue(match.success() && match.multiblockRecipe().id().equals(id), "Frame pattern did not match " + id);
                var replacement = helper.getLevel().getBlockState(root).getFluidState().createLegacyBlock();
                match.assemble(helper.getLevel());
                var entityType = type.equals("rowboat") ? FirmacivEntities.TFC_ROWBOATS.get(wood).get() : FirmacivEntities.TFC_SLOOPS_UNDER_CONSTRUCTION.get(wood).get();
                var entities = helper.getLevel().getEntitiesOfClass(Entity.class, new net.minecraft.world.phys.AABB(origin).inflate(12), e -> e.getType() == entityType);
                helper.assertTrue(entities.size() == 1, "Frame failed to create exactly one boat " + id);
                entities.forEach(Entity::discard);
                helper.assertTrue(helper.getLevel().getBlockState(root).equals(replacement), "Assembly did not remove frames");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void completeSloopConsumesOneItemAtATime(GameTestHelper helper) {
        var ship = FirmacivEntities.TFC_SLOOPS_UNDER_CONSTRUCTION.get(TFCWood.OAK).get().create(helper.getLevel());
        ship.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(6, 2, 6)));
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var construction = ship.getConstructionInput().value();
        int steps = 0;
        while (!ship.isFinished() && steps++ < 256) {
            var ingredient = construction.getIngredient(ship.getConstructionState().inputStage());
            var stack = ingredient.getItems()[0].copyWithCount(64);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
            helper.assertTrue(ship.interactFromConstructionEntity(player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(), "Construction rejected material");
            helper.assertTrue(player.getMainHandItem().getCount() == 63, "Construction consumed a whole stack");
        }
        helper.assertTrue(ship.isFinished() && ship.isRemoved(), "Sloop construction did not finish");
        helper.assertEntityPresent(FirmacivEntities.TFC_SLOOPS.get(TFCWood.OAK).get());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void breakingSealedBarrelPreservesCargo(GameTestHelper helper) {
        var barrel = new TFCBarrelCompartmentEntity(FirmacivEntities.TFC_BARREL_COMPARTMENT_ENTITY.get(), helper.getLevel(), new ItemStack(Wood.OAK.getBlock(Wood.BlockType.BARREL).get()));
        barrel.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(5, 2, 5)));
        barrel.setItem(BarrelBlockEntity.SLOT_ITEM, new ItemStack(Items.DIAMOND, 3));
        barrel.getFluidHandler(null).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        barrel.onSeal();
        helper.getLevel().addFreshEntity(barrel);
        barrel.hurt(helper.getLevel().damageSources().generic(), 100);
        var drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, barrel.getBoundingBox().inflate(3));
        helper.assertTrue(drops.size() == 1, "Sealed barrel spilled or duplicated contents");
        var restored = new TFCBarrelCompartmentEntity(FirmacivEntities.TFC_BARREL_COMPARTMENT_ENTITY.get(), helper.getLevel(), drops.getFirst().getItem());
        helper.assertTrue(restored.isSealed() && restored.getItem(BarrelBlockEntity.SLOT_ITEM).getCount() == 3, "Broken barrel lost cargo");
        helper.assertTrue(restored.getFluidHandler(null).getFluidInTank(0).getAmount() == 1000, "Broken barrel lost fluid");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void craftingIngredientsResolve(GameTestHelper helper) {
        for (var recipe : helper.getLevel().getRecipeManager().getRecipes()) {
            if (!recipe.id().getNamespace().equals("firmaciv")) continue;
            for (var ingredient : recipe.value().getIngredients()) {
                helper.assertTrue(ingredient.isEmpty() || !ingredient.hasNoItems(), "Unusable ingredient in " + recipe.id());
            }
        }
        for (var wood : TFCWood.values()) {
            var id = ResourceLocation.fromNamespaceAndPath("tfc", "crafting/wood/boat/" + wood.getSerializedName());
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(id).isEmpty(), "Vanilla TFC boat recipe still enabled: " + id);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 240)
    public static void boatsFloatInTfcWater(GameTestHelper helper) {
        var fluids = new net.minecraft.world.level.material.Fluid[]{
            Fluids.WATER,
            net.dries007.tfc.common.fluids.TFCFluids.SALT_WATER.getSource(),
            net.dries007.tfc.common.fluids.TFCFluids.RIVER_WATER.get(),
            net.dries007.tfc.common.fluids.TFCFluids.SPRING_WATER.getSource()
        };
        var origin = helper.absolutePos(new BlockPos(8, 1, 8));
        for (var fluid : fluids) {
            var state = fluid.defaultFluidState().createLegacyBlock();
            for (var pos : BlockPos.betweenClosed(origin.offset(-6, 0, -6), origin.offset(6, 4, 6))) {
                helper.getLevel().setBlock(pos, state, 2 | 16);
            }
            var boats = new com.alekiponi.alekiships.common.entity.vehicle.AbstractAlekiBoatEntity<?>[]{
                FirmacivEntities.KAYAK_ENTITY.get().create(helper.getLevel()),
                FirmacivEntities.TFC_CANOES.get(TFCWood.OAK).get().create(helper.getLevel()),
                FirmacivEntities.TFC_ROWBOATS.get(TFCWood.OAK).get().create(helper.getLevel()),
                FirmacivEntities.TFC_SLOOPS.get(TFCWood.OAK).get().create(helper.getLevel())
            };
            for (var boat : boats) {
                var label = boat.getType().toShortString() + " in " + net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid);
                double surface = origin.getY() + 4 + fluid.defaultFluidState().getHeight(helper.getLevel(), origin.above(4));
                boat.setPos(origin.getX() + 0.5, surface - 0.2, origin.getZ() + 0.5);
                helper.assertTrue(boat.getStatus() == com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle.MediumStatus.IN_WATER, "Water not recognized: " + label);
                // Exercise the actual vertical physics; keep horizontal wind drift inside the test pool.
                boat.setPos(boat.getX(), surface + 0.3, boat.getZ());
                double lowest = Double.POSITIVE_INFINITY, highest = Double.NEGATIVE_INFINITY;
                for (int tick = 0; tick < 400; tick++) {
                    boat.tickCount++; // Normally advanced by ServerLevel before Entity.tick().
                    boat.setImmobile(false);
                    boat.tick();
                    boat.setPos(origin.getX() + 0.5, boat.getY(), origin.getZ() + 0.5);
                    helper.assertTrue(!boat.isRemoved() && boat.getY() > surface - 1, "Boat sank: " + label + " at tick " + tick);
                    if (tick >= 100) {
                        lowest = Math.min(lowest, boat.getY()); highest = Math.max(highest, boat.getY());
                    }
                }
                org.slf4j.LoggerFactory.getLogger(PortGameTests.class).info("BUOYANCY {} range={} min={} max={}", label, highest - lowest, lowest, highest);
                helper.assertTrue(highest - lowest < 0.02, "Unstable buoyancy: " + label + " range=" + (highest - lowest));
                helper.assertTrue(boat.getY() < surface + 0.3 && boat.getY() > surface - 0.8, "Boat did not settle on surface: " + label + " y=" + boat.getY() + " surface=" + surface);
                boat.getIndirectPassengers().forEach(Entity::discard);
                boat.discard();
            }
        }
        helper.succeed();
    }

}
