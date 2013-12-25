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

import com.flowpowered.networking.protocol.Protocol;
import com.flowpowered.networking.session.PulsingSession;
import io.netty.channel.Channel;
import org.spoutcraft.client.networking.message.handshake.HandshakeMessage;
import org.spoutcraft.client.networking.message.login.LoginStartMessage;
import org.spoutcraft.client.networking.protocol.ClientProtocol;
import org.spoutcraft.client.networking.protocol.LoginProtocol;

/**
 * Represents an open connection to the server. All {@link com.flowpowered.networking.Message}s are sent through the session
 */
public class ClientSession extends PulsingSession {
    private String uuid;
    private String username;

    public ClientSession(Channel channel, Protocol protocol) {
        super(channel, protocol);
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void setProtocol(Protocol protocol) {
        super.setProtocol(protocol);
    }

    @Override
    public void onReady() {
        send(SendType.FORCE, new HandshakeMessage("localhost", ClientProtocol.DEFAULT_PORT, HandshakeMessage.HandshakeState.LOGIN));
        setProtocol(new LoginProtocol());
        send(SendType.FORCE, new LoginStartMessage("Spoutcrafty"));
    }
}
