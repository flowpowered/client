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

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * An atomic HashMap that maps integers to positive short values<br> <br> Once a key value pair is set, it cannot be changed again
 */
public class AtomicIntShortSingleUseHashMap {
    private final static short EMPTY_VALUE = -1;
    private final static long EMPTY_ENTRY = 0xFFFF000000000000L;
    private final AtomicLongArray array;
    private final int length;

    AtomicIntShortSingleUseHashMap(int length) {
        this.array = new AtomicLongArray(length);
        for (int i = 0; i < length; i++) {
            this.array.set(i, EMPTY_ENTRY);
        }
        this.length = length;
    }

    public short get(int key) {
        int hashed = hash(key);
        int index = hashed;
        long probedEntry;
        boolean empty;
        while (!(empty = isEmpty(probedEntry = array.get(index))) && getKey(probedEntry) != key) {
            index = (index + 1) % length;
            if (index == hashed) {
                return EMPTY_VALUE;
            }
        }
        if (!empty) {
            return getValue(probedEntry);
        } else {
            return EMPTY_VALUE;
        }
    }

    public short putIfAbsent(int key, short value) {
        int hashed = hash(key);
        int index = hashed;
        long probedEntry = 0; // Doesn't actually need initialization
        boolean entrySet;
        while (!(entrySet = setEntry(index, key, value)) && getKey(probedEntry = array.get(index)) != key) {
            index = (index + 1) % length;
            if (index == hashed) {
                throw new IllegalStateException("Map is full");
            }
        }
        if (entrySet) {
            return EMPTY_VALUE;
        } else {
            return getValue(probedEntry);
        }
    }

    public boolean isEmptyValue(short value) {
        return value == EMPTY_VALUE;
    }

    private boolean setEntry(int index, int key, short value) {
        return array.compareAndSet(index, EMPTY_ENTRY, pack(key, value));
    }

    private int hash(int h) {
        h ^= (h >>> 20) ^ (h >>> 12);
        h = (h ^ (h >>> 7) ^ (h >>> 4));
        h = (h & 0x7FFFFFFF) % length;
        return h;
    }

    private static int getKey(long entry) {
        return (int) (entry >> 16);
    }

    private static short getValue(long entry) {
        return (short) entry;
    }

    private static boolean isEmpty(long entry) {
        return entry == EMPTY_ENTRY;
    }

    private static long pack(int key, short value) {
        return ((key & 0xFFFFFFFFL) << 16) | (value & 0xFFFFL);
    }
}
