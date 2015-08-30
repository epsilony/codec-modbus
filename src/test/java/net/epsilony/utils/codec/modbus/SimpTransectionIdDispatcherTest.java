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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class SimpTransectionIdDispatcherTest {

    @Test
    public void testMax31() {
        SimpTransectionIdDispatcher dispatcher = new SimpTransectionIdDispatcher();
        dispatcher.setMaxTransectionId(31);
        for (int i = 0; i < 32; i++) {
            assertEquals(i, dispatcher.borrow());
            assertEquals(i + 1, dispatcher.countBorrowed());
        }
        for (int i = 0; i < 7; i++) {
            assertEquals(-1, dispatcher.borrow());
        }

        for (int i = 10; i <= 20; i++) {
            dispatcher.repay(i);
            assertEquals(i, dispatcher.borrow());
        }

        for (int i = 5; i <= 10; i++) {
            dispatcher.repay(i);
        }

        for (int i = 5; i <= 10; i++) {
            assertEquals(i, dispatcher.borrow());
        }
    }

    @Test
    public void testMax32() {
        SimpTransectionIdDispatcher dispatcher = new SimpTransectionIdDispatcher();
        dispatcher.setMaxTransectionId(32);
        for (int i = 0; i < 33; i++) {
            assertEquals(i, dispatcher.borrow());
            assertEquals(i + 1, dispatcher.countBorrowed());
        }
        for (int i = 0; i < 7; i++) {
            assertEquals(-1, dispatcher.borrow());
        }

        for (int i = 10; i <= 32; i++) {
            dispatcher.repay(i);
            assertEquals(i, dispatcher.borrow());
        }

        for (int i = 25; i <= 32; i++) {
            dispatcher.repay(i);
        }

        for (int i = 25; i <= 32; i++) {
            assertEquals(i, dispatcher.borrow());
        }
    }

}
