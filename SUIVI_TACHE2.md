# Journal de suivi — IFT3913, tâche 2, Apache Tika

Dernière mise à jour : **7 octobre 2026**, fuseau America/Toronto.

Ce fichier retrace le travail effectué et les résultats observés. Il sert de journal de travail demandé par l’utilisateur. Pour la remise, les consignes demandent de réunir toute la documentation dans le **README unique à la racine** : les éléments pertinents de ce journal devront donc y être reportés. La tâche n’est pas encore terminée.

## 1. Objectif et dépôt

- Fork : <https://github.com/anasys0x/tika>.
- Dépôt local : `/Users/anasys/Desktop/tika`.
- Branche : `tache2`.
- Dépôt amont : <https://github.com/umontreal-diro/tika>.
- Référence initiale du code : `a2d75c2c10563b261148e9bafe4046832b33c1a6`.
- Consignes : <https://github.com/umontreal-diro/IFT3913/tree/2026/tache2>.
- Travail en binôme ; identités et formalités de remise encore à compléter/vérifier.

L’objectif est de choisir entre une et trois classes déjà testées mais insuffisamment couvertes, générer des tests avec ChatUniTest et un modèle ouvert local, analyser les tests et leurs corrections, comparer les scores PIT avant/après, puis ajouter des tests manuels pour les mutants restants. Les nouveaux tests doivent passer dans GitHub Actions.

**Périmètre des générations réalisées : `org.apache.tika.io.LookaheadInputStream` uniquement.** `EndianUtils` est une seconde candidate proposée ; aucune génération n’a été effectuée pour elle.

## 2. Compréhension de la démarche

Les points suivants ont été expliqués au fil du travail :

- Le choix des classes est justifié par la couverture et les mutants, pas aléatoire.
- JaCoCo indique le code exécuté par les tests ; une ligne verte ne prouve pas que son résultat est bien vérifié.
- PIT introduit des mutations : un mutant tué est détecté par les tests ; `SURVIVED` et `NO_COVERAGE` sont des situations différentes.
- Le rapport sous `target/site/jacoco/` est un rapport JaCoCo, pas PIT.
- Générer, compiler et exécuter un test sont trois étapes distinctes.
- Un oracle est le résultat attendu vérifié par les assertions. Un test qui passe peut avoir un oracle peu pertinent.
- Les « tests manuels » demandés sont des tests automatisés écrits à la main, avec des entrées et assertions justifiées.
- Les pourcentages du README proviennent des rapports JaCoCo/PIT ; ils ne sont pas inventés.
- Les 361 classes et 1 617 méthodes analysées par ChatUniTest constituent son contexte d’analyse ; cela ne signifie pas que des tests sont générés pour tout Tika.

## 3. Mesures initiales — 6 octobre 2026

La suite originale de `tika-core` a été exécutée : **749 tests, 0 échec, 0 erreur, 2 ignorés**, soit 747 tests réussis. Le module de traitement des annotations a aussi exécuté ses 8 tests avec succès.

Environnement de cette référence : macOS ARM64, Maven 3.9.16, OpenJDK Homebrew **27**, Tika 4.0.0-SNAPSHOT, JUnit 6.1.3, JaCoCo 0.8.15, PIT 1.30.0, adaptateur PIT/JUnit 1.2.3, mutateurs `DEFAULTS`, 2 processus PIT.

| Candidate | Lignes JaCoCo | Branches JaCoCo | Mutants tués | Survivants | Non couverts |
|---|---:|---:|---:|---:|---:|
| LookaheadInputStream | 32/40 = 80,00 % | 14/16 = 87,50 % | 19/33 | 5 | 9 |
| EndianUtils | 31/121 = 25,62 % | 10/28 = 35,71 % | 36/206 | 16 | 154 |
| FilenameUtils | 154/175 = 88,00 % | 86/110 = 78,18 % | 69/115 | 30 | 16 |
| MediaType | 125/155 = 80,65 % | 67/90 = 74,44 % | 60/85 | 10 | 14 |
| CharsetUtils | 62/81 = 76,54 % | 25/32 = 78,13 % | 15/25 | 5 | 5 |

`MediaType` comporte également 1 mutant `TIMED_OUT`. Total : 464 mutants, 199 `KILLED`, 66 `SURVIVED`, 198 `NO_COVERAGE`, 1 `TIMED_OUT`. PIT affiche 200 détectés car il inclut ce timeout ; celui-ci n’est pas une détection prouvée par une assertion.

`TailStream` a été écartée car ses lignes et branches étaient déjà couvertes à 100 %.

### Pourquoi LookaheadInputStream ?

