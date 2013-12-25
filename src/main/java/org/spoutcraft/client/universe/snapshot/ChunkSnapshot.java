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
package org.spoutcraft.client.universe.snapshot;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.spout.math.vector.Vector3i;

import org.spoutcraft.client.universe.Chunk;
import org.spoutcraft.client.universe.World;
import org.spoutcraft.client.universe.block.Block;
import org.spoutcraft.client.universe.block.material.Material;

/**
 *
 */
public class ChunkSnapshot {
    private final short[] blockIDs;
    private final short[] blockSubIDs;
    private final WeakReference<World> world;
    private final Vector3i position;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public ChunkSnapshot(World world, Vector3i position, short[] blockIDs, short[] blockSubIDs) {
        this.world = new WeakReference<>(world);
        this.position = position;
        this.blockIDs = blockIDs;
        this.blockSubIDs = blockSubIDs;
    }

    public World getWorld() {
        return world.get();
    }

    public Vector3i getPosition() {
        return position;
    }

    public Block getBlock(Vector3i position) {
        return new Block(getMaterial(position), position);
    }

    public Block getBlock(int x, int y, int z) {
        return getBlock(new Vector3i(x, y, z));
    }

    public Material getMaterial(Vector3i position) {
        return getMaterial(position.getX(), position.getY(), position.getZ());
    }

    public Material getMaterial(int x, int y, int z) {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return Material.get(blockIDs[getBlockIndex(x, y, z)], blockSubIDs[getBlockIndex(x, y, z)]);
        } finally {
            lock.unlock();
        }
    }

    // This should not be exposed in any API
    public short[] getBlockIDs() {
        return blockIDs;
    }

    // This should not be exposed in any API
    public short[] getBlockSubIDs() {
        return blockSubIDs;
    }

    // This should not be exposed in any API
    public ReadWriteLock getLock() {
        return lock;
    }

    private static int getBlockIndex(int x, int y, int z) {
        return (y & Chunk.BLOCKS.MASK) << Chunk.BLOCKS.DOUBLE_BITS | (z & Chunk.BLOCKS.MASK) << Chunk.BLOCKS.BITS | x & Chunk.BLOCKS.MASK;
    }
}
