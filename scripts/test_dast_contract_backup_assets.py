#!/usr/bin/env python3
"""
Guards issue #1067 (CU76 / CU78 / CU75): DAST ZAP, OpenAPI contract diff,
and backup→restore smoke gated on #256.

Asserts:
- OWASP ZAP baseline workflow exists (schedule + workflow_dispatch), uploads
  a report artifact, and does not gate every PR
- Trivy SCA remains configured in ci.yml (DAST is additive)
- Committed OpenAPI artifact exists at the documented path
- OpenAPI export script exists and documents regeneration
- PR OpenAPI contract workflow fails on breaking changes
- Backup→restore smoke workflow skips with an explicit #256 signal when
  backup tooling is absent (no false-green restore)

Plain stdlib unittest (+ PyYAML), consistent with
scripts/test_performance_test_assets.py.
Run with: python3 scripts/test_dast_contract_backup_assets.py

Also discoverable via: python3 -m unittest discover -s scripts/tests
"""

from __future__ import annotations

import os
import re
import unittest

import yaml

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ZAP_WORKFLOW = os.path.join(REPO_ROOT, ".github", "workflows", "dast-zap.yml")
OPENAPI_WORKFLOW = os.path.join(
    REPO_ROOT, ".github", "workflows", "openapi-contract.yml"
)
BACKUP_WORKFLOW = os.path.join(
    REPO_ROOT, ".github", "workflows", "backup-restore-smoke.yml"
)
CI_WORKFLOW = os.path.join(REPO_ROOT, ".github", "workflows", "ci.yml")
OPENAPI_ARTIFACT = os.path.join(
    REPO_ROOT, "backend-api", "openapi", "openapi.yaml"
)
EXPORT_SCRIPT = os.path.join(REPO_ROOT, "scripts", "export-openapi.sh")
BACKUP_SENTINEL = os.path.join(REPO_ROOT, "scripts", "backup-postgres.sh")


def _load_workflow(path: str) -> tuple[str, dict]:
    with open(path, encoding="utf-8") as f:
        raw = f.read()
    return raw, yaml.safe_load(raw)


def _triggers(workflow: dict) -> dict:
    return workflow.get(True, workflow.get("on", {})) or {}


class ZapDastWorkflowTest(unittest.TestCase):
    def test_zap_workflow_file_exists(self):
        self.assertTrue(
            os.path.isfile(ZAP_WORKFLOW),
            f"expected ZAP workflow at {ZAP_WORKFLOW}",
        )

    def test_is_scheduled_and_dispatchable_not_every_pr(self):
        self.assertTrue(os.path.isfile(ZAP_WORKFLOW), f"missing {ZAP_WORKFLOW}")
        raw, workflow = _load_workflow(ZAP_WORKFLOW)
        triggers = _triggers(workflow)
        self.assertIn("schedule", triggers, "ZAP must run on a schedule")
        self.assertIn(
            "workflow_dispatch",
            triggers,
            "ZAP must support manual dispatch",
        )
        self.assertNotIn(
            "pull_request",
            triggers,
            "ZAP baseline must not gate every PR (OpenAPI diff does)",
        )
        self.assertIn("zaproxy", raw.lower())
        self.assertRegex(raw, r"8080|localhost", "must target the running API")

    def test_uploads_zap_report_artifact(self):
        self.assertTrue(os.path.isfile(ZAP_WORKFLOW), f"missing {ZAP_WORKFLOW}")
        raw, _ = _load_workflow(ZAP_WORKFLOW)
        self.assertIn("upload-artifact", raw)
        self.assertRegex(raw, r"(?i)zap|report")
        self.assertNotRegex(
            raw,
            r"if-no-files-found:\s*ignore",
            "upload must not silently ignore a missing ZAP report",
        )


class TrivyRetainedTest(unittest.TestCase):
    def test_trivy_remains_in_ci_workflow(self):
        with open(CI_WORKFLOW, encoding="utf-8") as f:
            raw = f.read()
        self.assertIn("trivy-action", raw)
        self.assertIn("trivy-results", raw)


class OpenApiContractTest(unittest.TestCase):
    def test_committed_openapi_artifact_exists(self):
        self.assertTrue(
            os.path.isfile(OPENAPI_ARTIFACT),
            f"expected committed OpenAPI at {OPENAPI_ARTIFACT}",
        )
        with open(OPENAPI_ARTIFACT, encoding="utf-8") as f:
            doc = yaml.safe_load(f)
        self.assertIn(doc.get("openapi", ""), ("3.0.1", "3.0.3", "3.1.0"))
        info = doc.get("info") or {}
        self.assertIn("Notaire", info.get("title", ""))

    def test_export_script_exists_and_documents_path(self):
        self.assertTrue(
            os.path.isfile(EXPORT_SCRIPT),
            f"expected export script at {EXPORT_SCRIPT}",
        )
        with open(EXPORT_SCRIPT, encoding="utf-8") as f:
            script = f.read()
        self.assertIn("backend-api/openapi/openapi.yaml", script)
        self.assertIn("v3/api-docs", script)

    def test_openapi_contract_workflow_on_pull_request(self):
        raw, workflow = _load_workflow(OPENAPI_WORKFLOW)
        triggers = _triggers(workflow)
        self.assertIn(
            "pull_request",
            triggers,
            "OpenAPI contract must run on pull requests",
        )
        self.assertRegex(raw, r"(?i)oasdiff|openapi-diff|breaking")
        self.assertIn("backend-api/openapi/openapi.yaml", raw)
        self.assertIn("export-openapi", raw)


class BackupRestoreSmokeTest(unittest.TestCase):
    def test_backup_restore_workflow_exists(self):
        self.assertTrue(
            os.path.isfile(BACKUP_WORKFLOW),
            f"expected backup-restore workflow at {BACKUP_WORKFLOW}",
        )

    def test_skips_with_issue_256_when_backup_tooling_absent(self):
        self.assertTrue(os.path.isfile(BACKUP_WORKFLOW), f"missing {BACKUP_WORKFLOW}")
        raw, _ = _load_workflow(BACKUP_WORKFLOW)
        self.assertIn("#256", raw)
        self.assertIn("backup-postgres.sh", raw)
        # Must not claim a successful restore when the sentinel is missing.
        self.assertRegex(raw, r"(?i)skip|blocked|absent|not yet")
        if not os.path.isfile(BACKUP_SENTINEL):
            # While #256 is open, the workflow body must document the skip path
            # rather than only running restore steps unconditionally.
            self.assertRegex(raw, r"(?i)if:|test ! -f|\[ ! -f|backup tooling")


if __name__ == "__main__":
    unittest.main()
