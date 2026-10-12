
## 5. Génération des tests et corrections

### Ce que ChatUniTest a produit

Paramètres : `qwen2.5-coder:7b`, 2 tests par méthode, 3 tours au maximum. Comme `stopWhenSuccess` vaut `true`,
ChatUniTest s'arrête au premier test qu'il juge réussi. On obtient donc au plus un fichier par méthode.

| Méthode | Durée | Résultat selon ChatUniTest | Fichier produit |
|---|---|---|---|
| `union` | 6 min | réussi au tour 1, après 1 correction | `MediaType_union_11_0_Test` |
| `parse` | 9 min | réussi au tour 0 | `MediaType_parse_7_0_Test` |
| `set` | 39 min | `set(MediaType...)` réussi au tour 1 ; `set(String...)` : ne compile jamais | `MediaType_set_5_0_Test` |
| `getSanitizedEmbeddedFileName` | 32 min | 2 tests × 3 tours, échouent tous à l'exécution | aucun |
| `getSanitizedEmbeddedFilePath` | 10 min | réussi au tour 0 | `FilenameUtils_getSanitizedEmbeddedFilePath_4_0_Test` |

- **Où sont les tests** : les sorties brutes, non modifiées, sont dans `tika-core/chatunitest-tests/`. Les versions
  corrigées sont dans `tika-core/src/test/java/org/apache/tika/mime/` et `.../io/`. Les journaux, les prompts et les
  messages d'erreur de chaque génération sont dans [`docs/chatunitest/<méthode>/`](docs/chatunitest).
- **Est-ce qu'ils compilent** : oui, les 4 classes compilent (une fois Mockito ajouté, voir l'intervention 4).
- **Est-ce qu'ils passent** : non. Copiés tels quels, **21 tests sur 39 échouent**
  ([log](docs/chatunitest/execution-tests-bruts.log)), alors que ChatUniTest les annonçait réussis.

### Corrections

On a fait 12 corrections, qui touchent 24 des 39 tests. Le détail (fichier | problème | correction) est dans
[`docs/chatunitest/corrections.md`](docs/chatunitest/corrections.md). En résumé :

- **1 problème de mise en place** : dans `FilenameUtils`, le mock visait `Metadata.get(String)`, alors que le code
  appelle `Metadata.get(Property)`. La méthode recevait donc toujours `null`.
- **20 oracles faux** : 12 dans `parse` (le modèle attendait `null` pour des chaînes que Tika accepte) et 8 dans
  `FilenameUtils` (le modèle attendait des `_` là où la méthode garde un chemin avec des `/`). On les a remplacés par
  la vraie valeur, après l'avoir vérifiée dans le code. **Ces 20 oracles viennent donc de nous, pas de l'IA.**
- **1 appel faux** dans `set` : un mauvais usage de la réflexion. L'oracle de l'IA (`NullPointerException`) était
  juste, on l'a gardé.
- **1 test sans oracle** (`withPathEndingWithForwardSlash`) : il n'appelle même pas la méthode. On l'a laissé tel
  quel, pour ne pas mélanger un test de l'IA avec un test écrit par nous.

Après les corrections : `Tests run: 39, Failures: 0`, et toute la suite de `tika-core` passe (788 tests).

Au build, Spotless (configuré par `tika-parent`) ajoute l'en-tête de licence et retire les imports inutilisés. On a
vérifié par un diff que le corps des tests n'avait pas changé.

### Un vrai bug de Tika trouvé grâce à un test de l'IA

Le test généré `withPathExceedingMaxLength` appelle :

```java
FilenameUtils.getSanitizedEmbeddedFilePath(metadata /* "examplefile.txt" */, "txt", 5);
```

Au lieu de renvoyer un nom tronqué, **Tika lève `StringIndexOutOfBoundsException`**. Le nom (`examplefile`) dépasse
`maxLength`, et le code tronque avec :

```java
return namePart.substring(0, maxLength - extension.length() - 3) + "..." + extension;
```

Avec `maxLength = 5` et `extension = ".txt"`, ça donne `substring(0, 5 - 4 - 3)`, soit `substring(0, -2)`. La méthode
ne vérifie jamais que `maxLength` est assez grand pour contenir `"..."` et l'extension. `getSanitizedEmbeddedFileName`
utilise exactement la même formule (ligne 190). On l'a vérifié dans `jshell` avec un vrai `Metadata`, sans mock
(`resourceName = "examplefile.txt"`, `maxLength = 5`) : les deux méthodes lèvent
`java.lang.StringIndexOutOfBoundsException: begin 0, end -2, length 11`. Les tests écrits à la main
(`FilenameUtilsTest`) utilisent toujours `maxLength = 50` et ne pouvaient pas voir ce cas.

L'oracle de l'IA (`"example..."`) était faux lui aussi, mais la **donnée** qu'elle a choisie était bonne : c'est
exactement le genre de valeur limite qu'on n'aurait pas forcément pensé à tester. On n'a pas corrigé le bug, puisque
la consigne interdit de modifier `src/main`. Dans le test, on a mis `maxLength = 10` pour que le test passe, et on
documente le bug ici.
ut>
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

Les tests générés vont dans `tika-core/chatunitest-tests/`. On ne les copie dans `src/test/java` qu'après les avoir
analysés (voir la section suivante).

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
Après avoir supprimé ce dossier, la 2.1.1 a refait sa propre analyse, et l'erreur a disparu. On n'a pas vérifié
dans le code ce que la 2.1.1 fait de différent, seulement que ça fonctionne.

**3. Limiter la longueur des réponses (`maxResponseTokens`).**
Le plugin coupe chaque requête au LLM après 5 minutes, une valeur fixée dans son code. Sur notre processeur, le
modèle génère environ 4 tokens par seconde. Avec la 1.4.1, sur `getSanitizedEmbeddedFileName`, quatre requêtes ont
été coupées à 5 minutes. La seule réponse complète (4 min 36) a donné un test qui ne compilait pas, et aucun test
n'a été produit. Avec `maxResponseTokens` à 900, les réponses prennent environ 3 minutes et passent sous
la limite. Pour la même raison, on a réduit `testNumber` à 2 (5 par défaut) et `maxRounds` à 3 (5 par défaut),
sinon la génération des six méthodes aurait pris plusieurs heures. Même comme ça, certaines requêtes de correction
dépassent encore les 5 minutes : elles contiennent le test et le message d'erreur, donc le prompt est plus long.

**4. Ajouter Mockito aux dépendances de test de `tika-core`.**
Une fois les trois premiers problèmes réglés, le modèle a bien généré des tests pour `union`, mais aucun ne
compilait : les 2 tests × 3 tours ont tous échoué avec `package org.mockito does not exist`. ChatUniTest ajoute
toujours les imports de Mockito (`org.mockito.*`, `MockitoExtension`) dans les tests qu'il génère, et `tika-core`
n'avait pas Mockito. Les tours de correction ne pouvaient donc pas régler le problème. On a ajouté `mockito-core` et
`mockito-junit-jupiter` en portée `test`, avec la version déjà fixée par `tika-parent` (5.23.0). Les sorties brutes
de cet essai raté sont dans [`docs/chatunitest/essai1-union-sans-mockito`](docs/chatunitest/essai1-union-sans-mockito).
