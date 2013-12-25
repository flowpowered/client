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

import org.junit.Assert;
import org.junit.Test;

public class Int21TripleHashedTest {
    public void testValue(int x, int y, int z) {
        long key = Int21TripleHashed.key(x, y, z);
        Assert.assertEquals(x, Int21TripleHashed.key1(key));
        Assert.assertEquals(y, Int21TripleHashed.key2(key));
        Assert.assertEquals(z, Int21TripleHashed.key3(key));
    }

    @Test
    public void testHashes() {
        testValue(-1048575, -1048575, -1048575);
        testValue(0, 0, 0);
        testValue(1048575, 1048575, 1048575);
        testValue(1048575, -1048575, 1048575);
        testValue(-1048575, 1048575, -1048575);
        testValue(32423, 14144, 24114);
        testValue(10475, 104865, 104835);
        testValue(128, 512, 1024);
        testValue(-34, 2421, -4452);
    }
}
