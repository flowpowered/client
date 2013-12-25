package org.spoutcraft.client.networking.codec.play;

import java.io.IOException;

import com.flowpowered.networking.Codec;
import com.flowpowered.networking.MessageHandler;
import com.flowpowered.networking.session.Session;
import io.netty.buffer.ByteBuf;
import org.spoutcraft.client.game.Difficulty;
import org.spoutcraft.client.game.Dimension;
import org.spoutcraft.client.game.GameMode;
import org.spoutcraft.client.game.LevelType;
import org.spoutcraft.client.networking.ByteBufUtils;
import org.spoutcraft.client.networking.ClientSession;
import org.spoutcraft.client.networking.message.play.RespawnMessage;

public class RespawnCodec extends Codec<RespawnMessage> implements MessageHandler<RespawnMessage> {
    public static final int OP_CODE = 7;

    public RespawnCodec() {
        super(RespawnMessage.class, OP_CODE);
    }

    @Override
    public RespawnMessage decode(ByteBuf buf) throws IOException {
        final Dimension dimension = Dimension.get(buf.readInt());
        final Difficulty difficulty = Difficulty.get(buf.readUnsignedByte());
        final GameMode gameMode = GameMode.get(buf.readUnsignedByte());
        final LevelType levelType = LevelType.get(ByteBufUtils.readUTF8(buf));
        return new RespawnMessage(dimension, difficulty, gameMode, levelType);
    }

    @Override
    public ByteBuf encode(ByteBuf buf, RespawnMessage message) throws IOException {
        throw new IOException("The client should not send a respawn to the Minecraft server!");
    }

    @Override
    public void handle(Session session, RespawnMessage message) {
        ((ClientSession) session).getGame().getUniverse().updateWorld(message);
    }
}
