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
public abstract class ReadRegistersFunction implements ModbusFunction {

    protected ModbusRegisterType registerType;
    protected int startAddress;
    protected int quantity;

    public ModbusRegisterType getRegisterType() {
        return registerType;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(int startAddress) {
        if (startAddress < 0 || startAddress > 0xffff) {
            throw new IllegalArgumentException();
        }
        this.startAddress = startAddress;
    }

    public int getQuantity() {
        return quantity;
    }

    protected abstract void checkRegisterType(ModbusRegisterType registerType);

    public void setRegisterType(ModbusRegisterType registerType) {
        checkRegisterType(registerType);
        this.registerType = registerType;
    }

    protected abstract void checkQuantity(int quantity);

    public void setQuantity(int quantity) {
        checkQuantity(quantity);
        this.quantity = quantity;
    }

    @Override
    public String getName() {
        switch (registerType) {
        case COIL:
            return "read coils";
        case HOLDING:
            return "read holding registers";
        case INPUT:
            return "read input registers";
        case INPUT_DISCRETE:
            return "read input discretes registers";
        default:
            throw new IllegalStateException();
        }
    }

    @Override
    public int getCode() {
        switch (registerType) {
        case COIL:
            return 0x01;
        case HOLDING:
            return 0x03;
        case INPUT:
            return 0x04;
        case INPUT_DISCRETE:
            return 0x02;
        default:
            throw new IllegalStateException();
        }
    }

    @Override
    public void decodeRequestData(ByteBuf data) {
        if (data.readableBytes() < 4) {
            throw new DecoderException();
        }
        startAddress = data.readUnsignedShort();
        setQuantity(data.readUnsignedShort());
    }

    @Override
    public void encodeRequestData(ByteBuf data) {
        data.writeShort(startAddress);
        data.writeShort(quantity);
    }

    @Override
    public int getRequestDataLength() {
        return 4;
    }

}
