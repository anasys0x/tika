package org.apache.tika.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.InputStream;

public class LookaheadInputStream_mark_7_0_Test {

    private LookaheadInputStream lookaheadInputStream;

    private ByteArrayInputStream inputStream;

    @BeforeEach
    public void setUp() {
        byte[] testData = { 1, 2, 3, 4, 5 };
        inputStream = new ByteArrayInputStream(testData);
        lookaheadInputStream = new LookaheadInputStream(inputStream, 5);
    }

    @Test
    public void testMark() throws IOException, NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        // Set position to 2
        lookaheadInputStream.read();
        lookaheadInputStream.read();
        // Invoke mark method using reflection
        Method markMethod = LookaheadInputStream.class.getDeclaredMethod("mark", int.class);
        markMethod.setAccessible(true);
        markMethod.invoke(lookaheadInputStream, 10);
        // Check if mark is set correctly
        assertEquals(2, lookaheadInputStream.mark);
    }
}
