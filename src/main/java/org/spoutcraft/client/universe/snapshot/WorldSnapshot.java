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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.spoutcraft.client.universe.Chunk;
import org.spoutcraft.client.universe.World;

import org.spout.math.vector.Vector3i;

/**
 *
 */
public class WorldSnapshot {
    private final Map<Vector3i, ChunkSnapshot> chunks = new ConcurrentHashMap<>();
    private final UUID id;
    private final String name;

    public WorldSnapshot(World world) {
        this.id = world.getID();
        this.name = world.getName();
        for (Chunk chunk : world.getChunks()) {
            chunks.put(chunk.getPosition(), new ChunkSnapshot(this, chunk));
        }
    }

    public UUID getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean hasChunk(int x, int y, int z) {
        return hasChunk(new Vector3i(x, y, z));
    }

    public boolean hasChunk(Vector3i position) {
        return chunks.containsKey(position);
    }

    public ChunkSnapshot getChunk(int x, int y, int z) {
        return getChunk(new Vector3i(x, y, z));
    }

    public ChunkSnapshot getChunk(Vector3i position) {
        return chunks.get(position);
    }

    public Collection<ChunkSnapshot> getChunks() {
        return chunks.values();
    }

    public void update(World current) {
        if (!current.getID().equals(id)) {
            throw new IllegalArgumentException("Cannot update from a world with another ID");
        }
        final Set<Vector3i> validChunks = new HashSet<>();
        for (Chunk chunk : current.getChunks()) {
            final Vector3i position = chunk.getPosition();
            final ChunkSnapshot chunkSnapshot = chunks.get(position);
            if (chunkSnapshot != null) {
                chunkSnapshot.update(chunk);
            } else {
                chunks.put(position, new ChunkSnapshot(this, chunk));
            }
            validChunks.add(position);
        }
        for (ChunkSnapshot chunkSnapshot : chunks.values()) {
            final Vector3i position = chunkSnapshot.getPosition();
            if (!validChunks.contains(position)) {
                chunks.remove(position);
            }
        }
    }
}
