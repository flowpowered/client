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
package org.spoutcraft.client.universe.store.impl;

import java.util.concurrent.atomic.AtomicInteger;

import gnu.trove.set.hash.TIntHashSet;

import org.spout.math.vector.Vector3i;

import org.spoutcraft.client.universe.store.AtomicBlockStore;

public class AtomicPaletteBlockStore implements AtomicBlockStore {
    private final int shift;
    private final int doubleShift;
    private final int length;
    private final AtomicShortIntArray store;
    private final byte[] dirtyX;
    private final byte[] dirtyY;
    private final byte[] dirtyZ;
    private final int[] newState;
    private final int[] oldState;
    private final AtomicInteger maxX = new AtomicInteger();
    private final AtomicInteger maxY = new AtomicInteger();
    private final AtomicInteger maxZ = new AtomicInteger();
    private final AtomicInteger minX = new AtomicInteger();
    private final AtomicInteger minY = new AtomicInteger();
    private final AtomicInteger minZ = new AtomicInteger();
    private final AtomicInteger dirtyBlocks = new AtomicInteger(0);

    public AtomicPaletteBlockStore(int shift, boolean storeState) {
        this(shift, storeState, 10);
    }

    public AtomicPaletteBlockStore(int shift, boolean storeState, boolean compress, short[] initial) {
        this(shift, storeState, compress, 10, initial);
    }

    public AtomicPaletteBlockStore(int shift, boolean storeState, int dirtySize) {
        int side = 1 << shift;
        this.shift = shift;
        this.doubleShift = shift << 1;
        int size = side * side * side;
        store = new AtomicShortIntArray(size);
        this.length = size;
        dirtyX = new byte[dirtySize];
        dirtyY = new byte[dirtySize];
        dirtyZ = new byte[dirtySize];
        if (storeState) {
            oldState = new int[dirtySize];
            newState = new int[dirtySize];
        } else {
            oldState = null;
            newState = null;
        }
    }

    public AtomicPaletteBlockStore(int shift, boolean storeState, boolean compress, int dirtySize, short[] initial) {
        this(shift, storeState, compress, dirtySize, initial, null);
    }

    public AtomicPaletteBlockStore(int shift, boolean storeState, boolean compress, int dirtySize, short[] blocks, short[] data) {
        this(shift, storeState, dirtySize);
        if (blocks != null) {
            int[] initial = new int[Math.min(blocks.length, this.length)];
            for (int i = 0; i < blocks.length; i++) {
                short d = data != null ? data[i] : 0;
                initial[i] = blocks[i] << 16 | d & 0xFFFF;
            }
            if (compress) {
                store.set(initial);
            } else {
                store.uncompressedSet(initial);
            }
        }
    }

    public AtomicPaletteBlockStore(int shift, boolean storeState, boolean compress, int dirtySize, int[] palette, int blockArrayWidth, int[] variableWidthBlockArray) {
        this(shift, storeState, dirtySize);
        if (!compress) {
            throw new IllegalArgumentException("Cannot disable compression when loading from palette");
        }
        store.set(palette, blockArrayWidth, variableWidthBlockArray);
    }

    @Override
    public int getFullData(int x, int y, int z) {
        return getFullData(getIndex(x, y, z));
    }

    @Override
    public int getFullData(int index) {
        return store.get(index);
    }

    @Override
    public int getAndSetBlock(int x, int y, int z, short id, short data) {
        int newState = id << 16 | data & 0xFFFF;
        int oldState = 0;
        try {
            return oldState = store.set(getIndex(x, y, z), newState);
        } finally {
            markDirty(x, y, z, oldState, newState);
        }
    }

    @Override
    public int getAndSetBlock(int x, int y, int z, short id, short data, DataMask mask) {
        final int index = getIndex(x, y, z);
        data = mask.apply(data);
        boolean done = false;
        int oldState = 0, newState = 0;
        try {
            while (!done) {
                oldState = store.get(index);
                newState = id << 16 | (oldState & ~(mask.getMask() << mask.getShift()) & 0xFFFF | data & 0xFFFF);
                done = store.compareAndSet(index, oldState, newState);
            }
            return oldState;
        } finally {
            markDirty(x, y, z, oldState, newState);
        }
    }

