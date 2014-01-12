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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.flowpowered.commons.store.block.AtomicBlockStore;
import org.spoutcraft.client.universe.block.Block;
import org.spoutcraft.client.universe.block.material.Material;
import org.spoutcraft.client.universe.world.Chunk;

import org.spout.math.vector.Vector3i;

/**
 *
 */
public class ChunkSnapshot {
    private final short[] blockIDs = new short[Chunk.BLOCKS.VOLUME];
    private final short[] blockData = new short[Chunk.BLOCKS.VOLUME];
    private final WorldSnapshot world;
    private final Vector3i position;
    private long updateNumber = 0;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public ChunkSnapshot(WorldSnapshot world, Vector3i position) {
        this.world = world;
        this.position = position;
    }

    public WorldSnapshot getWorld() {
        return world;
    }

    public Vector3i getPosition() {
        return position;
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

    public Block getBlock(Vector3i position) {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            final int index = getBlockIndex(position);
            return new Block(position, blockIDs[index], blockData[index]);
        } finally {
            lock.unlock();
        }
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
            final int index = getBlockIndex(x, y, z);
            return Material.get(blockIDs[index], Chunk.SUB_ID_MASK.extract(blockData[index]));
        } finally {
            lock.unlock();
        }
    }

    public short getBlockLight(int x, int y, int z) {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return Chunk.BLOCK_LIGHT_MASK.extract(blockData[getBlockIndex(x, y, z)]);
        } finally {
            lock.unlock();
        }
    }

    public short getBlockSkyLight(int x, int y, int z) {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return Chunk.BLOCK_SKY_LIGHT_MASK.extract(blockData[getBlockIndex(x, y, z)]);
        } finally {
            lock.unlock();
        }
    }

    public long getUpdateNumber() {
        return updateNumber;
    }

    /**
     * Updates the snapshot to the current chunk passed to the constructor. The chunk passed must be a the same location and world than the snapshot. Returns whether or not the snapshot state has
     * changed. Clears the chunk block store dirty arrays.
     *
     * @param current The current chunk to update from
     * @return Whether or not the snapshot state has changed
     */
    public boolean update(Chunk current) {
        if (!current.getPosition().equals(position) || !current.getWorld().getID().equals(world.getID())) {
            throw new IllegalArgumentException("Cannot accept a chunk from another position or world");
        }
        final Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            // TODO: update only the dirty blocks, unless the dirty arrays are overflown
            final AtomicBlockStore blocks = current.getBlocks();
            if (blocks.isDirty()) {
                blocks.getBlockIdArray(blockIDs);
                blocks.getDataArray(blockData);
                blocks.resetDirtyArrays();
                updateNumber++;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChunkSnapshot)) {
            return false;
        }
        final ChunkSnapshot that = (ChunkSnapshot) o;
        if (!position.equals(that.position)) {
            return false;
        }
        return world.equals(that.world);
    }

    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + position.hashCode();
        return result;
    }

    private static int getBlockIndex(Vector3i position) {
        return getBlockIndex(position.getX(), position.getY(), position.getZ());
    }

    private static int getBlockIndex(int x, int y, int z) {
        return (y & Chunk.BLOCKS.MASK) << Chunk.BLOCKS.DOUBLE_BITS | (z & Chunk.BLOCKS.MASK) << Chunk.BLOCKS.BITS | x & Chunk.BLOCKS.MASK;
    }
}
