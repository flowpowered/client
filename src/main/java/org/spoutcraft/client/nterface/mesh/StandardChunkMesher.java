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
package org.spoutcraft.client.nterface.mesh;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;

import org.spout.math.vector.Vector3i;

import org.spoutcraft.client.nterface.mesh.Mesh.MeshAttribute;
import org.spoutcraft.client.universe.Chunk;
import org.spoutcraft.client.universe.block.BlockFace;
import org.spoutcraft.client.universe.block.material.Material;
import org.spoutcraft.client.universe.snapshot.ChunkSnapshot;

/**
 *
 */
public class StandardChunkMesher implements ChunkMesher {
    @Override
    public Mesh mesh(ChunkSnapshot chunk) {
        // TODO: add textures
        final Mesh mesh = new Mesh(MeshAttribute.POSITIONS, MeshAttribute.NORMALS);
        final TFloatList positions = mesh.getAttribute(MeshAttribute.POSITIONS);
        final TIntList indices = mesh.getIndices();

        final int x = chunk.getX() << Chunk.BLOCKS.BITS;
        final int y = chunk.getY() << Chunk.BLOCKS.BITS;
        final int z = chunk.getZ() << Chunk.BLOCKS.BITS;

        int index = 0;

        for (int xx = 0; xx < Chunk.BLOCKS.SIZE; xx++) {
            for (int zz = 0; zz < Chunk.BLOCKS.SIZE; zz++) {
                Material lastMaterial = null;
                for (int yy = 0; yy < Chunk.BLOCKS.SIZE; yy++) {
                    final Material currentMaterial = chunk.getMaterial(x + xx, y + yy, z + zz);

                    if (lastMaterial == null) {
                        lastMaterial = currentMaterial;
                        continue;
                    }

                    if (yy != -1) {
                        if (faceNeeded(lastMaterial, currentMaterial, BlockFace.TOP)) {
                            final Vector3i offset = BlockFace.TOP.getOffset();
                            final float fx = xx + (offset.getX() + 1) / 2;
                            final float fy = yy + (offset.getY() + 1) / 2;
                            final float fz = zz + (offset.getZ() + 1) / 2;
                            add(positions, fx, fy, fz);
                            add(positions, fx + 1, fy, fz);
                            add(positions, fx, fy, fz + 1);
                            add(positions, fx + 1, fy, fz + 1);
                            add(indices, index + 3, index + 1, index + 2, index + 2, index + 1, index);
                            index += 4;
                        }
                    }

                    if (yy != Chunk.BLOCKS.SIZE + 1) {
                        if (faceNeeded(currentMaterial, lastMaterial, BlockFace.BOTTOM)) {
                            final Vector3i offset = BlockFace.BOTTOM.getOffset();
                            final float fx = xx + (offset.getX() + 1) / 2;
                            final float fy = yy + (offset.getY() + 1) / 2;
                            final float fz = zz + (offset.getZ() + 1) / 2;
                            add(positions, fx, fy, fz);
                            add(positions, fx + 1, fy, fz);
                            add(positions, fx, fy, fz + 1);
                            add(positions, fx + 1, fy, fz + 1);
                            add(indices, index + 3, index + 2, index + 1, index + 2, index, index + 1);
                            index += 4;
                        }
                    }

                    lastMaterial = currentMaterial;
                }
            }
        }

        return mesh;
    }

    private boolean faceNeeded(Material current, Material last, BlockFace face) {
        return true;
    }

    private static void add(TFloatList list, float x, float y, float z) {
        list.add(x);
        list.add(y);
        list.add(z);
    }

    private static void add(TIntList list, int i0, int i1, int i2, int i3, int i4, int i5) {
        list.add(i0);
        list.add(i1);
        list.add(i2);
        list.add(i3);
        list.add(i4);
        list.add(i5);
    }
}
