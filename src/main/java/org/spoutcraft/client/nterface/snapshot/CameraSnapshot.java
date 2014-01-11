package org.spoutcraft.client.nterface.snapshot;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.spout.math.imaginary.Quaternionf;
import org.spout.math.matrix.Matrix4f;
import org.spout.math.vector.Vector3f;
import org.spout.renderer.Camera;

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
