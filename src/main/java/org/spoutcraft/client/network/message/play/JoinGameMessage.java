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
 * Client-bound message that instructs the client to setup the game with the following attributes.
 */
public class JoinGameMessage extends ChannelMessage {
    private static final Channel REQUIRED_CHANNEL = Channel.UNIVERSE;
    private final int playerId;
    private final GameMode gameMode;
    private final Dimension dimension;
    private final Difficulty difficulty;
    private final short maxPlayers;
    private final LevelType levelType;

    /**
     * Constructs a new join game
     *
     * @param playerId The entity id for the {@link org.spoutcraft.client.physics.entity.Player}
     * @param gameMode The {@link org.spoutcraft.client.game.GameMode} the {@link org.spoutcraft.client.universe.World} should be
     * @param dimension The {@link org.spoutcraft.client.game.Dimension} the {@link org.spoutcraft.client.universe.World} should be
     * @param difficulty The {@link org.spoutcraft.client.game.Difficulty} the {@link org.spoutcraft.client.universe.World} should be
     * @param maxPlayers The max players the server supports, used when rendering the player list
     * @param levelType The {@link org.spoutcraft.client.game.LevelType} the {@link org.spoutcraft.client.universe.World} should be
     */
    public JoinGameMessage(int playerId, GameMode gameMode, Dimension dimension, Difficulty difficulty, short maxPlayers, LevelType levelType) {
        super(REQUIRED_CHANNEL);
        this.playerId = playerId;
        this.gameMode = gameMode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.maxPlayers = maxPlayers;
        this.levelType = levelType;
    }

    public int getPlayerId() {
        return playerId;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public short getMaxPlayers() {
        return maxPlayers;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
