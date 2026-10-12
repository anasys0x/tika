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

import org.junit.jupiter.api.Test;

public class CharsetUtils_clean_2_0_Test {

    @Test
    public void testClean() {
        assertEquals("UTF-8", CharsetUtils.clean("UTF-8"));
        assertEquals("ISO-8859-1", CharsetUtils.clean("ISO-8859-1"));
        assertEquals("US-ASCII", CharsetUtils.clean("US-ASCII"));
        assertEquals("UTF-16", CharsetUtils.clean("UTF-16"));
        assertEquals("UTF-16BE", CharsetUtils.clean("UTF-16BE"));
        assertEquals("UTF-16LE", CharsetUtils.clean("UTF-16LE"));
        assertEquals("UTF-32", CharsetUtils.clean("UTF-32"));
        assertEquals("UTF-32BE", CharsetUtils.clean("UTF-32BE"));
        assertEquals("UTF-32LE", CharsetUtils.clean("UTF-32LE"));
        assertEquals(null, CharsetUtils.clean("iso 8859-1"));
        assertEquals("windows-1252", CharsetUtils.clean("cp-1252"));
        assertEquals("windows-1252", CharsetUtils.clean("win-1252"));
        assertEquals(null, CharsetUtils.clean("invalid-charset"));
    }
}
