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
package org.spoutcraft.client.networking.message.login;

import org.spoutcraft.client.networking.message.ChannelMessage;

/**
 * Client bound message that instructs the client that login was successful to the server.
 * </p>
 * If the server is in online mode, this occurs after encryption. Otherwise, this occurs after {@link LoginStartMessage} is sent.
 */
public class LoginSuccessMessage extends ChannelMessage {
    private static final Channel[] REQUIRED_CHANNELS = new Channel[] {Channel.NETWORK};
    private final String uuid;
    private final String username;

    /**
     * Constructs a new login success
     *
     * @param uuid The unique identifier of this session
     * @param username The username of this session
     */
    public LoginSuccessMessage(String uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public String getUUID() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Channel[] getRequiredChannels() {
        return REQUIRED_CHANNELS;
    }
}
