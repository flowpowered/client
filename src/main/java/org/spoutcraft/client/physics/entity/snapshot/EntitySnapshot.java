package org.spoutcraft.client.physics.entity.snapshot;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.spout.math.vector.Vector3f;

import org.spoutcraft.client.physics.entity.Entity;
import org.spoutcraft.client.universe.snapshot.WorldSnapshot;

/**
 *
 */
public class EntitySnapshot {
    private final int id;
    private String displayName;
    private WorldSnapshot world;
    private Vector3f position;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public EntitySnapshot(Entity entity) {
        id = entity.getId();
        displayName = entity.getDisplayName();
        world = entity.getWorld();
        position = entity.getPosition();
    }

    public int getId() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return id;
        } finally {
            lock.unlock();
        }
    }

    public String getDisplayName() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return displayName;
        } finally {
            lock.unlock();
        }
    }

    public WorldSnapshot getWorld() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return world;
        } finally {
            lock.unlock();
        }
    }

    public Vector3f getPosition() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return position;
        } finally {
            lock.unlock();
        }
    }

    public void update(Entity current) {
        if (id != current.getId()) {
            throw new IllegalArgumentException("Cannot update from an entity with a different ID");
        }
        final Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            displayName = current.getDisplayName();
            world = current.getWorld();
            position = current.getPosition();
        } finally {
            lock.unlock();
        }
    }
}
