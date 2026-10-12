# PIT – Run 3, premier essai (remplacé par ../run3)

162 mutants tués sur 200. `MediaTypeManualTest.testParseCachesSimpleTypes` utilisait alors le nom fixe
`application/x-ift3913-manual`. PIT redéfinit la classe mutée dans une JVM déjà démarrée, sans réinitialiser le cache
statique `SIMPLE_TYPES`. Le type était donc déjà en cache, et `assertSame` passait même avec les mutants 260 et 293.
Après le passage à un nom aléatoire (UUID), ces deux mutants sont tués (run 3 : 164).
