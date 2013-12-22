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
