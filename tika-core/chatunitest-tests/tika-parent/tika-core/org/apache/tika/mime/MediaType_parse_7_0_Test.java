package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.function.Executable;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaType_parse_7_0_Test {

    private MediaType mediaType;

    @BeforeEach
    public void setUp() {
        mediaType = new MediaType("text", "plain", Collections.emptyMap());
    }

    @Test
    public void testParseNullString() {
        MediaType result = MediaType.parse(null);
        assertNull(result);
    }

    @Test
    public void testParseSimpleType() {
        MediaType result = MediaType.parse("text/plain");
        assertNotNull(result);
        assertEquals("text/plain", result.toString());
    }

    @Test
    public void testParseComplexType() {
        MediaType result = MediaType.parse("text/plain; charset=UTF-8");
        assertNotNull(result);
        assertEquals("text/plain; charset=UTF-8", result.toString());
    }

    @Test
    public void testParseInvalidType() {
        MediaType result = MediaType.parse("text");
        assertNull(result);
    }

    @Test
    public void testParseEmptyString() {
        MediaType result = MediaType.parse("");
        assertNull(result);
    }

    @Test
    public void testParseWhitespaceString() {
        MediaType result = MediaType.parse("  ");
        assertNull(result);
    }

    @Test
    public void testParseNullParameters() {
        MediaType result = MediaType.parse("text/plain;");
        assertNotNull(result);
        assertEquals("text/plain", result.toString());
    }

    @Test
    public void testParseEmptyParameters() {
        MediaType result = MediaType.parse("text/plain;");
        assertNotNull(result);
        assertEquals("text/plain", result.toString());
    }

    @Test
    public void testParseWhitespaceParameters() {
        MediaType result = MediaType.parse("text/plain;  ");
        assertNotNull(result);
        assertEquals("text/plain", result.toString());
    }

    @Test
    public void testParseNullCharset() {
        MediaType result = MediaType.parse("text/plain; charset=");
        assertNull(result);
    }

    @Test
    public void testParseEmptyCharset() {
        MediaType result = MediaType.parse("text/plain; charset=");
        assertNull(result);
    }

    @Test
    public void testParseWhitespaceCharset() {
        MediaType result = MediaType.parse("text/plain; charset=  ");
        assertNull(result);
    }

    @Test
    public void testParseInvalidCharset() {
        MediaType result = MediaType.parse("text/plain; charset=invalid");
        assertNull(result);
    }

    @Test
    public void testParseNullParameter() {
        MediaType result = MediaType.parse("text/plain; name=");
        assertNull(result);
    }

    @Test
    public void testParseEmptyParameter() {
        MediaType result = MediaType.parse("text/plain; name=");
        assertNull(result);
    }

    @Test
    public void testParseWhitespaceParameter() {
        MediaType result = MediaType.parse("text/plain; name=  ");
        assertNull(result);
    }

    @Test
    public void testParseInvalidParameter() {
        MediaType result = MediaType.parse("text/plain; name=invalid");
        assertNull(result);
    }

    @Test
    public void testParseNullValue() {
        MediaType result = MediaType.parse("text/plain; name=value=");
        assertNull(result);
    }

    @Test
    public void testParseEmptyValue() {
        MediaType result = MediaType.parse("text/plain; name=value=");
        assertNull(result);
    }

    @Test
    public void testParseWhitespaceValue() {
        MediaType result = MediaType.parse("text/plain; name=value=  ");
        assertNull(result);
    }

    @Test
    public void testParseInvalidValue() {
        MediaType result = MediaType.parse("text/plain; name=value=invalid");
        assertNull(result);
    }

    @Test
    public void testParseNullTypeAndSubtype() {
        MediaType result = MediaType.parse("/");
        assertNull(result);
    }

    @Test
    public void testParseEmptyTypeAndSubtype() {
        MediaType result = MediaType.parse("");
        assertNull(result);
    }
}
