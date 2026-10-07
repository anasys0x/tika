# IFT3913 — Tâche 2 : tests de LookaheadInputStream dans Apache Tika

| Nom complet | Identifiant GitHub |
|---|---|
| Mrani Alaoui Anas | Anasys0x |
| Rachidi Aymane | Aymtrack |

- [Fork et branche de travail](https://github.com/anasys0x/tika/tree/tache2).
- [Consignes de la tâche 2](https://github.com/umontreal-diro/IFT3913/tree/2026/tache2).
- Référence initiale : `a2d75c2c10563b261148e9bafe4046832b33c1a6`. Le code de production et les tests originaux sont inchangés.

Toute la documentation de la tâche est réunie dans ce README. Les fichiers ajoutés servent à la génération, à l’exécution et à l’analyse des tests demandées.

## 1. Résultats

| Phase | Tests core (dont ignorés) | Lignes JaCoCo | Branches JaCoCo | Mutants tués / total | Score | Survivants | Non couverts |
|---|---:|---:|---:|---:|---:|---:|---:|
| Originaux seuls, Java 17 | 749 (2 ignorés) | 32/40 | 14/16 | 19/33 | 57,58 % | 5 | 9 |
| Originaux + IA corrigés | 763 (2 ignorés) | 40/40 | 16/16 | 28/33 | 84,85 % | 5 | 0 |
| Originaux + IA + manuels | 767 (2 ignorés) | 40/40 | 16/16 | 33/33 | 100,00 % | 0 | 0 |

Les trois phases ont réussi avec zéro échec et zéro erreur. Les 8 tests de `tika-annotation-processor` réussissent également, séparément des totaux de `tika-core`.

Score de mutation : `100 × KILLED / tous les mutants générés`, en incluant les mutants non couverts dans le dénominateur. Les trois analyses portent sur les mêmes 33 mutants, sans timeout ni erreur. Les tests IA ajoutent 9 détections et les tests manuels les 5 restantes. Le résultat de 100 % concerne cette classe et les mutateurs `DEFAULTS` ; il ne prouve pas l’absence de tout défaut dans Tika.

## 2. Choix de la classe

Classe retenue : [org.apache.tika.io.LookaheadInputStream](tika-core/src/main/java/org/apache/tika/io/LookaheadInputStream.java), dans le module autorisé `tika-core`. Une classe suffit aux consignes.

Ses [six tests originaux](tika-core/src/test/java/org/apache/tika/io/LookaheadInputStreamTest.java) couvrent notamment un flux nul/vide, une fenêtre limitée, le marquage, la remise à zéro et le saut. Avant ajout, seulement 32/40 lignes (80 %) et 14/16 branches (87,50 %) étaient couvertes. `read(byte[], int, int)` n’était pas exécutée (7 lignes et 2 branches), pas davantage `markSupported()`.

PIT trouvait 5 survivants et 9 mutants non couverts. Les survivants concernent le marquage du constructeur, la frontière de remplissage, le calcul de longueur et la fermeture à la fin du flux. La classe est petite (142 lignes, commentaires compris), se teste avec des flux en mémoire et permet des oracles précis, sans fichier externe ou OCR. Ces mesures justifient le choix de méthodes insuffisamment testées.

## 3. ChatUniTest dans Maven et modèle local

Versions utilisées : Java **17.0.20.1**, Maven **3.9.16**, Tika **4.0.0-SNAPSHOT**, JUnit Jupiter **6.1.3**, JaCoCo **0.8.15**, PIT **1.30.0**, adaptateur PIT/JUnit **1.2.3**. PIT utilise `DEFAULTS`, 2 processus et `timeoutConstant=10000`.

ChatUniTest **2.1.1** est configuré dans [tika-core/pom.xml](tika-core/pom.xml) avec **Qwen2.5-Coder 14B**, modèle ouvert exécuté localement par **Ollama 0.35.1**, sur macOS ARM64 avec 24 Go de mémoire. Quantification : **Q4_K_M**.

ChatUniTest refuse le nom natif `qwen2.5-coder:14b`. L’alias local `code-llama`, créé avec `ollama cp`, désigne le même modèle Qwen. L’URL est `http://127.0.0.1:11434/v1/chat/completions`, avec la clé factice `ollama`.

Paramètres : température 0,2, un candidat par méthode, au maximum 3 tours au total (initial + deux réparations), exécution activée, arrêt après succès et absence de parallélisme. Le journal affiche `MaxPromptTokens=10923` et `MaxResponseTokens=1024`.

| Profil Maven | Fonction dans l’expérience |
|---|---|
| `ift3913-chatunitest` | Générer les tests avec ChatUniTest et Ollama local |
| `ift3913-pit` | Analyser les mutations de LookaheadInputStream |
| `ift3913-ai` | Ajouter les 14 tests IA aux tests originaux |
| `ift3913-manual` | Ajouter les 4 tests supplémentaires |

Les profils de tests permettent de comparer les originaux seuls, puis les ajouts IA, puis les ajouts manuels. Mockito 5.23.0 et le launcher correspondant à JUnit ont été ajoutés pour la compilation/exécution des propositions. Les règles Spotless du dépôt incluent les deux dossiers de tests supplémentaires.

## 4. Génération et corrections

### Résultat de la génération

Un premier essai, le 6 octobre, a échoué avant intégration : incompatibilité avec Java 27, nom de modèle refusé, puis dépendances Mockito absentes et erreurs Java dans les propositions. La génération complète a produit 24 tours en échec de compilation. Son `BUILD SUCCESS` annonçait la fin du traitement du plugin, pas des tests réussis. La durée affichée de 1 h 34 incluait une période de veille du Mac.

L’expérience retenue est le deuxième essai du **7 octobre 2026**, sous Java 17 avec Mockito : **12 min 47 s**, 8 méthodes traitées et **16 tours** (8 générations initiales et 8 réparations automatiques). ChatUniTest exporte 5 classes de tests et une ancienne suite JUnit. Les cinq classes compilent ; avant correction de leurs assertions, **14 tests sont exécutés : 11 réussis et 3 en échec**.

| Cible / fichier généré | Tours | Dernier candidat compilable | Résultat avant correction | Décision |
|---|---:|---|---|---|
| `close` / `close_0_0_Test` | 2 | Oui | 2 réussis | Intégré |
| `read()` / `read_2_0_Test` | 2 | Oui | 4 réussis | Intégré |
| `read(byte[],int,int)` / `read_3_0_Test` | 1 | Oui | 3 réussis exportés | Intégré |
| `available` / `available_5_0_Test` | 1 | Oui | 1 réussi, 3 en échec | Intégré après 5 corrections d’attentes |
| `markSupported` / `markSupported_6_0_Test` | 1 | Oui | 1 réussi | Intégré |
| `skip` / `skip_4_0_Test` | 3 | Non | Non exécuté | Import `Field` manquant notamment |
| `mark` / `mark_7_0_Test` | 3 | Non | Non exécuté | Exceptions de réflexion non déclarées |
| `reset` / `reset_8_0_Test` | 3 | Non | Non exécuté | `NoSuchFieldException` non déclarée |

Les tests générés intégrés sont dans [tika-core/src/ift3913-ai/java/org/apache/tika/io](tika-core/src/ift3913-ai/java/org/apache/tika/io). Les trois candidats non compilables n’ont pas été intégrés. La suite exportée utilisant un ancien runner JUnit 4/Platform a été écartée ; Surefire découvre directement les cinq classes Jupiter.

ChatUniTest a filtré automatiquement 5 des 8 propositions de la lecture dans un tableau avant export : comparaison de tableaux de tailles différentes, attente d’exception incorrecte pour une longueur négative, séquence attendue incorrecte et deux lectures dépassant le tableau destination. Seuls les 3 tests exportés ont été intégrés. Ce filtrage est une intervention automatique de l’outil, distincte de nos corrections.

Le plugin annonçait un succès pour `available()` malgré trois échecs JUnit. Les fichiers exportés ont donc été exécutés indépendamment avant de conclure. `markSupported` a été confirmé hors du bac à sable de l’assistant, qui empêchait l’attachement de l’agent Mockito.

### Corrections des tests intégrés

| Test | Attente initiale | Correction | Justification |
|---|---:|---:|---|
| `testAvailable`, après la première lecture | 9 | 4 | 5 octets réellement chargés, 1 consommé |
| `testAvailable`, après reset puis lecture | 9 | 4 | Retour au marqueur 0, puis consommation d’un octet |
| `testAvailableAfterMark`, après deux lectures | 8 | 3 | 5 octets chargés moins 2 consommés |
| `testAvailableAfterMark`, après reset | 9 | 4 | Marqueur à la position 1, donc 4 octets restants |
| `testAvailableAfterSkip` | 7 | 2 | 1 octet lu et 2 sautés sur 5 |

**5 assertions corrigées dans 3 tests d’un seul fichier.** Le modèle confondait capacité du tampon (10) et nombre d’octets de la source (5). Aucune autre assertion, entrée ou méthode n’a été modifiée dans les quatre autres classes retenues.

Adaptations techniques séparées : ajout de l’en-tête Apache, remplacement des imports inutilisés/génériques par des imports explicites, puis formatage, dans chacun des 5 fichiers. Les dépendances Maven corrigent la configuration, pas les oracles. Après ces interventions, les 14 tests IA réussissent avec les originaux (763 tests core, zéro échec/erreur, 2 ignorés).

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

Le fichier de production a la même empreinte dans les trois phases ; les clés de comparaison sont classe, méthode, signature JVM, index d’instruction et mutateur. La comparaison des rapports XML a confirmé que les trois ensembles de mutants coïncident. Les numéros M1–M33 ci-dessous suivent l’ordre XML de la référence Java 17.

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

## 8. Commandes Maven

Depuis la racine du fork, avec Java 17. Sur ce Mac :

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Originaux seuls, puis PIT :

```bash
mvn -B -ntp -pl tika-core -am clean install -Pci
mvn -B -ntp -pl tika-core -Pift3913-pit \
  org.pitest:pitest-maven:mutationCoverage -Dift3913.stage=baseline
```

Originaux + tests IA corrigés, puis PIT :

```bash
mvn -B -ntp -pl tika-core -am clean test -Pci,ift3913-ai
mvn -B -ntp -pl tika-core -Pift3913-pit,ift3913-ai \
  org.pitest:pitest-maven:mutationCoverage -Dift3913.stage=ai
```

Originaux + tests IA + tests manuels, puis PIT :

```bash
mvn -B -ntp -pl tika-core -am clean verify -Pci,ift3913-ai,ift3913-manual
mvn -B -ntp -pl tika-core -Pift3913-pit,ift3913-ai,ift3913-manual \
  org.pitest:pitest-maven:mutationCoverage -Dift3913.stage=final
```

JaCoCo : `tika-core/target/site/jacoco/index.html`. PIT : `tika-core/target/pit-reports/PHASE/index.html`. Chaque `clean` supprime les résultats précédents : relever les compteurs avant de passer à la phase suivante. Il évite également que des tests compilés dans une phase contaminent la suivante.

Pour la génération locale : démarrer Ollama si nécessaire, puis utiliser la configuration Maven :

```bash
ollama pull qwen2.5-coder:14b
ollama cp qwen2.5-coder:14b code-llama
mvn -B -ntp -pl tika-core -am install -Pci -DskipTests
caffeinate -i mvn -B -ntp -pl tika-core -Pci,ift3913-chatunitest \
  io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:class \
  -DselectClass=org.apache.tika.io.LookaheadInputStream \
  -Dift3913.model=code-llama
```

Les nouvelles sorties temporaires seront dans `tika-core/target/chatunitest/`. Sur macOS, garder le capot ouvert ; `caffeinate` empêche la veille automatique. Sur les autres systèmes, retirer `caffeinate -i`.

## 9. GitHub Actions et remise

Le [workflow dédié](.github/workflows/ift3913-tache2.yml) utilise Temurin Java 17 et exécute directement `mvn -B -ntp -pl tika-core -am clean verify -Pci,ift3913-ai,ift3913-manual`. Il vérifie les tests IA et supplémentaires avec les originaux. Ollama n’est pas nécessaire en CI, les tests intégrés étant déjà versionnés.

Les 18 nouveaux tests ont réussi dans [GitHub Actions le 7 octobre 2026](https://github.com/anasys0x/tika/actions/runs/37666427788), au commit `a8ebf0c355ef2340b3311b6f2ef41bd03ea033d5`. Le journal confirme zéro échec/erreur et les résultats des trois phases de la section 1.

L’inscription du binôme est déjà réalisée par la [PR nº 1239](https://github.com/umontreal-diro/IFT3913/pull/1239). La remise finale consiste à ajouter les liens du fork et de ce README dans `tache2/MRANI_ALAOUI-RACHIDI/readme.md` du dépôt du cours, par une seconde PR. Les [instructions de remise](https://github.com/umontreal-diro/IFT3913/blob/2026/instructions-PR.md) autorisent cette soumission à partir du **9 octobre 2026** ; la limite indiquée par le cours est le **13 octobre 2026 à 17 h EDT**.

## 10. Déclaration d’usage de l’IA

Les 6–7 octobre 2026, l’utilisateur a utilisé ChatUniTest 2.1.1 avec Qwen2.5-Coder 14B local via Ollama pour générer des tests JUnit 5 à partir de la méthode ciblée, de la classe et des dépendances fournies automatiquement par l’outil. ChatUniTest a ensuite demandé des réparations en utilisant les erreurs de compilation/exécution.

Codex a aidé à lire les consignes, inspecter le fork, mesurer JaCoCo/PIT, préparer Maven et GitHub Actions, diagnostiquer les erreurs, corriger les cinq attentes d’available(), intégrer les tests, écrire les quatre tests supplémentaires à partir des survivants et rédiger le README. Le nom exact du modèle sous-jacent à chaque intervention de Codex n’est pas attesté ici. Les corrections et les tests supplémentaires écrits avec Codex sont déclarés comme interventions assistées par IA, distinctes des générations et réparations automatiques de ChatUniTest.

Le binôme doit comprendre et valider les contributions retenues conformément aux [règles d’usage de l’IA du cours](https://github.com/umontreal-diro/IFT3913/tree/2026#usage-de-lintelligence-artificielle). Les pourcentages proviennent des exécutions Maven, JaCoCo et PIT ; ils ne sont pas des estimations de l’assistant.

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
