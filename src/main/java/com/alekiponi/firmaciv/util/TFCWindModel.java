package com.alekiponi.firmaciv.util;

import com.alekiponi.alekiships.wind.Wind;
import com.alekiponi.alekiships.wind.WindModel;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;

public class TFCWindModel implements WindModel {

    private Level level;

    public TFCWindModel(Level level) {
        this.level = level;
    }

    @Override
    public Wind getWind(final BlockPos blockPos) {
        Vec2 wind = Climate.getWindVector(level, blockPos);
        return Wind.fromVec(wind);
    }
}
