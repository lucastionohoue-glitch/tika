# Analyse des mutants : run 1 vs run 2

- Run 1 : tests originaux (`MediaTypeTest`, `FilenameUtilsTest`), rapport dans [`pit/run1`](pit/run1).
- Run 2 : les mêmes, plus les 4 classes générées par ChatUniTest (après corrections), rapport dans [`pit/run2`](pit/run2).

Le run 2 a été lancé avec `fullMutationMatrix=true` : pour chaque mutant, `mutations.xml` donne **tous** les tests qui
le tuent, pas seulement le premier. Le run 1 n'avait pas de sortie XML. On a donc retrouvé son résultat à partir de la
matrice du run 2 : un mutant était tué au run 1 si au moins un test de `MediaTypeTest` ou de `FilenameUtilsTest` le
tue. Ce calcul redonne exactement les chiffres du run 1 (73/20/22 et 52/15/18), ce qui confirme qu'il est juste.

Pour chaque test généré, on a noté l'origine de son assert (voir [`chatunitest/corrections.md`](chatunitest/corrections.md)) :
**IA d'origine** (l'assert est celui que le modèle a écrit), **corrigé** (on a remplacé l'oracle par la vraie
valeur), ou **sans oracle**.

## 1. Scores

| Classe | Run | Tués | Survivants | Sans couverture | Score de mutation | Test strength |
|---|---|---|---|---|---|---|
| `FilenameUtils` (115 mutants) | 1 | 73 | 20 | 22 | 63 % | 78 % |
| | 2 | 73 | 20 | 22 | 63 % | 78 % |
| `MediaType` (85 mutants) | 1 | 52 | 15 | 18 | 61 % | 78 % |
| | 2 | **65** | 11 | 9 | **76 %** | **86 %** |
| **Total** (200 mutants) | 1 | 125 | 35 | 40 | 63 % | 78 % |
| | 2 | **138** | 31 | 31 | **69 %** | **82 %** |

Couverture de lignes : 83 % (284/341) au run 1, puis 88 % (300/341) au run 2. Tout le gain vient de `MediaType`.

## 2. Les 13 mutants tués au run 2 mais pas au run 1

Les 13 sont dans `MediaType`, et **tous sont tués par un assert d'origine de l'IA**. Aucun n'a besoin de nos
corrections.

