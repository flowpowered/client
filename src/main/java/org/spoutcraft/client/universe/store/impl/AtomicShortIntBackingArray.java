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

import java.util.concurrent.atomic.AtomicIntegerArray;

import gnu.trove.set.hash.TIntHashSet;

public abstract class AtomicShortIntBackingArray {
    private final int length;

    /**
     * Creates an AtomicShortIntArray
     *
     * @param length the number of entries
     */
    public AtomicShortIntBackingArray(int length) {
        this.length = length;
    }

    /**
     * Gets the width of the internal array, in bits
     *
     * @return the width
     */
    public abstract int width();

    /**
     * Gets the length of the array
     *
     * @return the length
     */
    public int length() {
        return length;
    }

    /**
     * Gets the size of the internal palette
     *
     * @return the palette size
     */
    public abstract int getPaletteSize();

    /**
     * Gets the number of palette entries in use
     *
     * @return the number of entries
     */
    public abstract int getPaletteUsage();

    /**
     * Gets an element from the array at a given index
     *
     * @param i the index
     * @return the element
     */
    public abstract int get(int i);

    /**
     * Sets an element to the given value
     *
     * @param i the index
     * @param newValue the new value
     * @return the old value
     */
    public abstract int set(int i, int newValue) throws PaletteFullException;

    /**
     * Sets the element at the given index, but only if the previous value was the expected value.
     *
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return true on success
     */
    public abstract boolean compareAndSet(int i, int expect, int update) throws PaletteFullException;

    public abstract boolean isPaletteMaxSize();

    /**
     * Gets the number of unique entries in the array
     */
    public int getUnique() {
        return getUnique(new TIntHashSet());
    }

    /**
     * Gets the number of unique entries in the array
     *
     * @param inUseSet set to use to store used ids
     */
    public int getUnique(TIntHashSet inUseSet) {
        inUseSet.clear();
        int unique = 0;
        for (int i = 0; i < length; i++) {
            if (inUseSet.add(get(i))) {
                unique++;
            }
        }
        return unique;
    }

    /**
     * Gets the palette in use by the backing array or an array of zero length if no palette is in use.
     */
    public abstract int[] getPalette();

    /**
     * Gets the packed array used by the backing store.  This is a flat array if there is no palette in use.
     */
    public abstract int[] getBackingArray();

    protected void copyFromPrevious(AtomicShortIntBackingArray previous) throws PaletteFullException {
        if (previous != null) {
            for (int i = 0; i < length; i++) {
                set(i, previous.get(i));
            }
        } else {
            set(0, 0);
        }
    }

    protected static int[] toIntArray(AtomicIntegerArray array, int length) {
        int[] packed = new int[length];
        for (int i = 0; i < length; i++) {
            packed[i] = array.get(i);
        }
        return packed;
    }

    protected static int[] toIntArray(AtomicIntegerArray array) {
        int length = array.length();
        int[] packed = new int[length];
        for (int i = 0; i < length; i++) {
            packed[i] = array.get(i);
        }
        return packed;
    }
}
