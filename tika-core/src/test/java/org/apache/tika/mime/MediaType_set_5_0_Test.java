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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;

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
        Executable executable = () -> MediaType.set((MediaType[]) null);
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
