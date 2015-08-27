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

import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ExceptionResponse extends ModbusResponse {
    private int exceptionCode;

    public ExceptionResponse(int transectionId, int unitId, int functionCode, int exceptionCode) {
        super(transectionId, unitId, functionCode);
        this.exceptionCode = exceptionCode;
    }

    public ExceptionResponse() {

    }

    public int getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(int exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    @Override
    public void writePduCore(ByteBuf out) {
        out.writeByte(exceptionCode);
    }

    @Override
    public int getPduCoreLength() {
        return 1;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + exceptionCode;
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
        ExceptionResponse other = (ExceptionResponse) obj;
        if (exceptionCode != other.exceptionCode)
            return false;
        return true;
    }

}
