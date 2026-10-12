package org.apache.tika.mime;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaType_union_11_0_Test {

    private Map<String, String> map1;

    private Map<String, String> map2;

    @BeforeEach
    public void setUp() {
        map1 = new HashMap<>();
        map1.put("key1", "value1");
        map2 = new HashMap<>();
        map2.put("key2", "value2");
    }

    @Test
    public void testUnion() throws Exception {
        Method method = MediaType.class.getDeclaredMethod("union", Map.class, Map.class);
        method.setAccessible(true);
        Map<String, String> result = (Map<String, String>) method.invoke(null, map1, map2);
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
        Map<String, String> emptyMap = new HashMap<>();
        result = (Map<String, String>) method.invoke(null, emptyMap, map1);
        assertEquals(1, result.size());
        assertEquals("value1", result.get("key1"));
        result = (Map<String, String>) method.invoke(null, map1, emptyMap);
        assertEquals(1, result.size());
        assertEquals("value1", result.get("key1"));
        Map<String, String> bothEmpty = new HashMap<>();
        result = (Map<String, String>) method.invoke(null, bothEmpty, bothEmpty);
        assertTrue(result.isEmpty());
    }
}
