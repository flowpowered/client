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
package org.spoutcraft.client.network.message.play;

import org.spoutcraft.client.network.message.ChannelMessage;

public class ChunkDataMessage extends ChannelMessage {
    private final int columnX;
    private final int columnZ;
    private final boolean groundUpContinuous;
    private final short primaryBitMap;
    private final short additionalDataBitMap;
    private final int compressedSize;
    private final byte[] compressedData;

    public ChunkDataMessage(int columnX, int columnZ, boolean groundUpContinuous, short primaryBitMap, short additionalDataBitMap, int compressedSize, byte[] compressedData) {
        this.columnX = columnX;
        this.columnZ = columnZ;
        this.groundUpContinuous = groundUpContinuous;
        this.primaryBitMap = primaryBitMap;
        this.additionalDataBitMap = additionalDataBitMap;
        this.compressedSize = compressedSize;
        this.compressedData = compressedData;
    }

    public int getColumnX() {
        return columnX;
    }

    public int getColumnZ() {
        return columnZ;
    }

    public boolean isGroundUpContinuous() {
        return groundUpContinuous;
    }

    public short getPrimaryBitMap() {
        return primaryBitMap;
    }

    public short getAdditionalDataBitMap() {
        return additionalDataBitMap;
    }

    public int getCompressedSize() {
        return compressedSize;
    }

    public byte[] getCompressedData() {
        return compressedData;
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
