"""Lead-time and stage duration metrics (#2138)."""
from __future__ import annotations

import json
import sys
import unittest
from pathlib import Path

from helpers import KIT, Repo, output

FIXTURE = KIT / "tests" / "fixtures" / "lead-time-notaire-sample.json"


class LeadTimeMetricTest(unittest.TestCase):
    def setUp(self) -> None:
        sys.path.insert(0, str(KIT))
        from aisdlc.metrics import (  # noqa: E402
            EVENT_PAIR,
            lead_time_from_sample,
            redact_metric_attributes,
            stage_durations,
        )

        self.EVENT_PAIR = EVENT_PAIR
        self.lead_time_from_sample = lead_time_from_sample
        self.redact_metric_attributes = redact_metric_attributes
        self.stage_durations = stage_durations

    def test_event_pair_is_documented(self):
        self.assertEqual("issue.created_at", self.EVENT_PAIR["start"])
        self.assertEqual("pull_request.checks_green_at", self.EVENT_PAIR["end"])

    def test_lead_time_from_notaire_sample_fixture(self):
        sample = json.loads(FIXTURE.read_text(encoding="utf-8"))
        result = self.lead_time_from_sample(sample)
        self.assertEqual(2135, result["issue"])
        self.assertEqual(2144, result["pull_request"])
        # 20:00 → 21:45 = 6300 seconds
        self.assertEqual(6300.0, result["seconds"])
        self.assertEqual(self.EVENT_PAIR["start"], result["start_event"])
        self.assertEqual(self.EVENT_PAIR["end"], result["end_event"])

    def test_lead_time_picks_earliest_green_linked_pr(self):
        sample = {
            "sample": {
                "issue": {"number": 1, "created_at": "2026-01-01T00:00:00Z"},
                "pull_requests": [
                    {
                        "number": 10,
                        "checks_green_at": "2026-01-01T03:00:00Z",
                        "linked_issues": [1],
                    },
                    {
                        "number": 9,
                        "checks_green_at": "2026-01-01T01:00:00Z",
                        "linked_issues": [1],
                    },
                ],
            }
        }
        result = self.lead_time_from_sample(sample)
        self.assertEqual(9, result["pull_request"])
        self.assertEqual(3600.0, result["seconds"])

    def test_missing_green_pr_is_no_evaluable(self):
        sample = {
            "sample": {
                "issue": {"number": 2, "created_at": "2026-01-01T00:00:00Z"},
                "pull_requests": [{"number": 3, "linked_issues": [2]}],
            }
        }
        result = self.lead_time_from_sample(sample)
        self.assertEqual("no-evaluable", result["status"])
        self.assertIsNone(result["seconds"])

    def test_metrics_redact_secrets_and_prompts(self):
        clean = self.redact_metric_attributes(
            {
                "aisdlc.issue": 2135,
                "prompt": "leak",
                "diff": "--- a",
                "api_key": "secret",
                "aisdlc.lead_time_seconds": 10,
            }
        )
        self.assertEqual({"aisdlc.issue": 2135, "aisdlc.lead_time_seconds": 10}, clean)


class StageDurationMetricTest(unittest.TestCase):
    def test_stage_durations_from_ledger_runs(self):
        sys.path.insert(0, str(KIT))
        from aisdlc.metrics import stage_durations  # noqa: E402

        runs = [
            {
                "stage": "plan",
                "ok": True,
                "gates": [{"name": "a", "status": "pass", "seconds": 2.0},
                          {"name": "b", "status": "pass", "seconds": 3.0}],
            },
            {
                "stage": "plan",
                "ok": True,
                "gates": [{"name": "a", "status": "pass", "seconds": 4.0}],
            },
            {
                "stage": "green",
                "ok": False,
                "gates": [{"name": "t", "status": "fail", "seconds": 10.0}],
            },
        ]
        stages = stage_durations(runs)
        by_name = {s["stage"]: s for s in stages}
        self.assertEqual(2, by_name["plan"]["runs"])
        # run1 sum=5, run2 sum=4 → median 4.5
        self.assertEqual(4.5, by_name["plan"]["median_seconds"])
        self.assertEqual(10.0, by_name["green"]["median_seconds"])


class MetricsCliTest(unittest.TestCase):
    def test_report_includes_stages_and_fixture_lead_time(self):
        repo = Repo(
            self,
            """
            [project]
            name = "demo"
            policy = "POLICY.md"
            """,
        )
        # Seed a stage run via cost/report path: write ledger manually.
        ledger = repo.root / ".aisdlc" / "evidence"
        ledger.mkdir(parents=True)
        (ledger / "ledger.jsonl").write_text(
            json.dumps(
                {
                    "ts": "2026-11-08T20:00:00+00:00",
                    "stage": "plan",
                    "ok": True,
                    "gates": [{"name": "g", "status": "pass", "seconds": 1.5}],
                    "prev": "genesis",
                },
                sort_keys=True,
            )
            + "\n",
            encoding="utf-8",
        )
        result = repo.run("report", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertIn("stages", data)
        self.assertEqual("plan", data["stages"][0]["stage"])

        lead = repo.run("metrics", "lead-time", "--fixture", str(FIXTURE), "--json")
        self.assertEqual(0, lead.returncode, output(lead))
        payload = json.loads(lead.stdout)
        self.assertEqual(6300.0, payload["seconds"])
        self.assertNotIn("prompt", json.dumps(payload))

    def test_metrics_emit_to_telemetry_port_when_bound(self):
        sys.path.insert(0, str(KIT))
        from aisdlc.metrics import emit_lead_time  # noqa: E402
        from aisdlc.telemetry import MemoryTelemetry  # noqa: E402

        port = MemoryTelemetry()
        emit_lead_time(
            port,
            {
                "issue": 1,
                "pull_request": 2,
                "seconds": 100.0,
                "status": "ok",
                "start_event": "issue.created_at",
                "end_event": "pull_request.checks_green_at",
                "prompt": "NO",
            },
        )
        blob = json.dumps(port.events())
        self.assertIn("aisdlc.lead_time", blob)
        self.assertNotIn("NO", blob)
        self.assertNotIn("prompt", blob)


if __name__ == "__main__":
    unittest.main()
