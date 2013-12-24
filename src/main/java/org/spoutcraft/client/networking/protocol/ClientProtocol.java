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
package org.spoutcraft.client.networking.protocol;

import com.flowpowered.networking.Codec;
import com.flowpowered.networking.exception.UnknownPacketException;
import com.flowpowered.networking.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import org.spoutcraft.client.networking.codec.LoginStartCodec;

/**
 * The protocol used for client communication.
 */
public abstract class ClientProtocol extends Protocol {
    public static final int DEFAULT_PORT = 25565;
    public static final int VERSION = 4;

    public ClientProtocol(String name, int messageCount) {
        super(name, DEFAULT_PORT, messageCount);
    }

    @Override
    public Codec<?> readHeader(ByteBuf buf) throws UnknownPacketException {
        int length = buf.readInt();
        int opcode = buf.readInt();

        final Codec<?> codec = getCodecLookupService().find(opcode);
        if (codec == null) {
            throw new UnknownPacketException(length, opcode);
        }
        return codec;
    }

    @Override
    public ByteBuf writeHeader(Codec<?> codec, ByteBuf data, ByteBuf out) {
        //Length -> opcode -> data
        final int length = data.capacity();
        final int opcode = codec.getOpcode();
        out.writeInt(length);
        out.writeInt(opcode);
        return out;
    }
}
