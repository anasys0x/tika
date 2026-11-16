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

"""Expose whether a Maven test report records a failed Tika test."""
import os
import xml.etree.ElementTree as ET
from pathlib import Path


def has_failed_test(root: Path) -> bool:
    for directory in ("surefire-reports", "failsafe-reports"):
        for report in root.glob(f"**/target/{directory}/TEST-*.xml"):
            try:
                suite = ET.parse(report).getroot()
            except (ET.ParseError, OSError) as error:
                print(f"Rapport JUnit illisible : {report} ({error})")
                continue
            if any(
                child.tag.rsplit("}", 1)[-1] in {"failure", "error"}
                for case in suite.iter()
                if case.tag.rsplit("}", 1)[-1] == "testcase"
                for child in case
            ):
                print(f"Cas de test Tika en échec : {report}")
                return True
    return False


if __name__ == "__main__":
    result = "true" if has_failed_test(Path.cwd()) else "false"
    with Path(os.environ["GITHUB_OUTPUT"]).open("a", encoding="utf-8") as output:
        print(f"failed={result}", file=output)
