package dev.tenacity.event.impl.network;


import dev.tenacity.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;

@Setter
@Getter
public class PacketEvent extends CancellableEvent {
    private final PacketEventType type;
    private Packet<?> packet;

    public PacketEvent(Packet<?> packet, PacketEventType type) {
        this.type = type;
        this.packet = packet;
    }


    public int getPacketID() {
        return getPacket().getID();
    }
    public enum PacketEventType {
        SEND, RECEIVE
    }
}
