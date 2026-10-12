# PIT – Run 2 (tests originaux + tests générés corrigés)

PIT 1.30.0 + pitest-junit5-plugin 1.2.3, mutateurs par défaut, JDK 17, `fullMutationMatrix=true` (sorties XML et
HTML). Tests : `MediaTypeTest`, `FilenameUtilsTest`, `MediaType_union_11_0_Test`, `MediaType_parse_7_0_Test`,
`MediaType_set_5_0_Test`, `FilenameUtils_getSanitizedEmbeddedFilePath_4_0_Test`.
Rapport complet : `index.html`. Matrice complète : `mutations.xml`. Analyse : [`docs/mutants-run2.md`](../../mutants-run2.md).

| Classe | Couverture de lignes | Mutants générés | Tués | Survivants | Sans couverture | Score de mutation | Test strength |
|---|---|---|---|---|---|---|---|
| `org.apache.tika.io.FilenameUtils` | 88 % (161/184) | 115 | 73 | 20 | 22 | 63 % (73/115) | 78 % (73/93) |
| `org.apache.tika.mime.MediaType` | 89 % (139/157) | 85 | 65 | 11 | 9 | 76 % (65/85) | 86 % (65/76) |
| **Total** | **88 % (300/341)** | **200** | **138** | **31** | **31** | **69 % (138/200)** | **82 % (138/169)** |
