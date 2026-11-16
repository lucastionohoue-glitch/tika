# IFT3913 – Tâche 2 : génération de tests avec ChatUniTest

[![Tache 2 - tests tika-core](https://github.com/lucastionohoue-glitch/tika/actions/workflows/tache2.yml/badge.svg)](https://github.com/lucastionohoue-glitch/tika/actions/workflows/tache2.yml)

Ce dépôt est un fork d'Apache Tika (4.0.0-SNAPSHOT). Tout notre travail se trouve dans le module `tika-core`. Le
README original de Tika a été remplacé par celui-ci, puisque la consigne demande un seul README à la racine. Le
dossier `docs/` contient les fichiers bruts (rapports PIT complets, journaux de ChatUniTest, matrice des mutants),
mais tout ce qu'il faut pour la correction se trouve dans ce README.

Sommaire :
1. Binôme
2. Classes et méthodes choisies
3. PIT, run 1 : les tests originaux
4. Installation et configuration de ChatUniTest
5. Tests générés : où ils sont, compilation, corrections
6. Comparaison des oracles : IA vs tests manuels
7. PIT, run 2 : avec les tests générés
8. Tests manuels pour les survivants et PIT, run 3
9. GitHub Action
10. Bugs trouvés et conclusion

## 1. Binôme

| Nom |
|---|
| Lucas TIONOHOUE |
| Moussa ADAMA SOGOBA |

## 2. Classes et méthodes choisies

On a pris deux classes de `tika-core` qui avaient déjà des tests, mais pas une couverture de 100 % :

- `org.apache.tika.mime.MediaType` (tests existants : `MediaTypeTest`) : représentation et analyse d'un type MIME
  (`text/html; charset=UTF-8`).
- `org.apache.tika.io.FilenameUtils` (tests existants : `FilenameUtilsTest`) : nettoyage des noms et des chemins de
  fichiers trouvés dans les documents (pour éviter, par exemple, un `../../../` dans un nom de pièce jointe).

Pour choisir les méthodes, on a regardé chaque méthode dans le rapport JaCoCo (couverture des lignes et des branches)
et dans le rapport PIT du run 1 (mutants sans couverture et survivants). On a laissé de côté les méthodes trop
simples : getters, `equals`, `hashCode`, `toString`, `compareTo` et les fabriques d'une ligne comme `text()`.

JaCoCo a été lancé avec tous les tests de `tika-core`, alors que PIT n'exécute que `MediaTypeTest` et
`FilenameUtilsTest`. Certains pourcentages JaCoCo sont donc plus hauts que la couverture réelle de ces deux classes
de test.

| Classe | Méthode | % lignes (JaCoCo) | % branches | Mutants sans couverture + survivants (PIT run 1) | Pourquoi |
|---|---|---|---|---|---|
| `MediaType` | `union` (privée) | 25 % (2/8) | 25 % (1/4) | 7 (5 + 2) | Aucun test ne passe une map vide, ni deux maps non vides. Les mutants `emptyMap` et « `putAll` supprimé » restent en vie. |
| `MediaType` | `parse` | 75 % (18/24) | 72 % (13/18) | 3 (2 + 1) | Une mutation arithmétique survit (ligne 260), et la branche « charset en premier » (lignes 277–278) n'est jamais exécutée. |
| `MediaType` | `set(MediaType...)`, `set(String...)` | 0 % | 0 % | 4 (4 + 0) | Jamais appelées par les tests originaux. |
| `FilenameUtils` | `getSanitizedEmbeddedFileName` | 94 % (31/33) | 83 % (15/18) | 5 (2 + 3), plus 8 (3 + 5) dans `getEmbeddedName` | `getEmbeddedName` cherche le nom dans 5 propriétés des métadonnées, dans un ordre précis. Aucun test ne vérifie cet ordre. |
| `FilenameUtils` | `getSanitizedEmbeddedFilePath` | 91 % (42/46) | 83 % (20/24) | 7 (4 + 3), plus 8 (3 + 5) dans `getEmbeddedPath` et 8 (6 + 2) dans `getPrefixLength` | Même problème d'ordre dans `getEmbeddedPath`. Trois « changed conditional boundary » survivent (lignes 215, 264 et 265). |

Au départ, on voulait cibler directement `getEmbeddedName`, `getEmbeddedPath` et `getPrefixLength`. Mais elles sont
privées, et ChatUniTest ne génère pas de tests pour une méthode privée. On a donc pris les deux méthodes publiques qui
les appellent : leurs tests passent forcément par le code privé.

Le tableau complet, avec toutes les méthodes des deux classes, est dans `docs/methodes-run1.md`.

## 3. PIT, run 1 : les tests originaux

On a ajouté PIT dans `tika-core/pom.xml` : `pitest-maven` 1.30.0 avec `pitest-junit5-plugin` 1.2.3, puisque Tika
utilise JUnit 6 (Jupiter). PIT ne mute que nos deux classes, avec les mutateurs par défaut.

Deux réglages ont été nécessaires pour que PIT démarre. Sans eux, il plantait en reprenant l'`argLine` de Surefire
défini par Tika :

```xml
<parseSurefireArgLine>false</parseSurefireArgLine>
<argLine>-Xmx4g -Djava.awt.headless=true</argLine>
```

Commande (PowerShell, depuis la racine du dépôt) :

```powershell
.\mvnw.cmd -pl tika-core test-compile org.pitest:pitest-maven:mutationCoverage "-Drat.skip=true" "-Dcheckstyle.skip=true" "-Dforbiddenapis.skip=true"
```

Sous PowerShell, il faut mettre les `-D...` entre guillemets, sinon PowerShell coupe l'argument au point.

| Classe | Couverture de lignes | Mutants | Tués | Survivants | Sans couverture | Score de mutation | Test strength |
|---|---|---|---|---|---|---|---|
| `FilenameUtils` | 88 % (161/184) | 115 | 73 | 20 | 22 | 63 % | 78 % |
| `MediaType` | 78 % (123/157) | 85 | 52 | 15 | 18 | 61 % | 78 % |
| **Total** | **83 % (284/341)** | **200** | **125** | **35** | **40** | **63 %** | **78 %** |

Le score de mutation, c'est tués / générés. La test strength, c'est tués / mutants couverts (générés moins ceux sans
couverture). Le rapport HTML complet est dans `docs/pit/run1/`.

## 4. Installation et configuration de ChatUniTest

### Ce qu'on a installé

1. **Ollama** (LLM local), installé avec `winget install --id Ollama.Ollama -e`.
2. **Le modèle** `qwen2.5-coder:7b` (environ 4,7 Go) : `ollama pull qwen2.5-coder:7b`.
3. **Le plugin Maven ChatUniTest** dans `tika-core/pom.xml` :

```xml
<plugin>
  <groupId>io.github.zju-aces-ise</groupId>
  <artifactId>chatunitest-maven-plugin</artifactId>
  <version>2.1.1</version>
  <configuration>
    <url>${chatunitest.url}</url>   <!-- http://localhost:11434/v1/chat/completions -->
    <model>code-llama</model>
    <apiKeys>ollama</apiKeys>
    <maxResponseTokens>900</maxResponseTokens>
    <testNumber>2</testNumber>
    <maxRounds>3</maxRounds>
    <temperature>0.5</temperature>
    <enableMultithreading>false</enableMultithreading>
    <testOutput>${project.basedir}/chatunitest-tests</testOutput>
    <tmpOutput>${project.build.directory}/chatunitest-info</tmpOutput>
  </configuration>
</plugin>
```

Ollama ignore la clé d'API, mais le plugin en exige une, d'où la valeur `ollama`. Le dossier `tmpOutput` par défaut
(`/tmp/chatunitest-info`) ne fonctionne pas sous Windows, alors on l'a mis dans `target`. On a coupé le
multithreading parce que le modèle tourne sur le processeur et ne traite qu'une requête à la fois.

La génération se lance méthode par méthode, depuis le dossier `tika-core` :

```powershell
cd tika-core
..\mvnw.cmd io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:method "-DselectMethod=MediaType#union" "-Drat.skip=true" "-Dcheckstyle.skip=true" "-Dforbiddenapis.skip=true"
```

On l'a lancée 5 fois : `MediaType#union`, `MediaType#parse`, `MediaType#set` (les deux surcharges),
`FilenameUtils#getSanitizedEmbeddedFileName` et `FilenameUtils#getSanitizedEmbeddedFilePath`.

### Les 4 interventions manuelles

ChatUniTest n'a pas fonctionné tel quel sur notre machine (Windows 11, Intel i7-1355U, sans carte graphique
dédiée). Voici ce qu'on a dû faire.

**1. Passer à ChatUniTest 2.1.1, avec un alias `code-llama` pour le modèle.**
On avait d'abord pris la version 1.4.1 (`io.github.ZJU-ACES-ISE`), la dernière publiée sous ce groupId. Pour
`MediaType`, elle échouait avant même d'appeler le modèle, avec seulement le message « Error when generating tests
for union in MediaType ». En faisant journaliser les exceptions par la JVM (`-Xlog:exceptions=info`), on a trouvé la
cause : `InvalidPathException: Illegal char <<>`. Le plugin transforme les types dont dépend la méthode en chemins
de fichiers, et pour `union` l'un d'eux est `java.util.Map<java.lang.String, java.lang.String>`. Windows refuse `<`
dans un chemin.

On est donc passés à la version 2.1.1, publiée sous un autre groupId (`io.github.zju-aces-ise`, en minuscules).
Cette version n'accepte qu'une liste fixe de noms de modèles (`gpt-4o`, `code-llama`, `codeqwen:v1.5-chat`, etc.),
et `qwen2.5-coder:7b` n'en fait pas partie. On a donc créé dans Ollama un alias qui pointe vers le même modèle :

```powershell
ollama cp qwen2.5-coder:7b code-llama
```

Le plugin envoie `code-llama`, mais c'est bien `qwen2.5-coder:7b` qui répond : les deux noms ont le même
identifiant (`dae161e27b0e`).

**2. Supprimer l'analyse laissée par l'ancienne version.**
Au premier lancement, la 2.1.1 a affiché « Parse output already exists, skip parsing! ». Elle réutilisait l'analyse
du projet faite par la 1.4.1 dans `target/chatunitest-info`, et on retombait sur la même `InvalidPathException`.
Après avoir supprimé ce dossier, la 2.1.1 a refait sa propre analyse, et l'erreur a disparu. On n'a pas vérifié dans
le code ce que la 2.1.1 fait de différent, seulement que ça fonctionne.

**3. Limiter la longueur des réponses (`maxResponseTokens`).**
Le plugin coupe chaque requête au LLM après 5 minutes, une valeur fixée dans son code. Sur notre processeur, le
modèle génère environ 4 tokens par seconde. Avec la 1.4.1, sur `getSanitizedEmbeddedFileName`, quatre requêtes ont
été coupées à 5 minutes. La seule réponse complète (4 min 36) a donné un test qui ne compilait pas, et aucun test n'a
été produit. Avec `maxResponseTokens` à 900, les réponses prennent environ 3 minutes et passent sous la limite. Pour
la même raison, on a réduit `testNumber` à 2 (5 par défaut) et `maxRounds` à 3 (5 par défaut), sinon la génération
aurait pris plusieurs heures. Même comme ça, certaines requêtes de correction dépassent encore les 5 minutes : elles
contiennent le test et le message d'erreur, donc le prompt est plus long.

**4. Ajouter Mockito aux dépendances de test de `tika-core`.**
Une fois les trois premiers problèmes réglés, le modèle a bien généré des tests pour `union`, mais aucun ne compilait :
les 2 tests × 3 tours ont tous échoué avec `package org.mockito does not exist`. ChatUniTest ajoute toujours les
imports de Mockito (`org.mockito.*`, `MockitoExtension`) dans les tests qu'il génère, et `tika-core` n'avait pas
Mockito. Les tours de correction ne pouvaient donc pas régler le problème. On a ajouté `mockito-core` et
`mockito-junit-jupiter` en portée `test`, avec la version déjà fixée par `tika-parent` (5.23.0). Les sorties brutes
de cet essai raté sont dans `docs/chatunitest/essai1-union-sans-mockito/`.

## 5. Tests générés : où ils sont, compilation, corrections

### Ce que ChatUniTest a produit

Paramètres : `qwen2.5-coder:7b`, 2 tests par méthode, 3 tours au maximum. Comme `stopWhenSuccess` vaut `true`,
ChatUniTest s'arrête au premier test qu'il juge réussi. On obtient donc au plus un fichier par méthode.

| Méthode | Durée | Résultat selon ChatUniTest | Fichier produit | Nombre de tests |
|---|---|---|---|---|
| `union` | 6 min | réussi au tour 1, après 1 correction | `MediaType_union_11_0_Test` | 1 |
| `parse` | 9 min | réussi au tour 0 | `MediaType_parse_7_0_Test` | 23 |
| `set` | 39 min | `set(MediaType...)` réussi au tour 1 ; `set(String...)` : 2 tests qui ne compilent jamais | `MediaType_set_5_0_Test` | 4 |
| `getSanitizedEmbeddedFileName` | 32 min | 2 tests × 3 tours, tous en échec à l'exécution | aucun | 0 |
| `getSanitizedEmbeddedFilePath` | 10 min | réussi au tour 0 | `FilenameUtils_getSanitizedEmbeddedFilePath_4_0_Test` | 11 |

### Où sont les tests

- **Sorties brutes de ChatUniTest, jamais modifiées** : `tika-core/chatunitest-tests/tika-parent/tika-core/org/apache/tika/{mime,io}/`.
- **Versions corrigées, exécutées par Maven** : `tika-core/src/test/java/org/apache/tika/mime/` (3 classes) et
  `tika-core/src/test/java/org/apache/tika/io/` (1 classe).
- **Journaux, prompts et messages d'erreur de chaque génération** : `docs/chatunitest/<Classe>_<méthode>/`.

### Est-ce qu'ils compilent ? Est-ce qu'ils passent ?

- **Compilation** : oui, les 4 classes compilent, une fois Mockito ajouté (intervention 4).
- **Exécution** : non. Copiés tels quels, **21 tests sur 39 échouent**, alors que ChatUniTest les annonçait réussis.
  Pour lui, un test est « réussi » dès qu'il compile et s'exécute, même si des assertions échouent (log complet dans
  `docs/chatunitest/execution-tests-bruts.log`).

| Fichier | Tests | Passent | Échouent |
|---|---|---|---|
| `MediaType_union_11_0_Test` | 1 | 1 | 0 |
| `MediaType_set_5_0_Test` | 4 | 3 | 1 |
| `MediaType_parse_7_0_Test` | 23 | 11 | 12 |
| `FilenameUtils_getSanitizedEmbeddedFilePath_4_0_Test` | 11 | 3 | 8 |
| **Total** | **39** | **18** | **21** |

Remarque : au build, Spotless (configuré par `tika-parent`) ajoute automatiquement l'en-tête de licence Apache et
enlève les imports inutilisés. On a vérifié par un diff que le corps des classes n'avait pas changé. Ce n'est donc
pas une correction de notre part.

### Les corrections

Notre règle : on ne touche pas à `src/main`. Quand un oracle est faux, on le remplace par la valeur réellement
renvoyée, mais seulement après avoir vérifié dans le code que ce comportement est voulu. On garde les données et
l'intention du test chaque fois que c'est possible. Chaque cas est noté ci-dessous.

| # | Fichier / test | Problème | Correction |
|---|---|---|---|
| 1 | `FilenameUtils_…FilePath_4_0_Test` (les 11 tests) | Le mock `when(metadata.get(anyString()))` vise `Metadata.get(String)`, alors que `getEmbeddedPath` appelle `Metadata.get(Property)`. Le mock renvoie donc toujours `null`, et la méthode aussi. | `anyString()` remplacé par `any(Property.class)` |
| 2 | idem, `withPathContainingProtocol` | Oracle `"file.txt"`. `removeProtocol` enlève seulement `http://`. | `"example.com/file.txt"` |
| 3 | idem, `withPathContainingInvalidCharacters` | Oracle `"example_file.txt"`. `normalize` remplace `?` par son code hexadécimal. | `"example%3Ffile.txt"` |
| 4 | idem, `withPathExceedingMaxLength` | Avec `maxLength = 5`, Tika lève `StringIndexOutOfBoundsException` (**bug de Tika**, voir la section 10). L'oracle `"example..."` était faux de toute façon : il manque l'extension. | `maxLength = 10`, oracle `"exa....txt"` |
| 5 | idem, `withPathEndingWithMultipleSlashes` | Oracle `"examplefile.txt"`. La méthode renvoie un chemin : `//` devient `/`. | `"example/file.txt"` |
| 6 | idem, `withPathEndingWithDot` | Oracle `"example_file.txt"`. Un point au milieu du nom ne change pas. | `"example.file.txt"` |
| 7 | idem, `withPathEndingWithDoubleDot` | Oracle `"example_file.txt"`. `\.{2,}` est remplacé par un seul `.`. | `"example.file.txt"` |
| 8 | idem, `withPathEndingWithColon` | Oracle `"example_file.txt"`. `:` est remplacé par `/`. | `"example/file.txt"` |
| 9 | idem, `withPathEndingWithBackslash` | Oracle `"examplefile.txt"`. `\` est remplacé par `/`. | `"example/file.txt"` |
| 10 | idem, `withPathEndingWithForwardSlash` | Le test prépare le mock mais n'appelle jamais la méthode et n'a **aucun oracle**. Il passe toujours. | **Pas corrigé** : si on ajoutait l'appel et l'assert, ce serait un test écrit par nous, et la comparaison IA / manuel ne tiendrait plus. On le compte comme un test sans oracle. |
| 11 | `MediaType_parse_7_0_Test` (12 tests `*Charset`, `*Parameter`, `*Value`) | Le modèle suppose que `parse` renvoie `null` pour `"text/plain; charset="`, `"…; name="`, `"…; name=value="`, etc. En fait, `parseParameters` accepte une valeur vide ou contenant `=`. | `assertNull` remplacé par `assertNotNull` + `assertEquals(<toString réel>)`, par ex. `"text/plain; charset="` |
| 12 | `MediaType_set_5_0_Test.testSetMethodWithNull` | `setMethod.invoke(null, (Object[]) null)` veut dire « aucun argument » pour la réflexion. `invoke` lève donc `IllegalArgumentException` sans jamais entrer dans `set`. | Appel direct `MediaType.set((MediaType[]) null)`. L'oracle `NullPointerException` de l'IA est gardé, et il est juste. |

**Bilan des corrections :**
- Sur les 12 cas, **11 sont des corrections**. Le cas 10 a été laissé tel quel.
- Ces 11 corrections changent :
  - **20 oracles** : 8 dans `FilenameUtils` (cas 2 à 9) et 12 dans `parse` (cas 11) ;
  - **1 mise en place** : le mock (cas 1), présent dans les 11 tests du fichier ;
  - **1 appel** (cas 12).
- **24 tests sur 39 sont modifiés**, et 15 ne sont pas touchés.
- Après les corrections : `Tests run: 39, Failures: 0`.

**Ces 20 oracles corrigés viennent de nous, pas de l'IA.** Pour l'analyse des mutants (section 7), on sépare donc
les asserts en trois groupes :

| Origine de l'assert | Tests |
|---|---|
| **IA, d'origine** | `union` (1) ; `set` (4) ; les 11 tests de `parse` qui passaient déjà ; `FilenameUtils` : `withNullPath`, `withEmptyPath` |
| **Corrigé par nous** | `parse` : les 12 tests du cas 11 ; `FilenameUtils` : les 8 tests des cas 2 à 9 |
| **Aucun oracle** | `FilenameUtils` : `withPathEndingWithForwardSlash` |

## 6. Comparaison des oracles : IA vs tests manuels

On compare les asserts **bruts** de ChatUniTest (avant nos corrections) avec ceux de `MediaTypeTest` et
`FilenameUtilsTest`.

| Méthode ciblée | Asserts IA | Dont faux | Asserts manuels |
|---|---|---|---|
| `union` (privée) | 8 (7 `assertEquals`, 1 `assertTrue`) | 0 | 0 (jamais testée directement) |
| `set(MediaType...)` | 4 (2 `assertEquals`, 1 `assertTrue`, 1 `assertThrows`) | 0 (mais l'appel du `assertThrows` était faux) | 0 |
| `set(String...)` | 0 (rien n'a compilé) | – | 0 |
| `parse` | 28 (5 `assertEquals`, 5 `assertNotNull`, 18 `assertNull`) | **12** (tous des `assertNull`) | 24 dans 5 tests |
| `getSanitizedEmbeddedFilePath` | 10 (8 `assertEquals`, 2 `assertNull`) + 1 test sans assert | **8** (tous les `assertEquals`) | 34 dans `testEmbeddedFilePaths` |
| `getSanitizedEmbeddedFileName` | 0 (aucun test produit) | – | 38 dans `testEmbeddedFileNames` |
| **Total** | **50** | **20 (40 %)** | |

Côté manuel, aucun assert n'est faux : ces tests passent et décrivent le comportement voulu par les développeurs.

**D'où vient l'oracle.**
- *Manuel* : de la connaissance du comportement voulu, et souvent d'un bug réel. Les tests citent leur source :
  `TIKA-121` (paramètres), `TIKA-349` (`;;` dans les paramètres), RFC 2045 §5.1 (charset entre guillemets). Dans
  `FilenameUtilsTest`, chaque assert correspond à une règle précise de nettoyage.
- *IA* : il est deviné à partir du nom de la méthode et de la forme de l'entrée. Le modèle n'a pas suivi le corps de
  la méthode. Deux suppositions reviennent tout le temps :
  - « entrée bizarre, donc `null` » : les 12 `assertNull` faux de `parse` ;
  - « caractère spécial, donc `_` » : les 8 `assertEquals` faux de `…FilePath`. Ça ressemble aux règles de
    `getSanitizedEmbeddedFileName` (`..` devient `_`), mais `…FilePath` garde un **chemin** : `:` devient `/`,
    `?` devient `%3F`, `..` devient `.`.

**Force des asserts.**
- *IA* : sur les 28 asserts de `parse`, 23 sont des `assertNull` / `assertNotNull`. Ils vérifient seulement que la
  méthode renvoie quelque chose, pas ce qu'elle renvoie. Les 5 `assertEquals` comparent `toString()`, ce qui est fort.
  Le meilleur test généré est celui de `union` : il appelle la méthode privée par réflexion et vérifie les 4 branches
  (a vide, b vide, les deux remplies, les deux vides), avec la taille **et** le contenu de la map. Les tests manuels
  n'entrent jamais dans la branche qui fusionne deux maps non vides.
- *Manuel* : les tests de `parse` vérifient le contenu de `getParameters()` (nombre de clés, valeurs une par une). Il
  y a aussi des asserts manuels faibles, comme `assertNotNull(type.getParameters().keySet())`, qui ne peut jamais
  échouer, ou `testParseWithParams`, qui vérifie les clés mais pas les valeurs.

**Choix des données.**
- *IA* : beaucoup de doublons. `testParseNullCharset` et `testParseEmptyCharset` ont exactement la même entrée, et
  `testParseEmptyString` et `testParseEmptyTypeAndSubtype` testent tous les deux `""`. Le nom promet « Null », mais la
  donnée ne le teste pas. Par contre, l'IA essaie des valeurs limites que les humains n'ont pas testées : `parse("/")`
  (tue 2 mutants), `set` avec des doublons (tue 2 mutants d'`equals`) et surtout `maxLength = 5`, **qui a révélé un
  vrai bug**.
- *Manuel* : des données réalistes et variées (chemins Windows, `~/`, `file:///`, guillemets, `\u0000`, noms de 80
  caractères), mais toujours `maxLength = 50`, et la même valeur dans `RESOURCE_NAME_KEY` et `EMBEDDED_RESOURCE_PATH`.
  Ces deux choix laissent passer des mutants (section 7).

