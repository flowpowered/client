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

import com.flowpowered.math.imaginary.Quaternionf;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Indicates the facing of a Block
 */
public enum BlockFace {
    TOP(0, 1, 0, Quaternionf.fromRotationTo(Vector3f.FORWARD, Vector3f.UP)),
    BOTTOM(0, -1, 0, Quaternionf.fromRotationTo(Vector3f.FORWARD, Vector3f.UP.negate()), TOP),
    NORTH(-1, 0, 0, Quaternionf.fromRotationTo(Vector3f.FORWARD, Vector3f.RIGHT.negate())),
    SOUTH(1, 0, 0, Quaternionf.fromRotationTo(Vector3f.FORWARD, Vector3f.RIGHT), NORTH),
    EAST(0, 0, -1, Quaternionf.fromAngleDegAxis(180, 0, 1, 0)),
    WEST(0, 0, 1, Quaternionf.IDENTITY, EAST),
    THIS(0, 0, 0, null);
    private final Vector3i offset;
    private final Quaternionf direction;
    private BlockFace opposite = this;
    private static final TIntObjectHashMap<BlockFace> OFFSET_MAP = new TIntObjectHashMap<>(7);

    static {
        for (BlockFace face : values()) {
            OFFSET_MAP.put(getOffsetHash(face.getOffset()), face);
        }
    }

    private BlockFace(int dx, int dy, int dz, Quaternionf direction, BlockFace opposite) {
        this(dx, dy, dz, direction);
        this.opposite = opposite;
        opposite.opposite = this;
    }

    private BlockFace(int dx, int dy, int dz, Quaternionf direction) {
        this.offset = new Vector3i(dx, dy, dz);
        this.direction = direction;
    }

    /**
     * Represents the rotation of the BlockFace in the world as a Quaternion. This is the rotation form the west face to this face.
     *
     * @return the direction of the block face.
     */
    public Quaternionf getDirection() {
        return this.direction;
    }

    /**
     * Represents the directional offset of this block face as a Vector3i.
     *
     * @return the offset of this directional.
     */
    public Vector3i getOffset() {
        return offset;
    }

    /**
     * Gets the opposite BlockFace. If this BlockFace has no opposite the method will return itself.
     *
     * @return the opposite BlockFace, or this if it has no opposite.
     */
    public BlockFace getOpposite() {
        return this.opposite;
    }

    /**
     * Uses a yaw angle to get the north, east, west or south face which points into the same direction.
     *
     * @param yaw to use
     * @return the block face
     */
    public static BlockFace fromYaw(float yaw) {
        return BlockFaces.WSEN.get(Math.round(yaw / 90f) & 0x3);
    }

    public static BlockFace fromOffset(Vector3i offset) {
        return fromOffset(offset.toFloat());
    }

    public static BlockFace fromOffset(Vector3f offset) {
        return OFFSET_MAP.get(getOffsetHash(offset.normalize().round().toInt()));
    }

    private static int getOffsetHash(Vector3i offset) {
        int x = offset.getX() + 1;
        int y = offset.getY() + 1;
        int z = offset.getZ() + 1;
        return x | y << 2 | z << 4;
    }
}
