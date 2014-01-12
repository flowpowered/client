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
package org.spoutcraft.client.network.message;

import java.util.concurrent.atomic.AtomicInteger;

import com.flowpowered.networking.Message;

/**
 * Represents a message found in one ore more channels. Stores a which channels have marked the message as read. When constructing a channel message, the list of channels that have to read the message
 * is given in the constructor. This list can be empty. When all channels in the list have read the message, it will be removed from the message queue in all the channels.
 */
public abstract class ChannelMessage implements Message {
    private AtomicInteger read = new AtomicInteger(0);
    private final int requiredMask;

    /**
     * Constructs a new channel message that required no channels to have read it.
     */
    public ChannelMessage() {
        requiredMask = 0;
    }

    /**
     * Constructs a new message that requires a single channel to have read it.
     *
     * @param requiredRead The channel that needs to read the message
     */
    public ChannelMessage(Channel requiredRead) {
        requiredMask = requiredRead.getMask();
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    /**
     * Constructs a new message that requires multiple channels to have read it.
     *
     * @param requiredRead The channels that need to have read the message
     */
    public ChannelMessage(Channel... requiredRead) {
        short required = 0;
        for (Channel channel : requiredRead) {
            required |= channel.getMask();
        }
        requiredMask = required;
    }

    /**
     * Marks a channel as having read the message.
     *
     * @param channel The channel that has read the message
     */
    public void markChannelRead(Channel channel) {
        boolean done = false;
        while (!done) {
            int readOld = read.get();
            int readNew = readOld | channel.getMask();
            done = read.compareAndSet(readOld, readNew);
        }
    }

    /**
     * Returns true if the channel has read the message, false if otherwise.
     *
     * @param channel The channel to check
     * @return Whether or not the channel has read the message
     */
    public boolean isChannelRead(Channel channel) {
        return (read.get() & channel.getMask()) == channel.getMask();
    }

    /**
     * Returns true if all channels that had to read the message have done so, false if otherwise.
     *
     * @return Whether or not all channels that have to read the message have done so
     */
    public boolean isFullyRead() {
        return (read.get() & requiredMask) == requiredMask;
    }

    /**
     * An enum of all the message channels.
     */
    public static enum Channel {
        UNIVERSE(0x1),
        INTERFACE(0x2),
        NETWORK(0x4),
        PHYSICS(0x8);
        private final int mask;

        private Channel(int mask) {
            this.mask = mask;
        }

        /**
         * Returns the mask of the channel, used internally to mark the channel as having read a channel message using bit flags.
         *
         * @return The channel's mask
         */
        public int getMask() {
            return mask;
        }
    }
}

