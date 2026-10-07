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

public class LookaheadInputStream_reset_8_0_Test {

    private LookaheadInputStream lookaheadInputStream;

    private ByteArrayInputStream inputStream;

    @BeforeEach
    public void setUp() {
        byte[] testData = { 1, 2, 3, 4, 5 };
        inputStream = new ByteArrayInputStream(testData);
        lookaheadInputStream = new LookaheadInputStream(inputStream, 5);
    }

    @Test
    public void testReset() throws IOException, NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        // Move the position to the middle of the buffer
        lookaheadInputStream.read();
        lookaheadInputStream.read();
        lookaheadInputStream.read();
        // Set a mark at the current position
        lookaheadInputStream.mark(0);
        // Move the position further
        lookaheadInputStream.read();
        lookaheadInputStream.read();
        // Reset to the mark position
        Method resetMethod = LookaheadInputStream.class.getDeclaredMethod("reset");
        resetMethod.setAccessible(true);
        resetMethod.invoke(lookaheadInputStream);
        // Verify that the position has been reset to the mark position
        assertEquals(3, lookaheadInputStream.position);
    }
}
