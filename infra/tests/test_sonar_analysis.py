#!/usr/bin/env python3
"""
Guards the SonarQube analysis of backend and frontend (issue #1466, CU78).
Spec: docs/openspec/changes/feat-1466-sonar-backend-frontend/.

Plain stdlib unittest, like the other infra config guards.
Run with: python3 infra/tests/test_sonar_analysis.py
"""
import os
import re
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
POM = os.path.join(REPO_ROOT, "backend-api", "pom.xml")
FRONTEND_PROPS = os.path.join(REPO_ROOT, "frontend", "sonar-project.properties")
SCRIPT = os.path.join(REPO_ROOT, "infra", "scripts", "run-sonar.sh")
SECRET = re.compile(r"sq[pua]_[0-9a-f]{20,}|(?i:password)\s*=\s*\S+")


def read(path):
    with open(path, encoding="utf-8") as f:
        return f.read()


def load_properties(path):
    props = {}
    for line in read(path).splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            key, value = line.split("=", 1)
            props[key.strip()] = value.strip()
    return props


class SonarAnalysisTest(unittest.TestCase):
    def test_backend_properties(self):
        pom = read(POM)
        self.assertIn("<sonar.projectKey>notaire-backend</sonar.projectKey>", pom)
        self.assertIn("sonar.coverage.jacoco.xmlReportPaths", pom)
        self.assertRegex(pom, r"<sonar.coverage.exclusions>[^<]*com/licensis/notaire/jpa/")

    def test_frontend_properties_and_reports(self):
        props = load_properties(FRONTEND_PROPS)
        self.assertEqual("notaire-frontend", props["sonar.projectKey"])
        self.assertEqual("src", props["sonar.sources"])
        self.assertEqual("coverage/lcov.info", props["sonar.javascript.lcov.reportPaths"])
        self.assertTrue(props["sonar.eslint.reportPaths"].endswith(".json"))

    def test_script_enforces_quality_gate(self):
        script = read(SCRIPT)
        self.assertIn("notaire-frontend", script)
        self.assertIn("notaire-backend", script)
        self.assertIn("api/qualitygates", script)
        self.assertIn("sonar.qualitygate.wait=true", script)
        images = re.findall(r"sonarsource/sonar-scanner-cli:\S+", script)
        self.assertTrue(images, "frontend must use the scanner container")
        for image in images:
            self.assertNotIn(":latest", image)

    def test_no_credentials_in_configuration(self):
        for path in (POM, FRONTEND_PROPS):
            self.assertIsNone(SECRET.search(read(path)), path)


if __name__ == "__main__":
    unittest.main()
