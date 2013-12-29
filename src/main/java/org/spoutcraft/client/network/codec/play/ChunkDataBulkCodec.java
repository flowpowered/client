package org.spoutcraft.client.network.codec.play;

import java.io.IOException;

import com.flowpowered.networking.Codec;
import com.flowpowered.networking.MessageHandler;
import com.flowpowered.networking.session.Session;
import io.netty.buffer.ByteBuf;
import org.spoutcraft.client.network.ClientSession;
import org.spoutcraft.client.network.message.ChannelMessage;
import org.spoutcraft.client.network.message.play.ChunkDataBulkMessage;

public class ChunkDataBulkCodec extends Codec<ChunkDataBulkMessage> implements MessageHandler<ChunkDataBulkMessage> {
    private static final int OP_CODE = 26;

    public ChunkDataBulkCodec() {
        super(ChunkDataBulkMessage.class, OP_CODE);
    }

    @Override
    public ChunkDataBulkMessage decode(ByteBuf buf) throws IOException {
        final short columnCount = buf.readShort();
        final int compressedDataLength = buf.readInt();
        final boolean hasSkyLight = buf.readBoolean();
        final byte[] compressedData = new byte[compressedDataLength];
        buf.readBytes(compressedData);
        final int[] columnXs = new int[columnCount];
        final int[] columnZs = new int[columnCount];
        final short[] primaryBitMaps = new short[columnCount];
        final short[] additionalDataBitMaps = new short[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columnXs[i] = buf.readInt();
            columnZs[i] = buf.readInt();
            primaryBitMaps[i] = buf.readShort();
            additionalDataBitMaps[i] = buf.readShort();
        }
        return new ChunkDataBulkMessage(columnCount, compressedDataLength, hasSkyLight, compressedData, columnXs, columnZs, primaryBitMaps, additionalDataBitMaps);
    }

    @Override
    public ByteBuf encode(ByteBuf buf, ChunkDataBulkMessage message) throws IOException {
        throw new IOException("The client cannot send a chunk data bulk to the Minecraft server!");
    }

    @Override
    public void handle(Session session, ChunkDataBulkMessage message) {
        ((ClientSession) session).getGame().getNetwork().offer(ChannelMessage.Channel.UNIVERSE, message);
    }
}
