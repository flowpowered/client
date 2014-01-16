/**
 * This file is part of Client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spoutcraft <http://spoutcraft.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spoutcraft.client.network.codec.play;

import java.io.IOException;

import com.flowpowered.networking.Codec;
import com.flowpowered.networking.MessageHandler;
import io.netty.buffer.ByteBuf;
import org.spoutcraft.client.network.ClientSession;
import org.spoutcraft.client.network.message.ChannelMessage;
import org.spoutcraft.client.network.message.play.ChunkDataBulkMessage;

public class ChunkDataBulkCodec extends Codec<ChunkDataBulkMessage> implements MessageHandler<ClientSession, ChunkDataBulkMessage> {
    public ChunkDataBulkCodec() {
        super(ChunkDataBulkMessage.class);
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
            primaryBitMaps[i] = (short) buf.readUnsignedShort();
            additionalDataBitMaps[i] = (short) buf.readUnsignedShort();
        }
        return new ChunkDataBulkMessage(columnCount, compressedDataLength, hasSkyLight, compressedData, columnXs, columnZs, primaryBitMaps, additionalDataBitMaps);
    }

    @Override
    public ByteBuf encode(ByteBuf buf, ChunkDataBulkMessage message) throws IOException {
        throw new IOException("The client cannot send a chunk data bulk to the Minecraft server!");
    }

    @Override
    public void handle(ClientSession session, ChunkDataBulkMessage message) {
        session.getGame().getNetwork().offer(ChannelMessage.Channel.UNIVERSE, message);
    }
}
