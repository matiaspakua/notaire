"""ApprovalPort runtime: mint/verify custody, honest approvals_live (#2205)."""
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
level = 1
"""


def _ssh_keygen() -> str | None:
    return shutil.which("ssh-keygen")


def _make_dev_keys(dir_path: Path, identity: str = "owner@example") -> dict[str, str]:
    """Create ephemeral ed25519 key + allowed_signers for local/dev custody tests."""
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


class ApprovalPortTest(unittest.TestCase):
    def test_approval_status_is_no_evaluable_and_not_live_without_keys(self):
        """#2205/#2190: without custody keys, ApprovalPort stays no-evaluable; not live."""
        from aisdlc.approval import approval_status  # noqa: E402

        env_backup = {
            k: os.environ.get(k)
            for k in (
                "AISDLC_APPROVAL_KEY",
                "AISDLC_APPROVAL_ALLOWED_SIGNERS",
                "AISDLC_APPROVAL_IDENTITY",
                "AISDLC_APPROVAL",
            )
        }
        try:
            for k in env_backup:
                os.environ.pop(k, None)
            payload = approval_status()
        finally:
            for k, v in env_backup.items():
                if v is None:
                    os.environ.pop(k, None)
                else:
                    os.environ[k] = v

        self.assertEqual("no-evaluable", payload["status"])
        self.assertIs(False, payload["approvals_live"])
        self.assertIs(False, payload["agent_can_approve"])
        detail = (payload.get("detail") or "").lower()
        self.assertTrue(
            "ssh" in detail or "dsse" in detail or "token" in detail or "key" in detail,
            detail,
        )
        blob = json.dumps(payload).lower()
        self.assertNotIn("approvals_live\": true", blob.replace(" ", ""))
        self.assertNotRegex(blob, r'"approvals_live"\s*:\s*true')
        self.assertNotRegex(blob, r'"agent_can_approve"\s*:\s*true')

    def test_doctor_reports_approval_no_evaluable_without_failing(self):
        """#2205/#2190: doctor surfaces ApprovalPort no-evaluable; does not fail for it alone."""
        repo = Repo(self, PROFILE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        result = repo.run("doctor")
        combined = output(result).lower()
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("approval", combined)
        self.assertTrue(
            "no-evaluable" in combined or "approvals_live=false" in combined,
            combined,
        )
        self.assertNotIn("approvals_live=true", combined)
        self.assertNotIn("agent_can_approve=true", combined)

    def test_approval_design_forbids_agent_self_approval(self):
        """#2205: agent_can_approve stays false; mint refuses agent role."""
        from aisdlc.approval import approval_status, mint  # noqa: E402

        payload = approval_status()
        self.assertIs(False, payload["agent_can_approve"])
        detail = (payload.get("detail") or "").lower()
        self.assertIn("self-approv", detail)

        with tempfile.TemporaryDirectory(prefix="aisdlc-appr-") as tmp:
            keys = _make_dev_keys(Path(tmp))
            subject = Path(tmp) / "subject.txt"
            subject.write_text("digest-subject\n", encoding="utf-8")
            os.environ["AISDLC_APPROVAL_KEY"] = keys["key"]
            os.environ["AISDLC_APPROVAL_ALLOWED_SIGNERS"] = keys["allowed_signers"]
            os.environ["AISDLC_APPROVAL_IDENTITY"] = keys["identity"]
            try:
                result = mint(subject, role="agent")
            finally:
                for k in (
                    "AISDLC_APPROVAL_KEY",
                    "AISDLC_APPROVAL_ALLOWED_SIGNERS",
                    "AISDLC_APPROVAL_IDENTITY",
                ):
                    os.environ.pop(k, None)
        self.assertIs(False, result.get("ok", True))
        self.assertIn("agent", (result.get("detail") or result.get("error") or "").lower())

    def test_mint_verify_round_trip_sets_approvals_live(self):
        """#2205: proven mint→verify round-trip is the only path to approvals_live=true."""
        from aisdlc.approval import approval_status, mint, verify  # noqa: E402

        with tempfile.TemporaryDirectory(prefix="aisdlc-appr-") as tmp:
            root = Path(tmp)
            keys = _make_dev_keys(root)
            subject = root / "subject.txt"
            subject.write_text('{"kind":"approval","digest":"abc123"}\n', encoding="utf-8")
            os.environ["AISDLC_APPROVAL_KEY"] = keys["key"]
            os.environ["AISDLC_APPROVAL_ALLOWED_SIGNERS"] = keys["allowed_signers"]
            os.environ["AISDLC_APPROVAL_IDENTITY"] = keys["identity"]
            os.environ.pop("AISDLC_APPROVAL", None)
            try:
                minted = mint(subject, role="person")
                self.assertTrue(minted.get("ok"), minted)
                self.assertTrue(Path(minted["signature"]).is_file(), minted)
                checked = verify(subject, signature=Path(minted["signature"]))
                self.assertTrue(checked.get("ok"), checked)
                status = approval_status()
            finally:
                for k in (
                    "AISDLC_APPROVAL_KEY",
                    "AISDLC_APPROVAL_ALLOWED_SIGNERS",
                    "AISDLC_APPROVAL_IDENTITY",
                    "AISDLC_APPROVAL",
                ):
                    os.environ.pop(k, None)

        self.assertIs(True, status["approvals_live"], status)
        self.assertEqual("live", status["status"], status)
        self.assertIs(False, status["agent_can_approve"])
        self.assertIn("ssh", (status.get("algorithm") or "").lower())
        self.assertEqual(keys["identity"], status.get("identity"))

    def test_tampered_subject_fails_verify(self):
        """#2205: altered subject bytes fail closed on verify."""
        from aisdlc.approval import mint, verify  # noqa: E402

        with tempfile.TemporaryDirectory(prefix="aisdlc-appr-") as tmp:
            root = Path(tmp)
            keys = _make_dev_keys(root)
            subject = root / "subject.txt"
            subject.write_text("original-bytes\n", encoding="utf-8")
            os.environ["AISDLC_APPROVAL_KEY"] = keys["key"]
            os.environ["AISDLC_APPROVAL_ALLOWED_SIGNERS"] = keys["allowed_signers"]
            os.environ["AISDLC_APPROVAL_IDENTITY"] = keys["identity"]
            try:
                minted = mint(subject, role="person")
                self.assertTrue(minted.get("ok"), minted)
                subject.write_text("tampered-bytes\n", encoding="utf-8")
                checked = verify(subject, signature=Path(minted["signature"]))
            finally:
                for k in (
                    "AISDLC_APPROVAL_KEY",
                    "AISDLC_APPROVAL_ALLOWED_SIGNERS",
                    "AISDLC_APPROVAL_IDENTITY",
                ):
                    os.environ.pop(k, None)
        self.assertIs(False, checked.get("ok", True), checked)

    def test_force_off_keeps_approvals_not_live(self):
        """#2205: AISDLC_APPROVAL=0 forces approvals_live=false even with keys."""
        from aisdlc.approval import approval_status  # noqa: E402

        with tempfile.TemporaryDirectory(prefix="aisdlc-appr-") as tmp:
            keys = _make_dev_keys(Path(tmp))
            os.environ["AISDLC_APPROVAL_KEY"] = keys["key"]
            os.environ["AISDLC_APPROVAL_ALLOWED_SIGNERS"] = keys["allowed_signers"]
            os.environ["AISDLC_APPROVAL_IDENTITY"] = keys["identity"]
            os.environ["AISDLC_APPROVAL"] = "0"
            try:
                payload = approval_status()
            finally:
                for k in (
                    "AISDLC_APPROVAL_KEY",
                    "AISDLC_APPROVAL_ALLOWED_SIGNERS",
                    "AISDLC_APPROVAL_IDENTITY",
                    "AISDLC_APPROVAL",
                ):
                    os.environ.pop(k, None)
        self.assertIs(False, payload["approvals_live"], payload)
        self.assertEqual("no-evaluable", payload["status"], payload)

    def test_cli_approval_status_json_honesty(self):
        """#2205: aisdlc approval status --json reports honesty fields."""
        repo = Repo(self, PROFILE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        result = repo.run("approval", "status", "--json")
        self.assertEqual(0, result.returncode, output(result))
        payload = json.loads(result.stdout)
        self.assertIn("approvals_live", payload)
        self.assertIs(False, payload["approvals_live"])
        self.assertIs(False, payload["agent_can_approve"])


if __name__ == "__main__":
    unittest.main()
