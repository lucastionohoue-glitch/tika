package org.apache.tika.io;

import org.apache.tika.io.FilenameUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.utils.StringUtils;
import org.junit.jupiter.api.function.Executable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.tika.extractor.EmbeddedDocumentUtil;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

public class FilenameUtils_getSanitizedEmbeddedFilePath_4_0_Test {

    @Test
    public void testGetSanitizedEmbeddedFilePath_withNullPath() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(anyString())).thenReturn(null);
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertNull(result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withEmptyPath() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(anyString())).thenReturn("");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertNull(result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathContainingProtocol() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(anyString())).thenReturn("http://example.com/file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathContainingInvalidCharacters() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(anyString())).thenReturn("example?file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("example_file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathExceedingMaxLength() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(anyString())).thenReturn("examplefile.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 5);
        assertEquals("example...", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathEndingWithMultipleSlashes() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(anyString())).thenReturn("example//file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("examplefile.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathEndingWithDot() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(anyString())).thenReturn("example.file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("example_file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathEndingWithDoubleDot() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(anyString())).thenReturn("example..file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("example_file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathEndingWithColon() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(anyString())).thenReturn("example:file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("example_file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathEndingWithBackslash() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(anyString())).thenReturn("example\\file.txt");
        String result = FilenameUtils.getSanitizedEmbeddedFilePath(metadata, "txt", 100);
        assertEquals("examplefile.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withPathEndingWithForwardSlash() throws Exception {
        Metadata metadata = mock(Metadata.class);
        when(metadata.get(anyString())).thenReturn("example/file.txt");
    }
}
