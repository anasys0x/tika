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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests authored after the AI phase to distinguish the remaining PIT mutants. */
public class CharsetUtilsManualTest {

    private static final String ICU_ONLY_NAME = "tika-fake-icu";
    private static final String ICU_FAILING_NAME = "tika-fake-icu-error";

    private Method savedForNameIcu;
    private Method savedIsSupportedIcu;

    @BeforeEach
    public void saveIcuHooks() throws ReflectiveOperationException {
        savedForNameIcu = (Method) icuField("getCharsetICU").get(null);
        savedIsSupportedIcu = (Method) icuField("isSupportedICU").get(null);
    }

    @AfterEach
    public void restoreIcuHooks() throws ReflectiveOperationException {
        icuField("getCharsetICU").set(null, savedForNameIcu);
        icuField("isSupportedICU").set(null, savedIsSupportedIcu);
    }

    @Test
    public void testForNameRejectsNoAsIllegalCharsetName() {
        // "no" is a placeholder seen in real documents, not a charset name.
        assertThrows(IllegalCharsetNameException.class, () -> CharsetUtils.forName("no"));
        // A well-formed but unknown name fails differently, so the exception type matters.
        assertThrows(UnsupportedCharsetException.class, () -> CharsetUtils.forName("bogus-name"));
    }

    @Test
    public void testForNameFallsBackToIcuForUnknownNames() throws ReflectiveOperationException {
        installFakeIcu();
        assertEquals(StandardCharsets.UTF_16LE, CharsetUtils.forName(ICU_ONLY_NAME));
    }

    @Test
    public void testForNamePrefersRemappedCommonCharsetOverIcu() throws ReflectiveOperationException {
        installFakeIcu();
        // "win-1252" is remapped to "windows-1252", which must win over the ICU answer.
        assertEquals(Charset.forName("windows-1252"), CharsetUtils.forName("win-1252"));
    }

    @Test
    public void testIsSupportedAcceptsCharsetKnownOnlyToIcu() throws ReflectiveOperationException {
        installFakeIcu();
        assertTrue(CharsetUtils.isSupported(ICU_ONLY_NAME));
    }

    @Test
    public void testIsSupportedReturnsFalseWhenIcuFails() throws ReflectiveOperationException {
        installFakeIcu();
        assertFalse(CharsetUtils.isSupported(ICU_FAILING_NAME));
    }

    private static void installFakeIcu() throws ReflectiveOperationException {
        icuField("getCharsetICU").set(null, FakeCharsetICU.class.getMethod("forNameICU", String.class));
        icuField("isSupportedICU").set(null, FakeCharsetICU.class.getMethod("isSupported", String.class));
    }

    private static Field icuField(String name) throws NoSuchFieldException {
        Field field = CharsetUtils.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    /**
     * Stand-in for ICU4J's CharsetICU, which is not on tika-core's test classpath.
     * It answers UTF-16LE for every name, so a UTF-16LE result can only come from ICU.
     */
    public static class FakeCharsetICU {

        public static Charset forNameICU(String name) {
            return StandardCharsets.UTF_16LE;
        }

        public static boolean isSupported(String name) {
            if (ICU_FAILING_NAME.equals(name)) {
                throw new IllegalStateException("simulated ICU failure");
            }
            return ICU_ONLY_NAME.equals(name);
        }
    }
}
