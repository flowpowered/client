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
package org.spoutcraft.client.nterface.mesh;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

import org.spout.renderer.data.VertexAttribute;
import org.spout.renderer.data.VertexAttribute.DataType;
import org.spout.renderer.data.VertexData;
import org.spout.renderer.util.CausticUtil;

/**
 *
 */
public class Mesh {
    private final Map<MeshAttribute, TFloatList> attributes = new EnumMap<>(MeshAttribute.class);
    private final TIntList indices = new TIntArrayList();

    public Mesh(MeshAttribute... attributes) {
        for (MeshAttribute attribute : attributes) {
            this.attributes.put(attribute, new TFloatArrayList());
        }
    }

    public boolean hasAttribute(MeshAttribute attribute) {
        return attributes.containsKey(attribute);
    }

    public void addAttribute(MeshAttribute attribute) {
        if (!hasAttribute(attribute)) {
            attributes.put(attribute, new TFloatArrayList());
        }
    }

    public TFloatList getAttribute(MeshAttribute attribute) {
        return attributes.get(attribute);
    }

    public void removeAttribute(MeshAttribute attribute) {
        attributes.remove(attribute);
    }

    public TIntList getIndices() {
        return indices;
    }

    public VertexData build() {
        final VertexData vertexData = new VertexData();
        int i = 0;
        for (Entry<MeshAttribute, TFloatList> entry : attributes.entrySet()) {
            MeshAttribute attribute = entry.getKey();
            final VertexAttribute vertexAttribute = new VertexAttribute(attribute.getName(), DataType.FLOAT, attribute.getComponentCount());
            final TFloatList data = entry.getValue();
            if (data.isEmpty() && attribute.generateDataIfMissing()) {
                switch (attribute) {
                    case NORMALS:
                        CausticUtil.generateNormals(attributes.get(MeshAttribute.POSITIONS), indices, data);
                        break;
                    case TANGENTS:
                        CausticUtil.generateTangents(attributes.get(MeshAttribute.POSITIONS), attributes.get(MeshAttribute.NORMALS), attributes.get(MeshAttribute.TEXTURE_COORDS), indices, data);
                }
            }
            vertexAttribute.setData(data);
            vertexData.addAttribute(i++, vertexAttribute);
        }
        vertexData.getIndices().addAll(indices);
        return vertexData;
    }

    public static enum MeshAttribute {
        // Enum ordering is important here, don't change
        POSITIONS("positions", 3, false),
        NORMALS("normals", 3, true),
        TEXTURE_COORDS("textureCoords", 2, false),
        TANGENTS("tangents", 4, true);
        private final String name;
        private final int componentCount;
        private final boolean generateIfDataMissing;

        private MeshAttribute(String name, int componentCount, boolean generateIfDataMissing) {
            this.name = name;
            this.componentCount = componentCount;
            this.generateIfDataMissing = generateIfDataMissing;
        }

        public String getName() {
            return name;
        }

        public int getComponentCount() {
            return componentCount;
        }

        public boolean generateDataIfMissing() {
            return generateIfDataMissing;
        }
    }
}
