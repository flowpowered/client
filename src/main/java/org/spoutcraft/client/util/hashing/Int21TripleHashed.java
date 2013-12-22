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
package org.spoutcraft.client.util.hashing;

/**
 * A class for hashing 3 21 bit integers into a long, and vice-versa.
 */
public class Int21TripleHashed {
    /**
     * Packs the most significant and the twenty least significant of each int into a <code>long</code>
     *
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param z an <code>int</code> value
     * @return the most significant and the twenty least significant of each int packed into a <code>long</code>
     */
    public static long key(int x, int y, int z) {
        return ((long) ((x >> 11) & 0x100000 | x & 0xFFFFF)) << 42 | ((long) ((y >> 11) & 0x100000 | y & 0xFFFFF)) << 21 | ((z >> 11) & 0x100000 | z & 0xFFFFF);
    }

    /**
     * Gets the first 21-bit integer value from a long key
     *
     * @param key to get from
     * @return the first 21-bit integer value in the key
     */
    public static int key1(long key) {
        return keyInt((key >> 42) & 0x1FFFFF);
    }

    /**
     * Gets the second 21-bit integer value from a long key
     *
     * @param key to get from
     * @return the second 21-bit integer value in the key
     */
    public static int key2(long key) {
        return keyInt((key >> 21) & 0x1FFFFF);
    }

    /**
     * Gets the third 21-bit integer value from a long key
     *
     * @param key to get from
     * @return the third 21-bit integer value in the key
     */
    public static int key3(long key) {
        return keyInt(key & 0x1FFFFF);
    }

    private static int keyInt(long key) {
        return (int) (key - ((key & 0x100000) << 1));
    }
}
