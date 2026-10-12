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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Tests écrits à la main pour les mutants qui survivent au run 2 de PIT
 * (voir docs/tests-manuels.md).
 */
public class MediaTypeManualTest {

    @Test
    public void testSetFromStringsSkipsUnparseable() {
        Set<MediaType> types = MediaType.set("text/plain", null, "pas-un-type", "text/html", "text/plain");
        assertEquals(Set.of(MediaType.TEXT_PLAIN, MediaType.TEXT_HTML), types);
        assertThrows(UnsupportedOperationException.class, () -> types.add(MediaType.OCTET_STREAM));
    }

    @Test
    public void testParseCharsetFirst() {
        // TIKA-350 : certains serveurs mettent le charset avant le type
        MediaType type = MediaType.parse("charset=UTF-8; text/html");
        assertEquals("text/html; charset=UTF-8", type.toString());
    }

    @Test
    public void testParseCachesSimpleTypes() {
        // nouveau type à chaque exécution : le cache est statique, et PIT réutilise la même JVM
        // d'un mutant à l'autre, donc un nom fixe serait déjà dans le cache
        String string = "application/x-" + UUID.randomUUID();
        MediaType first = MediaType.parse(string);
        MediaType second = MediaType.parse(string);
        assertSame(first, second);
    }
}