**Mise en place.**
- *IA* : elle bouchonne `Metadata` avec Mockito, alors que c'est une simple classe de données, et elle se trompe de
  surcharge. Elle utilise aussi la réflexion pour `set`, qui est publique.
- *Manuel* : un vrai `Metadata`, construit par une petite méthode utilitaire.

**Tests sans oracle** : un dans les tests de l'IA (cas 10), aucun dans les tests manuels.

**Conclusion.** Les oracles manuels sont justes et expliqués. Ceux de l'IA sont faux dans 40 % des cas, souvent
faibles quand ils sont justes, et souvent en double. Par contre, l'IA propose des **données** auxquelles les humains
n'avaient pas pensé, et c'est ce qui lui permet de tuer des mutants ou de trouver un bug. Elle est utile pour proposer
des entrées, mais il faut vérifier chaque sortie attendue.

## 7. PIT, run 2 : avec les tests générés

On a ajouté les 4 classes générées (corrigées) à `targetTests`, et activé la matrice complète :

```xml
<fullMutationMatrix>true</fullMutationMatrix>
<outputFormats>
  <param>XML</param>
  <param>HTML</param>
</outputFormats>
```

Avec `fullMutationMatrix`, `mutations.xml` donne **tous** les tests qui tuent chaque mutant, pas seulement le
premier. Le run 1 n'avait pas de XML, alors on a recalculé son résultat à partir de la matrice du run 2 : un mutant
était tué au run 1 si au moins un test de `MediaTypeTest` ou de `FilenameUtilsTest` le tue. Ce calcul redonne
exactement les chiffres du run 1 (73/20/22 et 52/15/18), ce qui confirme qu'il est juste. Le rapport est dans
`docs/pit/run2/`.

