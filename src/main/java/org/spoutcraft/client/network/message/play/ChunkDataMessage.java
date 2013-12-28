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
