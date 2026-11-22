"""Harness reporting contract — local-worker reference adapter (#2255)."""
from __future__ import annotations

import json
import unittest

from helpers import Repo, output

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"

[[harness.adapters]]
id = "local-worker"
runtime = "local"
source = ".aisdlc/harness/local-worker.yml"
"""


class HarnessReportTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for kind, cid in (("agents", "implementer"), ("skills", "java"), ("mcp", "github")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )
        self.repo.run(
            "project", "create", "--id", "alpha",
            "--profile-ref", "aisdlc.toml", "--json",
        )
        self.repo.run(
            "workflow", "create", "--id", "feature-delivery",
            "--summary", "Feature delivery",
            "--agent", "implementer", "--skill", "java", "--mcp", "github",
            "--json",
        )
        for action, extra in (
            ("define", []),
            ("request_validation", ["--waive-reason", "unit"]),
            ("approve", ["--owner-override", "--reason", "unit"]),
        ):
            result = self.repo.run(
                "workflow", "transition", "--id", "feature-delivery",
                "--action", action, *extra, "--json",
            )
            self.assertEqual(0, result.returncode, output(result))
        started = self.repo.run(
            "run", "start", "--project", "alpha", "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        self.run_id = json.loads(started.stdout)["id"]

    def test_harness_report_moves_run_to_running_then_finished(self):
        started = self.repo.run(
            "harness", "report",
            "--run", self.run_id,
            "--event", "run.started",
            "--adapter", "local-worker",
            "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        shown = self.repo.run("run", "show", "--id", self.run_id, "--json")
        self.assertEqual("running", json.loads(shown.stdout)["status"])

        finished = self.repo.run(
            "harness", "report",
            "--run", self.run_id,
            "--event", "run.finished",
            "--ok", "true",
            "--adapter", "local-worker",
            "--json",
        )
        self.assertEqual(0, finished.returncode, output(finished))
        shown = self.repo.run("run", "show", "--id", self.run_id, "--json")
        self.assertEqual("succeeded", json.loads(shown.stdout)["status"])

    def test_budget_overrun_marks_blocked_skip_never_pass(self):
        self.repo.run(
            "harness", "report",
            "--run", self.run_id,
            "--event", "run.started",
            "--adapter", "local-worker",
            "--json",
        )
        skip = self.repo.run(
            "harness", "report",
            "--run", self.run_id,
            "--event", "step.finished",
            "--step", "build",
            "--step-status", "skip",
            "--adapter", "local-worker",
            "--json",
        )
        self.assertEqual(0, skip.returncode, output(skip))
        shown = json.loads(self.repo.run("run", "show", "--id", self.run_id, "--json").stdout)
        step = next(s for s in shown["steps"] if s["id"] == "build")
        self.assertEqual("skip", step["status"])
        self.assertNotEqual("pass", step["status"])

        blocked = self.repo.run(
            "harness", "report",
            "--run", self.run_id,
            "--event", "step.finished",
            "--step", "test",
            "--step-status", "fail",
            "--budget-overrun",
            "--adapter", "local-worker",
            "--json",
        )
        self.assertEqual(0, blocked.returncode, output(blocked))
        shown = json.loads(self.repo.run("run", "show", "--id", self.run_id, "--json").stdout)
        self.assertEqual("blocked", shown["status"])


    def test_reference_adapter_reports_without_registration(self):
        """#2385 — the kit's reference adapter 'local-worker' is always accepted
        even when no [harness.adapters] are declared in the profile."""
        # Build a minimal repo with NO harness adapters registered.
        from helpers import Repo as _Repo, output as _output
        import tempfile, shutil, subprocess, sys
        from pathlib import Path

        root = Path(tempfile.mkdtemp(prefix="aisdlc-ref-"))
        self.addCleanup(shutil.rmtree, root, ignore_errors=True)

        env = {k: v for k, v in __import__("os").environ.items() if not k.startswith("GIT_")}
        subprocess.run(["git", "init", "-q", "-b", "main"], cwd=root, check=True, env=env)
        subprocess.run(["git", "config", "user.email", "test@example.com"], cwd=root, check=True, env=env)
        subprocess.run(["git", "config", "user.name", "Test"], cwd=root, check=True, env=env)
        subprocess.run(["git", "config", "commit.gpgsign", "false"], cwd=root, check=True, env=env)

        (root / "POLICY.md").write_text("# Policy\n")
        # Profile with NO [harness.adapters] — only the essentials for a run.
        (root / "aisdlc.toml").write_text(
            '[project]\nname = "demo"\npolicy = "POLICY.md"\n\n'
            '[catalogs]\ndir = ".aisdlc/catalogs"\n'
        )
        subprocess.run(["git", "add", "-A"], cwd=root, check=True, env=env)
        subprocess.run(["git", "commit", "-q", "-m", "init"], cwd=root, check=True, env=env)

        bin_path = str(Path(__file__).resolve().parents[1] / "bin" / "aisdlc")

        def run(*args: str) -> subprocess.CompletedProcess:
            return subprocess.run(
                [sys.executable, bin_path, "--root", str(root), *args],
                cwd=root, env=env, capture_output=True, text=True,
            )

        # Confirm harness list is empty.
        hl = run("harness", "list", "--json")
        self.assertEqual(0, hl.returncode, _output(hl))
        rows = json.loads(hl.stdout)
        self.assertEqual([], [r["id"] for r in rows if r["id"] == "local-worker"])

        # Set up a run (catalog, project, workflow, approve, start).
        for kind, cid in (("agents", "implementer"), ("skills", "java"), ("mcp", "github")):
            r = run("catalog", "add", "--kind", kind, "--id", cid,
                     "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid)
            self.assertEqual(0, r.returncode, _output(r))
        r = run("project", "create", "--id", "alpha", "--profile-ref", "aisdlc.toml", "--json")
        self.assertEqual(0, r.returncode, _output(r))
        r = run("workflow", "create", "--id", "feature-delivery", "--summary", "Feature delivery",
                "--agent", "implementer", "--skill", "java", "--mcp", "github", "--json")
        self.assertEqual(0, r.returncode, _output(r))
        for action, extra in (
            ("define", []),
            ("request_validation", ["--waive-reason", "unit"]),
            ("approve", ["--owner-override", "--reason", "unit"]),
        ):
            r = run("workflow", "transition", "--id", "feature-delivery",
                    "--action", action, *extra, "--json")
            self.assertEqual(0, r.returncode, _output(r))
        started = run("run", "start", "--project", "alpha", "--workflow", "feature-delivery", "--json")
        self.assertEqual(0, started.returncode, _output(started))
        run_id = json.loads(started.stdout)["id"]

        # Report with default adapter (no --adapter flag) — should succeed.
        report = run("harness", "report", "--run", run_id, "--event", "run.started", "--json")
        self.assertEqual(0, report.returncode, _output(report))
        shown = run("run", "show", "--id", run_id, "--json")
        self.assertEqual("running", json.loads(shown.stdout)["status"])

    def test_harness_report_refuses_unknown_adapter(self):
        """CU76 #2385 — adapter not returned by harness list must be rejected."""
        known = self.repo.run("harness", "list", "--json")
        self.assertEqual(0, known.returncode, output(known))
        adapter_ids = [row["id"] for row in json.loads(known.stdout)]
        unknown = "phantom-adapter"
        self.assertNotIn(unknown, adapter_ids)

        ledger_before = len(self.repo.ledger())

        result = self.repo.run(
            "harness", "report",
            "--run", self.run_id,
            "--event", "run.started",
            "--adapter", unknown,
            "--json",
        )
        self.assertEqual(2, result.returncode, output(result))
        combined = output(result)
        self.assertIn("unknown_adapter", combined)
        for aid in adapter_ids:
            self.assertIn(aid, combined)

        self.assertEqual(ledger_before, len(self.repo.ledger()))

    def test_harness_report_accepts_known_adapter(self):
        """PRESERVE — adapter returned by harness list still reports successfully."""
        known = self.repo.run("harness", "list", "--json")
        self.assertEqual(0, known.returncode, output(known))
        adapter_ids = [row["id"] for row in json.loads(known.stdout)]
        self.assertIn("local-worker", adapter_ids)

        result = self.repo.run(
            "harness", "report",
            "--run", self.run_id,
            "--event", "run.started",
            "--adapter", "local-worker",
            "--json",
        )
        self.assertEqual(0, result.returncode, output(result))
        shown = self.repo.run("run", "show", "--id", self.run_id, "--json")
        self.assertEqual("running", json.loads(shown.stdout)["status"])


if __name__ == "__main__":
    unittest.main()
