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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.flowpowered.networking.session.PulsingSession;
import org.spoutcraft.client.Game;
import org.spoutcraft.client.networking.message.ChannelMessage;
import org.spoutcraft.client.networking.message.login.LoginSuccessMessage;
import org.spoutcraft.client.networking.protocol.PlayProtocol;
import org.spoutcraft.client.ticking.TickingElement;

public class Network extends TickingElement {
    private final Game game;
    private final GameNetworkClient client;
    private final EnumMap<ChannelMessage.Channel, ConcurrentLinkedQueue<ChannelMessage>> messageQueue = new EnumMap<>(ChannelMessage.Channel.class);

    public Network(Game game) {
        super(20);
        this.game = game;
        client = new GameNetworkClient(game);
        messageQueue.put(ChannelMessage.Channel.UNIVERSE, new ConcurrentLinkedQueue<ChannelMessage>());
        messageQueue.put(ChannelMessage.Channel.NETWORK, new ConcurrentLinkedQueue<ChannelMessage>());
    }

    @Override
    public void onTick() {
        if (client.getSession() == null) {
            return;
        }
        for (Map.Entry<ChannelMessage.Channel, ConcurrentLinkedQueue<ChannelMessage>> entry : messageQueue.entrySet()) {
            final Iterator<ChannelMessage> messages = entry.getValue().iterator();
            while (messages.hasNext()) {
                final ChannelMessage message = messages.next();
                //Handle all Network channel message
                if (entry.getKey() == ChannelMessage.Channel.NETWORK && !message.isFullyRead()) {
                    processMessage(message);
                }
                if (message.isFullyRead()) {
                    messages.remove();
                }
            }
        }

        //Pulse session for new messages
        client.getSession().pulse();
    }

    @Override
    public void onStart() {
        System.out.println("Network start");
    }

    @Override
    public void onStop() {
        System.out.println("Network stop");

        client.shutdown();
    }

    public boolean connect() {
        return connect(new InetSocketAddress(25565));
    }

    public boolean connect(SocketAddress address) {
        Future<Void> future = client.connect(address);
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            return false;
        }
        return true;
    }

    public Game getGame() {
        return game;
    }

    public ClientSession getSession() {
        return client.getSession();
    }

    /**
     * Gets the {@link java.util.Iterator} storing the messages for the {@link org.spoutcraft.client.networking.message.ChannelMessage.Channel}
     * @param c See {@link org.spoutcraft.client.networking.message.ChannelMessage.Channel}
     * @return The iterator
     */
    public Iterator<ChannelMessage> getChannel(ChannelMessage.Channel c) {
        return messageQueue.get(c).iterator();
    }

    /**
     * Offers a {@link org.spoutcraft.client.networking.message.ChannelMessage} to a queue mapped to {@link org.spoutcraft.client.networking.message.ChannelMessage.Channel}
     * @param c See {@link org.spoutcraft.client.networking.message.ChannelMessage.Channel}
     * @param m See {@link org.spoutcraft.client.networking.message.ChannelMessage}
     */
    public void offer(ChannelMessage.Channel c, ChannelMessage m) {
        messageQueue.get(c).offer(m);
    }

    /**
     * Processes the next {@link org.spoutcraft.client.networking.message.ChannelMessage} in the network pipeline
     * @param message See {@link org.spoutcraft.client.networking.message.ChannelMessage}
     */
    public void processMessage(ChannelMessage message) {
        if (message.getClass() == LoginSuccessMessage.class) {
            final LoginSuccessMessage loginSuccessMessage = (LoginSuccessMessage) message;
            System.out.println("Server says login is successful...Woo!!");

            ClientSession session = getSession();
            session.setProtocol(new PlayProtocol());
            session.setUUID(loginSuccessMessage.getUUID());
            session.setUsername(loginSuccessMessage.getUsername());
            session.setState(PulsingSession.State.OPEN);
            message.markChannelRead(ChannelMessage.Channel.NETWORK);
        }
    }
}
