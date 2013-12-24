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
    /**
     * -1 is reserved for the client player because no other entities can be spawned on the client without a valid id (>= 0).
     */
    public final static int LOCALPLAYERID = -1;
    private final ClientSession session;

    public Player(String displayName, World world, Vector3f position, ClientSession session) {
        super(LOCALPLAYERID, displayName, world, position);
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
