#!/usr/bin/env python3
"""ZAP baseline is a blocking gate with a reviewed allowlist (#1738, CU78).

Run with: python3 security/tests/test_zap_dast_ratchet.py
"""

from __future__ import annotations

import unittest
from pathlib import Path

import yaml

REPO = Path(__file__).resolve().parents[2]
ZAP = REPO / ".github" / "workflows" / "dast-zap.yml"
RULES = REPO / ".zap" / "rules.tsv"
PERF = REPO / ".github" / "workflows" / "performance-test.yml"

# Reviewed allowlist (#1738, #1932). Only informational rules whose finding is the
# wanted behaviour of an unauthenticated JSON API may be ignored; anything else must
# fail the scan.
REVIEWED_IGNORES = {"90005", "10049"}


def allowlist_lines() -> list[str]:
    return [
        line for line in RULES.read_text(encoding="utf-8").splitlines() if line.strip() and not line.startswith("#")
    ]


def upload_steps(path: Path) -> list[dict]:
    steps = []
    for job in load_workflow(path)["jobs"].values():
        steps += [s for s in job.get("steps", []) if "actions/upload-artifact" in str(s.get("uses", ""))]
    return steps


def load_workflow(path: Path) -> dict:
    data = yaml.safe_load(path.read_text(encoding="utf-8"))
    if "on" not in data and True in data:
        data["on"] = data.pop(True)
    return data


class ZapDastRatchetTest(unittest.TestCase):
    def test_workflow_fails_on_alerts_with_rules_file(self):
        text = ZAP.read_text(encoding="utf-8")
        self.assertIn("zaproxy/action-baseline", text)
        self.assertIn("fail_action: true", text)
        self.assertIn("rules_file_name:", text)
        self.assertIn(".zap/rules.tsv", text)
        self.assertNotIn("fail_action: false", text)

    def test_rules_allowlist_ignores_sec_fetch_informational(self):
        self.assertTrue(RULES.is_file(), ".zap/rules.tsv must exist")
        lines = [
            line for line in RULES.read_text(encoding="utf-8").splitlines() if line.strip() and not line.startswith("#")
        ]
        self.assertTrue(any(line.split("\t")[0] == "90005" and "IGNORE" in line for line in lines), lines)

    def test_rules_allowlist_ignores_non_storable_content_informational(self):
        # #1932: 10049 flags 404 JSON answers on /, /robots.txt, /sitemap.xml as non-storable;
        # an API that must not be cached by shared proxies is the wanted behaviour.
        lines = allowlist_lines()
        self.assertTrue(any(line.split("\t")[0] == "10049" and "IGNORE" in line for line in lines), lines)

    def test_every_allowlist_entry_is_reviewed_and_has_a_reason(self):
        for line in allowlist_lines():
            cols = line.split("\t")
            self.assertEqual(3, len(cols), line)
            self.assertIn(cols[0], REVIEWED_IGNORES, f"unreviewed ZAP allowlist entry: {line}")
            self.assertEqual("IGNORE", cols[1], line)
            self.assertTrue(cols[2].startswith("(") and cols[2].endswith(")") and len(cols[2]) > 10, line)

    def test_report_uploads_do_not_fail_a_scan_on_artifact_quota(self):
        # #1932: "Artifact storage quota has been hit" turned a passing k6 run red.
        # The scan step decides the job; report uploads are best-effort evidence.
        for path in (ZAP, PERF):
            steps = upload_steps(path)
            self.assertTrue(steps, path.name)
            for step in steps:
                self.assertIs(True, step.get("continue-on-error"), f"{path.name}: {step.get('name')}")
                self.assertEqual("always()", step.get("if"), f"{path.name}: {step.get('name')}")


if __name__ == "__main__":
    unittest.main()
