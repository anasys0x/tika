# IFT3913 — Tâche 2 : augmentation des tests de Tika

| Nom complet | Identifiant GitHub |
|---|---|
| Mrani Alaoui Anas | Anasys0x |
| Rachidi Aymane | Aymtrack |

[Dépôt de la tâche](https://github.com/anasys0x/tika/tree/tache2) · [Consignes du professeur](https://github.com/umontreal-diro/IFT3913/tree/2026/tache2)

## 1. Choix de la classe

Nous avons retenu [LookaheadInputStream](tika-core/src/main/java/org/apache/tika/io/LookaheadInputStream.java), dans le module autorisé `tika-core`. Elle possède déjà [six tests](tika-core/src/test/java/org/apache/tika/io/LookaheadInputStreamTest.java), mais sa couverture initiale était de **80 % des lignes** et **87,50 % des branches**. La lecture dans un tableau et `markSupported()` n’étaient pas couvertes.

PIT trouvait **5 mutants survivants et 9 non couverts**. Cette classe permet donc d’étudier des comportements insuffisamment testés. Elle se teste avec des flux en mémoire, dont les octets et la position sont faciles à contrôler.

## 2. Génération avec ChatUniTest et corrections

ChatUniTest est intégré à Maven dans [tika-core/pom.xml](tika-core/pom.xml). Le modèle utilisé est **Qwen2.5-Coder 14B**, ouvert et exécuté **localement avec Ollama**. L’alias `code-llama` dans la configuration désigne ce même modèle : il permet de contourner la liste de noms acceptés par ChatUniTest.

Un premier essai n’a exporté aucun test valide. La configuration a été adaptée pour utiliser Java 17 et ajouter les dépendances Mockito nécessaires aux propositions générées. Le deuxième essai a produit **5 classes contenant 14 tests** : ils compilent, mais **11 passent et 3 échouent avant correction**.

Les tests retenus sont dans [tika-core/src/ift3913-ai/java/org/apache/tika/io](tika-core/src/ift3913-ai/java/org/apache/tika/io) :

| Fichier | Tests | Résultat avant correction |
|---|---:|---|
| `LookaheadInputStream_close_0_0_Test.java` | 2 | Tous réussis |
| `LookaheadInputStream_read_2_0_Test.java` | 4 | Tous réussis |
| `LookaheadInputStream_read_3_0_Test.java` | 3 | Tous réussis |
| `LookaheadInputStream_available_5_0_Test.java` | 4 | 1 réussi, 3 en échec |
| `LookaheadInputStream_markSupported_6_0_Test.java` | 1 | Réussi |

Les candidats pour `skip`, `mark` et `reset` n’ont pas été intégrés : ils ne compilaient pas, à cause d’un import de réflexion manquant ou d’exceptions non déclarées. L’ancienne suite JUnit exportée a également été écartée ; Maven découvre directement les cinq classes de tests.

Pour la lecture dans un tableau, ChatUniTest a filtré automatiquement 5 propositions incorrectes avant d’exporter les 3 tests retenus : tableaux de tailles différentes, exception attendue incorrecte, séquence attendue incorrecte et lectures hors des bornes. Ce filtrage automatique est distinct des corrections apportées ci-dessous.

### Corrections nécessaires

Les trois tests en échec confondaient la capacité du tampon (10) avec les données réellement présentes dans la source (5 octets). Nous avons corrigé **5 assertions dans 3 tests du fichier `available`** :

| Test et moment de la vérification | Avant | Après | Justification |
|---|---:|---:|---|
| `testAvailable`, après une lecture | 9 | 4 | Un octet consommé sur 5 |
| `testAvailable`, après reset puis lecture | 9 | 4 | Retour au début, puis un octet consommé |
| `testAvailableAfterMark`, après deux lectures | 8 | 3 | Deux octets consommés sur 5 |
| `testAvailableAfterMark`, après reset | 9 | 4 | Retour au marqueur placé après le premier octet |
| `testAvailableAfterSkip` | 7 | 2 | Un octet lu et deux sautés sur 5 |

Les cinq fichiers ont aussi reçu l’en-tête Apache, des imports explicites et le formatage du projet. Les assertions des quatre autres fichiers sont inchangées. Après ces interventions, **les 14 tests réussissent**.

## 3. Explication et critique des tests générés

Un oracle est le résultat attendu que le test vérifie. Les noms ci-dessous correspondent aux méthodes des cinq fichiers précédents.

| Test | Entrée et résultat attendu | Pertinence ou limite |
|---|---|---|
| `close.testClose` | Fermer un flux `[1,2,3]` ; le champ `stream` doit être nul | Vérifie un état interne par réflexion, sans vérifier la restauration du flux |
| `close.testCloseWithNullStream` | Forcer `stream` à null, puis fermer ; il reste nul | Couvre un cas particulier, mais construit artificiellement l’état interne |
| `read_2.testRead` | Lire `[1,2,3]` ; attendre 1, 2, 3 puis -1 | Oracle précis ; réflexion inutile sur une méthode publique |
| `read_2.testReadWithEmptyStream` | Flux vide ; attendre -1 | Cas pertinent, déjà couvert par les tests originaux |
| `read_2.testReadWithSingleByteStream` | Source `[1]`, fenêtre 3 ; attendre 1 puis -1 | Vérifie une source plus courte que la fenêtre et détecte un mutant de remplissage |
| `read_2.testReadWithMultipleReads` | Lire `[1,2,3]`, puis attendre deux fois -1 | Vérifie la stabilité de la fin de flux ; recoupe largement `testRead` |
| `read_3.testReadWithBufferFull` | Copier `[1,2,3,4,5]` ; attendre le compte 5, le contenu exact puis -1 | Couvre une méthode auparavant absente et détecte plusieurs mutants |
| `read_3.testReadWithZeroLength` | Demander 0 octet ; attendre 0 et un tableau inchangé | Bon cas limite, sans couvrir la même demande sur une source vide |
| `read_3.testReadWithOffset` | Copier aux indices 2 puis 3 ; vérifier les tableaux obtenus | Vérifie le placement et l’ordre des octets |
| `available.testAvailable` | Avant lecture, attendre 0 ; après lecture, 4 ; après épuisement, 0 ; après reset et lecture, 4 | Vérifie le compteur ; les attentes initiales étaient incorrectes |
| `available.testAvailableAfterMark` | Lire, marquer, relire puis reset ; attendre 3 puis 4 disponibles | Vérifie le lien entre position et marqueur après correction |
| `available.testAvailableAfterSkip` | Lire un octet puis en sauter deux ; attendre 2 disponibles | Oracle précis fondé sur les 5 octets de la source |
| `available.testAvailableAfterClose` | Fermer avant toute lecture ; attendre 0 disponible | Cas limité : ne vérifie pas la fermeture après remplissage |
| `markSupported.testMarkSupported` | Attendre `true` | Vérification simple qui détecte le mutant retournant `false` ; Mockito est plus lourd qu’un flux mémoire |

Les tests originaux écrits à la main vérifient directement la lecture et la restauration du flux par l’API publique. Les tests IA apportent notamment la lecture dans un tableau, mais certains sont redondants ou dépendent des champs internes. Les assertions sur le contenu exact des tableaux sont plus précises qu’une simple vérification d’absence d’exception.

## 4. Scores PIT et mutants détectés

Le code de production et les tests originaux sont inchangés. Les trois analyses utilisent les mêmes **33 mutants**, avec les mutateurs PIT `DEFAULTS`, sans timeout ni erreur.

| Tests utilisés | Lignes couvertes | Branches couvertes | Mutants tués | Score PIT | Survivants | Non couverts |
|---|---:|---:|---:|---:|---:|---:|
| Originaux seuls | 32/40 — 80 % | 14/16 — 87,50 % | 19/33 | 57,58 % | 5 | 9 |
| Originaux + IA corrigés | 40/40 — 100 % | 16/16 — 100 % | 28/33 | 84,85 % | 5 | 0 |
| Originaux + IA + supplémentaires | 40/40 — 100 % | 16/16 — 100 % | 33/33 | 100 % | 0 | 0 |

Score : `100 × mutants tués / mutants générés`, en incluant les non couverts dans le total. Le résultat de 100 % concerne cette classe et ces mutateurs.

### Les 9 mutants supplémentaires détectés par les tests IA

`testReadWithSingleByteStream` détecte le premier mutant ci-dessous ; `testMarkSupported` détecte le deuxième. Les sept suivants sont détectés par `testReadWithBufferFull`, dans le fichier `read_3`.

| Méthode et ligne | Mutation | Pourquoi le test la détecte |
|---|---|---|
| `fill`, 82 | `buffer.length - buffered` devient une addition | Avec un octet et une fenêtre de 3, la deuxième lecture dépasse le tampon au lieu de renvoyer -1 |
| `markSupported`, 129 | Retour `false` | L’assertion attend `true` |
| `read(byte[],int,int)`, 104 | `buffered > position` devient `>=` | Après la copie complète, le mutant renvoie 0 alors que le test attend -1 |
| `read(byte[],int,int)`, 107 | Avancement de position remplacé par une soustraction | La position devient négative ; la lecture suivante échoue au lieu de renvoyer -1 |
| `read(byte[],int,int)`, 104 | Condition forcée à false | La première lecture renvoie -1 au lieu des 5 octets attendus |
| `read(byte[],int,int)`, 103 | Suppression de `fill()` | Le tampon reste vide ; le retour est -1 au lieu de 5 |
| `read(byte[],int,int)`, 106 | Suppression de `arraycopy()` | Le tableau contient des zéros au lieu de `[1,2,3,4,5]` |
| `read(byte[],int,int)`, 108 | Retour remplacé par 0 | Le test attend un compte de 5 après la copie |
| `read(byte[],int,int)`, 110 | Retour de fin remplacé par 0 | Le test attend -1 après épuisement de la fenêtre |

Ces neuf détections proviennent de tests dont les assertions n’ont pas été corrigées. Les corrections d’`available()` ne sont donc pas responsables de l’amélioration du score. Malgré 100 % de couverture après IA, cinq mutants survivent : couvrir une ligne ne suffit pas à détecter une erreur.

## 5. Tests supplémentaires pour les mutants restants

Les quatre tests sont dans [LookaheadInputStreamManualTest.java](tika-core/src/ift3913-manual/java/org/apache/tika/io/LookaheadInputStreamManualTest.java). Ils ont été écrits avec l’aide de Codex, séparément de ChatUniTest, à partir des cinq mutants encore vivants.

| Test | Intention et mutant ciblé | Données choisies et motivation | Oracle et justification |
|---|---|---|---|
| `testCloseRestoresNonzeroInitialPosition` | Vérifier le marquage du constructeur ; détecter la suppression de sa condition ou de `mark()` | Source `[10,20,30,40]` ; consommer 10 avant de créer la fenêtre de 2, pour distinguer début de source et début de fenêtre | Lire 20,30,-1 dans la fenêtre ; après fermeture, la source doit renvoyer 20,30,40. Le contrat garantit la restauration à la position de création. Sans marquage, elle revient à 10 |
| `testBulkReadClampsToRemainingWindowAfterSingleByteRead` | Vérifier la longueur copiée ; détecter `buffered-position` remplacé par une addition | Source `[10,20,30,40,50]`, fenêtre 3 ; lire 10, puis demander 5 octets à l’index 2 d’un tableau de taille 8. La demande dépasse la fenêtre restante, mais respecte le tableau | Deux octets restent : retour 2 et tableau `[0,0,20,30,0,0,0,0]`, puis -1. L’attendu découle de la limite de la fenêtre |
| `testSourceEndRestoresUnderlyingPositionAutomatically` | Vérifier la restauration à la fin de la source ; détecter la suppression de `close()` dans `fill()` | Source `[10,20]`, fenêtre 4 : provoquer la fin de source avant de remplir la fenêtre | Après 10,20,-1, la source doit renvoyer 10 avant la fermeture explicite. Cet oracle suit le comportement de `fill()`, qui ferme et restaure à la fin de source |
| `testFullWindowDoesNotResetSourceBeforeClose` | Vérifier la frontière de remplissage ; détecter `<` remplacé par `<=` | Source `[10,20]`, fenêtre exactement 2 : atteindre précisément la frontière | Après 10,20,-1, la source reste épuisée ; après fermeture, elle retrouve 2 octets. Avec `<=`, une lecture supplémentaire à EOF déclenche une restauration prématurée. L’oracle est lié à cette implémentation |

Le premier test détecte deux mutants ; chacun des trois autres en détecte un. **Les 33 mutants sont finalement tués**, sans mutant déclaré équivalent. Les 18 nouveaux tests (14 IA et 4 supplémentaires) réussissent.

## 6. Exécution dans GitHub Actions

Le [workflow](.github/workflows/ift3913-tache2.yml) exécute les tests originaux et les nouveaux tests avec Maven :

```bash
mvn -B -ntp -pl tika-core -am clean verify -Pci,ift3913-ai,ift3913-manual
```

[GitHub Actions a réussi le 7 octobre 2026](https://github.com/anasys0x/tika/actions/runs/37669952167) : **767 tests, zéro échec, zéro erreur et 2 ignorés**. Les 18 nouveaux tests ont tous réussi. Le [premier run](https://github.com/anasys0x/tika/actions/runs/37666427788) confirme aussi la comparaison des trois phases.

## 7. Déclaration d’usage de l’IA

Les 6–7 octobre 2026, ChatUniTest avec Qwen2.5-Coder 14B local a généré les tests à partir des méthodes ciblées et du contexte fourni par l’outil. Les réparations automatiques utilisaient les erreurs de compilation et d’exécution.

Codex a aidé à configurer les outils, mesurer les résultats, corriger les cinq assertions, intégrer les tests, écrire les quatre tests supplémentaires et rédiger ce README. Ces interventions assistées par IA sont distinctes de la génération automatique de ChatUniTest. Le nom exact du modèle sous-jacent à chaque intervention de Codex n’est pas attesté. Le binôme doit comprendre et valider les contributions, conformément aux [règles du cours](https://github.com/umontreal-diro/IFT3913/tree/2026#usage-de-lintelligence-artificielle).

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
