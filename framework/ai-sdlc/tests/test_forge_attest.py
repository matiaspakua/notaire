"""ForgePort write / factory gate attest gated by ApprovalPort (#2208).

TDD: these tests MUST fail before attest/write exists.
"""
from __future__ import annotations

import json
import os
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from typing import Any

from helpers import ENV, KIT, Repo, output

sys.path.insert(0, str(KIT))


PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"
level = 1
"""


class _FakeGh:
    """Injectable gh runner that records argv."""

    def __init__(self, responses: dict[tuple[str, ...], str | tuple[int, str]] | None = None) -> None:
        self.calls: list[list[str]] = []
        self._responses = responses or {}

    def __call__(self, argv: list[str]) -> subprocess.CompletedProcess[str]:
        self.calls.append(list(argv))
        key = tuple(argv)
        payload = self._responses.get(key)
        if payload is None:
            for stored, value in self._responses.items():
                if argv[: len(stored)] == list(stored):
                    payload = value
                    break
        if payload is None:
            # Default success for scoped writes under test.
            return subprocess.CompletedProcess(argv, 0, stdout='{"ok":true}\n', stderr="")
        if isinstance(payload, tuple):
            code, stdout = payload
        else:
            code, stdout = 0, payload
        return subprocess.CompletedProcess(argv, code, stdout=stdout, stderr="")


def _mutating(calls: list[list[str]]) -> list[list[str]]:
    banned = {
        "comment",
        "create",
        "edit",
        "close",
        "reopen",
        "merge",
        "delete",
        "approve",
        "label",
        "add-label",
        "remove-label",
    }
    out: list[list[str]] = []
    for argv in calls:
        tokens = {t.lower().lstrip("-") for t in argv}
        if tokens & banned:
            out.append(argv)
        # checks API POST is a write
        if len(argv) >= 2 and argv[0] == "api" and "-X" in argv:
            out.append(argv)
    return out


class ForgePortAttestTest(unittest.TestCase):
    def test_without_approval_verify_refuses_and_stays_unattested(self):
        """#2208: no verify → attested=false, write_enabled=false, no gh mutation."""
        from aisdlc.forge import attest  # noqa: E402

        fake = _FakeGh()

        def verify_fail(_subject: Path, **_kwargs: Any) -> dict[str, Any]:
            return {
                "ok": False,
                "approvals_live": False,
                "agent_can_approve": False,
                "detail": "no signature",
            }

        payload = attest(
            gate="preflight",
            outcome="pass",
            write=False,
            runner=fake,
            verify_fn=verify_fail,
            approval_status_fn=lambda **_: {
                "status": "no-evaluable",
                "approvals_live": False,
                "agent_can_approve": False,
            },
        )
        self.assertIs(False, payload["attested"])
        self.assertIs(False, payload["write_enabled"])
        self.assertIs(False, payload["approvals_live"])
        self.assertEqual([], _mutating(fake.calls))
        self.assertIn("write_enabled", payload)
        self.assertIn("detail", payload)

    def test_verified_approval_dry_run_attests_without_writing(self):
        """#2208: verify OK + no --write → attested=true, write_enabled=false."""
        from aisdlc.forge import attest  # noqa: E402

        fake = _FakeGh()

        def verify_ok(subject: Path, **_kwargs: Any) -> dict[str, Any]:
            self.assertTrue(subject.is_file())
            return {
                "ok": True,
                "approvals_live": True,
                "agent_can_approve": False,
                "detail": "good signature",
            }

        payload = attest(
            gate="preflight",
            outcome="pass",
            write=False,
            runner=fake,
            verify_fn=verify_ok,
            approval_status_fn=lambda **_: {
                "status": "live",
                "approvals_live": True,
                "agent_can_approve": False,
            },
        )
        self.assertIs(True, payload["attested"])
        self.assertIs(False, payload["write_enabled"])
        self.assertIs(True, payload["approvals_live"])
        self.assertEqual([], _mutating(fake.calls))
        subject = payload.get("subject")
        self.assertTrue(subject)
        data = json.loads(Path(subject).read_text(encoding="utf-8"))
        self.assertEqual(1, data["schema"])
        self.assertEqual("factory/gate", data["kind"])
        self.assertEqual("preflight", data["gate"])

    def test_write_without_verify_is_refused(self):
        """#2208: --write without Approval verify never mutates."""
        from aisdlc.forge import attest  # noqa: E402

        fake = _FakeGh()

        def verify_fail(_subject: Path, **_kwargs: Any) -> dict[str, Any]:
            return {"ok": False, "approvals_live": False, "detail": "bad sig"}

        payload = attest(
            gate="preflight",
            outcome="pass",
            write=True,
            kind="comment",
            pr=2207,
            runner=fake,
            verify_fn=verify_fail,
            approval_status_fn=lambda **_: {"approvals_live": False, "status": "no-evaluable"},
        )
        self.assertIs(False, payload["attested"])
        self.assertIs(False, payload["write_enabled"])
        self.assertEqual([], _mutating(fake.calls))

    def test_write_with_verify_performs_scoped_comment_only(self):
        """#2208: verify + --write → scoped comment; write_enabled=true."""
        from aisdlc.forge import attest  # noqa: E402

        fake = _FakeGh()

        def verify_ok(_subject: Path, **_kwargs: Any) -> dict[str, Any]:
            return {"ok": True, "approvals_live": True, "detail": "good"}

        payload = attest(
            gate="preflight",
            outcome="pass",
            write=True,
            kind="comment",
            pr=2207,
            runner=fake,
            verify_fn=verify_ok,
            approval_status_fn=lambda **_: {"approvals_live": True, "status": "live"},
        )
        self.assertIs(True, payload["attested"])
        self.assertIs(True, payload["write_enabled"])
        mut = _mutating(fake.calls)
        self.assertTrue(mut, "expected a scoped write call")
        flat = " ".join(" ".join(c) for c in mut).lower()
        self.assertIn("comment", flat)
        self.assertNotIn(" merge", f" {flat} ")
        self.assertNotIn("close", flat)
        self.assertNotIn("approve", flat)

    def test_write_with_verify_scoped_label_and_check_run(self):
        """#2208: label and check-run kinds are allowed when verified."""
        from aisdlc.forge import attest  # noqa: E402

        def verify_ok(_subject: Path, **_kwargs: Any) -> dict[str, Any]:
            return {"ok": True, "approvals_live": True, "detail": "good"}

        for kind in ("label", "check-run"):
            fake = _FakeGh()
            payload = attest(
                gate="preflight",
                outcome="pass",
                write=True,
                kind=kind,
                pr=99,
                runner=fake,
                verify_fn=verify_ok,
                approval_status_fn=lambda **_: {"approvals_live": True, "status": "live"},
            )
            self.assertIs(True, payload["write_enabled"], kind)
            self.assertTrue(_mutating(fake.calls), kind)

    def test_unscoped_mutation_verbs_remain_refused(self):
        """#2208: merge/close/approve stay refused even on write path."""
        from aisdlc.forge import ForgeError, assert_scoped_write  # noqa: E402

        with self.assertRaises(ForgeError):
            assert_scoped_write(["pr", "merge", "1"])
        with self.assertRaises(ForgeError):
            assert_scoped_write(["issue", "close", "1"])
        with self.assertRaises(ForgeError):
            assert_scoped_write(["pr", "review", "approve", "1"])

    def test_doctor_reports_forge_write_honesty_without_failing(self):
        """#2208: doctor INFO for forge write; does not fail for dry-run alone."""
        repo = Repo(self, PROFILE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        result = repo.run("doctor")
        combined = output(result).lower()
        self.assertEqual(0, result.returncode, output(result))
        self.assertTrue(
            "forge" in combined and ("attest" in combined or "write" in combined),
            combined,
        )
        self.assertNotIn("write_enabled=true", combined.replace(" ", ""))

    def test_json_honesty_fields_present_on_cli(self):
        """#2208: forge attest --json emits honesty fields."""
        repo = Repo(self, PROFILE)
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        subject = repo.root / "gate-attest.json"
        subject.write_text(
            json.dumps(
                {
                    "schema": 1,
                    "kind": "factory/gate",
                    "gate": "preflight",
                    "outcome": "pass",
                    "source": "aisdlc-forge-attest",
                }
            )
            + "\n",
            encoding="utf-8",
        )
        result = repo.run(
            "forge",
            "attest",
            "--gate",
            "preflight",
            "--subject",
            str(subject),
            "--json",
        )
        self.assertIn(result.returncode, (0, 1), output(result))
        payload = json.loads(result.stdout)
        for key in ("write_enabled", "attested", "approvals_live", "detail"):
            self.assertIn(key, payload)


class ForgePortAttestCustodyTest(unittest.TestCase):
    """Optional real SSH round-trip when ssh-keygen -Y is available."""

    def test_real_approval_verify_dry_run(self):
        import shutil

        from aisdlc import approval  # noqa: E402
        from aisdlc.forge import attest  # noqa: E402

        if not shutil.which("ssh-keygen") or approval.ssh_keygen_path() is None:
            self.skipTest("ssh-keygen -Y unavailable")

        with tempfile.TemporaryDirectory(prefix="aisdlc-forge-attest-") as tmp:
            tmp_path = Path(tmp)
            key = tmp_path / "dev_ed25519"
            subprocess.run(
                [
                    "ssh-keygen",
                    "-t",
                    "ed25519",
                    "-f",
                    str(key),
                    "-N",
                    "",
                    "-C",
                    "aisdlc-forge-attest",
                    "-q",
                ],
                check=True,
                capture_output=True,
                env=ENV,
            )
            identity = "owner@example"
            pub = (tmp_path / "dev_ed25519.pub").read_text(encoding="utf-8").strip()
            allowed = tmp_path / "allowed_signers"
            allowed.write_text(f'{identity} namespaces="file" {pub}\n', encoding="utf-8")
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
                os.environ["AISDLC_APPROVAL_KEY"] = str(key)
                os.environ["AISDLC_APPROVAL_ALLOWED_SIGNERS"] = str(allowed)
                os.environ["AISDLC_APPROVAL_IDENTITY"] = identity
                os.environ.pop("AISDLC_APPROVAL", None)
                subject = tmp_path / "subject.json"
                fake = _FakeGh()
                # First build+sign via mint after subject exists: use forge to write subject,
                # then mint, then attest with real verify (no inject).
                payload_plan = attest(
                    gate="preflight",
                    outcome="pass",
                    subject=subject,
                    write=False,
                    runner=fake,
                    verify_fn=lambda *_a, **_k: {
                        "ok": False,
                        "approvals_live": False,
                        "detail": "unsigned",
                    },
                    approval_status_fn=lambda **_: {"approvals_live": True, "status": "live"},
                )
                # subject should exist for signing even when verify fails
                self.assertTrue(subject.is_file() or Path(payload_plan.get("subject") or subject).is_file())
                subj = Path(payload_plan.get("subject") or subject)
                minted = approval.mint(subj, role="person")
                self.assertTrue(minted.get("ok"), minted)
                fake2 = _FakeGh()
                payload = attest(
                    gate="preflight",
                    outcome="pass",
                    subject=subj,
                    signature=Path(minted["signature"]),
                    write=False,
                    runner=fake2,
                )
                self.assertIs(True, payload["attested"], payload)
                self.assertIs(False, payload["write_enabled"], payload)
                self.assertEqual([], _mutating(fake2.calls))
            finally:
                for k, v in env_backup.items():
                    if v is None:
                        os.environ.pop(k, None)
                    else:
                        os.environ[k] = v


if __name__ == "__main__":
    unittest.main()
