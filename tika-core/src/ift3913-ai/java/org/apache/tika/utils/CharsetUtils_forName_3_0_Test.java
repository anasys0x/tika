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
package org.apache.tika.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.junit.jupiter.api.Test;

public class CharsetUtils_forName_3_0_Test {

    @Test
    public void testForName() {
        // Test valid charset names
        assertEquals(Charset.forName("UTF-8"), CharsetUtils.forName("UTF-8"));
        assertEquals(Charset.forName("ISO-8859-1"), CharsetUtils.forName("ISO-8859-1"));
        assertEquals(Charset.forName("windows-1252"), CharsetUtils.forName("windows-1252"));
        // Test invalid charset names
        assertThrows(UnsupportedCharsetException.class, () -> CharsetUtils.forName("invalid-charset"));
        // Test null charset name
        assertThrows(IllegalArgumentException.class, () -> CharsetUtils.forName(null));
    }
}
