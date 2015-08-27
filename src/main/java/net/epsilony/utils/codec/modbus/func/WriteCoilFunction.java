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
import net.epsilony.utils.codec.modbus.UnexpectedResponseException;
import net.epsilony.utils.codec.modbus.reqres.ModbusResponse;
import net.epsilony.utils.codec.modbus.reqres.WriteCoilResponse;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class WriteCoilFunction implements ModbusFunction {
    private int address;
    private boolean value;

    @Override
    public String getName() {
        return "write a coil";
    }

    @Override
    public int getCode() {
        return 0x05;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        if (address < 0 || address > 0xffff) {
            throw new IllegalArgumentException();
        }
        this.address = address;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public void decodeRequestData(ByteBuf data) {
        setAddress(data.readUnsignedShort());
        value = data.readUnsignedShort() == 0xFF00;
    }

    @Override
    public void decodeResponseData(ByteBuf data, ModbusResponse response) {
        WriteCoilResponse rResponse = (WriteCoilResponse) response;
        rResponse.setAddress(data.readUnsignedShort());
        rResponse.setValue(data.readUnsignedShort() == 0xFF00);
        if (rResponse.getAddress() != address || rResponse.getValue() != value) {
            throw new UnexpectedResponseException();
        }
    }

    @Override
    public void encodeRequestData(ByteBuf data) {
        data.writeShort(address);
        data.writeShort(value ? 0xFF00 : 0x0000);

    }

    @Override
    public int getRequestDataLength() {
        return 4;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + address;
        result = prime * result + (value ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WriteCoilFunction other = (WriteCoilFunction) obj;
        if (address != other.address)
            return false;
        if (value != other.value)
            return false;
        return true;
    }

}
