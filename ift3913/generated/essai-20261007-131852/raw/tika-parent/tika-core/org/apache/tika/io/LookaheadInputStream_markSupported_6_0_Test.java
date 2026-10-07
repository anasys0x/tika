package org.apache.tika.io;

import java.io.InputStream;
import java.io.IOException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class LookaheadInputStream_markSupported_6_0_Test {

    @Test
    public void testMarkSupported() throws IOException {
        InputStream mockStream = mock(InputStream.class);
        LookaheadInputStream lookaheadInputStream = new LookaheadInputStream(mockStream, 1024);
        boolean result = lookaheadInputStream.markSupported();
        assertTrue(result);
    }
}
