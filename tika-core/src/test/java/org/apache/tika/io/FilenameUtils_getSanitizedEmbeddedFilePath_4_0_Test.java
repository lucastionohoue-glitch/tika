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
package org.apache.tika.io;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.*;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;

public class FilenameUtils_getSanitizedEmbeddedFilePath_4_0_Test {

    @Test
    public void testGetSanitizedEmbeddedFilePath_withNullPath() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(any(Property.class))).thenReturn(null);
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertNull(result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withEmptyPath() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(any(Property.class))).thenReturn("");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertNull(result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathContainingProtocol() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(any(Property.class))).thenReturn("http://example.com/file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("example.com/file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathContainingInvalidCharacters() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(any(Property.class))).thenReturn("example?file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("example%3Ffile.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathExceedingMaxLength() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(any(Property.class))).thenReturn("examplefile.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 10);
        assertEquals("exa....txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathEndingWithMultipleSlashes() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(any(Property.class))).thenReturn("example//file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("example/file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathEndingWithDot() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(any(Property.class))).thenReturn("example.file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("example.file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathEndingWithDoubleDot() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(any(Property.class))).thenReturn("example..file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("example.file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathEndingWithColon() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(any(Property.class))).thenReturn("example:file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("example/file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathEndingWithBackslash() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(any(Property.class))).thenReturn("example\\file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("example/file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathEndingWithForwardSlash() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(any(Property.class))).thenReturn("example/file.txt");
    }
}
