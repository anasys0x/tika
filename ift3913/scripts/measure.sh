#!/usr/bin/env bash
# Reproduce all phases in order with fresh classes, preserving each report before clean.
set -euo pipefail
IFT3913_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$IFT3913_ROOT"
if [[ -z "${JAVA_HOME:-}" && -d /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ]]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
fi
if [[ -n "${JAVA_HOME:-}" ]]; then export PATH="$JAVA_HOME/bin:$PATH"; fi
mvn -version
IFT3913_OUT="${IFT3913_EVIDENCE_DIR:-$IFT3913_ROOT/ift3913/evidence/reproduction-$(date +%Y%m%d-%H%M%S)}"
mkdir -p "$IFT3913_OUT"
for IFT3913_PHASE in baseline-java17 ai final; do
    IFT3913_PROFILES=ci
    IFT3913_GOAL=test
    IFT3913_PIT_PROFILES=ift3913-pit
    if [[ "$IFT3913_PHASE" != baseline-java17 ]]; then
        IFT3913_PROFILES+=,ift3913-ai
        IFT3913_PIT_PROFILES+=,ift3913-ai
    fi
    if [[ "$IFT3913_PHASE" == final ]]; then
        IFT3913_PROFILES+=,ift3913-manual
        IFT3913_PIT_PROFILES+=,ift3913-manual
        IFT3913_GOAL=verify
    fi
    mkdir -p "$IFT3913_OUT/$IFT3913_PHASE"
    mvn -B -ntp -pl tika-core -am clean "$IFT3913_GOAL" -P"$IFT3913_PROFILES" \
        2>&1 | tee "$IFT3913_OUT/$IFT3913_PHASE/tests.log"
    cp tika-core/target/site/jacoco/jacoco.xml tika-core/target/site/jacoco/jacoco.csv "$IFT3913_OUT/$IFT3913_PHASE/"
    # The standalone PIT goal resolves internal reactor artifacts from the local repository.
    if [[ "$IFT3913_PHASE" == baseline-java17 ]]; then
        mvn -B -ntp -pl tika-core -am install -Pci -DskipTests \
            2>&1 | tee "$IFT3913_OUT/install.log"
    fi
    mvn -B -ntp -pl tika-core -P"$IFT3913_PIT_PROFILES" \
        org.pitest:pitest-maven:mutationCoverage \
        -Dift3913.classes=org.apache.tika.io.LookaheadInputStream \
        -Dift3913.stage="$IFT3913_PHASE" \
        2>&1 | tee "$IFT3913_OUT/$IFT3913_PHASE/pit.log"
    cp -R "tika-core/target/pit-reports/$IFT3913_PHASE" "$IFT3913_OUT/$IFT3913_PHASE/pit-report"
done
python3 ift3913/scripts/summarize.py "$IFT3913_OUT"