    @Override
    public int touchBlock(int x, int y, int z) {
        int state = getFullData(x, y, z);
        markDirty(x, y, z, state, state);
        return state;
    }

    @Override
    public void setBlockId(int x, int y, int z, short id) {
        final int index = getIndex(x, y, z);
        boolean done = false;
        int oldState = 0, newState = 0;
        try {
            while (!done) {
                oldState = store.get(index);
                newState = id << 16 | oldState & 0xFFFF;
                done = store.compareAndSet(index, oldState, newState);
            }
        } finally {
            markDirty(x, y, z, oldState, newState);
        }
    }

    @Override
    public void setData(int x, int y, int z, short data) {
        final int index = getIndex(x, y, z);
        boolean done = false;
        int oldState = 0, newState = 0;
        try {
            while (!done) {
                oldState = store.get(index);
                newState = oldState & 0xFFFF0000 | data & 0xFFFF;
                done = store.compareAndSet(index, oldState, newState);
            }
        } finally {
            markDirty(x, y, z, oldState, newState);
        }
    }

    @Override
    public void setData(int x, int y, int z, short data, DataMask mask) {
        final int index = getIndex(x, y, z);
        data = mask.apply(data);
        boolean done = false;
        int oldState = 0, newState = 0;
        try {
            while (!done) {
                oldState = store.get(index);
                newState = oldState & 0xFFFF0000 | data & 0xFFFF;
                done = store.compareAndSet(index, oldState, newState);
            }
        } finally {
            markDirty(x, y, z, oldState, newState);
        }
    }

    @Override
    public void setBlock(int x, int y, int z, short id, short data) {
        getAndSetBlock(x, y, z, id, data);
    }

    @Override
    public void setBlock(int x, int y, int z, short id, short data, DataMask mask) {
        getAndSetBlock(x, y, z, id, data, mask);
    }

    @Override
    public short getBlockId(int x, int y, int z) {
        return (short) (getFullData(x, y, z) >> 16);
    }

    @Override
    public short getData(int x, int y, int z) {
        return (short) getFullData(x, y, z);
    }

    @Override
    public short getData(int x, int y, int z, DataMask mask) {
        return mask.extract(getData(x, y, z));
    }

    @Override
    public boolean compareAndSetBlock(int x, int y, int z, short expectId, short expectData, short newId, short newData) {
        int exp = expectId << 16 | expectData & 0xFFFF;
        int update = newId << 16 | newData & 0xFFFF;
        boolean success = store.compareAndSet(getIndex(x, y, z), exp, update);
        if (success && exp != update) {
            markDirty(x, y, z, exp, update);
        }
        return success;
    }

    @Override
    public boolean needsCompression() {
        // TODO - needs removal or optimisation
        return true;
    }

    @Override
    public short[] getBlockIdArray() {
        return getBlockIdArray(new short[length]);
    }

    @Override
    public short[] getBlockIdArray(short[] array) {
        if (array.length != length) {
            array = new short[length];
        }
        for (int i = 0; i < length; i++) {
            array[i] = (short) (store.get(i) >> 16);
        }
        return array;
    }

    @Override
    public short[] getDataArray() {
        return getDataArray(new short[length]);
    }

    @Override
    public short[] getDataArray(DataMask mask) {
        return getDataArray(new short[length], mask);
    }

    @Override
    public short[] getDataArray(short[] array) {
        if (array.length != length) {
            array = new short[length];
        }
        for (int i = 0; i < length; i++) {
            array[i] = (short) (store.get(i));
        }
        return array;
    }

