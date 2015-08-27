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
import java.util.function.IntFunction;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import net.epsilony.utils.codec.modbus.UnsupportedFunctionCodeException;
import net.epsilony.utils.codec.modbus.Utils;
import net.epsilony.utils.codec.modbus.reqres.ExceptionResponse;
import net.epsilony.utils.codec.modbus.reqres.MissMatchResponse;
import net.epsilony.utils.codec.modbus.reqres.ModbusRequest;
import net.epsilony.utils.codec.modbus.reqres.ModbusResponse;
import net.epsilony.utils.codec.modbus.reqres.ReadBooleanRegistersResponse;
import net.epsilony.utils.codec.modbus.reqres.ReadWordRegistersResponse;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ModbusMasterResponseDecoder extends ByteToMessageDecoder {

    private boolean withCheckSum = false;
    private IntFunction<ModbusRequest> transectingRequestRetriever;

    public ModbusMasterResponseDecoder() {

    }

    public ModbusMasterResponseDecoder(IntFunction<ModbusRequest> transectingRequestRetriever) {
        this.transectingRequestRetriever = transectingRequestRetriever;
    }

    public boolean isWithCheckSum() {
        return withCheckSum;
    }

    public void setWithCheckSum(boolean withCheckSum) {
        this.withCheckSum = withCheckSum;
    }

    public IntFunction<ModbusRequest> getTransectingRequestRetriever() {
        return transectingRequestRetriever;
    }

    public void setTransectingRequestRetriever(IntFunction<ModbusRequest> transectingRequestGetter) {
        this.transectingRequestRetriever = transectingRequestGetter;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 6) {
            return;
        }
        int adupLength = in.getUnsignedShort(in.readerIndex() + 4);
        int wholeLength = adupLength + 6 + (withCheckSum ? 2 : 0);
        if (wholeLength > in.readableBytes()) {
            return;
        }
        if (withCheckSum) {
            checkSum(in, wholeLength);
        }
        int transectionId = in.readUnsignedShort();
        ModbusRequest request = transectingRequestRetriever.apply(transectionId);
        if (null == request) {
            in.readerIndex(in.readerIndex() + wholeLength - 2);
            out.add(new MissMatchResponse(transectionId));
            return;
        }
        in.readerIndex(in.readerIndex() + 4);
        int unitId = in.readUnsignedByte();
        if (unitId != request.getUnitId()) {
            throw new DecoderException();
        }
        int functionCode = in.readUnsignedByte();
        if ((functionCode & 0x7F) != request.getFunction().getCode()) {
            throw new DecoderException();
        }

        ModbusResponse response;

        if ((functionCode & 0x80) == 0) {
            switch (functionCode) {
            case 0x01:
            case 0x02:
                response = new ReadBooleanRegistersResponse();
                break;
            case 0x03:
            case 0x04:
                response = new ReadWordRegistersResponse();
                break;
            default:
                throw new UnsupportedFunctionCodeException();
            }
            request.getFunction().decodeResponseData(in, response);
        } else {
            ExceptionResponse exResponse = new ExceptionResponse();
            exResponse.setExceptionCode(in.readUnsignedByte());
            response = exResponse;
        }
        response.setFunctionCode(functionCode);
        response.setTransectionId(transectionId);
        response.setUnitId(unitId);

        out.add(response);

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
