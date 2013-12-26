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
package org.spoutcraft.client.universe.block.material;

import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TShortObjectHashMap;

import org.spoutcraft.client.universe.block.BlockFace;

/**
 *
 */
public abstract class Material {
    private static final TShortObjectMap<MasterMaterial> MATERIALS_BY_ID = new TShortObjectHashMap<>();
    private final short id;
    private final short subID;

    protected Material(short id, short subID) {
        this.id = id;
        this.subID = subID;
    }

    public short getID() {
        return id;
    }

    public short getSubID() {
        return subID;
    }

    public abstract boolean isVisible();

    public abstract boolean occludes(Material material, BlockFace direction);

    protected static void register(MasterMaterial material) {
        final MasterMaterial previous = MATERIALS_BY_ID.put(material.getID(), material);
        if (previous != null) {
            System.out.println("New material has conflicting ID, previous material was overwritten: " + previous + " => " + material);
        }
    }

    public static Material get(short id) {
        return get(id, (short) 0);
    }

    public static Material getPacked(int packed) {
        return get((short) (packed >> 16), (short) packed);
    }

    public static Material get(short id, short subID) {
        final MasterMaterial master = MATERIALS_BY_ID.get(id);
        if (master == null) {
            return Materials.AIR;
        }
        if (subID == 0) {
            return master;
        }
        final SubMaterial sub = master.getSubMaterial(subID);
        if (sub == null) {
            return master;
        }
        return sub;
    }

    @Override
    public String toString() {
        return "Material{" +
                "id=" + id +
                ", subID=" + subID +
                '}';
    }
}
