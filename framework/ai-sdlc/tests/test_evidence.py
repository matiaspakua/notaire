"""Every stage run leaves an auditable record; the report and cost ledger read it (#1958)."""
from __future__ import annotations

import json
import unittest

from helpers import Repo, output

TWO_GATES = """
    [[gates]]
    name = "lint"
    stage = "pre-push"
    run = "true"

    [[gates]]
    name = "tests"
    stage = "pre-push"
    run = "test -f marker"
"""


class EvidenceLedgerTest(unittest.TestCase):
    def test_stage_run_appends_one_record_with_every_gate(self):
        repo = Repo(self, TWO_GATES)
        repo.run("gate", "pre-push")
        records = repo.ledger()
        self.assertEqual(1, len(records))
        record = records[0]
        self.assertEqual("pre-push", record["stage"])
        self.assertEqual(repo.base, record["commit"])
        self.assertFalse(record["ok"])
        self.assertEqual(["lint", "tests"], [g["name"] for g in record["gates"]])
        for gate in record["gates"]:
            self.assertIn(gate["status"], {"pass", "fail", "skip"})
            self.assertIn("exit", gate)
            self.assertIn("seconds", gate)

    def test_failed_gate_keeps_the_tail_of_its_output(self):
        repo = Repo(self, """
            [[gates]]
            name = "noisy"
            stage = "pre-push"
            run = "echo the-reason; exit 1"
        """)
        repo.run("gate", "pre-push")
        gate = repo.ledger()[0]["gates"][0]
        self.assertIn("the-reason", "\n".join(gate["tail"]))

    def test_report_shows_runs_and_pass_rate_per_gate(self):
        repo = Repo(self, TWO_GATES)
        repo.run("gate", "pre-push")
        repo.write("marker", "x\n")
        repo.run("gate", "pre-push")
        result = repo.run("report", "--json")
        self.assertEqual(0, result.returncode, output(result))
        gates = {g["gate"]: g for g in json.loads(result.stdout)["gates"]}
        self.assertEqual(2, gates["tests"]["runs"])
        self.assertEqual(50.0, gates["tests"]["pass_rate"])
        self.assertEqual(100.0, gates["lint"]["pass_rate"])

    def test_report_markdown_renders_a_table(self):
        repo = Repo(self, TWO_GATES)
        repo.run("gate", "pre-push")
        result = repo.run("report")
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("| pre-push | tests |", result.stdout)


class CostLedgerTest(unittest.TestCase):
    def test_cost_is_priced_from_the_profile_and_reported(self):
        repo = Repo(self, """
            [cost.prices."model-a"]
            input = 3.0
            output = 15.0
        """)
        result = repo.run("cost", "--agent", "worker", "--model", "model-a",
                          "--input", "1000000", "--output", "100000", "--issue", "7")
        self.assertEqual(0, result.returncode, output(result))
        report = json.loads(repo.run("report", "--json").stdout)
        self.assertAlmostEqual(4.5, report["cost"]["total_usd"])
        self.assertEqual(1, report["cost"]["by_model"]["model-a"]["entries"])

    def test_unpriced_model_records_null_usd(self):
        repo = Repo(self, "")
        result = repo.run("cost", "--agent", "worker", "--model", "unknown", "--input", "10", "--output", "5")
        self.assertEqual(0, result.returncode, output(result))
        report = json.loads(repo.run("report", "--json").stdout)
        self.assertEqual(0.0, report["cost"]["total_usd"])
        self.assertEqual(1, report["cost"]["unpriced_entries"])


if __name__ == "__main__":
    unittest.main()
