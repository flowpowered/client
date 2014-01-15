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
package org.spoutcraft.client.nterface.snapshot;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.flowpowered.math.imaginary.Quaternionf;
import com.flowpowered.math.matrix.Matrix4f;
import com.flowpowered.math.vector.Vector3f;

import org.spout.renderer.api.Camera;

/**
 *
 */
public class CameraSnapshot {
    private Matrix4f projection = Matrix4f.IDENTITY;
    private Vector3f position = Vector3f.ZERO;
    private Quaternionf rotation = Quaternionf.IDENTITY;
    private Matrix4f viewMatrix = Matrix4f.IDENTITY;
    private Vector3f right = Vector3f.ZERO;
    private Vector3f up = Vector3f.ZERO;
    private Vector3f forward = Vector3f.ZERO;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Returns the perspective projection matrix.
     *
     * @return The perspective projection matrix
     */
    public Matrix4f getProjectionMatrix() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return projection;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the view matrix, which is the transformation matrix for the position and rotation.
     *
     * @return The view matrix
     */
    public Matrix4f getViewMatrix() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return viewMatrix;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the camera position.
     *
     * @return The camera position
     */
    public Vector3f getPosition() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return position;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the camera rotation.
     *
     * @return The camera rotation
     */
    public Quaternionf getRotation() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return rotation;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the vector representing the right direction for the camera.
     *
     * @return The camera's right direction vector
     */
    public Vector3f getRight() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return right;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the vector representing the up direction for the camera.
     *
     * @return The camera's up direction vector
     */
    public Vector3f getUp() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return up;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the vector representing the forward direction for the camera.
     *
     * @return The camera's forward direction vector
     */
    public Vector3f getForward() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return forward;
        } finally {
            lock.unlock();
        }
    }

    public void update(Camera camera) {
        final Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            projection = camera.getProjectionMatrix();
            position = camera.getPosition();
            rotation = camera.getRotation();
            viewMatrix = camera.getViewMatrix();
            right = camera.getRight();
            up = camera.getUp();
            forward = camera.getForward();
        } finally {
            lock.unlock();
        }
    }
}
