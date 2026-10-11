#!/usr/bin/env python3
"""Security case catalog, local gates, and the AI SDLC wiring (issue #1500, CU78).

Run with: python3 security/tests/test_security_cases.py
"""

from __future__ import annotations

import importlib.util
import json
import stat
import sys
import tempfile
import threading
import unittest
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]
CATALOG = REPO_ROOT / "security" / "cases" / "catalog.yml"
RUNNER = REPO_ROOT / "security" / "run_gates.py"
DAST = REPO_ROOT / "security" / "dast_baseline.py"
RULES = REPO_ROOT / "security" / "semgrep" / "notaire.yml"
FIXTURE = REPO_ROOT / "security" / "semgrep" / "fixtures" / "bad"
ENSURE = REPO_ROOT / "security" / "tools" / "ensure_local_tools.sh"

REQUIRED_CASES = (
    "SEC-TM-01",
    "SEC-SEC-01",
    "SEC-SAST-01",
    "SEC-SCA-01",
    "SEC-DAST-01",
    "SEC-SDLC-01",
)
SDLC_FILES = (
    "workspace/sdlc/run_pipeline.sh",
    "workspace/sdlc/preflight.sh",
    "AGENTS.md",
    "CONSTITUTION.md",
    "CONSTITUTION-AGENT-CARD.md",
    ".claude/agents/openspec.md",
    ".claude/rules/ai-agent-workflow.md",
    "docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md",
)


