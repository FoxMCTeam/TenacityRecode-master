package dev.tenacity.event.impl.network;

import dev.tenacity.event.Event;
import net.minecraft.network.Packet;

public class PacketEvent extends Event {
    private Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    
    public Packet<?> getPacket() {
        return packet;
    }

    
    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    
    public int getPacketID() {
        return getPacket().getID();
    }

}
