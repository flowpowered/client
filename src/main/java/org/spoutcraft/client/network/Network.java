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
package org.spoutcraft.client.network;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.flowpowered.commons.ticking.TickingElement;
import com.flowpowered.networking.util.AnnotatedMessageHandler;
import com.flowpowered.networking.util.AnnotatedMessageHandler.Handle;

import org.spoutcraft.client.Game;
import org.spoutcraft.client.network.message.ChannelMessage;
import org.spoutcraft.client.network.message.ChannelMessage.Channel;
import org.spoutcraft.client.network.message.login.LoginSuccessMessage;
import org.spoutcraft.client.network.message.play.KeepAliveMessage;
import org.spoutcraft.client.network.protocol.ClientProtocol;
import org.spoutcraft.client.network.protocol.PlayProtocol;

/**
 * The main network component and thread. Ticks at 20 TPS.
 */
public class Network extends TickingElement {
    private static final int TPS = 20;
    private final Game game;
    private final GameNetworkClient client;
    private final AnnotatedMessageHandler handler;
    private final Map<Channel, ConcurrentLinkedQueue<ChannelMessage>> messageQueue = new EnumMap<>(Channel.class);

    /**
     * Constructs a new game network from the game.
     *
     * @param game The game
     */
    public Network(Game game) {
        super("network", TPS);
        this.game = game;
        client = new GameNetworkClient(game);
        handler = new AnnotatedMessageHandler(this);
        messageQueue.put(Channel.UNIVERSE, new ConcurrentLinkedQueue<ChannelMessage>());
        messageQueue.put(Channel.PHYSICS, new ConcurrentLinkedQueue<ChannelMessage>());
    }

    @Override
    public void onStart() {
        game.getLogger().info("Starting network");

        connect();
    }

    @Override
    public void onTick(long dt) {
    }

    @Override
    public void onStop() {
        game.getLogger().info("Stopping network");

        client.shutdown();
    }

    /**
     * Attempts to connect the network.
     */
    private void connect() {
        connect(new InetSocketAddress(ClientProtocol.DEFAULT_PORT));
    }

    /**
     * Attempts to connect the network using the provided socket address.
     *
     * @param address The socket address
     */
    private void connect(SocketAddress address) {
        if (!isRunning()) {
            throw new RuntimeException("Attempt made to issue a connection but the Network thread isn't running!");
        }
        client.connect(address);
    }

    /**
     * Returns the game associated to the network.
     *
     * @return The game
     */
    public Game getGame() {
        return game;
    }

    /**
     * Returns the network's client session.
     *
     * @return The client session
     */
    public ClientSession getSession() {
        return client.getSession();
    }

    /**
     * Gets the {@link java.util.Iterator} storing the messages for the {@link org.spoutcraft.client.network.message.ChannelMessage.Channel}
     *
     * @param c See {@link org.spoutcraft.client.network.message.ChannelMessage.Channel}
     * @return The iterator
     */
    public Iterator<ChannelMessage> getChannel(Channel c) {
        return messageQueue.get(c).iterator();
    }

    /**
     * Offers a {@link org.spoutcraft.client.network.message.ChannelMessage} to a queue mapped to {@link org.spoutcraft.client.network.message.ChannelMessage.Channel}
     *
     * @param c See {@link org.spoutcraft.client.network.message.ChannelMessage.Channel}
     * @param m See {@link org.spoutcraft.client.network.message.ChannelMessage}
     */
    public void offer(Channel c, ChannelMessage m) {
        if (c == Channel.NETWORK) {
            handler.handle(m);
        } else {
            messageQueue.get(c).offer(m);
        }
    }

    @Handle
    private void handleLoginSuccess(LoginSuccessMessage message) {
        getSession().setProtocol(new PlayProtocol(game));
        getSession().setUUID(message.getUUID());
        getSession().setUsername(message.getUsername());
    }

    @Handle
    private void handleKeepAlive(KeepAliveMessage message) {
        getSession().send(new KeepAliveMessage(message.getRandom()));
    }
}
