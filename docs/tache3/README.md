# Tâche 3 : analyse statique avec Semgrep

## 0. État de départ

- Commit de départ (fin de la tâche 2) : `bbf47f0b2`
- Rapport de référence : `semgrep-avant.json` / `semgrep-avant.txt` (Semgrep 1.183.0, configs `p/java` + `p/security-audit`)
  - 19 findings : 13 `java.lang.security`, 3 `python.lang.security`, 3 `generic.html-templates.security`
  - 58 erreurs d'analyse (fichiers que Semgrep n'a pas pu analyser)

## 1. Workflow GitHub Actions

Le job `semgrep` est dans `.github/workflows/tache2.yml`, avant le job `test`.

- `semgrep scan --config p/java --config p/security-audit --baseline-commit <ref> --error`
  - `--baseline-commit` : seuls les findings absents du commit de référence sont rapportés,
    donc les vulnérabilités déjà présentes dans Tika ne bloquent pas le build.
  - `--error` : code de sortie ≠ 0 dès qu'un nouveau finding est trouvé, donc le job échoue.
- Le rapport `semgrep.json` est publié comme artefact (`semgrep-report`), même en cas d'échec.
- Choix du commit de référence :
  - `pull_request` : base de la PR ;
  - `push` : commit précédent (`github.event.before`) ;
  - premier push d'une branche, force-push ou lancement manuel sans entrée : `merge-base` avec la branche par défaut.
- Le job `test` (Maven) a `needs: semgrep` : le build ne démarre pas si Semgrep échoue.
  Le job est dans le même fichier que le build parce que `needs` ne fonctionne qu'entre jobs
  d'un même workflow ; un `semgrep.yml` séparé ne pourrait pas bloquer le build.

### Validation (branche `tache3`)