### Scores

| Classe | Run | Tués | Survivants | Sans couverture | Couverture de lignes | Score de mutation | Test strength |
|---|---|---|---|---|---|---|---|
| `FilenameUtils` (115) | 1 | 73 | 20 | 22 | 88 % | 63 % | 78 % |
| | 2 | 73 | 20 | 22 | 88 % | 63 % | 78 % |
| `MediaType` (85) | 1 | 52 | 15 | 18 | 78 % | 61 % | 78 % |
| | 2 | **65** | 11 | 9 | 89 % | **76 %** | **86 %** |
| **Total** (200) | 1 | 125 | 35 | 40 | 83 % | 63 % | 78 % |
| | 2 | **138** | 31 | 31 | 88 % | **69 %** | **82 %** |

### Les 13 mutants tués au run 2 mais pas au run 1

Ils sont tous dans `MediaType`, et **tous sont tués par un assert d'origine de l'IA**. Aucun n'a besoin de nos
corrections.

| # | Méthode | Ligne | Mutateur | Run 1 | Tests qui le tuent | Pourquoi |
|---|---|---|---|---|---|---|
| 1 | `set(MediaType...)` | 211 | NegateConditionals (`type != null`) | sans couverture | `testSetMethod`, `testSetMethodWithDuplicateValues` | Le mutant ne garde que les `null` : l'ensemble est vide, et les `assertEquals` sur le contenu et la taille échouent. |
| 2 | `set(MediaType...)` | 215 | EmptyObjectReturnVals (`emptySet`) | sans couverture | idem | L'ensemble est vide au lieu de contenir 2 éléments (ou 1 pour les doublons). |
| 3 | `parse` | 277 | NegateConditionals (`CHARSET_FIRST_PATTERN` matches) | sans couverture | `testParseNullTypeAndSubtype` | Pour `"/"`, aucun motif ne correspond. Le mutant appelle quand même `matcher.group(2)`, ce qui lève `IllegalStateException`. Il est tué par l'**exception**, et l'`assertNull` n'est même pas atteint. |
| 4 | `isSimpleName` | 293 | BooleanTrueReturnVals | sans couverture | `testParseNullTypeAndSubtype` | `""` passe pour un nom valide, donc `parse("/")` construit un `MediaType`, et `assertNull` échoue. |
| 5 | `union` | 349 | NegateConditionals (`a.isEmpty()`) | survivant | `testUnion` | `union(map1, map2)` renvoie `map2` seul : la taille vaut 1 au lieu de 2. |
| 6 | `union` | 350 | EmptyObjectReturnVals (`return b`) | survivant | `testUnion` | `union(vide, map1)` renvoie une map vide. |
| 7 | `union` | 351 | NegateConditionals (`b.isEmpty()`) | sans couverture | `testUnion` | `union(map1, map2)` renvoie `map1` seul. |
| 8 | `union` | 352 | EmptyObjectReturnVals (`return a`) | sans couverture | `testUnion` | `union(map1, vide)` renvoie une map vide. |
| 9 | `union` | 355 | VoidMethodCall (`putAll(a)` supprimé) | sans couverture | `testUnion` | `key1` manque. |
| 10 | `union` | 356 | VoidMethodCall (`putAll(b)` supprimé) | sans couverture | `testUnion` | `key2` manque. |
| 11 | `union` | 357 | EmptyObjectReturnVals | sans couverture | `testUnion` | La map fusionnée est vide. |
| 12 | `equals` | 416 | NegateConditionals (`instanceof`) | survivant | `testSetMethodWithDuplicateValues` | Deux `application/json` égaux ne sont plus `equals`, et le `HashSet` garde les deux : la taille vaut 2 au lieu de 1. |
| 13 | `equals` | 418 | BooleanFalseReturnVals | survivant | `testSetMethodWithDuplicateValues` | Même raison. |

