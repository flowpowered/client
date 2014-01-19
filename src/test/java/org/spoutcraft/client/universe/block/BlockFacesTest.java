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
package org.spoutcraft.client.universe.block;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class BlockFacesTest {
    @Test
    public void testContains() {
        Assert.assertEquals(BlockFaces.NESW.contains(BlockFace.NORTH), true);
        Assert.assertEquals(BlockFaces.NESW.contains(BlockFace.THIS), false);
    }

    @Test
    public void testIndexOf() {
        Assert.assertEquals(BlockFaces.NESW.indexOf(BlockFace.NORTH, -1), 0);
        Assert.assertEquals(BlockFaces.NESW.indexOf(BlockFace.EAST, -1), 1);
        Assert.assertEquals(BlockFaces.NESW.indexOf(BlockFace.WEST, -1), 3);
        Assert.assertEquals(BlockFaces.NESW.indexOf(BlockFace.TOP, -1), -1);
        Assert.assertEquals(BlockFaces.NESW.indexOf(BlockFace.TOP, 55), 55);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(BlockFaces.NESW.get(0), BlockFace.NORTH);
        Assert.assertEquals(BlockFaces.NESW.get(3), BlockFace.WEST);
        Assert.assertEquals(BlockFaces.NESW.get(5), BlockFace.WEST);
        Assert.assertEquals(BlockFaces.NESW.get(-4), BlockFace.NORTH);
        Assert.assertEquals(BlockFaces.NESW.get(6, BlockFace.THIS), BlockFace.THIS);
        Assert.assertEquals(BlockFaces.NESW.get(6, null), null);
    }

    @Test
    public void testNext() {
        Assert.assertEquals(BlockFaces.NESW.next(BlockFace.NORTH, 2), BlockFace.SOUTH);
        Assert.assertEquals(BlockFaces.NESW.next(BlockFace.NORTH, -2), BlockFace.SOUTH);
        Assert.assertEquals(BlockFaces.NESW.next(BlockFace.WEST, -8), BlockFace.WEST);
        Assert.assertEquals(BlockFaces.NESW.next(BlockFace.WEST, -1), BlockFace.SOUTH);
        Assert.assertEquals(BlockFaces.NESW.next(BlockFace.WEST, 1), BlockFace.NORTH);
    }
}
