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

public class AtomicShortIntDirectBackingArray extends AtomicShortIntBackingArray {
    private final static int[] NO_PALETTE = new int[0];
    private final AtomicIntegerArray store;
    private final int width;

    public AtomicShortIntDirectBackingArray(int length) {
        this(length, (AtomicShortIntBackingArray) null);
    }

    public AtomicShortIntDirectBackingArray(AtomicShortIntBackingArray previous) {
        this(previous.length(), previous);
    }

    private AtomicShortIntDirectBackingArray(int length, AtomicShortIntBackingArray previous) {
        super(length);
        store = new AtomicIntegerArray(length);
        width = AtomicShortIntPaletteBackingArray.roundUpWidth(length - 1);
        try {
            copyFromPrevious(previous);
        } catch (PaletteFullException pfe) {
            throw new IllegalStateException("Unable to copy old array to new array");
        }
    }

    public AtomicShortIntDirectBackingArray(int length, int[] initial) {
        super(length);
        if (initial.length != length) {
            throw new IllegalArgumentException("The length of the initialization array must match the given length");
        }
        store = new AtomicIntegerArray(initial);
        width = AtomicShortIntPaletteBackingArray.roundUpWidth(length - 1);
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int getPaletteSize() {
        return length();
    }

    @Override
    public int getPaletteUsage() {
        return length();
    }

    @Override
    public int get(int i) {
        return store.get(i);
    }

    @Override
    public int set(int i, int newValue) throws PaletteFullException {
        return store.getAndSet(i, newValue);
    }

    @Override
    public boolean compareAndSet(int i, int expect, int update) throws PaletteFullException {
        return store.compareAndSet(i, expect, update);
    }

    @Override
    public boolean isPaletteMaxSize() {
        return true;
    }

    @Override
    public int[] getPalette() {
        return NO_PALETTE;
    }

    @Override
    public int[] getBackingArray() {
        return toIntArray(store);
    }
}
