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

import org.spoutcraft.client.game.Difficulty;
import org.spoutcraft.client.game.Dimension;
import org.spoutcraft.client.game.GameMode;
import org.spoutcraft.client.game.LevelType;
import org.spoutcraft.client.network.message.ChannelMessage;

/**
 * Client-bound {@link com.flowpowered.networking.Message} that instructs the client to respawn, updating the active
 * {@link org.spoutcraft.client.universe.World}'s characteristics.
 */
public class RespawnMessage extends ChannelMessage {
    private static final Channel REQUIRED_CHANNEL = Channel.UNIVERSE;
    private final Dimension dimension;
    private final Difficulty difficulty;
    private final GameMode gameMode;
    private final LevelType levelType;

    /**
     * Constructs a new respawn
     *
     * @param gameMode The {@link org.spoutcraft.client.game.GameMode} the active {@link org.spoutcraft.client.universe.World} should be
     * @param dimension The {@link org.spoutcraft.client.game.Dimension} the active {@link org.spoutcraft.client.universe.World} should be
     * @param difficulty The {@link org.spoutcraft.client.game.Difficulty} the active {@link org.spoutcraft.client.universe.World} should be
     * @param levelType The {@link org.spoutcraft.client.game.LevelType} the active {@link org.spoutcraft.client.universe.World} should be
     */
    public RespawnMessage(Dimension dimension, Difficulty difficulty, GameMode gameMode, LevelType levelType) {
        super(REQUIRED_CHANNEL);
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.gameMode = gameMode;
        this.levelType = levelType;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
