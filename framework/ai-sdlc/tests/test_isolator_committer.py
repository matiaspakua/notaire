"""Isolator sole-committer enforcement (#2200) — proven probe; works without bwrap."""
from __future__ import annotations

import json
import os
import subprocess
import sys
import unittest
from unittest import mock

from helpers import BIN, ENV, KIT, Repo, git, output

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


class IsolatorCommitterTest(unittest.TestCase):
    def test_commit_without_grant_fails_under_isolator_custody(self):
        """#2200: with custody hooks active and no AISDLC_COMMIT_GRANT, git commit fails."""
        from aisdlc import isolator  # noqa: E402

        repo = Repo(self, BASE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        isolator.install_commit_custody(repo.root)
        (repo.root / "docs").mkdir(exist_ok=True)
        (repo.root / "docs" / "note.md").write_text("x\n", encoding="utf-8")
        git(repo.root, "add", "-A")
        env = {**ENV}
        env.pop("AISDLC_COMMIT_GRANT", None)
        result = subprocess.run(
            ["git", "commit", "-m", "should fail"],
            cwd=repo.root,
            env=env,
            capture_output=True,
            text=True,
        )
        self.assertNotEqual(0, result.returncode, output(result))

    def test_enforces_committer_true_only_after_proven_probe(self):
        """#2200: enforces_committer becomes true only when interception probe fails closed."""
        from aisdlc import isolator  # noqa: E402

        with mock.patch.dict(os.environ, {"AISDLC_BWRAP": ""}), mock.patch.object(
            isolator, "probe_committer_enforcement", return_value=False
        ):
            payload = isolator.probe()
        self.assertIs(False, payload["enforces_committer"])

        with mock.patch.dict(os.environ, {"AISDLC_BWRAP": ""}), mock.patch.object(
            isolator, "probe_committer_enforcement", return_value=True
        ):
            payload = isolator.probe()
        self.assertIs(True, payload["enforces_committer"])

    def test_enforces_committer_works_without_bwrap(self):
        """#2200: without bwrap, a working hooks probe may still set enforces_committer=true."""
        from aisdlc import isolator  # noqa: E402

        with mock.patch.dict(os.environ, {"AISDLC_BWRAP": ""}):
            payload = isolator.probe()
        self.assertIs(False, payload["sandbox"])
        self.assertEqual("none", payload["backend"])
        # Real git hooks probe should succeed on this host.
        self.assertIs(True, payload["enforces_committer"])

    def test_sandbox_stays_false_without_containment_probe(self):
        """#2200: committer enforcement success must not flip sandbox without containment."""
        from aisdlc import isolator  # noqa: E402

        with mock.patch.dict(os.environ, {"AISDLC_BWRAP": ""}), mock.patch.object(
            isolator, "probe_committer_enforcement", return_value=True
        ):
            payload = isolator.isolator_status()
        self.assertIs(True, payload["enforces_committer"])
        self.assertIs(False, payload["sandbox"])

    def test_wrap_blocks_agent_git_commit(self):
        """#2200: isolator wrap runs with custody; agent commit without grant fails."""
        repo = Repo(self, BASE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        (repo.root / "docs").mkdir(exist_ok=True)
        (repo.root / "docs" / "note.md").write_text("blocked\n", encoding="utf-8")
        # wrap a shell that stages and commits without grant
        script = (
            "git add -A && "
            "git commit -m 'agent commit' ; "
            "echo EXIT:$? "
        )
        result = _run(
            repo,
            "isolator",
            "wrap",
            "--",
            "bash",
            "-c",
            script,
            env={"AISDLC_BWRAP": "", "AISDLC_COMMIT_GRANT": ""},
        )
        self.assertEqual(0, result.returncode, output(result))
        combined = output(result)
        self.assertIn("EXIT:", combined)
        # The inner git commit must have failed (non-zero EXIT)
        self.assertRegex(combined, r"EXIT:[1-9]")

    def test_grant_allows_owner_runtime_commit(self):
        """#2200: with AISDLC_COMMIT_GRANT set, commit under custody is allowed."""
        from aisdlc import isolator  # noqa: E402

        repo = Repo(self, BASE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        isolator.install_commit_custody(repo.root)
        (repo.root / "docs").mkdir(exist_ok=True)
        (repo.root / "docs" / "ok.md").write_text("ok\n", encoding="utf-8")
        git(repo.root, "add", "-A")
        env = {**ENV, "AISDLC_COMMIT_GRANT": "1"}
        result = subprocess.run(
            ["git", "commit", "-m", "owner commit"],
            cwd=repo.root,
            env=env,
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, result.returncode, output(result))

    def test_doctor_never_claims_enforces_committer_without_probe(self):
        """#2200: doctor must not claim enforces_committer=true when probe did not prove it."""
        repo = Repo(self, BASE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        from helpers import ENV as helpers_env

        helpers_env["AISDLC_BWRAP"] = ""
        helpers_env["AISDLC_ISOLATOR_COMMITTER"] = "0"
        try:
            result = _run(
                repo,
                "doctor",
                env={"AISDLC_BWRAP": "", "AISDLC_ISOLATOR_COMMITTER": "0"},
            )
        finally:
            helpers_env.pop("AISDLC_BWRAP", None)
            helpers_env.pop("AISDLC_ISOLATOR_COMMITTER", None)
        self.assertEqual(0, result.returncode, output(result))
        combined = output(result).lower()
        self.assertIn("isolator", combined)
        self.assertNotIn("enforces_committer=true", combined)


if __name__ == "__main__":
    unittest.main()
