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
package org.spoutcraft.client.networking.codec;

import java.io.IOException;
import java.util.UUID;

import com.flowpowered.networking.Codec;
import com.flowpowered.networking.MessageHandler;
import com.flowpowered.networking.session.PulsingSession;
import com.flowpowered.networking.session.Session;
import io.netty.buffer.ByteBuf;
import org.spoutcraft.client.networking.ByteBufUtils;
import org.spoutcraft.client.networking.ClientSession;
import org.spoutcraft.client.networking.message.LoginSuccessMessage;
import org.spoutcraft.client.networking.protocol.PlayProtocol;

public class LoginSuccessCodec extends Codec<LoginSuccessMessage> implements MessageHandler<LoginSuccessMessage> {
    public static final int OP_CODE = 2;

    public LoginSuccessCodec() {
        super(LoginSuccessMessage.class, OP_CODE);
    }

    @Override
    public LoginSuccessMessage decode(ByteBuf buf) throws IOException {
        final String uuid = ByteBufUtils.readUTF8(buf);
        final String username = ByteBufUtils.readUTF8(buf);
        return new LoginSuccessMessage(uuid, username);
    }

    @Override
    public ByteBuf encode(ByteBuf buf, LoginSuccessMessage message) throws IOException {
        throw new IOException("The client should not send a login success to the Minecraft server!");
    }

    @Override
    public void handle(Session session, LoginSuccessMessage message) {
        System.out.println("Server says login is successful...Woo!!");

        final ClientSession clientSession = (ClientSession) session;
        clientSession.setProtocol(new PlayProtocol());
        clientSession.setUUID(UUID.fromString(message.getUUID()));
        clientSession.setUsername(message.getUsername());
        clientSession.setState(PulsingSession.State.OPEN);
    }
}
