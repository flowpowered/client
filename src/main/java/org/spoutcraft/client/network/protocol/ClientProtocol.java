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
package org.spoutcraft.client.network.protocol;

import java.io.IOException;

import com.flowpowered.networking.ByteBufUtils;
import com.flowpowered.networking.Codec;
import com.flowpowered.networking.Message;
import com.flowpowered.networking.MessageHandler;
import com.flowpowered.networking.exception.IllegalOpcodeException;
import com.flowpowered.networking.exception.UnknownPacketException;
import com.flowpowered.networking.protocol.keyed.KeyedProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.spoutcraft.client.Game;

public class ClientProtocol extends KeyedProtocol {
    /**
     * From Server
     */
    protected static final String INBOUND = "INBOUND";
    /**
     * To Server
     */
    protected static final String OUTBOUND = "OUTBOUND";
    /**
     * The server's default port.
     */
    public static final int DEFAULT_PORT = 25565;
    /**
     * The server's protocol version.
     */
    public static final int VERSION = 4;
    private final Game game;

    public ClientProtocol(Game game, String name, int highestOpcode) {
        super(name, DEFAULT_PORT, highestOpcode + 1);
        this.game = game;
    }

    @Override
    public <M extends Message> MessageHandler<?, M> getMessageHandle(Class<M> clazz) {
        return getHandlerLookupService(INBOUND).find(clazz);
    }

    @Override
    public Codec<?> readHeader(ByteBuf buf) throws UnknownPacketException {
        int length = -1;
        int opcode = -1;
        try {
            length = ByteBufUtils.readVarInt(buf);
            opcode = ByteBufUtils.readVarInt(buf);
            return getCodecLookupService(INBOUND).find(opcode).getCodec();
        } catch (IOException e) {
            throw new UnknownPacketException("Failed to read packet data (corrupt?)", opcode, length);
        } catch (IllegalOpcodeException e) {
            throw new UnknownPacketException("Opcode received is not a registered codec on the client!", opcode, length);
        }
    }

    @Override
    public <M extends Message> Codec.CodecRegistration getCodecRegistration(Class<M> clazz) {
        return getCodecLookupService(OUTBOUND).find(clazz);
    }

    @Override
    public ByteBuf writeHeader(Codec.CodecRegistration codec, ByteBuf data, ByteBuf out) {
        final int length = data.readableBytes();
        final ByteBuf opcodeBuffer = Unpooled.buffer();
        ByteBufUtils.writeVarInt(opcodeBuffer, codec.getOpcode());
        ByteBufUtils.writeVarInt(out, length + opcodeBuffer.readableBytes());
        ByteBufUtils.writeVarInt(out, codec.getOpcode());
        return out;
    }

    public Game getGame() {
        return game;
    }
}
