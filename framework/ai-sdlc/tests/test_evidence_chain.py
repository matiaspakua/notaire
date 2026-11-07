"""The evidence ledger is hash-chained: an altered record is detected, not trusted.

Source: agentic-os journal (each entry carries the SHA-256 of the one before it) and the
factory-control assessment (F-05). The chain cannot see a change to its own last line, so the
verifier prints the head digest for the Foreman to anchor in the PR.
"""
from __future__ import annotations

import json
import unittest

from helpers import Repo, output

GATE = """
    [[gates]]
    name = "lint"
    stage = "pre-push"
    run = "true"
"""


def ledger_path(repo: Repo):
    return repo.root / ".aisdlc" / "evidence" / "ledger.jsonl"


class EvidenceChainTest(unittest.TestCase):
    def test_records_link_to_the_line_before_them(self):
        repo = Repo(self, GATE)
        repo.run("gate", "pre-push")
        repo.run("gate", "pre-push")
        records = repo.ledger()
        self.assertEqual("genesis", records[0]["prev"])
        self.assertNotEqual("genesis", records[1]["prev"])
        result = repo.run("evidence", "verify")
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("2 chained", output(result))
        self.assertIn("head", output(result))

    def test_edited_record_breaks_the_chain(self):
        repo = Repo(self, GATE)
        for _ in range(3):
            repo.run("gate", "pre-push")
        path = ledger_path(repo)
        lines = path.read_text(encoding="utf-8").splitlines()
        first = json.loads(lines[0])
        first["ok"] = not first["ok"]
        lines[0] = json.dumps(first, sort_keys=True)
        path.write_text("\n".join(lines) + "\n", encoding="utf-8")
        result = repo.run("evidence", "verify")
        self.assertEqual(1, result.returncode)
        self.assertIn("line 2", output(result))

    def test_deleted_record_breaks_the_chain(self):
        repo = Repo(self, GATE)
        for _ in range(3):
            repo.run("gate", "pre-push")
        path = ledger_path(repo)
        lines = path.read_text(encoding="utf-8").splitlines()
        path.write_text("\n".join([lines[0], lines[2]]) + "\n", encoding="utf-8")
        self.assertEqual(1, repo.run("evidence", "verify").returncode)

    def test_legacy_records_are_accepted_only_as_a_prefix(self):
        repo = Repo(self, GATE)
        path = ledger_path(repo)
        path.parent.mkdir(parents=True)
        legacy = json.dumps({"ts": "2026-10-01T00:00:00+00:00", "stage": "pre-push", "ok": True, "gates": []})
        path.write_text(legacy + "\n", encoding="utf-8")
        repo.run("gate", "pre-push")
        result = repo.run("evidence", "verify")
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("1 legacy", output(result))
        with path.open("a", encoding="utf-8") as handle:
            handle.write(legacy + "\n")
        self.assertEqual(1, repo.run("evidence", "verify").returncode)

    def test_cost_log_is_chained_too(self):
        repo = Repo(self, GATE)
        repo.run("cost", "--agent", "worker", "--model", "m", "--input", "10", "--output", "5", "--usd", "0.1")
        repo.run("cost", "--agent", "worker", "--model", "m", "--input", "10", "--output", "5", "--usd", "0.1")
        result = repo.run("evidence", "verify")
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("cost.jsonl: 2 chained", output(result))

    def test_anchor_detects_a_truncated_tail(self):
        repo = Repo(self, GATE)
        for _ in range(3):
            repo.run("gate", "pre-push")
        head = repo.run("evidence", "verify").stdout.split("head ")[1].split()[0]
        self.assertEqual(0, repo.run("evidence", "verify", "--anchor", head).returncode)
        path = ledger_path(repo)
        lines = path.read_text(encoding="utf-8").splitlines()
        path.write_text("\n".join(lines[:2]) + "\n", encoding="utf-8")
        self.assertEqual(0, repo.run("evidence", "verify").returncode)   # a prefix is a valid chain
        result = repo.run("evidence", "verify", "--anchor", head)
        self.assertEqual(1, result.returncode)
        self.assertIn("truncated", output(result))

    def test_emptied_ledger_is_no_evidence_and_fails_against_an_anchor(self):
        repo = Repo(self, GATE)
        repo.run("gate", "pre-push")
        head = repo.run("evidence", "verify").stdout.split("head ")[1].split()[0]
        ledger_path(repo).unlink()
        result = repo.run("evidence", "verify")
        self.assertEqual(0, result.returncode)
        self.assertIn("NO EVIDENCE", output(result))
        self.assertNotIn("PASSED", output(result))
        self.assertEqual(1, repo.run("evidence", "verify", "--anchor", head).returncode)

    def test_report_still_reads_chained_records(self):
        repo = Repo(self, GATE)
        repo.run("gate", "pre-push")
        result = repo.run("report", "--json")
        self.assertEqual(0, result.returncode, output(result))
        self.assertEqual(1, json.loads(result.stdout)["runs"])


if __name__ == "__main__":
    unittest.main()
