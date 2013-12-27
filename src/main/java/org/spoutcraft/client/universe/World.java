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

import java.util.Collection;
import java.util.UUID;

import org.spout.math.vector.Vector3i;

import org.spoutcraft.client.game.Difficulty;
import org.spoutcraft.client.game.Dimension;
import org.spoutcraft.client.game.GameMode;
import org.spoutcraft.client.game.LevelType;
import org.spoutcraft.client.util.map.TripleIntObjectMap;
import org.spoutcraft.client.util.map.impl.TTripleInt21ObjectHashMap;

/**
 *
 */
public class World {
    private final TripleIntObjectMap<Chunk> chunks = new TTripleInt21ObjectHashMap<>();
    private final UUID id;
    private final String name;
    // Characteristics
    private Vector3i spawnPosition;
    private GameMode gameMode;
    private Dimension dimension;
    private Difficulty difficulty;
    private LevelType levelType;

    public World(String name) {
        this(UUID.randomUUID(), name);
    }

    public World(UUID id, String name) {
        this(id, name, GameMode.SURVIVAL, Dimension.NORMAL, Difficulty.NORMAL, LevelType.DEFAULT);
    }

    public World(String name, GameMode gameMode, Dimension dimension, Difficulty difficulty, LevelType levelType) {
        this(UUID.randomUUID(), name, gameMode, dimension, difficulty, levelType);
    }

    public World(UUID id, String name, GameMode gameMode, Dimension dimension, Difficulty difficulty, LevelType levelType) {
        this.id = id;
        this.name = name;
        this.gameMode = gameMode;
        this.difficulty = difficulty;
        this.dimension = dimension;
        this.levelType = levelType;
    }

    public UUID getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Vector3i getSpawnPosition() {
        return spawnPosition;
    }

    public void setSpawnPosition(Vector3i spawnPosition) {
        this.spawnPosition = spawnPosition;
    }

    public boolean hasChunk(Vector3i position) {
        return hasChunk(position.getX(), position.getY(), position.getZ());
    }

    public boolean hasChunk(int x, int y, int z) {
        return chunks.containsKey(x, y, z);
    }

    public Chunk getChunk(Vector3i position) {
        return getChunk(position.getX(), position.getY(), position.getZ());
    }

    public Chunk getChunk(int x, int y, int z) {
        return chunks.get(x, y, z);
    }

    public Chunk setChunk(Chunk chunk) {
        final Vector3i position = chunk.getPosition();
        return chunks.put(position.getX(), position.getY(), position.getZ(), chunk);
    }

    public Chunk removeChunk(Vector3i position) {
        return removeChunk(position.getX(), position.getY(), position.getZ());
    }

    public Chunk removeChunk(int x, int y, int z) {
        return chunks.remove(x, y, z);
    }

    public Collection<Chunk> getChunks() {
        return chunks.valueCollection();
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public void setLevelType(LevelType levelType) {
        this.levelType = levelType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof World)) {
            return false;
        }
        final World world = (World) o;
        return id.equals(world.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
