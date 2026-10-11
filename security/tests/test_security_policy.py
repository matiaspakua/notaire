#!/usr/bin/env python3
"""Guards the cybersecurity program and policy-as-code (issue #307, CU78).

Run with: python3 security/tests/test_security_policy.py
"""

from __future__ import annotations

import importlib.util
import json
import re
import sys
import textwrap
import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]
POLICY = REPO_ROOT / "security" / "policy" / "security-policy.yml"
CI = REPO_ROOT / ".github" / "workflows" / "ci.yml"
VERIFY = REPO_ROOT / "security" / "verify.sh"
DASHBOARD = (
    REPO_ROOT
    / "infra"
    / "observability"
    / "grafana"
    / "provisioning"
    / "dashboards"
    / "notaire-security.json"
)
ALERTS = REPO_ROOT / "infra" / "observability" / "prometheus" / "alert-rules.yml"
PROGRAM = REPO_ROOT / "docs" / "200-architecture" / "206-security" / "CYBERSECURITY-PROGRAM.md"
THREAT = REPO_ROOT / "docs" / "200-architecture" / "206-security" / "THREAT-MODEL-SYSTEM.md"
TOOLS = REPO_ROOT / "docs" / "200-architecture" / "206-security" / "SECURITY-TOOLS.md"
MONITORING = REPO_ROOT / "docs" / "200-architecture" / "206-security" / "MONITORING.md"
OWASP = REPO_ROOT / "docs" / "200-architecture" / "206-security" / "OWASP-TOP10.md"
DIAGRAM = REPO_ROOT / "docs" / "200-architecture" / "206-security" / "diagrams" / "trust-boundaries.puml"
README = REPO_ROOT / "docs" / "200-architecture" / "206-security" / "README.md"
AUTH_THREAT = REPO_ROOT / "docs" / "200-architecture" / "206-security" / "THREAT-MODEL-AUTH-AUDIT.md"
SAD = REPO_ROOT / "docs" / "200-architecture" / "201-SAD" / "sad.md"
FLEET = REPO_ROOT / "docs" / "300-development" / "304-ai-sdlc-cloud" / "FLEET-ARCHITECTURE.md"
CU78 = REPO_ROOT / "docs" / "100-business" / "102-use-cases" / "CU78 – Security and Compliance.md"
SCANNER = REPO_ROOT / "security" / "scan_secrets.py"

STALE_RBAC = "no per-role authorization yet"


def load_scanner():
    spec = importlib.util.spec_from_file_location("scan_secrets", SCANNER)
    if spec is None or spec.loader is None:
        raise AssertionError(f"cannot load {SCANNER}")
    module = importlib.util.module_from_spec(spec)
    sys.modules["scan_secrets"] = module
    spec.loader.exec_module(module)
    return module


def planted_private_key() -> str:
    body = "A" * 64
    return (
        "-----BEGIN PRIVATE KEY-----\n"
        + body
        + "\n-----END PRIVATE KEY-----\n"
    )


def planted_github_token() -> str:
    return "ghp_" + ("b" * 36)


class PolicyDocumentTest(unittest.TestCase):
    def test_policy_declares_tools_sla_secrets_exceptions_and_review(self):
        policy = yaml.safe_load(POLICY.read_text(encoding="utf-8"))
        self.assertEqual(1, policy["version"])
        self.assertEqual("CU78", policy["use_case"])
        self.assertEqual("quarterly", policy["review_cadence"])
        tools = policy["tools"]
        for key in ("sast", "sca", "secrets", "dast", "sbom", "signing"):
            self.assertTrue(tools[key], key)
        self.assertEqual("codeql", tools["sast"])
        self.assertEqual("trivy", tools["sca"])
        self.assertIn("scan_secrets.py", tools["secrets"])
        self.assertLessEqual(policy["severity_sla"]["critical_days"], 7)
        self.assertLessEqual(policy["severity_sla"]["high_days"], 30)
        self.assertIn("ci.yml", policy["secret_scan"]["enforced_in"])
        self.assertIn("procedure", policy["exceptions"])
        self.assertIn("owner", policy["exceptions"]["procedure"].lower())

    def test_program_documents_exist_and_are_indexed(self):
        for path in (PROGRAM, THREAT, TOOLS, MONITORING, OWASP, DIAGRAM):
            self.assertTrue(path.is_file(), path)
        program = PROGRAM.read_text(encoding="utf-8")
        self.assertIn("STRIDE", program)
        self.assertIn("LINDDUN", program)
        self.assertIn("Sec by Design", program)
        self.assertIn("SAMM", program)
        self.assertIn("final_projects_specialization.md", program)
        readme = README.read_text(encoding="utf-8")
        self.assertIn("CYBERSECURITY-PROGRAM.md", readme)
        self.assertNotIn(STALE_RBAC, readme)
        self.assertIn("CYBERSECURITY-PROGRAM.md", CU78.read_text(encoding="utf-8"))
        self.assertIn("security/verify.sh", FLEET.read_text(encoding="utf-8"))

    def test_system_threat_model_records_stride_linddun_and_new_requirements(self):
        text = THREAT.read_text(encoding="utf-8")
        for marker in ("STRIDE", "LINDDUN", "SR-11", "SR-14", "Escribano"):
            self.assertIn(marker, text)
        self.assertIn("trust boundary", DIAGRAM.read_text(encoding="utf-8").lower())
        auth = AUTH_THREAT.read_text(encoding="utf-8")
        self.assertIn("Partial", auth)
        self.assertNotIn(STALE_RBAC, auth)
        sad = SAD.read_text(encoding="utf-8")
        self.assertIn("ROLE_ADMIN", sad)
        self.assertNotIn(STALE_RBAC, sad)

    def test_live_security_docs_name_english_types(self):
        retired = (
            "Persona",
            "Escritura",
            "Presupuesto",
            "Testimonio",
            "registro_auditoria",
            "Oficial de Seguridad",
            "Equipo de Desarrollo",
        )
        threat = THREAT.read_text(encoding="utf-8")
        for live in (
            "`Person`",
            "`Deed`",
            "`Testimony`",
            "`Budget`",
            "`Payment`",
            "`audit_records`",
            "Security Officer",
            "Development Team",
        ):
            self.assertIn(live, threat, live)
        for token in retired:
            self.assertIsNone(
                re.search(rf"\b{re.escape(token)}\b", threat),
                token,
            )
        program = PROGRAM.read_text(encoding="utf-8")
        monitoring = MONITORING.read_text(encoding="utf-8")
        diagram = DIAGRAM.read_text(encoding="utf-8")
        for token in ("Persona", "Escritura", "Presupuesto"):
            self.assertIsNone(re.search(rf"\b{token}\b", program), token)
            self.assertIsNone(re.search(rf"\b{token}\b", diagram), token)
        self.assertIsNone(re.search(r"\bPersona\b", monitoring))
        self.assertIn("`Person`", program)
        self.assertIn("`Person`", monitoring)
        self.assertIn("Person, Deed, Budget", diagram)


