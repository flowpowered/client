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

public enum GameMode {
    ADVENTURE(2),
    CREATIVE(1),
    HARDCORE(3),
    SURVIVAL(0);
    private final int value;
    private static final TIntObjectHashMap<GameMode> map = new TIntObjectHashMap<>();

    static {
        for (GameMode mode : GameMode.values()) {
            map.put(mode.value(), mode);
        }
    }

    private GameMode(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static GameMode get(int id) {
        return map.get(id);
    }

    public static GameMode get(String name) {
        return valueOf(name.toUpperCase());
    }
}
