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
