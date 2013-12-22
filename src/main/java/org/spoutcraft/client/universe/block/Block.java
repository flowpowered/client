package org.spoutcraft.client.universe.block;

import org.spout.math.vector.Vector3i;

import org.spoutcraft.client.universe.block.material.Material;

/**
 *
 */
public class Block {
    private final Material material;
    private final Vector3i position;

    public Block(Material material, Vector3i position) {
        this.material = material;
        this.position = position;
    }

    public Material getMaterial() {
        return material;
    }

    public Vector3i getPosition() {
        return position;
    }
}
