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
package org.spoutcraft.client.game;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * An enum of the game difficulties.
 */
public enum Difficulty {
    EASY(0),
    HARD(3),
    NORMAL(2),
    PEACEFUL(1);
    private final int value;
    private static final TIntObjectHashMap<Difficulty> map = new TIntObjectHashMap<>();

    static {
        for (Difficulty dim : Difficulty.values()) {
            map.put(dim.value(), dim);
        }
    }

    private Difficulty(int value) {
        this.value = value;
    }

    /**
     * Returns the numerical value associated to the difficulty.
     *
     * @return The numerical value
     */
    public int value() {
        return value;
    }

    /**
     * Returns the difficulty associated to the numerical value (ID).
     *
     * @param id The ID to lookup
     * @return The associated difficulty
     */
    public static Difficulty get(int id) {
        return map.get(id);
    }

    /**
     * Returns the difficulty of the given name.
     *
     * @param name The name to lookup
     * @return The associated difficulty
     */
    public static Difficulty get(String name) {
        return valueOf(name.toUpperCase());
    }
}
