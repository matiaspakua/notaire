"""Self-improvement loop: proposal-only, Wilson autonomy, no agent approve (#2141)."""
from __future__ import annotations

import json
import math
import unittest
from pathlib import Path

from helpers import KIT, Repo, output


PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[protected]
owner = "the Owner"
paths = ["POLICY.md", ".aisdlc/gates/**", "CONSTITUTION.md"]

[[roles]]
name = "implementer"
tier = "mid"
may_merge = false

[[roles]]
name = "foreman"
tier = "top"
may_merge = true
"""


def _seed_ledger(repo: Repo, runs: list[dict]) -> None:
    ledger = repo.root / ".aisdlc" / "evidence"
    ledger.mkdir(parents=True)
    lines = []
    prev = "genesis"
    for i, run in enumerate(runs):
        record = {
            "ts": f"2026-11-08T20:{i:02d}:00+00:00",
            "prev": prev,
            **run,
        }
        line = json.dumps(record, sort_keys=True)
        lines.append(line)
        import hashlib

        prev = hashlib.sha256(line.encode("utf-8")).hexdigest()
    (ledger / "ledger.jsonl").write_text("\n".join(lines) + "\n", encoding="utf-8")


class WilsonMathTest(unittest.TestCase):
    def test_wilson_lower_bound_perfect_sample(self):
        import sys

        sys.path.insert(0, str(KIT))
        from aisdlc.improve import wilson_lower_bound

        # 30/30 at 95% should be well above 0.80
        lower = wilson_lower_bound(30, 30)
        self.assertGreaterEqual(lower, 0.80)
        self.assertLessEqual(lower, 1.0)

    def test_wilson_lower_bound_rejects_small_n(self):
        import sys

        sys.path.insert(0, str(KIT))
        from aisdlc.improve import wilson_lower_bound

        self.assertIsNone(wilson_lower_bound(5, 5, min_n=30))


class ImproveProposeTest(unittest.TestCase):
    def test_propose_writes_artifact_without_mutating_policy(self):
        repo = Repo(self, PROFILE)
        policy_before = (repo.root / "POLICY.md").read_text(encoding="utf-8")
        _seed_ledger(
            repo,
            [
                {
                    "stage": "pre-pr",
                    "ok": False,
                    "gates": [{"name": "lint", "status": "fail", "seconds": 1.0}],
                },
                {
                    "stage": "pre-pr",
                    "ok": False,
                    "gates": [{"name": "lint", "status": "fail", "seconds": 1.2}],
                },
                {
                    "stage": "green",
                    "ok": True,
                    "gates": [{"name": "unit", "status": "pass", "seconds": 2.0}],
                },
            ],
        )
        result = repo.run("improve", "propose", "--json")
        self.assertEqual(0, result.returncode, output(result))
        proposal = json.loads(result.stdout)
        self.assertEqual("proposed", proposal["status"])
        self.assertFalse(proposal["commits"])
        self.assertIsNone(proposal.get("approved_by"))
        self.assertTrue(proposal["friction"])
        path = Path(proposal["path"])
        self.assertTrue(path.is_file())
        self.assertEqual(policy_before, (repo.root / "POLICY.md").read_text(encoding="utf-8"))
        # never silently rewrite flags / protected policy
        self.assertFalse((repo.root / ".aisdlc" / "flags.json").exists())

    def test_validate_rejects_agent_self_approval(self):
        repo = Repo(self, PROFILE)
        bad = {
            "kind": "self-improve",
            "status": "approved",
            "commits": False,
            "approved_by": "implementer",
            "merge_rule": "foreman",
            "candidate_paths": ["docs/note.md"],
            "protected_paths_touched": [],
            "friction": [],
        }
        path = repo.write(".aisdlc/proposals/bad.json", json.dumps(bad))
        result = repo.run("improve", "validate", "--file", str(path))
        self.assertEqual(1, result.returncode, output(result))
        self.assertIn("agent", result.stdout.lower() + result.stderr.lower())

    def test_validate_requires_owner_for_protected_candidates(self):
        repo = Repo(self, PROFILE)
        bad = {
            "kind": "self-improve",
            "status": "proposed",
            "commits": False,
            "approved_by": None,
            "merge_rule": "foreman",
            "candidate_paths": ["POLICY.md", "docs/note.md"],
            "protected_paths_touched": ["POLICY.md"],
            "friction": [{"what": "policy drift", "rule": "document", "check": "aisdlc doctor"}],
        }
        path = repo.write(".aisdlc/proposals/prot.json", json.dumps(bad))
        result = repo.run("improve", "validate", "--file", str(path))
        self.assertEqual(1, result.returncode, output(result))
        self.assertIn("owner", (result.stdout + result.stderr).lower())

        bad["merge_rule"] = "owner"
        path.write_text(json.dumps(bad), encoding="utf-8")
        ok = repo.run("improve", "validate", "--file", str(path))
        self.assertEqual(0, ok.returncode, output(ok))


class AutonomyWilsonTest(unittest.TestCase):
    def test_autonomy_no_evaluable_under_min_sample(self):
        repo = Repo(self, PROFILE)
        _seed_ledger(
            repo,
            [{"stage": "green", "ok": True, "gates": [{"name": "t", "status": "pass", "seconds": 1.0}]}] * 10,
        )
        result = repo.run("improve", "autonomy", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("no-evaluable", data["status"])

    def test_autonomy_promotes_with_wilson_floor_and_demotes_on_incident(self):
        repo = Repo(self, PROFILE)
        clean = [
            {"stage": "green", "ok": True, "gates": [{"name": "t", "status": "pass", "seconds": 1.0}]}
            for _ in range(30)
        ]
        _seed_ledger(repo, clean)
        result = repo.run("improve", "autonomy", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("ok", data["status"])
        self.assertGreaterEqual(data["wilson_lower"], 0.80)
        self.assertEqual(3, data["level"])

        # inject incident → demote
        import hashlib

        ledger = repo.root / ".aisdlc" / "evidence" / "ledger.jsonl"
        lines = ledger.read_text(encoding="utf-8").splitlines()
        prev = hashlib.sha256(lines[-1].encode("utf-8")).hexdigest()
        incident = json.dumps(
            {
                "ts": "2026-11-08T21:00:00+00:00",
                "prev": prev,
                "stage": "green",
                "ok": False,
                "gates": [{"name": "t", "status": "fail", "seconds": 1.0}],
                "incident": True,
            },
            sort_keys=True,
        )
        with ledger.open("a", encoding="utf-8") as handle:
            handle.write(incident + "\n")
        demoted = repo.run("improve", "autonomy", "--json")
        self.assertEqual(0, demoted.returncode, output(demoted))
        payload = json.loads(demoted.stdout)
        self.assertEqual(1, payload["level"])
        self.assertEqual("demoted", payload["status"])


if __name__ == "__main__":
    unittest.main()
