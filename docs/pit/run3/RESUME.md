# PIT – Run 3 (tests originaux + tests générés corrigés + tests manuels)

Même configuration que le run 2, avec en plus `MediaTypeManualTest` et `FilenameUtilsManualTest` dans `targetTests`.
Rapport complet : `index.html`. Matrice : `mutations.xml`. Analyse : [`docs/tests-manuels.md`](../../tests-manuels.md).

| Classe | Couverture de lignes | Mutants générés | Tués | Survivants | Sans couverture | Score de mutation | Test strength |
|---|---|---|---|---|---|---|---|
| `org.apache.tika.io.FilenameUtils` | 91 % (168/184) | 115 | 93 | 7 | 15 | 81 % (93/115) | 93 % (93/100) |
| `org.apache.tika.mime.MediaType` | 94 % (147/157) | 85 | 71 | 8 | 6 | 84 % (71/85) | 90 % (71/79) |
| **Total** | **92 % (315/341)** | **200** | **164** | **15** | **21** | **82 % (164/200)** | **92 % (164/179)** |

Le premier essai de ce run (162 tués) est dans `../run3-essai1`. Le test du cache de `parse` utilisait un nom de type
fixe, et ne tuait pas les mutants 260 et 293 à cause de l'état statique gardé par PIT entre les mutants.
