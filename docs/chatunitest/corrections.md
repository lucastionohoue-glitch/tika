# Corrections des tests générés par ChatUniTest

Les sorties brutes de ChatUniTest sont dans `tika-core/chatunitest-tests/` et on ne les a pas modifiées. Les versions
corrigées sont dans `tika-core/src/test/java/org/apache/tika/{mime,io}/`.

## Avant les corrections

On a copié les 4 classes telles quelles dans `src/test/java`. Elles compilent toutes, mais **21 tests sur 39
échouent** (log : [`execution-tests-bruts.log`](execution-tests-bruts.log)). ChatUniTest avait pourtant marqué chaque
classe comme réussie.

| Fichier | Tests | Passent | Échouent |
|---|---|---|---|
| `MediaType_union_11_0_Test` | 1 | 1 | 0 |
| `MediaType_set_5_0_Test` | 4 | 3 | 1 |
| `MediaType_parse_7_0_Test` | 23 | 11 | 12 |
| `FilenameUtils_getSanitizedEmbeddedFilePath_4_0_Test` | 11 | 3 | 8 |

`getSanitizedEmbeddedFileName` n'a produit aucun test : les 2 tests × 3 tours ont tous échoué à l'exécution.

Remarque : au build, Spotless (configuré par `tika-parent` avec `spotless.goal=apply`) ajoute automatiquement l'en-tête
de licence Apache et enlève les imports inutilisés. On a vérifié avec un diff que le corps des classes n'avait pas
changé. Ce n'est donc pas une correction de notre part.

## Règle qu'on a suivie

On ne touche pas à `src/main`. Quand un oracle est faux, on le remplace par la valeur réellement renvoyée, mais
seulement après avoir vérifié dans le code que ce comportement est voulu. On garde les données et l'intention du test
chaque fois que c'est possible.

## Tableau des corrections