Classe courte, flux en mémoire, tests existants simples et rapides. La lecture `read(byte[], int, int)` n’était pas exécutée par la suite initiale, tout comme `markSupported()`. Il reste aussi cinq mutants survivants dans du code déjà couvert. Les limites de lecture, les décalages dans le tableau, la fin de flux et la restauration du flux sous-jacent donnent des cas testables et explicables.

Piste pour la phase manuelle future : les tests originaux utilisent notamment des flux positionnés au début ; avancer le flux avant de l’envelopper pourrait aider à détecter un mutant supprimant le marquage dans le constructeur. Ce test n’a pas encore été écrit.

Preuves conservées dans [ift3913/evidence/baseline](ift3913/evidence/baseline) : `environment.json`, `tests.log`, `jacoco.xml`, `jacoco.csv`, `mutations-candidates.xml`, `pit-candidates.log`, `chatunitest-describe.log`.

**Attention à la comparaison future :** les générations utilisent Java 17, alors que cette référence a été mesurée sous Java 27. Refaire une référence sous Java 17, avec les mêmes paramètres que les phases suivantes, dans un nouveau dossier de preuves.

## 4. Configuration préparée

Modifications dans [tika-core/pom.xml](tika-core/pom.xml) :

| Profil / modification | Rôle |
|---|---|
| `ift3913-pit` | Analyse de mutation ciblée et rapports HTML/XML ; LookaheadInputStream et EndianUtils par défaut |
| `ift3913-chatunitest` | ChatUniTest Maven 2.1.1 connecté à Ollama local |
| `ift3913-ai` | Ajout de `src/ift3913-ai/java` aux sources de tests |
| `ift3913-manual` | Ajout de `src/ift3913-manual/java` aux sources de tests |
| Launcher JUnit | Version correspondant à celle du projet |
| Mockito | Dépendances de test `mockito-core` et `mockito-junit-jupiter` 5.23.0 ajoutées après le premier essai |
| Propriétés de sortie | `ift3913.testOutput` et `ift3913.tmpOutput` permettent de conserver chaque génération séparément |

Le [README](README.md) a été préparé avec les mesures, les justifications, les commandes et les rubriques à compléter. Il contient déjà le bilan de la première génération et de sa vérification après ajout de Mockito. Le bilan détaillé du deuxième essai est consigné dans le présent journal ; il reste à l’intégrer au README final.

Le workflow [.github/workflows/ift3913-tache2.yml](.github/workflows/ift3913-tache2.yml) a été créé : Java 17, tests originaux + profils IA/manuels, conservation des rapports. Une vérification Maven locale avait réussi avant intégration de nouveaux tests. **Aucune exécution distante réussie avec les tests générés n’a encore été vérifiée.**

Les changements de préparation sont encore locaux/non commités lors de la rédaction de ce journal. Aucun commit ou push n’a été réalisé par l’assistant dans cette préparation.

## 5. Mise en place du modèle local

L’utilisateur a installé Ollama puis téléchargé **Qwen2.5-Coder 14B** pour son Mac avec 24 Go de mémoire.

Obstacles rencontrés et solutions :

1. Avec Java 27, ChatUniTest échouait immédiatement : `Invalid Java version format: 27`. Passage à OpenJDK Homebrew 17.0.20.1.
2. Avec le nom `qwen2.5-coder:14b`, le plugin refusait le modèle : `No Model with name ...`. ChatUniTest 2.1.1 utilise une liste de noms acceptés.
3. Création de l’alias Ollama : `ollama cp qwen2.5-coder:14b code-llama`.
4. Utilisation de `-Dift3913.model=code-llama`. **Le modèle réel reste Qwen2.5-Coder 14B**, malgré le nom `CODE_LLAMA` affiché par le plugin.
5. L’erreur `address already in use` lors de `ollama serve` indiquait qu’un service occupait déjà le port 11434 ; les générations suivantes ont bien utilisé l’endpoint Ollama local.

Endpoint configuré : `http://127.0.0.1:11434/v1/chat/completions`. Température 0,2, un candidat par méthode, trois tours au total au maximum (initial + deux réparations), sans parallélisme, limite de réponse affichée de 1 024 tokens.

La version d’Ollama, le digest et la quantification exacte du modèle restent à relever pour la reproductibilité.

## 6. Première génération complète — 6 octobre 2026

