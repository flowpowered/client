/**
 * This file is part of Client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013-2014 Spoutcraft <http://spoutcraft.org/>
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
package org.spoutcraft.client.physics.entity;

import java.util.concurrent.atomic.AtomicReference;

import com.flowpowered.math.vector.Vector3f;
import org.spoutcraft.client.universe.snapshot.WorldSnapshot;

/**
 * Entities are objects which are dynamic unlike their static {@link org.spoutcraft.client.universe.block.Block} brethren.
 * <p/>
 * TODO Make other players just entities (should be easily done)? TODO Component system so entities don't store logic?
 */
public class Entity {
    private final int id;
    private AtomicReference<String> displayName = new AtomicReference<>(null);
    private AtomicReference<WorldSnapshot> world = new AtomicReference<>(null);
    private AtomicReference<Vector3f> position = new AtomicReference<>(null);

    //TODO Transform?
    //TODO Auto assign ID from AtomicInteger counter?
    public Entity(int id, String displayName, WorldSnapshot world, Vector3f position) {
        this.id = id;
        this.displayName.set(displayName);
        this.world.set(world);
        this.position.set(position);
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName.get();
    }

    public WorldSnapshot getWorld() {
        return world.get();
    }

    public Vector3f getPosition() {
        return position.get();
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }
}
