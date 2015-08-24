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
package net.epsilony.utils.codec.modbus;

import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ModbusMasterRequestEncoder extends MessageToByteEncoder<ModbusRequest> {

    private Consumer<ModbusRequest> requestRegister;
    private boolean withCheckSum = false;

    public ModbusMasterRequestEncoder(Consumer<ModbusRequest> requestRegister) {
        this.requestRegister = requestRegister;
    }

    public ModbusMasterRequestEncoder() {
    }

    public boolean isWithCheckSum() {
        return withCheckSum;
    }

    public void setWithCheckSum(boolean withCheckSum) {
        this.withCheckSum = withCheckSum;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ModbusRequest msg, ByteBuf out) throws Exception {
        int from = out.writerIndex();
        out.writeShort(msg.getTransectionId());
        out.writeShort(0);
        out.writeShort(2 + msg.getFunction().getRequestDataLength());
        out.writeByte(msg.getUnitId());
        out.writeByte(msg.getFunction().getCode());
        msg.getFunction().encodeRequestData(out);
        requestRegister.accept(msg);
        if (withCheckSum) {
            out.writeShort(checkSum(out, from));
        }
    }

    private int checkSum(ByteBuf out, int from) {
        return Utils.crc(out, from, out.writerIndex() - from);
    }

    public Consumer<ModbusRequest> getRequestRegister() {
        return requestRegister;
    }

    public void setRequestRegister(Consumer<ModbusRequest> encodedRequestConsumer) {
        this.requestRegister = encodedRequestConsumer;
    }

}
