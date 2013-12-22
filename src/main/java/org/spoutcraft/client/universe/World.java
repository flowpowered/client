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

import org.spoutcraft.client.util.map.TripleIntObjectMap;
import org.spoutcraft.client.util.map.impl.TTripleInt21ObjectHashMap;

/**
 *
 */
public class World {
    private final TripleIntObjectMap<Chunk> chunks = new TTripleInt21ObjectHashMap<>();

    public Chunk getChunk(Vector3i position) {
        return getChunk(position.getX(), position.getY(), position.getZ());
    }

    public Chunk getChunk(int x, int y, int z) {
        return chunks.get(x, y, z);
    }

    public Chunk setChunk(Vector3i position, Chunk chunk) {
        return setChunk(position.getX(), position.getY(), position.getZ(), chunk);
    }

    public Chunk setChunk(int x, int y, int z, Chunk chunk) {
        return chunks.put(x, y, z, chunk);
    }

    public Chunk removeChunk(Vector3i position) {
        return removeChunk(position.getX(), position.getY(), position.getZ());
    }

    public Chunk removeChunk(int x, int y, int z) {
        return chunks.remove(x, y, z);
    }
}
