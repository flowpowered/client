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
package org.spoutcraft.client.network.codec.login;

import java.io.IOException;
import java.util.UUID;

import com.flowpowered.networking.Codec;
import com.flowpowered.networking.MessageHandler;
import com.flowpowered.networking.session.Session;

import io.netty.buffer.ByteBuf;

import org.spoutcraft.client.network.ByteBufUtils;
import org.spoutcraft.client.network.ClientSession;
import org.spoutcraft.client.network.message.ChannelMessage;
import org.spoutcraft.client.network.message.login.LoginSuccessMessage;

/**
 * The codec for the login success message. Also handles the login success message.
 */
public class LoginSuccessCodec extends Codec<LoginSuccessMessage> implements MessageHandler<LoginSuccessMessage> {
    private static final int OP_CODE = 2;

    /**
     * Constructs a new login success message codec and handler.
     */
    public LoginSuccessCodec() {
        super(LoginSuccessMessage.class, OP_CODE);
    }

    @Override
    public LoginSuccessMessage decode(ByteBuf buf) throws IOException {
        final UUID uuid = UUID.fromString(ByteBufUtils.readUTF8(buf));
        final String username = ByteBufUtils.readUTF8(buf);
        return new LoginSuccessMessage(uuid, username);
    }

    @Override
    public ByteBuf encode(ByteBuf buf, LoginSuccessMessage message) throws IOException {
        throw new IOException("The client should not send a login success to the Minecraft server!");
    }

    @Override
    public void handle(Session session, LoginSuccessMessage message) {
        ((ClientSession) session).getGame().getNetwork().offer(ChannelMessage.Channel.NETWORK, message);
    }
}
