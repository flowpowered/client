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

public class AtomicShortIntUniformBackingArray extends AtomicShortIntBackingArray {
    private final AtomicInteger store;

    public AtomicShortIntUniformBackingArray(int length) {
        this(length, null);
    }

    public AtomicShortIntUniformBackingArray(AtomicShortIntBackingArray previous) {
        this(previous.length(), previous);
    }

    private AtomicShortIntUniformBackingArray(int length, AtomicShortIntBackingArray previous) {
        super(length);
        if (previous == null) {
            store = new AtomicInteger(0);
        } else {
            store = new AtomicInteger(previous.get(0));
        }
        try {
            copyFromPrevious(previous);
        } catch (PaletteFullException e) {
            throw new IllegalStateException("Unable to create uniform block store");
        }
    }

    public AtomicShortIntUniformBackingArray(int length, int initial) {
        super(length);
        store = new AtomicInteger(initial);
    }

    @Override
    public int width() {
        return 0;
    }

    @Override
    public int getPaletteSize() {
        return 1;
    }

    @Override
    public int getPaletteUsage() {
        return 1;
    }

    @Override
    public int get(int i) {
        return store.get();
    }

    @Override
    public int set(int i, int newValue) throws PaletteFullException {
        if (!store.compareAndSet(newValue, newValue)) {
            throw new PaletteFullException();
        }
        return newValue;
    }

    @Override
    public boolean compareAndSet(int i, int expect, int update) throws PaletteFullException {
        if (store.get() != expect) {
            return false;
        } else {
            if (expect != update) {
                throw new PaletteFullException();
            }
            return store.compareAndSet(expect, update);
        }
    }

    @Override
    public boolean isPaletteMaxSize() {
        return false;
    }

    @Override
    public int[] getPalette() {
        return new int[] {store.get()};
    }

    @Override
    public int[] getBackingArray() {
        return new int[] {};
    }
}
