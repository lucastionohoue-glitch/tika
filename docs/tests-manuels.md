# Tests manuels pour les mutants survivants

Fichiers :
- `tika-core/src/test/java/org/apache/tika/io/FilenameUtilsManualTest.java` (4 tests)
- `tika-core/src/test/java/org/apache/tika/mime/MediaTypeManualTest.java` (3 tests)

On est partis des survivants listés dans [`mutants-run2.md`](mutants-run2.md) et on a pris les plus intéressants dans
les méthodes ciblées. Les 7 tests passent sur le code original. On n'a pas touché à `src/main`.

## 1. Les tests

### `FilenameUtilsManualTest.testEmbeddedPathPropertyPriority`

- **Intention** : vérifier l'ordre dans lequel `getEmbeddedPath` (privée, appelée par `getSanitizedEmbeddedFilePath`)
  lit les propriétés : `EMBEDDED_RESOURCE_PATH`, puis `INTERNAL_PATH`, `RESOURCE_NAME_KEY`, `EMBEDDED_RELATIONSHIP_ID`
  et enfin `ORIGINAL_RESOURCE_NAME`.
- **Données** : une valeur **différente** par propriété (`embedded.txt`, `internal.txt`, `resource.txt`,
  `relationship.txt`, `original.txt`). On fait 5 tours : au tour *k*, on ne remplit que les propriétés à partir du
  rang *k*. Les tests existants (`getMetadata` de `FilenameUtilsTest`, et le mock de l'IA) mettaient la même valeur
  partout, donc on ne pouvait pas savoir quelle propriété avait été lue.
- **Oracle** : le code de `getEmbeddedPath` (lignes 342–360). La première propriété non vide gagne, donc au tour *k*
  on doit obtenir la valeur du rang *k*. Les noms sont déjà propres, donc `getSanitizedEmbeddedFilePath` les rend tels
  quels.
- **Mutants visés** : 344, 348, 352, 356 (NegateConditionals) ; 349, 353, 357 (EmptyObjectReturnVals, sans couverture
  avant) ; 359 (EmptyObjectReturnVals).

### `FilenameUtilsManualTest.testEmbeddedNamePropertyPriority`

- **Intention** : la même chose pour `getEmbeddedName` (appelée par `getSanitizedEmbeddedFileName`), dont l'ordre est
  différent : `RESOURCE_NAME_KEY`, `INTERNAL_PATH`, `EMBEDDED_RELATIONSHIP_ID`, `EMBEDDED_RESOURCE_PATH`,
  `ORIGINAL_RESOURCE_NAME`.
- **Données** : le même principe, avec les valeurs rangées dans cet ordre-là.
- **Oracle** : le code de `getEmbeddedName` (lignes 363–381) et son commentaire (« this tries for resource name first,
  and then backs off to path »).
- **Mutants visés** : 365, 369, 373, 377 (NegateConditionals) ; 370, 374, 378 (EmptyObjectReturnVals) ; 380.

### `FilenameUtilsManualTest.testEmbeddedFilePathMaxLengthBoundaries`

- **Intention** : tester les deux comparaisons avec `maxLength` dans `getSanitizedEmbeddedFilePath`, **pile à la
  limite**, ainsi que la branche « chemin trop long mais nom assez court ».
- **Données** (extension `.txt`, 4 caractères) :

  | Entrée | `maxLength` | Attendu | Pourquoi ce choix |
  |---|---|---|---|
  | `dir/abc.txt` (11 caractères) | 11 | `dir/abc.txt` | chemin de **exactement** `maxLength` caractères : pas raccourci |
  | `dir/abc.txt` | 10 | `abc.txt` | 1 caractère de trop : on enlève le dossier, et le nom tient |
  | `abcdefgh.txt` (nom de 8) | 8 | `abcdefgh.txt` | nom de **exactement** `maxLength` caractères : pas tronqué |
  | `abcdefghi.txt` (nom de 9) | 8 | `a....txt` | 1 caractère de trop : tronqué à `8 - 4 - 3 = 1` caractère, plus `...` et `.txt` |

- **Oracle** : le code des lignes 263–270 (`retPath.length() > maxLength`, puis `namePart.length() > maxLength`, puis
  la formule de troncature). Remarque : `abcdefgh.txt` fait 12 caractères, plus que `maxLength`. La limite porte
  sur le nom **sans** l'extension. C'est le comportement du code, même si ce n'est peut-être pas ce qu'on attendrait.
- **Mutants visés** : 264, 265 (ConditionalsBoundary) ; 268 (EmptyObjectReturnVals, sans couverture avant).

### `FilenameUtilsManualTest.testEmbeddedFileNameMaxLengthBoundary`

- **Intention** : la même limite dans `getSanitizedEmbeddedFileName` (ligne 189).
- **Données** : `abcdefgh.txt` / `abcdefghi.txt` avec `maxLength = 8`, comme ci-dessus.
- **Oracle** : le code des lignes 188–192, qui utilise la même formule que `…FilePath`.
- **Mutants visés** : 189 (ConditionalsBoundary).

On a choisi `maxLength = 8` exprès, pour que `maxLength - extension.length() - 3` reste positif. Avec une valeur plus
petite, on retombe sur le bug déjà trouvé par l'IA (voir le README).

### `MediaTypeManualTest.testSetFromStringsSkipsUnparseable`

- **Intention** : tester `set(String...)`, qu'aucun test n'appelait (ChatUniTest n'a rien produit qui compile).
- **Données** : `"text/plain"`, `null`, `"pas-un-type"` (pas de `/`), `"text/html"` et encore `"text/plain"`. On a donc
  un `null`, une chaîne que `parse` refuse, et un doublon.
- **Oracle** : la Javadoc : « parses the given media type strings and returns an unmodifiable set that contains all
  the parsed types ». `parse` renvoie `null` pour `null` et pour une chaîne sans `/`, et le code ignore ces `null`.
  On attend donc exactement `{text/plain, text/html}`, et un ensemble **non modifiable** (`add` lève
  `UnsupportedOperationException`).
- **Mutants visés** : 230 (NegateConditionals) et 234 (EmptyObjectReturnVals), tous les deux sans couverture avant.
  Le test tue aussi 418 (`equals` renvoie toujours `true`) au passage : `equals` est appelé quand on construit et compare
  les ensembles.

### `MediaTypeManualTest.testParseCharsetFirst`

- **Intention** : tester la forme « charset en premier », que `parse` accepte pour les serveurs mal configurés
  (TIKA-350).
- **Données** : `"charset=UTF-8; text/html"`.
- **Oracle** : la Javadoc de `parse` (« we also handle "charset=xxx; type/subtype" for broken web servers ») et
  `CHARSET_FIRST_PATTERN`. Le résultat doit être le même que pour `"text/html; charset=UTF-8"`.
- **Mutants visés** : 278 (NullReturnVals, sans couverture avant).

### `MediaTypeManualTest.testParseCachesSimpleTypes`

- **Intention** : vérifier que `parse` met les types simples en cache (`SIMPLE_TYPES`) : deux appels avec la même
  chaîne doivent renvoyer **le même objet**.
- **Données** : `"application/x-" + UUID.randomUUID()`, une chaîne nouvelle à chaque exécution (voir la remarque plus
  bas).
- **Oracle** : la Javadoc de `SIMPLE_TYPES` (« used to optimize type lookup and to avoid having too many MediaType
  instances in memory ») et le code des lignes 252–267. Le premier appel met le type dans le cache, et le second
  le récupère.
- **Mutants visés** : 260 (Math, `slash + 1` devient `slash - 1`) et 293 (BooleanFalseReturnVals). Avec ces mutants, le
  type n'est jamais mis en cache et `parse` crée un nouvel objet à chaque appel. Le **résultat** reste `equals`, donc
  seul `assertSame` peut voir la différence.

**Remarque sur ce test.** Notre première version utilisait un nom fixe (`application/x-ift3913-manual`), et les deux
mutants survivaient quand même (premier essai sauvegardé dans [`pit/run3-essai1`](pit/run3-essai1)). PIT installe
chaque mutant en redéfinissant la classe dans une JVM qui tourne déjà, et les champs `static` ne sont pas
réinitialisés. Le cache contenait donc déjà notre type depuis une exécution précédente, et `assertSame` passait même
avec le mutant. Avec un nom aléatoire, le test ne dépend plus de l'état du cache.

## 2. PIT run 3

Rapport : [`pit/run3`](pit/run3) (tests originaux, tests générés corrigés et nos 7 tests manuels).

| Classe | Run | Tués | Survivants | Sans couverture | Couverture de lignes | Score de mutation | Test strength |
|---|---|---|---|---|---|---|---|
| `FilenameUtils` (115) | 1 | 73 | 20 | 22 | 88 % | 63 % | 78 % |
| | 2 | 73 | 20 | 22 | 88 % | 63 % | 78 % |
| | 3 | **93** | 7 | 15 | 91 % | **81 %** | **93 %** |
| `MediaType` (85) | 1 | 52 | 15 | 18 | 78 % | 61 % | 78 % |
| | 2 | 65 | 11 | 9 | 89 % | 76 % | 86 % |
| | 3 | **71** | 8 | 6 | 94 % | **84 %** | **90 %** |
| **Total** (200) | 1 | 125 | 35 | 40 | 83 % | 63 % | 78 % |
| | 2 | 138 | 31 | 31 | 88 % | 69 % | 82 % |
| | 3 | **164** | 15 | 21 | 92 % | **82 %** | **92 %** |

Nos 7 tests tuent **26 mutants de plus** : 20 dans `FilenameUtils` et 6 dans `MediaType`. Chacun des mutants visés plus
haut est tué, plus le 418 d'`equals` au passage.

On s'était trompés sur deux mutants : 359 et 380 (`getEmbeddedPath` / `getEmbeddedName` renvoient `""` au lieu de
`ORIGINAL_RESOURCE_NAME`). Au run 2, on les avait classés comme **équivalents**, en pensant que `null` et `""` sont
traités pareil par `isBlank`. Mais le mutant remplace **toujours** la valeur renvoyée par `""`, même quand
`ORIGINAL_RESOURCE_NAME` est rempli. Le dernier tour des tests de priorité (seule cette propriété remplie) les tue.
On a corrigé `mutants-run2.md`.

## 3. Ce qui reste (15 survivants, 21 sans couverture)

### Mutants équivalents (on ne peut pas les tuer)

| Méthode | Ligne | Mutateur | Pourquoi c'est équivalent |
|---|---|---|---|
| `getSanitizedEmbeddedFileName` | 156 | ConditionalsBoundary (`prefixLength > 0` devient `>= 0`) | `getPrefixLength` (le wrapper privé) ne renvoie jamais un nombre négatif : il renvoie la valeur de commons-io si elle est > 0, sinon 2 ou 0. Le mutant ne change donc quelque chose que pour `prefixLength == 0`, où il exécute `path.substring(0)`, qui renvoie la même chaîne. |
| `getSanitizedEmbeddedFilePath` | 215 | ConditionalsBoundary | Même raisonnement. |
| `getPrefixLength` | 320 | ConditionalsBoundary (`> 0` devient `>= 0`) | Le mutant renvoie 0 tout de suite quand commons-io renvoie 0. L'original continue vers le test de la ligne 323, qui ne peut être vrai que pour une chaîne de 2 caractères `X:`. Or commons-io renvoie déjà 2 pour `"C:"`, `"a:"`, etc. (vérifié dans `jshell`), donc l'original renvoie 0 lui aussi. Pour -1, les deux renvoient 0. |
| `getPrefixLength` | 323 | NegateConditionals (survivant) | Même raison : quand on arrive à la ligne 323, commons-io a déjà traité tous les cas `X:`. La condition est donc toujours fausse, et la négation d'une de ses parties ne change pas le résultat. |
| `getPrefixLength` | 323, 324 | 5 mutants sans couverture | Pas équivalents au sens strict, mais **impossibles à atteindre** : la branche `return 2` (ligne 324) est du code mort tant que commons-io reconnaît `"X:"`, et PIT ne voit jamais les dernières conditions de la ligne 323 évaluées. |

### Pas équivalent, mais on ne veut pas le tuer : `getSanitizedEmbeddedFileName` ligne 156 (négation)

En cherchant à justifier ce mutant, on a trouvé un **deuxième bug de Tika**. Pour `"~"` et `"~user"`, commons-io
renvoie un préfixe plus long que la chaîne elle-même (2 pour `"~"`, 6 pour `"~user"`, qu'il interprète comme `"~/"`
et `"~user/"`). Tika fait alors `path.substring(prefixLength)` sans vérifier la longueur :

```
getSanitizedEmbeddedFileName(resourceName = "~")     -> StringIndexOutOfBoundsException: begin 2, end 1, length 1
getSanitizedEmbeddedFilePath(resourceName = "~user") -> StringIndexOutOfBoundsException: begin 6, end 5, length 5
```

Le mutant (`prefixLength <= 0`) n'appelle pas ce `substring`, donc il ne plante pas. Pour le tuer, il faudrait un test
qui **attend** l'exception, autrement dit un test qui fige le bug. On préfère le signaler et ne pas écrire ce test.
Pour un chemin normal (`C:\a.txt`, `/a.txt`, `\\serveur\partage\a.txt`), le mutant donne le même résultat, parce que
`getName` jette le préfixe de toute façon.

### Tuables, mais on ne l'a pas fait (on s'est limités à 7 tests)

| Méthode | Ligne | État | Ce qu'il faudrait |
|---|---|---|---|
| `getSuffixFromPath` | 134 | survivant | une extension de exactement 5 lettres (`a.abcde`) |
| `getSanitizedEmbeddedFileName` | 174, 185 | sans couverture | un nom qui devient vide après nettoyage |
| `getSanitizedEmbeddedFilePath` | 232, 250, 259 | sans couverture | idem, pour les chemins |
| `resolveWithin` | 305, 308 | survivant / sans couverture | des liens symboliques réels (hors cible) |
| `calculateExtension`, `lookupExtension` | 402, 416 | sans couverture | hors cible |
| `MediaType` : `getBaseType`, `getType`, `getSubtype`, `hasParameters`, `compareTo` | 367–429 | survivants | hors cible : getters exécutés au passage, sans assert |
| `MediaType` : `application`, `audio`, `image`, `text`, `video` ; `equals` | 181–197, 420 | sans couverture | hors cible |
