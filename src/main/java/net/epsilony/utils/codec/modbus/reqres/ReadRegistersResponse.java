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

import net.epsilony.utils.codec.modbus.ModbusRegisterType;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public abstract class ReadRegistersResponse extends ModbusResponse {

    protected int startingAddress;
    protected int quantity;

    public ReadRegistersResponse() {

    }

    public ReadRegistersResponse(int transectionId, int unitId, int functionCode, int startingAddress, int quantity) {
        super(transectionId, unitId, functionCode);
        this.startingAddress = startingAddress;
        setQuantityAndAllocate(quantity);
    }

    public ReadRegistersResponse(int transectionId, int unitId, int functionCode, int startingAddress) {
        super(transectionId, unitId, functionCode);
        this.startingAddress = startingAddress;
    }

    public int getStartingAddress() {
        return startingAddress;
    }

    public void setStartingAddress(int startingAddress) {
        this.startingAddress = startingAddress;
    }

    public int getQuantity() {
        return quantity;
    }

    public abstract void setQuantityAndAllocate(int quantity);

    public ModbusRegisterType getRegisterType() {
        switch (getFunctionCode()) {
        case 0x01:
            return ModbusRegisterType.COIL;
        case 0x02:
            return ModbusRegisterType.INPUT_DISCRETE;
        case 0x03:
            return ModbusRegisterType.HOLDING;
        case 0x04:
            return ModbusRegisterType.INPUT;
        default:
            throw new IllegalStateException();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + quantity;
        result = prime * result + startingAddress;
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
        ReadRegistersResponse other = (ReadRegistersResponse) obj;
        if (quantity != other.quantity)
            return false;
        if (startingAddress != other.startingAddress)
            return false;
        return true;
    }

}