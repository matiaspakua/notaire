"""Workflow governance FSM + ApprovalPort honesty (#2239 Phase A)."""
from __future__ import annotations

import json
import os
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from helpers import ENV, KIT, Repo, output

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""


def _ssh_keygen() -> str | None:
    return shutil.which("ssh-keygen")


def _make_dev_keys(dir_path: Path, identity: str = "owner@example") -> dict[str, str]:
    binary = _ssh_keygen()
    if not binary:
        raise unittest.SkipTest("ssh-keygen not available")
    key = dir_path / "dev_ed25519"
    subprocess.run(
        [binary, "-t", "ed25519", "-f", str(key), "-N", "", "-C", "aisdlc-approval-fixture", "-q"],
        check=True,
        capture_output=True,
        text=True,
        env=ENV,
    )
    pub = (dir_path / "dev_ed25519.pub").read_text(encoding="utf-8").strip()
    allowed = dir_path / "allowed_signers"
    allowed.write_text(f'{identity} namespaces="file" {pub}\n', encoding="utf-8")
    return {
        "key": str(key),
        "allowed_signers": str(allowed),
        "identity": identity,
        "namespace": "file",
    }


class WorkflowGovernanceTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        # Seed catalog refs a workflow may point at.
        for kind, cid in (("agents", "implementer"), ("skills", "java"), ("mcp", "github")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )

    def _create(self, wid: str = "feature-delivery") -> None:
        created = self.repo.run(
            "workflow", "create", "--id", wid,
            "--summary", "Feature delivery blueprint",
            "--agent", "implementer",
            "--skill", "java",
            "--mcp", "github",
            "--json",
        )
        self.assertEqual(0, created.returncode, output(created))
        payload = json.loads(created.stdout)
        self.assertEqual("draft", payload["governance"])
        self.assertEqual(1, payload["version"])

    def test_illegal_approve_from_draft_fails(self):
        self._create()
        result = self.repo.run(
            "workflow", "transition", "--id", "feature-delivery",
            "--action", "approve", "--owner-override", "--reason", "should-not-matter",
            "--json",
        )
        self.assertNotEqual(0, result.returncode)
        shown = self.repo.run("workflow", "show", "--id", "feature-delivery", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        self.assertEqual("draft", json.loads(shown.stdout)["governance"])

    def test_happy_path_with_owner_override_when_not_live(self):
        self._create()
        define = self.repo.run(
            "workflow", "transition", "--id", "feature-delivery", "--action", "define", "--json",
        )
        self.assertEqual(0, define.returncode, output(define))
        self.assertEqual("defined", json.loads(define.stdout)["governance"])

        validate = self.repo.run(
            "workflow", "transition", "--id", "feature-delivery",
            "--action", "request_validation", "--waive-reason", "dry-run waived in unit test",
            "--json",
        )
        self.assertEqual(0, validate.returncode, output(validate))
        self.assertEqual("validated", json.loads(validate.stdout)["governance"])

        # Without override and without live approvals → fail closed.
        blocked = self.repo.run(
            "workflow", "transition", "--id", "feature-delivery", "--action", "approve", "--json",
        )
        self.assertNotEqual(0, blocked.returncode)

        approve = self.repo.run(
            "workflow", "transition", "--id", "feature-delivery",
            "--action", "approve", "--owner-override", "--reason", "Owner Phase A test override",
            "--json",
        )
        self.assertEqual(0, approve.returncode, output(approve))
        self.assertEqual("approved", json.loads(approve.stdout)["governance"])

    def test_approved_filter_excludes_draft(self):
        self._create("keep-draft")
        self._create("promote-me")
        for action, extra in (
            ("define", []),
            ("request_validation", ["--waive-reason", "unit"]),
            ("approve", ["--owner-override", "--reason", "unit"]),
        ):
            result = self.repo.run(
                "workflow", "transition", "--id", "promote-me", "--action", action, *extra, "--json",
            )
            self.assertEqual(0, result.returncode, output(result))

        listed = self.repo.run("workflow", "list", "--approved", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        rows = json.loads(listed.stdout)
        ids = [row["id"] for row in rows["entries"]]
        self.assertIn("promote-me", ids)
        self.assertNotIn("keep-draft", ids)

    def test_live_approval_verify_path(self):
        if not _ssh_keygen():
            self.skipTest("ssh-keygen not available")
        keys_dir = Path(tempfile.mkdtemp(prefix="aisdlc-wf-keys-"))
        self.addCleanup(shutil.rmtree, keys_dir, ignore_errors=True)
        keys = _make_dev_keys(keys_dir)

        env = dict(ENV)
        env["AISDLC_APPROVAL_KEY"] = keys["key"]
        env["AISDLC_APPROVAL_ALLOWED_SIGNERS"] = keys["allowed_signers"]
        env["AISDLC_APPROVAL_IDENTITY"] = keys["identity"]
        env["AISDLC_APPROVAL_NAMESPACE"] = keys["namespace"]

        self._create("signed-flow")
        for action, extra in (
            ("define", []),
            ("request_validation", ["--waive-reason", "unit"]),
        ):
            result = subprocess.run(
                [
                    sys.executable, str(KIT / "bin" / "aisdlc"),
                    "--root", str(self.repo.root),
                    "workflow", "transition", "--id", "signed-flow",
                    "--action", action, *extra, "--json",
                ],
                cwd=self.repo.root,
                env=env,
                capture_output=True,
                text=True,
            )
            self.assertEqual(0, result.returncode, output(result))

        # Mint a signature over the current workflow subject file.
        subject = (
            self.repo.root / ".aisdlc" / "catalogs" / "workflows" / "signed-flow" / "current.json"
        )
        mint = subprocess.run(
            [
                sys.executable, str(KIT / "bin" / "aisdlc"),
                "--root", str(self.repo.root),
                "approval", "mint", "--subject", str(subject), "--role", "person", "--json",
            ],
            cwd=self.repo.root,
            env=env,
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, mint.returncode, output(mint))
        sig = json.loads(mint.stdout)["signature"]

        approve = subprocess.run(
            [
                sys.executable, str(KIT / "bin" / "aisdlc"),
                "--root", str(self.repo.root),
                "workflow", "transition", "--id", "signed-flow",
                "--action", "approve", "--signature", sig, "--json",
            ],
            cwd=self.repo.root,
            env=env,
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, approve.returncode, output(approve))
        self.assertEqual("approved", json.loads(approve.stdout)["governance"])


if __name__ == "__main__":
    unittest.main()
