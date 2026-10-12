package org.apache.tika.mime;

import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Method;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MediaType_set_5_0_Test {

    @Test
    public void testSetMethod() throws Exception {
        // Create an instance of MediaType
        MediaType mediaType1 = new MediaType("application", "json");
        MediaType mediaType2 = new MediaType("text", "html");
        Set<MediaType> mediaTypeSet = new HashSet<>();
        mediaTypeSet.add(mediaType1);
        mediaTypeSet.add(mediaType2);
        // Get the set method
        Method setMethod = MediaType.class.getDeclaredMethod("set", MediaType[].class);
        setMethod.setAccessible(true);
        // Invoke the set method using reflection
        Set<MediaType> result = (Set<MediaType>) setMethod.invoke(null, new Object[] { new MediaType[] { mediaType1, mediaType2 } });
        // Verify the result
        assertEquals(mediaTypeSet, result);
    }

    @Test
    public void testSetMethodWithNull() throws Exception {
        // Get the set method
        Method setMethod = MediaType.class.getDeclaredMethod("set", MediaType[].class);
        setMethod.setAccessible(true);
        // Invoke the set method using reflection with null
        Executable executable = () -> setMethod.invoke(null, (Object[]) null);
        assertThrows(NullPointerException.class, executable);
    }

    @Test
    public void testSetMethodWithEmptyArray() throws Exception {
        // Get the set method
        Method setMethod = MediaType.class.getDeclaredMethod("set", MediaType[].class);
        setMethod.setAccessible(true);
        // Invoke the set method using reflection with empty array
        Set<MediaType> result = (Set<MediaType>) setMethod.invoke(null, new Object[] { new MediaType[] {} });
        // Verify the result
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSetMethodWithDuplicateValues() throws Exception {
        // Create an instance of MediaType
        MediaType mediaType1 = new MediaType("application", "json");
        MediaType mediaType2 = new MediaType("application", "json");
        // Get the set method
        Method setMethod = MediaType.class.getDeclaredMethod("set", MediaType[].class);
        setMethod.setAccessible(true);
        // Invoke the set method using reflection
        Set<MediaType> result = (Set<MediaType>) setMethod.invoke(null, new Object[] { new MediaType[] { mediaType1, mediaType2 } });
        // Verify the result
        assertEquals(1, result.size());
    }
}
