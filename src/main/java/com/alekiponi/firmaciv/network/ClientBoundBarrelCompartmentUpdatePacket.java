package com.alekiponi.firmaciv.network;

import com.alekiponi.alekiships.util.ClientHelper;
import com.alekiponi.firmaciv.common.entity.compartment.TFCBarrelCompartmentEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.alekiponi.firmaciv.Firmaciv;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public final class ClientBoundBarrelCompartmentUpdatePacket implements CustomPacketPayload {
    public static final Type<ClientBoundBarrelCompartmentUpdatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Firmaciv.MOD_ID, "barrel_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundBarrelCompartmentUpdatePacket> CODEC = StreamCodec.ofMember(ClientBoundBarrelCompartmentUpdatePacket::encoder, ClientBoundBarrelCompartmentUpdatePacket::new);
    @Override public Type<ClientBoundBarrelCompartmentUpdatePacket> type() { return TYPE; }


    private final int compartmentID;
    private final FluidStack fluid;
    private final ItemStack visibleStack;

    public ClientBoundBarrelCompartmentUpdatePacket(final TFCBarrelCompartmentEntity barrelCompartment,
            final FluidStack fluid, final ItemStack visibleStack) {
        this.compartmentID = barrelCompartment.getId();
        this.fluid = fluid;
        this.visibleStack = visibleStack;
    }

    ClientBoundBarrelCompartmentUpdatePacket(final RegistryFriendlyByteBuf friendlyByteBuf) {
        this.compartmentID = friendlyByteBuf.readInt();
        this.fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(friendlyByteBuf);
        this.visibleStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(friendlyByteBuf);
    }

    void encoder(final RegistryFriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.compartmentID);
        FluidStack.OPTIONAL_STREAM_CODEC.encode(friendlyByteBuf, this.fluid);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(friendlyByteBuf, this.visibleStack);
    }

    void handle() {
        final Level level = ClientHelper.getLevel();

        if (level == null) return;

        if (level.getEntity(this.compartmentID) instanceof final TFCBarrelCompartmentEntity barrelCompartment) {
            barrelCompartment.syncContents(this.fluid, this.visibleStack);
        }
    }
}