package org.apache.tika.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class LookaheadInputStream_close_0_0_Test {

    private LookaheadInputStream lookaheadInputStream;

    private InputStream inputStream;

    @BeforeEach
    public void setUp() {
        inputStream = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
        lookaheadInputStream = new LookaheadInputStream(inputStream, 3);
    }

    @Test
    public void testClose() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Test when stream is not null
        lookaheadInputStream.close();
        Field streamField = LookaheadInputStream.class.getDeclaredField("stream");
        streamField.setAccessible(true);
        assertNull(streamField.get(lookaheadInputStream));
    }

    @Test
    public void testCloseWithNullStream() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Test when stream is already null
        Field streamField = LookaheadInputStream.class.getDeclaredField("stream");
        streamField.setAccessible(true);
        streamField.set(lookaheadInputStream, null);
        lookaheadInputStream.close();
        assertNull(streamField.get(lookaheadInputStream));
    }
}
