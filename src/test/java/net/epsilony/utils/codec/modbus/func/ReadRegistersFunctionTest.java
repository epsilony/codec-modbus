package net.epsilony.utils.codec.modbus.func;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.epsilony.utils.codec.modbus.ModbusRegisterType;
import net.epsilony.utils.codec.modbus.reqres.ModbusResponse;

public class ReadRegistersFunctionTest {

    public ReadRegistersFunction mockFunction() {
        return new ReadRegistersFunction() {

            @Override
            public void decodeResponseData(ByteBuf data, ModbusResponse response) {
                throw new UnsupportedOperationException();

            }

            @Override
            protected void checkRegisterType(ModbusRegisterType registerType) {
            }

            @Override
            protected void checkQuantity(int quantity) {
            }
        };
    }

    public static class SampleData {
        public ModbusRegisterType registerType;
        public int startingAddress;
        public int quantity;
        public int[] buffer;

        public SampleData(ModbusRegisterType registerType, int startingAddress, int quantity, int[] buffer) {
            this.registerType = registerType;
            this.startingAddress = startingAddress;
            this.quantity = quantity;
            this.buffer = buffer;
        }

        public SampleData() {
        }

        public void setup(ReadRegistersFunction func) {
            func.setRegisterType(registerType);
            func.setQuantity(quantity);
            func.setStartingAddress(startingAddress);
        }

        public void assertBuffer(ByteBuf buf) {
            for (int i = 0; buf.readableBytes() > 0; i++) {
                assertEquals(buffer[i], buf.readUnsignedByte());
            }
        }

        public void write(ByteBuf buf) {
            for (int bufferByte : buffer) {
                buf.writeByte(bufferByte);
            }
        }

        public void assertFunction(ReadRegistersFunction func) {
            assertEquals(startingAddress, func.getStartingAddress());
            assertEquals(quantity, func.getQuantity());
        }

    }

    @Test
    public void testEncodeRequestData() {
        ReadRegistersFunction func = mockFunction();
        ByteBuf buf = Unpooled.buffer();

        int randShift = new Random().nextInt(10) + 7;
        buf.writerIndex(randShift);
        buf.readerIndex(randShift);

        SampleData[] datas = new SampleData[] {
                new SampleData(ModbusRegisterType.COIL, 0xF000, 0x203, new int[] { 0xF0, 0x00, 0x02, 0x03 }),
                new SampleData(ModbusRegisterType.HOLDING, 0x80B0, 0xF0E0, new int[] { 0x80, 0xB0, 0xF0, 0xE0 }) };
        for (SampleData data : datas) {
            data.setup(func);
            func.encodeRequestData(buf);
            data.assertBuffer(buf);
        }
    }

    @Test
    public void testDecodeRequestData() {
        ReadRegistersFunction func = mockFunction();
        ByteBuf buf = Unpooled.buffer();

        int randShift = new Random().nextInt(10) + 7;
        buf.writerIndex(randShift);
        buf.readerIndex(randShift);

        SampleData[] datas = new SampleData[] {
                new SampleData(ModbusRegisterType.COIL, 0xF000, 0x203, new int[] { 0xF0, 0x00, 0x02, 0x03 }),
                new SampleData(ModbusRegisterType.HOLDING, 0x80B0, 0xF0E0, new int[] { 0x80, 0xB0, 0xF0, 0xE0 }) };
        for (SampleData data : datas) {
            data.write(buf);
            func.decodeRequestData(buf);
            data.assertFunction(func);
        }
    }

}