| # | Méthode | Ligne | Mutateur | Run 1 | Tests qui le tuent | Pourquoi | Catégorie |
|---|---|---|---|---|---|---|---|
| 1 | `set(MediaType...)` | 211 | NegateConditionals (`type != null` devient `type == null`) | sans couverture | `testSetMethod`, `testSetMethodWithDuplicateValues` | Le mutant n'ajoute que les `null` : l'ensemble renvoyé est vide, et `assertEquals(mediaTypeSet, result)` / `assertEquals(1, result.size())` échouent. | tué par un assert IA d'origine |
| 2 | `set(MediaType...)` | 215 | EmptyObjectReturnVals (renvoie `Collections.emptySet()`) | sans couverture | `testSetMethod`, `testSetMethodWithDuplicateValues` | Même raison : l'ensemble est vide au lieu de contenir 2 éléments (ou 1 pour les doublons). | tué par un assert IA d'origine |
| 3 | `parse` | 277 | NegateConditionals (`if (matcher.matches())` sur `CHARSET_FIRST_PATTERN`) | sans couverture | `testParseNullTypeAndSubtype` | Pour `"/"`, aucun des deux motifs ne correspond. Le mutant entre quand même dans le `if` et appelle `matcher.group(2)` sur un matcher sans correspondance, ce qui lève `IllegalStateException`. **Le mutant est tué par l'exception**, l'`assertNull` n'est même pas atteint. | tué par un test IA d'origine (exception) |
| 4 | `isSimpleName` | 293 | BooleanTrueReturnVals (renvoie toujours `true`) | sans couverture | `testParseNullTypeAndSubtype` | Avec le mutant, `""` passe pour un nom valide, donc `parse("/")` construit un `MediaType` au lieu de renvoyer `null`. `assertNull(result)` échoue. | tué par un assert IA d'origine |
| 5 | `union` | 349 | NegateConditionals (`a.isEmpty()`) | survivant | `testUnion` | `union(map1, map2)` renvoie `map2` seul : `assertEquals(2, result.size())` échoue. | tué par un assert IA d'origine |
| 6 | `union` | 350 | EmptyObjectReturnVals (`return b` devient `emptyMap`) | survivant | `testUnion` | `union(emptyMap, map1)` renvoie une map vide : `assertEquals(1, result.size())` échoue. | tué par un assert IA d'origine |
| 7 | `union` | 351 | NegateConditionals (`b.isEmpty()`) | sans couverture | `testUnion` | `union(map1, map2)` renvoie `map1` seul : la taille vaut 1 au lieu de 2. | tué par un assert IA d'origine |
| 8 | `union` | 352 | EmptyObjectReturnVals (`return a` devient `emptyMap`) | sans couverture | `testUnion` | `union(map1, emptyMap)` renvoie une map vide. | tué par un assert IA d'origine |
| 9 | `union` | 355 | VoidMethodCall (suppression de `union.putAll(a)`) | sans couverture | `testUnion` | `key1` manque : taille 1 au lieu de 2, et `result.get("key1")` vaut `null`. | tué par un assert IA d'origine |
| 10 | `union` | 356 | VoidMethodCall (suppression de `union.putAll(b)`) | sans couverture | `testUnion` | `key2` manque. | tué par un assert IA d'origine |
| 11 | `union` | 357 | EmptyObjectReturnVals (renvoie `emptyMap`) | sans couverture | `testUnion` | La map fusionnée est vide. | tué par un assert IA d'origine |
| 12 | `equals` | 416 | NegateConditionals (`object instanceof MediaType`) | survivant | `testSetMethodWithDuplicateValues` | Avec le mutant, deux `application/json` égaux ne sont plus `equals`, et le `HashSet` garde les deux : `assertEquals(1, result.size())` échoue. | tué par un assert IA d'origine |
| 13 | `equals` | 418 | BooleanFalseReturnVals (renvoie toujours `false`) | survivant | `testSetMethodWithDuplicateValues` | Même raison : le doublon n'est plus détecté. | tué par un assert IA d'origine |

`equals` n'est pas une méthode qu'on visait. Ces deux mutants sont tués « au passage », parce que le test des doublons
de `set` dépend de `equals`.

### Ce qu'apportent vraiment nos oracles corrigés

Si on regarde **tous** les mutants que les tests générés tuent (et pas seulement les nouveaux), la séparation par
origine change beaucoup les conclusions :

| Classe | Tués par au moins 1 test généré | … par un assert IA d'origine | … seulement par un assert corrigé (parmi les générés) | Tués **seulement** par des tests générés |
|---|---|---|---|---|
| `FilenameUtils` | 41 | 2 | 39 | 0 |
| `MediaType` | 60 | 55 | 5 | 13 |

- Pour `FilenameUtils`, les tests générés tuent 41 mutants, mais **39 de ces morts viennent de nos oracles
  corrigés**. Les deux seuls asserts d'origine utiles (`withNullPath`, `withEmptyPath`) ne vérifient que le cas
  `null`. De toute façon, ces 41 mutants étaient tous déjà tués par `FilenameUtilsTest`. Le gain est donc nul. Si on
  attribuait ces 39 morts à l'IA, on surestimerait beaucoup la qualité des tests générés.
- Pour `MediaType`, c'est l'inverse : les asserts d'origine font presque tout le travail, et les 13 nouveaux mutants
  tués le sont tous par des asserts IA. Les tests qui marchent sont ceux qui vérifient des choses simples et
  certaines (taille d'une map, contenu d'un ensemble, `null` pour `"/"`). Les 12 oracles de `parse` qu'on a corrigés
  ne tuent aucun nouveau mutant.
- Le test sans oracle (`withPathEndingWithForwardSlash`) ne tue rien, comme prévu.

## 3. Mutants pas tués au run 2

On distingue trois raisons :
- **jamais atteint** : aucun test n'exécute la ligne (NO_COVERAGE) ;
- **oracle / données trop faibles** : la ligne est exécutée, mais aucun test ne vérifie le comportement qui change ;
- **équivalent** : le mutant ne change pas le résultat observable.

### `MediaType` (11 survivants, 9 sans couverture)

