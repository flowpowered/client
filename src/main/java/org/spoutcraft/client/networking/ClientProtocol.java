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
package org.spoutcraft.client.networking;

import com.flowpowered.networking.Codec;
import com.flowpowered.networking.exception.UnknownPacketException;
import com.flowpowered.networking.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import org.spoutcraft.client.networking.codec.HandshakeCodec;
import org.spoutcraft.client.networking.codec.LoginStartCodec;
import org.spoutcraft.client.networking.codec.LoginSuccessCodec;
import org.spoutcraft.client.networking.handler.HandshakeHandler;
import org.spoutcraft.client.networking.handler.KeepAliveHandler;
import org.spoutcraft.client.networking.handler.LoginStartHandler;
import org.spoutcraft.client.networking.handler.LoginSuccessHandler;

/**
 * The protocol used for client communication.
 */
public class ClientProtocol extends Protocol {
    public static final int VERSION = 4;

    public ClientProtocol(String name, int defaultPort) {
        super(name, defaultPort, 50);
        //TODO Put handlers here
        registerMessage(HandshakeCodec.class, HandshakeHandler.class);
        registerMessage(LoginStartCodec.class, LoginStartHandler.class);
        registerMessage(LoginSuccessCodec.class, LoginSuccessHandler.class);
        registerMessage(KeepAliveCodec.class, KeepAliveHandler.class);
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
        final int opcode;
        //TODO Merely for testing, I need to get kitskub to make changes to flow-networking.
        if (codec instanceof LoginStartCodec) {
            opcode = 0;
        } else {
            opcode = codec.getOpcode();
        }
        out.writeInt(length);
        out.writeInt(opcode);
        return out;
    }
}
