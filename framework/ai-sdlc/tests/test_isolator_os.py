"""Isolator OS probe/run (#2198) — honest bwrap degrade; never fake sandbox."""
from __future__ import annotations

import json
import os
import shutil
import subprocess
import sys
import unittest
from unittest import mock

from helpers import BIN, ENV, KIT, Repo, output

sys.path.insert(0, str(KIT))

BASE = """
[project]
name = "demo"
policy = "POLICY.md"

[[gates]]
name = "plan check"
stage = "plan"
run = "true"

[[grants]]
id = "plan-build"
stage = "plan"
worktree = "."
write_set = ["docs/**"]
required_checks = ["plan check"]
"""

EXPIRED = """
[project]
name = "demo"
policy = "POLICY.md"

[[gates]]
name = "plan check"
stage = "plan"
run = "true"

[[grants]]
id = "plan-build"
worktree = "."
write_set = ["docs/**"]
required_checks = ["plan check"]
expires_at = "2020-01-01T00:00:00Z"
"""


def _run(repo: Repo, *args: str, env: dict | None = None) -> subprocess.CompletedProcess:
    merged = dict(ENV)
    if env:
        merged.update(env)
    return subprocess.run(
        [sys.executable, str(BIN), "--root", str(repo.root), *args],
        cwd=repo.root,
        env=merged,
        capture_output=True,
        text=True,
    )


class IsolatorOsTest(unittest.TestCase):
    def test_isolator_status_without_bwrap_is_honest(self):
        """#2198: missing bwrap → sandbox=false, status no-evaluable, backend none."""
        repo = Repo(self, BASE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        result = _run(repo, "isolator", "status", "--json", env={"AISDLC_BWRAP": ""})
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("no-evaluable", data["status"])
        self.assertIs(False, data["sandbox"])
        self.assertEqual("none", data["backend"])
        self.assertIs(False, data.get("enforces_committer", False))
        blob = json.dumps(data).lower()
        self.assertNotRegex(blob, r'"sandbox"\s*:\s*true')

    def test_isolator_probe_reports_backend_and_sandbox(self):
        """#2198: probe JSON always includes backend + sandbox from real result."""
        from aisdlc import isolator  # noqa: E402

        repo = Repo(self, BASE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        result = _run(repo, "isolator", "probe", "--json", env={"AISDLC_BWRAP": ""})
        self.assertIn(result.returncode, (0, 1), output(result))
        data = json.loads(result.stdout)
        self.assertIn("backend", data)
        self.assertIn("sandbox", data)
        self.assertIs(False, data["sandbox"])
        self.assertEqual("none", data["backend"])

        with mock.patch.object(isolator, "bwrap_path", return_value="/usr/bin/bwrap"), mock.patch.object(
            isolator, "_run_bwrap_probe", return_value=False
        ):
            payload = isolator.probe()
        self.assertIs(False, payload["sandbox"])
        self.assertEqual("bwrap", payload["backend"])

    def test_isolator_run_refuses_when_sandbox_unavailable(self):
        """#2198: without containment backend, run exits non-zero and does not claim sandbox."""
        repo = Repo(self, BASE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        result = _run(repo, "isolator", "run", "--", "true", env={"AISDLC_BWRAP": ""})
        self.assertNotEqual(0, result.returncode, output(result))
        combined = output(result).lower()
        self.assertNotIn("sandbox=true", combined)
        self.assertNotRegex(combined, r'"sandbox"\s*:\s*true')

    def test_isolator_run_refuses_expired_grant(self):
        """#2198: expired grants → run fails closed."""
        repo = Repo(self, EXPIRED)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        # Force a fake bwrap path so expire (not missing-backend) is the refuse reason when possible.
        result = _run(
            repo,
            "isolator",
            "run",
            "--json",
            "--",
            "true",
            env={"AISDLC_BWRAP": "/usr/bin/bwrap"},
        )
        self.assertNotEqual(0, result.returncode, output(result))
        combined = output(result).lower()
        self.assertIn("expir", combined)
        if result.stdout.strip().startswith("{"):
            data = json.loads(result.stdout)
            self.assertIs(False, data.get("sandbox", False))

    def test_doctor_never_claims_sandbox_without_probe_success(self):
        """#2198: doctor INFO must not claim sandbox=true without probe success."""
        repo = Repo(self, BASE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        result = _run(repo, "doctor", env={"AISDLC_BWRAP": ""})
        self.assertEqual(0, result.returncode, output(result))
        combined = output(result).lower()
        self.assertIn("isolator", combined)
        self.assertNotIn("sandbox=true", combined)


@unittest.skipUnless(shutil.which("bwrap"), "bwrap not installed — honesty path covered above")
class IsolatorOsBwrapLiveTest(unittest.TestCase):
    def test_live_bwrap_probe_may_set_sandbox_true(self):
        """Optional live probe: only when bwrap exists may sandbox become true."""
        from aisdlc.isolator import probe  # noqa: E402

        # Clear override so real detection runs.
        os.environ.pop("AISDLC_BWRAP", None)
        payload = probe()
        self.assertIn(payload["backend"], ("bwrap", "none"))
        if payload["backend"] == "bwrap" and payload.get("probe_ok"):
            self.assertIs(True, payload["sandbox"])
        else:
            self.assertIs(False, payload["sandbox"])


if __name__ == "__main__":
    unittest.main()
