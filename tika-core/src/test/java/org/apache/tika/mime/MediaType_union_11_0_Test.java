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
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.mockito.*;

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
