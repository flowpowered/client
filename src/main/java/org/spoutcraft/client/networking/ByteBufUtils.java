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
package org.spoutcraft.client.networking;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import org.apache.commons.io.Charsets;

public class ByteBufUtils {
    public static void writeUTF8(ByteBuf buf, String str) throws IOException {
        int len = str.length();
        if (len >= 65536) {
            throw new IOException("Attempt to write a String with a length greater than Integer.MAX_VALUE to ByteBuf!");
        }
        //Write the String's length
        buf.writeByte(len);

        //Write the String
        for (int i = 0; i < len; i++) {
            buf.writeChar(str.charAt(i));
        }
    }

    public static String readUTF8(ByteBuf buf) {
        //Read size of String
        int len = buf.readInt(); //TODO Test this
        byte[] data = new byte[len];

        //Read bytes of String from length
        buf.readBytes(data, 0, len);
        return new String(data, Charsets.UTF_8);
    }
}
