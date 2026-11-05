#!/usr/bin/env python3
"""The checked-in STRIDE register matches the threat-model markdown (CU78, #2003).

Run with: python3 -m unittest security.tests.test_threat_register
"""

from __future__ import annotations

import unittest
from pathlib import Path

import yaml

REPO = Path(__file__).resolve().parents[2]
REGISTER = REPO / "docs" / "200-architecture" / "206-security" / "threat-model" / "system.stride.yml"
SYSTEM = REPO / "docs" / "200-architecture" / "206-security" / "THREAT-MODEL-SYSTEM.md"
AUTH = REPO / "docs" / "200-architecture" / "206-security" / "THREAT-MODEL-AUTH-AUDIT.md"

STRIDE = {
    "Spoofing",
    "Tampering",
    "Repudiation",
    "Information disclosure",
    "Denial of service",
    "Elevation of privilege",
}
SYSTEM_IDS = {"SR-11", "SR-12", "SR-13", "SR-14", "SR-15", "SR-16"}
LINDDUN_IDS = {"LD-1", "LD-2", "LD-3", "LD-4", "LD-5", "LD-6"}
SECRET_MARKERS = ("ghp_", "BEGIN PRIVATE KEY", "AKIA", "sk_live_")


class ThreatRegisterTest(unittest.TestCase):
    def setUp(self):
        self.text = REGISTER.read_text(encoding="utf-8")
        self.data = yaml.safe_load(self.text)

    def test_register_names_stride_and_linddun(self):
        self.assertEqual(1, self.data["version"])
        self.assertEqual("2026-11-05", self.data["assessed"])
        self.assertEqual("CU78", self.data["use_case"])
        self.assertEqual("STRIDE", self.data["method"]["primary"])
        self.assertEqual("LINDDUN", self.data["method"]["privacy"])
        evaluated = {item["name"] for item in self.data["method"]["evaluated_not_adopted"]}
        self.assertTrue({"OWASP Threat Dragon", "pytm", "Threagile"} <= evaluated)

    def test_every_requirement_is_in_a_markdown_model(self):
        system = SYSTEM.read_text(encoding="utf-8")
        auth = AUTH.read_text(encoding="utf-8")
        ids = {row["id"] for row in self.data["requirements"]}
        self.assertTrue(SYSTEM_IDS <= ids)
        strides = {row["stride"] for row in self.data["requirements"]}
        self.assertEqual(STRIDE, strides)
        for row in self.data["requirements"]:
            self.assertIn(row["id"], system + auth, row["id"])
            self.assertIn(row["status"], {"Mitigated", "Partial", "Open", "Accepted"})

    def test_linddun_rows_and_module_grants_match_the_system_model(self):
        system = SYSTEM.read_text(encoding="utf-8")
        ids = {row["id"] for row in self.data["linddun"]}
        self.assertEqual(LINDDUN_IDS, ids)
        for row in self.data["linddun"]:
            self.assertIn(row["id"], system)
        grant = next(row for row in self.data["requirements"] if row["id"] == "SR-11")
        self.assertIn("RoleModuleAuthorizationFilter", grant["verification"])
        self.assertIn("1683", str(grant["verification"]))
        self.assertEqual("Accepted", grant["status"])

    def test_register_has_no_secrets_or_procedures(self):
        for marker in SECRET_MARKERS:
            self.assertNotIn(marker, self.text)
        self.assertEqual("none", self.data["procedures"])
        techniques = {row["id"] for row in self.data["attack_names"]}
        self.assertTrue({"T1110", "T1552", "T1190", "T1195", "T1078"} <= techniques)


if __name__ == "__main__":
    unittest.main()
