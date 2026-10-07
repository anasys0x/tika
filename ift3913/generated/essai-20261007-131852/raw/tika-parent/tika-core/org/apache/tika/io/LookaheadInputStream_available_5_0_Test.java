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

public class LookaheadInputStream_available_5_0_Test {

    private LookaheadInputStream lookaheadInputStream;

    private byte[] buffer;

    private ByteArrayInputStream inputStream;

    @BeforeEach
    public void setUp() {
        buffer = new byte[10];
        inputStream = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 });
        lookaheadInputStream = new LookaheadInputStream(inputStream, buffer.length);
    }

    @Test
    public void testAvailable() throws IOException {
        // Test when no data is read
        assertEquals(0, lookaheadInputStream.available());
        // Read some data
        lookaheadInputStream.read();
        assertEquals(9, lookaheadInputStream.available());
        // Read all data
        while (lookaheadInputStream.read() != -1) {
        }
        assertEquals(0, lookaheadInputStream.available());
        // Reset and read again
        inputStream.reset();
        lookaheadInputStream.reset();
        lookaheadInputStream.read();
        assertEquals(9, lookaheadInputStream.available());
    }

    @Test
    public void testAvailableAfterMark() throws IOException {
        // Read some data and mark
        lookaheadInputStream.read();
        lookaheadInputStream.mark(10);
        // Read more data
        lookaheadInputStream.read();
        assertEquals(8, lookaheadInputStream.available());
        // Reset to mark
        lookaheadInputStream.reset();
        assertEquals(9, lookaheadInputStream.available());
    }

    @Test
    public void testAvailableAfterSkip() throws IOException {
        // Read some data and skip
        lookaheadInputStream.read();
        lookaheadInputStream.skip(2);
        // Check available
        assertEquals(7, lookaheadInputStream.available());
    }

    @Test
    public void testAvailableAfterClose() throws IOException {
        // Close the stream
        lookaheadInputStream.close();
        // Check available
        assertEquals(0, lookaheadInputStream.available());
    }
}
