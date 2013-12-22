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

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import gnu.trove.set.hash.TIntHashSet;

/**
 * An integer array that has a short index.  The array is atomic and is backed by a palette based lookup system.
 */
public class AtomicShortIntArray {
    /**
     * The length of the array
     */
    private final int length;
    /**
     * A reference to the store.  When the palette fills, or when the store is compressed.  A new store is created.
     */
    private final AtomicReference<AtomicShortIntBackingArray> store = new AtomicReference<>();
    /**
     * Locks<br> A ReadWrite lock is used to managing locking<br> When copying to a new store instance, and updating to new the store reference, all updates must be stopped.  The write lock is used as
     * the resize lock.<br> When making changes to the data stored in an array instance, multiple threads can access the array concurrently.  The read lock is used for the update lock. Reads to the
     * array are atomic and do not require any locking. <br>
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock resizeLock = lock.writeLock();
    private final Lock updateLock = lock.readLock();

    public AtomicShortIntArray(int length) {
        this.length = length;
        store.set(new AtomicShortIntUniformBackingArray(length));
    }

    /**
     * Gets the width of the internal array, in bits
     *
     * @return the width
     */
    public int width() {
        return store.get().width();
    }

    /**
     * Gets the length of the array
     *
     * @return the length
     */
    public final int length() {
        return length;
    }

    /**
     * Gets the size of the internal palette
     *
     * @return the palette size
     */
    public int getPaletteSize() {
        return store.get().getPaletteSize();
    }

    /**
     * Gets the number of palette entries in use
     *
     * @return the number of entries
     */
    public int getPaletteUsage() {
        return store.get().getPaletteUsage();
    }

    /**
     * Gets an element from the array at a given index
     *
     * @param i the index
     * @return the element
     */
    public int get(int i) {
        return store.get().get(i);
    }

    /**
     * Sets an element to the given value
     *
     * @param i the index
     * @param newValue the new value
     * @return the old value
     */
    public int set(int i, int newValue) {
        while (true) {
            try {
                updateLock.lock();
                try {
                    return store.get().set(i, newValue);
                } finally {
                    updateLock.unlock();
                }
            } catch (PaletteFullException pfe) {
                resizeLock.lock();
                try {
                    try {
                        return store.get().set(i, newValue);
                    } catch (PaletteFullException pfe2) {
                        if (store.get().isPaletteMaxSize()) {
                            store.set(new AtomicShortIntDirectBackingArray(store.get()));
                        } else {
                            store.set(new AtomicShortIntPaletteBackingArray(store.get(), true));
                        }
                    }
                } finally {
                    resizeLock.unlock();
                }
            }
        }
    }

    /**
     * Sets the array equal to the given array.  The array should be the same length as this array
     *
     * @param initial the array containing the new values
     */
    public void set(int[] initial) {
        resizeLock.lock();
        try {
            if (initial.length != length) {
                throw new IllegalArgumentException("Array length mismatch, expected " + length + ", got " + initial.length);
            }
            int unique = AtomicShortIntArray.getUnique(initial);
            int allowedPalette = AtomicShortIntPaletteBackingArray.getAllowedPalette(length);
            if (unique == 1) {
                store.set(new AtomicShortIntUniformBackingArray(length, initial[0]));
            } else if (unique > allowedPalette) {
                store.set(new AtomicShortIntDirectBackingArray(length, initial));
            } else {
                store.set(new AtomicShortIntPaletteBackingArray(length, unique, initial));
            }
        } finally {
            resizeLock.unlock();
        }
    }

    /**
     * Sets the array equal to the given array without automatically compressing the data. The array should be the same length as this array
     *
     * @param initial the array containing the new values
     */
    public void uncompressedSet(int[] initial) {
        resizeLock.lock();
        try {
            if (initial.length != length) {
                throw new IllegalArgumentException("Array length mismatch, expected " + length + ", got " + initial.length);
            }
            store.set(new AtomicShortIntDirectBackingArray(length, initial));
        } finally {
            resizeLock.unlock();
        }
    }

