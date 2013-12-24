package org.spoutcraft.client.entity;

import org.spoutcraft.client.universe.World;

import org.spout.math.vector.Vector3f;

/**
 * Entities are objects which are dynamic unlike their static {@link org.spoutcraft.client.universe.block.Block} brethren.
 *
 * TODO Make other players just entities (should be easily done)? TODO Component system so entities don't store logic?
 */
public class Entity {
    //Unique
    private final int id;
    private String displayName;
    private World world;
    private Vector3f position;

    //TODO Transform?
    public Entity(int id, String displayName, World world, Vector3f position) {
        this.id = id;
        this.displayName = displayName;
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public World getWorld() {
        return world;
    }

    public Vector3f getPosition() {
        return position;
    }
}
