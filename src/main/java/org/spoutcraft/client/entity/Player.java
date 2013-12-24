package org.spoutcraft.client.entity;

import java.util.UUID;

import com.flowpowered.networking.session.Session;
import org.spoutcraft.client.networking.ClientSession;
import org.spoutcraft.client.universe.World;

import org.spout.math.vector.Vector3f;

/**
 * The local client player which has the {@link com.flowpowered.networking.session.Session} tied to it.
 */
public class Player extends Entity {
    private final ClientSession session;

    public Player(int id, String displayName, World world, Vector3f position, ClientSession session) {
        super(id, displayName, world, position);
        this.session = session;
    }

    public UUID getUUID() {
        return session.getUUID();
    }

    public String getUsername() {
        return session.getUsername();
    }

    public Session getSession() {
        return session;
    }
}
