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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ReadWordRegistersFunction extends ReadRegistersFunction {

    public ReadWordRegistersFunction() {
    };

    public ReadWordRegistersFunction(ModbusRegisterType registerType) {
        setRegisterType(registerType);
    }

    @Override
    public void decodeResponseData(ByteBuf data, ModbusResponse response) {
        if (data.readableBytes() < getResponseDataLength()) {
            throw new DecoderException();
        }

        ReadWordRegistersResponse readWordRegistersResponse = (ReadWordRegistersResponse) response;

        int dataContentBytes = data.readUnsignedByte();
        if (dataContentBytes != getResponseDataLength() - 1) {
            throw new DecoderException();
        }

        readWordRegistersResponse.setQuantityAndAllocate(quantity);
        for (int i = 0; i < quantity; i++) {
            readWordRegistersResponse.setValue(i, data.readUnsignedShort());
        }
    }

    public int getResponseDataLength() {
        return 2 * quantity + 1;
    }

    @Override
    protected void checkRegisterType(ModbusRegisterType registerType) {
        if (registerType != ModbusRegisterType.HOLDING && registerType != ModbusRegisterType.INPUT) {
            throw new IllegalArgumentException("register type should be HOLDING or INPUT not " + registerType);
        }
    }

    @Override
    protected void checkQuantity(int quantity) {
        if (quantity < 1 || quantity > 125) {
            throw new IllegalArgumentException("quantity should be in [1,125] not " + quantity);
        }
    }

}
