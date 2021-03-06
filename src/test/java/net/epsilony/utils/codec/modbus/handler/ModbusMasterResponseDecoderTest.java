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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import net.epsilony.utils.codec.modbus.ModbusRegisterType;
import net.epsilony.utils.codec.modbus.Utils;
import net.epsilony.utils.codec.modbus.func.MF;
import net.epsilony.utils.codec.modbus.reqres.ExceptionResponse;
import net.epsilony.utils.codec.modbus.reqres.MissMatchResponse;
import net.epsilony.utils.codec.modbus.reqres.ModbusRequest;
import net.epsilony.utils.codec.modbus.reqres.ModbusResponse;
import net.epsilony.utils.codec.modbus.reqres.ReadBooleanRegistersResponse;
import net.epsilony.utils.codec.modbus.reqres.ReadWordRegistersResponse;
import net.epsilony.utils.codec.modbus.reqres.WriteCoilResponse;
import net.epsilony.utils.codec.modbus.reqres.WriteHoldingResponse;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ModbusMasterResponseDecoderTest {

    Map<Integer, ModbusRequest> requests = new LinkedHashMap<>();

    class TestData {
        ModbusRequest request;
        ModbusResponse response;
        int[] buffer;

        public TestData(ModbusRequest request, ModbusResponse response, int[] buffer) {
            this.request = request;
            this.response = response;
            this.buffer = buffer;
            requests.put(request.getTransectionId(), request);
        }

        public TestData(ModbusRequest request, ModbusResponse response, int[] buffer, boolean addToRecord) {
            this.request = request;
            this.response = response;
            this.buffer = buffer;
            if (addToRecord) {
                requests.put(request.getTransectionId(), request);
            }
        }

        public void writeBuffer(ByteBuf buf) {
            for (int bufferByte : buffer) {
                buf.writeByte(bufferByte);
            }
        }

        public void writeBufferWithCrc(ByteBuf buf) {
            int from = buf.writerIndex();
            writeBuffer(buf);
            buf.writeShort(Utils.crc(buf, from, buf.writerIndex() - from));
        }

    }

    public static ByteBuf createBuf() {
        ByteBuf buf = Unpooled.buffer(1024);
        int randShift = new Random().nextInt(10) + 7;
        buf.writerIndex(randShift);
        buf.readerIndex(randShift);
        return buf;
    }

    @Test
    public void testDecoding() {
        EmbeddedChannel channel = new EmbeddedChannel(new ModbusMasterResponseDecoder(requests::remove));
        TestData[] datas = initTestData();

        ByteBuf buf = createBuf();

        for (TestData data : datas) {
            data.writeBuffer(buf);
        }

        channel.writeInbound(buf);

        for (TestData data : datas) {
            Object decoded = channel.readInbound();
            assertEquals(data.response, decoded);
        }
        assertEquals(null, channel.readInbound());
        assertTrue(buf.refCnt() <= 0);
        assertTrue(requests.isEmpty());
    }

    @Test
    public void testDecodingWithCheckSome() {
        ModbusMasterResponseDecoder decoder = new ModbusMasterResponseDecoder(requests::remove);
        decoder.setWithCheckSum(true);
        EmbeddedChannel channel = new EmbeddedChannel(decoder);
        TestData[] datas = initTestData();

        ByteBuf buf = createBuf();

        for (TestData data : datas) {
            data.writeBufferWithCrc(buf);
        }

        channel.writeInbound(buf);

        for (TestData data : datas) {
            Object decoded = channel.readInbound();
            assertEquals(data.response, decoded);
        }
        assertEquals(null, channel.readInbound());
        assertTrue(buf.refCnt() <= 0);
        assertTrue(requests.isEmpty());
    }

    private TestData[] initTestData() {
        return new TestData[] {
                new TestData(new ModbusRequest(0xAC01, 0xFB, MF.writeRegister(0xFEDC, 0xDAFE)),
                        new WriteHoldingResponse(0xAC01, 0xFB, 0xFEDC, 0xDAFE),
                        new int[] { 0xAC, 0x01, 0x00, 0x00, 0x00, 0x06, 0xFB, 0x06, 0xFE, 0xDC, 0xDA, 0xFE }),
                new TestData(new ModbusRequest(0xAC02, 0xFB, MF.writeRegister(0xFEDC, false)),
                        new WriteCoilResponse(0xAC02, 0xFB, 0xFEDC, false),
                        new int[] { 0xAC, 0x02, 0x00, 0x00, 0x00, 0x06, 0xFB, 0x05, 0xFE, 0xDC, 0x00, 0x00 }),
                new TestData(new ModbusRequest(0xAC03, 0xFB, MF.writeRegister(0xFEDC, true)),
                        new WriteCoilResponse(0xAC03, 0xFB, 0xFEDC, true),
                        new int[] { 0xAC, 0x03, 0x00, 0x00, 0x00, 0x06, 0xFB, 0x05, 0xFE, 0xDC, 0xff, 0x00 }),
                new TestData(new ModbusRequest(0xAB01, 0xFB, MF.readRegisters(ModbusRegisterType.INPUT, 0xFEDC, 3)),
                        new ReadWordRegistersResponse(0xAB01, 0xFB, ModbusRegisterType.INPUT, 0xFEDC,
                                new int[] { 0xFF01, 0xFF02, 0xFF03 }),
                        new int[] {
                                0xAB,
                                0x01,
                                0x00,
                                0x00,
                                0x00,
                                0x09,
                                0xFB,
                                0x04,
                                0x06,
                                0xff,
                                0x01,
                                0xff,
                                0x02,
                                0xff,
                                0x03 }),
                new TestData(new ModbusRequest(0xAB02, 0xFB, MF.readRegisters(ModbusRegisterType.HOLDING, 0xFEDC, 3)),
                        new ReadWordRegistersResponse(0xAB02, 0xFB, ModbusRegisterType.HOLDING, 0xFEDC,
                                new int[] { 0xFF01, 0xFF02, 0xFF03 }),
                        new int[] {
                                0xAB,
                                0x02,
                                0x00,
                                0x00,
                                0x00,
                                0x09,
                                0xFB,
                                0x03,
                                0x06,
                                0xff,
                                0x01,
                                0xff,
                                0x02,
                                0xff,
                                0x03 }),
                new TestData(
                        new ModbusRequest(0xAB04, 0xFB, MF.readRegisters(ModbusRegisterType.DISCRETE_INPUT, 0xFEDC, 3)),
                        new ReadBooleanRegistersResponse(0xAB04, 0xFB, ModbusRegisterType.DISCRETE_INPUT, 0xFEDC,
                                new boolean[] { true, false, true }),
                        new int[] { 0xAB, 0x04, 0x00, 0x00, 0x00, 0x04, 0xFB, 0x02, 0x01, 0x05 }),
                new TestData(
                        new ModbusRequest(0xAB05, 0xFB, MF.readRegisters(ModbusRegisterType.DISCRETE_INPUT, 0xFEDC, 3)),
                        new ExceptionResponse(0xAB05, 0xFB, 0x82, 0x01),
                        new int[] { 0xAB, 0x05, 0x00, 0x00, 0x00, 0x03, 0xFB, 0x82, 0x01 }),
                new TestData(new ModbusRequest(0xAB03, 0xFB, MF.readRegisters(ModbusRegisterType.COIL, 0xFEDC, 3)),
                        new ReadBooleanRegistersResponse(0xAB03, 0xFB, ModbusRegisterType.COIL, 0xFEDC,
                                new boolean[] { true, false, true }),
                        new int[] { 0xAB, 0x03, 0x00, 0x00, 0x00, 0x04, 0xFB, 0x01, 0x01, 0x05 }),
                new TestData(
                        new ModbusRequest(0xAB06, 0xFB, MF.readRegisters(ModbusRegisterType.DISCRETE_INPUT, 0xFEDC, 3)),
                        new MissMatchResponse(0xAB06),
                        new int[] { 0xAB, 0x06, 0x00, 0x00, 0x00, 0x03, 0xFB, 0x82, 0x01 }, false),
                new TestData(new ModbusRequest(0xAB07, 0xFB, MF.readRegisters(ModbusRegisterType.COIL, 0xFEDC, 3)),
                        new ReadBooleanRegistersResponse(0xAB07, 0xFB, ModbusRegisterType.COIL, 0xFEDC,
                                new boolean[] { true, false, true }),
                        new int[] { 0xAB, 0x07, 0x00, 0x00, 0x00, 0x04, 0xFB, 0x01, 0x01, 0x05 }),
                new TestData(
                        new ModbusRequest(0xAB08, 0xFB, MF.readRegisters(ModbusRegisterType.DISCRETE_INPUT, 0xFEDC, 3)),
                        new ReadBooleanRegistersResponse(0xAB08, 0xFB, ModbusRegisterType.DISCRETE_INPUT, 0xFEDC,
                                new boolean[] { true, false, true }),
                        new int[] { 0xAB, 0x08, 0x00, 0x00, 0x00, 0x04, 0xFB, 0x02, 0x01, 0x05 }),
                new TestData(
                        new ModbusRequest(0xAB09, 0xFB, MF.readRegisters(ModbusRegisterType.DISCRETE_INPUT, 0xFEDC, 3)),
                        new MissMatchResponse(0xAB09),
                        new int[] { 0xAB, 0x09, 0x00, 0x00, 0x00, 0x03, 0xFB, 0x82, 0x01 }, false),
                new TestData(
                        new ModbusRequest(0xAB0a, 0xFB, MF.readRegisters(ModbusRegisterType.DISCRETE_INPUT, 0xFEDC, 3)),
                        new ExceptionResponse(0xAB0a, 0xFB, 0x82, 0x01),
                        new int[] { 0xAB, 0x0a, 0x00, 0x00, 0x00, 0x03, 0xFB, 0x82, 0x01 }), };
    }

}
