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
package org.spoutcraft.client.physics.entity;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.flowpowered.networking.session.Session;

import org.spout.math.imaginary.Quaternionf;
import org.spout.math.matrix.Matrix3f;
import org.spout.math.vector.Vector3f;

import org.spoutcraft.client.network.ClientSession;
import org.spoutcraft.client.universe.snapshot.WorldSnapshot;

/**
 * The local client player which has the {@link com.flowpowered.networking.session.Session} tied to it.
 */
public class Player extends Entity {
    private static final Vector3f RIGHT = new Vector3f(-1, 0, 0);
    private static final Vector3f UP = new Vector3f(0, 1, 0);
    private static final Vector3f FORWARD = new Vector3f(0, 0, -1);
    private AtomicReference<Quaternionf> headRotation = new AtomicReference<>(Quaternionf.IDENTITY);
    private AtomicReference<Matrix3f> toHeadRotation = new AtomicReference<>(Matrix3f.IDENTITY);
    private final ClientSession session;

    public Player(int id, String displayName, WorldSnapshot world, Vector3f position, ClientSession session) {
        super(id, displayName, world, position);
        this.session = session;
    }

    public Quaternionf getHeadRotation() {
        return headRotation.get();
    }

    public void setHeadRotation(Quaternionf headRotation) {
        this.headRotation.set(headRotation);
        toHeadRotation.set(Matrix3f.createRotation(headRotation));
    }

    public Vector3f getRight() {
        return toHeadRotation.get().transform(RIGHT);
    }

    public Vector3f getUp() {
        return toHeadRotation.get().transform(UP);
    }

    public Vector3f getForward() {
        return toHeadRotation.get().transform(FORWARD);
    }

    public UUID getUUID() {
        return session != null ? session.getUUID() : null;
    }

    public String getUsername() {
        return session != null ? session.getUsername() : null;
    }

    public Session getSession() {
        return session;
    }
}
