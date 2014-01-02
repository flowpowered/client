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