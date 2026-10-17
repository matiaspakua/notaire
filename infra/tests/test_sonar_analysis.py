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
        self.assertEqual("git", props.get("sonar.scm.provider"), "#1680 SCM must be explicit")

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

    def test_script_ratchets_new_blocker_critical_major(self):
        """Issue #1737: Notaire way fails when new high-severity issues appear."""
        script = read(SCRIPT)
        for metric in (
            "new_blocker_violations",
            "new_critical_violations",
            "new_major_violations",
        ):
            self.assertIn(metric, script, metric)
        self.assertIn('error=0', script)
        report = os.path.join(REPO_ROOT, "infra", "scripts", "sonar-severity-report.py")
        self.assertTrue(os.path.isfile(report), "sonar-severity-report.py")
        body = read(report)
        self.assertIn("notaire-backend", body)
        self.assertIn("notaire-frontend", body)
        for severity in ("BLOCKER", "CRITICAL", "MAJOR"):
            self.assertIn(severity, body)
        inventory = os.path.join(REPO_ROOT, "infra", "scripts", "sonar-legacy-inventory.py")
        self.assertTrue(os.path.isfile(inventory), "sonar-legacy-inventory.py (#1535)")

    def test_script_exposes_git_scm_to_frontend_scanner(self):
        """Issue #1680: without SCM, the whole tree is 'new code' and the gate fails."""
        script = read(SCRIPT)
        self.assertIn("sonar.scm.provider=git", script)
        self.assertRegex(
            script,
            r"-v\s+\"\$APP_DIR:/usr/src\"|-v\s+\$APP_DIR:/usr/src",
            "scanner must mount the monorepo (with .git), not only frontend/",
        )
        self.assertIn("sonar.projectBaseDir=/usr/src/frontend", script)
        self.assertIn('sonar.projectKey="$BACKEND_KEY"', script)

    def test_no_credentials_in_configuration(self):
        for path in (POM, FRONTEND_PROPS):
            self.assertIsNone(SECRET.search(read(path)), path)

    def test_script_regenerates_invalid_analysis_token(self):
        """Stale SONAR_TOKEN after a Sonar volume recreate must not 401 the scanner.

        Observed on the #1862 --with-infra pipeline: a persisted token returned
        HTTP 200 with valid:false, so the frontend scanner failed with 401 while
        password auth still worked. The script must validate the token and
        regenerate when it is empty or invalid.
        """
        script = read(SCRIPT)
        self.assertIn("token_works", script)
        self.assertIn("Stored SONAR_TOKEN is invalid", script)
        self.assertIn("regenerating", script)
        self.assertIn('"valid":true', script)

    def test_script_sets_global_previous_version_new_code(self):
        """Community Edition ignores project-level new-code; set global PREVIOUS_VERSION."""
        script = read(SCRIPT)
        self.assertIn("type=PREVIOUS_VERSION", script)
        self.assertIn("Global new-code period", script)
        self.assertIn("SONAR_SCANNER_JAVA_OPTS", script)
        self.assertIn("sonar.projectVersion", script)
        self.assertNotRegex(
            script,
            r"new_code_periods/set[^\n]*project=\$KEY",
            msg="project-scoped new_code_periods/set is a no-op on Community Edition",
        )


if __name__ == "__main__":
    unittest.main()
