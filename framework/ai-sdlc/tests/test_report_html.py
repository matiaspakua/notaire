"""Static HTML monitor for aisdlc report (#2142) — read-only, no secrets."""
from __future__ import annotations

import json
import unittest
from pathlib import Path

from helpers import KIT, Repo, output

FIXTURE = KIT / "tests" / "fixtures" / "lead-time-notaire-sample.json"


class ReportHtmlTest(unittest.TestCase):
    def test_report_html_writes_stage_and_cost_summaries(self):
        repo = Repo(
            self,
            """
            [project]
            name = "demo"
            policy = "POLICY.md"

            [cost.prices.demo-model]
            input = 1.0
            output = 2.0
            """,
        )
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
                    "prompt": "SECRET_PROMPT_SHOULD_NOT_APPEAR",
                },
                sort_keys=True,
            )
            + "\n",
            encoding="utf-8",
        )
        (ledger / "cost.jsonl").write_text(
            json.dumps(
                {
                    "ts": "2026-11-08T20:01:00+00:00",
                    "agent": "worker",
                    "model": "demo-model",
                    "input": 1000,
                    "output": 500,
                    "usd": 0.002,
                    "issue": "2142",
                    "prev": "genesis",
                },
                sort_keys=True,
            )
            + "\n",
            encoding="utf-8",
        )
        out = repo.root / "monitor.html"
        result = repo.run("report", "--html", str(out))
        self.assertEqual(0, result.returncode, output(result))
        self.assertTrue(out.is_file())
        html = out.read_text(encoding="utf-8")
        self.assertIn("<!DOCTYPE html>", html)
        self.assertIn("plan", html)
        self.assertIn("pass", html.lower())
        self.assertIn("demo-model", html)
        self.assertIn("0.002", html)
        self.assertNotIn("SECRET_PROMPT_SHOULD_NOT_APPEAR", html)
        self.assertNotIn("prompt", html.lower())

    def test_report_json_and_html_surface_wilson_l0_autonomy(self):
        """#2165: report embeds honest L0 autonomy from signal gates."""
        repo = Repo(
            self,
            """
            [project]
            name = "demo"
            policy = "POLICY.md"
            """,
        )
        ledger = repo.root / ".aisdlc" / "evidence"
        ledger.mkdir(parents=True)
        import hashlib

        lines = []
        prev = "genesis"
        for i in range(15):
            record = {
                "ts": f"2026-11-09T06:{i:02d}:00+00:00",
                "prev": prev,
                "stage": "signal",
                "ok": True,
                "gates": [
                    {"name": "ratchet", "status": "pass", "required": True, "seconds": 0.1},
                    {"name": "skills", "status": "pass", "required": True, "seconds": 0.1},
                ],
            }
            line = json.dumps(record, sort_keys=True)
            lines.append(line)
            prev = hashlib.sha256(line.encode("utf-8")).hexdigest()
        (ledger / "ledger.jsonl").write_text("\n".join(lines) + "\n", encoding="utf-8")

        json_result = repo.run("report", "--json")
        self.assertEqual(0, json_result.returncode, output(json_result))
        payload = json.loads(json_result.stdout)
        auto = payload.get("autonomy") or {}
        self.assertEqual("ok", auto.get("status"))
        self.assertEqual(0, auto.get("level"))
        self.assertNotIn(auto.get("level"), (2, 3))
        self.assertGreaterEqual(auto.get("wilson_lower") or 0, 0.80)

        out = repo.root / "monitor.html"
        html_result = repo.run("report", "--html", str(out))
        self.assertEqual(0, html_result.returncode, output(html_result))
        html = out.read_text(encoding="utf-8")
        self.assertIn("Autonomy", html)
        self.assertIn("L0", html)
        self.assertRegex(html.lower(), r"status:\s*<strong>ok</strong>|status.*\bok\b")
        self.assertNotIn("level=3", html.lower())
        self.assertNotRegex(html, r"level:\s*<strong>L[23]")
        self.assertNotIn("level 3", html.lower().replace("never claims level 3", ""))

    def test_report_html_embeds_lead_time_fixture_without_secrets(self):
        repo = Repo(
            self,
            """
            [project]
            name = "demo"
            policy = "POLICY.md"
            """,
        )
        out = repo.root / "monitor.html"
        result = repo.run(
            "report",
            "--html",
            str(out),
            "--lead-time-fixture",
            str(FIXTURE),
        )
        self.assertEqual(0, result.returncode, output(result))
        html = out.read_text(encoding="utf-8")
        self.assertIn("6300", html)
        self.assertNotIn("prompt", html.lower())
        # Fixture must not leak raw forge noise into attributes we care about
        self.assertNotIn("SECRET", html)

    def test_report_html_refuses_to_overwrite_outside_repo_without_path(self):
        repo = Repo(
            self,
            """
            [project]
            name = "demo"
            policy = "POLICY.md"
            """,
        )
        result = repo.run("report", "--html")
        self.assertNotEqual(0, result.returncode, output(result))


if __name__ == "__main__":
    unittest.main()