    /**
     * Sets the array equal to the given palette based array.  The main array should be the same length as this array
     *
     * @param palette the palette, if the palette is of length 0, variableWidthBlockArray contains the data, in flat format
     * @param blockArrayWidth the with of each entry in the main array
     * @param variableWidthBlockArray the array containing the new values, packed into ints
     */
    public void set(int[] palette, int blockArrayWidth, int[] variableWidthBlockArray) {
        resizeLock.lock();
        try {
            if (palette.length == 0) {
                store.set(new AtomicShortIntDirectBackingArray(length, variableWidthBlockArray));
            } else if (palette.length == 1) {
                store.set(new AtomicShortIntUniformBackingArray(length, palette[0]));
            } else {
                store.set(new AtomicShortIntPaletteBackingArray(length, palette, blockArrayWidth, variableWidthBlockArray));
            }
        } finally {
            resizeLock.unlock();
        }
    }

    /**
     * Sets the element at the given index, but only if the previous value was the expected value.
     *
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return true on success
     */
    public boolean compareAndSet(int i, int expect, int update) {
        while (true) {
            try {
                updateLock.lock();
                try {
                    return store.get().compareAndSet(i, expect, update);
                } finally {
                    updateLock.unlock();
                }
            } catch (PaletteFullException pfe) {
                resizeLock.lock();
                try {
                    if (store.get().isPaletteMaxSize()) {
                        store.set(new AtomicShortIntDirectBackingArray(store.get()));
                    } else {
                        store.set(new AtomicShortIntPaletteBackingArray(store.get(), true));
                    }
                } finally {
                    resizeLock.unlock();
                }
            }
        }
    }

    /**
     * Attempts to compress the array
     */
    public void compress() {
        compress(new TIntHashSet());
    }

    /**
     * Attempts to compress the array
     *
     * @param inUseSet to use to store used ids
     */
    public void compress(TIntHashSet inUseSet) {
        resizeLock.lock();
        try {
            AtomicShortIntBackingArray s = store.get();
            if (s instanceof AtomicShortIntUniformBackingArray) {
                return;
            }
            int unique = s.getUnique(inUseSet);
            if (AtomicShortIntPaletteBackingArray.roundUpWidth(unique - 1) >= s.width()) {
                return;
            }
            if (unique > AtomicShortIntPaletteBackingArray.getAllowedPalette(s.length())) {
                return;
            }
            if (unique == 1) {
                store.set(new AtomicShortIntUniformBackingArray(s));
            } else {
                store.set(new AtomicShortIntPaletteBackingArray(s, length, true, false, unique));
            }
        } finally {
            resizeLock.unlock();
        }
    }

    /**
     * Gets the number of unique entries in the array
     */
    public int getUnique() {
        TIntHashSet inUse = new TIntHashSet();
        int unique = 0;
        for (int i = 0; i < length; i++) {
            if (inUse.add(get(i))) {
                unique++;
            }
        }
        return unique;
    }

    /**
     * Gets the palette in use by the backing array or an array of zero length if no palette is in use.<br> <br> Data tearing may occur if the store is updated during this method call.
     */
    public int[] getPalette() {
        return store.get().getPalette();
    }

    /**
     * Gets the packed array used by the backing store.  This is a flat array if there is no palette in use.<br> <br> Data tearing may occur if the store is updated during this method call.
     */
    public int[] getBackingArray() {
        return store.get().getBackingArray();
    }

    private static int getUnique(int[] initial) {
        TIntHashSet inUse = new TIntHashSet();
        int unique = 0;
        for (int anInitial : initial) {
            if (inUse.add(anInitial)) {
                unique++;
            }
        }
        return unique;
    }

    /**
     * Locks the store so that reads and writes are prevented
     */
    public void lock() {
        resizeLock.lock();
    }

    /**
     * Unlocks the store
     */
    public void unlock() {
        resizeLock.unlock();
    }

    /**
     * Attempts to lock the store
     *
     * @return true on success
     */
    public boolean tryLock() {
        return resizeLock.tryLock();
    }

    /**
     * Gets if the store is uniform
     */
    public boolean isUniform() {
        return store.get() instanceof AtomicShortIntUniformBackingArray;
    }
}
