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
package org.spoutcraft.client.network.message.handshake;

import org.spoutcraft.client.network.message.ChannelMessage;

/**
 * Server-bound message that initiates the connection process to the server
 */
public class HandshakeMessage extends ChannelMessage {
    private final int version;
    private final String address;
    private final int port;
    private final HandshakeState state;

    /**
     * Constructs a new handshake
     *
     * @param version The protocol version the client will connect with
     * @param address The address of the server
     * @param port The port to connect to on the server
     * @param state The state of the handshake, see {@link HandshakeMessage.HandshakeState}
     */
    public HandshakeMessage(int version, String address, int port, HandshakeState state) {
        this.version = version;
        this.address = address;
        this.port = port;
        this.state = state;
    }

    /**
     * Returns the protocol version.
     *
     * @return The protocol version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns the client address.
     *
     * @return The client address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the client port.
     *
     * @return The client port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the handshake state.
     *
     * @return The handshake state
     */
    public HandshakeState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "HandshakeMessage{" +
                "version=" + version +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", state=" + state +
                '}';
    }

    /**
     * An enum of the handshake states.
     */
    public enum HandshakeState {
        /**
         * Client is asking for server status (i.e. Multiplayer menu in the Minecraft client)
         */
        STATUS(1),
        /**
         * Client is attempting to login to a server
         */
        LOGIN(2);
        private final int state;

        private HandshakeState(int state) {
            this.state = state;
        }

        /**
         * Returns the numerical value associated to the handshake state.
         *
         * @return The numerical value
         */
        public int value() {
            return state;
        }
    }
}
