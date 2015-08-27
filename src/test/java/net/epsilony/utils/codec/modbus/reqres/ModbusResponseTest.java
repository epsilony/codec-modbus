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
package net.epsilony.utils.codec.modbus.reqres;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ModbusResponseTest {

    static class MockResponse extends ModbusResponse {

        int functionCode;

        @Override
        public int getFunctionCode() {
            return functionCode;
        }

        public void setFunctionCode(int functionCode) {
            this.functionCode = functionCode;
        }

        @Override
        public void writePduCore(ByteBuf out) {
            out.writeByte(0xAB);
            out.writeByte(0xCD);
            out.writeByte(0xEF);

        }

        @Override
        public int getPduCoreLength() {
            return 3;
        }

    }

    static class TestData {
        int transectionId;
        int unitId;
        int functionCode;
        int[] buffer;

        public TestData(int transectionId, int unitId, int functionCode, int[] buffer) {
            super();
            this.transectionId = transectionId;
            this.unitId = unitId;
            this.functionCode = functionCode;
            this.buffer = buffer;
        }

        ModbusResponse createResponse() {
            MockResponse response = new MockResponse();
            response.setTransectionId(transectionId);
            response.setFunctionCode(functionCode);
            response.setUnitId(unitId);
            return response;
        }

        void assertBuffer(ByteBuf buf) {
            assertEquals(buffer.length, buf.readableBytes());
            for (int i = 0; buf.isReadable(); i++) {
                assertEquals(buffer[i], buf.readUnsignedByte());
            }
        }
    }

    @Test
    public void testEncode() {
        TestData[] datas = new TestData[] {
                new TestData(0xFEED, 0x83, 0x81,
                        new int[] { 0xFE, 0xED, 0x00, 0x00, 0x00, 0x05, 0x83, 0x81, 0xAB, 0xCD, 0xEF }) };

        ByteBuf buf = Unpooled.buffer();
        int randShift = new Random().nextInt(10) + 7;
        buf.writerIndex(randShift);
        buf.readerIndex(randShift);

        for (TestData data : datas) {
            ModbusResponse response = data.createResponse();
            response.encode(buf);
            data.assertBuffer(buf);
        }
    }

}
