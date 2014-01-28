/**
 * This file is part of Client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013-2014 Spoutcraft <http://spoutcraft.org/>
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
import io.netty.buffer.ByteBuf;
import org.spoutcraft.client.network.message.play.ChunkDataMessage;

public class ChunkDataCodec implements Codec<ChunkDataMessage> {
    @Override
    public ChunkDataMessage decode(ByteBuf buf) throws IOException {
        final int x = buf.readInt();
        final int z = buf.readInt();
        final boolean groundUpContinuous = buf.readBoolean();
        final short primaryBitMap = (short) buf.readUnsignedShort();
        final short additionalDataBitMap = (short) buf.readUnsignedShort();
        final int compressedSize = buf.readInt();
        final byte[] compressedData = new byte[compressedSize];
        buf.readBytes(compressedData);
        return new ChunkDataMessage(x, z, groundUpContinuous, primaryBitMap, additionalDataBitMap, compressedSize, compressedData);
    }

    @Override
    public void encode(ByteBuf buf, ChunkDataMessage message) throws IOException {
        throw new IOException("The client cannot send a chunk data to the Minecraft server!");
    }
}