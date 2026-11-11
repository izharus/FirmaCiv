package com.alekiponi.firmaciv.util;

import com.alekiponi.alekiships.wind.*;
import com.alekiponi.alekiships.common.AlekiShipsRegistries;
import com.mojang.serialization.MapCodec;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public enum TFCWindModel implements WindModel {
    INSTANCE;
    public static final DeferredRegister<WindModelSerializer<?>> SERIALIZERS = DeferredRegister.create(AlekiShipsRegistries.WIND_MODEL_SERIALIZERS, "firmaciv");
    public static final DeferredHolder<WindModelSerializer<?>, WindModelSerializer<TFCWindModel>> TYPE = SERIALIZERS.register("tfc", () -> new WindModelSerializer<>() {
        public MapCodec<TFCWindModel> codec() { return MapCodec.unit(INSTANCE); }
        public StreamCodec<RegistryFriendlyByteBuf, TFCWindModel> streamCodec() { return StreamCodec.unit(INSTANCE); }
    });
    @Override public WindModelSerializer<?> getSerializer() { return TYPE.get(); }
    @Override public Wind getWind(Level level, double x, double y, double z) {
        var wind = Climate.get(level).getWind(level, BlockPos.containing(x, y, z));
        return Wind.of((float) Math.hypot(wind.x, wind.y), (float) Math.toDegrees(Math.atan2(wind.y, wind.x)));
    }
}