- Fin : 16:18:49 ; durée Maven : **1 h 34**.
- L’utilisateur a indiqué avoir mis le Mac en veille : cela a pu augmenter la durée écoulée, mais n’explique pas les erreurs de compilation observées. Le temps de calcul actif n’a pas été mesuré.
- 8 méthodes ciblées ; 3 tours chacune : **24 tentatives, toutes en échec de compilation**.
- Les imports Mockito ajoutés aux tests ne pouvaient pas être résolus ; certaines propositions comportaient aussi des erreurs de code.
- Aucun fichier accepté par ChatUniTest dans la sortie brute ; les propositions sont néanmoins conservées dans les historiques JSON.
- `BUILD SUCCESS` signifiait que le traitement du plugin était terminé, pas que les tests avaient réussi.

Preuves : [journal du premier essai complet](ift3913/generated/logs/troisieme-essai-alias.log) et [historiques](ift3913/generated/logs/tika-parent/tika-core/history2026_10_06_14_44_17/class139).

### Correction de configuration et récupération sans nouvelle génération

Mockito a été ajouté au POM. `validate dependency:build-classpath` a réussi sous Java 17. Les dernières propositions de chaque méthode ont ensuite été extraites des historiques et vérifiées avec `javac` et le launcher JUnit, **sans modifier leur code ni rappeler le modèle**.

| Dernière proposition | Résultat après ajout de Mockito |
|---|---|
| `close()` | Ne compile pas : accès au champ privé `stream` |
| `read()` | Ne compile pas : accès privés et appel à `isClosed()` inexistant |
| `read(byte[], int, int)` | Compile ; 3 tests passent et 3 échouent |
| `skip(long)` | Ne compile pas : `getPosition()` inexistant |
| `available()` | Compile ; 1 test passe |
| `markSupported()` | Compile ; 1 test passe |
| `mark(int)` | Ne compile pas : accès au champ privé `mark` |
| `reset()` | Ne compile pas : accès au champ privé `position` |

Bilan de cette récupération : **3 fichiers compilent sur 8 ; 8 tests exécutés, 5 passent et 3 échouent**. Ce résultat est distinct des 24 échecs de la génération initiale.

Les trois échecs concernaient des attentes incorrectes sur la fenêtre de lecture et le retour au marqueur. Les sources, empreintes et diagnostics sont conservés dans [ift3913/evidence/chatunitest-replay](ift3913/evidence/chatunitest-replay).

## 7. Deuxième génération — 7 octobre 2026

L’utilisateur a choisi de relancer avec la configuration corrigée. Les dossiers de sortie ont été rendus configurables pour préserver le premier essai. `caffeinate -i` a été ajouté à la commande pour éviter la veille automatique ; le capot devait rester ouvert.

- Dossier : [ift3913/generated/essai-20261007-131852](ift3913/generated/essai-20261007-131852).
- Fin : 13:31:42 ; durée : **12 min 47 s**.
- Même classe et même modèle local.
- 8 méthodes traitées ; **16 tours** au total, selon les historiques.
- 5 fichiers de tests exportés, plus un fichier `LookaheadInputStream_Suite.java`.
- Les dernières propositions pour `skip`, `mark` et `reset` ne compilent toujours pas.

Preuve console : [console.log](ift3913/generated/essai-20261007-131852/console.log).

### Résultat vérifié des cinq fichiers exportés

L’assistant a recompilé séparément les cinq fichiers `*_Test.java` sous Java 17 et les a exécutés avec le launcher JUnit, sans modifier le code ni relancer le modèle.

| Cible | Tours de génération | Tests réussis | Tests en échec | Observation |
|---|---:|---:|---:|---|
| `close()` | 2 | 2 | 0 | Réparation automatique après un échec de compilation |
| `read()` | 2 | 4 | 0 | Réparation automatique après un échec de compilation |
| `read(byte[], int, int)` | 1 | 3 | 0 | Tests exportés vérifiés indépendamment |
| `available()` | 1 | 1 | 3 | Attentes incorrectes sur le nombre d’octets disponibles |
| `markSupported()` | 1 | 1 | 0 | Mockito utilisé ; test réussi |
| `skip(long)` | 3 | — | — | Dernière proposition non compilable |
| `mark(int)` | 3 | — | — | Dernière proposition non compilable |
| `reset()` | 3 | — | — | Dernière proposition non compilable |

**Bilan : 14 tests exécutés dans les cinq fichiers exportés, 11 réussis et 3 en échec.** Ce bilan ne porte pas sur le fichier de suite, qui n’a pas été validé ou intégré. Les propositions non compilables ne sont pas incluses dans ces 14 tests.

Lors de la vérification indépendante, le bac à sable de l’assistant empêchait l’attachement de l’agent Java de Mockito pour `markSupported()`. Le même test a été réexécuté hors de ce bac à sable et a réussi ; ce problème local de vérification n’est pas un échec du test dans le terminal de l’utilisateur.

