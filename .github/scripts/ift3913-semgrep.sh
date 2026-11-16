#!/usr/bin/env bash
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -euo pipefail

baseline="${BASE_SHA:?Le commit de référence est requis}"
if [[ "$baseline" == '0000000000000000000000000000000000000000' ]]; then
  baseline="$(git merge-base HEAD refs/remotes/origin/tache2)"
fi
if [[ ! "$baseline" =~ ^[a-f0-9]{40}$ ]] || ! git cat-file -e "$baseline^{commit}"; then
  echo '::error::Impossible de trouver le commit de référence pour Semgrep.'
  exit 2
fi

printf 'Semgrep baseline: %s; scanned commit: %s\n' "$baseline" "$(git rev-parse HEAD)"
exec semgrep scan --config "${SEMGREP_CONFIG:-p/java}" --baseline-commit "$baseline" \
  --error --strict --metrics=off --disable-version-check --no-rewrite-rule-ids \
  --json-output "${RUNNER_TEMP:?Le répertoire des rapports est requis}/semgrep-diff.json"
