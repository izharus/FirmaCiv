package com.alekiponi.firmaciv.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class PacketHandler {
    public static void init(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(ClientBoundBarrelCompartmentUpdatePacket.TYPE,
            ClientBoundBarrelCompartmentUpdatePacket.CODEC,
            (payload, context) -> context.enqueueWork(payload::handle));
    }
}