    @Override
    public short[] getDataArray(short[] array, DataMask mask) {
        if (array.length != length) {
            array = new short[length];
        }
        for (int i = 0; i < length; i++) {
            array[i] = mask.extract((short) store.get(i));
        }
        return array;
    }

    @Override
    public void compress() {
        compress(new TIntHashSet());
    }

    @Override
    public void compress(TIntHashSet inUseSet) {
        store.compress(inUseSet);
    }

    @Override
    public boolean isDirtyOverflow() {
        return dirtyBlocks.get() >= dirtyX.length;
    }

    @Override
    public boolean isDirty() {
        return dirtyBlocks.get() > 0;
    }

    @Override
    public boolean resetDirtyArrays() {
        minX.set(Integer.MAX_VALUE);
        minY.set(Integer.MAX_VALUE);
        minZ.set(Integer.MAX_VALUE);
        maxX.set(Integer.MIN_VALUE);
        maxY.set(Integer.MIN_VALUE);
        maxZ.set(Integer.MIN_VALUE);
        return dirtyBlocks.getAndSet(0) > 0;
    }

    @Override
    public int getDirtyBlocks() {
        return dirtyBlocks.get();
    }

    @Override
    public Vector3i getMaxDirty() {
        return new Vector3i(maxX.get(), maxY.get(), maxZ.get());
    }

    @Override
    public Vector3i getMinDirty() {
        return new Vector3i(minX.get(), minY.get(), minZ.get());
    }

    @Override
    public Vector3i getDirtyBlock(int i) {
        if (i >= dirtyBlocks.get()) {
            return null;
        }

        return new Vector3i(dirtyX[i] & 0xFF, dirtyY[i] & 0xFF, dirtyZ[i] & 0xFF);
    }

    @Override
    public int getDirtyOldState(int i) {
        if (oldState == null || i >= dirtyBlocks.get()) {
            return -1;
        }

        return oldState[i];
    }

    @Override
    public int getDirtyNewState(int i) {
        if (newState == null || i >= dirtyBlocks.get()) {
            return -1;
        }

        return newState[i];
    }

    public void markDirty(int x, int y, int z, int oldState, int newState) {
        setAsMax(maxX, x);
        setAsMin(minX, x);

        setAsMax(maxY, y);
        setAsMin(minY, y);

        setAsMax(maxZ, z);
        setAsMin(minZ, z);

        int index = incrementDirtyIndex();
        if (index < dirtyX.length) {
            dirtyX[index] = (byte) x;
            dirtyY[index] = (byte) y;
            dirtyZ[index] = (byte) z;
            if (this.oldState != null) {
                this.oldState[index] = oldState;
                this.newState[index] = newState;
            }
        }
    }

    public int incrementDirtyIndex() {
        boolean success = false;
        int index = -1;
        while (!success) {
            index = dirtyBlocks.get();
            if (index > dirtyX.length) {
                break;
            }
            int next = index + 1;
            success = dirtyBlocks.compareAndSet(index, next);
        }
        return index;
    }

    private int getIndex(int x, int y, int z) {
        return (y << doubleShift) + (z << shift) + x;
    }

    @Override
    public int getPackedWidth() {
        return store.width();
    }

    @Override
    public int[] getPackedArray() {
        return store.getBackingArray();
    }

    @Override
    public int[] getPalette() {
        return store.getPalette();
    }

    @Override
    public void writeLock() {
        store.lock();
    }

    @Override
    public void writeUnlock() {
        store.unlock();
    }

    @Override
    public boolean tryWriteLock() {
        return store.tryLock();
    }

    @Override
    public boolean isBlockUniform() {
        return store.isUniform();
    }

    private void setAsMin(AtomicInteger i, int x) {
        int old;
        while ((old = i.get()) > x) {
            if (i.compareAndSet(old, x)) {
                return;
            }
        }
    }

    private void setAsMax(AtomicInteger i, int x) {
        int old;
        while ((old = i.get()) < x) {
            if (i.compareAndSet(old, x)) {
                return;
            }
        }
    }
}
