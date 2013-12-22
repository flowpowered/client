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

import java.net.SocketAddress;

import com.flowpowered.networking.NetworkClient;
import com.flowpowered.networking.protocol.Protocol;
import com.flowpowered.networking.session.Session;
import io.netty.channel.Channel;

/**
 * The network entry point for the client. Handles connecting to the server as well as creating {@link Session}s.
 */
public class GameNetworkClient extends NetworkClient {
    private ClientSession session;

    public GameNetworkClient(SocketAddress remoteAddress) {
        super(remoteAddress, new ClientProtocol("Client", 25565));
    }

    @Override
    public Session newSession(Channel channel, Protocol protocol) {
        session = new ClientSession(channel, protocol);
        return session;
    }

    @Override
    public void sessionInactivated(Session session) {
        //TODO Show generic client GUI for disconnection
        session = null;
    }

    public ClientSession getSession() {
        return session;
    }
}
