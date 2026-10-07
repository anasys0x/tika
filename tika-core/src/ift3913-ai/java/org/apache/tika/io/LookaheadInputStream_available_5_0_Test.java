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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LookaheadInputStream_available_5_0_Test {

    private LookaheadInputStream lookaheadInputStream;

    private byte[] buffer;

    private ByteArrayInputStream inputStream;

    @BeforeEach
    public void setUp() {
        buffer = new byte[10];
        inputStream = new ByteArrayInputStream(new byte[]{ 1, 2, 3, 4, 5});
        lookaheadInputStream = new LookaheadInputStream(inputStream, buffer.length);
    }

    @Test
    public void testAvailable() throws IOException {
        // Test when no data is read
        assertEquals(0, lookaheadInputStream.available());
        // Read some data
        lookaheadInputStream.read();
        assertEquals(4, lookaheadInputStream.available());
        // Read all data
        while (lookaheadInputStream.read() != -1) {
        }
        assertEquals(0, lookaheadInputStream.available());
        // Reset and read again
        inputStream.reset();
        lookaheadInputStream.reset();
        lookaheadInputStream.read();
        assertEquals(4, lookaheadInputStream.available());
    }

    @Test
    public void testAvailableAfterMark() throws IOException {
        // Read some data and mark
        lookaheadInputStream.read();
        lookaheadInputStream.mark(10);
        // Read more data
        lookaheadInputStream.read();
        assertEquals(3, lookaheadInputStream.available());
        // Reset to mark
        lookaheadInputStream.reset();
        assertEquals(4, lookaheadInputStream.available());
    }

    @Test
    public void testAvailableAfterSkip() throws IOException {
        // Read some data and skip
        lookaheadInputStream.read();
        lookaheadInputStream.skip(2);
        // Check available
        assertEquals(2, lookaheadInputStream.available());
    }

    @Test
    public void testAvailableAfterClose() throws IOException {
        // Close the stream
        lookaheadInputStream.close();
        // Check available
        assertEquals(0, lookaheadInputStream.available());
    }
}
