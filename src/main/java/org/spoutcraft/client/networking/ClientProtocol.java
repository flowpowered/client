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
import com.flowpowered.networking.protocol.HandlerLookupService;
import com.flowpowered.networking.protocol.Protocol;
import io.netty.buffer.ByteBuf;

/**
 * The protocol used for client communication.
 */
public class ClientProtocol extends Protocol {
    private final ClientHandlerLookupService service;

    public ClientProtocol(String name, int defaultPort) {
        super(name, defaultPort, 50);
        service = new ClientHandlerLookupService();

        //TODO Bind messages to handlers here
    }

    @Override
    public Codec<?> readHeader(ByteBuf byteBuf) throws UnknownPacketException {
        return null;
    }

    @Override
    public ByteBuf writeHeader(Codec<?> codec, ByteBuf byteBuf, ByteBuf byteBuf2) {
        return null;
    }
}

final class ClientHandlerLookupService extends HandlerLookupService {
    protected ClientHandlerLookupService() {
        //TODO Bind messages to handlers here
        //bind()
    }
}
