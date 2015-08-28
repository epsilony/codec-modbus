package net.epsilony.utils.codec.modbus.func;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.epsilony.utils.codec.modbus.ModbusRegisterType;
import net.epsilony.utils.codec.modbus.reqres.ReadBooleanRegistersResponse;

public class ReadBooleanRegistersFunctionTest {

    static class SampleData {
        ModbusRegisterType registerType;
        int startingAddress;
        boolean values[];
        int[] buffer;

        ReadBooleanRegistersFunction function() {
            ReadBooleanRegistersFunction func = new ReadBooleanRegistersFunction();
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

        public void assertResponse(ReadBooleanRegistersResponse response) {
            assertEquals(startingAddress, response.getStartingAddress());
            assertEquals(values.length, response.getQuantity());
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], response.getValue(i));
            }
        }

        public SampleData(ModbusRegisterType registerType, int startingAddress, boolean[] values, int[] buffer) {
            this.registerType = registerType;
            this.startingAddress = startingAddress;
            this.values = values;
            this.buffer = buffer;
        }

    }

    @Test
    public void testDecodeResponseData() {
        SampleData[] datas = new SampleData[] {
                new SampleData(ModbusRegisterType.DISCRETE_INPUT, 11, new boolean[] { true }, new int[] { 0x01, 0x01 }),
                new SampleData(ModbusRegisterType.COIL, 13,
                        new boolean[] { false, true, false, true, false, false, false, true },
                        new int[] { 0x01, 0x8A }),
                new SampleData(ModbusRegisterType.COIL, 13,
                        new boolean[] { false, true, false, true, false, false, true }, new int[] { 0x01, 0x4A }),
                new SampleData(ModbusRegisterType.COIL, 13,
                        new boolean[] { false, true, false, true, false, false, true, true, false },
                        new int[] { 0x02, 0xCA, 0x00 }),
                new SampleData(ModbusRegisterType.COIL, 13,
                        new boolean[] { false, true, false, true, false, false, true, true, false, true },
                        new int[] { 0x02, 0xCA, 0x02 }) };

        ByteBuf buf = Unpooled.buffer();
        int randShift = new Random().nextInt(10) + 7;
        buf.writerIndex(randShift);
        buf.readerIndex(randShift);

        ReadBooleanRegistersResponse response = new ReadBooleanRegistersResponse();
        for (SampleData data : datas) {
            data.write(buf);
            data.function().decodeResponseData(buf, response);
            data.assertResponse(response);
        }

    }

}
