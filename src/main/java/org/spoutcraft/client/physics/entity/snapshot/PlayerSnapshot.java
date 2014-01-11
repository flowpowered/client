package org.spoutcraft.client.physics.entity.snapshot;

import java.util.UUID;
import java.util.concurrent.locks.Lock;

import org.spout.math.imaginary.Quaternionf;

import org.spoutcraft.client.physics.entity.Player;

/**
 *
 */
public class PlayerSnapshot extends EntitySnapshot {
    private Quaternionf headRotation;
    private UUID uuid;
    private String username;

    public PlayerSnapshot(Player player) {
        super(player);
        headRotation = player.getHeadRotation();
        uuid = player.getUUID();
        username = player.getUsername();
    }

    public Quaternionf getHeadRotation() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return headRotation;
        } finally {
            lock.unlock();
        }
    }

    public UUID getUUID() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return uuid;
        } finally {
            lock.unlock();
        }
    }

    public String getUsername() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return username;
        } finally {
            lock.unlock();
        }
    }

    public void update(Player current) {
        super.update(current);
        final Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            headRotation = current.getHeadRotation();
            uuid = current.getUUID();
            username = current.getUsername();
        } finally {
            lock.unlock();
        }
    }
}
