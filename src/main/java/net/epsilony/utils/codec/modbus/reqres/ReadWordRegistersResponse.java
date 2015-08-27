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

import gnu.trove.list.array.TShortArrayList;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ReadWordRegistersResponse extends ReadRegistersResponse {
    private TShortArrayList values;

    public ReadWordRegistersResponse() {
    }

    public ReadWordRegistersResponse(int transectionId, int unitId, int functionCode, int startingAddress,
            int[] values) {
        super(transectionId, unitId, functionCode, startingAddress);
        this.quantity = values.length;
        this.values = new TShortArrayList(values.length);
        for (int v : values) {
            this.values.add((short) v);
        }
    }

    @Override
    public void setQuantityAndAllocate(int quantity) {
        if (null == values) {
            values = new TShortArrayList(quantity);
        } else {
            values.clear();
            values.ensureCapacity(quantity);
        }
        this.quantity = quantity;
        values.fill(0, quantity, (short) 0);
    }

    public void setValue(int index, int value) {
        values.set(index, (short) value);
    }

    public int getValue(int offset) {
        return values.get(offset) & 0xFFFF;
    }

    @Override
    public void writePduCore(ByteBuf out) {
        out.writeByte(2 * quantity);
        for (int i = 0; i < quantity; i++) {
            out.writeShort(getValue(i));
        }
    }

    @Override
    public int getPduCoreLength() {
        return 1 + 2 * quantity;
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
        ReadWordRegistersResponse other = (ReadWordRegistersResponse) obj;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }

}
