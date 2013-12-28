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

import org.spout.math.vector.Vector3i;

import org.spoutcraft.client.universe.block.Block;
import org.spoutcraft.client.universe.block.material.Material;
import org.spoutcraft.client.universe.store.AtomicBlockStore;
import org.spoutcraft.client.universe.store.impl.AtomicPaletteBlockStore;
import org.spoutcraft.client.util.BitSize;

/**
 *
 */
public class Chunk {
    // Stores the size of the amount of blocks in this Chunk
    public static final BitSize BLOCKS = new BitSize(4);
    // Default size of the arrays in the block store that store the dirty blocks since the last reset
    private static final int DIRTY_ARRAY_SIZE = 10;
    // Stores all the blocks in the chunk
    private final AtomicBlockStore blocks;
    // A reference to the chunk's world
    private final World world;
    // The chunk's position
    private final Vector3i position;

    public Chunk(World world, Vector3i position) {
        this.world = world;
        this.position = position;
        blocks = new AtomicPaletteBlockStore(BLOCKS.BITS, true, DIRTY_ARRAY_SIZE);
    }

    public Chunk(World world, Vector3i position, short[] blocks, short[] data) {
        this.world = world;
        this.position = position;
        this.blocks = new AtomicPaletteBlockStore(BLOCKS.BITS, true, false, DIRTY_ARRAY_SIZE, blocks, data);
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

    public AtomicBlockStore getBlocks() {
        return blocks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Chunk)) {
            return false;
        }
        final Chunk chunk = (Chunk) o;
        if (!position.equals(chunk.position)) {
            return false;
        }
        return world.equals(chunk.world);
    }

    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + position.hashCode();
        return result;
    }
}
