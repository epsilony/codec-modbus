package net.epsilony.utils.codec.modbus.reqres;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ReadWordRegistersResponseTest {

    static class TestData {
        int[] values;
        int[] buffer;

        ReadWordRegistersResponse createResponse() {
            ReadWordRegistersResponse response = new ReadWordRegistersResponse();
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

        public TestData(int[] values, int[] buffer) {
            this.values = values;
            this.buffer = buffer;
        }

    }

    @Test
    public void testWritePduCore() {
        TestData[] testDatas = new TestData[] {
                new TestData(new int[] { 0xABCD }, new int[] { 0x02, 0xAB, 0xCD }),
                new TestData(new int[] { 0xDEAF, 0xFACE }, new int[] { 0x04, 0xDE, 0xAF, 0xFA, 0xCE }), };

        ByteBuf buf = Unpooled.buffer();
        int randShift = new Random().nextInt(10) + 7;
        buf.writerIndex(randShift);
        buf.readerIndex(randShift);

        for (TestData data : testDatas) {
            ReadWordRegistersResponse response = data.createResponse();
            assertEquals(data.buffer.length, response.getPduCoreLength());
            response.writePduCore(buf);
            data.assertBuffer(buf);
        }

    }

}
