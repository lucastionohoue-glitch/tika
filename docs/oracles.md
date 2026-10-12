# Comparaison des oracles : IA vs tests écrits à la main

On compare les asserts des tests **bruts** générés par ChatUniTest (avant nos corrections,
`tika-core/chatunitest-tests/`) avec ceux des tests écrits par les développeurs de Tika (`MediaTypeTest`,
`FilenameUtilsTest`).

## 1. Les chiffres

| Méthode ciblée | Asserts IA | Dont faux | Asserts manuels (tests concernés) |
|---|---|---|---|
| `union` (privée) | 8 (7 `assertEquals`, 1 `assertTrue`) | 0 | 0 (jamais testée directement, seulement via le constructeur) |
| `set(MediaType...)` | 4 (2 `assertEquals`, 1 `assertTrue`, 1 `assertThrows`) | 0 (mais l'appel du `assertThrows` était faux) | 0 |
| `set(String...)` | 0 (rien n'a compilé) | – | 0 |
| `parse` | 28 (5 `assertEquals`, 5 `assertNotNull`, 18 `assertNull`) | **12** (tous des `assertNull`) | 24 dans 5 tests (`testParseWithParams`, `…QuotedCharset`, `…NoParams`, `…NoParamsWithSemi`, `testOddParameters`) |
| `getSanitizedEmbeddedFilePath` | 10 (8 `assertEquals`, 2 `assertNull`) + 1 test sans assert | **8** (tous les `assertEquals`) | 34 dans `testEmbeddedFilePaths` |
| `getSanitizedEmbeddedFileName` | 0 (aucun test produit) | – | 38 dans `testEmbeddedFileNames` |
| **Total** | **50** | **20 (40 %)** | |

Côté manuel, aucun assert n'est faux : les tests passent, et ils décrivent le comportement voulu par les développeurs.

## 2. Différences qualitatives

### D'où vient l'oracle

- **Manuel** : l'oracle vient de la connaissance du comportement voulu, et souvent d'un bug réel. Les tests citent
  leur source : `TIKA-121` (paramètres), `TIKA-349` (`;;` dans les paramètres), RFC 2045 §5.1 (charset entre
  guillemets). Dans `FilenameUtilsTest`, chaque assert correspond à une règle précise de nettoyage (`..` devient `_`
  au début du nom, `\` devient `/`, protocole retiré, etc.).
- **IA** : l'oracle est deviné à partir du nom de la méthode et de la forme de l'entrée. Le modèle n'a pas lu le corps
  de la méthode, ou ne l'a pas suivi. Deux suppositions reviennent tout le temps :
  - « entrée bizarre, donc `null` » : 12 `assertNull` faux dans `parse` (`"text/plain; charset="`,
    `"…; name=value=…"`). En réalité, `parseParameters` accepte tout ça.
  - « caractère spécial, donc `_` » : dans `getSanitizedEmbeddedFilePath`, le modèle attend `example_file.txt` pour
    `:`, `?`, `.` ou `..`. Ça ressemble aux règles de `getSanitizedEmbeddedFileName` (`..` devient `_`), mais
    `…FilePath` garde un **chemin** : `:` devient `/`, `?` devient `%3F`, `..` devient `.`. Les 8 `assertEquals` de
    ce test sont tous faux.

### Force des asserts

- **IA** : sur les 28 asserts de `parse`, 23 sont des `assertNull` / `assertNotNull`. Ils ne vérifient pas **ce que**
  renvoie la méthode, seulement qu'elle renvoie quelque chose. Les 5 `assertEquals` comparent `toString()`, ce qui est
  fort (type, sous-type et paramètres en même temps).
- **Manuel** : les tests de `parse` vérifient le contenu de `getParameters()` (nombre de clés, valeurs une par une,
  map exacte avec `singletonMap`). Certains asserts manuels sont faibles eux aussi, par exemple
  `assertNotNull(type.getParameters().keySet())`, qui ne peut jamais échouer, et `testParseWithParams`, qui vérifie
  les clés mais pas les valeurs.
- **IA**, cas `union` : c'est le meilleur test généré. Il appelle la méthode privée directement et vérifie les
  4 branches (a vide, b vide, les deux remplies, les deux vides), avec la taille **et** le contenu de la map. Les tests
  manuels ne passent jamais dans la branche où il faut fusionner deux maps non vides (mutants 351–357 sans couverture
  au run 1).

### Choix des données

- **IA** :
  - beaucoup de doublons : `testParseNullCharset` et `testParseEmptyCharset` ont exactement la même entrée,
    `testParseNullParameters` et `testParseEmptyParameters` aussi, et `testParseEmptyString` et
    `testParseEmptyTypeAndSubtype` testent tous les deux `""`. Le nom du test promet une chose (« Null ») que la donnée
    ne teste pas ;
  - par contre, l'IA essaie des valeurs limites que les humains n'ont pas testées : `parse("/")` (tue 2 mutants),
    `set` avec des doublons (tue 2 mutants d'`equals`), et surtout **`maxLength = 5`, qui a révélé un vrai bug**
    (`StringIndexOutOfBoundsException`, voir le README).
- **Manuel** : des données réalistes et variées (chemins Windows, `~/`, `file:///`, guillemets, `\u0000`, noms de
  80 caractères), mais toujours `maxLength = 50`, et `getMetadata` met le même nom dans deux propriétés. Ces deux
  choix laissent passer des mutants (voir [`mutants-run2.md`](mutants-run2.md)).

### Mise en place

- **IA** : elle bouchonne `Metadata` avec Mockito, alors que c'est une simple classe de données, et elle se trompe de
  surcharge (`get(String)` au lieu de `get(Property)`). Un mock qui renvoie la même valeur pour **toutes** les
  propriétés rend aussi impossible de tester l'ordre de priorité entre elles.
- **IA** : elle utilise la réflexion même pour `set`, qui est publique, et un appel par réflexion mal fait a donné une
  exception différente de celle attendue.
- **Manuel** : un vrai `Metadata`, construit par une petite méthode utilitaire (`getMetadata`).

### Tests sans oracle

- **IA** : `withPathEndingWithForwardSlash` prépare le mock et s'arrête là, sans appel ni assert. Il passe toujours.
- **Manuel** : aucun.

## 3. Ce que ça donne au mutation testing

Résumé de [`mutants-run2.md`](mutants-run2.md) :

- Les oracles d'origine de l'IA tuent **13 nouveaux mutants** dans `MediaType` (`union`, `set`, `parse("/")`). Ce
  sont tous des asserts simples, sur une taille ou une valeur `null`.
- Pour `FilenameUtils`, les tests générés ne tuent rien de nouveau. Sur les 41 mutants qu'ils tuent, 39 ne meurent
  que grâce aux oracles **qu'on a corrigés**. Les asserts d'origine de l'IA n'en tuent que 2.

## Conclusion

Les oracles manuels sont plus justes et plus « expliqués » : chaque assert correspond à une règle ou à un bug connu.
Ceux de l'IA sont faux dans 40 % des cas, faibles quand ils sont justes (beaucoup de `null` / non-`null`), et
souvent en double. Par contre, l'IA va chercher des **données** auxquelles les humains n'ont pas pensé (`"/"`, des
doublons, un `maxLength` trop petit), et c'est ce qui lui permet de tuer des mutants ou de trouver un bug. En gros,
l'IA est utile pour proposer des entrées, mais il faut toujours vérifier ses sorties attendues à la main.
