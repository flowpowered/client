/**
 * This file is part of Client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spoutcraft <http://spoutcraft.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spoutcraft.client.physics.snapshot;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.flowpowered.math.vector.Vector3f;

import org.spoutcraft.client.physics.entity.Entity;
import org.spoutcraft.client.universe.snapshot.WorldSnapshot;

/**
 *
 */
public class EntitySnapshot {
    protected static final String UNNAMED = "unnamed";
    private final int id;
    private String displayName = UNNAMED;
    private WorldSnapshot world = null;
    private Vector3f position = Vector3f.ZERO;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public EntitySnapshot(Entity entity) {
        id = entity.getId();
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
