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

import org.junit.jupiter.api.Test;

/** Tests authored after the AI phase to distinguish the remaining PIT mutants. */
public class LookaheadInputStreamManualTest {

    @Test
    public void testCloseRestoresNonzeroInitialPosition() throws IOException {
        ByteArrayInputStream source = new ByteArrayInputStream(new byte[]{10, 20, 30, 40});
        assertEquals(10, source.read());
        try (LookaheadInputStream lookahead = new LookaheadInputStream(source, 2)) {
            assertEquals(20, lookahead.read());
            assertEquals(30, lookahead.read());
            assertEquals(-1, lookahead.read());
        }
        // The mark belongs to the wrapper's starting position, not the source's start.
        assertEquals(20, source.read());
        assertEquals(30, source.read());
        assertEquals(40, source.read());
    }

    @Test
    public void testBulkReadClampsToRemainingWindowAfterSingleByteRead() throws IOException {
        ByteArrayInputStream source = new ByteArrayInputStream(new byte[]{10, 20, 30, 40, 50});
        try (LookaheadInputStream lookahead = new LookaheadInputStream(source, 3)) {
            assertEquals(10, lookahead.read());
            byte[] destination = new byte[8];
            assertEquals(2, lookahead.read(destination, 2, 5));
            assertArrayEquals(new byte[]{0, 0, 20, 30, 0, 0, 0, 0}, destination);
            assertEquals(-1, lookahead.read(destination, 0, destination.length));
        }
    }

    @Test
    public void testSourceEndRestoresUnderlyingPositionAutomatically() throws IOException {
        ByteArrayInputStream source = new ByteArrayInputStream(new byte[]{10, 20});
        try (LookaheadInputStream lookahead = new LookaheadInputStream(source, 4)) {
            assertEquals(10, lookahead.read());
            assertEquals(20, lookahead.read());
            assertEquals(-1, lookahead.read());
            // A short source reaches EOF during fill(), which closes and resets the source.
            assertEquals(10, source.read());
        }
    }

    @Test
    public void testFullWindowDoesNotResetSourceBeforeClose() throws IOException {
        ByteArrayInputStream source = new ByteArrayInputStream(new byte[]{10, 20});
        try (LookaheadInputStream lookahead = new LookaheadInputStream(source, 2)) {
            assertEquals(10, lookahead.read());
            assertEquals(20, lookahead.read());
            assertEquals(-1, lookahead.read());
            // A full window must not issue another fill and reset the source early.
            assertEquals(0, source.available());
        }
        assertEquals(2, source.available());
        assertEquals(10, source.read());
    }
}
