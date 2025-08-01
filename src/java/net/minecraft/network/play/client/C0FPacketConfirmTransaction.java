package net.minecraft.network.play.client;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.io.IOException;

public class C0FPacketConfirmTransaction implements Packet<INetHandlerPlayServer>
{
    private int windowId;
    private short uid;
    private boolean accepted;

    public C0FPacketConfirmTransaction()
    {
    }

    public C0FPacketConfirmTransaction(int windowId, short uid, boolean accepted)
    {
        this.windowId = windowId;
        this.uid = uid;
        this.accepted = accepted;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.processConfirmTransaction(this);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.windowId = buf.readByte();
        this.uid = buf.readShort();
        this.accepted = buf.readByte() != 0;
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            buf.writeInt(this.windowId);
        } else {
            buf.writeByte(this.windowId);
            buf.writeShort(this.uid);
            buf.writeByte(this.accepted ? 1 : 0);
        }
    }

    public int getWindowId()
    {
        return this.windowId;
    }

    public short getUid()
    {
        return this.uid;
    }

    public void setWindowId(int windowId) {
        this.windowId = windowId;
    }

    public void setUid(short uid) {
        this.uid = uid;
    }


    @Override
    public int getID() {
        return 5;
    }
}
