/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LookaheadInputStream_read_2_0_Test {

    private LookaheadInputStream lookaheadInputStream;

    private ByteArrayInputStream byteArrayInputStream;

    @BeforeEach
    public void setUp() {
        byteArrayInputStream = new ByteArrayInputStream(new byte[]{ 0x01, 0x02, 0x03});
        lookaheadInputStream = new LookaheadInputStream(byteArrayInputStream, 3);
    }

    @Test
    public void testRead() throws IOException, NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Method readMethod = LookaheadInputStream.class.getDeclaredMethod("read");
        readMethod.setAccessible(true);
        assertEquals(0x01, readMethod.invoke(lookaheadInputStream));
        assertEquals(0x02, readMethod.invoke(lookaheadInputStream));
        assertEquals(0x03, readMethod.invoke(lookaheadInputStream));
        assertEquals(-1, readMethod.invoke(lookaheadInputStream));
    }

    @Test
    public void testReadWithEmptyStream() throws IOException, NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        byteArrayInputStream = new ByteArrayInputStream(new byte[0]);
        lookaheadInputStream = new LookaheadInputStream(byteArrayInputStream, 3);
        Method readMethod = LookaheadInputStream.class.getDeclaredMethod("read");
        readMethod.setAccessible(true);
        assertEquals(-1, readMethod.invoke(lookaheadInputStream));
    }

    @Test
    public void testReadWithSingleByteStream() throws IOException, NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        byteArrayInputStream = new ByteArrayInputStream(new byte[]{ 0x01});
        lookaheadInputStream = new LookaheadInputStream(byteArrayInputStream, 3);
        Method readMethod = LookaheadInputStream.class.getDeclaredMethod("read");
        readMethod.setAccessible(true);
        assertEquals(0x01, readMethod.invoke(lookaheadInputStream));
        assertEquals(-1, readMethod.invoke(lookaheadInputStream));
    }

    @Test
    public void testReadWithMultipleReads() throws IOException, NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Method readMethod = LookaheadInputStream.class.getDeclaredMethod("read");
        readMethod.setAccessible(true);
        assertEquals(0x01, readMethod.invoke(lookaheadInputStream));
        assertEquals(0x02, readMethod.invoke(lookaheadInputStream));
        assertEquals(0x03, readMethod.invoke(lookaheadInputStream));
        assertEquals(-1, readMethod.invoke(lookaheadInputStream));
        assertEquals(-1, readMethod.invoke(lookaheadInputStream));
    }
}
