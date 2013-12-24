package org.spoutcraft.client.universe.snapshot;

import java.util.Collection;
import java.util.UUID;

import org.spout.math.vector.Vector3i;

import org.spoutcraft.client.util.map.TripleIntObjectMap;
import org.spoutcraft.client.util.map.impl.TTripleInt21ObjectHashMap;

/**
 *
 */
public class WorldSnapshot {
    private final TripleIntObjectMap<ChunkSnapshot> chunks = new TTripleInt21ObjectHashMap<>();
    private final UUID id;

    public WorldSnapshot(UUID id) {
        this.id = id;
    }

    public UUID getID() {
        return id;
    }

    public boolean hasChunk(Vector3i position) {
        return hasChunk(position.getX(), position.getY(), position.getZ());
    }

    public boolean hasChunk(int x, int y, int z) {
        return chunks.containsKey(x, y, z);
    }

    public ChunkSnapshot getChunk(Vector3i position) {
        return getChunk(position.getX(), position.getY(), position.getZ());
    }

    public ChunkSnapshot getChunk(int x, int y, int z) {
        return chunks.get(x, y, z);
    }

    public ChunkSnapshot setChunk(ChunkSnapshot chunk) {
        final Vector3i position = chunk.getPosition();
        return chunks.put(position.getX(), position.getY(), position.getZ(), chunk);
    }

    public ChunkSnapshot removeChunk(Vector3i position) {
        return removeChunk(position.getX(), position.getY(), position.getZ());
    }

    public ChunkSnapshot removeChunk(int x, int y, int z) {
        return chunks.remove(x, y, z);
    }

    public Collection<ChunkSnapshot> getChunks() {
        return chunks.valueCollection();
    }
}
