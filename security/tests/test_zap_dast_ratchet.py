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


if __name__ == "__main__":
    unittest.main()
