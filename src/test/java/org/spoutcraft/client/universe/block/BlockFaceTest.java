package org.spoutcraft.client.universe.block;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class BlockFaceTest {
    @Test
    public void testFaceFromYaw() {
        Assert.assertEquals(BlockFace.fromYaw(-80f), BlockFace.NORTH);
        Assert.assertEquals(BlockFace.fromYaw(283f), BlockFace.NORTH);
        Assert.assertEquals(BlockFace.fromYaw(12f), BlockFace.WEST);
        Assert.assertEquals(BlockFace.fromYaw(87f), BlockFace.SOUTH);
        Assert.assertEquals(BlockFace.fromYaw(180f), BlockFace.EAST);
    }

    @Test
    public void testFaceFromOffset() {
        for (BlockFace face : BlockFace.values()) {
            Assert.assertEquals(face, BlockFace.fromOffset(face.getOffset().toFloat()));
        }
    }
}
