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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.spoutcraft.client.ticking.TickingElement;

public class Network extends TickingElement {
    private final GameNetworkClient client = new GameNetworkClient();

    public Network() {
        super(20);
    }

    @Override
    public void onTick() {
        if (client.getSession() != null) {
            client.getSession().pulse();
        }
    }

    @Override
    public void onStart() {
        
    }

    @Override
    public void onStop() {        
    }

    public boolean connect() throws Exception {
        return connect(new InetSocketAddress(25565));
    }

    public boolean connect(SocketAddress address) throws Exception {
        Future<Void> future = client.connect(address);
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            return false;
        }
        return true;
    }

    public ClientSession getSession() {
        return client.getSession();
    }
}
