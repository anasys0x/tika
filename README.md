# IFT3913 - Tâche 3

| Nom complet | Identifiant GitHub |
|---|---|
| Anas Mrani Alaoui | Anasys0x |
| Aymane Rachidi | Aymtrack |

## 1. Semgrep dans le workflow

On a créé le workflow [`ift3913-tache3.yml`](.github/workflows/ift3913-tache3.yml) à partir de celui de la tâche 2. Il a deux jobs :

- `semgrep` lance Semgrep 1.144.0 avec les règles `p/java`, avec le script [`ift3913-semgrep.sh`](.github/scripts/ift3913-semgrep.sh) ;
- `tests` lance les tests Maven de la tâche 2. Il a `needs: semgrep`, donc il ne démarre pas si Semgrep échoue.

Pour bloquer seulement les nouvelles vulnérabilités, le script donne `--baseline-commit` à Semgrep : tout ce qui existait déjà dans le commit de référence est ignoré. Pour un push, la référence est le commit d'avant le push. Pour une PR, c'est le dernier commit de la branche cible. Au premier push d'une branche, GitHub donne un SHA nul, donc on prend l'ancêtre commun avec `tache2`.

Les autres choix :

- `--error` : Semgrep renvoie 1 s'il trouve un nouveau signalement, et le job échoue ;
- `--strict` : le job échoue aussi si Semgrep n'arrive pas à analyser un fichier ;
- `fetch-depth: 0` : on récupère tout l'historique pour avoir le commit de référence ;
- la version de Semgrep est fixée pour avoir les mêmes résultats d'un run à l'autre ;
- le rapport JSON est gardé dans l'artefact `semgrep-diff`.

## 2. Validation du workflow

- En local, dans un dépôt de test : un commit qui déplace un signalement existant sans en ajouter donne le code 0, et un commit qui en ajoute 7 donne le code 1 avec ces 7 signalements dans le rapport. Le cas du SHA nul donne le même résultat.
- Sur Tika, le scan complet trouve les mêmes 11 signalements avant et après l'ajout du workflow.
- [Premier run sur GitHub](https://github.com/anasys0x/tika/actions/runs/43934546881) : aucun nouveau signalement, Semgrep passe, puis les tests Maven passent (775 tests, 2 ignorés). Le [rapport `semgrep-diff`](https://github.com/anasys0x/tika/actions/runs/43934546881/artifacts/14259992885) est bien téléversé.
- Run après le push des 7 vulnérabilités : LIEN_DU_RUN_A_AJOUTER

## 3. Rickroll

Si les tests Maven échouent, le script [`ift3913-check-junit.py`](.github/scripts/ift3913-check-junit.py) lit les rapports JUnit de Surefire et Failsafe. S'il trouve un test en échec ou en erreur, le workflow appelle notre action composite [`ift3913-rickroll`](.github/actions/ift3913-rickroll/action.yml), qui met le lien du rickroll dans le résumé du run. On lit les rapports JUnit pour ne pas rickroller quand Maven échoue pour une autre raison, par exemple une erreur de compilation. Le job reste en échec.

On l'a testé en local (pas de rapport, tests réussis, échec d'assertion, erreur Failsafe), puis sur GitHub :

