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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.spout.math.GenericMath;

/**
 * This class implements a variable width Atomic array.  It is backed by an AtomicInteger array.<br> <br> Entries widths can be a power of 2 from 1 to 32
 */
public class AtomicVariableWidthArray implements Serializable {
    private static final long serialVersionUID = 423785245671235L;
    private final static int[] log2 = new int[33];

    static {
        log2[1] = 0;
        log2[2] = 1;
        log2[4] = 2;
        log2[8] = 3;
        log2[16] = 4;
        log2[32] = 5;
    }

    private final boolean fullWidth;
    private final int indexShift;
    private final int subIndexMask;
    private final int[] valueBitmask;
    private final int[] valueShift;
    private final int maxValue;
    private final int width;
    private AtomicIntegerArray array;
    private final int length;

    /**
     * Creates a variable Atomic array.  The width must be a power of two from 1 to 32 and the length must be a multiple of the number of elements that fit in an int after packing.
     *
     * @param length the length of the array
     * @param width the number of bits in each entry
     */
    public AtomicVariableWidthArray(int length, int width) {
        this(length, width, null);
    }

    /**
     * Creates a variable Atomic array.  The width must be a power of two from 1 to 32 and the length must be a multiple of the number of elements that fit in an int after packing.
     *
     * @param length the length of the array
     * @param width the number of bits in each entry
     * @param initial the initial state of the array (in packed format)
     */
    public AtomicVariableWidthArray(int length, int width, int[] initial) {
        if (GenericMath.roundUpPow2(width) != width || width < 1 || width > 32) {
            throw new IllegalArgumentException("Width must be a power of 2 between 1 and 32 " + width);
        }

        indexShift = 5 - log2[width];
        subIndexMask = (1 << indexShift) - 1;

        int valuesPerInt = 32 / width;

        valueBitmask = new int[valuesPerInt];
        valueShift = new int[valuesPerInt];

        for (int i = 0; i < valuesPerInt; i++) {
            valueShift[i] = i * width;
            valueBitmask[i] = ((1 << width) - 1) << valueShift[i];
        }

        this.length = length;

        int newLength = length / valuesPerInt;

        if (newLength * valuesPerInt != length) {
            throw new IllegalArgumentException("The length must be a multiple of " + valuesPerInt + " for arrays of width " + width);
        }

        if (initial != null) {
            if (newLength != initial.length) {
                throw new IllegalArgumentException("Length of packed array did not match expected");
            }
            this.array = new AtomicIntegerArray(initial);
        } else {
            this.array = new AtomicIntegerArray(newLength);
        }

        this.fullWidth = width == 32;

        this.maxValue = this.fullWidth ? -1 : valueBitmask[0];

        this.width = width;
    }

    /**
     * Gets the maximum unsigned value that can be stored in the array
     *
     * @return the max value
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * Gets an element from the array at a given index
     *
     * @param i the index
     * @return the element
     */
    public final int get(int i) {
        if (fullWidth) {
            return array.get(i);
        }

        return unPack(array.get(getIndex(i)), getSubIndex(i));
    }

    /**
     * Sets an element to the given value
     *
     * @param i the index
     * @param newValue the new value
     */
    public final void set(int i, int newValue) {
        if (fullWidth) {
            array.set(i, newValue);
            return;
        }

        boolean success = false;
        int index = getIndex(i);
        int subIndex = getSubIndex(i);
        while (!success) {
            int prev = array.get(index);
            int next = pack(prev, newValue, subIndex);
            success = array.compareAndSet(index, prev, next);
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
    public final boolean compareAndSet(int i, int expect, int update) {
        if (fullWidth) {
            return array.compareAndSet(i, expect, update);
        }

        boolean success = false;
        int index = getIndex(i);
        int subIndex = getSubIndex(i);
        while (!success) {
            int prev = array.get(index);
            if (unPack(prev, subIndex) != expect) {
                return false;
            }

            int next = pack(prev, update, subIndex);
            success = array.compareAndSet(index, prev, next);
        }
        return true;
    }

    /**
     * Sets an element in the array at a given index and returns the old value
     *
     * @param i the index
     * @param newValue the new value
     * @return the old value
     */
    public final int getAndSet(int i, int newValue) {
        if (fullWidth) {
            return array.getAndSet(i, newValue);
        }

        boolean success = false;
        int index = getIndex(i);
        int subIndex = getSubIndex(i);
        int prev = 0;
        while (!success) {
            prev = array.get(index);
            int next = pack(prev, newValue, subIndex);
            success = array.compareAndSet(index, prev, next);
        }
        return unPack(prev, subIndex);
    }

    private int addAndGet(int i, int delta, boolean old) {
        if (fullWidth) {
            if (old) {
                return array.getAndAdd(i, delta);
            } else {
                return array.addAndGet(i, delta);
            }
        }

        boolean success = false;
        int index = getIndex(i);
        int subIndex = getSubIndex(i);
        int prev;
        int prevValue = 0;
        int newValue = 0;
        while (!success) {
            prev = array.get(index);
            prevValue = unPack(prev, subIndex);
            newValue = prevValue + delta;
            int next = pack(prev, newValue, subIndex);
            success = array.compareAndSet(index, prev, next);
        }
        return (old ? prevValue : newValue) & valueBitmask[0];
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
     * Gets the width of the array
     *
     * @return the width
     */
    public final int width() {
        return width;
    }

    /**
     * Gets an array containing all the values in the array. The returned values are not guaranteed to be from the same time instant.
     * <p/>
     * If an array is provided and it is the correct length, then that array will be used as the destination array.
     *
     * @param array the provided array
     * @return an array containing the values in the array
     */
    public final int[] getArray(int[] array) {
        if (array == null || array.length != length()) {
            array = new int[length()];
        }

        for (int i = 0; i < length(); i++) {
            array[i] = get(i);
        }

        return array;
    }

    /**
     * Gets a packed version of this array.  Tearing may occur if the array is updated during this method call.
     */
    public int[] getPacked() {
        int length = this.array.length();
        int[] packed = new int[length];
        for (int i = 0; i < length; i++) {
            packed[i] = this.array.get(i);
        }
        return packed;
    }

	/*
	 * Remaining methods use the above methods
	 */

    public int addAndGet(int i, int delta) {
        return addAndGet(i, delta, false);
    }

    public int getAndAdd(int i, int delta) {
        return addAndGet(i, delta, true);
    }

    public int incrementAndGet(int i) {
        return addAndGet(i, 1);
    }

    public int decrementAndGet(int i) {
        return addAndGet(i, -1);
    }

    public int getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }

    public int getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }

    private int getIndex(int i) {
        return i >> indexShift;
    }

    private int getSubIndex(int i) {
        return subIndexMask & i;
    }

    private int unPack(int packed, int subIndex) {
        return (packed & valueBitmask[subIndex]) >>> valueShift[subIndex];
    }

    private int pack(int prev, int newValue, int subIndex) {
        int bitmask = valueBitmask[subIndex];
        int shift = valueShift[subIndex];
        return (prev & ~bitmask) | (bitmask & (newValue << shift));
    }
}
