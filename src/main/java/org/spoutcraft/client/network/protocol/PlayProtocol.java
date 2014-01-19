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
package org.spoutcraft.client.network.protocol;

import org.spoutcraft.client.Game;
import org.spoutcraft.client.network.codec.play.ChunkDataBulkCodec;
import org.spoutcraft.client.network.codec.play.ChunkDataCodec;
import org.spoutcraft.client.network.codec.play.ClientStatusCodec;
import org.spoutcraft.client.network.codec.play.JoinGameCodec;
import org.spoutcraft.client.network.codec.play.KeepAliveCodec;
import org.spoutcraft.client.network.codec.play.PlayerCodec;
import org.spoutcraft.client.network.codec.play.PositionLookCodec;
import org.spoutcraft.client.network.codec.play.RespawnCodec;
import org.spoutcraft.client.network.codec.play.SpawnPositionCodec;
import org.spoutcraft.client.network.message.play.ChunkDataBulkMessage;
import org.spoutcraft.client.network.message.play.ChunkDataMessage;
import org.spoutcraft.client.network.message.play.ClientStatusMessage;
import org.spoutcraft.client.network.message.play.JoinGameMessage;
import org.spoutcraft.client.network.message.play.KeepAliveMessage;
import org.spoutcraft.client.network.message.play.PlayerMessage;
import org.spoutcraft.client.network.message.play.PositionLookMessage;
import org.spoutcraft.client.network.message.play.RespawnMessage;
import org.spoutcraft.client.network.message.play.SpawnPositionMessage;

/**
 * The main play protocol for the client protocol.
 */
public class PlayProtocol extends ClientProtocol {
    private static final int HIGHEST_OP_CODE = 26;

    /**
     * Constructs a new play protocol.
     */
    public PlayProtocol(Game game) {
        super(game, "play", HIGHEST_OP_CODE);
        /**
         * From Server, in order of opcodes
         */
        registerMessage(INBOUND, KeepAliveMessage.class, KeepAliveCodec.class, KeepAliveCodec.class, 0);
        registerMessage(INBOUND, JoinGameMessage.class, JoinGameCodec.class, JoinGameCodec.class, 1);
        registerMessage(INBOUND, SpawnPositionMessage.class, SpawnPositionCodec.class, SpawnPositionCodec.class, 5);
        registerMessage(INBOUND, RespawnMessage.class, RespawnCodec.class, RespawnCodec.class, 7);
        registerMessage(INBOUND, PositionLookMessage.class, PositionLookCodec.class, PositionLookCodec.class, 8);
        registerMessage(INBOUND, ChunkDataMessage.class, ChunkDataCodec.class, ChunkDataCodec.class, 21);
        registerMessage(INBOUND, ChunkDataBulkMessage.class, ChunkDataBulkCodec.class, ChunkDataBulkCodec.class, 26);
        /**
         * To Server, in order of opcodes
         */
        registerMessage(OUTBOUND, KeepAliveMessage.class, KeepAliveCodec.class, null, 0);
        registerMessage(OUTBOUND, PlayerMessage.class, PlayerCodec.class, null, 3);
        registerMessage(OUTBOUND, PositionLookMessage.class, PositionLookCodec.class, null, 4);
        registerMessage(OUTBOUND, ClientStatusMessage.class, ClientStatusCodec.class, null, 16);
    }
}
