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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;

/**
 * Tests écrits à la main pour les mutants qui survivent au run 2 de PIT
 * (voir docs/tests-manuels.md).
 */
public class FilenameUtilsManualTest {

    // ordre de getEmbeddedPath
    private static final Property[] PATH_ORDER = {
            TikaCoreProperties.EMBEDDED_RESOURCE_PATH,
            TikaCoreProperties.INTERNAL_PATH,
            TikaCoreProperties.RESOURCE_NAME_KEY,
            TikaCoreProperties.EMBEDDED_RELATIONSHIP_ID,
            TikaCoreProperties.ORIGINAL_RESOURCE_NAME
    };

    // ordre de getEmbeddedName
    private static final Property[] NAME_ORDER = {
            TikaCoreProperties.RESOURCE_NAME_KEY,
            TikaCoreProperties.INTERNAL_PATH,
            TikaCoreProperties.EMBEDDED_RELATIONSHIP_ID,
            TikaCoreProperties.EMBEDDED_RESOURCE_PATH,
            TikaCoreProperties.ORIGINAL_RESOURCE_NAME
    };

    @Test
    public void testEmbeddedPathPropertyPriority() {
        String[] expected = {"embedded.txt", "internal.txt", "resource.txt", "relationship.txt", "original.txt"};
        for (int first = 0; first < PATH_ORDER.length; first++) {
            Metadata metadata = metadataFrom(PATH_ORDER, expected, first);
            assertEquals(expected[first], FilenameUtils.getSanitizedEmbeddedFilePath(metadata, ".bin", 50),
                    "propriétés remplies à partir de " + PATH_ORDER[first].getName());
        }
    }

    @Test
    public void testEmbeddedNamePropertyPriority() {
        String[] expected = {"resource.txt", "internal.txt", "relationship.txt", "embedded.txt", "original.txt"};
        for (int first = 0; first < NAME_ORDER.length; first++) {
            Metadata metadata = metadataFrom(NAME_ORDER, expected, first);
            assertEquals(expected[first], FilenameUtils.getSanitizedEmbeddedFileName(metadata, ".bin", 50),
                    "propriétés remplies à partir de " + NAME_ORDER[first].getName());
        }
    }

    @Test
    public void testEmbeddedFilePathMaxLengthBoundaries() {
        // chemin complet de exactement maxLength caractères : rendu tel quel
        assertEquals("dir/abc.txt", sanitizePath("dir/abc.txt", 11));
        // chemin trop long d'un caractère, mais le nom tient : on garde seulement le nom
        assertEquals("abc.txt", sanitizePath("dir/abc.txt", 10));
        // nom (sans extension) de exactement maxLength caractères : pas tronqué
        assertEquals("abcdefgh.txt", sanitizePath("abcdefgh.txt", 8));
        // nom plus long d'un caractère : tronqué (8 - 4 - 3 = 1 caractère gardé)
        assertEquals("a....txt", sanitizePath("abcdefghi.txt", 8));
    }

    @Test
    public void testEmbeddedFileNameMaxLengthBoundary() {
        // nom (sans extension) de exactement maxLength caractères : pas tronqué
        assertEquals("abcdefgh.txt", sanitizeName("abcdefgh.txt", 8));
        // nom plus long d'un caractère : tronqué
        assertEquals("a....txt", sanitizeName("abcdefghi.txt", 8));
    }

    private static Metadata metadataFrom(Property[] order, String[] values, int first) {
        Metadata metadata = new Metadata();
        for (int i = first; i < order.length; i++) {
            metadata.set(order[i], values[i]);
        }
        return metadata;
    }

    private static String sanitizePath(String path, int maxLength) {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.EMBEDDED_RESOURCE_PATH, path);
        return FilenameUtils.getSanitizedEmbeddedFilePath(metadata, ".bin", maxLength);
    }

    private static String sanitizeName(String name, int maxLength) {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, name);
        return FilenameUtils.getSanitizedEmbeddedFileName(metadata, ".bin", maxLength);
    }
}
