package org.spoutcraft.client.networking;

import java.io.IOException;

import com.flowpowered.networking.Codec;
import io.netty.buffer.ByteBuf;
import org.spoutcraft.client.networking.message.KeepAliveMessage;

public class KeepAliveCodec extends Codec<KeepAliveMessage> {
    public static final int OPCODE = 0;

    public KeepAliveCodec() {
        super(KeepAliveMessage.class, OPCODE);
    }

    @Override
    public KeepAliveMessage decode(ByteBuf buf) throws IOException {
        return new KeepAliveMessage(buf.readInt());
    }

    @Override
    public ByteBuf encode(ByteBuf buf, KeepAliveMessage message) throws IOException {
        buf.writeInt(message.getRandom());
        return buf;
    }
}
