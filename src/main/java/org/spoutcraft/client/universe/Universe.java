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
package org.spoutcraft.client.universe;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.flowpowered.commons.ticking.TickingElement;
import com.flowpowered.math.vector.Vector3i;
import org.spoutcraft.client.Game;
import org.spoutcraft.client.game.Difficulty;
import org.spoutcraft.client.game.Dimension;
import org.spoutcraft.client.game.GameMode;
import org.spoutcraft.client.game.LevelType;
import org.spoutcraft.client.network.Network;
import org.spoutcraft.client.network.message.ChannelMessage;
import org.spoutcraft.client.network.message.ChannelMessage.Channel;
import org.spoutcraft.client.network.message.play.ChunkDataBulkMessage;
import org.spoutcraft.client.network.message.play.ChunkDataMessage;
import org.spoutcraft.client.network.message.play.JoinGameMessage;
import org.spoutcraft.client.network.message.play.PlayerMessage;
import org.spoutcraft.client.network.message.play.PositionLookMessage;
import org.spoutcraft.client.network.message.play.RespawnMessage;
import org.spoutcraft.client.network.message.play.SpawnPositionMessage;
import org.spoutcraft.client.universe.block.material.Materials;
import org.spoutcraft.client.universe.snapshot.WorldSnapshot;
import org.spoutcraft.client.universe.world.Chunk;
import org.spoutcraft.client.universe.world.World;
import org.spoutcraft.client.util.AnnotatedMessageHandler;
import org.spoutcraft.client.util.AnnotatedMessageHandler.Handle;

/**
 * Contains and manages all the voxel worlds.
 */
public class Universe extends TickingElement {
    private static final int TPS = 20;
    // Chunk data handling
    private static final int MAX_CHUNK_COLUMN_SECTIONS = 16;
    private static final byte[] UNLOAD_CHUNKS_IN_COLUMN = {0x78, (byte) 0x9C, 0x63, 0x64, 0x1C, (byte) 0xD9, 0x00, 0x00, (byte) 0x81, (byte) 0x80, 0x01, 0x01};
    private static final Inflater INFLATER = new Inflater();
    private final Game game;
    private final Map<UUID, World> worlds = new ConcurrentHashMap<>();
    private final Map<UUID, WorldSnapshot> worldSnapshots = new ConcurrentHashMap<>();
    private final Map<String, UUID> worldIDsByName = new ConcurrentHashMap<>();
    private final AtomicReference<World> activeWorld = new AtomicReference<>(null);
    private final AnnotatedMessageHandler messageHandler;

    public Universe(Game game) {
        super("universe", TPS);
        this.game = game;
        messageHandler = new AnnotatedMessageHandler(this);
    }

    @Override
    public void onStart() {
        System.out.println("Universe start");

        // TEST CODE
        final short[] chunkIDs = new short[Chunk.BLOCKS.VOLUME];
        Arrays.fill(chunkIDs, Materials.SOLID.getID());
        final short[] chunkSubIDs = new short[Chunk.BLOCKS.VOLUME];
        final World world = new World("test");
        for (int xx = -2; xx < 2; xx++) {
            for (int zz = -2; zz < 2; zz++) {
                world.setChunk(new Chunk(world, new Vector3i(xx, 0, zz), chunkIDs, chunkSubIDs));
            }
        }
        addWorld(world, true);
    }

    @Override
    public void onTick(long dt) {
        // TODO: Optimization needed here, process so many per tick?
        final Network network = game.getNetwork();

        final Iterator<ChannelMessage> messages = network.getChannel(Channel.UNIVERSE);
        while (messages.hasNext()) {
            final ChannelMessage message = messages.next();
            handleMessage(message);
        }

        if (network.getSession() != null) {
            network.getSession().send(new PlayerMessage(true));
        }

        // TEST CODE
        final Random random = new Random();
        final World world = activeWorld.get();
        int x = random.nextInt(64) - 32;
        int z = random.nextInt(64) - 32;
        final Chunk chunk = world.getChunk(x >> Chunk.BLOCKS.BITS, 0, z >> Chunk.BLOCKS.BITS);
        if (chunk != null) {
            for (int y = 15; y >= 0; y--) {
                if (chunk.getMaterial(x, y, z) != Materials.AIR) {
                    chunk.setMaterial(x, y, z, Materials.AIR);
                    break;
                }
            }
        }

        updateWorldTimes(dt);
        updateSnapshots();
    }

