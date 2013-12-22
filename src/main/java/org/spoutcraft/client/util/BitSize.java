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
package org.spoutcraft.client.util;

/**
 * Stores the size that spans a fixed amount of bits<br> For example: 1, 2, 4, 8, 16 and 32 sizes match this description
 */
public class BitSize {
    public final int SIZE;
    public final int HALF_SIZE;
    public final int DOUBLE_SIZE;
    public final int MASK;
    public final int BITS;
    public final int DOUBLE_BITS;
    public final int AREA;
    public final int HALF_AREA;
    public final int DOUBLE_AREA;
    public final int VOLUME;
    public final int HALF_VOLUME;
    public final int DOUBLE_VOLUME;

    public BitSize(int bitCount) {
        this.BITS = bitCount;
        this.SIZE = 1 << bitCount;
        this.AREA = this.SIZE * this.SIZE;
        this.VOLUME = this.AREA * this.SIZE;
        this.HALF_SIZE = this.SIZE >> 1;
        this.HALF_AREA = this.AREA >> 1;
        this.HALF_VOLUME = this.VOLUME >> 1;
        this.DOUBLE_SIZE = this.SIZE << 1;
        this.DOUBLE_AREA = this.AREA << 1;
        this.DOUBLE_VOLUME = this.VOLUME << 1;
        this.DOUBLE_BITS = this.BITS << 1;
        this.MASK = this.SIZE - 1;
    }
}
