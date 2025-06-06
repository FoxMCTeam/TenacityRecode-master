/*
 * This file is part of ViaLoadingBase - https://github.com/FlorianMichael/ViaLoadingBase
 * Copyright (C) 2023 FlorianMichael/EnZaXD and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.tenacity.utils.client.addons.viamcp.vialoadingbase.netty.handler;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.types.VarIntType;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelDecoderException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

@ChannelHandler.Sharable
public class VLBViaDecodeHandler extends MessageToMessageDecoder<ByteBuf> {
    public static int stateId;
    private final UserConnection user;

    public VLBViaDecodeHandler(final UserConnection user) {
        super();
        this.user = user;
    }

    @Override
    protected void decode(final ChannelHandlerContext channelHandlerContext, final ByteBuf byteBuf, final List<Object> list) throws Exception {
        if (!this.user.checkIncomingPacket()) {
            throw CancelDecoderException.generate(null);
        }
        if (!this.user.shouldTransformPacket()) {
            list.add(byteBuf.retain());
            return;
        }
        final ByteBuf writeBytes = channelHandlerContext.alloc().buffer().writeBytes(byteBuf);
        final ByteBuf copy = writeBytes.copy();
        try {
            copy.markReaderIndex();
            final int primitive = new VarIntType().readPrimitive(copy);
            this.user.transformIncoming(writeBytes, CancelDecoderException::generate);
            list.add(writeBytes.retain());
            if (primitive == 20 || primitive == 22) {
                copy.readUnsignedByte();
                VLBViaDecodeHandler.stateId = new VarIntType().readPrimitive(copy);
            }
        } finally {
            writeBytes.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (PipelineUtil.containsCause(cause, CancelCodecException.class))
            return;

        if ((PipelineUtil.containsCause(cause, InformativeException.class) && user.getProtocolInfo().getServerState() != State.HANDSHAKE) || Via.getManager().debugHandler().enabled())
            throw new RuntimeException(cause);
    }
}