    @Override
    public void onStop() {
        System.out.println("Universe stop");

        worlds.clear();
        updateSnapshots();
    }

    public Game getGame() {
        return game;
    }

    public WorldSnapshot getWorldSnapshot(UUID id) {
        return worldSnapshots.get(id);
    }

    public WorldSnapshot getWorldSnapshot(String name) {
        return worldSnapshots.get(worldIDsByName.get(name));
    }

    public WorldSnapshot getActiveWorldSnapshot() {
        return activeWorld.get() != null ? worldSnapshots.get(activeWorld.get().getID()) : null;
    }

    private World getWorld(String name) {
        return worlds.get(worldIDsByName.get(name));
    }

    private void addWorld(World world, boolean setActive) {
        worlds.put(world.getID(), world);
        worldIDsByName.put(world.getName(), world.getID());
        if (setActive) {
            activeWorld.set(world);
        }
    }

    private void updateWorldTimes(long dt) {
        for (World world : worlds.values()) {
            world.updateTime(dt);
        }
    }

    private void updateSnapshots() {
        for (Iterator<WorldSnapshot> iterator = worldSnapshots.values().iterator(); iterator.hasNext(); ) {
            if (!worlds.containsKey(iterator.next().getID())) {
                iterator.remove();
            }
        }
        for (Entry<UUID, World> entry : worlds.entrySet()) {
            final UUID id = entry.getKey();
            final World world = entry.getValue();
            WorldSnapshot worldSnapshot = worldSnapshots.get(id);
            if (worldSnapshot == null) {
                worldSnapshot = new WorldSnapshot(world);
                worldSnapshots.put(id, worldSnapshot);
            }
            worldSnapshot.update(world);
        }
    }

    /**
     * Creates a {@link org.spoutcraft.client.universe.world.World} from a variety of characteristics.
     *
     * @param gameMode See {@link org.spoutcraft.client.game.GameMode}
     * @param dimension See {@link org.spoutcraft.client.game.Dimension}
     * @param difficulty See {@link org.spoutcraft.client.game.Difficulty}
     * @param levelType See {@link org.spoutcraft.client.game.LevelType}
     * @param isActive True if the created {@link org.spoutcraft.client.universe.world.World} should be made active (receives {@link org.spoutcraft.client.universe.world.Chunk}s)
     * @return The constructed {@link org.spoutcraft.client.universe.world.World}
     */
    private World createWorld(GameMode gameMode, Dimension dimension, Difficulty difficulty, LevelType levelType, boolean isActive) {
        final World world = new World("world-" + dimension.name(), gameMode, dimension, difficulty, levelType);
        addWorld(world, isActive);
        return world;
    }

    /**
     * Processes the next {@link org.spoutcraft.client.network.message.ChannelMessage} in the network pipeline.
     *
     * @param message See {@link org.spoutcraft.client.network.message.ChannelMessage}
     */
    private void handleMessage(ChannelMessage message) {
        messageHandler.handle(message);
        message.markChannelRead(Channel.UNIVERSE);
    }

    // TODO: move the message handle methods to another class?

    /**
     * Handles a {@link org.spoutcraft.client.network.message.play.JoinGameMessage}.
     *
     * @param message See {@link org.spoutcraft.client.network.message.play.JoinGameMessage}
     */
    @Handle
    private void handleJoinGame(JoinGameMessage message) {
        System.out.println("Server says join is successful...Woo!!");
        createWorld(message.getGameMode(), message.getDimension(), message.getDifficulty(), message.getLevelType(), true);
    }

    @Handle
    private void handleSpawnPosition(SpawnPositionMessage message) {
        if (getGame().getNetwork().isRunning()) {
            //TODO Test code
            getGame().getNetwork().getSession().send(new PositionLookMessage(message.getX(), message.getY(), message.getZ(), 0f, 0f, true, message.getY() + 1));
        }
    }

