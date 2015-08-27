package net.epsilony.utils.codec.modbus.func;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.epsilony.utils.codec.modbus.ModbusRegisterType;
import net.epsilony.utils.codec.modbus.reqres.ReadWordRegistersResponse;

public class ReadWordRegistersFunctionTest {

    static class SampleData {
        ModbusRegisterType registerType;
        int startingAddress;
        int values[];
        int[] buffer;

        ReadWordRegistersFunction function() {
            ReadWordRegistersFunction func = new ReadWordRegistersFunction();
            func.setRegisterType(registerType);
            func.setStartingAddress(startingAddress);
            func.setQuantity(values.length);
            return func;
        }

        void write(ByteBuf buf) {
            for (int byteData : buffer) {
                buf.writeByte(byteData);
            }
        }

        public void assertResponse(ReadWordRegistersResponse response) {
            assertEquals(values.length, response.getQuantity());
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], response.getValue(i));
            }
        }

        public SampleData(ModbusRegisterType registerType, int startingAddress, int[] values, int[] buffer) {
            this.registerType = registerType;
            this.startingAddress = startingAddress;
            this.values = values;
            this.buffer = buffer;
        }

    }

    @Test
    public void testDecodeResponseData() {
        SampleData[] datas = new SampleData[] {
                new SampleData(ModbusRegisterType.HOLDING, 11, new int[] { 0xABCD, 0xDCBA },
                        new int[] { 0x04, 0xAB, 0xCD, 0xDC, 0xBA }),
                new SampleData(ModbusRegisterType.HOLDING, 11, new int[] { 0xABCD, 0xDCBA, 0x8081 },
                        new int[] { 0x06, 0xAB, 0xCD, 0xDC, 0xBA, 0x80, 0x81 }), };

        ByteBuf buf = Unpooled.buffer();
        int randShift = new Random().nextInt(10) + 7;
        buf.writerIndex(randShift);
        buf.readerIndex(randShift);

        ReadWordRegistersResponse response = new ReadWordRegistersResponse();
        for (SampleData data : datas) {
            data.write(buf);
            data.function().decodeResponseData(buf, response);
            data.assertResponse(response);
        }
    }

}