| # | Fichier | Problème | Correction |
|---|---|---|---|
| 1 | `FilenameUtils_getSanitizedEmbeddedFilePath_4_0_Test` (les 11 tests) | Le bouchon `when(metadata.get(anyString()))` vise `Metadata.get(String)`, alors que `getEmbeddedPath` appelle `Metadata.get(Property)`. Le mock renvoie donc toujours `null`, et la méthode aussi. | `anyString()` remplacé par `any(Property.class)`, avec l'import de `org.apache.tika.metadata.Property` |
| 2 | idem, `withPathContainingProtocol` | Oracle `"file.txt"`. `removeProtocol` enlève seulement `http://` et garde le reste du chemin. | `"example.com/file.txt"` |
| 3 | idem, `withPathContainingInvalidCharacters` | Oracle `"example_file.txt"`. `normalize` remplace `?` par son code hexadécimal (voir la javadoc). | `"example%3Ffile.txt"` |
| 4 | idem, `withPathExceedingMaxLength` | Avec `maxLength=5` et l'extension `.txt`, le code fait `substring(0, 5 - 4 - 3)` et lève `StringIndexOutOfBoundsException`. **C'est un bug de Tika, pas du test.** L'oracle `"example..."` était faux de toute façon : il manque l'extension. | `maxLength` passé à 10 (assez pour éviter l'indice négatif), oracle `"exa....txt"` (3 caractères + `...` + `.txt`) |
| 5 | idem, `withPathEndingWithMultipleSlashes` | Oracle `"examplefile.txt"`. La méthode renvoie un chemin, pas un nom : `//` devient `/`. | `"example/file.txt"` |
| 6 | idem, `withPathEndingWithDot` | Oracle `"example_file.txt"`. Un point au milieu du nom n'est pas modifié. | `"example.file.txt"` |
| 7 | idem, `withPathEndingWithDoubleDot` | Oracle `"example_file.txt"`. `\.{2,}` est remplacé par un seul `.`. | `"example.file.txt"` |
| 8 | idem, `withPathEndingWithColon` | Oracle `"example_file.txt"`. `:` est remplacé par `/`. | `"example/file.txt"` |
| 9 | idem, `withPathEndingWithBackslash` | Oracle `"examplefile.txt"`. `\` est remplacé par `/`. | `"example/file.txt"` |
| 10 | idem, `withPathEndingWithForwardSlash` | Le test prépare le mock mais n'appelle jamais la méthode et n'a **aucun oracle**, donc il passe toujours. | **Aucune correction** (à part le mock de la correction 1). On l'a laissé tel que l'IA l'a écrit : si on ajoutait l'appel et l'assertion, ce serait un test écrit par nous, et la comparaison IA / manuel ne tiendrait plus. On le compte comme un test sans oracle. |
| 11 | `MediaType_parse_7_0_Test` (12 tests : `*Charset`, `*Parameter`, `*Value`) | Le modèle suppose que `parse` renvoie `null` pour `charset=`, `name=`, `name=value=`, etc. En fait, `parseParameters` accepte une valeur vide ou contenant `=`, et `parse` renvoie un `MediaType`. | `assertNull(result)` remplacé par `assertNotNull(result)` + `assertEquals(<toString réel>, result.toString())`, par ex. `"text/plain; charset="` ou `"text/plain; name=\"value\\=\""` |
| 12 | `MediaType_set_5_0_Test`, `testSetMethodWithNull` | Le test appelle `setMethod.invoke(null, (Object[]) null)`, ce qui veut dire « aucun argument » pour la réflexion. `invoke` lève donc `IllegalArgumentException` sans jamais entrer dans `set`. | Appel direct `MediaType.set((MediaType[]) null)`. L'oracle `NullPointerException` du modèle est gardé, et il est juste. |

## Après les corrections

Sur les 12 cas du tableau, 11 sont des corrections : le cas 10 a été laissé tel quel. Ces 11 corrections changent
20 oracles (cas 2 à 9 et cas 11), 1 mise en place (cas 1, dans les 11 tests du fichier) et 1 appel (cas 12).
24 des 39 tests ont été modifiés : les 11 de `FilenameUtils` (le mock), 12 dans `parse` et 1 dans `set`. Les 15 autres
n'ont pas été touchés.

**20 oracles ne viennent plus de l'IA, mais de nous** : les 12 de `parse` et 8 de `FilenameUtils` (les corrections
2 à 9, dont `maxLength`, où on a aussi changé la donnée). Ces oracles contiennent la vraie valeur, qu'on a trouvée en
exécutant la méthode et en lisant le code. Pour la correction 12 (`set`), on a changé l'appel, mais l'oracle est
celui de l'IA.

Pour l'analyse PIT, on sépare donc les asserts en deux groupes :

| Origine de l'assert | Tests |
|---|---|
| **IA, d'origine** | `union` (1 test) ; `set` (4 tests) ; `parse` : les 11 tests qui passaient déjà (`NullString`, `SimpleType`, `ComplexType`, `InvalidType`, `EmptyString`, `WhitespaceString`, `NullParameters`, `EmptyParameters`, `WhitespaceParameters`, `NullTypeAndSubtype`, `EmptyTypeAndSubtype`) ; `FilenameUtils` : `withNullPath`, `withEmptyPath` |
| **Corrigé par nous** | `parse` : les 12 tests `*Charset`, `*Parameter`, `*Value` ; `FilenameUtils` : les 8 tests des corrections 2 à 9 |
| **Aucun oracle** | `FilenameUtils` : `withPathEndingWithForwardSlash` |

Si un mutant n'est tué que par un assert corrigé, le mérite nous revient, pas à l'IA. Le compter pour l'IA
surestimerait la qualité des tests générés.

```
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Ce qu'on retient

- Les tests générés **compilent** (après l'ajout de Mockito), mais plus de la moitié de leurs oracles sont faux.
  ChatUniTest les considère quand même réussis.
- Les erreurs viennent surtout de suppositions du modèle sur le comportement (« entrée bizarre, donc `null` »,
  « caractère spécial, donc `_` ») au lieu de la lecture du code. Ça ressemble aux règles de
  `getSanitizedEmbeddedFileName`, que le modèle a peut-être confondu avec `getSanitizedEmbeddedFilePath`.
- La correction 4 a quand même fait ressortir un vrai bug : `getSanitizedEmbeddedFilePath` plante si `maxLength`
  est plus petit que la longueur de l'extension + 3 (voir le README).
- Un test sur 39 n'a aucun oracle. Il passera toujours, et il ne peut tuer aucun mutant.
- `MediaType.set` est publique, mais le modèle l'appelle par réflexion avec `setAccessible(true)`. Pour `union`, qui
  est privée, la réflexion est justifiée.
