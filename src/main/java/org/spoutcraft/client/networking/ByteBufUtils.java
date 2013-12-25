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
    public static String readUTF8(ByteBuf buf) throws IOException {
        //Read the string's length
        int len = readVarInt(buf);

        byte[] bytes = new byte[len];
        buf.readBytes(bytes);

        return new String(bytes, Charsets.UTF_8);
    }

    public static void writeUTF8(ByteBuf buf, String value) throws IOException {
        final byte[] bytes = value.getBytes(Charsets.UTF_8);
        if (bytes.length >= Short.MAX_VALUE) {
            throw new IOException("Attempt to write a string with a length greater than Short.MAX_VALUE to ByteBuf!");
        }
        //Write the string's length
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    public static int readVarInt(ByteBuf buf) throws IOException {
        int out = 0;
        int bytes = 0;
        byte in;
        while (true) {
            in = buf.readByte();

            out |= (in & 0x7F) << (bytes * 7);

            if (bytes > 32) {
                throw new IOException("Attempt to read int bigger than allowed for a varint!");
            }

            if ((in & 0x80) != 0x80) {
                break;
            }
        }

        return out;
    }

    public static void writeVarInt(ByteBuf buf, int value) {
        int part;
        while (true) {
            part = value & 0x7F;

            value >>>= 7;
            if (value != 0) {
                part |= 0x80;
            }

            buf.writeByte(part);

            if (value == 0) {
                break;
            }
        }
    }
}
