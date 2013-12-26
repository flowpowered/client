package org.spoutcraft.client.nterface.mesh;

import org.spout.math.vector.Vector3i;

import org.spoutcraft.client.universe.Chunk;
import org.spoutcraft.client.universe.block.material.Material;
import org.spoutcraft.client.universe.block.material.Materials;
import org.spoutcraft.client.universe.snapshot.ChunkSnapshot;
import org.spoutcraft.client.universe.snapshot.WorldSnapshot;

/**
 * A chunk and it's immediate neighbours (BTNESW), used for meshing the chunk including it's edge blocks with proper occlusion.
 */
public class ChunkSnapshotGroup {
    private final ChunkSnapshot middle;
    private final ChunkSnapshot top;
    private final ChunkSnapshot bottom;
    private final ChunkSnapshot north;
    private final ChunkSnapshot east;
    private final ChunkSnapshot south;
    private final ChunkSnapshot west;

    /**
     * Constructs a new snapshot group from the middle chunk snapshot and the world snapshot. The world snapshot will be used to source the neighbouring chunks (if they exist).
     *
     * @param middle The middle chunk
     * @param world The world snapshot containing the chunk
     */
    public ChunkSnapshotGroup(ChunkSnapshot middle, WorldSnapshot world) {
        this.middle = middle;
        final Vector3i position = middle.getPosition();
        top = world.getChunk(position.add(Vector3i.UP));
        bottom = world.getChunk(position.sub(Vector3i.UP));
        north = world.getChunk(position.sub(Vector3i.RIGHT));
        south = world.getChunk(position.add(Vector3i.RIGHT));
        east = world.getChunk(position.sub(Vector3i.FORWARD));
        west = world.getChunk(position.add(Vector3i.FORWARD));
    }

    /**
     * Returns the material at the position, looking at the directly neighbouring chunks if the position is outside the chunk. Will return {@link
     * org.spoutcraft.client.universe.block.material.Materials#AIR} if the neighbour is missing.
     *
     * @param position The position to lookup the material at
     * @return The material
     */
    public Material getMaterial(Vector3i position) {
        return getMaterial(position.getX(), position.getY(), position.getZ());
    }

    /**
     * Returns the material at the position, looking at the directly neighbouring chunks if the position is outside the chunk. Will return {@link
     * org.spoutcraft.client.universe.block.material.Materials#AIR} if the neighbour is missing.
     *
     * @param x The x coordinate of the position
     * @param y The y coordinate of the position
     * @param z The z coordinate of the position
     * @return The material
     */
    public Material getMaterial(int x, int y, int z) {
        if (x < 0) {
            return north != null ? north.getMaterial(x, y, z) : Materials.AIR;
        } else if (x >= Chunk.BLOCKS.SIZE) {
            return south != null ? south.getMaterial(x, y, z) : Materials.AIR;
        } else if (y < 0) {
            return bottom != null ? bottom.getMaterial(x, y, z) : Materials.AIR;
        } else if (y >= Chunk.BLOCKS.SIZE) {
            return top != null ? top.getMaterial(x, y, z) : Materials.AIR;
        } else if (z < 0) {
            return east != null ? east.getMaterial(x, y, z) : Materials.AIR;
        } else if (z >= Chunk.BLOCKS.SIZE) {
            return west != null ? west.getMaterial(x, y, z) : Materials.AIR;
        }
        return middle.getMaterial(x, y, z);
    }
}
