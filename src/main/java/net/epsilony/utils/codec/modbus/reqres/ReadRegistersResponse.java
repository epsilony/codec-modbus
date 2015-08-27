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

    protected ModbusRegisterType registerType;
    protected int startingAddress;
    protected int quantity;

    public ReadRegistersResponse() {

    }

    public ReadRegistersResponse(int transectionId, int unitId, ModbusRegisterType registerType, int startingAddress) {
        super(transectionId, unitId);
        this.registerType = registerType;
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
        return registerType;
    }

    public void setRegisterType(ModbusRegisterType registerType) {
        checkRegisterType(registerType);
        this.registerType = registerType;
    }

    protected abstract void checkRegisterType(ModbusRegisterType registerType);

    @Override
    public int getFunctionCode() {
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
        if (getRegisterType() != other.getRegisterType())
            return false;
        return true;
    }

}