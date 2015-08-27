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

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import net.epsilony.utils.codec.modbus.ModbusRegisterType;
import net.epsilony.utils.codec.modbus.UnsupportedFunctionCodeException;
import net.epsilony.utils.codec.modbus.Utils;
import net.epsilony.utils.codec.modbus.func.ModbusFunction;
import net.epsilony.utils.codec.modbus.func.ReadBooleanRegistersFunction;
import net.epsilony.utils.codec.modbus.func.ReadWordRegistersFunction;
import net.epsilony.utils.codec.modbus.func.WriteCoilFunction;
import net.epsilony.utils.codec.modbus.func.WriteHoldingFunction;
import net.epsilony.utils.codec.modbus.reqres.ModbusRequest;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ModbusSlaveRequestDecoder extends ByteToMessageDecoder {
    private boolean withCheckSum = false;

    public boolean isWithCheckSum() {
        return withCheckSum;
    }

    public void setWithCheckSum(boolean withCheckSum) {
        this.withCheckSum = withCheckSum;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 6) {
            return;
        }
        int aduLength = in.getUnsignedShort(in.readerIndex() + 4);
        int wholeLength = 6 + aduLength + (withCheckSum ? 2 : 0);
        if (in.readableBytes() < wholeLength) {
            return;
        }

        if (withCheckSum) {
            checkSum(in, wholeLength);
        }
        int transectionId = in.readUnsignedShort();
        in.readerIndex(in.readerIndex() + 4);
        int unitId = in.readUnsignedByte();
        int functionCode = in.readUnsignedByte();
        ModbusFunction function;
        switch (functionCode) {
        case 0x01:
            function = new ReadBooleanRegistersFunction(ModbusRegisterType.COIL);
            break;
        case 0x02:
            function = new ReadBooleanRegistersFunction(ModbusRegisterType.INPUT_DISCRETE);
            break;
        case 0x03:
            function = new ReadWordRegistersFunction(ModbusRegisterType.HOLDING);
            break;
        case 0x04:
            function = new ReadWordRegistersFunction(ModbusRegisterType.INPUT);
            break;
        case 0x05:
            function = new WriteCoilFunction();
            break;
        case 0x06:
            function = new WriteHoldingFunction();
            break;
        default:
            throw new UnsupportedFunctionCodeException();
        }
        try {
            function.decodeRequestData(in);
        } catch (Throwable ex) {
            if (ex instanceof DecoderException) {
                throw ex;
            } else {
                throw new DecoderException(ex);
            }
        }
        ModbusRequest request = new ModbusRequest(transectionId, unitId, function);
        out.add(request);
        if (withCheckSum) {
            in.readerIndex(in.readerIndex() + 2);
        }
    }

    private void checkSum(ByteBuf in, int wholeLength) {
        int crcGet = in.getUnsignedShort(in.readerIndex() + wholeLength - 2);
        int crcCalc = Utils.crc(in, in.readerIndex(), wholeLength - 2);
        if (crcCalc != crcGet) {
            throw new DecoderException("wrong crc");
        }
    }

}
