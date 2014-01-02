package org.spoutcraft.client.util;

import org.junit.Assert;
import org.junit.Test;

import org.spout.math.matrix.Matrix4f;
import org.spout.math.vector.Vector3f;

public class ViewFrustumTest {
    @Test
    public void testIntersects() {
        final Matrix4f projection = Matrix4f.createPerspective(70, 16 / 9.0f, 1, 2000);
        final Matrix4f view = Matrix4f.createLookAt(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), Vector3f.UP);
        final ViewFrustum frustum = new ViewFrustum();
        frustum.update(projection, view);
        final Vector3f[] vertices = new Vector3f[8];
        // Front
        vertices[0] = new Vector3f(0, 0, 16);
        vertices[1] = new Vector3f(16, 0, 16);
        vertices[2] = new Vector3f(16, 16, 16);
        vertices[3] = new Vector3f(0, 16, 16);
        // Back
        vertices[4] = new Vector3f(0, 0, 0);
        vertices[5] = new Vector3f(16, 0, 0);
        vertices[6] = new Vector3f(16, 16, 0);
        vertices[7] = new Vector3f(0, 16, 0);

        Assert.assertTrue(frustum.intersectsCuboid(vertices, Vector3f.ZERO));
    }

    @Test
    public void testIntersectsFalse() {
        final Matrix4f projection = Matrix4f.createPerspective(70, 16 / 9.0f, 1, 2000);
        final Matrix4f view = Matrix4f.createLookAt(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), Vector3f.UP);
        final ViewFrustum frustum = new ViewFrustum();
        frustum.update(projection, view);
        final Vector3f[] vertices = new Vector3f[8];
        // Front
        vertices[0] = new Vector3f(-32, -32, -16);
        vertices[1] = new Vector3f(-16, -32, -16);
        vertices[2] = new Vector3f(-16, -16, -16);
        vertices[3] = new Vector3f(-32, -16, -16);
        // Back
        vertices[4] = new Vector3f(-32, -32, -32);
        vertices[5] = new Vector3f(-16, -32, -32);
        vertices[6] = new Vector3f(-16, -16, -32);
        vertices[7] = new Vector3f(-32, -16, -32);

        Assert.assertFalse(frustum.intersectsCuboid(vertices, Vector3f.ZERO));
    }

    @Test
    public void testContains() {
        final Matrix4f projection = Matrix4f.createPerspective(70, 16 / 9.0f, 1, 2000);
        final Matrix4f view = Matrix4f.createLookAt(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), Vector3f.UP);
        final ViewFrustum frustum = new ViewFrustum();
        frustum.update(projection, view);

        Assert.assertTrue(frustum.contains(new Vector3f(0, 0, 2)));
        Assert.assertTrue(frustum.contains(new Vector3f(0, 0, 100)));
        Assert.assertFalse(frustum.contains(new Vector3f(0, 0, -1)));
    }
}