package org.apache.tika.io;

import org.apache.tika.io.LookaheadInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.InputStream;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LookaheadInputStream_read_3_0_Test {

    @Test
    public void testRead() throws IOException {
        byte[] data = { 1, 2, 3, 4, 5 };
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        LookaheadInputStream lookaheadInputStream = new LookaheadInputStream(inputStream, 5);
        byte[] buffer = new byte[5];
        int bytesRead = lookaheadInputStream.read(buffer, 0, 5);
        assertEquals(5, bytesRead);
        assertArrayEquals(data, buffer);
        bytesRead = lookaheadInputStream.read(buffer, 0, 5);
        assertEquals(-1, bytesRead);
    }

    @Test
    public void testReadWithPartialBuffer() throws IOException {
        byte[] data = { 1, 2, 3, 4, 5 };
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        LookaheadInputStream lookaheadInputStream = new LookaheadInputStream(inputStream, 3);
        byte[] buffer = new byte[5];
        int bytesRead = lookaheadInputStream.read(buffer, 0, 5);
        assertEquals(3, bytesRead);
        assertArrayEquals(new byte[] { 1, 2, 3, 0, 0 }, buffer);
        bytesRead = lookaheadInputStream.read(buffer, 3, 2);
        assertEquals(2, bytesRead);
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, buffer);
        bytesRead = lookaheadInputStream.read(buffer, 0, 5);
        assertEquals(-1, bytesRead);
    }

    @Test
    public void testReadWithEmptyStream() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
        LookaheadInputStream lookaheadInputStream = new LookaheadInputStream(inputStream, 5);
        byte[] buffer = new byte[5];
        int bytesRead = lookaheadInputStream.read(buffer, 0, 5);
        assertEquals(-1, bytesRead);
    }

    @Test
    public void testReadWithMarkAndReset() throws IOException {
        byte[] data = { 1, 2, 3, 4, 5 };
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        LookaheadInputStream lookaheadInputStream = new LookaheadInputStream(inputStream, 5);
        byte[] buffer = new byte[5];
        lookaheadInputStream.read(buffer, 0, 3);
        assertArrayEquals(new byte[] { 1, 2, 3, 0, 0 }, buffer);
        lookaheadInputStream.mark(0);
        lookaheadInputStream.read(buffer, 3, 2);
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, buffer);
        lookaheadInputStream.reset();
        int bytesRead = lookaheadInputStream.read(buffer, 0, 5);
        assertEquals(5, bytesRead);
        assertArrayEquals(data, buffer);
    }

    @Test
    public void testReadWithBufferFull() throws IOException {
        byte[] data = { 1, 2, 3, 4, 5 };
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        LookaheadInputStream lookaheadInputStream = new LookaheadInputStream(inputStream, 5);
        byte[] buffer = new byte[5];
        int bytesRead = lookaheadInputStream.read(buffer, 0, 5);
        assertEquals(5, bytesRead);
        assertArrayEquals(data, buffer);
        bytesRead = lookaheadInputStream.read(buffer, 0, 5);
        assertEquals(-1, bytesRead);
    }

    @Test
    public void testReadWithBufferPartial() throws IOException {
        byte[] data = { 1, 2, 3, 4, 5 };
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        LookaheadInputStream lookaheadInputStream = new LookaheadInputStream(inputStream, 3);
        byte[] buffer = new byte[5];
        int bytesRead = lookaheadInputStream.read(buffer, 0, 5);
        assertEquals(3, bytesRead);
        assertArrayEquals(new byte[] { 1, 2, 3, 0, 0 }, buffer);
        bytesRead = lookaheadInputStream.read(buffer, 3, 2);
        assertEquals(2, bytesRead);
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, buffer);
        bytesRead = lookaheadInputStream.read(buffer, 0, 5);
        assertEquals(-1, bytesRead);
    }
}
