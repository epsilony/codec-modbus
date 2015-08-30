package net.epsilony.utils.codec.modbus.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import net.epsilony.utils.codec.modbus.Utils;
import net.epsilony.utils.codec.modbus.func.ModbusFunction;
import net.epsilony.utils.codec.modbus.reqres.ModbusRequest;
import net.epsilony.utils.codec.modbus.reqres.ModbusResponse;

public class ModbusMasterRequestEncoderTest {

    @Test
    public void test() {
        ModbusMasterRequestEncoder encoder = new ModbusMasterRequestEncoder();
        EmbeddedChannel channel = new EmbeddedChannel(encoder);

        ModbusRequest request = new ModbusRequest(0xAB00, 0x83, new ModbusFunction() {

            @Override
            public int getRequestDataLength() {
                return 4;
            }

            @Override
            public String getName() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getCode() {
                return 0x06;
            }

            @Override
            public void encodeRequestData(ByteBuf data) {
                data.writeShort(0xABCD);
                data.writeShort(0xCDEF);
            }

            @Override
            public void decodeResponseData(ByteBuf data, ModbusResponse response) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void decodeRequestData(ByteBuf data) {
                throw new UnsupportedOperationException();
            }
        });

        for (int i = 0; i < 3; i++) {
            request.setTransectionId(0xAB00 + i);
            channel.writeOutbound(request);
            ByteBuf buf = (ByteBuf) channel.readOutbound();
            int[] buffer = new int[] { 0xAB, i, 0, 0, 0, 6, 0x83, 0x06, 0xab, 0xcd, 0xcd, 0xef };
            assertEquals(buffer.length, buf.readableBytes());
            for (int b : buffer) {
                assertEquals(b, buf.readUnsignedByte());
            }
        }

        encoder.setWithCheckSum(true);
        for (int i = 0; i < 3; i++) {
            request.setTransectionId(0xAB00 + i + 3);
            channel.writeOutbound(request);
            ByteBuf buf = (ByteBuf) channel.readOutbound();
            int[] buffer = new int[] { 0xAB, i + 3, 0, 0, 0, 6, 0x83, 0x06, 0xab, 0xcd, 0xcd, 0xef };
            assertEquals(buffer.length, buf.readableBytes() - 2);
            for (int b : buffer) {
                assertEquals(b, buf.readUnsignedByte());
            }
            int calcCrc = Utils.crc(buf, buf.readerIndex() - buffer.length, buffer.length);
            assertEquals(calcCrc, buf.readUnsignedShort());
        }

    }

}