    /**
     * Handles a {@link org.spoutcraft.client.network.message.play.RespawnMessage}.
     *
     * @param message See {@link org.spoutcraft.client.network.message.play.RespawnMessage}
     */
    @Handle
    private void handleRespawn(RespawnMessage message) {
        final World world;
        if (message.getDimension() == activeWorld.get().getDimension()) {
            world = activeWorld.get();
        } else {
            world = getWorld("world-" + message.getDimension().name());
        }
        if (world != null) {
            world.setGameMode(message.getGameMode());
            world.setDimension(message.getDimension());
            world.setDifficulty(message.getDifficulty());
            world.setLevelType(message.getLevelType());
        }
    }

    /**
     * Handles a {@link org.spoutcraft.client.network.message.play.ChunkDataMessage}.
     *
     * @param message See {@link org.spoutcraft.client.network.message.play.ChunkDataMessage}
     */
    @Handle
    private void handleChunkData(ChunkDataMessage message) {
        // Check if we should remove a column of chunks
        if (Arrays.equals(UNLOAD_CHUNKS_IN_COLUMN, message.getCompressedData())) {
            activeWorld.get().removeChunkColumn(message.getColumnX(), message.getColumnZ(), 0, MAX_CHUNK_COLUMN_SECTIONS);
        } else {
            final byte[][][] data = new byte[MAX_CHUNK_COLUMN_SECTIONS][][];
            try {

                // Find out how many non-air chunks we have in the column and add on a data segment for it
                int columnDataSize = 0;
                for (int i = 0; i < MAX_CHUNK_COLUMN_SECTIONS; i++) {
                    boolean hasData = ((message.getPrimaryBitMap() & 1 << i) != 0);
                    if (hasData) {
                        data[i] = new byte[ChunkDataIndex.values().length][];
                        //Blocks + (Metadata + Light + Skylight)
                        columnDataSize += Chunk.BLOCKS.VOLUME + (Chunk.BLOCKS.HALF_VOLUME * 3);
                    }
                }
                // If ground up continuous, biome data is sent per column. We need to add on to the data size for this.
                if (message.isGroundUpContinuous()) {
                    columnDataSize += Chunk.BLOCKS.AREA;
                }

                decompressChunkData(data, message.isGroundUpContinuous(), message.getCompressedData(), columnDataSize, true);
            } catch (IOException e) {
                System.out.println(e);
            }
            populateChunks(message.getColumnX(), message.getColumnZ(), data);
        }
    }