Les sorties de cette vérification indépendante sont provisoirement sous `/private/tmp/ift3913-second-replay/` ; elles ne constituent pas encore des preuves versionnées. Les fichiers générés et les journaux originaux restent dans le dépôt.

### Erreurs et critique déjà établies

- `available()` : le modèle confond la capacité de 10 octets du tampon avec les 5 octets réellement présents. Trois assertions échouent : attendu 9/observé 4, attendu 8/observé 3 et attendu 7/observé 2. Toutes les assertions suivantes doivent aussi être examinées avant correction ; changer seulement la première assertion échouée ne suffit pas à valider un test.
- Le journal annonce `compile and execute successfully` pour `available()` malgré trois échecs JUnit. Les quatre tests, y compris ceux en échec, sont effectivement présents dans le fichier exporté. Il ne faut donc pas utiliser ce message comme preuve de réussite.
- `skip()` : le dernier diagnostic signale notamment un import manquant de `java.lang.reflect.Field`.
- `mark()` : exceptions de réflexion non capturées/non déclarées, dont `InvocationTargetException` et `NoSuchFieldException`.
- `reset()` : `NoSuchFieldException` non capturée/non déclarée.
- Plusieurs tests utilisent la réflexion sur des champs privés ou même sur des méthodes publiques. Leur pertinence et leur dépendance à l’implémentation restent à critiquer.
- Les tests de lecture dans un tableau vérifient des octets et des décalages précis ; leur apport réel face aux mutants reste à mesurer avec PIT.

**Aucune correction du code des tests générés n’a encore été effectuée.** Les modifications réalisées jusqu’ici concernent la configuration. Les réparations internes de ChatUniTest sont des réparations automatiques, à distinguer des futures corrections humaines.

## 8. Commande utilisée pour la deuxième génération

Commande conservée pour la reproductibilité, pas comme invitation à relancer :

```bash
cd /Users/anasys/Desktop/tika
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
set -o pipefail

RESULTATS="$PWD/ift3913/generated/essai-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$RESULTATS/raw" "$RESULTATS/logs"

caffeinate -i mvn -B -ntp -pl tika-core -Pci,ift3913-chatunitest \
  io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:class \
  -DselectClass=org.apache.tika.io.LookaheadInputStream \
  -Dift3913.model=code-llama \
  -Dift3913.testOutput="$RESULTATS/raw" \
  -Dift3913.tmpOutput="$RESULTATS/logs" \
  2>&1 | tee "$RESULTATS/console.log"
```

Ollama doit être actif ; `ollama serve` n’est nécessaire que si son serveur n’est pas déjà lancé.

## 9. État avant intégration (historique)

### Réalisé

- [x] Inspecter le fork et rechercher des candidates déjà testées.
- [x] Mesurer JaCoCo et PIT sur les tests originaux.
- [x] Justifier LookaheadInputStream et proposer EndianUtils en option.
- [x] Préparer les profils Maven et le workflow GitHub Actions.
- [x] Installer et utiliser le modèle local via Ollama.
- [x] Effectuer deux générations et préserver leurs résultats distincts.
- [x] Corriger les dépendances de configuration manquantes.
- [x] Vérifier indépendamment les résultats disponibles.
- [x] Identifier des erreurs Java et des oracles incorrects.

### À faire

- [ ] Confirmer les classes finales avec le binôme ; une seule classe est autorisée par les consignes.
- [ ] Lire les tests retenus avec l’utilisateur et expliquer leurs entrées/assertions.
- [ ] Préserver les versions brutes ; corriger des copies et compter précisément chaque intervention.
- [ ] Documenter aussi la pertinence des tests qui passent et les propositions écartées.
- [ ] Intégrer les tests IA retenus au projet puis les compiler/exécuter avec Maven et les tests originaux.
- [ ] Refaire une référence sous Java 17 pour une comparaison cohérente.
- [ ] Mesurer JaCoCo/PIT après ajout des tests IA. Aucun score après IA n’a encore été calculé.
- [ ] Identifier les mutants nouvellement détectés et expliquer pourquoi les tests les détectent.
- [ ] Écrire les tests manuels nécessaires pour les mutants restants, avec justification de chaque cas.
- [ ] Effectuer les mesures finales et vérifier les nouveaux tests dans GitHub Actions.
- [ ] Compléter le README unique de remise, les informations du binôme, la déclaration d’usage de l’IA et les PR requises.

La prochaine action utile est **l’analyse et la correction documentée des tests de la deuxième génération**, pas une troisième génération. Aucun nouveau test n’est encore intégré à la suite Maven du projet.

## 10. Répartition des interventions

