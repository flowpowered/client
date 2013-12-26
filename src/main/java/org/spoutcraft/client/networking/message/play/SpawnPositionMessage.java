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
package org.spoutcraft.client.networking.message.play;

import org.spoutcraft.client.networking.message.ChannelMessage;

import org.spout.math.vector.Vector3i;

/**
 * Client bound message that instructs the client to update the compass position to the new spawn position.
 */
public class SpawnPositionMessage extends ChannelMessage {
    private static final Channel[] REQUIRED_CHANNELS = new Channel[] {Channel.UNIVERSE};
    private final Vector3i position;

    /**
     * Constructs a new spawn position
     *
     * @param x The x-axis coordinate (block)
     * @param y The y-axis coordinate (block)
     * @param z The z-axis coordinate (block)
     */
    public SpawnPositionMessage(int x, int y, int z) {
        this(new Vector3i(x, y, z));
    }

    /**
     * Constructs a new spawn position
     *
     * @param position {@link org.spout.math.vector.Vector3i} containing the x, y, z axis coordinates of spawn (block)
     */
    public SpawnPositionMessage(Vector3i position) {
        this.position = position;
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }

    public int getZ() {
        return position.getZ();
    }

    public Vector3i getPosition() {
        return position;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Channel[] getRequiredChannels() {
        return REQUIRED_CHANNELS;
    }
}
