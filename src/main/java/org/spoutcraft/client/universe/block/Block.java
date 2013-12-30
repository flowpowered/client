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
package org.spoutcraft.client.universe.block;

import org.spout.math.vector.Vector3i;

import org.spoutcraft.client.universe.Chunk;
import org.spoutcraft.client.universe.block.material.Material;

/**
 *
 */
public class Block {
    private final Vector3i position;
    private final Material material;
    private final short blockLight;
    private final short blockSkyLight;

    public Block(Vector3i position, int packed) {
        this(position, (short) (packed >> 16), (short) packed);
    }

    public Block(Vector3i position, short id, short data) {
        this(position,
                Material.get(id, Chunk.SUB_ID_MASK.extract(data)),
                Chunk.BLOCK_LIGHT_MASK.extract(data),
                Chunk.BLOCK_SKY_LIGHT_MASK.extract(data));
    }

    public Block(Vector3i position, Material material, short blockLight, short blockSkyLight) {
        this.position = position;
        this.material = material;
        this.blockLight = blockLight;
        this.blockSkyLight = blockSkyLight;
    }

    public Material getMaterial() {
        return material;
    }

    public Vector3i getPosition() {
        return position;
    }

    public short getBlockLight() {
        return blockLight;
    }

    public short getBlockSkyLight() {
        return blockSkyLight;
    }
}
