"""Intent runs: record, toggle knowledge, and compare without inventing numbers."""
from __future__ import annotations

import json
import os
import stat
import subprocess
import sys
import unittest

from helpers import BIN, ENV, KIT, Repo, output

sys.path.insert(0, str(KIT))

from aisdlc.intents import end_intent, read_events, start_intent  # noqa: E402

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[knowledge]
provider = "repolens"
command = "missing-knowledge-cli"

[cost.prices.sonnet]
input = 3.0
output = 15.0
"""


class IntentRecordTest(unittest.TestCase):
    def test_start_end_and_tool_counts_are_append_only(self):
        repo = Repo(self, PROFILE)
        started = repo.run(
            "intent", "start", "--id", "implement", "--type", "implement",
            "--issue", "12", "--harness", "cursor", "--provider", "cursor", "--model", "sonnet", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["run_id"]
        tool = repo.run("intent", "tool", "--run", run_id, "--kind", "rg", "--json")
        self.assertEqual(0, tool.returncode, output(tool))
        ended = repo.run(
            "intent", "end", "--run", run_id, "--outcome", "success",
            "--input-tokens", "1000", "--output-tokens", "100", "--json",
        )
        self.assertEqual(0, ended.returncode, output(ended))
        body = json.loads(ended.stdout)
        self.assertEqual("success", body["outcome"])
        self.assertEqual("repolens", body["knowledge"])
        self.assertIsNone(body["knowledge_version"])
        self.assertEqual(1, body["tool_calls"]["by_kind"]["rg"])
        self.assertEqual(0.0045, body["cost_usd"])
        self.assertGreaterEqual(body["duration_s"], 0)
        log = (repo.root / ".aisdlc" / "metrics" / "intents.jsonl").read_text(encoding="utf-8").splitlines()
        self.assertEqual(3, len(log))

    def test_knowledge_none_skips_the_command_and_is_recorded(self):
        repo = Repo(self, PROFILE)
        script = repo.root / "bin"
        script.mkdir()
        tool = script / "missing-knowledge-cli"
        tool.write_text("#!/bin/sh\necho should-not-run\n", encoding="utf-8")
        tool.chmod(tool.stat().st_mode | stat.S_IEXEC)
        env = dict(ENV)
        env["PATH"] = str(script) + os.pathsep + env.get("PATH", "")
        env["AISDLC_KNOWLEDGE"] = "none"
        # Point the profile command at the script so a real call would print.
        repo.write("aisdlc.toml", PROFILE.replace("missing-knowledge-cli", "missing-knowledge-cli"))
        result = subprocess.run(
            [sys.executable, str(BIN), "--root", str(repo.root), "knowledge", "search", "where"],
            cwd=repo.root, env=env, capture_output=True, text=True,
        )
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("knowledge disabled", result.stdout)
        self.assertNotIn("should-not-run", result.stdout)
        env["AISDLC_INTENTS_LOG"] = str(repo.root / "intents.jsonl")
        os.environ["AISDLC_KNOWLEDGE"] = "none"
        os.environ["AISDLC_INTENTS_LOG"] = env["AISDLC_INTENTS_LOG"]
        try:
            from aisdlc.profile import load_profile
            from aisdlc.context import Context

            profile = load_profile(repo.root / "aisdlc.toml")
            ctx = Context(root=repo.root, profile=profile, profile_path=repo.root / "aisdlc.toml")
            started = start_intent(ctx, intent_id="implement", intent_type="implement", env=env)
            self.assertEqual("none", started["knowledge"])
            self.assertIsNone(started["knowledge_version"])
        finally:
            os.environ.pop("AISDLC_KNOWLEDGE", None)
            os.environ.pop("AISDLC_INTENTS_LOG", None)

    def test_cost_stays_null_without_tokens_or_a_reported_cost(self):
        repo = Repo(self, PROFILE)
        from aisdlc.context import Context
        from aisdlc.profile import load_profile

        profile = load_profile(repo.root / "aisdlc.toml")
        ctx = Context(root=repo.root, profile=profile, profile_path=repo.root / "aisdlc.toml")
        started = start_intent(ctx, intent_id="review", intent_type="review", model="sonnet")
        ended = end_intent(ctx, started["run_id"], outcome="fail")
        self.assertIsNone(ended["cost_usd"])
        self.assertIsNone(ended["input_tokens"])

    def test_compare_warns_until_each_side_has_two_runs(self):
        repo = Repo(self, PROFILE)
        from aisdlc.context import Context
        from aisdlc.profile import load_profile

        profile = load_profile(repo.root / "aisdlc.toml")
        ctx = Context(root=repo.root, profile=profile, profile_path=repo.root / "aisdlc.toml")
        for backend in ("repolens", "none"):
            env = {"AISDLC_KNOWLEDGE": backend, "AISDLC_INTENTS_LOG": str(repo.root / ".aisdlc/metrics/intents.jsonl")}
            started = start_intent(ctx, intent_id="implement", intent_type="implement", env=env)
            end_intent(ctx, started["run_id"], outcome="success", env=env)
        listed = repo.run("metrics", "intents", "compare", "--a", "knowledge=repolens", "--b", "knowledge=none", "--json")
        self.assertEqual(0, listed.returncode, output(listed))
        data = json.loads(listed.stdout)
        self.assertTrue(any("insufficient sample" in line for line in data["warnings"]))
        for _ in range(1):
            for backend in ("repolens", "none"):
                env = {"AISDLC_KNOWLEDGE": backend}
                started = start_intent(ctx, intent_id="implement", intent_type="implement", env=env)
                end_intent(ctx, started["run_id"], outcome="success", input_tokens=10, output_tokens=1, env=env)
        compared = repo.run("metrics", "intents", "compare", "--a", "knowledge=repolens", "--b", "knowledge=none", "--json")
        body = json.loads(compared.stdout)
        self.assertEqual(2, body["runs_a"])
        self.assertEqual(2, body["runs_b"])
        self.assertIsNotNone(body["metrics"]["duration_s"]["delta_median_b_minus_a"])
        self.assertTrue(all(not item.get("example") for item in read_events(repo.root / ".aisdlc/metrics/intents.jsonl")))

    def test_example_file_is_labelled_and_not_loaded_as_a_measurement(self):
        example = (KIT / "schemas" / "intent-run.example.jsonl").read_text(encoding="utf-8")
        row = json.loads(example)
        self.assertTrue(row["example"])
        self.assertIn("EXAMPLE ONLY", row["note"])
        self.assertEqual([], read_events(KIT / "schemas" / "intent-run.example.jsonl"))

    def test_intent_run_records_the_child_exit_code(self):
        repo = Repo(self, PROFILE)
        failed = repo.run("intent", "run", "--id", "bench", "--type", "bench", "--", sys.executable, "-c", "import sys; sys.exit(3)")
        self.assertEqual(3, failed.returncode, output(failed))
        events = read_events(repo.root / ".aisdlc" / "metrics" / "intents.jsonl")
        ends = [item for item in events if item["event"] == "end"]
        self.assertEqual("fail", ends[-1]["outcome"])


if __name__ == "__main__":
    unittest.main()
