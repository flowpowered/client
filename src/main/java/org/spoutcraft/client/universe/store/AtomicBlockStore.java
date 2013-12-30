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
package org.spoutcraft.client.universe.store;

import gnu.trove.set.hash.TIntHashSet;

import org.spout.math.vector.Vector3i;

/**
 * This store stores block data for each chunk. Each block can either store a short id, or a short id, a short data value and a reference to a &lt;T&gt; object.
 */
public interface AtomicBlockStore {
    /**
     * Gets the block id for a block at a particular location.<br> <br> Block ids range from 0 to 65535.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the block id
     */
    short getBlockId(int x, int y, int z);

    /**
     * Gets the block data for a block at a particular location.<br> <br> Block data ranges from 0 to 65535.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the block data
     */
    short getData(int x, int y, int z);

    /**
     * Gets the block data for a block at a particular location, applying the provided mask and shift to extract it from the raw data.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param mask the data mask
     * @return the block id
     */
    short getData(int x, int y, int z, DataMask mask);

    /**
     * Atomically gets the full set of data associated with the block.<br> <br>
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the full state of the block
     */
    int getFullData(int x, int y, int z);

    /**
     * Atomically gets the full set of data associated with the block.<br> <br>
     *
     * @param index the block index
     * @return the full state of the block
     */
    int getFullData(int index);

    /**
     * Marks the block id at (x, y, z) as dirty.<br>
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the full state of the block
     */
    int touchBlock(int x, int y, int z);

    /**
     * Sets the block id for the block at (x, y, z).
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param id the block id
     */
    void setBlockId(int x, int y, int z, short id);

    /**
     * Sets the block data for the block at (x, y, z).
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param data the block data
     */
    void setData(int x, int y, int z, short data);

    /**
     * Sets the block data for the block at (x, y, z). The data mask is used to only update the desired bits.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param data the block data
     * @param mask the data mask
     */
    void setData(int x, int y, int z, short data, DataMask mask);

    /**
     * Tests if all the entries in the block store are uniform.<br> <br> Note: this method may spuriously return false for uniform stores
     *
     * @return false if the store is not uniform
     */
    boolean isBlockUniform();

    /**
     * Sets the block id and data for the block at (x, y, z).<br> <br> If the data is 0, then the block will be stored as a single short.<br>
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param id the block id
     * @param data the block data
     */
    void setBlock(int x, int y, int z, short id, short data);

    /**
     * Sets the block id and data for the block at (x, y, z).<br> <br> If the data is 0, then the block will be stored as a single short.<br> The data mask will be used to update only the desired
     * bits.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param id the block id
     * @param data the block data
     * @param mask the mask for data
     */
    void setBlock(int x, int y, int z, short id, short data, DataMask mask);

    /**
     * Sets the block id, data for the block at (x, y, z).<br> <br> If the data is 0, then the block will be stored as a single short.<br>
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param id the block id
     * @param data the block data
     * @return the old full state of the block
     */
    int getAndSetBlock(int x, int y, int z, short id, short data);

    /**
     * Sets the block id, data for the block at (x, y, z).<br> <br> If the data is 0, then the block will be stored as a single short.<br> The data mask will be used to update only the desired bits.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param id the block id
     * @param data the block data
     * @param mask the mask for data
     * @return the old full state of the block
     */
    int getAndSetBlock(int x, int y, int z, short id, short data, DataMask mask);

    /**
     * Sets the block id, data for the block at (x, y, z), if the current data matches the expected data.<br>
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param expectId the expected block id
     * @param expectData the expected block data
     * @param newId the new block id
     * @param newData the new block data
     * @return true if the block was set
     */
    boolean compareAndSetBlock(int x, int y, int z, short expectId, short expectData, short newId, short newData);

    /**
     * Gets if the store would benefit from compression.<br> <br> If this method is called when the store is being accessed by another thread, it may give spurious results.
     *
     * @return true if compression would reduce the store size
     */
    boolean needsCompression();

    /**
     * Gets a short array containing the block ids in the store.<br> <br> If the store is updated while this snapshot is being taken, data tearing could occur.
     *
     * @return the array
     */
    short[] getBlockIdArray();

    /**
     * Copies the block ids in the store into an array.<br> <br> If the store is updated while this snapshot is being taken, data tearing could occur.<br> <br> If the array is the wrong length or
     * null, a new array is created.
     *
     * @param array to place the data
     * @return the array
     */
    short[] getBlockIdArray(short[] array);

