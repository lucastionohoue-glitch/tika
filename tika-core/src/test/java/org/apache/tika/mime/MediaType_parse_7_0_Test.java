/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.mime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.*;
import org.mockito.*;

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
        assertNotNull(result);
        assertEquals("text/plain; charset=", result.toString());
    }

    @Test
    public void testParseEmptyCharset() {
        MediaType result = MediaType.parse("text/plain; charset=");
        assertNotNull(result);
        assertEquals("text/plain; charset=", result.toString());
    }

    @Test
    public void testParseWhitespaceCharset() {
        MediaType result = MediaType.parse("text/plain; charset=  ");
        assertNotNull(result);
        assertEquals("text/plain; charset=", result.toString());
    }

    @Test
    public void testParseInvalidCharset() {
        MediaType result = MediaType.parse("text/plain; charset=invalid");
        assertNotNull(result);
        assertEquals("text/plain; charset=invalid", result.toString());
    }

    @Test
    public void testParseNullParameter() {
        MediaType result = MediaType.parse("text/plain; name=");
        assertNotNull(result);
        assertEquals("text/plain; name=", result.toString());
    }

    @Test
    public void testParseEmptyParameter() {
        MediaType result = MediaType.parse("text/plain; name=");
        assertNotNull(result);
        assertEquals("text/plain; name=", result.toString());
    }

    @Test
    public void testParseWhitespaceParameter() {
        MediaType result = MediaType.parse("text/plain; name=  ");
        assertNotNull(result);
        assertEquals("text/plain; name=", result.toString());
    }

    @Test
    public void testParseInvalidParameter() {
        MediaType result = MediaType.parse("text/plain; name=invalid");
        assertNotNull(result);
        assertEquals("text/plain; name=invalid", result.toString());
    }

    @Test
    public void testParseNullValue() {
        MediaType result = MediaType.parse("text/plain; name=value=");
        assertNotNull(result);
        assertEquals("text/plain; name=\"value\\=\"", result.toString());
    }

    @Test
    public void testParseEmptyValue() {
        MediaType result = MediaType.parse("text/plain; name=value=");
        assertNotNull(result);
        assertEquals("text/plain; name=\"value\\=\"", result.toString());
    }

    @Test
    public void testParseWhitespaceValue() {
        MediaType result = MediaType.parse("text/plain; name=value=  ");
        assertNotNull(result);
        assertEquals("text/plain; name=\"value\\=\"", result.toString());
    }

    @Test
    public void testParseInvalidValue() {
        MediaType result = MediaType.parse("text/plain; name=value=invalid");
        assertNotNull(result);
        assertEquals("text/plain; name=\"value\\=invalid\"", result.toString());
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
