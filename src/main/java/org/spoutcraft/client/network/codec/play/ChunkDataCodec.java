package org.spoutcraft.client.network.codec.play;

import java.io.IOException;

import com.flowpowered.networking.Codec;
import com.flowpowered.networking.MessageHandler;
import com.flowpowered.networking.session.Session;
import io.netty.buffer.ByteBuf;
import org.spoutcraft.client.network.ClientSession;
import org.spoutcraft.client.network.message.ChannelMessage;
import org.spoutcraft.client.network.message.play.ChunkDataMessage;

public class ChunkDataCodec extends Codec<ChunkDataMessage> implements MessageHandler<ChunkDataMessage> {
    private static final int OP_CODE = 21;

    public ChunkDataCodec() {
        super(ChunkDataMessage.class, OP_CODE);
    }

    @Override
    public ChunkDataMessage decode(ByteBuf buf) throws IOException {
        final int x = buf.readInt();
        final int z = buf.readInt();
        final boolean groundUpContinuous = buf.readBoolean();
        final int primaryBitMap = buf.readUnsignedShort();
        final int addBitMap = buf.readUnsignedShort();
        final int compressedSize = buf.readInt();
        final byte[] compressedData = new byte[compressedSize];
        buf.readBytes(compressedData);
        return new ChunkDataMessage(x, z, groundUpContinuous, (short) primaryBitMap, (short) addBitMap, compressedSize, compressedData);
    }

    @Override
    public ByteBuf encode(ByteBuf buf, ChunkDataMessage message) throws IOException {
        throw new IOException("The client cannot send a chunk data to the Minecraft server!");
    }

    @Override
    public void handle(Session session, ChunkDataMessage message) {
        ((ClientSession) session).getGame().getNetwork().offer(ChannelMessage.Channel.UNIVERSE, message);
    }
}
