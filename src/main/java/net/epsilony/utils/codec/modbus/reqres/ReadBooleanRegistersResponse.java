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
package net.epsilony.utils.codec.modbus.reqres;

import gnu.trove.list.array.TByteArrayList;
import io.netty.buffer.ByteBuf;
import net.epsilony.utils.codec.modbus.ModbusRegisterType;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ReadBooleanRegistersResponse extends ReadRegistersResponse {

    private TByteArrayList values;

    public ReadBooleanRegistersResponse() {
    }

    public ReadBooleanRegistersResponse(int transectionId, int unitId, ModbusRegisterType registerType,
            int startingAddress, boolean[] values) {
        super(transectionId, unitId, registerType, startingAddress);
        this.values = new TByteArrayList(values.length);
        quantity = values.length;
        for (boolean v : values) {
            this.values.add((byte) (v ? 1 : 0));
        }
    }

    @Override
    public void setQuantityAndAllocate(int quantity) {
        this.quantity = quantity;
        if (values == null) {
            values = new TByteArrayList(quantity);
        } else {
            values.clear();
            values.ensureCapacity(quantity);
        }
        values.fill(0, quantity, (byte) 0);
    }

    public boolean getValue(int index) {
        return values.get(index) != 0;
    }

    public void setValue(int index, boolean value) {
        values.set(index, value ? (byte) 1 : (byte) 0);
    }

    @Override
    public void writePduCore(ByteBuf out) {
        out.writeByte(getPduCoreLength() - 1);
        int mask = 1;
        int dataByte = 0;
        for (int i = 0; i < quantity; i++) {
            if (getValue(i)) {
                dataByte |= mask;
            }
            mask <<= 1;
            if (mask == 0x100) {
                out.writeByte(dataByte);
                mask = 1;
                dataByte = 0;
            }
        }
        if (mask != 1) {
            out.writeByte(dataByte);
        }

    }

    @Override
    public int getPduCoreLength() {
        return 1 + (7 + quantity) / 8;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReadBooleanRegistersResponse other = (ReadBooleanRegistersResponse) obj;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }

    @Override
    protected void checkRegisterType(ModbusRegisterType registerType) {
        if (registerType != ModbusRegisterType.COIL && registerType != ModbusRegisterType.DISCRETE_INPUT) {
            throw new IllegalArgumentException();
        }

    }

    @Override
    public String toString() {
        return "ReadBooleanRegistersResponse [transectionId=" + transectionId + ", unitId=" + unitId + ", registerType="
                + registerType + ", startingAddress=" + startingAddress + ", quantity=" + quantity + ", values="
                + values + "]";
    }

}
