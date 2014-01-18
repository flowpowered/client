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
package org.spoutcraft.client.network.protocol;

import org.spoutcraft.client.Game;
import org.spoutcraft.client.network.codec.login.LoginStartCodec;
import org.spoutcraft.client.network.codec.login.LoginSuccessCodec;
import org.spoutcraft.client.network.message.login.LoginStartMessage;
import org.spoutcraft.client.network.message.login.LoginSuccessMessage;

/**
 * The login protocol for the client protocol.
 */
public class LoginProtocol extends ClientProtocol {
    private static final int HIGHEST_OP_CODE = 2;

    /**
     * Constructs a new login protocol.
     */
    public LoginProtocol(Game game) {
        super(game, "login", HIGHEST_OP_CODE);
        /**
         * From Server, in order of opcodes
         */
        registerMessage(INBOUND, LoginSuccessMessage.class, LoginSuccessCodec.class, LoginSuccessCodec.class, 2);
        /**
         * To Server, in order of opcodes
         */
        registerMessage(OUTBOUND, LoginStartMessage.class, LoginStartCodec.class, null, 0);
    }
}
