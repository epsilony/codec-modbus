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

import gnu.trove.list.array.TByteArrayList;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ReadBooleanRegistersResponse extends ReadRegistersResponse {

    private TByteArrayList values;

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
    protected void writePduData(ByteBuf out) {
        int mask = 1;
        int dataByte = 0;
        for (int i = 0; i < quantity; i++) {
            if (getValue(i)) {
                dataByte |= mask;
            }
            mask <<= 1;
            if (mask == 0x100) {
                mask = 1;
                dataByte = 0;
                out.writeByte(dataByte);
            }
        }
        if (mask != 1) {
            out.writeByte(dataByte);
        }

    }

    @Override
    protected int getReadDataLength() {
        return 3 + (7 + quantity) / 8;
    }

}
