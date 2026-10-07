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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LookaheadInputStream_read_3_0_Test {

    private LookaheadInputStream lookaheadInputStream;

    private ByteArrayInputStream inputStream;

    @BeforeEach
    public void setUp() {
        inputStream = new ByteArrayInputStream(new byte[]{ 1, 2, 3, 4, 5});
        lookaheadInputStream = new LookaheadInputStream(inputStream, 5);
    }

    @Test
    public void testReadWithBufferFull() throws IOException {
        byte[] buffer = new byte[5];
        int bytesRead = lookaheadInputStream.read(buffer, 0, 5);
        assertEquals(5, bytesRead);
        assertArrayEquals(new byte[]{ 1, 2, 3, 4, 5}, buffer);
        bytesRead = lookaheadInputStream.read(buffer, 0, 1);
        assertEquals(-1, bytesRead);
    }

    @Test
    public void testReadWithZeroLength() throws IOException {
        byte[] buffer = new byte[3];
        int bytesRead = lookaheadInputStream.read(buffer, 0, 0);
        assertEquals(0, bytesRead);
        assertArrayEquals(new byte[]{ 0, 0, 0}, buffer);
    }

    @Test
    public void testReadWithOffset() throws IOException {
        byte[] buffer = new byte[5];
        int bytesRead = lookaheadInputStream.read(buffer, 2, 3);
        assertEquals(3, bytesRead);
        assertArrayEquals(new byte[]{ 0, 0, 1, 2, 3}, buffer);
        bytesRead = lookaheadInputStream.read(buffer, 3, 2);
        assertEquals(2, bytesRead);
        assertArrayEquals(new byte[]{ 0, 0, 1, 4, 5}, buffer);
    }
}