class SecretScanTest(unittest.TestCase):
    def test_scanner_flags_private_key_and_github_token(self):
        scan_secrets = load_scanner()
        policy = scan_secrets.load_policy(POLICY)
        key_findings = scan_secrets.scan_text(planted_private_key(), "tmp/key.pem", policy)
        token_findings = scan_secrets.scan_text(
            "token=" + planted_github_token() + "\n", "tmp/notes.txt", policy
        )
        self.assertIn("private-key", {item.kind for item in key_findings})
        self.assertIn("github-pat", {item.kind for item in token_findings})
        rendered = scan_secrets.format_findings(key_findings + token_findings)
        self.assertNotIn("A" * 64, rendered)
        self.assertNotIn(planted_github_token(), rendered)

    def test_scanner_accepts_placeholders(self):
        scan_secrets = load_scanner()
        policy = scan_secrets.load_policy(POLICY)
        sample = textwrap.dedent(
            """
            password=changeme
            jwt.secret=your-secret-here
            token=ghp_example
            aws=AKIAIOSFODNN7EXAMPLE
            """
        )
        self.assertEqual([], scan_secrets.scan_text(sample, ".env.example", policy))

    def test_repository_tree_has_no_high_confidence_secret(self):
        scan_secrets = load_scanner()
        policy = scan_secrets.load_policy(POLICY)
        findings = scan_secrets.scan_tree(REPO_ROOT, policy)
        self.assertEqual([], findings, scan_secrets.format_findings(findings))

    def test_ci_and_module_verify_enforce_the_scan(self):
        ci = CI.read_text(encoding="utf-8")
        self.assertIn("python3 security/scan_secrets.py", ci)
        lines = ci.splitlines()
        for index, line in enumerate(lines):
            if "python3 security/scan_secrets.py" in line:
                window = "\n".join(lines[max(0, index - 3) : index + 2])
                self.assertNotIn("continue-on-error", window)
                break
        else:
            self.fail("secret scan step not found")
        self.assertIn("test_security_policy.py", VERIFY.read_text(encoding="utf-8"))


class SecurityMonitoringTest(unittest.TestCase):
    def test_dashboard_shows_login_failures_and_firing_alerts(self):
        dashboard = json.loads(DASHBOARD.read_text(encoding="utf-8"))
        self.assertEqual("notaire-security", dashboard["uid"])
        blob = json.dumps(dashboard)
        self.assertIn("notaire_operation_total", blob)
        self.assertIn("bad_credentials", blob)
        self.assertIn("locked", blob)
        self.assertIn("ALERTS", blob)
        self.assertIn("LoginLockoutSpike", blob)

    def test_lockout_alert_is_actionable(self):
        groups = yaml.safe_load(ALERTS.read_text(encoding="utf-8"))["groups"]
        alerts = {rule["alert"]: rule for group in groups for rule in group["rules"]}
        rule = alerts["LoginLockoutSpike"]
        self.assertIn('status="locked"', rule["expr"])
        self.assertEqual("warning", rule["labels"]["severity"])
        self.assertTrue(rule["annotations"]["summary"])
        self.assertTrue(rule["annotations"]["description"])


if __name__ == "__main__":
    unittest.main()
