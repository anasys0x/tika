# IFT3913 — Tâche 2 : tests de LookaheadInputStream dans Apache Tika

Dernière mise à jour : **7 octobre 2026**. Classe retenue : `org.apache.tika.io.LookaheadInputStream`, module autorisé `tika-core`. Les phases locales sont terminées ; les informations du binôme et la validation GitHub Actions doivent encore être complétées.

| Nom complet | Identifiant GitHub |
|---|---|
| À compléter par l’utilisateur | anasys0x |
| À compléter par le binôme | À compléter |

- [Fork](https://github.com/anasys0x/tika), branche `tache2`.
- [README de remise sur cette branche](https://github.com/anasys0x/tika/blob/tache2/README.md).
- Code de production initial : `a2d75c2c10563b261148e9bafe4046832b33c1a6`. Le code de production et les tests originaux n’ont pas été modifiés.
- [Consignes officielles](https://github.com/umontreal-diro/IFT3913/tree/2026/tache2), [modèle de remise](https://github.com/umontreal-diro/IFT3913/blob/2026/.github/PULL_REQUEST_TEMPLATE/tache2-readme.md), [plan du cours et échéances](https://github.com/umontreal-diro/IFT3913/tree/2026).

Toute l’analyse destinée à la remise est rassemblée ici. Le fichier `SUIVI_TACHE2.md`, demandé comme journal de travail, suit l’avancement ; les journaux, sources et XML liés ci-dessous sont des preuves.

## 1. Résultat et périmètre de l’expérience

| Phase | Tests core (dont ignorés) | Lignes JaCoCo | Branches JaCoCo | Mutants tués / total | Score | Survivants | Non couverts |
|---|---:|---:|---:|---:|---:|---:|---:|
| Originaux seuls, Java 17 | 749 (2 ignorés) | 32/40 | 14/16 | 19/33 | 57.58 % | 5 | 9 |
| Originaux + IA corrigés | 763 (2 ignorés) | 40/40 | 16/16 | 28/33 | 84.85 % | 5 | 0 |
| Originaux + IA + manuels | 767 (2 ignorés) | 40/40 | 16/16 | 33/33 | 100.00 % | 0 | 0 |

Score de mutation strict : `100 × KILLED / tous les mutants générés`, en incluant les mutants non couverts dans le dénominateur. Les trois analyses contrôlées produisent les **mêmes 33 mutants**, avec zéro timeout ou erreur. Le score affiché par PIT coïncide ici avec le score strict.

Les tests IA apportent **9 détections nouvelles**, et les tests manuels les **5 restantes**. La couverture complète des lignes et branches après IA ne suffisait donc pas à détecter tous les mutants. Le résultat de 100 % porte sur cette classe et les mutateurs `DEFAULTS`, pas sur tous les défauts possibles ni sur tout Tika.

Preuves : [synthèse structurée](ift3913/evidence/summary.json), [référence Java 17](ift3913/evidence/baseline-java17), [phase IA](ift3913/evidence/ai), [phase finale](ift3913/evidence/final). Chaque phase conserve JaCoCo, le rapport PIT HTML/XML et le journal Maven. Les rapports Surefire IA/final prouvent la présence et le résultat des nouveaux tests. Les 8 tests de `tika-annotation-processor` réussissent également, séparément des totaux de `tika-core`.

## 2. Choix de la classe et exploration des candidates

La sélection est fondée sur des tests existants, une couverture incomplète et des mutants non détectés. Une seule classe suffit aux consignes ; nous retenons `LookaheadInputStream` pour maîtriser l’analyse. Les cinq candidates suivantes ont été mesurées avec toute la suite originale de `tika-core` :

| Candidate | Test existant | Lignes | Branches | KILLED / total | SURVIVED | NO_COVERAGE |
|---|---|---:|---:|---:|---:|---:|
| LookaheadInputStream | LookaheadInputStreamTest | 32/40 = 80,00 % | 14/16 = 87,50 % | 19/33 | 5 | 9 |
| EndianUtils | EndianUtilsTest | 31/121 = 25,62 % | 10/28 = 35,71 % | 36/206 | 16 | 154 |
| FilenameUtils | FilenameUtilsTest | 154/175 = 88,00 % | 86/110 = 78,18 % | 69/115 | 30 | 16 |
| MediaType | MediaTypeTest | 125/155 = 80,65 % | 67/90 = 74,44 % | 60/85 | 10 | 14 |
| CharsetUtils | CharsetUtilsTest | 62/81 = 76,54 % | 25/32 = 78,13 % | 15/25 | 5 | 5 |

`MediaType` a en plus 1 `TIMED_OUT`. Total exploratoire : 464 mutants, 199 `KILLED`, 66 `SURVIVED`, 198 `NO_COVERAGE`, 1 `TIMED_OUT` ; PIT compte 200 détectés en incluant ce timeout. `TailStream` a été écartée car ses 61 lignes et 22 branches étaient couvertes à 100 %.

Cette exploration du 6 octobre utilisait Java 27. Les preuves sont conservées dans [evidence/baseline](ift3913/evidence/baseline). **La comparaison finale utilise une nouvelle référence entièrement mesurée sous Java 17**, dans un dossier distinct. La référence LookaheadInputStream a confirmé les mêmes compteurs initiaux.

### Justification de LookaheadInputStream

La [classe](tika-core/src/main/java/org/apache/tika/io/LookaheadInputStream.java) comporte 142 lignes de source, commentaires compris, et se teste avec des flux en mémoire. Ses [six tests originaux](tika-core/src/test/java/org/apache/tika/io/LookaheadInputStreamTest.java) couvrent notamment un flux nul/vide, une fenêtre limitée, le marquage, la remise à zéro et le saut.

Avant ajout, `read(byte[], int, int)` n’était pas exécutée (7 lignes et 2 branches), pas davantage `markSupported()`. Cinq mutants survivaient : marquage dans le constructeur, frontière de remplissage, calcul de longueur et fermeture à la fin du flux. Cela fournit des comportements testables et des oracles concrets, sans fichier externe ou OCR.

`EndianUtils` était la seconde recommandation possible : conversions numériques simples, couverture faible et nombreux mutants. Elle n’a pas été retenue dans le périmètre final ; aucune génération ni amélioration n’est revendiquée pour elle. Les autres candidates demandent davantage de contexte sur les métadonnées, MIME ou ICU.

## 3. Maven, modèle local et reproductibilité

Comparaison contrôlée : Java Homebrew **17.0.20.1**, Maven **3.9.16**, Tika **4.0.0-SNAPSHOT**, JUnit Jupiter **6.1.3**, JaCoCo **0.8.15**, PIT **1.30.0**, adaptateur PIT/JUnit **1.2.3**, mutateurs `DEFAULTS`, 2 processus, `timeoutConstant=10000`. Le profil `fast`, qui ignore les tests, n’a pas servi aux mesures.

Modèle : **Qwen2.5-Coder 14B**, ouvert et exécuté localement avec **Ollama 0.35.1**, sur macOS ARM64 avec 24 Go de mémoire. Les métadonnées indiquent 14.8B paramètres, GGUF, quantification **Q4_K_M**, taille 8 988 124 298 octets. Digest : `9ec8897f747e246e970bc5cfdda85d22f1123dc2e3d34978a010a75968716849`. [Métadonnées conservées](ift3913/evidence/ollama-environment.json).

ChatUniTest 2.1.1 refuse le nom natif `qwen2.5-coder:14b` et accepte une liste de noms fixes. L’alias local `code-llama` créé avec `ollama cp` a **le même digest** que Qwen : il ne change pas le modèle réel. L’URL est `http://127.0.0.1:11434/v1/chat/completions`, la clé factice `ollama`. La génération n’utilise pas une API de modèle distant.

Paramètres : température 0,2, un candidat par méthode, 3 tours **au total** au maximum (initial + deux réparations), exécution activée, arrêt après succès, sans parallélisme. Le journal affiche `MaxPromptTokens=10923`, `MaxResponseTokens=1024`. Une relance peut produire des propositions différentes.

Profils préparés dans [tika-core/pom.xml](tika-core/pom.xml) :

| Profil | Fonction |
|---|---|
| `ift3913-chatunitest` | Plugin ChatUniTest 2.1.1 et launcher JUnit correspondant au projet ; génération locale |
| `ift3913-pit` | PIT, classe LookaheadInputStream, rapports HTML/XML séparés par phase |
| `ift3913-ai` | Ajoute `src/ift3913-ai/java` aux sources de tests, avec les originaux |
| `ift3913-manual` | Ajoute `src/ift3913-manual/java` aux sources de tests, avec les originaux et IA |

Les dépendances de test Mockito 5.23.0 ont été ajoutées après le premier essai. Le starter ChatUniTest 1.4.0, qui apporte une ancienne pile JUnit/Mockito, n’a pas été ajouté tel quel. Les règles Spotless du dépôt incluent désormais les deux dossiers supplémentaires. Les sources brutes ne sont ni formatées ni modifiées par ce contrôle.

## 4. Générations, validation sans intervention et corrections

### Premier essai du 6 octobre

Les deux tentatives préliminaires ont échoué avant génération : Java 27 (`Invalid Java version format: 27`), puis modèle non accepté. Après passage à Java 17 et création de l’alias, une génération complète a traité 8 méthodes. Durée affichée : **1 h 34**, fin à 16:18:49. L’utilisateur ayant mis le Mac en veille, cette durée n’est pas un temps de calcul actif mesuré.

**24 tours, 24 échecs de compilation, aucun fichier accepté par le plugin.** Mockito était absent de la configuration ; certaines propositions avaient aussi des erreurs Java. `BUILD SUCCESS` signifiait la fin du traitement, pas la réussite des tests. [Journal](ift3913/generated/logs/troisieme-essai-alias.log), [propositions et prompts](ift3913/generated/logs/tika-parent/tika-core/history2026_10_06_14_44_17/class139).

Après ajout de Mockito, les dernières propositions ont été compilées/exécutées sans modification de leur code ni rappel du modèle : 3 fichiers compilables, 8 tests exécutés, 5 réussis et 3 en échec. Cinq autres fichiers accédaient à des champs privés ou appelaient des méthodes inexistantes. [Preuves de cette récupération](ift3913/evidence/chatunitest-replay/results.json). Ces résultats ne remplacent pas les 24 échecs initiaux.

### Deuxième essai du 7 octobre : expérience retenue

Durée **12 min 47 s**, fin à 13:31:42. Dossiers distincts, Java 17, mêmes paramètres et modèle, `caffeinate -i` pour éviter la veille automatique. Le plugin analyse 361 classes et 1 617 méthodes pour son contexte, mais génère uniquement pour LookaheadInputStream.

8 méthodes traitées, **16 tours**, soit 8 générations initiales et 8 tentatives de réparation automatique. Le plugin exporte **5 fichiers de tests + 1 fichier de suite**. Les cinq fichiers de tests bruts compilent avec la configuration corrigée : **14 tests exportés, 11 réussis et 3 en échec**, avant modification de leur code.

| Cible / fichier brut | Tours | Compilation du dernier candidat | Résultat des tests exportés | Décision |
|---|---:|---|---|---|
| `close` / `close_0_0_Test` | 2 | Oui | 2 réussis | Retenu, imports/en-tête adaptés |
| `read()` / `read_2_0_Test` | 2 | Oui | 4 réussis | Retenu, imports/en-tête adaptés |
| `read(byte[],int,int)` / `read_3_0_Test` | 1 | Oui | 3 réussis exportés | Retenu ; filtrage automatique décrit ci-dessous |
| `available` / `available_5_0_Test` | 1 | Oui | 1 réussi, 3 en échec | Retenu après 5 corrections d’attentes |
| `markSupported` / `markSupported_6_0_Test` | 1 | Oui | 1 réussi | Retenu, imports/en-tête adaptés |
| `skip` / `skip_4_0_Test` | 3 | Non | Non exécuté | Non intégré ; import de `Field` notamment manquant |
| `mark` / `mark_7_0_Test` | 3 | Non | Non exécuté | Non intégré ; exceptions de réflexion non déclarées |
| `reset` / `reset_8_0_Test` | 3 | Non | Non exécuté | Non intégré ; `NoSuchFieldException` non déclarée |

[Fichiers exportés originaux](ift3913/generated/essai-20261007-131852/raw/tika-parent/tika-core/org/apache/tika/io), [journal console](ift3913/generated/essai-20261007-131852/console.log), [prompts/réponses et tours](ift3913/generated/essai-20261007-131852/logs/tika-parent/tika-core/history2026_10_07_13_18_55/class139), [erreurs de compilation](ift3913/generated/essai-20261007-131852/logs/tika-parent/tika-core/error-message), [relecture indépendante des fichiers bruts](ift3913/evidence/second-generation-replay).

Le test `markSupported` a aussi été confirmé hors du bac à sable de l’assistant, qui empêchait l’attachement de l’agent Mockito. Ce problème de vérification est distinct du résultat réussi dans le terminal utilisateur. Le journal du plugin annonce un succès pour `available()` malgré 3 échecs JUnit : ses messages de succès ne suffisent donc pas.

**Filtrage automatique à ne pas masquer :** le `records.json` de la lecture dans un tableau contient 8 tests, mais le fichier exporté seulement 3. Les 5 absents incluent une comparaison d’un tableau de taille 3 avec un attendu de taille 2, une exception attendue incorrecte pour une longueur négative, une séquence qui saute un octet dans l’attendu, et deux demandes dépassant les bornes du tableau de destination. Nous avons conservé l’historique de ces propositions ; leur absence est un filtrage de ChatUniTest, pas une correction humaine de notre part. Seuls les 3 tests effectivement exportés sont intégrés.

Le fichier `LookaheadInputStream_Suite.java` utilise un ancien runner JUnit 4/Platform. Il est écarté de l’intégration ; Surefire et PIT découvrent directement les cinq classes Jupiter `*Test`. Les trois candidats non compilables sont également conservés comme observations, sans les présenter comme des tests exécutés ou corrigés.

### Corrections apportées aux copies intégrées

Les versions brutes sont conservées. Les [copies intégrées](tika-core/src/ift3913-ai/java/org/apache/tika/io) gardent les noms et les méthodes exportés.

| Test | Attente initiale | Correction | Justification |
|---|---:|---:|---|
| `testAvailable`, après la première lecture | 9 | 4 | 5 octets réellement chargés, 1 consommé |
| `testAvailable`, après reset puis lecture | 9 | 4 | Retour au marqueur 0, puis consommation d’un octet |
| `testAvailableAfterMark`, après deux lectures | 8 | 3 | 5 octets chargés moins 2 consommés |
| `testAvailableAfterMark`, après reset | 9 | 4 | Marqueur à la position 1, donc 4 octets restants |
| `testAvailableAfterSkip` | 7 | 2 | 1 octet lu et 2 sautés sur 5 |

**Décompte fonctionnel : 5 assertions corrigées, dans 3 tests d’un seul fichier.** Aucune autre assertion, entrée, méthode ou comportement n’a été changé dans les quatre autres fichiers retenus. Les assertions après la première erreur ont aussi été examinées, car le premier échec les masquait.

**Adaptation technique, décomptée séparément :** sur chacun des 5 fichiers, ajout de l’en-tête Apache, remplacement des imports inutilisés/génériques par des imports explicites, puis normalisation du formatage. Cela représente 3 catégories d’adaptation dans 5 fichiers, sans modification d’oracle. Les 2 dépendances Mockito et les changements de POM sont des corrections de configuration, pas des réparations de code généré.

[Manifest avec empreintes brutes et intégrées](ift3913/evidence/ai-corrections/manifest.json) et [diffs exacts](ift3913/evidence/ai-corrections). Les 14 tests corrigés ont ensuite été exécutés avec les originaux : 763 tests, 0 échec, 0 erreur, 2 ignorés.

## 5. Explication et critique des 14 tests IA retenus

| Test exporté | Entrées et oracle | Pertinence et limites |
|---|---|---|
| `close.testClose` | Flux `[1,2,3]`, fermeture, champ privé `stream` attendu nul | Vérifie le détachement interne, mais pas directement la restauration du flux ; réflexion couplée à l’implémentation |
| `close.testCloseWithNullStream` | Champ `stream` forcé à null par réflexion, fermeture, toujours null | Branche sans flux ; état construit artificiellement plutôt qu’avec le constructeur public |
| `read_2.testRead` | `[1,2,3]`, lectures attendues 1, 2, 3, -1 | Oracle précis sur ordre/fin de flux ; proche du test original ; réflexion inutile sur une méthode publique |
| `read_2.testReadWithEmptyStream` | Flux vide, résultat -1 | Cas pertinent mais déjà testé à la main dans la suite originale |
| `read_2.testReadWithSingleByteStream` | `[1]`, fenêtre 3, résultats 1 puis -1 | Source plus courte que la fenêtre : détecte le mutant du calcul de longueur de `fill` |
| `read_2.testReadWithMultipleReads` | `[1,2,3]`, puis deux résultats -1 | Vérifie la stabilité de la fin de flux ; recouvre largement `testRead` |
| `read_3.testReadWithBufferFull` | 5 octets vers tableau de taille 5 : nombre 5, contenu exact, puis -1 | Couvre la surcharge auparavant absente et détecte plusieurs mutations de copie/retour/position |
| `read_3.testReadWithZeroLength` | Demande de longueur 0 sur un flux non vide : retour 0 et tableau inchangé | Cas limite précis ; ne couvre pas toutes les situations de longueur nulle, notamment sur un flux vide |
| `read_3.testReadWithOffset` | Copies aux indices 2 puis 3, contenu complet attendu à chaque étape | Vérifie les bornes des écritures et l’ordre ; demandes exactement égales aux données restantes |
| `available.testAvailable` | Compteur initial 0, première lecture, épuisement, reset et nouvelle lecture | Oracle corrigé fondé sur les octets réellement chargés ; état initial 0 reflète le remplissage paresseux |
| `available.testAvailableAfterMark` | Lecture, marqueur à 1, autre lecture, reset : 3 puis 4 disponibles | Vérifie le lien position/marqueur ; attentes initiales confondaient taille du tampon et données |
| `available.testAvailableAfterSkip` | Lecture puis saut de 2 : 2 octets disponibles | Vérifie le compteur après consommation ; corrigé à partir de données connues |
| `available.testAvailableAfterClose` | Fermeture avant tout remplissage : 0 disponible | Cas limité : ne prouve pas le comportement de fermeture après remplissage |
| `markSupported.testMarkSupported` | Flux Mockito, `markSupported()` attendu vrai | Assertion simple mais pertinente : elle détecte précisément le mutant retournant false ; simulation plus lourde qu’un flux mémoire |

Comparaison aux tests écrits à la main : les originaux vérifient directement la restauration et la lecture du flux sous-jacent par l’API publique. Les propositions IA utilisent parfois la réflexion et plusieurs tests sont redondants. Les nouveaux tests manuels ci-dessous choisissent des entrées à partir des mutants survivants et vérifient des valeurs observables, sans réflexion ni champs privés. Le nombre de tests ou une compilation réussie ne suffit donc pas à juger la qualité d’un oracle.

## 6. Analyse PIT avant/après IA

Le fichier de production a la même empreinte dans les trois phases ; les clés de comparaison sont classe, méthode, signature JVM, index d’instruction et mutateur. Le [script de synthèse](ift3913/scripts/summarize.py) vérifie que les trois ensembles de mutants coïncident. Les numéros M1–M33 ci-dessous suivent l’ordre XML de la référence Java 17.

Statuts : `K` = KILLED, `S` = SURVIVED, `NC` = NO_COVERAGE. Les détecteurs sont ceux enregistrés par PIT dans `killingTest` ; un autre test pourrait aussi détecter le même mutant.

| ID | Méthode, ligne | Mutation | Originaux | + IA | + manuels | Détecteur de la nouvelle détection |
|---|---|---|---|---|---|---|
| M1 | `<init>`, 67 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | S | S | K | `LookaheadInputStreamManualTest.testCloseRestoresNonzeroInitialPosition()` |
| M2 | `<init>`, 68 | removed call to java/io/InputStream::mark (`VoidMethodCallMutator`) | S | S | K | `LookaheadInputStreamManualTest.testCloseRestoresNonzeroInitialPosition()` |
| M3 | `available`, 124 | Replaced integer subtraction with addition (`MathMutator`) | K | K | K | `—` |
| M4 | `available`, 124 | replaced int return with 0 for org/apache/tika/io/LookaheadInputStream::available (`PrimitiveReturnsMutator`) | K | K | K | `—` |
| M5 | `close`, 74 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | K | K | K | `—` |
| M6 | `close`, 75 | removed call to java/io/InputStream::reset (`VoidMethodCallMutator`) | K | K | K | `—` |
| M7 | `fill`, 81 | changed conditional boundary (`ConditionalsBoundaryMutator`) | S | S | K | `LookaheadInputStreamManualTest.testFullWindowDoesNotResetSourceBeforeClose()` |
| M8 | `fill`, 82 | Replaced integer subtraction with addition (`MathMutator`) | S | K | K | `LookaheadInputStream_read_2_0_Test.testReadWithSingleByteStream()` |
| M9 | `fill`, 84 | Replaced integer addition with subtraction (`MathMutator`) | K | K | K | `—` |
| M10 | `fill`, 81 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | K | K | K | `—` |
| M11 | `fill`, 81 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | K | K | K | `—` |
| M12 | `fill`, 83 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | K | K | K | `—` |
| M13 | `fill`, 81 | removed conditional - replaced comparison check with false (`RemoveConditionalMutator_ORDER_ELSE`) | K | K | K | `—` |
| M14 | `fill`, 86 | removed call to org/apache/tika/io/LookaheadInputStream::close (`VoidMethodCallMutator`) | S | S | K | `LookaheadInputStreamManualTest.testSourceEndRestoresUnderlyingPositionAutomatically()` |
| M15 | `markSupported`, 129 | replaced boolean return with false for org/apache/tika/io/LookaheadInputStream::markSupported (`BooleanFalseReturnValsMutator`) | NC | K | K | `LookaheadInputStream_markSupported_6_0_Test.testMarkSupported()` |
| M16 | `read()`, 94 | changed conditional boundary (`ConditionalsBoundaryMutator`) | K | K | K | `—` |
| M17 | `read()`, 95 | Replaced integer addition with subtraction (`MathMutator`) | K | K | K | `—` |
| M18 | `read()`, 95 | Replaced bitwise AND with OR (`MathMutator`) | K | K | K | `—` |
| M19 | `read()`, 94 | removed conditional - replaced comparison check with false (`RemoveConditionalMutator_ORDER_ELSE`) | K | K | K | `—` |
| M20 | `read()`, 93 | removed call to org/apache/tika/io/LookaheadInputStream::fill (`VoidMethodCallMutator`) | K | K | K | `—` |
| M21 | `read()`, 95 | replaced int return with 0 for org/apache/tika/io/LookaheadInputStream::read (`PrimitiveReturnsMutator`) | K | K | K | `—` |
| M22 | `read()`, 97 | replaced int return with 0 for org/apache/tika/io/LookaheadInputStream::read (`PrimitiveReturnsMutator`) | K | K | K | `—` |
| M23 | `read(byte[],int,int)`, 104 | changed conditional boundary (`ConditionalsBoundaryMutator`) | NC | K | K | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` |
| M24 | `read(byte[],int,int)`, 105 | Replaced integer subtraction with addition (`MathMutator`) | NC | S | K | `LookaheadInputStreamManualTest.testBulkReadClampsToRemainingWindowAfterSingleByteRead()` |
| M25 | `read(byte[],int,int)`, 107 | Replaced integer addition with subtraction (`MathMutator`) | NC | K | K | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` |
| M26 | `read(byte[],int,int)`, 104 | removed conditional - replaced comparison check with false (`RemoveConditionalMutator_ORDER_ELSE`) | NC | K | K | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` |
| M27 | `read(byte[],int,int)`, 103 | removed call to org/apache/tika/io/LookaheadInputStream::fill (`VoidMethodCallMutator`) | NC | K | K | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` |
| M28 | `read(byte[],int,int)`, 106 | removed call to java/lang/System::arraycopy (`VoidMethodCallMutator`) | NC | K | K | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` |
| M29 | `read(byte[],int,int)`, 108 | replaced int return with 0 for org/apache/tika/io/LookaheadInputStream::read (`PrimitiveReturnsMutator`) | NC | K | K | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` |
| M30 | `read(byte[],int,int)`, 110 | replaced int return with 0 for org/apache/tika/io/LookaheadInputStream::read (`PrimitiveReturnsMutator`) | NC | K | K | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` |
| M31 | `skip`, 118 | Replaced long addition with subtraction (`MathMutator`) | K | K | K | `—` |
| M32 | `skip`, 116 | removed call to org/apache/tika/io/LookaheadInputStream::fill (`VoidMethodCallMutator`) | K | K | K | `—` |
| M33 | `skip`, 119 | replaced long return with 0 for org/apache/tika/io/LookaheadInputStream::skip (`PrimitiveReturnsMutator`) | K | K | K | `—` |

### Pourquoi les 9 nouvelles détections IA ?

| Mutant | Détecteur PIT | Explication |
|---|---|---|
| M8 | `LookaheadInputStream_read_2_0_Test.testReadWithSingleByteStream()` | Avec une source d’un octet et une fenêtre de 3, la seconde lecture demande la fin de flux alors que `buffered` vaut déjà 1. L’addition `buffer.length+buffered` demande une zone dépassant le tampon, tandis que la soustraction originale reste dans les bornes. Le test distingue l’exception du résultat -1 attendu. |
| M15 | `LookaheadInputStream_markSupported_6_0_Test.testMarkSupported()` | Le test appelle `markSupported()` et attend true. Le mutant renvoie false ; l’assertion échoue directement. |
| M23 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | Après copie complète, `buffered==position`. Le mutant autorise encore la branche de copie et renvoie 0 ; le test attend -1 à la fin de la fenêtre. |
| M25 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | La position doit avancer de la longueur copiée. Une soustraction rend la position négative après la copie ; la lecture suivante échoue au lieu de renvoyer -1. |
| M26 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | La branche de copie est forcée à false même lorsque 5 octets sont disponibles ; le retour -1 contredit le nombre 5 attendu. |
| M27 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | Sans fill(), le tampon n’a aucun octet chargé. La première lecture renvoie -1 au lieu de 5. |
| M28 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | Sans arraycopy(), le tableau destination reste rempli de zéros. L’égalité exacte avec [1,2,3,4,5] détecte la mutation, même si le compte renvoyé reste 5. |
| M29 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | Le mutant renvoie 0 après une copie réussie ; le test attend un compte de 5, indépendamment du contenu copié. |
| M30 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | À l’épuisement de la fenêtre, le mutant renvoie 0 au lieu de -1 ; l’assertion sur la seconde lecture le détecte. |

Ces neuf détections reposent sur des tests dont les oracles n’ont pas été corrigés : les corrections d’`available()` ne sont pas responsables de l’amélioration mesurée. Après IA, il n’y a plus de mutant non couvert, mais cinq survivants subsistent ; un nouveau chemin couvert peut donc faire passer `NO_COVERAGE` à `SURVIVED` sans détecter l’erreur.

## 7. Tests manuels supplémentaires pour les survivants

Ces [quatre tests](tika-core/src/ift3913-manual/java/org/apache/tika/io/LookaheadInputStreamManualTest.java) ont été rédigés séparément de ChatUniTest, après observation des cinq survivants de la phase IA, avec l’aide de Codex. Ils doivent être compris et validés par le binôme ; leur origine est déclarée en section 10.

| Nom du test | Intention et mutants ciblés | Données et raison du choix | Oracle et origine de l’attendu |
|---|---|---|---|
| `testCloseRestoresNonzeroInitialPosition` | Marquage du constructeur ; suppression du test non-null ou de `stream.mark(n)` | Source `[10,20,30,40]`, consommer 10 avant de créer une fenêtre 2 | Lecture 20 puis 30, fin de fenêtre ; après close, source renvoie 20,30,40. La documentation garantit la restauration à la position lors de la création, pas au début du flux. Sans mark, ByteArrayInputStream revient à son marqueur initial 0 et renvoie 10 |
| `testBulkReadClampsToRemainingWindowAfterSingleByteRead` | Calcul `buffered-position` remplacé par une addition dans `read(byte[],int,int)` | Source 5 octets, fenêtre 3 ; lire 10, demander 5 octets vers un tableau de taille 8 à l’index 2 | Seulement 2 octets restent autorisés : retour 2, tableau `[0,0,20,30,0,0,0,0]`, puis -1. La demande dépasse volontairement la fenêtre restante tout en respectant le tableau de destination ; le mutant surestime la copie |
| `testSourceEndRestoresUnderlyingPositionAutomatically` | Suppression de `close()` lorsque `fill()` observe -1 | Source `[10,20]`, fenêtre 4 : source plus courte que la fenêtre | Lectures 10,20,-1 puis source renvoie 10 avant la fermeture explicite du wrapper. `fill()` ferme/restaure à la fin de la source ; sans close, la source reste épuisée. Ce comportement dépend de l’implémentation observée et complète le contrat général de restauration |
| `testFullWindowDoesNotResetSourceBeforeClose` | Frontière `buffered < buffer.length` changée en `<=` | Source `[10,20]`, fenêtre exactement 2 ; lire jusqu’à la fin de fenêtre | Avant close, source.available() vaut 0 ; après close, 2 puis lecture 10. Le remplissage doit s’arrêter à la fenêtre pleine. Avec `<=`, ByteArrayInputStream à EOF renvoie -1 lors de la lecture supplémentaire de longueur 0, ce qui déclenche une restauration prématurée observable |

Tous les cinq survivants sont devenus `KILLED`, chacun avec un de ces tests comme détecteur dans le XML final. Aucun mutant n’a dû être déclaré équivalent ou inatteignable. La suite finale contient **18 nouveaux tests** (14 IA + 4 manuels), tous réussis.

## 8. Commandes de reproduction

Depuis la racine du fork, sur ce Mac :

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# Refaire les trois phases ; chaque clean est précédé d’un archivage des résultats.
bash ift3913/scripts/measure.sh
```

Le script conserve un nouveau dossier `ift3913/evidence/reproduction-DATE-HEURE`, exécute les tests originaux seuls, puis originaux + IA, puis originaux + IA + manuels, lance PIT après chaque phase et produit `summary.json`. Il échoue si les commandes Maven échouent ou si les ensembles de mutants diffèrent. La phase finale impose également que tous les mutants soient tués et qu’aucun test n’échoue. Pour un autre JDK/OS, les résultats doivent être revérifiés plutôt que supposés identiques.

Pour exécuter seulement la suite finale :

```bash
mvn -B -ntp -pl tika-core -am clean verify -Pci,ift3913-ai,ift3913-manual
open tika-core/target/site/jacoco/index.html
```

Pour refaire uniquement PIT après compilation des tests de la phase correspondante :

```bash
mvn -B -ntp -pl tika-core -Pift3913-pit,ift3913-ai,ift3913-manual \
  org.pitest:pitest-maven:mutationCoverage -Dift3913.stage=final
open tika-core/target/pit-reports/final/index.html
```

**Attention :** `clean` empêche les tests compilés d’une phase précédente de contaminer la suivante. Les fichiers dans `target` sont temporaires ; utiliser les preuves archivées pour comparer. JaCoCo mesure ce qui est exécuté ; PIT mesure la détection des mutations.

Pour reproduire une génération (facultatif, les tests sont déjà conservés) : lancer Ollama, télécharger le modèle si absent, créer l’alias, puis utiliser un nouveau dossier :

```bash
ollama pull qwen2.5-coder:14b
ollama cp qwen2.5-coder:14b code-llama
# ollama serve : seulement si le serveur n’est pas déjà actif.
mvn -B -ntp -pl tika-core -am install -Pci -DskipTests
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

Sur macOS, garder le capot ouvert ; `caffeinate` empêche la veille automatique. [Documentation Ollama](https://docs.ollama.com/api/openai-compatibility), [plugin ChatUniTest](https://github.com/ZJU-ACES-ISE/chatunitest-maven-plugin).

## 9. GitHub Actions et remise

Le [workflow dédié](.github/workflows/ift3913-tache2.yml) utilise Temurin Java 17 et reproduit les trois phases avec `measure.sh`. Il exécute les nouveaux tests avec les originaux et publie les rapports en artefact. Le modèle et Ollama ne sont pas nécessaires en CI : les tests acceptés sont déjà versionnés.

**Statut : validation locale réussie ; publication et exécution distante en préparation.** Aucun succès GitHub Actions n’est encore revendiqué. Le lien du run et le commit testé seront ajoutés après vérification.

| Critère du barème | Poids | État et preuve |
|---|---:|---|
| Choix de classes/méthodes pertinentes | 10 % | Section 2 : couverture, tests existants et mutants |
| ChatUniTest dans Maven, modèle ouvert local | 10 % | Section 3, POM, métadonnées Ollama |
| Génération effective | 10 % | Section 4, sources brutes et historiques |
| Explication/critique des tests et corrections | 20 % | Sections 4–5, manifest et diffs |
| PIT originaux puis nouveaux tests | 15 % | Sections 1 et 6, trois XML contrôlés |
| Documentation des mutants détectés | 15 % | Section 6, détecteurs et explications |
| Tests manuels documentés | 10 % | Section 7 : noms, intentions, entrées et oracles |
| Nouveaux tests réussis dans GitHub Actions | 10 % | Workflow préparé ; statut exact ci-dessus |

Formalités restant à vérifier avec le binôme : noms complets/identifiants, première PR dans le dépôt du cours créant `tache2/NOM1_NOM2/readme.md`, puis seconde PR de remise indiquant le lien du fork et du README. Aucun nom ou état de PR ne sera inventé. L’échéance doit être vérifiée sur le plan du cours avant remise.

## 10. Déclaration d’usage de l’IA

Les 6–7 octobre 2026, l’utilisateur a utilisé **ChatUniTest 2.1.1 avec Qwen2.5-Coder 14B local via Ollama**, alias `code-llama`, pour les deux générations. Les prompts, réponses, propositions, erreurs et réparations automatiques sont conservés dans les historiques liés en section 4.

Codex a aidé à lire les consignes, inspecter le fork, mesurer les tests/JaCoCo/PIT, comparer les candidates, préparer les profils Maven et le workflow, diagnostiquer la configuration, corriger les cinq attentes d’`available()`, intégrer les tests, rédiger les quatre tests supplémentaires à partir des survivants, vérifier les résultats et rédiger la documentation. Le nom exact du modèle sous-jacent à chaque intervention de Codex n’est pas attesté ici. Les modifications de tests faites par Codex sont des **interventions humaines assistées par IA**, distinctes des réparations automatiques de ChatUniTest ; nous ne présentons pas les tests manuels comme une production non assistée des étudiants.

Le binôme doit relire, comprendre, adapter si nécessaire et valider les contributions conservées conformément aux [règles d’usage de l’IA du cours](https://github.com/umontreal-diro/IFT3913/tree/2026#usage-de-lintelligence-artificielle). Les résultats rapportés proviennent des outils exécutés et des preuves archivées, pas d’estimations du modèle.

---

# Documentation d’origine d’Apache Tika

Welcome to Apache Tika  <https://tika.apache.org/>
=================================================

[![license](https://img.shields.io/github/license/apache/tika.svg?maxAge=2592000)](http://www.apache.org/licenses/LICENSE-2.0)
[![Jenkins](https://img.shields.io/jenkins/s/https/ci-builds.apache.org/job/Tika/job/tika-main-jdk17.svg?maxAge=3600)](https://ci-builds.apache.org/job/Tika/job/tika-main-jdk17/)
[![Jenkins tests](https://img.shields.io/jenkins/t/https/ci-builds.apache.org/job/Tika/job/tika-main-jdk17.svg?maxAge=3600)](https://ci-builds.apache.org/job/Tika/job/tika-main-jdk17/lastBuild/testReport/)
[![Maven Central](https://img.shields.io/maven-central/v/org.apache.tika/tika.svg?maxAge=86400)](http://search.maven.org/#search|ga|1|g%3A%22org.apache.tika%22)

Apache Tika(TM) is a toolkit for detecting and extracting metadata and structured text content from various documents using existing parser libraries.

Tika is a project of the [Apache Software Foundation](https://www.apache.org).

Apache Tika, Tika, Apache, the Apache feather logo, and the Apache Tika project logo are trademarks of The Apache Software Foundation.

Quick Start
===========

**Parse a file in Java:**

```java
import org.apache.tika.Tika;

Tika tika = new Tika();
String text = tika.parseToString(new File("document.pdf"));
System.out.println(text);
```

**From the command line:**

```bash
java -jar tika-app-*.jar --text document.pdf
```

**Maven dependency:**

```xml
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <version>4.x.y</version>
    <type>pom</type>
</dependency>
```

Getting Started
===============
Pre-built binaries of Apache Tika standalone applications are available
from https://tika.apache.org/download.html . Pre-built binaries of all the
Tika jars can be fetched from Maven Central or your favourite Maven mirror.

**Tika 2.X and support for Java 8 reached End of Life (EOL) in April, 2025. 
See [Tika Roadmap 2.x, 3.x and beyond](https://cwiki.apache.org/confluence/display/TIKA/Tika+Roadmap+--+2.x%2C+3.x+and+Beyond).** 

Tika is based on **Java 17** and uses the [Maven 3](https://maven.apache.org) build system.
**N.B.** [Docker](https://www.docker.com/products/personal) is used for tests in tika-integration-tests. If Docker is not installed, those tests are skipped.

To build Tika from source, use the following command in the main directory:

    ./mvnw clean install

The Maven wrapper (`mvnw`) is included in the repository and will automatically download
the correct Maven version if needed. On Windows, use `mvnw.cmd` instead.

The build consists of a number of components, including a standalone runnable jar that you can use to try out Tika features. You can run it like this:

    java -jar tika-app/target/tika-app-*.jar --help


To build a specific project (for example, tika-server-standard):

    ./mvnw clean install -am -pl :tika-server-standard

If the ossindex-maven-plugin is causing the build to fail because a dependency
has now been discovered to have a vulnerability:

    ./mvnw clean install -Dossindex.skip


Faster Builds
=============

**Fast profile** - Use `-Pfast` to skip tests, checkstyle, and spotless:

    ./mvnw clean install -Pfast

**Parallel builds** - Add `-T1C` to build with 1 thread per CPU core:

    ./mvnw clean install -Pfast -T1C

**Maven Daemon (mvnd)** - Keeps a warm JVM running for 2-3x faster rebuilds:

```bash
# Install: https://github.com/apache/maven-mvnd
# macOS: brew install mvndaemon/tap/mvnd

# Use exactly like mvn
mvnd clean install -Pfast
mvnd test -pl :tika-core
```

**Combine both** for maximum speed during development:

    mvnd clean install -Pfast -T1C


Reproducible Builds
===================

Apache Tika supports [reproducible builds](https://reproducible-builds.org/). This means
that building the same source code with the same JDK version should produce
byte-for-byte identical artifacts, regardless of the build machine or time.

Key configuration:
- `project.build.outputTimestamp` is set in `tika-parent/pom.xml`
- All Maven plugins are configured to produce deterministic output

To verify the build plan supports reproducibility:

    ./mvnw artifact:check-buildplan

To verify two builds produce identical artifacts:

    ./mvnw clean install -DskipTests
    mv ~/.m2/repository/org/apache/tika tika-build-1
    ./mvnw clean install -DskipTests
    diff -r tika-build-1 ~/.m2/repository/org/apache/tika


Maven Dependencies
==================

Apache Tika provides *Bill of Material* (BOM) artifact to align Tika module versions and simplify version management. 
To avoid convergence errors in your own project, import this
bom or Tika's parent pom.xml in your dependency management section.

If you use Apache Maven:

```xml
<project>
  <dependencyManagement>
    <dependencies>
      <dependency>
       <groupId>org.apache.tika</groupId>
       <artifactId>tika-bom</artifactId>
       <version>4.x.y</version>
       <type>pom</type>
       <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency>
      <groupId>org.apache.tika</groupId>
      <artifactId>tika-parsers-standard-package</artifactId>
      <type>pom</type>
      <!-- version not required since BOM included -->
    </dependency>
  </dependencies>
</project>
```

For Gradle:

```kotlin
dependencies {
  implementation(platform("org.apache.tika:tika-bom:4.x.y"))

  // version not required since bom (platform in Gradle terms)
  implementation("org.apache.tika:tika-parsers-standard-package@pom")
}
```

Migrating to 4.x
================
TBD

Contributing
============
See [CONTRIBUTING.md](CONTRIBUTING.md) and https://tika.apache.org/contribute.html

[![contributors](https://contributors-img.web.app/image?repo=apache/tika)](https://github.com/apache/tika/graphs/contributors)

Building from a Specific Tag
============================
Let's assume that you want to build the 3.0.1 tag:
```
0. Download and install hub.github.com
1. git clone https://github.com/apache/tika.git
2. cd tika
3. git checkout 3.0.1
4. ./mvnw clean install
```

If a new vulnerability has been discovered between the date of the
tag and the date you are building the tag, you may need to build with:

```
4. ./mvnw clean install -Dossindex.skip
```

If a local test is not working in your environment, please notify
 the project at dev@tika.apache.org. As an immediate workaround,
 you can turn off individual tests with e.g.:

```
4. ./mvnw clean install -Dossindex.skip -Dtest=\!UnpackerResourceTest#testPDFImages
```

License (see also LICENSE.txt)
==============================

Collective work: Copyright 2011 The Apache Software Foundation.

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at

<https://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.

Apache Tika includes a number of subcomponents with separate copyright notices and license terms. Your use of these subcomponents is subject to the terms and conditions of the licenses listed in the LICENSE.txt file.

Export Control
==============

This distribution includes cryptographic software.  The country in which you currently reside may have restrictions on the import, possession, use, and/or re-export to another country, of encryption software.  BEFORE using any encryption software, please  check your country's laws, regulations and policies concerning the import, possession, or use, and re-export of encryption software, to  see if this is permitted.  See <http://www.wassenaar.org/> for more information.

The U.S. Government Department of Commerce, Bureau of Industry and Security (BIS), has classified this software as Export Commodity Control Number (ECCN) 5D002.C.1, which includes information security software using or performing cryptographic functions with asymmetric algorithms.  The form and manner of this Apache Software Foundation distribution makes it eligible for export under the License Exception ENC Technology Software Unrestricted (TSU) exception (see the BIS Export Administration Regulations, Section 740.13) for both object code and source code.

The following provides more details on the included cryptographic software:

Apache Tika uses the Bouncy Castle generic encryption libraries for extracting text content and metadata from encrypted PDF files.  See <http://www.bouncycastle.org/> for more details on Bouncy Castle.  

Mailing Lists
=============

* user@tika.apache.org - About using Tika
* dev@tika.apache.org - About developing Tika

Subscribe by sending a message to `{list}-subscribe@tika.apache.org`.

Issue Tracker
=============

https://issues.apache.org/jira/browse/TIKA

Security
========

See [SECURITY.md](SECURITY.md) and https://tika.apache.org/security.html
