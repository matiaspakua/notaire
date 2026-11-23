"""Time, tokens, money and RepoLens effect per intent and overall (#2447)."""
from __future__ import annotations

import json
import sys
import threading
import time
import unittest
import urllib.request
from datetime import datetime, timezone

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"

[cost.prices.claude-test]
input = 4.0
output = 20.0
cache_read = 0.2
cache_write = 5.0
"""


def _parse(ts: str) -> datetime:
    return datetime.fromisoformat(ts.replace("Z", "+00:00"))


class FactoryMetricsTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for args in (
            ("catalog", "add", "--kind", "agents", "--id", "worker", "--ref", ".aisdlc/agents/w", "--summary", "w"),
            ("workflow", "create", "--id", "feature", "--summary", "v1", "--agent", "worker"),
            ("migrate", "--json"),
            ("workflow", "transition", "--id", "feature", "--action", "define"),
            ("workflow", "transition", "--id", "feature", "--action", "request_validation", "--waive-reason", "u"),
            ("workflow", "transition", "--id", "feature", "--action", "approve", "--owner-override", "--reason", "u"),
            ("project", "create", "--id", "web", "--repo-id", "web", "--url", "https://example.invalid/web",
             "--forge", "github"),
        ):
            result = self.repo.run(*args)
            self.assertEqual(0, result.returncode, output(result))
        submitted = self.repo.run("intent", "submit", "--text", "Speed up the checkout", "--json")
        self.intent = json.loads(submitted.stdout)["id"]
        self.ok("run", "start", "--project", "web", "--workflow", "feature", "--id", "r1", "--intent", self.intent)
        for event in (
            ("--event", "run.started"),
            ("--event", "step.started", "--step", "red"),
            ("--event", "step.finished", "--step", "red", "--step-status", "fail"),
            ("--event", "step.started", "--step", "red"),
            ("--event", "step.finished", "--step", "red", "--step-status", "pass"),
            ("--event", "step.started", "--step", "green"),
            ("--event", "step.finished", "--step", "green", "--step-status", "pass"),
            ("--event", "run.finished", "--ok", "true"),
        ):
            self.ok("harness", "report", "--run", "r1", "--adapter", "local-worker", *event)

    def ok(self, *args: str):
        result = self.repo.run(*args)
        self.assertEqual(0, result.returncode, output(result))
        return result

    def window(self, step: str) -> tuple[datetime, datetime]:
        events = json.loads(self.ok("run", "events", "--id", "r1", "--json").stdout)["events"]
        starts = [_parse(e["ts"]) for e in events if e["kind"] == "step.started" and e.get("step") == step]
        ends = [_parse(e["ts"]) for e in events if e["kind"] == "step.finished" and e.get("step") == step]
        return starts[-1], ends[-1]

    def costs(self) -> list[dict]:
        path = self.repo.root / ".aisdlc" / "evidence" / "cost.jsonl"
        return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]

    def junie_log(self, at: datetime, tokens_in: int, tokens_out: int, cache_read: int, issue: int | None = None) -> None:
        local = at.astimezone()
        name = f"junie/log-{local:%Y-%m-%d_%H%M%S}-pid-{tokens_in}.log"
        snapshot = {"schemaVersion": 1, "totals": {"rowCount": 3, "successCount": 3, "inputTokens": tokens_in,
                    "outputTokens": tokens_out, "cacheReadTokens": cache_read},
                    "attributedGroups": [{"modelName": "Qwen/Test", "provider": "Vllm"}]}
        stamp = f"{local:%Y-%m-%d %H:%M:%S}.000"
        early = {**snapshot, "totals": {**snapshot["totals"], "inputTokens": 1, "outputTokens": 1, "cacheReadTokens": 0}}
        self.repo.write(name, "\n".join([
            f"{stamp} [main] INFO  JunieRunner - Startup: Junie 26.11.2",
            f"{stamp} [main] INFO  Task - You are a Worker implementing GitHub issue #{issue}." if issue else stamp,
            f"{stamp} [main] INFO  Analytics - [ANALYTICS:EVENT] type=llm_cost_trajectory_snapshot properties="
            f"schema_version=1, total_cost=0.0, snapshot={json.dumps(early)}",
            f"{stamp} [main] INFO  Analytics - [ANALYTICS:EVENT] type=llm_cost_trajectory_snapshot properties="
            f"schema_version=1, total_cost=0.0, snapshot={json.dumps(snapshot)}",
        ]) + "\n")

    def transcript(self, at: datetime) -> None:
        usage = {"input_tokens": 10, "output_tokens": 100, "cache_read_input_tokens": 1000,
                 "cache_creation_input_tokens": 200,
                 "cache_creation": {"ephemeral_1h_input_tokens": 200, "ephemeral_5m_input_tokens": 0}}
        line = {"type": "assistant", "timestamp": at.astimezone(timezone.utc).isoformat().replace("+00:00", "Z"),
                "requestId": "req_1", "message": {"id": "msg_1", "model": "claude-test", "usage": usage}}
        outside = {**line, "timestamp": "2020-01-01T00:00:00Z", "requestId": "req_old"}
        synthetic = {**line, "requestId": "req_syn", "message": {"id": "msg_syn", "model": "<synthetic>",
                     "usage": {"input_tokens": 0, "output_tokens": 0}}}
        self.repo.write("claude/session.jsonl",
                        "\n".join(json.dumps(x) for x in (line, line, outside, synthetic)) + "\n")

    def test_a_cost_row_carries_run_step_and_cache_tokens(self):
        self.ok("cost", "--agent", "foreman", "--model", "claude-test", "--input", "1000000", "--output", "0",
                "--cache-read", "1000000", "--run", "r1", "--step", "green", "--source", "manual")
        row = self.costs()[-1]
        self.assertEqual(("r1", "green", 1000000), (row["run"], row["step"], row["cache_read"]))
        self.assertAlmostEqual(4.2, row["usd"])

    def test_an_unpriced_model_stays_unpriced(self):
        self.ok("cost", "--agent", "x", "--model", "unknown-model", "--input", "10", "--output", "10", "--run", "r1")
        self.assertIsNone(self.costs()[-1]["usd"])

    def test_junie_usage_is_attributed_to_its_step_once(self):
        start, _ = self.window("red")
        self.junie_log(start, 1000, 200, 5000)
        imported = self.ok("usage", "import", "--source", "junie", "--path", "junie", "--run", "r1", "--json")
        self.assertEqual(1, json.loads(imported.stdout)["recorded"])
        row = self.costs()[-1]
        self.assertEqual(("r1", "red", "junie", "Qwen/Test"), (row["run"], row["step"], row["source"], row["model"]))
        self.assertEqual((1000, 200, 5000), (row["input"], row["output"], row["cache_read"]))
        self.assertEqual(0.0, row["usd"])
        self.assertEqual("reported", row["usd_source"])
        again = self.ok("usage", "import", "--source", "junie", "--path", "junie", "--run", "r1", "--json")
        self.assertEqual(0, json.loads(again.stdout)["recorded"])

    def test_a_session_naming_another_issue_is_not_attributed(self):
        self.ok("run", "start", "--project", "web", "--workflow", "feature", "--id", "issue-7")
        for event in (("--event", "run.started"), ("--event", "step.started", "--step", "red"),
                      ("--event", "step.finished", "--step", "red", "--step-status", "pass")):
            self.ok("harness", "report", "--run", "issue-7", "--adapter", "local-worker", *event)
        events = json.loads(self.ok("run", "events", "--id", "issue-7", "--json").stdout)["events"]
        start = _parse(next(e["ts"] for e in events if e["kind"] == "step.started"))
        self.junie_log(start, 111, 1, 0, issue=8)
        other = self.ok("usage", "import", "--source", "junie", "--path", "junie", "--run", "issue-7", "--json")
        self.assertEqual(0, json.loads(other.stdout)["recorded"])
        self.junie_log(start, 222, 2, 0, issue=7)
        mine = self.ok("usage", "import", "--source", "junie", "--path", "junie", "--run", "issue-7", "--json")
        self.assertEqual([222], [r["input"] for r in json.loads(mine.stdout)["rows"]])

    def test_claude_code_usage_is_counted_once_per_request_and_priced(self):
        start, end = self.window("green")
        self.transcript(start + (end - start) / 2)
        imported = self.ok("usage", "import", "--source", "claude-code", "--path", "claude", "--run", "r1",
                           "--step", "foreman", "--json")
        self.assertEqual(1, json.loads(imported.stdout)["recorded"])
        row = self.costs()[-1]
        self.assertEqual((10, 100, 1000, 200), (row["input"], row["output"], row["cache_read"], row["cache_write"]))
        # 1h cache writes cost 2x input: 10*4 + 100*20 + 1000*0.2 + 200*8 per million.
        self.assertAlmostEqual((40 + 2000 + 200 + 1600) / 1e6, row["usd"])

    def test_foreman_usage_across_runs_is_split_and_overhead_is_kept(self):
        time.sleep(2.1)  # ledger stamps have second resolution; leave a gap between the runs
        self.ok("run", "start", "--project", "web", "--workflow", "feature", "--id", "r2")
        for event in (("--event", "run.started"), ("--event", "step.started", "--step", "red"),
                      ("--event", "step.finished", "--step", "red", "--step-status", "pass"),
                      ("--event", "run.finished", "--ok", "true")):
            self.ok("harness", "report", "--run", "r2", "--adapter", "local-worker", *event)
        events = {r: json.loads(self.ok("run", "events", "--id", r, "--json").stdout)["events"] for r in ("r1", "r2")}
        span = {r: (_parse(e[0]["ts"]), _parse(e[-1]["ts"])) for r, e in events.items()}
        lines = []
        def message(request: str, at: datetime) -> dict:
            usage = {"input_tokens": 100, "output_tokens": 10, "cache_read_input_tokens": 0,
                     "cache_creation_input_tokens": 0}
            return {"timestamp": at.astimezone(timezone.utc).isoformat().replace("+00:00", "Z"),
                    "requestId": request, "message": {"id": request, "model": "claude-test", "usage": usage}}
        lines.append(message("only-r1", span["r1"][0] + (span["r1"][1] - span["r1"][0]) / 2))
        lines.append(message("between", span["r1"][1] + (span["r2"][0] - span["r1"][1]) / 2))
        self.repo.write("claude/s.jsonl", "\n".join(json.dumps(x) for x in lines) + "\n")
        imported = json.loads(self.ok("usage", "import", "--source", "claude-code", "--path", "claude",
                                      "--all-runs", "--json").stdout)
        by_run = {(r["run"], r["step"]): r for r in imported["rows"]}
        self.assertEqual(100, by_run[("r1", "foreman")]["input"])
        self.assertEqual(100, by_run[(None, "overhead")]["input"])
        report = json.loads(self.ok("metrics", "factory", "--usage", "none.jsonl", "--bench-dir", "none",
                                    "--json").stdout)
        self.assertEqual(100, report["overall"]["overhead"]["tokens"]["input"])
        self.assertEqual(200, report["overall"]["tokens"]["input"])
        again = json.loads(self.ok("usage", "import", "--source", "claude-code", "--path", "claude",
                                   "--all-runs", "--json").stdout)
        self.assertEqual(0, again["recorded"])

    def test_factory_metrics_answer_time_cost_and_repolens_per_intent(self):
        start, end = self.window("red")
        self.junie_log(start, 1000, 200, 5000)
        self.ok("usage", "import", "--source", "junie", "--path", "junie", "--run", "r1")
        self.ok("cost", "--agent", "foreman", "--model", "claude-test", "--input", "1000000", "--output", "0",
                "--run", "r1", "--step", "green")
        mid = (start + (end - start) / 2).timestamp()
        self.repo.write("usage.jsonl", json.dumps({"ts": mid, "client": "mcp", "tool": "search", "ms": 30.0,
                                                   "tokens": 250, "results": 4}) + "\n"
                        + json.dumps({"ts": 1.0, "client": "cli", "tool": "search", "ms": 5, "tokens": 9}) + "\n")
        self.repo.write("bench/agent-test.json", json.dumps({"model": "claude-test", "summary": {
            "baseline": {"recall": 0.9, "tool_calls": 6.0, "duration_s": 10.0, "cost_usd": 0.04,
                         "input_tokens": 40000, "output_tokens": 1000, "runs": 6},
            "graph": {"recall": 0.95, "tool_calls": 3.0, "duration_s": 8.0, "cost_usd": 0.03,
                      "input_tokens": 30000, "output_tokens": 900, "runs": 6}}}))
        self.ok("run", "start", "--project", "web", "--workflow", "feature", "--id", "r2")
        report = json.loads(self.ok("metrics", "factory", "--usage", "usage.jsonl", "--bench-dir", "bench",
                                    "--json").stdout)

        intent = next(i for i in report["intents"] if i["id"] == self.intent)
        self.assertEqual("Speed up the checkout", intent["title"])
        self.assertEqual(["r1"], intent["runs"])
        self.assertGreaterEqual(intent["time"]["wall_s"], intent["time"]["active_s"])
        self.assertEqual({"red", "green"}, set(intent["time"]["by_stage"]))
        self.assertEqual(1, intent["time"]["corrections"])
        self.assertEqual(1001000, intent["tokens"]["input"])
        self.assertEqual(5000, intent["tokens"]["cache_read"])
        self.assertAlmostEqual(4.0, intent["cost"]["usd"])
        self.assertTrue(intent["cost"]["fully_priced"])
        self.assertEqual(1, intent["repolens"]["calls"])
        self.assertEqual(250, intent["repolens"]["tokens_served"])

        r2 = next(r for r in report["runs"] if r["id"] == "r2")
        self.assertEqual(0, r2["tokens"]["input"])
        self.assertIsNone(r2["cost"]["usd"])

        overall = report["overall"]
        self.assertEqual(2, overall["runs"])
        self.assertEqual(1, overall["intents"])
        self.assertAlmostEqual(4.0, overall["cost"]["usd"])
        self.assertIn("median_active_s", overall["per_intent"])

        bench = report["repolens_effect"]["bench"][0]
        self.assertEqual("claude-test", bench["model"])
        self.assertEqual(6, bench["runs_per_arm"])
        self.assertAlmostEqual(-25.0, bench["cost_usd_change_pct"])
        self.assertAlmostEqual(-50.0, bench["tool_calls_change_pct"])
        self.assertAlmostEqual(0.05, bench["recall_change"])
        self.assertIn("live_ab", report["repolens_effect"])


class FactoryMetricsApiTest(unittest.TestCase):
    def test_the_route_serves_the_report_to_a_reader(self):
        from aisdlc import serve as serve_mod
        from aisdlc.cli import load_context
        from aisdlc.serve_token import add_token, ensure_token

        repo = Repo(self, PROFILE)
        repo.write("POLICY.md", "# Policy\n")
        ensure_token(repo.root, scope="owner")
        reader = add_token(repo.root, identity="bob", scope="read")
        ctx = load_context(repo.root, repo.root / "aisdlc.toml")
        server = serve_mod.start_server(ctx, host="127.0.0.1", port=0, enable_write=False)
        threading.Thread(target=server.serve_forever, daemon=True).start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        req = urllib.request.Request(f"http://127.0.0.1:{server.server_address[1]}/factory/metrics/factory",
                                     headers={"Authorization": f"Bearer {reader.value}"})
        with urllib.request.urlopen(req, timeout=5) as resp:
            body = json.loads(resp.read().decode("utf-8"))
        self.assertEqual({"intents", "runs", "overall", "repolens_effect", "generated_at"} - set(body), set())

    def test_the_contract_lists_the_route(self):
        from aisdlc import contract

        self.assertIn("getFactoryMetrics", contract.load_contract_text(KIT))
        self.assertEqual([], contract.validate_contract(KIT))


if __name__ == "__main__":
    unittest.main()