    /**
     * Gets a short array containing the block data for the blocks in the store.<br> <br> If the store is updated while this snapshot is being taken, data tearing could occur.
     *
     * @return the array
     */
    short[] getDataArray();

    /**
     * Gets a short array containing the block data for the blocks in the store.<br> <br> If the store is updated while this snapshot is being taken, data tearing could occur. The data mask will be
     * used to obtain only the desired bits.
     *
     * @param mask the data mask
     * @return the array
     */
    short[] getDataArray(DataMask mask);

    /**
     * Copies the block data in the store into an array.<br> <br> If the store is updated while this snapshot is being taken, data tearing could occur.<br> <br> If the array is the wrong length or
     * null, a new array is created.
     *
     * @param array to place the data
     * @return the array
     */
    short[] getDataArray(short[] array);

    /**
     * Copies the block data in the store into an array.<br> <br> If the store is updated while this snapshot is being taken, data tearing could occur.<br> <br> If the array is the wrong length or
     * null, a new array is created. The data mask will be used to obtain only the desired bits.
     *
     * @param array to place the data
     * @return the array
     */
    short[] getDataArray(short[] array, DataMask mask);

    /**
     * Compresses the store.<br>
     */
    void compress();

    /**
     * Compresses the store.<br>
     *
     * @param inUseSet to use to store used ids
     */
    void compress(TIntHashSet inUseSet);

    /**
     * Gets if the dirty array has overflowed since the last reset.<br> <br>
     *
     * @return true if there was an overflow
     */
    boolean isDirtyOverflow();

    /**
     * Gets if the store has been modified since the last reset of the dirty arrays
     *
     * @return true if the store is dirty
     */
    boolean isDirty();

    /**
     * Resets the dirty arrays
     *
     * @return true if there were dirty blocks
     */
    boolean resetDirtyArrays();

    /**
     * Gets the number of dirty blocks since the last update
     */
    int getDirtyBlocks();

    /**
     * Gets the coordinate of the lowest dirty block
     */
    Vector3i getMinDirty();

    /**
     * Gets the coordinate of the maximum dirty block
     */
    Vector3i getMaxDirty();

    /**
     * Gets the position of the dirty block at a given index.<br> <br> If there is no block at that index, then the method return null.<br> <br> Note: the x, y and z values returned are the chunk
     * coordinates, not the world coordinates and the method has no effect on the world field of the block.<br>
     */
    Vector3i getDirtyBlock(int i);

    /**
     * Gets the old state for the dirty block at a given index.<br> <br> If there is no block at that index, then the method return null.<br>
     */
    int getDirtyOldState(int i);

    /**
     * Gets the new state for the dirty block at a given index.<br> <br> If there is no block at that index, then the method return null.<br>
     */
    int getDirtyNewState(int i);

    /**
     * Gets the width of each entry in the packed array
     */
    int getPackedWidth();

    /**
     * Gets the packed array
     */
    int[] getPackedArray();

    /**
     * Gets the palette for the packed array
     */
    int[] getPalette();

    /**
     * Write locks the store
     */
    void writeLock();

    /**
     * Releases the store write lock
     */
    void writeUnlock();

    /**
     * Attempts to write lock the store
     */
    boolean tryWriteLock();

    /**
     * Represents a mask used for packing multiple value inside the data short.
     */
    public static class DataMask {
        private final short mask;
        private final short shift;

        /**
         * Constructs a new data mask from the mask and the shift, which is the position of the data in the short.
         *
         * @param mask The data mask
         * @param shift The shift
         */
        public DataMask(short mask, short shift) {
            this.mask = mask;
            this.shift = shift;
        }

        /**
         * Returns the data mask.
         *
         * @return The mask
         */
        public short getMask() {
            return mask;
        }

        /**
         * Returns the shift.
         *
         * @return The shift
         */
        public short getShift() {
            return shift;
        }

        /**
         * Applies the mask to the value.
         *
         * @param value The value
         * @return The masked value
         */
        public short apply(short value) {
            return (short) ((value & mask) << shift);
        }

        /**
         * Extracts a value from another using the mask.
         *
         * @param value The value
         * @return The extracted value
         */
        public short extract(short value) {
            return (short) (value >> shift & mask);
        }
    }
}
