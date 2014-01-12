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
package org.spoutcraft.client.network;

import java.util.UUID;

import com.flowpowered.networking.protocol.Protocol;
import com.flowpowered.networking.session.BasicSession;
import com.flowpowered.networking.session.PulsingSession;

import io.netty.channel.Channel;

import org.spoutcraft.client.Game;
import org.spoutcraft.client.network.message.handshake.HandshakeMessage;
import org.spoutcraft.client.network.message.login.LoginStartMessage;
import org.spoutcraft.client.network.protocol.ClientProtocol;
import org.spoutcraft.client.network.protocol.LoginProtocol;

/**
 * Represents an open connection to the server. All {@link com.flowpowered.networking.Message}s are sent through the session.
 */
public class ClientSession extends BasicSession {
    private final Game game;
    private UUID uuid;
    private String username;

    /**
     * Constructs a new client session from the game, channel and protocol.
     *
     * @param game The game
     * @param channel The network channel
     * @param protocol The client protocol
     */
    public ClientSession(Game game, Channel channel, Protocol protocol) {
        super(channel, protocol);
        this.game = game;
    }

    /**
     * Returns the ID of the session.
     *
     * @return The session's ID
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Sets the session's ID
     *
     * @param uuid The session ID
     */
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Returns the username of the player for the session.
     *
     * @return The player's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the player's username
     *
     * @param username The player's username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void setProtocol(Protocol protocol) {
        super.setProtocol(protocol);
    }

    @Override
    public void onReady() {
        send(new HandshakeMessage("localhost", ClientProtocol.DEFAULT_PORT, HandshakeMessage.HandshakeState.LOGIN));
        setProtocol(new LoginProtocol());
        send(new LoginStartMessage("Spoutcrafty"));
    }

    /**
     * Returns the game for the session.
     *
     * @return The game
     */
    public Game getGame() {
        return game;
    }
}
