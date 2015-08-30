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

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class SimpTransectionIdDispatcher implements TransectionIdDispatcher {
    public static final int DEFAULT_MAX_TRANSECTION_ID = 1023;

    private int[] transectionMasks;
    private int maxTransectionId = DEFAULT_MAX_TRANSECTION_ID;
    private int next = 0;
    private int borrowed = 0;

    public int getMaxTransectionId() {
        return maxTransectionId;
    }

    public void setMaxTransectionId(int maxTransectionId) {
        if (null != transectionMasks) {
            throw new IllegalArgumentException("max transection id could only be set before any borrow() callings");
        }
        if (maxTransectionId < 0 || maxTransectionId > 0xFFFF) {
            throw new IllegalArgumentException("max transection id should in [0,0xFFFF] not " + maxTransectionId);
        }
        this.maxTransectionId = maxTransectionId;
    }

    @Override
    public int borrow() {
        if (null == transectionMasks) {
            transectionMasks = new int[maxTransectionId / 32 + 1];
        }

        if (borrowed > maxTransectionId) {
            return -1;
        }

        int maskInt, mask;
        int from = next; // only because of hating infinite cycling
        while (true) {
            maskInt = transectionMasks[next / 32];
            mask = 1 << (next % 32);
            if (0 == (maskInt & mask)) {
                break;
            }
            next++;
            next %= (maxTransectionId + 1);
            if (next == from) {
                throw new IllegalStateException();
            }
        }

        int result = next;
        transectionMasks[next / 32] |= mask;
        next++;
        next %= (maxTransectionId + 1);
        borrowed++;
        return result;
    }

    @Override
    public void repay(int transectionId) {
        if (null == transectionMasks) {
            throw new IllegalStateException("repay(...) is called before borrow()");
        }
        if (transectionId < 0 || transectionId > maxTransectionId) {
            throw new IllegalArgumentException("Illegal transection id, maybe not borrowed from here");
        }
        int maskInt = transectionMasks[transectionId / 32];
        int mask = 1 << (transectionId % 32);
        if ((maskInt & mask) != 0) {
            borrowed--;
            transectionMasks[transectionId / 32] &= (~mask);
        }
    }

    @Override
    public int countBorrowed() {
        return borrowed;
    }

    @Override
    public void reset() {
        transectionMasks = null;
        next = 0;
        borrowed = 0;
    }

}