| # | Commit | Changement | Attendu | Obtenu | Run |
|---|--------|------------|---------|--------|-----|
| 1 | `e18965515` | Commit propre (ajout du workflow + cette doc) | ✅ | ✅ | [43890820162](https://github.com/lucastionohoue-glitch/tika/actions/runs/43890820162) |
| 2 | `211e03147` | Ajout de `Tache3Vulnerable.java` (MD5 + DES) | ❌ | ❌ | [43891730445](https://github.com/lucastionohoue-glitch/tika/actions/runs/43891730445) |
| 3 | `6318fac6a` | Suppression de `Tache3Vulnerable.java` | ✅ | ✅ | [43892089807](https://github.com/lucastionohoue-glitch/tika/actions/runs/43892089807) |
| 4 | `9b72a518e` | Modification de `MimeTypesReader.java`, déjà vulnérable (`saxparserfactory-disallow-doctype-decl-missing`), sans nouvelle vulnérabilité | ✅ | ✅ | [43893133402](https://github.com/lucastionohoue-glitch/tika/actions/runs/43893133402) |

Le run 4 montre que la comparaison avec la référence fonctionne : le fichier modifié contient
un finding préexistant, mais comme il existe déjà dans le commit de référence, il ne bloque pas le build.

Captures : TODO

## 2. Les 7 vulnérabilités injectées

Branche `vulnerabilites`, un commit par vulnérabilité. Chaque push est comparé au commit
précédent : chaque run ne rapporte donc que la vulnérabilité ajoutée par ce commit.
Dans les 7 runs, c'est l'étape « Scan Semgrep differentiel » qui échoue, et le job `test`
(build Maven) est sauté grâce à `needs: semgrep`.

| # | Type | Fichier | Règle Semgrep | Commit | Run |
|---|------|---------|---------------|--------|-----|
| 1 | Injection de commande | `tika-core/.../parser/external/ExternalParser.java` | `java.lang.security.audit.command-injection-formatted-runtime-call.command-injection-formatted-runtime-call` | `bfcd8bdae` | [❌ 43904205356](https://github.com/lucastionohoue-glitch/tika/actions/runs/43904205356) |
| 2 | Injection SQL | `tika-parser-jdbc-commons/.../jdbc/JDBCTableReader.java` | `java.lang.security.audit.formatted-sql-string.formatted-sql-string` | `d62240bd5` | [❌ 43904503772](https://github.com/lucastionohoue-glitch/tika/actions/runs/43904503772) |
| 3 | XXE | `tika-core/.../utils/XMLReaderUtils.java` | `java.lang.security.audit.xxe.documentbuilderfactory-disallow-doctype-decl-missing.documentbuilderfactory-disallow-doctype-decl-missing` | `92802816b` | [❌ 43904636969](https://github.com/lucastionohoue-glitch/tika/actions/runs/43904636969) |
| 4 | Hash faible (MD5) | `tika-core/.../digest/InputStreamDigester.java` | `java.lang.security.audit.crypto.use-of-md5.use-of-md5` | `5dc11e324` | [❌ 43904750948](https://github.com/lucastionohoue-glitch/tika/actions/runs/43904750948) |
| 5 | Chiffrement faible (DES) | `tika-core/.../parser/CryptoParser.java` | `java.lang.security.audit.crypto.des-is-deprecated.des-is-deprecated` | `6954b935f` | [❌ 43904858414](https://github.com/lucastionohoue-glitch/tika/actions/runs/43904858414) |
| 6 | Désérialisation non sûre | `tika-core/.../config/ConfigDeserializer.java` | `java.lang.security.audit.object-deserialization.object-deserialization` | `ebdca7871` | [❌ 43905837408](https://github.com/lucastionohoue-glitch/tika/actions/runs/43905837408) |
| 7 | TLS non vérifié | `tika-core/.../parser/NetworkParser.java` | `java.lang.security.audit.crypto.ssl.insecure-hostname-verifier.insecure-hostname-verifier` | `7c1ac577b` | [❌ 43905928719](https://github.com/lucastionohoue-glitch/tika/actions/runs/43905928719) |

Le code compile (`mvn -pl tika-core,…/tika-parser-jdbc-commons -am compile`).

Remarque sur l'injection de commande : la règle `command-injection-formatted-runtime-call`
ne détecte pas `Runtime.getRuntime().exec("sh -c " + x)`, ni un `return Runtime...exec(...)`.
Elle ne se déclenche qu'avec le nom complet `java.lang.Runtime.getRuntime().exec(...)` utilisé
comme instruction seule. C'est un faux négatif de Semgrep : la même vulnérabilité écrite
normalement passerait le contrôle.

Captures : TODO (dont la PR `vulnerabilites` → `tache3` avec le check rouge)

## 3. Comparaison avant/après

Rapports (Semgrep 1.183.0, `p/java` + `p/security-audit`) :

- `semgrep-avant.json` / `semgrep-avant.txt` : scan complet du commit de départ.
- `semgrep-apres.json` : scan complet de la branche `vulnerabilites` (commit `7c1ac577b`).
- `semgrep-nouvelles.txt` : seulement les nouveaux résultats, obtenus avec
  `semgrep scan --config p/java --config p/security-audit --baseline-commit bbf47f0b2 --text`.

### Nombre de résultats

| | Total | ERROR | WARNING | Java | Python | Templates HTML |
|---|---|---|---|---|---|---|
| Avant | 19 | 6 | 13 | 13 | 3 | 3 |
| Après | 26 | 9 | 17 | 20 | 3 | 3 |
| Nouveaux (`--baseline-commit`) | **7** | 3 | 4 | 7 | 0 | 0 |

26 − 19 = 7 : les 19 résultats de départ sont toujours là, et il y a exactement un nouveau résultat
par vulnérabilité injectée. Deux résultats préexistants ont changé de ligne
(`NetworkParser.java` 68 → 71 à cause des imports ajoutés, `MimeTypesReader.java` 223 → 224 à cause
du commentaire du test 4). Semgrep les reconnaît quand même comme anciens : ils ne figurent pas
dans les nouveaux résultats.

### Les 7 nouveaux résultats

| # | Règle | Fichier | Ligne | Sévérité |
|---|-------|---------|-------|----------|
| 1 | `java.lang.security.audit.command-injection-formatted-runtime-call.command-injection-formatted-runtime-call` | `tika-core/src/main/java/org/apache/tika/parser/external/ExternalParser.java` | 304 | ERROR |
| 2 | `java.lang.security.audit.formatted-sql-string.formatted-sql-string` | `tika-parsers/tika-parsers-standard/tika-parsers-standard-modules/tika-parser-jdbc-commons/src/main/java/org/apache/tika/parser/jdbc/JDBCTableReader.java` | 318 | ERROR |
| 3 | `java.lang.security.audit.xxe.documentbuilderfactory-disallow-doctype-decl-missing.documentbuilderfactory-disallow-doctype-decl-missing` | `tika-core/src/main/java/org/apache/tika/utils/XMLReaderUtils.java` | 1313 | ERROR |
| 4 | `java.lang.security.audit.crypto.use-of-md5.use-of-md5` | `tika-core/src/main/java/org/apache/tika/digest/InputStreamDigester.java` | 126 | WARNING |
| 5 | `java.lang.security.audit.crypto.des-is-deprecated.des-is-deprecated` | `tika-core/src/main/java/org/apache/tika/parser/CryptoParser.java` | 113 | WARNING |
| 6 | `java.lang.security.audit.object-deserialization.object-deserialization` | `tika-core/src/main/java/org/apache/tika/config/ConfigDeserializer.java` | 137 | WARNING |
| 7 | `java.lang.security.audit.crypto.ssl.insecure-hostname-verifier.insecure-hostname-verifier` | `tika-core/src/main/java/org/apache/tika/parser/NetworkParser.java` | 178 | WARNING |

Les 58 erreurs d'analyse sont les mêmes avant et après : ce sont des fichiers que Semgrep
n'arrive pas à analyser, sans lien avec les modifications.