`equals` n'était pas visé. Ses deux mutants meurent « au passage », parce que le test des doublons de `set` dépend
d'`equals`.

### Ce qu'apportent vraiment nos oracles corrigés

Si on regarde **tous** les mutants tués par les tests générés (pas seulement les nouveaux), la séparation par origine
change beaucoup les conclusions :

| Classe | Tués par au moins 1 test généré | … par un assert IA d'origine | … seulement par un assert corrigé (parmi les générés) | Tués **seulement** par des tests générés |
|---|---|---|---|---|
| `FilenameUtils` | 41 | 2 | 39 | 0 |
| `MediaType` | 60 | 55 | 5 | 13 |

- **`FilenameUtils`** : sur les 41 mutants tués par des tests générés, **39 ne meurent que grâce à nos oracles
  corrigés**. Les deux seuls asserts d'origine utiles (`withNullPath`, `withEmptyPath`) ne vérifient que le cas
  `null`. De toute façon, ces 41 mutants étaient déjà tués par `FilenameUtilsTest`, donc le gain est nul. Si on
  attribuait ces 39 morts à l'IA, on surestimerait beaucoup la qualité des tests générés.
- **`MediaType`** : c'est l'inverse. Les asserts d'origine font presque tout le travail (55 sur 60), et les 13 nouveaux
  mutants tués le sont tous par des asserts IA simples (taille d'une map, contenu d'un ensemble, `null` pour `"/"`).
  Les 12 oracles de `parse` qu'on a corrigés ne tuent aucun nouveau mutant.
