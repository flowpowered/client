package org.spoutcraft.client.network.message.play;

import org.spoutcraft.client.network.message.ChannelMessage;

public class ChunkDataMessage extends ChannelMessage {
    private final int x;
    private final int z;
    private final boolean groundUpContinuous;
    private final short primaryBitMap;
    private final short addBitMap;
    private final int compressedSize;
    private final byte[] compressedData;

    public ChunkDataMessage(int x, int z, boolean groundUpContinuous, short primaryBitMap, short addBitMap, int compressedSize, byte[] compressedData) {
        this.x = x;
        this.z = z;
        this.groundUpContinuous = groundUpContinuous;
        this.primaryBitMap = primaryBitMap;
        this.addBitMap = addBitMap;
        this.compressedSize = compressedSize;
        this.compressedData = compressedData;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean isGroundUpContinuous() {
        return groundUpContinuous;
    }

    public short getPrimaryBitMap() {
        return primaryBitMap;
    }

    public short getAddBitMap() {
        return addBitMap;
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
