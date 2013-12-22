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
    // Stores all the blocks in the chunk
    private final AtomicBlockStore blocks;

    public Chunk() {
        blocks = new AtomicPaletteBlockStore(BLOCKS.BITS, true, 10);
    }

    public Chunk(short[] blocks, short[] data) {
        this.blocks = new AtomicPaletteBlockStore(BLOCKS.BITS, true, false, 10, blocks, data);
    }

    public Block getBlock(int x, int y, int z) {
        return getBlock(new Vector3i(x, y, z));
    }

    public Block getBlock(Vector3i position) {
        return new Block(Material.getPacked(blocks.getFullData(position.getX() & BLOCKS.MASK, position.getY() & BLOCKS.MASK, position.getZ() & BLOCKS.MASK)), position);
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
}