- Le test sans oracle ne tue rien.

### Survivants après le run 2 (31 survivants, 31 sans couverture)

On les a classés en trois raisons : **jamais atteint** (aucun test n'exécute la ligne), **oracle ou données trop
faibles** (la ligne est exécutée, mais aucun test ne vérifie le comportement qui change), et **équivalent** (le
mutant ne change rien d'observable). Les principaux :

| Méthode | Lignes | Raison |
|---|---|---|
| `getEmbeddedPath`, `getEmbeddedName` | 344–359, 365–380 | **Données trop faibles.** Les deux méthodes essaient 5 propriétés dans un ordre précis. Le `getMetadata` de `FilenameUtilsTest` met le même nom dans deux propriétés, et le mock de l'IA renvoie la même valeur pour toutes. Si on inverse une condition, on obtient la même chaîne par une autre propriété. Les `return` des propriétés 2 à 4 ne sont jamais atteints. |
| `getSanitizedEmbeddedFilePath`, `getSanitizedEmbeddedFileName` | 264, 265, 189 (+ 268 sans couverture) | **Données trop faibles.** Aucun test n'a une longueur **exactement** égale à `maxLength`, ni un chemin trop long avec un nom court. |
| `set(String...)` | 230, 234 | **Jamais atteint** : ChatUniTest n'a rien produit qui compile. |
| `parse` | 278 | **Jamais atteint** : personne ne teste la forme « charset en premier » (TIKA-350). |
| `parse`, `isSimpleName` | 260, 293 | **Presque équivalents** : le type passe par l'expression régulière au lieu du chemin rapide, et le résultat est le même (`equals`). Seul le cache `SIMPLE_TYPES` change, donc on peut les tuer avec `assertSame`. |
| `getSanitizedEmbeddedFileName`, `…FilePath`, `getPrefixLength` | 156, 215, 320, 323 | **Équivalents** (justifiés en section 8). |
| Getters de `MediaType`, `resolveWithin`, fabriques | divers | Hors des méthodes ciblées. |

Au run 2, on avait aussi classé 359 et 380 (`return ""` au lieu de `ORIGINAL_RESOURCE_NAME`) comme équivalents.
C'était faux : voir la section 8.

## 8. Tests manuels pour les survivants et PIT, run 3

On a écrit 7 tests, dans deux classes, pour les survivants les plus intéressants des méthodes ciblées :
- `tika-core/src/test/java/org/apache/tika/io/FilenameUtilsManualTest.java` (4 tests) ;
- `tika-core/src/test/java/org/apache/tika/mime/MediaTypeManualTest.java` (3 tests).

Ils passent tous sur le code original.

### Les tests

**`FilenameUtilsManualTest.testEmbeddedPathPropertyPriority`**
- *Intention* : vérifier l'ordre dans lequel `getEmbeddedPath` (privée, appelée par `getSanitizedEmbeddedFilePath`)
  lit les propriétés : `EMBEDDED_RESOURCE_PATH`, `INTERNAL_PATH`, `RESOURCE_NAME_KEY`, `EMBEDDED_RELATIONSHIP_ID`,
  puis `ORIGINAL_RESOURCE_NAME`.
- *Données* : une valeur **différente** par propriété (`embedded.txt`, `internal.txt`, `resource.txt`,
  `relationship.txt`, `original.txt`). On fait 5 tours : au tour *k*, on ne remplit que les propriétés à partir du
  rang *k*. C'est exactement ce qui manquait aux tests existants, qui mettaient la même valeur partout.
- *Oracle* : le code de `getEmbeddedPath` (lignes 342–360) : la première propriété non vide gagne. Au tour *k*, on
  attend donc la valeur du rang *k* (les noms sont déjà propres, la méthode les rend tels quels).
- *Mutants visés* : 344, 348, 352, 356 (NegateConditionals) ; 349, 353, 357, 359 (EmptyObjectReturnVals).

**`FilenameUtilsManualTest.testEmbeddedNamePropertyPriority`**
- *Intention* : la même chose pour `getEmbeddedName` (appelée par `getSanitizedEmbeddedFileName`), dont l'ordre est
  différent : `RESOURCE_NAME_KEY`, `INTERNAL_PATH`, `EMBEDDED_RELATIONSHIP_ID`, `EMBEDDED_RESOURCE_PATH`,
  `ORIGINAL_RESOURCE_NAME`.
- *Données* : le même principe, avec les valeurs dans cet ordre-là.
- *Oracle* : le code de `getEmbeddedName` (lignes 363–381) et son commentaire (« this tries for resource name first,
  and then backs off to path »).
- *Mutants visés* : 365, 369, 373, 377 (NegateConditionals) ; 370, 374, 378, 380 (EmptyObjectReturnVals).

**`FilenameUtilsManualTest.testEmbeddedFilePathMaxLengthBoundaries`**
- *Intention* : tester les deux comparaisons avec `maxLength` dans `getSanitizedEmbeddedFilePath` **pile à la
  limite**, et la branche « chemin trop long mais nom assez court ».
- *Données* (extension `.txt`, de 4 caractères) :

  | Entrée | `maxLength` | Attendu | Pourquoi |
  |---|---|---|---|
  | `dir/abc.txt` (11 caractères) | 11 | `dir/abc.txt` | chemin de **exactement** `maxLength` caractères : pas raccourci |
  | `dir/abc.txt` | 10 | `abc.txt` | un caractère de trop : on enlève le dossier, et le nom tient |
  | `abcdefgh.txt` (nom de 8) | 8 | `abcdefgh.txt` | nom de **exactement** `maxLength` caractères : pas tronqué |
  | `abcdefghi.txt` (nom de 9) | 8 | `a....txt` | un caractère de trop : tronqué à `8 - 4 - 3 = 1` caractère, plus `...` et `.txt` |

- *Oracle* : le code des lignes 263–270. La limite porte sur le nom **sans** l'extension : `abcdefgh.txt` fait 12
  caractères et n'est pas tronqué. C'est le comportement du code, même s'il est un peu surprenant. On a pris
  `maxLength = 8` pour que `maxLength - 4 - 3` reste positif, sinon on retombe sur le bug de la section 10.
- *Mutants visés* : 264, 265 (ConditionalsBoundary) ; 268 (EmptyObjectReturnVals, sans couverture avant).

**`FilenameUtilsManualTest.testEmbeddedFileNameMaxLengthBoundary`**
- *Intention* : la même limite dans `getSanitizedEmbeddedFileName`.
- *Données* : `abcdefgh.txt` et `abcdefghi.txt`, avec `maxLength = 8`.
- *Oracle* : le code des lignes 188–192, qui utilise la même formule.
- *Mutants visés* : 189 (ConditionalsBoundary).

**`MediaTypeManualTest.testSetFromStringsSkipsUnparseable`**
- *Intention* : tester `set(String...)`, qu'aucun test n'appelait.
- *Données* : `"text/plain"`, `null`, `"pas-un-type"` (pas de `/`), `"text/html"`, puis encore `"text/plain"`. On a
  donc un `null`, une chaîne refusée par `parse`, et un doublon.
- *Oracle* : la Javadoc (« parses the given media type strings and returns an unmodifiable set that contains all the
  parsed types »). `parse` renvoie `null` pour `null` et pour une chaîne sans `/`, et ces `null` sont ignorés. On
  attend donc exactement `{text/plain, text/html}`, dans un ensemble non modifiable (`add` lève
  `UnsupportedOperationException`).
- *Mutants visés* : 230 (NegateConditionals) et 234 (EmptyObjectReturnVals). Le test tue aussi 418 (`equals` renvoie
  toujours `true`) au passage.

**`MediaTypeManualTest.testParseCharsetFirst`**
- *Intention* : tester la forme « charset en premier », que `parse` accepte pour les serveurs mal configurés.
- *Données* : `"charset=UTF-8; text/html"`.
- *Oracle* : la Javadoc de `parse` (« we also handle "charset=xxx; type/subtype" for broken web servers »), TIKA-350
  et `CHARSET_FIRST_PATTERN`. On attend la même chose que pour `"text/html; charset=UTF-8"`.
- *Mutants visés* : 278 (NullReturnVals, sans couverture avant).

**`MediaTypeManualTest.testParseCachesSimpleTypes`**
- *Intention* : vérifier que `parse` met les types simples en cache : deux appels avec la même chaîne doivent
  renvoyer **le même objet**.
- *Données* : `"application/x-" + UUID.randomUUID()`, une chaîne nouvelle à chaque exécution (voir la remarque plus
  bas).
- *Oracle* : la Javadoc de `SIMPLE_TYPES` (« used to optimize type lookup and to avoid having too many MediaType
  instances in memory ») et le code des lignes 252–267.
- *Mutants visés* : 260 (Math, `slash + 1` devient `slash - 1`) et 293 (BooleanFalseReturnVals). Avec ces mutants, le
  type n'est jamais mis en cache. Le résultat reste `equals`, donc seul `assertSame` voit la différence.

**Remarque sur le dernier test.** Notre première version utilisait un nom fixe, et les mutants 260 et 293 survivaient
quand même (162 tués au lieu de 164). PIT installe chaque mutant en redéfinissant la classe dans une JVM qui tourne
déjà, et les champs `static` ne sont pas réinitialisés. Le cache contenait donc déjà notre type depuis une exécution
précédente, et `assertSame` passait même avec le mutant. Avec un nom aléatoire, le test ne dépend plus de l'état du
cache. Le premier essai est gardé dans `docs/pit/run3-essai1/`.

### PIT, run 3

Les deux classes manuelles sont ajoutées à `targetTests`. Le rapport est dans `docs/pit/run3/`.

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

Nos 7 tests tuent **26 mutants de plus** : 20 dans `FilenameUtils` et 6 dans `MediaType`. Tous les mutants visés
plus haut sont tués, plus le 418 d'`equals`.

On s'était trompés sur 359 et 380. Au run 2, on pensait que `return ""` au lieu de `return metadata.get(...)` ne
changeait rien, puisque l'appelant traite `null` et `""` de la même façon avec `isBlank`. Mais le mutant remplace
**toujours** la valeur renvoyée par `""`, même quand `ORIGINAL_RESOURCE_NAME` est rempli. Le dernier tour des tests de
priorité, où seule cette propriété est remplie, les tue.

### Les mutants équivalents (on ne peut pas les tuer)

| Méthode | Ligne | Mutateur | Pourquoi c'est équivalent |
|---|---|---|---|
| `getSanitizedEmbeddedFileName` | 156 | ConditionalsBoundary (`prefixLength > 0` devient `>= 0`) | Notre `getPrefixLength` privé ne renvoie jamais un nombre négatif (la valeur de commons-io si elle est > 0, sinon 2 ou 0). Le mutant ne change donc quelque chose que pour `prefixLength == 0`, où il fait `path.substring(0)`, qui renvoie la même chaîne. |
| `getSanitizedEmbeddedFilePath` | 215 | ConditionalsBoundary | Même raisonnement. |
| `getPrefixLength` | 320 | ConditionalsBoundary (`> 0` devient `>= 0`) | Quand commons-io renvoie 0, le mutant renvoie 0 tout de suite. L'original continue vers le test de la ligne 323, qui ne peut être vrai que pour une chaîne de 2 caractères `X:`. Or commons-io renvoie déjà 2 pour `"C:"` ou `"a:"` (vérifié dans `jshell`), donc l'original renvoie 0 lui aussi. |
| `getPrefixLength` | 323 | NegateConditionals (le survivant) | Même raison : quand on arrive à la ligne 323, commons-io a déjà traité tous les cas `X:`. La condition est toujours fausse, et inverser une de ses parties ne change pas le résultat. |
| `getPrefixLength` | 323, 324 | 5 mutants sans couverture | Code mort : le `return 2` de la ligne 324 ne peut pas être atteint tant que commons-io reconnaît `"X:"`. |

Un cas à part : la **négation** à la ligne 156 n'est **pas** équivalente. En cherchant à la justifier, on a trouvé un
deuxième bug (section 10). Pour `"~"`, l'original plante et pas le mutant. Le seul moyen de le tuer serait un test qui
**attend** l'exception, ce qui figerait le bug. On ne l'a pas écrit.

### Ce qui reste, et qu'on aurait pu tuer

On s'est limités à 7 tests. Restent tuables :
- `getSuffixFromPath` 134 : il faudrait une extension de exactement 5 lettres ;
- `getSanitizedEmbedded…` 174, 185, 232, 250, 259 : il faudrait des noms qui deviennent vides après nettoyage ;
- `resolveWithin` : il faudrait de vrais liens symboliques ;
- les getters et les fabriques de `MediaType` (`getType`, `getSubtype`, `hasParameters`, `getBaseType`, `compareTo`,
  `text()`, etc.) et `equals` ligne 420. Ils sont tous hors des méthodes ciblées.

## 9. GitHub Action

Dernier run vert de l'Action : <https://github.com/lucastionohoue-glitch/tika/actions/workflows/tache2.yml> (le badge
en haut de ce README montre l'état du dernier run).

Le workflow `.github/workflows/tache2.yml` se lance à chaque push (et à la main avec `workflow_dispatch`), sur
`ubuntu-latest` avec Java 17 (Temurin) :

```yaml
- name: Build
  run: ./mvnw -B -pl tika-core -am install -DskipTests -Drat.skip=true -Dcheckstyle.skip=true -Dforbiddenapis.skip=true
- name: Tests tika-core
  run: ./mvnw -B -pl tika-core test -Drat.skip=true -Dcheckstyle.skip=true -Dforbiddenapis.skip=true
```

Il exécute **toute la suite de tests de `tika-core`** (795 tests), dont les 4 classes générées par ChatUniTest
(corrigées) et nos 2 classes de tests manuels. On a supprimé les workflows d'origine de Tika (builds JDK 17/21/25,
Windows, Docker, split-packages) pour que seuls nos tests tournent sur le fork.

## 10. Bugs trouvés et conclusion

### Deux vrais bugs dans `FilenameUtils`

On n'a corrigé aucun des deux, puisque la consigne interdit de modifier `src/main`. Les deux ont été reproduits dans
`jshell` avec un vrai `Metadata`, sans mock.

**Bug 1 : `maxLength` trop petit, trouvé grâce à un test de l'IA.**
Le test généré `withPathExceedingMaxLength` appelle `getSanitizedEmbeddedFilePath` sur `"examplefile.txt"` avec
`maxLength = 5`. Au lieu de renvoyer un nom tronqué, Tika lève une exception. Le code tronque avec :

```java
return namePart.substring(0, maxLength - extension.length() - 3) + "..." + extension;
```

Avec `maxLength = 5` et `extension = ".txt"`, ça donne `substring(0, 5 - 4 - 3)`, soit `substring(0, -2)`. Rien ne
vérifie que `maxLength` est assez grand pour contenir `"..."` et l'extension. `getSanitizedEmbeddedFileName` utilise
la même formule (ligne 190), et les deux méthodes lèvent :

```
java.lang.StringIndexOutOfBoundsException: begin 0, end -2, length 11
```

Les tests manuels de Tika utilisent toujours `maxLength = 50` et ne pouvaient pas voir ce cas. L'oracle de l'IA était
faux, mais la **donnée** qu'elle a choisie était bonne.

**Bug 2 : nom `"~"` ou `"~user"`, trouvé en analysant un mutant.**
En cherchant à prouver que la négation de la ligne 156 était équivalente, on a testé des préfixes inhabituels. Pour
`"~"` et `"~user"`, commons-io renvoie un préfixe plus long que la chaîne elle-même (2 et 6, comme s'il s'agissait de
`"~/"` et `"~user/"`). Tika fait ensuite `path.substring(prefixLength)` sans vérifier la longueur :

```
getSanitizedEmbeddedFileName(resourceName = "~")     -> StringIndexOutOfBoundsException: begin 2, end 1, length 1
getSanitizedEmbeddedFilePath(resourceName = "~user") -> StringIndexOutOfBoundsException: begin 6, end 5, length 5
```

Ces méthodes servent justement à nettoyer des noms de fichiers venant de documents qu'on ne contrôle pas. Un nom
d'attachement `~` suffit donc à faire planter l'appelant.

### Conclusion

- **ChatUniTest**, avec un modèle local de 7B sur processeur, a demandé beaucoup d'efforts : 4 interventions pour qu'il
  fonctionne, environ 1 h 30 de génération pour 5 méthodes, et une méthode sans aucun test. Ses tests compilent, mais
  21 sur 39 échouent alors qu'il les déclare réussis.
- **Les oracles de l'IA** sont faux dans 40 % des cas (20 sur 50). Une fois corrigés, ils semblent tuer beaucoup de
  mutants dans `FilenameUtils`, mais c'est grâce à nos corrections (39 sur 41) et non à l'IA. Les asserts d'origine qui
  fonctionnent sont simples : taille, contenu, `null`. Ils ont fait passer `MediaType` de 61 % à 76 % (13 mutants),
  surtout grâce à `union` et à `set`, que les humains n'avaient pas testées.
- **Les données de l'IA** sont son vrai apport : des valeurs limites comme `"/"`, des doublons ou un `maxLength` trop
  petit. Cette dernière nous a menés à un vrai bug.
- **Les tests manuels**, guidés par la matrice des mutants, sont beaucoup plus efficaces : 7 tests pour 26 mutants
  de plus, et un score total qui passe de 69 % à 82 % (test strength 92 %).
- **L'analyse des mutants équivalents** nous a aussi fait corriger deux erreurs de classement (359 et 380) et trouver
  un deuxième bug. Elle a été aussi utile que l'écriture des tests.

### Contenu de `docs/` (fichiers bruts)

| Dossier / fichier | Contenu |
|---|---|
| `docs/methodes-run1.md` | Couverture JaCoCo et mutants PIT pour chaque méthode des deux classes (run 1) |
| `docs/pit/run1/`, `run2/`, `run3/`, `run3-essai1/` | Rapports PIT HTML complets (et `mutations.xml` à partir du run 2) |
| `docs/chatunitest/` | Journaux, prompts et erreurs de chaque génération, plus l'exécution brute des tests générés |
| `docs/chatunitest/corrections.md`, `docs/oracles.md`, `docs/mutants-run2.md`, `docs/tests-manuels.md` | Version longue des sections 5 à 8 |