def load_module(path: Path, name: str):
    spec = importlib.util.spec_from_file_location(name, path)
    if spec is None or spec.loader is None:
        raise AssertionError(f"cannot load {path}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[name] = module
    spec.loader.exec_module(module)
    return module


def planted_github_token() -> str:
    return "ghp_" + ("c" * 36)


def write_executable(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8")
    path.chmod(path.stat().st_mode | stat.S_IEXEC)


class CatalogSchemaTest(unittest.TestCase):
    def test_catalog_specifies_threat_model_sast_dast_sca_and_secrets(self):
        catalog = yaml.safe_load(CATALOG.read_text(encoding="utf-8"))
        self.assertEqual(1, catalog["version"])
        self.assertEqual("CU78", catalog["use_case"])
        cases = {case["id"]: case for case in catalog["cases"]}
        for case_id in REQUIRED_CASES:
            self.assertIn(case_id, cases)
            case = cases[case_id]
            for field in ("family", "title", "requirement", "local_tool", "ci_tool", "blocking"):
                self.assertTrue(case.get(field) or case.get(field) is False, field)
            self.assertIn(
                case["family"],
                {"threat-model", "secrets", "sast", "sca", "dast", "policy"},
            )
        self.assertEqual("semgrep", cases["SEC-SAST-01"]["local_tool"])
        self.assertIn("codeql", cases["SEC-SAST-01"]["ci_tool"])
        self.assertEqual("trivy", cases["SEC-SCA-01"]["local_tool"])
        self.assertIn("zap", cases["SEC-DAST-01"]["ci_tool"].lower())
        self.assertIn("scan_secrets.py", cases["SEC-SEC-01"]["local_tool"])


class GateRunnerTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.gates = load_module(RUNNER, "run_gates")
        cls.dast = load_module(DAST, "dast_baseline")

    def test_secret_case_fails_on_a_planted_token_and_passes_on_the_repo(self):
        with tempfile.TemporaryDirectory() as tmp:
            planted = Path(tmp) / "notes.txt"
            planted.write_text("token=" + planted_github_token() + "\n", encoding="utf-8")
            failed = self.gates.assess_secrets(Path(tmp))
        self.assertEqual("FAIL", failed.status)
        self.assertNotIn(planted_github_token(), failed.detail)
        clean = self.gates.assess_secrets(REPO_ROOT)
        self.assertEqual("PASS", clean.status, clean.detail)

    def test_sast_requires_a_fixture_finding_and_a_clean_tree(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            semgrep = root / "semgrep"
            write_executable(
                semgrep,
                "#!/bin/sh\n"
                'if echo "$*" | grep -q "fixtures/bad"; then\n'
                '  printf \'%s\\n\' \'{"results":[{"check_id":"notaire-log-password-value"}]}\'\n'
                "  exit 1\n"
                "fi\n"
                "exit 0\n",
            )
            passed = self.gates.assess_sast(REPO_ROOT, str(semgrep))
            self.assertEqual("PASS", passed.status, passed.detail)
            dirty = root / "semgrep-dirty"
            write_executable(dirty, "#!/bin/sh\nexit 1\n")
            failed = self.gates.assess_sast(REPO_ROOT, str(dirty))
        self.assertEqual("FAIL", failed.status)
        missing = self.gates.assess_sast(REPO_ROOT, None)
        self.assertEqual("FAIL", missing.status)

    def test_local_sca_fails_when_trivy_is_missing_and_passes_when_it_runs(self):
        missing = self.gates.assess_sca(REPO_ROOT, None, profile="local")
        self.assertEqual("FAIL", missing.status)
        with tempfile.TemporaryDirectory() as tmp:
            trivy = Path(tmp) / "trivy"
            write_executable(trivy, "#!/bin/sh\nexit 0\n")
            passed = self.gates.assess_sca(REPO_ROOT, str(trivy), profile="local")
        self.assertEqual("PASS", passed.status, passed.detail)
        configured = self.gates.assess_sca(REPO_ROOT, None, profile="ci")
        self.assertEqual("PASS", configured.status, configured.detail)

    def test_dast_baseline_accepts_401_and_rejects_a_stack_trace(self):
        good = _serve(_GoodHandler)
        bad = _serve(_LeakHandler)
        try:
            self.assertEqual([], self.dast.check_base(good))
            leaks = self.dast.check_base(bad)
            self.assertTrue(leaks, "stack trace response must fail the baseline")
        finally:
            good_server = _SERVERS.pop(good)
            bad_server = _SERVERS.pop(bad)
            good_server.shutdown()
            bad_server.shutdown()
            good_server.server_close()
            bad_server.server_close()

    def test_report_lists_every_case(self):
        with tempfile.TemporaryDirectory() as tmp:
            tools = _passing_tools(Path(tmp))
            results = self.gates.run_cases(
                REPO_ROOT,
                profile="local",
                base_url=None,
                skip_dast=True,
                semgrep_bin=tools["semgrep"],
                trivy_bin=tools["trivy"],
            )
            dest = Path(tmp) / "report"
            self.gates.write_report(results, dest, profile="local")
            payload = json.loads((dest / "report.json").read_text(encoding="utf-8"))
            ids = {row["id"] for row in payload["cases"]}
            self.assertTrue(set(REQUIRED_CASES).issubset(ids))
            self.assertTrue((dest / "index.html").is_file())
            skipped = [row for row in payload["cases"] if row["id"] == "SEC-DAST-01"]
            self.assertEqual("SKIP", skipped[0]["status"])


class ProcessWiringTest(unittest.TestCase):
    def test_ai_sdlc_names_the_local_gate(self):
        for relative in SDLC_FILES:
            text = (REPO_ROOT / relative).read_text(encoding="utf-8")
            self.assertIn("security/run_gates.py", text, relative)
        preflight = (REPO_ROOT / "workspace/sdlc/preflight.sh").read_text(encoding="utf-8")
        self.assertIn("security gates (local)", preflight)
        pipeline = (REPO_ROOT / "workspace/sdlc/run_pipeline.sh").read_text(encoding="utf-8")
        self.assertIn("--only", pipeline)
        self.assertIn("dast", pipeline)
        openspec = (REPO_ROOT / ".claude/agents/openspec.md").read_text(encoding="utf-8")
        self.assertIn("security/cases/catalog.yml", openspec)
        self.assertTrue(ENSURE.is_file())
        self.assertTrue(RULES.is_file())
        self.assertTrue(FIXTURE.is_dir())


_SERVERS: dict[str, ThreadingHTTPServer] = {}


def _serve(handler: type[BaseHTTPRequestHandler]) -> str:
    server = ThreadingHTTPServer(("127.0.0.1", 0), handler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    url = f"http://127.0.0.1:{server.server_address[1]}"
    _SERVERS[url] = server
    return url


class _GoodHandler(BaseHTTPRequestHandler):
    def do_GET(self):  # noqa: N802
        if self.path.startswith("/actuator/health"):
            _send(self, 200, b'{"status":"UP"}')
        elif self.path.startswith("/api/"):
            _send(self, 401, b'{"error":"unauthorized"}')
        else:
            _send(self, 404, b"")

    def log_message(self, fmt, *args):
        return


class _LeakHandler(BaseHTTPRequestHandler):
    def do_GET(self):  # noqa: N802
        body = b"java.lang.IllegalStateException\n\tat com.licensis.notaire.Foo.bar(Foo.java:1)\n"
        if self.path.startswith("/actuator/health"):
            _send(self, 200, b'{"status":"UP"}')
        else:
            _send(self, 500, body)

    def log_message(self, fmt, *args):
        return


def _send(handler: BaseHTTPRequestHandler, status: int, body: bytes) -> None:
    handler.send_response(status)
    handler.send_header("Content-Length", str(len(body)))
    handler.end_headers()
    handler.wfile.write(body)


def _passing_tools(root: Path) -> dict[str, str]:
    semgrep = root / "semgrep"
    trivy = root / "trivy"
    write_executable(
        semgrep,
        "#!/bin/sh\n"
        'if echo "$*" | grep -q "fixtures/bad"; then\n'
        '  printf \'%s\\n\' \'{"results":[{"check_id":"notaire-log-password-value"}]}\'\n'
        "  exit 1\n"
        "fi\n"
        "exit 0\n",
    )
    write_executable(trivy, "#!/bin/sh\nexit 0\n")
    return {"semgrep": str(semgrep), "trivy": str(trivy)}


class RealSemgrepTest(unittest.TestCase):
    def test_rules_flag_the_fixture_and_the_repo_is_clean(self):
        gates = load_module(RUNNER, "run_gates_real")
        binary = gates.resolve_semgrep(REPO_ROOT)
        if binary is None:
            self.skipTest("semgrep is not installed; the CI security job installs it")
        result = gates.assess_sast(REPO_ROOT, binary)
        self.assertEqual("PASS", result.status, result.detail)


if __name__ == "__main__":
    unittest.main()
