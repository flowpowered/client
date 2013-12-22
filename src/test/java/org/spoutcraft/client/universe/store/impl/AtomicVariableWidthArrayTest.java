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

import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AtomicVariableWidthArrayTest {
    private final static int LENGTH = 16384;
    private AtomicVariableWidthArray array;
    private int valueMask;
    private int width;
    private int[] arrayData;
    private int[] arrayIndex;

    public void setup(int width) {
        if (width == 32) {
            valueMask = -1;
        } else {
            valueMask = (1 << width) - 1;
        }
        this.width = width;

        array = new AtomicVariableWidthArray(LENGTH, width);

        Random rand = new Random();

        arrayData = new int[LENGTH];
        arrayIndex = new int[LENGTH];

        for (int i = 0; i < LENGTH; i++) {
            arrayData[i] = (short) rand.nextInt() & valueMask;
            arrayIndex[i] = i;
        }

        shuffle(arrayIndex);
    }

    private void shuffle(int[] deck) {
        Random rand = new Random();

        for (int placed = 0; placed < deck.length; placed++) {
            int remaining = deck.length - placed;
            int newIndex = rand.nextInt(remaining);
            swap(deck, placed, newIndex + placed);
        }
    }

    private void swap(int[] array, int i1, int i2) {
        int temp = array[i1];
        array[i1] = array[i2];
        array[i2] = temp;
    }

    @Test
    public void testArray() {
        for (int i = 1; i <= 32; i <<= 1) {
            setup(i);
            testArray(i);
        }
    }

    public void testArray(int width) {
        Random rand = new Random();

        for (int i = 0; i < LENGTH; i++) {
            int index = arrayIndex[i];
            array.set(index, arrayData[index]);
        }

        for (int i = 0; i < LENGTH; i++) {
            assertTrue("Width = " + width + " Array data mismatch: " + array.get(i) + ":" + arrayData[i], array.get(i) == arrayData[i]);
        }

        for (int i = 0; i < LENGTH; i++) {
            compareAndSetTrue(rand.nextInt(LENGTH), (short) rand.nextInt());
            compareAndSetFalse(rand.nextInt(LENGTH), (short) rand.nextInt());
        }

        for (int i = 0; i < LENGTH; i++) {
            assertTrue("Width = " + width + " Array data mismatch after compare and set updates", array.get(i) == arrayData[i]);
        }
    }

    private void compareAndSetTrue(int index, int value) {
        assertTrue("Width = " + width + " Compare and set attempt failed, index = " + index + ", expected value incorrect " + array.get(index) + " expected " + value, array.compareAndSet(index, arrayData[index], value));
        arrayData[index] = value & valueMask;
    }

    private void compareAndSetFalse(int index, int value) {
        assertTrue("Width = " + width + " Compare and set attempt succeeded, index = " + index + ", when it should have failed", !array.compareAndSet(index, (short) (1 + arrayData[index]), value));
    }
}
