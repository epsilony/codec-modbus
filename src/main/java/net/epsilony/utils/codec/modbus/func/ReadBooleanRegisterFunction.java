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
package net.epsilony.utils.codec.modbus.func;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.epsilony.utils.codec.modbus.ModbusRegisterType;
import net.epsilony.utils.codec.modbus.reqres.ModbusResponse;
import net.epsilony.utils.codec.modbus.reqres.ReadBooleanRegistersResponse;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ReadBooleanRegisterFunction extends ReadRegistersFunction {

    public ReadBooleanRegisterFunction() {

    }

    public ReadBooleanRegisterFunction(ModbusRegisterType registerType) {
        setRegisterType(registerType);
    }

    @Override
    protected void checkRegisterType(ModbusRegisterType registerType) {
        if (registerType != ModbusRegisterType.COIL && registerType != ModbusRegisterType.INPUT_DISCRETE) {
            throw new IllegalArgumentException("register type should be COIL or INPUT_DISCRETE not " + registerType);
        }
    }

    @Override
    protected void checkQuantity(int quantity) {
        if (quantity < 1 || quantity > 2000) {
            throw new IllegalArgumentException("quantity should be in [1,2000] not " + quantity);
        }
    }

    @Override
    public void decodeResponseData(ByteBuf data, ModbusResponse response) {

        if (data.readableBytes() < getResponseDataLength()) {
            throw new DecoderException();
        }

        ReadBooleanRegistersResponse readBooleanRegistersResponse = (ReadBooleanRegistersResponse) response;

        int dataContentBytes = data.readUnsignedByte();
        if (dataContentBytes != getResponseDataLength() - 1) {
            throw new DecoderException();
        }
        readBooleanRegistersResponse.setQuantityAndAllocate(quantity);
        int dataByte = 0;
        int mask = 0;
        for (int i = 0; i < quantity; i++) {
            if (i % 8 == 0) {
                dataByte = data.readUnsignedByte();
                mask = 1;
            }
            readBooleanRegistersResponse.setValue(i, (mask & dataByte) != 0);
            mask <<= 1;
        }

    }

    public int getResponseDataLength() {
        return 1 + (7 + quantity) / 8;
    }

}