    /**
     * Handles a {@link org.spoutcraft.client.network.message.play.ChunkDataBulkMessage}.
     *
     * @param message See {@link org.spoutcraft.client.network.message.play.ChunkDataBulkMessage}
     */
    @Handle
    private void handleChunkDataBulk(ChunkDataBulkMessage message) {
        int index = 0;
        for (int i = 0; i < message.getColumnCount(); i++) {
            byte[][][] data = new byte[MAX_CHUNK_COLUMN_SECTIONS][][];
            final short primaryBitMap = message.getPrimaryBitMaps()[i];

            // Find out how many non-air chunks we have in the column and add on a data segment for it
            int columnDataSize = 0;
            for (int j = 0; j < MAX_CHUNK_COLUMN_SECTIONS; j++) {
                boolean hasData = ((primaryBitMap & 1 << j) != 0);
                if (hasData) {
                    data[j] = new byte[ChunkDataIndex.values().length][];
                    //Blocks + (Metadata + Light + Skylight)
                    columnDataSize += Chunk.BLOCKS.VOLUME + (Chunk.BLOCKS.HALF_VOLUME * (message.hasSkyLightData() ? 3 : 2));
                }
            }
            //ChunkDataBulk always sends biome data
            columnDataSize += Chunk.BLOCKS.AREA;

            byte[] compressedDataSection = new byte[columnDataSize];
            System.arraycopy(message.getCompressedData(), index, compressedDataSection, 0, columnDataSize);
            index += columnDataSize;

            if (Arrays.equals(UNLOAD_CHUNKS_IN_COLUMN, compressedDataSection)) {
                activeWorld.get().removeChunkColumn(message.getColumnXs()[i], message.getColumnZs()[i], 0, MAX_CHUNK_COLUMN_SECTIONS);
            } else {
                try {
                    decompressChunkData(data, true, compressedDataSection, columnDataSize, message.hasSkyLightData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                populateChunks(message.getColumnXs()[i], message.getColumnZs()[i], data);
            }
        }
    }

    /**
     * Decompresses the raw compressed data from the server into the provided 3D array that comprises:
     * <p/>
     * Section        - The section of the column, ground up ChunkDataIndex - See {@link org.spoutcraft.client.universe.Universe.ChunkDataIndex} data           - decompressed data as a byte
     *
     * @param groundUpContinuous True if this is the entire column, false if not. Used to determine if biome data is included
     * @param compressedData Compressed data from the server
     * @param hasSkyLight True if the compressed data has sky light, only {@link org.spoutcraft.client.network.message.play.ChunkDataBulkMessage}s can not provide this
     * @throws IOException If the chunk's data is corrupted during inflate or if all bytes are not decompressed
     */
    private void decompressChunkData(byte[][][] data, boolean groundUpContinuous, byte[] compressedData, int columnDataSize, boolean hasSkyLight) throws IOException {
        // Step 1 - Decompress the data
        final byte[] decompressedData = new byte[columnDataSize];
        INFLATER.setInput(compressedData);
        INFLATER.getRemaining();
        int decompressIndex = 0;

        try {
            while (decompressIndex < decompressedData.length) {
                int decompressedAmount = INFLATER.inflate(decompressedData, decompressIndex, decompressedData.length - decompressIndex);
                decompressIndex += decompressedAmount;
                if (decompressedAmount == 0) {
                    break;
                }
            }
            if (!INFLATER.needsInput()) {
                throw new IOException("Chunk data should be entirely decompressed but some bytes are still compressed!");
            }
        } catch (DataFormatException e) {
            throw new IOException("Chunk data is corrupted!", e);
        } finally {
            INFLATER.end();
        }

        // Step 2 - Build data array
        for (final byte[][] section : data) {
            if (section == null) {
                continue;
            }

            int index = 0;

            // Step 2a. - Fill Block ids
            section[ChunkDataIndex.BLOCK_ID.value()] = new byte[Chunk.BLOCKS.VOLUME];
            System.arraycopy(decompressedData, 0, section[ChunkDataIndex.BLOCK_ID.value()], 0, Chunk.BLOCKS.VOLUME);

            index += Chunk.BLOCKS.VOLUME;

            // Step 2b. - Fill Block metadata
            section[ChunkDataIndex.BLOCK_METADATA.value()] = new byte[Chunk.BLOCKS.VOLUME];
            fillHalfByteDataArray(section, ChunkDataIndex.BLOCK_METADATA, decompressedData, index, Chunk.BLOCKS.HALF_VOLUME);

            index += Chunk.BLOCKS.HALF_VOLUME;

            // Step 2c. - Fill Block light
            section[ChunkDataIndex.BLOCK_LIGHT.value()] = new byte[Chunk.BLOCKS.VOLUME];
            fillHalfByteDataArray(section, ChunkDataIndex.BLOCK_LIGHT, decompressedData, index, Chunk.BLOCKS.HALF_VOLUME);

            index += Chunk.BLOCKS.HALF_VOLUME;

            // Step 2d. - Fill Block additional data
            //TODO Official Minecraft doesn't use this as it simply lets mods go past 256 block ids, should we support it?
            section[ChunkDataIndex.BLOCK_ADDITIONAL_DATA.value()] = new byte[Chunk.BLOCKS.VOLUME];
            Arrays.fill(section[ChunkDataIndex.BLOCK_ADDITIONAL_DATA.value()], (byte) 0);

            index += Chunk.BLOCKS.HALF_VOLUME;

            // Step 2e. - Fill Block sky light
            section[ChunkDataIndex.BLOCK_SKY_LIGHT.value()] = new byte[Chunk.BLOCKS.VOLUME];

            if (!hasSkyLight) {
                Arrays.fill(section[ChunkDataIndex.BLOCK_SKY_LIGHT.value()], (byte) 0);
            } else {
                fillHalfByteDataArray(section, ChunkDataIndex.BLOCK_SKY_LIGHT, decompressedData, index, Chunk.BLOCKS.HALF_VOLUME);
            }

            //TODO Handle Biomes later as they are unique
        }
    }

    /**
     * Takes a byte array and splits each byte into two values.
     * <p/>
     * This is used in ChunkData to pull out the data provided for metadata, light, additional data, and skylight.
     *
     * @param sectionData The byte 2D array of the section of the column
     * @param index See {@link org.spoutcraft.client.universe.Universe.ChunkDataIndex}
     * @param decompressedData The byte array of decompressed data
     * @param startIndex Where to start reading data in the decompressed data array
     * @param length How much to read
     */
    private void fillHalfByteDataArray(byte[][] sectionData, ChunkDataIndex index, byte[] decompressedData, int startIndex, int length) {
        int position = 0;
        for (int i = startIndex; i < startIndex + length; i++) {
            final byte left = (byte) ((decompressedData[i] >> 4) & 0xF);
            final byte right = (byte) (decompressedData[i] & 0xF);

            sectionData[index.value()][position++] = left;
            sectionData[index.value()][position++] = right;
        }
    }

    /**
     * Populates all non-air {@link Chunk}s in the column.
     * <p/>
     * Any chunks that exist in the {@link org.spoutcraft.client.universe.world.World} will be replaced.
     *
     * @param columnX The column's x coordinate
     * @param columnZ The column's z coordinate
     * @param data The byte 3D array containing section, {@link org.spoutcraft.client.universe.Universe.ChunkDataIndex}, and byte (data)
     */
    private void populateChunks(int columnX, int columnZ, byte[][][] data) {
        final int baseX = columnX >> Chunk.BLOCKS.BITS;
        final int baseZ = columnZ >> Chunk.BLOCKS.BITS;

        for (int i = 0; i < MAX_CHUNK_COLUMN_SECTIONS; i++) {
            if (data[i] == null) {
                continue;
            }
            int baseY = i << Chunk.BLOCKS.BITS;
            for (int xx = 0; xx < Chunk.BLOCKS.SIZE; xx++) {
                for (int yy = 0; yy < Chunk.BLOCKS.SIZE; yy++) {
                    for (int zz = 0; zz < Chunk.BLOCKS.SIZE; zz++) {
                        int x = xx + baseX;
                        int y = yy + baseY;
                        int z = zz + baseZ;

                        //TODO Test Code, restore the commented out code once we have all materials in place!
                        //final short[] blockIds = toShort(data[i][ChunkDataIndex.BLOCK_ID.value()]);
                        final byte[] rawIds = data[i][ChunkDataIndex.BLOCK_ID.value()];
                        final short[] blockIds = new short[rawIds.length];
                        for (int idIndex = 0; idIndex < rawIds.length; idIndex++) {
                            if (rawIds[idIndex] != (byte) 0) {
                                blockIds[idIndex] = Materials.SOLID.getID();
                            } else {
                                blockIds[idIndex] = Materials.AIR.getID();
                            }
                        }
                        final short[] blockData = new short[Chunk.BLOCKS.VOLUME];
                        for (int d = 0; i < blockData.length; i++) {
                            // final block data order: 00MM-BB-SS (M = metadata, B = block light, S = sky light)
                            blockData[i] = (short) (((data[i][ChunkDataIndex.BLOCK_METADATA.value()][d] & 0xF) << 8)
                                    | ((data[i][ChunkDataIndex.BLOCK_LIGHT.value()][d] & 0xF) << 4)
                                    | ((data[i][ChunkDataIndex.BLOCK_SKY_LIGHT.value()][d] & 0xF)));
                        }
                        activeWorld.get().setChunk(new Chunk(activeWorld.get(), new Vector3i(x, y, z), blockIds, blockData));
                    }
                }
            }
        }
    }

    private static short[] toShort(byte[] a) {
        final short[] b = new short[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i];
        }
        return b;
    }

    private static enum ChunkDataIndex {
        /**
         * First index is {@link org.spoutcraft.client.universe.block.material.Material} type, whole byte
         */
        BLOCK_ID(0),
        /**
         * Second index is {@link org.spoutcraft.client.universe.block.Block} metadata, half byte
         */
        BLOCK_METADATA(1),
        /**
         * Third index is {@link org.spoutcraft.client.universe.block.Block} light values, half byte
         */
        BLOCK_LIGHT(2),
        /**
         * Fourth index is {@link org.spoutcraft.client.universe.block.Block} additional data, half byte and should always be 0
         */
        BLOCK_ADDITIONAL_DATA(3),
        /**
         * Fifth index is {@link org.spoutcraft.client.universe.block.Block} sky light, half byte
         */
        BLOCK_SKY_LIGHT(4);
        private final int value;

        ChunkDataIndex(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }
}
