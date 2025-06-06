package com.cubk.event.impl.network;


import com.cubk.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;

@Setter
@Getter
public class PacketEvent extends CancellableEvent {
    private Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }


    public int getPacketID() {
        return getPacket().getID();
    }

}
