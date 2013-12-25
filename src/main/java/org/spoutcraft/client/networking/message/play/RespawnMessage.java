package org.spoutcraft.client.networking.message.play;

import com.flowpowered.networking.Message;
import org.spoutcraft.client.game.Difficulty;
import org.spoutcraft.client.game.Dimension;
import org.spoutcraft.client.game.GameMode;
import org.spoutcraft.client.game.LevelType;

/**
 * Client-bound {@link com.flowpowered.networking.Message} that instructs the client to respawn, updating the active
 * {@link org.spoutcraft.client.universe.World}'s characteristics.
 */
public class RespawnMessage implements Message {
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
