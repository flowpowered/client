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

public class AtomicShortIntArrayTest {
    final AtomicShortIntArray a = new AtomicShortIntArray(256);
    final int[] copy = new int[256];
    final int COUNT = 16384;
    final int THREADS = 8;
    final int THREAD_REPEATS = 8;

    @Test
    public void repeatTest() {

        for (int i = 0; i < a.length(); i++) {
            set(i, 12345);
        }

        checkWidth(1);

        for (int i = 0; i < a.length(); i += 2) {
            set(i, 54321);
        }

        checkWidth(2);

        for (int i = 0; i < a.length(); i++) {
            check(i);
        }
    }

    @Test
    public void rampTest() {

        for (int i = 0; i < a.length(); i++) {
            set(i, i);
        }

        checkWidth(8);

        for (int i = 0; i < a.length(); i++) {
            check(i);
        }
    }

    @Test
    public void randomTest() {

        Random r = new Random();

        for (int i = 0; i < 1024; i++) {
            set(r.nextInt(256), r.nextInt());
        }

        for (int i = 0; i < 256; i++) {
            check(i);
        }

        checkWidth(8);

        a.compress();

        checkWidth(8);

        for (int i = 0; i < 256; i++) {
            check(i);
        }

        for (int i = 0; i < 1024; i++) {
            set(r.nextInt(256), r.nextInt());
        }

        checkWidth(8);

        for (int i = 0; i < 256; i++) {
            check(i);
        }

        for (int i = 0; i < 128; i++) {
            set(i, 123);
        }

        a.compress();

        checkWidth(8);

        for (int i = 0; i < 256; i++) {
            if (i < 246) {
                set(i, 321);
            } else {
                set(i, i);
            }
        }

        a.compress();

        checkWidth(4);

        for (int i = 0; i < 256; i++) {
            check(i);
        }
    }

    @Test
    public void compressionTest() {

        Random r = new Random();

        checkCompress(1, 1, r.nextInt());

        checkCompress(4, 2, r.nextInt());

        checkCompress(15, 4, r.nextInt());

        checkCompress(8, 4, r.nextInt());

        checkCompress(256, 8, r.nextInt());

        checkCompress(16, 4, r.nextInt());

        checkCompress(9, 4, r.nextInt());

        checkCompress(64, 8, r.nextInt());

        checkCompress(17, 8, r.nextInt());

        checkCompress(8, 4, r.nextInt());

        checkCompress(4, 2, r.nextInt());

        checkCompress(128, 8, r.nextInt());

        for (int i = 0; i < 256; i++) {
            check(i);
        }
    }

    @Test
    public void compareAndSet() {

        for (int i = 0; i < 256; i++) {
            set(i, i & 7);
        }

        for (int i = 0; i < 256; i++) {
            add(i, 16);
        }

        for (int i = 0; i < 256; i++) {
            check(i);
        }
    }

    private void checkCompress(int unique, int expWidth, int base) {
        for (int i = 0; i < 256; i++) {
            set(i, base + (i % unique));
        }

        a.compress();

        checkWidth(expWidth);

        System.out.println("");
    }

    Exception parallelException = null;
    Error parallelError = null;

    @Test
    public void parallel() {

        for (int i = 0; i < THREAD_REPEATS; i++) {
            parallelRun();
        }
    }

    private void parallelRun() {

        for (int i = 0; i < 256; i++) {
            a.set(i, 0);
        }
        a.compress();

        Thread[] thread = new Thread[THREADS];

        for (int i = 0; i < THREADS; i++) {
            thread[i] = new ArrayTest(i, THREADS, COUNT);
        }

        for (int i = 0; i < THREADS; i++) {
            thread[i].start();
        }

        for (int i = 0; i < THREADS; i++) {
            try {
                thread[i].join();
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            }
        }

        if (parallelException != null) {
            throw new RuntimeException("Exception thrown by thread", parallelException);
        }

        if (parallelError != null) {
            throw new RuntimeException("Error thrown by thread", parallelError);
        }
    }

    private void checkWidth(int exp) {
        assertTrue("Internal array has wrong width, got " + a.width() + ", exp " + exp, a.width() == exp);
    }

    private void set(int i, int value) {
        check(i);
        a.set(i, value);
        copy[i] = value;
        check(i);
    }

    private void add(int i, int delta) {
        check(i);
        int oldValue = a.get(i);
        int newValue = oldValue + delta;
        assertTrue("Unable to increment value " + delta + " for entry " + i, a.compareAndSet(i, oldValue, newValue));
        copy[i] = newValue;
    }

    private void check(int i) {
        int old = a.get(i);
        assertTrue("Old value did not match expected at position " + i + " (got " + old + ", expected " + copy[i] + ")", old == copy[i]);
    }

    private class ArrayTest extends Thread {
        public final int count;
        public final int base;
        public final int step;
        public int[] value = new int[a.length()];

        public ArrayTest(int base, int step, int count) {
            this.base = base;
            this.step = step;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                int length = a.length();
                Random r = new Random();
                int pos = base;
                for (int i = 0; i < count; i++) {
                    int got = a.get(pos);
                    int exp = value[pos];
                    assertTrue("Element mismatch at " + pos + " got=" + got + ", exp=" + exp, exp == got);
                    int val = r.nextInt();
                    value[pos] = val;
                    a.set(pos, val);

                    pos += step;
                    if (pos >= length) {
                        pos -= length;
                    }
                }
                for (int i = base; i < length; i += step) {
                    assertTrue("Element mismatch during final check at " + i + " got=" + a.get(i) + ", exp=" + value[i], a.get(i) == value[i]);
                }
            } catch (Exception exp) {
                exp.printStackTrace();
                parallelException = exp;
            } catch (Error err) {
                err.printStackTrace();
                parallelError = err;
            }
        }
    }
}
