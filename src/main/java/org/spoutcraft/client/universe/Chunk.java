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
package org.spoutcraft.client.universe;

import java.util.concurrent.locks.Lock;

import org.spout.math.vector.Vector3i;

import org.spoutcraft.client.universe.block.Block;
import org.spoutcraft.client.universe.block.material.Material;
import org.spoutcraft.client.universe.snapshot.ChunkSnapshot;
import org.spoutcraft.client.universe.store.AtomicBlockStore;
import org.spoutcraft.client.universe.store.impl.AtomicPaletteBlockStore;
import org.spoutcraft.client.util.BitSize;

/**
 *
 */
public class Chunk {
    // Stores the size of the amount of blocks in this Chunk
    public static final BitSize BLOCKS = new BitSize(4);
    // Stores all the blocks in the chunk
    private final AtomicBlockStore blocks;
    // A reference to the chunk's world
    private final World world;
    // The chunk's position
    private final Vector3i position;

    public Chunk(World world, Vector3i position) {
        this.world = world;
        this.position = position;
        blocks = new AtomicPaletteBlockStore(BLOCKS.BITS, true, 10);
    }

    public Chunk(World world, Vector3i position, short[] blocks, short[] data) {
        this.world = world;
        this.position = position;
        this.blocks = new AtomicPaletteBlockStore(BLOCKS.BITS, true, false, 10, blocks, data);
    }

    public World getWorld() {
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

    public Block getBlock(int x, int y, int z) {
        return getBlock(new Vector3i(x, y, z));
    }

    public Block getBlock(Vector3i position) {
        return new Block(getMaterial(position), position);
    }

    public void setBlock(Block block) {
        setMaterial(block.getPosition(), block.getMaterial());
    }

    public Material getMaterial(Vector3i position) {
        return getMaterial(position.getX(), position.getY(), position.getZ());
    }

    public Material getMaterial(int x, int y, int z) {
        return Material.getPacked(blocks.getFullData(x & BLOCKS.MASK, y & BLOCKS.MASK, z & BLOCKS.MASK));
    }

    public void setMaterial(Vector3i position, Material material) {
        setMaterial(position.getX(), position.getY(), position.getZ(), material);
    }

    public void setMaterial(int x, int y, int z, Material material) {
        blocks.setBlock(x & BLOCKS.MASK, y & BLOCKS.MASK, z & BLOCKS.MASK, material);
    }

    public ChunkSnapshot buildSnapshot() {
        return new ChunkSnapshot(world, position, blocks.getBlockIdArray(), blocks.getDataArray());
    }

    public void updateSnapshot(ChunkSnapshot old) {
        if (old.getPosition() != position || old.getWorld() != world) {
            throw new IllegalArgumentException("Cannot accept a chunk snapshot from another position or world");
        }
        if (!blocks.isDirty()) {
            return;
        }
        final Lock lock = old.getLock().writeLock();
        lock.lock();
        try {
            // TODO: update only the dirty blocks, unless the dirty arrays are overflown
            blocks.getBlockIdArray(old.getBlockIDs());
            blocks.getDataArray(old.getBlockSubIDs());
            blocks.resetDirtyArrays();
        } finally {
            lock.unlock();
        }
    }
}
