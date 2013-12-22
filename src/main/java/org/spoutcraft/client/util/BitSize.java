package org.spoutcraft.client.util;

/**
 * Stores the size that spans a fixed amount of bits<br> For example: 1, 2, 4, 8, 16 and 32 sizes match this description
 */
public class BitSize {
    public final int SIZE;
    public final int HALF_SIZE;
    public final int DOUBLE_SIZE;
    public final int MASK;
    public final int BITS;
    public final int DOUBLE_BITS;
    public final int AREA;
    public final int HALF_AREA;
    public final int DOUBLE_AREA;
    public final int VOLUME;
    public final int HALF_VOLUME;
    public final int DOUBLE_VOLUME;

    public BitSize(int bitCount) {
        this.BITS = bitCount;
        this.SIZE = 1 << bitCount;
        this.AREA = this.SIZE * this.SIZE;
        this.VOLUME = this.AREA * this.SIZE;
        this.HALF_SIZE = this.SIZE >> 1;
        this.HALF_AREA = this.AREA >> 1;
        this.HALF_VOLUME = this.VOLUME >> 1;
        this.DOUBLE_SIZE = this.SIZE << 1;
        this.DOUBLE_AREA = this.AREA << 1;
        this.DOUBLE_VOLUME = this.VOLUME << 1;
        this.DOUBLE_BITS = this.BITS << 1;
        this.MASK = this.SIZE - 1;
    }
}
