package org.apache.tika.io;

import org.apache.tika.io.LookaheadInputStream;
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

public class LookaheadInputStream_read_3_0_Test {

    private LookaheadInputStream lookaheadInputStream;

    private ByteArrayInputStream inputStream;

    @BeforeEach
    public void setUp() {
        inputStream = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 });
        lookaheadInputStream = new LookaheadInputStream(inputStream, 5);
    }

    @Test
    public void testReadWithBufferFull() throws IOException {
        byte[] buffer = new byte[5];
        int bytesRead = lookaheadInputStream.read(buffer, 0, 5);
        assertEquals(5, bytesRead);
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, buffer);
        bytesRead = lookaheadInputStream.read(buffer, 0, 1);
        assertEquals(-1, bytesRead);
    }

    @Test
    public void testReadWithZeroLength() throws IOException {
        byte[] buffer = new byte[3];
        int bytesRead = lookaheadInputStream.read(buffer, 0, 0);
        assertEquals(0, bytesRead);
        assertArrayEquals(new byte[] { 0, 0, 0 }, buffer);
    }

    @Test
    public void testReadWithOffset() throws IOException {
        byte[] buffer = new byte[5];
        int bytesRead = lookaheadInputStream.read(buffer, 2, 3);
        assertEquals(3, bytesRead);
        assertArrayEquals(new byte[] { 0, 0, 1, 2, 3 }, buffer);
        bytesRead = lookaheadInputStream.read(buffer, 3, 2);
        assertEquals(2, bytesRead);
        assertArrayEquals(new byte[] { 0, 0, 1, 4, 5 }, buffer);
    }
}
