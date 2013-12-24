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
package org.spoutcraft.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.spout.math.vector.Vector3i;

import org.spoutcraft.client.ticking.TickingElement;
import org.spoutcraft.client.universe.Chunk;
import org.spoutcraft.client.universe.World;
import org.spoutcraft.client.universe.snapshot.WorldSnapshot;

/**
 * Contains and manages all the voxel worlds.
 */
public class Universe extends TickingElement {
    private static final int TPS = 20;
    private final Map<UUID, World> worlds = new HashMap<>();
    private final Map<UUID, WorldSnapshot> worldSnapshots = new HashMap<>();

    public Universe() {
        super(TPS);
    }

    @Override
    public void onStart() {
        System.out.println("Universe start");

        // TEST CODE
        final short[] halfChunkIDs = new short[Chunk.BLOCKS.VOLUME];
        final short[] halfChunkSubIDs = new short[Chunk.BLOCKS.VOLUME];
        for (int xx = 0; xx < Chunk.BLOCKS.SIZE; xx++) {
            for (int yy = 0; yy < Chunk.BLOCKS.SIZE / 2; yy++) {
                for (int zz = 0; zz < Chunk.BLOCKS.SIZE; zz++) {
                    halfChunkIDs[yy << Chunk.BLOCKS.DOUBLE_BITS | zz << Chunk.BLOCKS.BITS | xx] = 1;
                }
            }
        }
        final World world = new World();
        for (int xx = 0; xx < 10; xx++) {
            for (int zz = 0; zz < 10; zz++) {
                world.setChunk(new Chunk(world, new Vector3i(xx, 0, zz), halfChunkIDs, halfChunkSubIDs));
            }
        }
        worlds.put(world.getID(), world);
        worldSnapshots.put(world.getID(), world.buildSnapshot());
    }

    @Override
    public void onTick() {
        System.out.println("Universe tick");

        // TEST CODE
        for (Entry<UUID, World> entry : worlds.entrySet()) {
            final UUID id = entry.getKey();
            worlds.get(id).updateSnapshot(worldSnapshots.get(id));
        }
    }

    @Override
    public void onStop() {
        System.out.println("Universe stop");

        // TEST CODE
        worlds.clear();
        worldSnapshots.clear();
    }
}
