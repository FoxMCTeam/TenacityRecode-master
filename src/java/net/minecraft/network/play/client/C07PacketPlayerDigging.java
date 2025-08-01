package net.minecraft.network.play.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.io.IOException;

@Setter
public class C07PacketPlayerDigging implements Packet<INetHandlerPlayServer> {
    private BlockPos position;
    private EnumFacing facing;

    /**
     * Status of the digging (started, ongoing, broken).
     */
    private C07PacketPlayerDigging.Action status;

    public C07PacketPlayerDigging() {
    }

    public C07PacketPlayerDigging(C07PacketPlayerDigging.Action statusIn, BlockPos posIn, EnumFacing facingIn) {
        this.status = statusIn;
        this.position = posIn;
        this.facing = facingIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.status = (C07PacketPlayerDigging.Action) buf.readEnumValue(C07PacketPlayerDigging.Action.class);
        this.position = buf.readBlockPos();
        this.facing = EnumFacing.getFront(buf.readUnsignedByte());
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(this.status);
        buf.writeBlockPos(this.position);
        buf.writeByte(this.facing.getIndex());
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler) {
        handler.processPlayerDigging(this);
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public EnumFacing getFacing() {
        return this.facing;
    }

    public C07PacketPlayerDigging.Action getStatus() {
        return this.status;
    }

    @Getter
    @AllArgsConstructor
    public enum Action {
        START_DESTROY_BLOCK(0),
        ABORT_DESTROY_BLOCK(1),
        STOP_DESTROY_BLOCK(2),
        DROP_ALL_ITEMS(3),
        DROP_ITEM(4),
        RELEASE_USE_ITEM(5);
        private final int id;
    }

    @Override
    public int getID() {
        return 13;
    }
}
