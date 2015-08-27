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

import java.util.Random;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import net.epsilony.utils.codec.modbus.ModbusRegisterType;
import net.epsilony.utils.codec.modbus.func.MF;
import net.epsilony.utils.codec.modbus.func.ModbusFunction;
import net.epsilony.utils.codec.modbus.reqres.ModbusRequest;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ModbusSlaveRequestDecoderTest {

    static class TestData {
        int[] buffer;
        int transectionId;
        int unitId;
        ModbusFunction function;

        public TestData(int[] buffer, int transectionId, int unitId, ModbusFunction function) {
            this.buffer = buffer;
            this.transectionId = transectionId;
            this.unitId = unitId;
            this.function = function;
        }

        public void writeBuffer(ByteBuf buf) {
            for (int bufferByte : buffer) {
                buf.writeByte(bufferByte);
            }
        }

        public ModbusRequest createRequest() {
            ModbusRequest request = new ModbusRequest();
            request.setTransectionId(transectionId);
            request.setUnitId(unitId);
            request.setFunction(function);
            return request;
        }

    }

    public static ByteBuf createBuf() {
        ByteBuf buf = Unpooled.buffer();
        int randShift = new Random().nextInt(10) + 7;
        buf.writerIndex(randShift);
        buf.readerIndex(randShift);
        return buf;
    }

    @Test
    public void testDecode() {

        EmbeddedChannel channel = new EmbeddedChannel(new ModbusSlaveRequestDecoder());

        TestData[] datas = new TestData[] {
                new TestData(new int[] { 0xFA, 0xCE, 0x00, 0x00, 0x00, 0x06, 0x83, 0x01, 0xfe, 0xed, 0x03, 0xff },
                        0xFACE, 0x83, MF.readRegisters(ModbusRegisterType.COIL, 0xFEED, 0x03ff)),
                new TestData(new int[] { 0xFB, 0xCE, 0x00, 0x00, 0x00, 0x06, 0x83, 0x02, 0xfe, 0xed, 0x03, 0xff },
                        0xFBCE, 0x83, MF.readRegisters(ModbusRegisterType.INPUT_DISCRETE, 0xFEED, 0x03ff)),
                new TestData(new int[] { 0xFA, 0xCE, 0x00, 0x00, 0x00, 0x06, 0x83, 0x03, 0xfe, 0xed, 0x00, 0x7C },
                        0xFACE, 0x83, MF.readRegisters(ModbusRegisterType.HOLDING, 0xFEED, 0x007C)),
                new TestData(new int[] { 0xFA, 0xCE, 0x00, 0x00, 0x00, 0x06, 0x83, 0x04, 0xfe, 0xed, 0x00, 0x7C },
                        0xFACE, 0x83, MF.readRegisters(ModbusRegisterType.INPUT, 0xFEED, 0x007C)), };
        for (TestData data : datas) {
            ByteBuf buf = createBuf();
            data.writeBuffer(buf);
            channel.writeInbound(buf);
            Object decoded = channel.readInbound();

            assertTrue(buf.refCnt() <= 0);
            ModbusRequest exp = data.createRequest();
            assertEquals(exp, decoded);
        }

        assertEquals(null, channel.readInbound());

        ByteBuf totalBuf = createBuf();
        for (TestData data : datas) {
            data.writeBuffer(totalBuf);
        }

        channel.writeInbound(totalBuf);
        for (TestData data : datas) {
            Object decoded = channel.readInbound();

            ModbusRequest exp = data.createRequest();
            assertEquals(exp, decoded);
        }
        assertTrue(totalBuf.refCnt() <= 0);
        assertEquals(null, channel.readInbound());
    }

}
