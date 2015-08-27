/*
 *
 * The MIT License (MIT)
 * 
 * Copyright (C) 2013 Man YUAN <epsilon@epsilony.net>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.epsilony.utils.codec.modbus.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import net.epsilony.utils.codec.modbus.Utils;
import net.epsilony.utils.codec.modbus.reqres.ModbusResponse;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ModbusSlaveResponseEncoderTest {

    @Test
    public void test() {
        ModbusSlaveResponseEncoder encoder = new ModbusSlaveResponseEncoder();
        EmbeddedChannel channel = new EmbeddedChannel(encoder);

        ModbusResponse response = new ModbusResponse() {
            {
                transectionId = 0xabcd;
                unitId = 0x83;
            }

            @Override
            public void writePduCore(ByteBuf out) {
                out.writeShort(0xabcd);
                out.writeShort(0xcdef);
                transectionId++;

            }

            @Override
            public int getPduCoreLength() {
                return 4;
            }

            @Override
            public int getFunctionCode() {
                // TODO Auto-generated method stub
                return 6;
            }
        };

        ByteBuf buf = null;
        for (int i = 0; i < 3; i++) {
            channel.writeOutbound(response);
            buf = (ByteBuf) channel.readOutbound();
            int[] buffer = new int[] { 0xab, 0xcd + i, 0x00, 0x00, 0x00, 0x06, 0x83, 0x06, 0xab, 0xcd, 0xcd, 0xef };
            assertEquals(buffer.length, buf.readableBytes());
            for (int b : buffer) {
                assertEquals(b, buf.readUnsignedByte());
            }
            assertTrue(!buf.isReadable());
            ReferenceCountUtil.release(buf);
        }

        for (int i = 0; i < 3; i++) {
            channel.writeOutbound(response);
        }

        for (int i = 0; i < 3; i++) {
            buf = (ByteBuf) channel.readOutbound();
            int[] buffer = new int[] { 0xab, 0xcd + i + 3, 0x00, 0x00, 0x00, 0x06, 0x83, 0x06, 0xab, 0xcd, 0xcd, 0xef };
            assertEquals(buffer.length, buf.readableBytes());
            for (int b : buffer) {
                assertEquals(b, buf.readUnsignedByte());
            }
            assertTrue(!buf.isReadable());
            ReferenceCountUtil.release(buf);
        }

        encoder.setWithCheckSum(true);
        for (int i = 0; i < 3; i++) {
            channel.writeOutbound(response);
        }
        for (int i = 0; i < 3; i++) {
            buf = (ByteBuf) channel.readOutbound();
            int[] buffer = new int[] { 0xab, 0xcd + i + 6, 0x00, 0x00, 0x00, 0x06, 0x83, 0x06, 0xab, 0xcd, 0xcd, 0xef };
            assertEquals(buffer.length, buf.readableBytes() - 2);
            for (int b : buffer) {
                assertEquals(b, buf.readUnsignedByte());
            }
            int calcCrc = Utils.crc(buf, buf.readerIndex() - buffer.length, buffer.length);
            assertEquals(calcCrc, buf.readUnsignedShort());
            assertTrue(!buf.isReadable());
            ReferenceCountUtil.release(buf);
        }
    }

}
