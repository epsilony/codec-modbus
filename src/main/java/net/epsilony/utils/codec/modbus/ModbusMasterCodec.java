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
import java.util.function.IntFunction;

import io.netty.channel.CombinedChannelDuplexHandler;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ModbusMasterCodec
        extends CombinedChannelDuplexHandler<ModbusMasterResponseDecoder, ModbusMasterRequestEncoder> {

    public ModbusMasterCodec() {
        super(new ModbusMasterResponseDecoder(), new ModbusMasterRequestEncoder());
    }

    public ModbusMasterCodec(IntFunction<ModbusRequest> transectionRequestRetriever,
            Consumer<ModbusRequest> requestRegister) {
        super(new ModbusMasterResponseDecoder(transectionRequestRetriever),
                new ModbusMasterRequestEncoder(requestRegister));
    }

    void setWithCheckSum(boolean value) {
        inboundHandler().setWithCheckSum(value);
        outboundHandler().setWithCheckSum(value);
    }

    boolean isWithCheckSum() {
        return inboundHandler().isWithCheckSum();
    }

}