| Méthode | Ligne | Mutateur | État | Raison |
|---|---|---|---|---|
| `application`, `audio`, `image`, `text`, `video` | 181–197 | NullReturnVals | sans couverture | Jamais atteint. Fabriques d'une ligne, hors des méthodes ciblées. |
| `set(String...)` | 230 | NegateConditionals | sans couverture | Jamais atteint : ChatUniTest n'a produit aucun test qui compile pour cette surcharge. |
| `set(String...)` | 234 | EmptyObjectReturnVals | sans couverture | Idem. |
| `parse` | 260 | Math (`slash + 1` devient `slash - 1`) | survivant | Le sous-type calculé contient alors `/`, donc `isSimpleName` renvoie `false`, et la chaîne passe par l'expression régulière, qui donne le même `MediaType`. La **valeur** est identique (même `equals`, même `toString`). Seul l'**objet** change : le résultat n'est plus mis dans le cache `SIMPLE_TYPES`. Presque équivalent : on peut le tuer avec `assertSame(parse(x), parse(x))` sur un type pas encore en cache. |
| `parse` | 278 | NullReturnVals | sans couverture | Jamais atteint : aucun test (ni manuel, ni généré) ne donne la forme « charset en premier » (`charset=utf-8; text/plain`, TIKA-350). |
| `isSimpleName` | 293 | BooleanFalseReturnVals | survivant | Même effet que la ligne 260 : tout passe par l'expression régulière, et le résultat est égal. Seule l'identité de l'objet (le cache) change. |
| `getBaseType` | 367, 370 | NegateConditionals, NullReturnVals | survivant | Oracle trop faible : seulement exécuté au passage par `FilenameUtilsTest.testGetName`, qui ne vérifie pas le résultat. Hors cible. |
| `getType` | 379 | EmptyObjectReturnVals | survivant | Idem. |
| `getSubtype` | 387 | Math, EmptyObjectReturnVals | survivant | Idem. |
| `hasParameters` | 398 | NegateConditionals, BooleanFalseReturnVals | survivant | Idem. |
| `equals` | 418 | BooleanTrueReturnVals (renvoie toujours `true`) | survivant | Données trop faibles : `testSetMethod` met deux types différents dans un `HashSet`, mais leurs `hashCode` sont différents, donc `equals` n'est jamais appelé entre eux. Il faudrait comparer deux `MediaType` différents (`assertNotEquals`). |
| `equals` | 420 | BooleanTrueReturnVals | sans couverture | Jamais atteint : aucun test ne compare un `MediaType` à un objet d'un autre type. |
| `compareTo` | 429 | PrimitiveReturns | survivant | Oracle trop faible, hors cible (même cas que `getType`). |

### `FilenameUtils` (20 survivants, 22 sans couverture)

Exactement les mêmes qu'au run 1 : le test généré pour `getSanitizedEmbeddedFilePath` n'en tue aucun de plus, et
`getSanitizedEmbeddedFileName` n'a aucun test généré.

