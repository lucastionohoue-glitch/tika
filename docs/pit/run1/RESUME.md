# PIT – Run 1 (tests originaux)

PIT 1.30.0 + pitest-junit5-plugin 1.2.3, mutateurs par défaut, JDK 17.
Tests : `MediaTypeTest`, `FilenameUtilsTest`. Rapport complet : `index.html`.

| Classe | Couverture de lignes | Mutants générés | Tués | Survivants | Sans couverture | Score de mutation | Test strength |
|---|---|---|---|---|---|---|---|
| `org.apache.tika.io.FilenameUtils` | 88 % (161/184) | 115 | 73 | 20 | 22 | 63 % (73/115) | 78 % (73/93) |
| `org.apache.tika.mime.MediaType` | 78 % (123/157) | 85 | 52 | 15 | 18 | 61 % (52/85) | 78 % (52/67) |
| **Total** | **83 % (284/341)** | **200** | **125** | **35** | **40** | **63 % (125/200)** | **78 % (125/160)** |

- Score de mutation = tués / générés.
- Test strength = tués / mutants couverts (générés − sans couverture).
