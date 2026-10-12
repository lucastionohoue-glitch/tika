# Run 1 : couverture et mutants par méthode (tests originaux)

Pour la couverture, on a pris les compteurs `LINE` et `BRANCH` de chaque méthode dans le rapport JaCoCo
(`tika-core/target/site/jacoco/jacoco.xml`). Pour les mutants, on a utilisé le rapport PIT dans `docs/pit/run1`
(PIT 1.30.0, mutateurs par défaut, avec seulement `MediaTypeTest` et `FilenameUtilsTest`). On a associé chaque
mutant à sa méthode avec son numéro de ligne et son champ `Location`. Les totaux par classe sont les mêmes que
dans `docs/pit/run1/RESUME.md`.

Attention, les deux outils n'ont pas roulé les mêmes tests. JaCoCo a été lancé avec tous les tests de `tika-core`,
alors que PIT n'exécute que nos deux classes de test. Certains pourcentages JaCoCo sont donc plus hauts que la
couverture réelle de `MediaTypeTest` et `FilenameUtilsTest`. Par exemple, JaCoCo donne 100 % pour `application`,
`image` et `text`, mais PIT marque leurs mutants `NO_COVERAGE`.

Les méthodes en gras sont celles qu'on a choisies.

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
| **`getSanitizedEmbeddedFileName`** | 145 | 94 % (31/33) | 83 % (15/18) | 2 | 3 |
| **`getSanitizedEmbeddedFilePath`** | 208 | 91 % (42/46) | 83 % (20/24) | 4 | 3 |
| `resolveWithin` | 296 | 57 % (8/14) | 38 % (3/8) | 2 | 1 |
| `getPrefixLength` (privé) | 319 | 83 % (5/6) | 30 % (3/10) | 6 | 2 |
| `removeProtocol` | 330 | 100 % (7/7) | 100 % (4/4) | 0 | 0 |
| `getEmbeddedPath` (privé) | 343 | 77 % (10/13) | 62 % (5/8) | 3 | 5 |
| `getEmbeddedName` (privé) | 364 | 77 % (10/13) | 62 % (5/8) | 3 | 5 |
| `calculateExtension` | 392 | 88 % (7/8) | 75 % (3/4) | 1 | 0 |
| `lookupExtension` | 407 | 71 % (5/7) | 50 % (1/2) | 1 | 0 |
| **Total** | | | | **22** | **20** |

## Pourquoi ces méthodes

On a laissé de côté les méthodes trop simples pour être intéressantes : les getters, `equals`, `hashCode`,
`toString`, `compareTo` et les fabriques d'une seule ligne comme `text()` ou `image()`.

### MediaType

| Méthode | Mutants vivants (sans couverture + survivants) | Pourquoi |
|---|---|---|
| `union` | 7 (5 + 2) | Seulement 25 % des lignes sont couvertes. Aucun test ne passe une map vide, ni deux maps non vides. C'est pour ça que les mutants `emptyMap` et « `putAll` supprimé » restent en vie. |
| `parse` | 3 (2 + 1) | À la ligne 260, PIT remplace une addition par une soustraction et aucun test ne le voit. La branche des lignes 277–278 n'est jamais exécutée. |
| `set(MediaType...)` et `set(String...)` | 4 (4 + 0) | Les tests originaux ne les appellent jamais. Elles n'ont aucun survivant, donc elles vont surtout faire monter le score. Elles ne nous apprendront pas grand-chose sur les survivants. |

### FilenameUtils

Au départ, on voulait cibler `getEmbeddedName`, `getEmbeddedPath` et `getPrefixLength`. En regardant le code, on
s'est rendu compte qu'elles sont toutes les trois privées, et ChatUniTest ne génère pas de tests pour une méthode
privée. On a donc pris les deux méthodes publiques qui les appellent. Les tests de ces méthodes passent forcément
par le code privé, donc ils devraient aussi pouvoir tuer ses mutants.

| Méthode | Appelle | Mutants vivants (sans couverture + survivants) | Pourquoi |
|---|---|---|---|
| `getSanitizedEmbeddedFileName` | `getEmbeddedName` | 5 (2 + 3), et 8 (3 + 5) dans `getEmbeddedName` | `getEmbeddedName` cherche un nom dans 5 champs des métadonnées, dans un ordre précis. Aucun test ne vérifie cet ordre : quatre conditions niées survivent (lignes 365–377) et plusieurs `return` ne sont jamais atteints. |
| `getSanitizedEmbeddedFilePath` | `getEmbeddedPath`, `getPrefixLength` | 7 (4 + 3), et 8 (3 + 5) dans `getEmbeddedPath`, 8 (6 + 2) dans `getPrefixLength` | `getEmbeddedPath` fonctionne comme `getEmbeddedName` (lignes 344–359) et a le même problème. `getPrefixLength` n'a que 30 % de ses branches couvertes. Dans la méthode elle-même, trois « changed conditional boundary » survivent (lignes 215, 264 et 265) et quatre `return ""` ne sont pas couverts. |
