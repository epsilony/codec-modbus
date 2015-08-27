package net.epsilony.utils.codec.modbus.reqres;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ReadBooleanRegistersResponseTest {

    static class TestData {
        boolean[] values;
        int[] buffer;

        ReadBooleanRegistersResponse createResponse() {
            ReadBooleanRegistersResponse response = new ReadBooleanRegistersResponse();
            response.setQuantityAndAllocate(values.length);
            for (int i = 0; i < values.length; i++) {
                response.setValue(i, values[i]);
            }
            return response;
        }

        public void assertBuffer(ByteBuf buf) {
            assertEquals(buffer.length, buf.readableBytes());
            for (int i = 0; buf.readableBytes() > 0; i++) {
                assertEquals(buffer[i], buf.readUnsignedByte());
            }
        }

        public TestData(boolean[] values, int[] buffer) {
            this.values = values;
            this.buffer = buffer;
        }

    }

    @Test
    public void testWritePduCore() {
        TestData[] testDatas = new TestData[] {
                new TestData(new boolean[] { true }, new int[] { 0x01, 0x01 }),
                new TestData(new boolean[] { false }, new int[] { 0x01, 0x00 }),
                new TestData(new boolean[] { true, false, false, true, false, false, false, true },
                        new int[] { 0x01, 0x89 }),
                new TestData(new boolean[] { true, false, false, true, false, false, false, true, true },
                        new int[] { 0x02, 0x89, 0x01 }), };

        ByteBuf buf = Unpooled.buffer();
        int randShift = new Random().nextInt(10) + 7;
        buf.writerIndex(randShift);
        buf.readerIndex(randShift);

        for (TestData data : testDatas) {
            ReadBooleanRegistersResponse response = data.createResponse();
            assertEquals(data.buffer.length, response.getPduCoreLength());
            response.writePduCore(buf);
            data.assertBuffer(buf);
        }

    }

}
