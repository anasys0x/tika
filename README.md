# IFT3913 — Tâche 2 : tests de LookaheadInputStream et CharsetUtils dans Apache Tika

| Nom complet | Identifiant GitHub |
|---|---|
| Mrani Alaoui Anas | Anasys0x |
| Rachidi Aymane | Aymtrack |

- [Fork et branche de travail](https://github.com/anasys0x/tika/tree/tache2).
- [Consignes de la tâche 2](https://github.com/umontreal-diro/IFT3913/tree/2026/tache2).

Toute la documentation de la tâche se trouve dans ce README. Les fichiers que nous avons ajoutés servent uniquement à générer, exécuter et analyser les tests.

Nous avons étudié deux classes de `tika-core`, avec la même configuration Maven : `LookaheadInputStream` (sections 2 à 7) et `CharsetUtils` (section 8).

## 1. Résultats

Le score de mutation est calculé ainsi : `100 × KILLED / nombre total de mutants générés`. Les mutants non couverts comptent donc dans le dénominateur.

### LookaheadInputStream (sections 2 à 7)

| Phase | Tests core (dont ignorés) | Lignes JaCoCo | Branches JaCoCo | Mutants tués / total | Score | Survivants | Non couverts |
|---|---:|---:|---:|---:|---:|---:|---:|
| Originaux seuls, Java 17 | 749 (2 ignorés) | 32/40 | 14/16 | 19/33 | 57,58 % | 5 | 9 |
| Originaux + IA corrigés | 763 (2 ignorés) | 40/40 | 16/16 | 28/33 | 84,85 % | 5 | 0 |
| Originaux + IA + manuels | 767 (2 ignorés) | 40/40 | 16/16 | 33/33 | 100,00 % | 0 | 0 |

Les trois phases passent sans aucun échec ni erreur. Les 8 tests de `tika-annotation-processor` passent aussi ; ils ne sont pas comptés dans les totaux de `tika-core`.

Les trois analyses portent sur les mêmes 33 mutants, sans timeout ni erreur. Les tests IA ajoutent 9 détections, et les tests manuels tuent les 5 mutants restants. Ce 100 % ne vaut toutefois que pour cette classe et pour les mutateurs `DEFAULTS` : il ne prouve pas que Tika est exempt de défauts.

### CharsetUtils (section 8)

| Phase | Tests core (dont ignorés) | Lignes JaCoCo | Branches JaCoCo | Mutants tués / total | Score | Survivants | Non couverts |
|---|---:|---:|---:|---:|---:|---:|---:|
| Originaux seuls | 749 (3 ignorés) | 62/81 | 25/32 | 15/25 | 60,00 % | 5 | 5 |
| Originaux + IA corrigés | 766 (3 ignorés) | 62/81 | 25/32 | 15/25 | 60,00 % | 5 | 5 |
| Originaux + IA + manuels | 775 (3 ignorés) | 68/81 | 29/32 | 24/25 | 96,00 % | 1 | 0 |

Nous avons fait ces mesures sous Windows 11 avec Java 23.0.1, sans aucun échec ni erreur. Comme les profils Maven ajoutent en même temps les tests des deux classes, les totaux comprennent aussi ceux de LookaheadInputStream. Petite différence avec le tableau précédent : sous Windows, 3 tests originaux sont ignorés au lieu de 2.

En résumé, les tests générés par l’IA n’ont tué aucun nouveau mutant sur les 25. Ce sont nos tests manuels qui ont fait passer le score de 60 % à 96 %. Le seul mutant encore vivant, C5, est équivalent (section 8.7), donc on ne peut pas faire mieux que 96 % sur cette classe avec les mutateurs `DEFAULTS`.

## 2. Choix de la classe

Pour la première classe, nous avons retenu [org.apache.tika.io.LookaheadInputStream](tika-core/src/main/java/org/apache/tika/io/LookaheadInputStream.java), dans le module autorisé `tika-core`. Une classe suffit pour respecter les consignes, mais nous en avons traité une seconde, `CharsetUtils`, en section 8.

Ses [six tests originaux](tika-core/src/test/java/org/apache/tika/io/LookaheadInputStreamTest.java) couvrent notamment un flux nul ou vide, une fenêtre limitée, le marquage, la remise à zéro et le saut. Malgré ça, seulement 32 lignes sur 40 (80 %) et 14 branches sur 16 (87,50 %) étaient couvertes avant nos ajouts. La méthode `read(byte[], int, int)` n’était jamais exécutée (7 lignes et 2 branches), pas plus que `markSupported()`.

Côté mutation, PIT trouvait 5 survivants et 9 mutants non couverts. Les survivants concernaient le marquage dans le constructeur, la limite de remplissage du tampon, un calcul de longueur et la fermeture en fin de flux. La classe est petite (142 lignes, commentaires compris), se teste avec de simples flux en mémoire et permet des vérifications précises, sans fichier externe ni OCR. Ces chiffres confirment que certaines de ses méthodes étaient insuffisamment testées.

## 3. ChatUniTest dans Maven et modèle local

Pour la première classe, nous avons utilisé Java **17.0.20.1**, Maven **3.9.16**, Tika **4.0.0-SNAPSHOT**, JUnit Jupiter **6.1.3**, JaCoCo **0.8.15**, PIT **1.30.0** et l’adaptateur PIT/JUnit **1.2.3**. PIT tourne avec les mutateurs `DEFAULTS`, 2 processus et `timeoutConstant=10000`.

ChatUniTest **2.1.1** est configuré dans [tika-core/pom.xml](tika-core/pom.xml). Le modèle est **Qwen2.5-Coder 14B** (quantification **Q4_K_M**), un modèle ouvert exécuté en local par **Ollama 0.35.1**, sur un Mac ARM64 avec 24 Go de mémoire.

ChatUniTest refuse le nom `qwen2.5-coder:14b`, car il n’accepte qu’une liste fixe de noms de modèles. Nous avons donc créé avec `ollama cp` un alias local, `code-llama`, qui pointe vers le même modèle Qwen. ChatUniTest l’appelle à l’adresse `http://127.0.0.1:11434/v1/chat/completions`, avec une clé factice, `ollama`.

Côté réglages : température 0,2, un candidat par méthode, au plus 3 tours au total (la génération initiale puis deux réparations), exécution des tests activée, arrêt dès qu’un test passe, et pas de parallélisme. Le journal affiche `MaxPromptTokens=10923` et `MaxResponseTokens=1024`.

Nous avons ajouté quatre profils Maven, qui ne s’activent que lorsqu’on les demande :

| Profil Maven | Rôle dans l’expérience |
|---|---|
| `ift3913-chatunitest` | Générer les tests avec ChatUniTest et Ollama en local |
| `ift3913-pit` | Analyser les mutations de la classe donnée par `ift3913.classes` (LookaheadInputStream par défaut) |
| `ift3913-ai` | Ajouter les tests IA aux tests originaux (14 pour LookaheadInputStream, 3 pour CharsetUtils) |
| `ift3913-manual` | Ajouter les tests supplémentaires (4 pour LookaheadInputStream, 5 pour CharsetUtils) |

Grâce à ces profils, on peut mesurer séparément les tests originaux seuls, puis avec les tests IA, puis avec les tests manuels. Nous avons aussi ajouté Mockito 5.23.0 et le launcher JUnit correspondant, nécessaires pour compiler et exécuter les tests proposés par ChatUniTest. Enfin, nous avons étendu les règles Spotless du dépôt aux deux dossiers de tests supplémentaires.

**Pour la seconde classe (`CharsetUtils`, section 8)**, nous avons gardé la même configuration Maven et les mêmes paramètres, mais la génération a tourné sur une autre machine : Windows 11, Java **23.0.1** et le Maven wrapper du dépôt (**3.9.12**). Le modèle Qwen2.5-Coder 14B ne tenait pas dans la carte graphique de cette machine (GeForce RTX 4070 Laptop, 8 Go de VRAM). Nous avons donc utilisé **CodeQwen 1.5 7B Chat** (`codeqwen:v1.5-chat`, 7,3 milliards de paramètres, quantification **Q4_0**), lui aussi un modèle ouvert exécuté en local par **Ollama 0.40.2**. Avantage : ce nom fait partie des modèles que ChatUniTest accepte directement (`CODE_QWEN`), donc pas besoin d’alias. Il suffit de le passer avec `-Dift3913.model=codeqwen:v1.5-chat`.

Un piège à connaître : sur une carte de 8 Go, Ollama limite par défaut le contexte à 4 096 tokens, alors que ChatUniTest peut envoyer des prompts allant jusqu’à 10 923 tokens. Les prompts auraient donc été tronqués. Nous avons réglé `OLLAMA_CONTEXT_LENGTH=16384` pour l’éviter. Dans le journal du plugin, on retrouve `Model=CODE_QWEN`, `TestNumber=1`, `Multithreading=false`, `MaxPromptTokens=10923` et `MaxResponseTokens=1024`.

## 4. Génération et corrections

### Résultat de la génération

Le premier essai, le 6 octobre, n’a pas abouti. Nous avons d’abord eu un problème d’incompatibilité avec Java 27, puis un nom de modèle refusé, des dépendances Mockito manquantes et des erreurs Java dans les tests proposés. La génération complète a donné 24 tours qui ne compilaient pas. Le `BUILD SUCCESS` affiché voulait seulement dire que le plugin avait terminé, pas que les tests passaient. Quant à la durée de 1 h 34, elle incluait une période où le Mac était en veille.

L’essai que nous avons retenu est le deuxième, celui du **7 octobre 2026**, sous Java 17 et avec Mockito : **12 min 47 s**, 8 méthodes traitées et **16 tours** (8 générations initiales et 8 réparations automatiques). ChatUniTest a exporté 5 classes de tests et une ancienne suite JUnit. Les cinq classes compilent et, avant toute correction, **14 tests s’exécutent : 11 passent et 3 échouent**.

| Méthode / fichier généré | Tours | Dernière version compilable ? | Résultat avant correction | Ce que nous en avons fait |
|---|---:|---|---|---|
| `close` / `close_0_0_Test` | 2 | Oui | 2 réussis | Gardé |
| `read()` / `read_2_0_Test` | 2 | Oui | 4 réussis | Gardé |
| `read(byte[],int,int)` / `read_3_0_Test` | 1 | Oui | 3 réussis exportés | Gardé |
| `available` / `available_5_0_Test` | 1 | Oui | 1 réussi, 3 en échec | Gardé, après 5 corrections d’attentes |
| `markSupported` / `markSupported_6_0_Test` | 1 | Oui | 1 réussi | Gardé |
| `skip` / `skip_4_0_Test` | 3 | Non | Non exécuté | Écarté : import `Field` manquant, entre autres |
| `mark` / `mark_7_0_Test` | 3 | Non | Non exécuté | Écarté : exceptions de réflexion non déclarées |
| `reset` / `reset_8_0_Test` | 3 | Non | Non exécuté | Écarté : `NoSuchFieldException` non déclarée |

Les tests intégrés se trouvent dans [tika-core/src/ift3913-ai/java/org/apache/tika/io](tika-core/src/ift3913-ai/java/org/apache/tika/io). Nous n’avons pas gardé les trois propositions qui ne compilaient pas. La suite exportée, qui utilisait un ancien lanceur JUnit 4/Platform, a aussi été écartée : Surefire trouve directement les cinq classes Jupiter.

Pour la lecture dans un tableau, ChatUniTest a lui-même écarté 5 propositions sur 8 avant l’export : une comparaison de tableaux de tailles différentes, une mauvaise exception attendue pour une longueur négative, une mauvaise séquence attendue et deux lectures qui dépassaient le tableau de destination. Nous n’avons intégré que les 3 tests exportés. Ce tri est fait automatiquement par l’outil et ne fait pas partie de nos corrections.

Le plugin annonçait aussi un succès pour `available()`, alors que trois tests JUnit échouaient. Nous avons donc relancé nous-mêmes les fichiers exportés avant de conclure quoi que ce soit. Nous avons vérifié le test de `markSupported` en dehors du bac à sable de Codex, qui empêchait Mockito d’attacher son agent.

### Corrections des tests intégrés

| Test | Attendu par l’IA | Après correction | Pourquoi |
|---|---:|---:|---|
| `testAvailable`, après la première lecture | 9 | 4 | 5 octets réellement chargés, dont 1 consommé |
| `testAvailable`, après reset puis lecture | 9 | 4 | Retour au marqueur 0, puis un octet consommé |
| `testAvailableAfterMark`, après deux lectures | 8 | 3 | 5 octets chargés, moins les 2 consommés |
| `testAvailableAfterMark`, après reset | 9 | 4 | Marqueur à la position 1, donc 4 octets restants |
| `testAvailableAfterSkip` | 7 | 2 | 1 octet lu et 2 sautés, sur 5 |

Nous avons corrigé **5 assertions, dans 3 tests d’un même fichier**. Le modèle confondait la capacité du tampon (10) et le nombre d’octets de la source (5). Dans les quatre autres classes, nous n’avons modifié aucune assertion, aucune entrée ni aucune méthode.

À côté de ça, nous avons fait quelques ajustements techniques dans chacun des 5 fichiers : ajout de l’en-tête Apache, remplacement des imports inutiles ou avec `*` par des imports explicites, puis formatage. Les dépendances Maven ajoutées corrigent la configuration, pas les vérifications. Après ces changements, les 14 tests IA passent avec les tests originaux (763 tests core, aucun échec ni erreur, 2 ignorés).

## 5. Explication et critique des 14 tests IA retenus

| Test généré | Entrées et vérification | Notre avis |
|---|---|---|
| `close.testClose` | Flux `[1,2,3]`, fermeture, puis le champ privé `stream` doit être nul | Vérifie que le flux interne est détaché, mais pas directement que le flux d’origine est restauré. La réflexion lie en plus le test à l’implémentation |
| `close.testCloseWithNullStream` | Le champ `stream` est mis à null par réflexion, puis fermeture : il doit rester null | Teste le cas sans flux, mais avec un état fabriqué artificiellement au lieu de passer par le constructeur public |
| `read_2.testRead` | `[1,2,3]`, lectures attendues 1, 2, 3 puis -1 | Vérification précise de l’ordre et de la fin du flux, proche du test original. La réflexion est inutile ici, la méthode étant publique |
| `read_2.testReadWithEmptyStream` | Flux vide, résultat -1 | Cas pertinent, mais déjà testé à la main dans la suite originale |
| `read_2.testReadWithSingleByteStream` | `[1]`, fenêtre de 3, résultats 1 puis -1 | La source est plus courte que la fenêtre : c’est ce qui permet de détecter le mutant sur le calcul de longueur dans `fill` |
| `read_2.testReadWithMultipleReads` | `[1,2,3]`, puis deux fois -1 | Vérifie que la fin du flux reste stable, mais recoupe largement `testRead` |
| `read_3.testReadWithBufferFull` | 5 octets lus dans un tableau de taille 5 : on attend 5, le contenu exact, puis -1 | Couvre enfin la surcharge qui n’était pas testée, et détecte plusieurs mutations sur la copie, le retour et la position |
| `read_3.testReadWithZeroLength` | Lecture de longueur 0 sur un flux non vide : retour 0 et tableau inchangé | Cas limite précis, mais qui ne couvre pas toutes les situations de longueur nulle, par exemple sur un flux vide |
| `read_3.testReadWithOffset` | Copies aux indices 2 puis 3, contenu complet vérifié à chaque étape | Vérifie les bornes d’écriture et l’ordre, mais les demandes correspondent exactement aux données restantes |
| `available.testAvailable` | Compteur à 0 au départ, première lecture, épuisement, reset et nouvelle lecture | Attentes corrigées à partir des octets réellement chargés. La valeur 0 au départ reflète le remplissage paresseux |
| `available.testAvailableAfterMark` | Lecture, marqueur à 1, autre lecture, reset : 3 puis 4 octets disponibles | Vérifie le lien entre la position et le marqueur. Les attentes de départ confondaient la taille du tampon et les données |
| `available.testAvailableAfterSkip` | Lecture puis saut de 2 : 2 octets disponibles | Vérifie le compteur après consommation ; attente corrigée à partir des données connues |
| `available.testAvailableAfterClose` | Fermeture avant tout remplissage : 0 disponible | Cas limité : il ne dit rien de la fermeture après un remplissage |
| `markSupported.testMarkSupported` | Flux Mockito, `markSupported()` doit renvoyer vrai | Assertion simple mais utile : elle détecte exactement le mutant qui renvoie false. Un flux en mémoire aurait suffi, Mockito alourdit le test |

Par rapport aux tests écrits à la main, la différence est nette. Les tests originaux vérifient directement, par l’API publique, que le flux d’origine est restauré et correctement lu. Les tests de l’IA passent parfois par la réflexion, et plusieurs se répètent. Nos tests manuels (section 7), eux, partent des mutants survivants et vérifient des valeurs observables, sans réflexion ni champ privé. Autrement dit, ni le nombre de tests ni le fait qu’ils compilent ne suffisent à juger la qualité d’un oracle.

## 6. Analyse PIT avant/après IA

Le code de production est strictement le même dans les trois phases. Pour comparer les mutants d’une phase à l’autre, nous nous sommes basés sur la classe, la méthode, la signature JVM, l’index d’instruction et le mutateur ; la comparaison des rapports XML confirme que les trois ensembles de mutants sont identiques. Les numéros M1 à M33 suivent l’ordre du rapport XML de la phase de référence, sous Java 17.

Statuts : `K` = KILLED, `S` = SURVIVED, `NC` = NO_COVERAGE. Le détecteur indiqué est celui que PIT a enregistré (`killingTest`) ; un autre test pourrait aussi détecter le même mutant.

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

### Pourquoi l’IA détecte-t-elle 9 nouveaux mutants ?

| Mutant | Détecteur PIT | Explication |
|---|---|---|
| M8 | `LookaheadInputStream_read_2_0_Test.testReadWithSingleByteStream()` | Avec une source d’un octet et une fenêtre de 3, la deuxième lecture arrive en fin de flux alors que `buffered` vaut déjà 1. Le mutant remplace la soustraction par l’addition `buffer.length+buffered`, ce qui demande une zone plus grande que le tampon, alors que la soustraction d’origine reste dans les bornes. Le test attend -1 et obtient une exception à la place |
| M15 | `LookaheadInputStream_markSupported_6_0_Test.testMarkSupported()` | Le test appelle `markSupported()` et attend true. Le mutant renvoie false, donc l’assertion échoue directement |
| M23 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | Après une copie complète, `buffered==position`. Le mutant laisse quand même entrer dans la branche de copie et renvoie 0, alors que le test attend -1 à la fin de la fenêtre |
| M25 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | La position doit avancer de la longueur copiée. Avec une soustraction, elle devient négative après la copie, et la lecture suivante échoue au lieu de renvoyer -1 |
| M26 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | La branche de copie est forcée à false même quand 5 octets sont disponibles. Le -1 renvoyé contredit le 5 attendu |
| M27 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | Sans `fill()`, aucun octet n’est chargé dans le tampon. La première lecture renvoie -1 au lieu de 5 |
| M28 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | Sans `arraycopy()`, le tableau de destination reste rempli de zéros. La comparaison exacte avec `[1,2,3,4,5]` détecte la mutation, même si le nombre renvoyé reste 5 |
| M29 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | Le mutant renvoie 0 après une copie réussie, alors que le test attend 5, quel que soit le contenu copié |
| M30 | `LookaheadInputStream_read_3_0_Test.testReadWithBufferFull()` | Quand la fenêtre est épuisée, le mutant renvoie 0 au lieu de -1, ce que l’assertion sur la deuxième lecture détecte |

Ces neuf détections viennent de tests dont nous n’avons pas corrigé les attentes : nos corrections d’`available()` n’y sont pour rien. Après la phase IA, il ne reste plus aucun mutant non couvert, mais cinq survivent encore. Un chemin nouvellement couvert peut donc faire passer un mutant de `NO_COVERAGE` à `SURVIVED` sans pour autant le détecter, comme M24.

## 7. Tests manuels supplémentaires pour les survivants

Nous avons écrit ces [quatre tests](tika-core/src/ift3913-manual/java/org/apache/tika/io/LookaheadInputStreamManualTest.java) après la phase IA, en partant des cinq mutants qui survivaient encore, avec l’aide de Codex (voir la section 11). Ils sont indépendants de ChatUniTest.

| Nom du test | Ce qu’on teste (mutants visés) | Données choisies et pourquoi | Résultat attendu et d’où il vient |
|---|---|---|---|
| `testCloseRestoresNonzeroInitialPosition` | Le marquage fait par le constructeur (M1 : test non-null supprimé, M2 : appel à `stream.mark(n)` supprimé) | Source `[10,20,30,40]` ; on lit 10 avant de créer une fenêtre de 2 | On lit 20 puis 30, puis la fin de la fenêtre. Après `close`, la source doit renvoyer 20, 30, 40 : la documentation promet un retour à la position au moment de la création, pas au début du flux. Sans `mark`, `ByteArrayInputStream` revient à son marqueur initial 0 et renverrait 10 |
| `testBulkReadClampsToRemainingWindowAfterSingleByteRead` | Le calcul `buffered-position` de `read(byte[],int,int)`, remplacé par une addition (M24) | Source de 5 octets, fenêtre de 3 ; on lit 10, puis on demande 5 octets dans un tableau de taille 8, à partir de l’index 2 | Il ne reste que 2 octets dans la fenêtre : on attend 2, le tableau `[0,0,20,30,0,0,0,0]`, puis -1. La demande dépasse volontairement la fenêtre restante tout en restant dans les limites du tableau ; le mutant, lui, copie trop |
| `testSourceEndRestoresUnderlyingPositionAutomatically` | La suppression de `close()` quand `fill()` voit -1 (M14) | Source `[10,20]`, fenêtre de 4 : la source est plus courte que la fenêtre | Lectures 10, 20, -1, puis la source doit renvoyer 10 avant même la fermeture explicite. `fill()` ferme et restaure la source quand elle est épuisée ; sans `close`, elle resterait épuisée. Ce comportement vient de l’implémentation observée et complète le contrat général de restauration |
| `testFullWindowDoesNotResetSourceBeforeClose` | La limite `buffered < buffer.length` changée en `<=` (M7) | Source `[10,20]`, fenêtre d’exactement 2 ; on lit jusqu’à la fin de la fenêtre | Avant `close`, `source.available()` vaut 0 ; après, il vaut 2, et on relit 10. Le remplissage doit s’arrêter quand la fenêtre est pleine. Avec `<=`, une lecture supplémentaire de longueur 0 sur un `ByteArrayInputStream` en fin de flux renvoie -1, ce qui déclenche une restauration trop tôt, visible par le test |

Les cinq survivants sont tous devenus `KILLED`, chacun avec l’un de ces tests comme détecteur dans le rapport XML final. Nous n’avons eu à déclarer aucun mutant équivalent ou inatteignable. Au final, LookaheadInputStream gagne **18 nouveaux tests** (14 IA et 4 manuels), qui passent tous.

## 8. Seconde classe : CharsetUtils

Pour cette deuxième classe, nous avons repris la même démarche qu’aux sections 2 à 7, avec la même configuration Maven. Seuls la classe et le modèle changent (voir la section 3). Pour ne pas les confondre avec les mutants M1–M33 de LookaheadInputStream, les mutants sont ici numérotés de C1 à C25.

### 8.1 Choix de la classe

Nous avons choisi [org.apache.tika.utils.CharsetUtils](tika-core/src/main/java/org/apache/tika/utils/CharsetUtils.java), elle aussi dans `tika-core`. Son rôle est de retrouver le bon encodage à partir des noms souvent mal écrits qu’on trouve dans les documents, comme `"utf-8>"`, `"ISO-8859-1, latin1"` ou `"win-1252"`. Elle a trois méthodes publiques :

- `forName(name)` fait l’essentiel du travail. Elle refuse `null`, ne garde que le premier mot du nom et le cherche en minuscules dans une table de noms et d’alias courants. Elle refuse ensuite `"none"` et `"no"`, corrige les fautes fréquentes du type `…8859-N`, `cp-N` ou `win-N`, interroge ICU4J s’il est installé et, en dernier recours, laisse Java décider avec `Charset.forName`.
- `clean(name)` renvoie le nom officiel trouvé par `forName`, ou `null` si `forName` lève une `IllegalArgumentException`.
- `isSupported(name)` indique si l’encodage est disponible, d’abord selon ICU4J puis selon Java, sans jamais lever d’exception.

ICU4J est une bibliothèque optionnelle que la classe cherche une seule fois, au moment de son chargement. Elle n’est pas dans le classpath de `tika-core`, donc les champs `getCharsetICU` et `isSupportedICU` restent à `null` et tout le code lié à ICU n’est jamais exécuté par les tests. Ce point a beaucoup compté pour la suite.

Au départ, les [quatre tests originaux](tika-core/src/test/java/org/apache/tika/utils/CharsetUtilsTest.java) couvraient 62 lignes sur 81 (76,5 %) et 25 branches sur 32 (78,1 %). `isSupported` était la moins bien testée, avec une seule branche couverte sur quatre. Sur les 25 mutants générés par PIT, 15 étaient tués, 5 survivaient et 5 n’étaient pas couverts, soit un score de 60 %. Les mutants restants concernaient le refus de `"no"`, le retour d’un nom corrigé et tout le chemin ICU.

Nous avons retenu cette classe parce qu’elle est petite (25 mutants, du même ordre que LookaheadInputStream), que les résultats attendus se vérifient avec la documentation de `java.nio.charset` et les tests existants, et qu’elle ne demande aucun fichier externe. Nous avions aussi mesuré `EndianUtils` (31 lignes couvertes sur 121, 206 mutants dont 36 tués), mais avec autant de mutants, une analyse détaillée n’aurait pas tenu dans le délai.

### 8.2 Génération avec ChatUniTest

Nous avons lancé la génération le **10 octobre 2026** ; elle a duré **3 min 01 s**. ChatUniTest a traité chacune des 3 méthodes en un seul tour, sans avoir besoin de réparation automatique. Il a exporté 3 classes de tests, plus une ancienne suite JUnit (`CharsetUtils_Suite`) que nous avons écartée pour la même raison qu’en section 4.

Les trois classes compilaient directement, sans retouche. À l’exécution, en revanche, **un seul test sur trois passait** :

| Méthode / fichier généré | Tours | Compile ? | Résultat avant correction | Ce que nous en avons fait |
|---|---:|---|---|---|
| `isSupported` / `isSupported_1_0_Test` | 1 | Oui | Réussi | Gardé tel quel |
| `clean` / `clean_2_0_Test` | 1 | Oui | Échec | Gardé, après 2 corrections d’attentes |
| `forName` / `forName_3_0_Test` | 1 | Oui | Échec | Gardé, après 1 correction d’attente |

ChatUniTest écrit sa sortie brute dans `tika-core/target/chatunitest/raw/` et ses journaux (erreurs, historique des tentatives) dans `tika-core/target/chatunitest/logs/`. Un `mvn clean` efface ces dossiers, nous en avons donc gardé une copie en dehors du dépôt. Les tests que nous avons intégrés se trouvent dans [tika-core/src/ift3913-ai/java/org/apache/tika/utils](tika-core/src/ift3913-ai/java/org/apache/tika/utils).

Comme pour `available()` (section 4), le plugin affichait « compile and execute successfully » pour `clean` et `forName`, alors que leurs tests échouaient. Nous avons donc relancé les tests nous-mêmes avec Surefire. Autre détail : JUnit s’arrête à la première assertion qui échoue, si bien que l’erreur sur `cp-1252` était cachée derrière celle sur `iso 8859-1`. Nous l’avons repérée en lisant le code et les tests originaux, puis confirmée en relançant les tests après correction.

### 8.3 Corrections des tests intégrés

Nous avons corrigé **3 assertions dans 2 fichiers**. Rien d’autre n’a changé dans les tests générés : ni les entrées, ni les autres assertions.

| Test | Appel | Attendu par l’IA | Après correction | Pourquoi |
|---|---|---|---|---|
| `testClean` | `clean("iso 8859-1")` | `"ISO-8859-1"` | `null` | Tika ne garde que le premier mot, `"iso"`, qui ne correspond à aucun encodage connu, donc `clean` renvoie `null`. Le code contient pourtant un commentaire « Handle "iso 8859-x" error » qui laisse croire le contraire. L’IA s’est fiée au commentaire ; nous avons suivi le comportement réel, mais cet écart entre le commentaire et le code mérite d’être signalé |
| `testClean` | `clean("cp-1252")` | `"cp-1252"` | `"windows-1252"` | Tika transforme `cp-1252` en `cp1252`, un alias Java de windows-1252. Le test original attend d’ailleurs `"windows-1251"` pour `"cp-1251"` |
| `testForName` | `forName("invalid-charset")` | `IllegalCharsetNameException` | `UnsupportedCharsetException` | Le nom est bien écrit, il est seulement inconnu. Dans ce cas, Java lève `UnsupportedCharsetException` ; `IllegalCharsetNameException` est réservée aux noms mal formés. L’IA a confondu les deux |

Il a aussi fallu quelques ajustements techniques dans les 3 fichiers, sans toucher aux vérifications. Nous avons remplacé les imports avec `*` et les imports inutiles par des imports explicites, comme l’exigent les règles Checkstyle `AvoidStarImport` et `UnusedImports` du dépôt, puis laissé Spotless ajouter l’en-tête Apache et formater le code. Après ça, les 3 tests IA passaient avec les tests originaux.

### 8.4 Explication et critique des 3 tests IA retenus

| Test généré | Ce qu’il vérifie | Notre avis |
|---|---|---|
| `isSupported.testIsSupported` | `"UTF-8"` est supporté, `"invalid-charset"` et `null` ne le sont pas | Des cas sensés, mais déjà vérifiés par les tests originaux (`"UTF-8"`, `"bogus"`, `null`). Il n’y a aucun nom mal formé comme `" utf-8"` ou `"charset1; charset2"`, donc le cas `IllegalCharsetNameException` n’est jamais vérifié |
| `clean.testClean` | 13 appels : 9 noms standards qui doivent ressortir à l’identique (`UTF-8`, `UTF-16`, `UTF-32`…), 3 noms à corriger (`iso 8859-1`, `cp-1252`, `win-1252`) et un nom inconnu qui doit donner `null` | Les 9 noms standards testent surtout Java, pas Tika : beaucoup d’assertions pour peu d’information. Les 3 seules assertions qui visaient vraiment les corrections de Tika contenaient 2 erreurs |
| `forName.testForName` | 3 noms courants comparés au résultat de `Charset.forName`, un nom inconnu qui doit lever une exception, et `null` qui doit lever `IllegalArgumentException` | Comparer avec Java est une bonne idée, mais seulement pour des noms que Tika transmet tels quels à Java. Le type d’exception était faux. L’assertion sur `null`, elle, est précise, et c’est elle qui détecte le mutant C3 |

La comparaison avec les tests écrits à la main est parlante. Les tests originaux visent les règles de Tika une par une : les guillemets (`"\"utf-8\""`), les caractères en trop (`"utf-8>"`), les listes (`"ISO-8859-1, latin1"`), les fautes connues (`"iso-8851-1"`, `"koi8r"`), les formes `8859-15`, `cp-1251`, `win1251` et `Windows`, le refus de `"none"` et `"no"`, ainsi que des noms mal formés. Les tests de l’IA sont justes sur les cas faciles, mais ils restent génériques, et c’est précisément sur le comportement propre à Tika qu’ils se trompent. Contrairement à ceux de la section 5, ils n’utilisent ni Mockito ni la réflexion, même si ChatUniTest avait ajouté les imports Mockito.

### 8.5 Analyse PIT avant/après IA

Le code de production n’a pas changé entre les trois analyses, qui portent donc sur les mêmes 25 mutants, sans timeout ni erreur. Les numéros C1 à C25 suivent l’ordre du rapport XML.

Pour les phases IA et finale, nous avons lancé PIT avec `-DfullMutationMatrix=true`. Par défaut, PIT ne retient que le premier test qui tue un mutant ; avec cette option, il les enregistre tous (`killingTests`). C’est ce qui nous permet de savoir ce que les tests IA détectent, même quand un test original détecte déjà le même mutant.

Statuts : `K` = KILLED, `S` = SURVIVED, `NC` = NO_COVERAGE.

| ID | Méthode, ligne | Mutation | Originaux | + IA | + manuels | Détecté par les tests IA | Détecteur de la nouvelle détection |
|---|---|---|---|---|---|---|---|
| C1 | `clean`, 131 | replaced return value with "" (`EmptyObjectReturnValsMutator`) | K | K | K | Oui | `—` |
| C2 | `clean`, 133 | replaced return value with "" (`EmptyObjectReturnValsMutator`) | K | K | K | Oui | `—` |
| C3 | `forName`, 144 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | K | K | K | Oui | `—` |
| C4 | `forName`, 150 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | K | K | K | Non | `—` |
| C5 | `forName`, 157 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | S | S | S | Non | Équivalent (section 8.7) |
| C6 | `forName`, 159 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | K | K | K | Oui | `—` |
| C7 | `forName`, 159 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | S | S | K | Non | `CharsetUtilsManualTest.testForNameRejectsNoAsIllegalCharsetName()` |
| C8 | `forName`, 165 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | K | K | K | Non | `—` |
| C9 | `forName`, 169 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | K | K | K | Oui | `—` |
| C10 | `forName`, 173 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | K | K | K | Oui | `—` |
| C11 | `forName`, 178 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | S | S | K | Non | `CharsetUtilsManualTest.testForNamePrefersRemappedCommonCharsetOverIcu()` |
| C12 | `forName`, 183 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | S | S | K | Non | `CharsetUtilsManualTest.testForNameFallsBackToIcuForUnknownNames()` |
| C13 | `forName`, 186 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | NC | NC | K | Non | `CharsetUtilsManualTest.testForNameFallsBackToIcuForUnknownNames()` |
| C14 | `forName`, 158 | replaced return value with null (`NullReturnValsMutator`) | K | K | K | Oui | `—` |
| C15 | `forName`, 179 | replaced return value with null (`NullReturnValsMutator`) | K | K | K | Oui | `—` |
| C16 | `forName`, 187 | replaced return value with null (`NullReturnValsMutator`) | NC | NC | K | Non | `CharsetUtilsManualTest.testForNameFallsBackToIcuForUnknownNames()` |
| C17 | `forName`, 195 | replaced return value with null (`NullReturnValsMutator`) | K | K | K | Oui | `—` |
| C18 | `isSupported`, 107 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | S | S | K | Non | `CharsetUtilsManualTest.testIsSupportedAcceptsCharsetKnownOnlyToIcu()` |
| C19 | `isSupported`, 107 | removed conditional - replaced equality check with false (`RemoveConditionalMutator_EQUAL_ELSE`) | NC | NC | K | Non | `CharsetUtilsManualTest.testIsSupportedAcceptsCharsetKnownOnlyToIcu()` |
| C20 | `isSupported`, 108 | replaced boolean return with false (`BooleanFalseReturnValsMutator`) | NC | NC | K | Non | `CharsetUtilsManualTest.testIsSupportedAcceptsCharsetKnownOnlyToIcu()` |
| C21 | `isSupported`, 110 | replaced boolean return with false (`BooleanFalseReturnValsMutator`) | K | K | K | Oui | `—` |
| C22 | `isSupported`, 110 | replaced boolean return with true (`BooleanTrueReturnValsMutator`) | K | K | K | Oui | `—` |
| C23 | `isSupported`, 112 | replaced boolean return with true (`BooleanTrueReturnValsMutator`) | K | K | K | Non | `—` |
| C24 | `isSupported`, 115 | replaced boolean return with true (`BooleanTrueReturnValsMutator`) | K | K | K | Oui | `—` |
| C25 | `isSupported`, 118 | replaced boolean return with true (`BooleanTrueReturnValsMutator`) | NC | NC | K | Non | `CharsetUtilsManualTest.testIsSupportedReturnsFalseWhenIcuFails()` |

### Pourquoi l’IA n’a détecté aucun nouveau mutant ?

C’est le résultat le plus marquant de cette partie. D’après la matrice complète, les tests IA tuent **12 mutants sur 25**, mais les tests originaux tuaient déjà ces 12 mutants. Il n’y a donc aucune nouvelle détection, et la couverture reste la même. Pour LookaheadInputStream, l’IA avait trouvé une méthode que personne ne testait. Ici, les tests originaux couvraient déjà les trois méthodes publiques, et l’IA a surtout refait des cas existants.

Voici pourquoi les tests IA détectent ces 12 mutants :

| Mutant | Test IA | Explication |
|---|---|---|
| C1 | `testClean` | `clean` renvoie `""` au lieu du nom trouvé, alors que le test attend par exemple `"UTF-8"` |
| C2 | `testClean` | `clean` renvoie `""` au lieu de `null` pour un nom inconnu, alors que le test attend `null` pour `"invalid-charset"` |
| C3 | `testForName` | Sans la vérification de `null`, `forName(null)` plante avec une `NullPointerException` au lieu de lever `IllegalArgumentException` |
| C6 | `testClean`, `testForName` | La double condition sur `"none"` et `"no"` est compilée en deux sauts. D’après les tests qui le tuent, ce mutant fait refuser tous les noms absents de la table : `clean("US-ASCII")` renvoie alors `null`, et `forName("invalid-charset")` lève la mauvaise exception |
| C9 | `testClean` | La correction `cp-N` ne se fait plus, donc `clean("cp-1252")` renvoie `null`. Cette détection repose sur une assertion que nous avons corrigée |
| C10 | `testClean` | La correction `win-N` ne se fait plus : `clean("win-1252")` renvoie `null` au lieu de `"windows-1252"` |
| C14 | `testClean`, `testForName` | `forName` renvoie `null` pour un nom de la table : `forName("UTF-8")` ne donne plus UTF-8, et `clean` plante en appelant `.name()` |
| C15 | `testClean` | `null` au lieu de l’encodage corrigé : `clean("win-1252")` plante avec une `NullPointerException` |
| C17 | `testClean` | `null` au lieu de la réponse de Java pour un nom hors de la table : `clean("US-ASCII")` plante |
| C21 | `testIsSupported` | `isSupported` renvoie toujours `false`, alors que `"UTF-8"` doit être supporté |
| C22 | `testIsSupported` | `isSupported` renvoie toujours `true`, alors que `"invalid-charset"` ne l’est pas |
| C24 | `testIsSupported` | `isSupported(null)` renvoie `true` au lieu de `false` |

Trois mutants sont tués par les tests originaux mais pas par ceux de l’IA. C4 demande un nom vide (`""`), que l’IA n’a jamais essayé. C8 demande une forme comme `8859-15`, sans espace : le `"iso 8859-1"` de l’IA visait justement cette règle, mais l’espace le réduit à `"iso"` avant qu’il ne l’atteigne. C23 demande un nom mal formé comme `" utf-8"`, alors que `"invalid-charset"` est bien formé. Quant aux 10 autres mutants, aucun test ne les tuait : aucun n’appelle `forName("no")` directement, et le chemin ICU est inaccessible sans ICU4J.

Précision : 11 des 12 détections viennent d’assertions écrites par l’IA, telles quelles. Seule celle de C9 dépend d’une de nos corrections.

### 8.6 Tests manuels supplémentaires pour les mutants restants

Nous avons écrit ces [cinq tests](tika-core/src/ift3913-manual/java/org/apache/tika/utils/CharsetUtilsManualTest.java) après la phase IA, en partant des 10 mutants encore vivants, avec l’aide de Claude (voir la section 11). Ils sont indépendants de ChatUniTest.

**Comment simuler ICU4J ?** Le problème, c’est que 8 des mutants restants se trouvent dans le code lié à ICU, qui ne s’exécute jamais dans `tika-core`. `CharsetUtils` garde les deux méthodes d’ICU4J dans des champs privés statiques, `getCharsetICU` et `isSupportedICU`, qui valent `null` ici. Dans les tests 2 à 5, nous remplaçons temporairement ces champs, par réflexion, par les méthodes d’une fausse classe `FakeCharsetICU` :

- son `forNameICU` renvoie toujours UTF-16LE. Aucune règle de Tika ne donne cet encodage pour les noms utilisés, donc obtenir UTF-16LE signifie que c’est ICU qui a répondu ;
- son `isSupported` répond vrai pour `"tika-fake-icu"`, lève une exception pour `"tika-fake-icu-error"` et répond faux dans les autres cas.

Les champs sont sauvegardés avant chaque test (`@BeforeEach`) et remis en place après (`@AfterEach`), pour ne pas perturber les autres tests. Nous avons préféré cette solution à l’ajout de la vraie bibliothèque ICU4J : la classe ne cherchant ICU qu’une seule fois, l’ajouter aurait changé le comportement de tous les tests de `tika-core` lancés dans la même JVM. Le défaut de cette approche, c’est que nos tests dépendent du nom de deux champs privés. C’est justement ce que la section 5 reproche aux tests IA, mais ici, c’était le seul moyen d’atteindre ce code sans modifier les dépendances du projet.

| Nom du test | Ce qu’on teste (mutants visés) | Données choisies et pourquoi | Résultat attendu et d’où il vient |
|---|---|---|---|
| `testForNameRejectsNoAsIllegalCharsetName` | `"no"` doit être refusé comme nom illégal (C7) | `"no"`, la valeur exacte vérifiée à la ligne 159 (`"none"` était déjà testé). Pour comparer, `"bogus-name"`, un nom bien écrit mais inconnu | Les lignes 159–160 lèvent `IllegalCharsetNameException` pour `"no"`. Avec C7, `"no"` arrive jusqu’à `Charset.forName("no")`, qui lève `UnsupportedCharsetException`. Les tests originaux passaient par `clean("no")`, qui renvoie `null` dans les deux cas et ne permettait pas de voir la différence. Le second appel montre que les deux chemins donnent bien des exceptions différentes |
| `testForNameFallsBackToIcuForUnknownNames` | Pour un nom que Tika ne connaît pas, `forName` doit demander à ICU (C12 : ICU jamais appelé, C13 : réponse ignorée, C16 : `null` renvoyé) | `"tika-fake-icu"` : bien écrit, absent de la table, impossible à corriger et inconnu de Java. Seul ICU peut répondre | La Javadoc de `forName` indique qu’elle « optionally uses ICU4J's CharsetICU.forNameICU, if it is found on the classpath ». Notre faux ICU répond UTF-16LE, c’est donc ce qu’on attend. Avec C12 ou C13, on retombe sur `Charset.forName`, qui lève `UnsupportedCharsetException` ; avec C16, on obtient `null` |
| `testForNamePrefersRemappedCommonCharsetOverIcu` | Un nom corrigé par Tika doit l’emporter sur ICU (C11 : le retour du nom corrigé est sauté) | `"win-1252"` : absent de la table tel quel, mais corrigé en `"windows-1252"` par Tika. C’est la même forme que dans le test original `clean("win-1251")` | Les lignes 178–179 renvoient l’encodage corrigé avant d’arriver au bloc ICU (ligne 183), donc on attend windows-1252. Avec C11, on arrive au faux ICU, qui répond UTF-16LE. Sans ICU, le mutant retombait sur `Charset.forName("windows-1252")`, qui donne le même résultat : c’est pour ça qu’il survivait |
| `testIsSupportedAcceptsCharsetKnownOnlyToIcu` | `isSupported` doit répondre vrai quand seul ICU connaît le nom (C18, C19, C20) | `"tika-fake-icu"` : Java répond non, notre faux ICU répond oui. Un résultat vrai ne peut donc venir que d’ICU | Aux lignes 107–108, si ICU est présent et répond oui, la méthode renvoie `true`. C18 (ICU jamais consulté), C19 (réponse ignorée) et C20 (`false` renvoyé) donnent tous `false` |
| `testIsSupportedReturnsFalseWhenIcuFails` | `isSupported` ne doit jamais lever d’exception, même si ICU plante (C25) | `"tika-fake-icu-error"` : notre faux ICU lève une `IllegalStateException`. Elle arrive enveloppée dans une `InvocationTargetException`, que seul le dernier `catch (Exception e)` attrape | La Javadoc promet une réponse « without throwing exceptions », et ce `catch` renvoie `false` (ligne 118). C25 renvoie `true` à la place |

Les 9 mutants visés sont tous devenus `KILLED`, et chacun a l’un de ces tests comme détecteur dans le rapport XML final. Au total, `CharsetUtils` gagne **8 nouveaux tests** (3 IA et 5 manuels), qui passent tous.

### 8.7 Mutant équivalent C5

C5 supprime le `return` des lignes 157–158, celui qui renvoie l’encodage dès qu’un nom est trouvé dans la table. Seulement, l’exécution continue avec la même valeur dans `charset`, et la ligne 178 la renvoie quand même, avant d’arriver à ICU. Pour obtenir un résultat différent, il faudrait un nom présent dans la table qu’une des corrections (`…8859-N`, `cp-N`, `win-N`) transforme en un autre encodage, ou en un nom absent de la table. Nous l’avons vérifié avec un petit script `jshell` qui reconstruit la table (243 noms et alias, JDK 23.0.1) : aucun nom ne pose problème. C5 est donc équivalent, avec ou sans ICU. Aucun test ne peut le tuer, et 24/25 (96 %) est le meilleur score possible pour cette classe.

## 9. Commandes Maven

### LookaheadInputStream (macOS, bash)

Toutes les commandes se lancent depuis la racine du fork, avec Java 17. Sur le Mac utilisé :

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Tests originaux seuls, puis PIT :

```bash
mvn -B -ntp -pl tika-core -am clean install -Pci
mvn -B -ntp -pl tika-core -Pift3913-pit \
  org.pitest:pitest-maven:mutationCoverage -Dift3913.stage=baseline
```

Avec les tests IA corrigés, puis PIT :

```bash
mvn -B -ntp -pl tika-core -am clean test -Pci,ift3913-ai
mvn -B -ntp -pl tika-core -Pift3913-pit,ift3913-ai \
  org.pitest:pitest-maven:mutationCoverage -Dift3913.stage=ai
```

Avec les tests IA et les tests manuels, puis PIT :

```bash
mvn -B -ntp -pl tika-core -am clean verify -Pci,ift3913-ai,ift3913-manual
mvn -B -ntp -pl tika-core -Pift3913-pit,ift3913-ai,ift3913-manual \
  org.pitest:pitest-maven:mutationCoverage -Dift3913.stage=final
```

Le rapport JaCoCo se trouve dans `tika-core/target/site/jacoco/index.html` et les rapports PIT dans `tika-core/target/pit-reports/PHASE/index.html`. Attention : chaque `clean` efface les résultats précédents, il faut donc noter les chiffres avant de passer à la phase suivante. L’avantage, c’est qu’aucun test compilé dans une phase ne vient fausser la suivante.

Pour la génération, démarrer Ollama si besoin, puis lancer :

```bash
ollama pull qwen2.5-coder:14b
ollama cp qwen2.5-coder:14b code-llama
mvn -B -ntp -pl tika-core -am install -Pci -DskipTests
caffeinate -i mvn -B -ntp -pl tika-core -Pci,ift3913-chatunitest \
  io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:class \
  -DselectClass=org.apache.tika.io.LookaheadInputStream \
  -Dift3913.model=code-llama
```

Les sorties temporaires arrivent dans `tika-core/target/chatunitest/`. Sur macOS, il faut garder le capot ouvert ; `caffeinate` empêche la mise en veille. Sur les autres systèmes, il suffit de retirer `caffeinate -i`.

### CharsetUtils (Windows, PowerShell)

Ces commandes se lancent depuis la racine du fork, avec le Maven wrapper. Sous PowerShell, il faut mettre les listes de profils entre guillemets, sinon la virgule pose problème. Pour la génération, Ollama doit tourner avec `OLLAMA_CONTEXT_LENGTH=16384` :

```powershell
ollama pull codeqwen:v1.5-chat
.\mvnw.cmd -B -ntp -pl tika-core -am install "-Pci" -DskipTests
.\mvnw.cmd -B -ntp -pl tika-core "-Pci,ift3913-chatunitest" io.github.zju-aces-ise:chatunitest-maven-plugin:2.1.1:class "-DselectClass=org.apache.tika.utils.CharsetUtils" "-Dift3913.model=codeqwen:v1.5-chat"
```

Ensuite, les trois phases (originaux seuls, puis + IA, puis + manuels), chacune suivie de son analyse PIT :

```powershell
.\mvnw.cmd -B -ntp -pl tika-core -am clean install "-Pci"
.\mvnw.cmd -B -ntp -pl tika-core "-Pift3913-pit" org.pitest:pitest-maven:mutationCoverage "-Dift3913.classes=org.apache.tika.utils.CharsetUtils" "-Dift3913.stage=charset-baseline"

.\mvnw.cmd -B -ntp -pl tika-core -am clean test "-Pci,ift3913-ai"
.\mvnw.cmd -B -ntp -pl tika-core "-Pift3913-pit,ift3913-ai" org.pitest:pitest-maven:mutationCoverage "-Dift3913.classes=org.apache.tika.utils.CharsetUtils" "-Dift3913.stage=charset-ai" "-DfullMutationMatrix=true"

.\mvnw.cmd -B -ntp -pl tika-core -am clean verify "-Pci,ift3913-ai,ift3913-manual"
.\mvnw.cmd -B -ntp -pl tika-core "-Pift3913-pit,ift3913-ai,ift3913-manual" org.pitest:pitest-maven:mutationCoverage "-Dift3913.classes=org.apache.tika.utils.CharsetUtils" "-Dift3913.stage=charset-final" "-DfullMutationMatrix=true"
```

Les rapports PIT se trouvent dans `tika-core/target/pit-reports/charset-*/index.html`. Après avoir ajouté ou modifié un test, lancer `.\mvnw.cmd -pl tika-core spotless:apply` (sans `-Pci`) pour ajouter l’en-tête Apache et formater le code. Avec `-Pci`, Spotless ne fait que vérifier, et le build échoue si un fichier n’est pas formaté.

## 10. GitHub Actions et remise

Le [workflow dédié](.github/workflows/ift3913-tache2.yml) utilise Temurin Java 17 et lance directement `mvn -B -ntp -pl tika-core -am clean verify -Pci,ift3913-ai,ift3913-manual`. Il exécute donc les tests IA et les tests supplémentaires en même temps que les tests originaux. Ollama n’est pas nécessaire en CI, puisque les tests générés sont déjà dans le dépôt.

Pour LookaheadInputStream, après le retrait des fichiers auxiliaires, le [workflow simplifié a réussi le 7 octobre 2026](https://github.com/anasys0x/tika/actions/runs/37669952167), au commit `8aefb69dbbb5360205305fe8eb80d39a2306b14f` : 767 tests core, aucun échec, aucune erreur et 2 ignorés, et les 18 nouveaux tests passent. Les résultats des trois phases de la section 1 avaient aussi été confirmés par le [premier run GitHub Actions](https://github.com/anasys0x/tika/actions/runs/37666427788), au commit `a8ebf0c355ef2340b3311b6f2ef41bd03ea033d5`. La mise à jour de ce lien dans le README ne modifie pas les sources validées.

Pour `CharsetUtils`, nous avons d’abord lancé la commande du workflow sur notre machine Windows (Java 23.0.1) : 775 tests core, aucun échec, aucune erreur et 3 ignorés. Les 8 nouveaux tests de `CharsetUtils` passent, sans violation Checkstyle ni appel interdit relevé par forbiddenapis. Run GitHub Actions après intégration dans `tache2` : **[À COMPLÉTER : lien du run et commit]**.

Nous nous sommes inscrits avec la [PR nº 1239](https://github.com/umontreal-diro/IFT3913/pull/1239). Pour la remise finale, nous ajoutons les liens du fork et de ce README dans `tache2/MRANI_ALAOUI-RACHIDI/readme.md` du dépôt du cours, avec une seconde PR. D’après les [instructions de remise](https://github.com/umontreal-diro/IFT3913/blob/2026/instructions-PR.md), cette PR peut être envoyée à partir du **9 octobre 2026**, et la date limite est le **13 octobre 2026 à 17 h EDT**.

## 11. Déclaration d’usage de l’IA

Pour `LookaheadInputStream`, nous avons généré les tests les 6 et 7 octobre 2026 avec ChatUniTest 2.1.1 et Qwen2.5-Coder 14B, en local via Ollama. L’outil construit lui-même le contexte envoyé au modèle (méthode ciblée, classe et dépendances), puis lui demande des réparations à partir des erreurs de compilation et d’exécution.

Codex nous a aidés à lire les consignes, inspecter le fork, mesurer JaCoCo et PIT, préparer Maven et GitHub Actions, diagnostiquer les erreurs, corriger les cinq attentes d’`available()`, intégrer les tests, écrire les quatre tests supplémentaires à partir des survivants et rédiger le README. Nous ne pouvons pas préciser le modèle exact utilisé par Codex à chaque intervention. Les corrections et les tests supplémentaires écrits avec Codex sont déclarés comme assistés par IA, à distinguer de la génération et des réparations automatiques de ChatUniTest.

Pour `CharsetUtils` (section 8), nous avons généré les tests le 10 octobre 2026 avec ChatUniTest 2.1.1 et CodeQwen 1.5 7B, en local via Ollama : une génération par méthode, sans réparation automatique.

Nous avons aussi été aidés par Claude (Anthropic, modèle Claude Opus 5.5, utilisé via Claude Code). Il nous a aidés à installer Ollama et le modèle, à vérifier que ChatUniTest et PIT fonctionnent avec JUnit 6, à mesurer les classes candidates avec JaCoCo et PIT, et à lancer la génération et les analyses de mutation. Il a aussi participé au diagnostic et à la correction des trois attentes, à l’adaptation des imports, à l’écriture des cinq tests supplémentaires à partir des mutants restants et à la vérification de l’équivalence de C5 avec un script `jshell`. Il a rédigé la section 8 et les ajouts sur `CharsetUtils` dans les autres sections. Enfin, à notre demande, il a reformulé l’ensemble du README dans un style plus naturel, sans changer les résultats, les tableaux de données ni les commandes. Ces interventions sont déclarées comme assistées par IA, à distinguer de la génération automatique faite par ChatUniTest.

Conformément aux [règles d’usage de l’IA du cours](https://github.com/umontreal-diro/IFT3913/tree/2026#usage-de-lintelligence-artificielle), nous devons comprendre et valider chaque contribution retenue. Tous les pourcentages viennent de nos exécutions de Maven, JaCoCo et PIT, et non d’estimations faites par une IA.

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
