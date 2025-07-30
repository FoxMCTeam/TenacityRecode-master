package dev.tenacity.utils.packet;

import dev.tenacity.utils.Utils;
import dev.tenacity.utils.server.PacketUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author d3Ck
 * @since 06/13/2025
 * a bullshit using to collect shit
 */
@Getter
@Setter
public class PacketCollector implements Utils {
    private final int min, max;
    private int count;
    private LinkedBlockingQueue<Packet<?>> packets;
    private CollectorType state;

    public PacketCollector(int min, int max) {
        this.min = min;
        this.max = max;
        this.count = min;
        this.state = CollectorType.READY;
        this.packets = new LinkedBlockingQueue<>();
    }

    public void add(Packet<?> packet) {
        this.state = CollectorType.COLLECTING;
        this.packets.add(packet);
        this.count++;
    }

    public void remove(Packet<?> index) {
        this.state = CollectorType.REMOVING;
        this.packets.remove(index);
        this.count--;
        this.state = CollectorType.READY;
    }
    public void removeAll() {
        this.state = CollectorType.REMOVING;
        for (Packet<?> packet : this.packets) {
            this.packets.remove(packet);
        }
        this.count = min;
        this.state = CollectorType.READY;
    }
    public void releasePackets(CollectorType type) {
        this.state = CollectorType.RELEASING;
        for (Packet<?> packet : packets) {
            switch (type) {
                case NO_EVENT -> PacketUtils.sendPacket(packet, true);
                case NORMAL -> PacketUtils.sendPacket(packet, false);
            }
        }
    }
}
