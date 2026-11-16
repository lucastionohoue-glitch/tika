# Tâche 3 : analyse statique avec Semgrep

## 0. État de départ

- Commit de départ (fin de la tâche 2) : `bbf47f0b2`
- Rapport de référence : `semgrep-avant.json` (Semgrep 1.183.0, configs `p/java` + `p/security-audit`)
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

### Validation

| # | Commit | Changement | Résultat attendu | Résultat obtenu | Run |
|---|--------|------------|------------------|-----------------|-----|
| 1 | `TODO` | Commit propre (ajout du workflow + cette doc) | ✅ | TODO | TODO |
| 2 | `TODO` | Ajout de `Tache3Vulnerable.java` (MD5 + DES) | ❌ | TODO | TODO |
| 3 | `TODO` | Suppression de `Tache3Vulnerable.java` | ✅ | TODO | TODO |
| 4 | `TODO` | Modification de `MimeTypesReader.java`, déjà vulnérable (`saxparserfactory-disallow-doctype-decl-missing`), sans nouvelle vulnérabilité | ✅ | TODO | TODO |

Captures : TODO
