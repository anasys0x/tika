package org.apache.tika.io;

import org.apache.tika.io.LookaheadInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.InputStream;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LookaheadInputStream_read_2_0_Test {

    private LookaheadInputStream lookaheadInputStream;

    private ByteArrayInputStream inputStream;

    @BeforeEach
    public void setUp() {
        inputStream = new ByteArrayInputStream(new byte[] { 0x01, 0x02, 0x03 });
        lookaheadInputStream = new LookaheadInputStream(inputStream, 3);
    }

    @Test
    public void testRead() throws IOException, NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Method fillMethod = LookaheadInputStream.class.getDeclaredMethod("fill");
        fillMethod.setAccessible(true);
        // Test reading the first byte
        int firstByte = lookaheadInputStream.read();
        assertEquals(0x01, firstByte);
        // Test reading the second byte
        int secondByte = lookaheadInputStream.read();
        assertEquals(0x02, secondByte);
        // Test reading the third byte
        int thirdByte = lookaheadInputStream.read();
        assertEquals(0x03, thirdByte);
        // Test reading beyond the end of the stream
        int endOfStream = lookaheadInputStream.read();
        assertEquals(-1, endOfStream);
        // Test fill method when buffer is full
        inputStream.reset();
        fillMethod.invoke(lookaheadInputStream);
        assertEquals(3, lookaheadInputStream.buffered);
        // Test fill method when stream is null
        lookaheadInputStream.stream = null;
        fillMethod.invoke(lookaheadInputStream);
        assertEquals(3, lookaheadInputStream.buffered);
    }

    @Test
    public void testAvailable() throws IOException {
        assertEquals(3, lookaheadInputStream.available());
        lookaheadInputStream.read();
        assertEquals(2, lookaheadInputStream.available());
        lookaheadInputStream.read();
        assertEquals(1, lookaheadInputStream.available());
        lookaheadInputStream.read();
        assertEquals(0, lookaheadInputStream.available());
    }

    @Test
    public void testClose() throws IOException {
        lookaheadInputStream.close();
        assertTrue(lookaheadInputStream.stream.isClosed());
    }
}
