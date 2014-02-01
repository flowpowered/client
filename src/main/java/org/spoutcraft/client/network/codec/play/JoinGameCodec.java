/**
 * This file is part of Client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013-2014 Spoutcraft <http://spoutcraft.org/>
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
package org.spoutcraft.client.network.codec.play;

import java.io.IOException;

import com.flowpowered.networking.Codec;
import com.flowpowered.networking.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import org.spoutcraft.client.game.Difficulty;
import org.spoutcraft.client.game.Dimension;
import org.spoutcraft.client.game.GameMode;
import org.spoutcraft.client.game.LevelType;
import org.spoutcraft.client.network.message.play.JoinGameMessage;

/**
 * The codec for the join game message.
 */
public class JoinGameCodec implements Codec<JoinGameMessage> {
    @Override
    public JoinGameMessage decode(ByteBuf buf) throws IOException {
        final int playerID = buf.readInt();
        final short modePacked = buf.readUnsignedByte();
        final boolean hardcore = (modePacked & 8) == 8;
        final GameMode gameMode = GameMode.get(modePacked & -9);
        final Dimension dimension = Dimension.get(buf.readByte());
        final Difficulty difficulty = Difficulty.get(buf.readUnsignedByte());
        final short maxPlayers = buf.readUnsignedByte();
        final LevelType levelType = LevelType.get(ByteBufUtils.readUTF8(buf));
        return new JoinGameMessage(playerID, hardcore, gameMode, dimension, difficulty, maxPlayers, levelType);
    }

    @Override
    public ByteBuf encode(ByteBuf buf, JoinGameMessage message) throws IOException {
        throw new IOException("The client cannot send a join game to the Minecraft server!");
    }
}