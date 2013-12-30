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

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import org.spoutcraft.client.util.set.ConcurrentRegularEnumSet;

public class ConcurrentRegularEnumSetTest {
    @Test
    public void testConstructor() {
        new ConcurrentRegularEnumSet<>(TestEnum.class);
    }

    @Test
    public void testAdd() {
        final ConcurrentRegularEnumSet<TestEnum> test = new ConcurrentRegularEnumSet<>(TestEnum.class);
        for (TestEnum t : TestEnum.values()) {
            test.add(t);
            Assert.assertTrue(test.contains(t));
        }
    }

    @Test
    public void testAddAll() {
        final ConcurrentRegularEnumSet<TestEnum> test = new ConcurrentRegularEnumSet<>(TestEnum.class);
        test.addAll(FULL);
        for (TestEnum t : TestEnum.values()) {
            Assert.assertTrue(test.contains(t));
        }
    }

    @Test
    public void testSize() {
        final ConcurrentRegularEnumSet<TestEnum> test = new ConcurrentRegularEnumSet<>(TestEnum.class);
        Assert.assertEquals(test.size(), 0);
        TestEnum[] values = TestEnum.values();
        for (int i = 0; i < values.length; i++) {
            TestEnum t = values[i];
            test.add(t);
            Assert.assertEquals(test.size(), i + 1);
        }
    }

    @Test
    public void testContains() {
        final ConcurrentRegularEnumSet<TestEnum> test = new ConcurrentRegularEnumSet<>(TestEnum.class);
        for (TestEnum t : TestEnum.values()) {
            test.add(t);
            Assert.assertTrue(test.contains(t));
        }
    }

    @Test
    public void testRemove() {
        final ConcurrentRegularEnumSet<TestEnum> test = new ConcurrentRegularEnumSet<>(TestEnum.class);
        test.addAll(FULL);
        for (TestEnum t : TestEnum.values()) {
            test.remove(t);
            Assert.assertFalse(test.contains(t));
        }
    }

    @Test
    public void testIsEmpty() {
        final ConcurrentRegularEnumSet<TestEnum> test = new ConcurrentRegularEnumSet<>(TestEnum.class);
        Assert.assertTrue(test.isEmpty());
        test.addAll(FULL);
        Assert.assertFalse(test.isEmpty());
        for (TestEnum t : TestEnum.values()) {
            test.remove(t);
        }
        Assert.assertTrue(test.isEmpty());
    }

    @Test
    public void testContainsAll() {
        final ConcurrentRegularEnumSet<TestEnum> test = new ConcurrentRegularEnumSet<>(TestEnum.class);
        final Set<TestEnum> valid = EnumSet.allOf(TestEnum.class);
        Assert.assertFalse(test.containsAll(valid));
        final TestEnum[] values = TestEnum.values();
        TestEnum[] values1 = TestEnum.values();
        for (int i = 0; i < values1.length; i++) {
            TestEnum t = values1[i];
            test.add(t);
            if (i < values.length - 1) {
                Assert.assertFalse(test.containsAll(valid));
            } else {
                Assert.assertTrue(test.containsAll(valid));
            }
        }
        for (TestEnum t : TestEnum.values()) {
            valid.remove(t);
            Assert.assertTrue(test.containsAll(valid));
        }
    }

    @Test
    public void testRemoveAll() {
        final ConcurrentRegularEnumSet<TestEnum> test = new ConcurrentRegularEnumSet<>(TestEnum.class);
        test.addAll(FULL);
        Assert.assertTrue(test.containsAll(FULL));
        test.removeAll(FULL);
        for (TestEnum t : TestEnum.values()) {
            Assert.assertFalse(test.contains(t));
        }
        Assert.assertTrue(test.isEmpty());
    }

    @Test
    public void testRetainAll() {
        final ConcurrentRegularEnumSet<TestEnum> test = new ConcurrentRegularEnumSet<>(TestEnum.class);
        test.addAll(FULL);
        test.retainAll(FULL);
        Assert.assertTrue(test.containsAll(FULL));
        test.retainAll(HALF_FULL);
        Assert.assertFalse(test.containsAll(FULL));
        Assert.assertTrue(test.containsAll(HALF_FULL));
    }

    @Test
    public void testIterator() {
        final ConcurrentRegularEnumSet<TestEnum> test = new ConcurrentRegularEnumSet<>(TestEnum.class);
        test.addAll(FULL);
        int i = 0;
        final TestEnum[] values = TestEnum.values();
        for (Iterator<TestEnum> iterator = test.iterator(); iterator.hasNext(); ) {
            final TestEnum t = iterator.next();
            Assert.assertEquals(values[i++], t);
            iterator.remove();
            Assert.assertEquals(test.size(), values.length - i);
            if (i > values.length) {
                Assert.fail();
            }
        }
        Assert.assertTrue(test.isEmpty());
    }

    @Test
    public void testToArray() {
        final TestEnum[] fullArray = (TestEnum[]) FULL.toArray();
        final TestEnum[] halfFullArray = (TestEnum[]) HALF_FULL.toArray();
        final TestEnum[] values = TestEnum.values();
        Assert.assertEquals(values.length, fullArray.length);
        Assert.assertEquals(values.length / 2, halfFullArray.length);
        for (int i = 0; i < values.length; i++) {
            Assert.assertEquals(values[i], fullArray[i]);
            if (i < values.length / 2) {
                Assert.assertEquals(values[i], halfFullArray[i]);
            }
        }
    }

    private static enum TestEnum {
        A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z
    }

    private static final Set<TestEnum> FULL = new ConcurrentRegularEnumSet<>(TestEnum.class);
    private static final Set<TestEnum> HALF_FULL = new ConcurrentRegularEnumSet<>(TestEnum.class);

    static {
        final TestEnum[] values = TestEnum.values();
        for (int i = 0; i < values.length; i++) {
            FULL.add(values[i]);
            if (i < values.length / 2) {
                HALF_FULL.add(values[i]);
            }
        }
    }
}