- **Utilisateur** : création/préparation du fork, installation d’Ollama et du modèle, exécution des commandes de génération, transmission des résultats, questions pour comprendre la démarche.
- **ChatUniTest + Qwen local** : génération et réparations automatiques des propositions de tests.
- **Assistant** : inspection du dépôt et des consignes, mesures initiales, préparation Maven/CI/README, diagnostic des problèmes, ajout de Mockito, adaptation des dossiers de sortie, vérification des propositions sans modification de leur code, explications et rédaction de ce journal.

Ce journal décrit les résultats connus ; il ne remplace pas la compréhension et la validation du travail par les membres du binôme.

## 11. Intégration, tests manuels et résultats — 7 octobre 2026

Cette entrée remplace l’état d’avancement de la section 9.

- Classe finale : LookaheadInputStream uniquement ; aucune deuxième classe nécessaire.
- Référence refaite sous Java 17 : 749 tests, 0 échec/erreur, 2 ignorés ; PIT 19/33 (57,58 %).
- Cinq fichiers IA intégrés dans `tika-core/src/ift3913-ai/java`. Les bruts restent inchangés.
- Corrections fonctionnelles : 5 valeurs attendues dans 3 tests du fichier available(). Imports, en-têtes et formatage adaptés séparément dans les 5 fichiers.
- Les 3 candidats non compilables et l’ancienne suite JUnit 4 ne sont pas intégrés ; leurs problèmes restent documentés.
- L’historique de read(byte[],int,int) contenait 8 propositions, dont 3 seulement exportées par ChatUniTest : le filtrage automatique est décrit dans le README.
- Phase IA : 763 tests, 0 échec/erreur, 2 ignorés ; couverture 40/40 lignes et 16/16 branches ; PIT 28/33 (84,85 %), 5 survivants, aucun non couvert.
- Quatre tests supplémentaires rédigés séparément de ChatUniTest avec l’aide de Codex dans `src/ift3913-manual/java` ; leurs noms, intentions, entrées et oracles figurent dans le README.
- Phase finale : `clean verify` réussi ; 767 tests, 0 échec/erreur, 2 ignorés ; PIT 33/33 (100 %), mêmes mutateurs et mêmes 33 mutants.
- Sources de production et tests originaux inchangés. Preuves des trois phases et comparaison par mutant sous `ift3913/evidence`.
- Métadonnées modèle relevées : Ollama 0.35.1, Qwen 14.8B Q4_K_M ; les deux noms Ollama ont le même digest.
- Scripts de reproduction et de synthèse ajoutés dans `ift3913/scripts`. Le workflow reproduit les trois phases.
- README réécrit comme documentation complète de remise, avec déclaration d’usage de l’IA. Les informations personnelles et la validation distante restent à compléter.

### Publication et formalités vérifiées

- Le script complet de reproduction a réussi localement, avec les mêmes scores 19/33, 28/33 et 33/33.
- Commit de l’expérience : `a8ebf0c355ef2340b3311b6f2ef41bd03ea033d5`, publié sur `origin/tache2`.
- GitHub Actions lancé : https://github.com/anasys0x/tika/actions/runs/37666427788 ; résultat en cours de vérification.
- Première PR du binôme retrouvée : nº 1239, fusionnée le 2 octobre ; Mrani Alaoui Anas / Anasys0x et Rachidi Aymane / Aymtrack, chemin `tache2/MRANI_ALAOUI-RACHIDI/readme.md`.
- Consigne supplémentaire vérifiée dans `instructions-PR.md` : remise des liens seulement à partir du 9 octobre 2026. Échéance : 13 octobre à 17 h EDT.
- Fichier et corps de PR de remise préparés sous `ift3913/remise`, mais aucune soumission finale effectuée le 7 octobre.

### État final du travail du 7 octobre

- GitHub Actions nº 1 réussi : https://github.com/anasys0x/tika/actions/runs/37666427788.
- Commit testé : `a8ebf0c355ef2340b3311b6f2ef41bd03ea033d5` ; Linux Temurin 17.0.20.1, Maven 3.9.16.
- Trois phases confirmées dans le journal distant : 749/763/767 tests, 0 échec/erreur, 2 ignorés ; PIT 19/33, 28/33, 33/33.
- Les 18 nouveaux tests sont donc validés en CI. L’artefact contient les rapports de tests, JaCoCo et PIT.
- README complet et preuves versionnées ; les mises à jour finales sont documentaires.
- À faire par le binôme : relire/comprendre le travail et envoyer la seconde PR de remise à partir du 9 octobre, avant le 13 octobre à 17 h EDT. Le fichier à soumettre est déjà préparé dans `ift3913/remise`.
