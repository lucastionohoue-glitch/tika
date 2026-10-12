# Analyse par méthode – Run 1 (tests originaux)

Sources :
- Couverture : rapport JaCoCo `tika-core/target/site/jacoco/jacoco.xml` (compteurs `LINE` et `BRANCH` par méthode).
- Mutants : rapport PIT `docs/pit/run1` (PIT 1.30.0, mutateurs par défaut, tests `MediaTypeTest` et `FilenameUtilsTest`).

Chaque mutant PIT a été rattaché à sa méthode à l'aide de son numéro de ligne et de son champ `Location`.
Les totaux par classe correspondent à `docs/pit/run1/RESUME.md`.

> **Limite :** le rapport JaCoCo a été produit avec l'ensemble des tests de `tika-core`, alors que PIT
> n'exécute que `MediaTypeTest` et `FilenameUtilsTest`. Les pourcentages JaCoCo peuvent donc être plus
> élevés que la couverture réelle de ces deux classes de test. Par exemple, `application`, `image` et
> `text` sont couvertes à 100 % selon JaCoCo, mais leurs mutants sont `NO_COVERAGE` dans PIT.

## `org.apache.tika.mime.MediaType`

| Méthode | Ligne | % lignes | % branches | Mutants sans couverture | Mutants survivants |
|---|---|---|---|---|---|
| `<init>(String, String, Map)` | 98 | 100 % (31/31) | 100 % (8/8) | 0 | 0 |
| `<init>(String, String)` | 139 | 100 % (2/2) | — | 0 | 0 |
| `<init>(String, int)` | 142 | 100 % (10/10) | 50 % (4/8) | 0 | 0 |
| `<init>(MediaType, Map)` | 154 | 100 % (2/2) | — | 0 | 0 |
| `<init>(MediaType, String, String)` | 166 | 0 % (0/2) | — | 0 | 0 |
| `<init>(MediaType, Charset)` | 177 | 0 % (0/2) | — | 0 | 0 |
| `application` | 181 | 100 % (1/1) | — | 1 | 0 |
| `audio` | 185 | 0 % (0/1) | — | 1 | 0 |
| `image` | 189 | 100 % (1/1) | — | 1 | 0 |
| `text` | 193 | 100 % (1/1) | — | 1 | 0 |
| `video` | 197 | 0 % (0/1) | — | 1 | 0 |
| **`set(MediaType...)`** | 209 | 0 % (0/5) | 0 % (0/4) | 2 | 0 |
| **`set(String...)`** | 227 | 0 % (0/6) | 0 % (0/4) | 2 | 0 |
| **`parse`** | 247 | 75 % (18/24) | 72 % (13/18) | 2 | 1 |
| `isSimpleName` | 286 | 100 % (6/6) | 90 % (18/20) | 1 | 1 |
| `parseParameters` | 297 | 100 % (20/20) | 100 % (10/10) | 0 | 0 |
| `unquote` | 339 | 100 % (5/5) | 100 % (8/8) | 0 | 0 |
| **`union`** | 349 | 25 % (2/8) | 25 % (1/4) | 5 | 2 |
| `getBaseType` | 367 | 100 % (3/3) | 100 % (2/2) | 0 | 2 |
| `getType` | 379 | 100 % (1/1) | — | 0 | 1 |
| `getSubtype` | 387 | 100 % (1/1) | — | 0 | 2 |
| `hasParameters` | 398 | 100 % (1/1) | 100 % (2/2) | 0 | 2 |
| `getParameters` | 408 | 100 % (1/1) | — | 0 | 0 |
| `toString` | 412 | 100 % (1/1) | — | 0 | 0 |
| `equals` | 416 | 75 % (3/4) | 50 % (1/2) | 1 | 3 |
| `hashCode` | 425 | 100 % (1/1) | — | 0 | 0 |
| `compareTo` | 429 | 100 % (1/1) | — | 0 | 1 |
| **Total** | | | | **18** | **15** |

## `org.apache.tika.io.FilenameUtils`

| Méthode | Ligne | % lignes | % branches | Mutants sans couverture | Mutants survivants |
|---|---|---|---|---|---|
| `<init>` (privé) | 34 | 0 % (0/1) | — | 0 | 0 |
| `normalize` | 69 | 100 % (10/10) | 100 % (8/8) | 0 | 0 |
| `getName` | 108 | 100 % (9/9) | 88 % (7/8) | 0 | 0 |
| `getSuffixFromPath` | 131 | 100 % (7/7) | 100 % (6/6) | 0 | 1 |
| `getSanitizedEmbeddedFileName` | 145 | 94 % (31/33) | 83 % (15/18) | 2 | 3 |
| **`getSanitizedEmbeddedFilePath`** | 208 | 91 % (42/46) | 83 % (20/24) | 4 | 3 |
| `resolveWithin` | 296 | 57 % (8/14) | 38 % (3/8) | 2 | 1 |
| `getPrefixLength` (privé) | 319 | 83 % (5/6) | 30 % (3/10) | 6 | 2 |
| `removeProtocol` | 330 | 100 % (7/7) | 100 % (4/4) | 0 | 0 |
| **`getEmbeddedPath`** | 343 | 77 % (10/13) | 62 % (5/8) | 3 | 5 |
| **`getEmbeddedName`** | 364 | 77 % (10/13) | 62 % (5/8) | 3 | 5 |
| `calculateExtension` | 392 | 88 % (7/8) | 75 % (3/4) | 1 | 0 |
| `lookupExtension` | 407 | 71 % (5/7) | 50 % (1/2) | 1 | 0 |
| **Total** | | | | **22** | **20** |

## Méthodes ciblées

Les méthodes triviales (getters, `equals`, `hashCode`, `toString`, `compareTo`, fabriques d'une ligne) ont été écartées.

### MediaType

| Méthode | Mutants vivants | Justification |
|---|---|---|
| `union` | 7 (5 sans couverture + 2 survivants) | Seulement 25 % de couverture. Les cas « une map vide » et « deux maps non vides » ne sont pas testés (mutants `emptyMap` et `putAll` supprimé). |
| `parse` | 3 (2 + 1) | Une addition remplacée par une soustraction survit à la l. 260. La branche des l. 277–278 n'est jamais exécutée. |
| `set(MediaType...)` / `set(String...)` | 4 (4 + 0) | Aucune couverture par les tests originaux. Ces méthodes n'ont aucun survivant : elles feront monter le score, mais ne serviront pas à l'analyse des survivants. |

### FilenameUtils

| Méthode | Mutants vivants | Justification |
|---|---|---|
| `getEmbeddedName` | 8 (3 + 5) | Quatre conditions niées survivent (l. 365–377). Les `return` des branches ne sont pas couverts. |
| `getEmbeddedPath` | 8 (3 + 5) | Même structure que `getEmbeddedName` (l. 344–359). |
| `getSanitizedEmbeddedFilePath` | 7 (4 + 3) | Elle appelle `getPrefixLength` (privée, l. 319 : 8 mutants vivants, 30 % des branches). C'est elle qu'on cible à la place de cette méthode privée. Les survivants sont des « changed conditional boundary » (l. 215, 264, 265) et 4 `return ""` sont sans couverture. |