| Méthode | Ligne | Mutateur | État | Raison |
|---|---|---|---|---|
| `getSuffixFromPath` | 134 | ConditionalsBoundary (`< 6` devient `<= 6`) | survivant | Données trop faibles : aucun test n'a d'extension de exactement 5 caractères après le point (`.abcde`). |
| `getSanitizedEmbeddedFileName` | 156 | ConditionalsBoundary (`> 0` devient `>= 0`) | survivant | Équivalent : quand `prefixLength == 0`, `substring(0)` ne change rien. |
| `getSanitizedEmbeddedFileName` | 156 | NegateConditionals | survivant | Probablement équivalent : si on ne retire pas le préfixe (`C:\`, `/`, `\\serveur\`), il est quand même jeté ensuite, parce que `getName` ne garde que ce qui suit le dernier séparateur. |
| `getSanitizedEmbeddedFileName` | 174, 185 | EmptyObjectReturnVals | sans couverture | Jamais atteint : aucun test n'a un nom qui devient vide après nettoyage. Pas de test généré pour cette méthode. |
| `getSanitizedEmbeddedFileName` | 189 | ConditionalsBoundary (`> maxLength` devient `>= maxLength`) | survivant | Données trop faibles : aucun test n'a un nom de exactement `maxLength` caractères. |
| `getSanitizedEmbeddedFilePath` | 215 | ConditionalsBoundary (`> 0` devient `>= 0`) | survivant | Équivalent (`substring(0)`). |
| `getSanitizedEmbeddedFilePath` | 232, 250, 259 | EmptyObjectReturnVals | sans couverture | Jamais atteint : cas où le nom devient vide après nettoyage. |
| `getSanitizedEmbeddedFilePath` | 264, 265 | ConditionalsBoundary | survivant | Données trop faibles : aucun chemin ni nom de exactement `maxLength` caractères. Le test généré `withPathExceedingMaxLength` (après correction : nom de 11 caractères, `maxLength = 10`) dépasse la limite d'un caractère, pas zéro. |
| `getSanitizedEmbeddedFilePath` | 268 | EmptyObjectReturnVals | sans couverture | Jamais atteint : il faut un chemin plus long que `maxLength` mais un nom plus court (`dossier/a.txt` avec `maxLength = 8`). |
| `resolveWithin` | 305, 308 | NegateConditionals | survivant / sans couverture | Hors cible (vérification des liens symboliques, seulement si les fichiers existent). |
| `getPrefixLength` | 320 | ConditionalsBoundary | survivant | Équivalent : quand commons-io renvoie 0, l'original continue et finit par renvoyer 0 lui aussi. Le cas `"X:"` est déjà reconnu par commons-io. |
| `getPrefixLength` | 323, 324 | NegateConditionals, ConditionalsBoundary, PrimitiveReturns | survivant / sans couverture | Branche presque morte : commons-io renvoie déjà 2 pour `"C:"`, donc ce test ne sert que si commons-io renvoie 0 ou -1 pour une chaîne de 2 caractères qui finit par `:`. Le survivant (négation) est probablement équivalent. |
| `getEmbeddedPath` | 344, 348, 352, 356 | NegateConditionals | survivant | **Données trop faibles.** La méthode essaie 5 propriétés dans un ordre de priorité. Le `getMetadata` de `FilenameUtilsTest` met le même nom dans `RESOURCE_NAME_KEY` et `EMBEDDED_RESOURCE_PATH`, et le mock de l'IA renvoie la même valeur pour toutes les propriétés. Si on inverse une condition, on obtient la même chaîne par une autre propriété. L'ordre de priorité n'est jamais vérifié. |
| `getEmbeddedPath` | 349, 353, 357 | EmptyObjectReturnVals | sans couverture | Jamais atteint : `EMBEDDED_RESOURCE_PATH` est toujours rempli, donc on ne passe jamais aux propriétés suivantes. |
| `getEmbeddedPath` | 359 | EmptyObjectReturnVals (`null` devient `""`) | survivant | Équivalent : l'appelant teste `StringUtils.isBlank(path)`, qui traite `null` et `""` de la même façon. |
| `getEmbeddedName` | 365, 369, 373, 377 | NegateConditionals | survivant | Même problème de données que pour `getEmbeddedPath`. |
| `getEmbeddedName` | 370, 374, 378 | EmptyObjectReturnVals | sans couverture | Jamais atteint (même raison). |
| `getEmbeddedName` | 380 | EmptyObjectReturnVals | survivant | Équivalent (`isBlank`). |
| `calculateExtension` | 402 | EmptyObjectReturnVals | sans couverture | Hors cible. |
| `lookupExtension` | 416 | EmptyObjectReturnVals | sans couverture | Hors cible. |

### Pistes pour les tests manuels (étape suivante)

Mutants qu'on peut tuer dans les méthodes ciblées :

- `getEmbeddedPath` / `getEmbeddedName` (344–357, 365–378) : un `Metadata` avec des valeurs **différentes** dans
  chaque propriété, pour vérifier l'ordre de priorité.
- `getSanitizedEmbeddedFilePath` 264, 265, 268 et `getSanitizedEmbeddedFileName` 189 : des longueurs **exactement**
  égales à `maxLength`, plus un chemin long avec un nom court.
- `set(String...)` 230, 234 : un test simple, avec un `null` et une chaîne invalide dans la liste.
- `parse` 278 : la forme « charset en premier ».
- `parse` 260 / `isSimpleName` 293 : `assertSame` sur deux `parse` du même type simple, pour vérifier le cache.
- `equals` 418 : deux `MediaType` différents.

On laisse de côté les mutants équivalents (156, 215, 320, 359, 380) et les méthodes hors cible.