- quand les tests passent, il n'y a pas de rickroll (premier run de la section 2) ;
- dans le [run d'une PR de validation](https://github.com/anasys0x/tika/actions/runs/43947090078) avec un test qui échoue exprès, le rickroll s'affiche. Cette PR n'a pas été fusionnée.

## 4. Les 7 vulnérabilités

Chaque vulnérabilité est dans son propre commit (`Vulnérabilité n/7`), dans une classe qui existe déjà dans Tika. À chaque fois, on a ajouté une nouvelle méthode ou une option désactivée par défaut, pour ne pas casser les tests existants. Le code vulnérable est quand même là, et Semgrep le détecte. On a choisi des règles de `p/java` avec des CWE différents, sans celles qui ont besoin de `HttpServletRequest`, parce que Tika n'est pas une application web.

| # | Type | Fichier | Règle Semgrep | Ligne |
|---|---|---|---|---|
| 1 | Désérialisation non sûre (CWE-502) | `tika-serialization/…/config/loader/JsonMergeUtils.java` | `jackson-unsafe-deserialization` | 112 |
| 2 | XXE (CWE-611) | `tika-core/…/utils/XMLReaderUtils.java` | `documentbuilderfactory-external-general-entities-true` | 281 |
| 3 | Injection SQL (CWE-89) | `tika-core/…/io/TikaInputStream.java` | `formatted-sql-string` | 198 |
| 4 | Transmission en clair (CWE-319) | `tika-core/…/parser/NetworkParser.java` | `unencrypted-socket` | 91 |
| 5 | Hachage faible (CWE-328) | `tika-core/…/digest/InputStreamDigester.java` | `use-of-md5` | 87 |
| 6 | Chiffrement trop faible (CWE-326) | `tika-core/…/parser/CryptoParser.java` | `des-is-deprecated` | 105 |
| 7 | Pas de chiffrement (CWE-327) | `tika-core/…/parser/CryptoParser.java` | `no-null-cipher` | 114 |

Pourquoi ces fichiers, et pourquoi c'est vulnérable :

1. `JsonMergeUtils` lit déjà la config JSON de Tika. La nouvelle méthode `deserializePolymorphic()` active `enableDefaultTyping()`, donc c'est le JSON qui choisit la classe Java créée. Un attaquant peut faire instancier une classe dangereuse.
2. `XMLReaderUtils` crée les parseurs XML de Tika. La nouvelle fabrique accepte les entités externes, donc un XML avec `<!ENTITY x SYSTEM "file:///etc/passwd">` peut lire un fichier du serveur.
3. `TikaInputStream` sait déjà ouvrir un document depuis un `Blob` SQL. `getFromDatabase()` colle la table et l'id directement dans la requête, donc un id comme `x' OR '1'='1` change la requête. Il faudrait un `PreparedStatement`.
4. `NetworkParser` envoie déjà les documents à un serveur distant. Avec `setPlainTextFallbackPort()`, si HTTPS échoue, le document est renvoyé sur un socket sans TLS, donc lisible sur le réseau.
5. `InputStreamDigester` calcule les empreintes des documents. Avec `setMd5Fallback(true)`, si l'algorithme demandé n'existe pas, on calcule un MD5 sans prévenir. MD5 n'est pas résistant aux collisions.
6. `CryptoParser` déchiffre les documents. Avec `setLegacyDesFallback(true)`, il peut se rabattre sur DES, dont la clé de 56 bits se casse par force brute.
7. Encore `CryptoParser`, mais un autre problème : avec `setPassThroughWithoutKey(true)`, un document sans clé passe par un `NullCipher`, qui ne fait rien. Un document pas chiffré est accepté comme s'il avait été déchiffré.

En local, le build Maven passe toujours avec les 7 vulnérabilités (`mvn -B -ntp -pl tika-core,tika-serialization -am clean install -Pci,ift3913-ai,ift3913-manual` : 775 tests dans `tika-core`, 216 dans `tika-serialization`, aucun échec).

## 5. Rapports Semgrep avant et après

On a lancé Semgrep 1.144.0 avec `p/java` sur tout le code Java de Tika, avant (commit `effeeb2`) et après les 7 commits : [avant](semgrep/semgrep-avant.txt), [après](semgrep/semgrep-apres.txt). Les versions JSON sont dans le même dossier.

| Règle | Avant | Après |
|---|---:|---:|
| `jackson-unsafe-deserialization` | 0 | 1 |
| `documentbuilderfactory-external-general-entities-true` | 0 | 1 |
| `formatted-sql-string` | 2 | 3 |
| `unencrypted-socket` | 4 | 5 |
| `use-of-md5` | 0 | 1 |
| `des-is-deprecated` | 0 | 1 |
| `no-null-cipher` | 0 | 1 |
| Cinq autres règles | 5 | 5 |
| Total | 11 | 18 |

Les 11 signalements d'avant sont toujours là, et les 7 nouveaux sont ceux de la section 4. Avec `--baseline-commit effeeb2`, comme dans le workflow, Semgrep garde seulement ces 7 et renvoie 1. Le socket `telnet` qui existait déjà dans `NetworkParser` a changé de ligne (68 avant, 78 après), mais il n'est pas compté comme nouveau, parce que Semgrep compare le code et pas les numéros de ligne.

## 6. Usage de l'IA

Claude (Anthropic) nous a aidés pour cette tâche.

---

La tâche 2 est documentée sur la branche [`tache2`](https://github.com/anasys0x/tika/blob/tache2/README.md).
