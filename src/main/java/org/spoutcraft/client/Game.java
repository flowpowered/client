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
package org.spoutcraft.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.spoutcraft.client.networking.GameNetworkClient;
import org.spoutcraft.client.networking.NetworkPulser;

/**
 * The game class.
 */
public class Game {
    private final Universe universe = new Universe();
    private final Interface nterface = new Interface();
    private final NetworkPulser pulser = new NetworkPulser();
    private GameNetworkClient network;

    public void start() {
        universe.start();
        nterface.start();
        pulser.start();
    }

    public void stop() {
        nterface.stop();
        universe.stop();
        pulser.stop();
    }

    public boolean connect() throws Exception {
        return connect(new InetSocketAddress(25565));
    }

    public boolean connect(SocketAddress address) throws Exception {
        network = new GameNetworkClient();
        Future<Void> future = network.connect(address);
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            return false;
        }
        pulser.setClient(network);
        return true;
    }

    public GameNetworkClient getNetwork() {
        return network;
    }
}
