"""Agent provider selection: Anthropic when it can authenticate, otherwise Cursor."""
from __future__ import annotations

import json
import os
import stat
import sys
import unittest

from helpers import BIN, ENV, KIT, Repo, output

sys.path.insert(0, str(KIT))
from aisdlc.agent_provider import build_argv, load_settings, select_provider, usage_from_stream  # noqa: E402


def which_of(found: dict[str, str]):
    def which(name: str) -> str | None:
        return found.get(name)

    return which


class AgentProviderTest(unittest.TestCase):
    def test_anthropic_wins_when_the_api_key_and_cli_exist(self):
        settings = load_settings(None, {})
        selection = select_provider(
            settings,
            {"ANTHROPIC_API_KEY": "present", "CURSOR_API_KEY": "also"},
            which=which_of({"claude": "/bin/claude", "agent": "/bin/agent"}),
            run_probe=lambda _argv: 1,
        )
        self.assertEqual("anthropic", selection.selected)
        self.assertEqual("preferred", selection.via)
        self.assertEqual("claude", selection.command)
        self.assertEqual("env:ANTHROPIC_API_KEY", selection.statuses[0].auth)

    def test_cursor_is_the_fallback_when_anthropic_cannot_authenticate(self):
        settings = load_settings(None, {})
        selection = select_provider(
            settings,
            {"CURSOR_API_KEY": "present"},
            which=which_of({"agent": "/bin/agent"}),
            run_probe=lambda _argv: 1,
        )
        self.assertEqual("cursor", selection.selected)
        self.assertEqual("fallback", selection.via)
        self.assertIn("claude", selection.statuses[0].reason)
        self.assertIn("ANTHROPIC_API_KEY", selection.statuses[0].reason)

    def test_missing_both_names_the_cli_and_the_env_var(self):
        settings = load_settings(None, {})
        selection = select_provider(settings, {}, which=which_of({}), run_probe=lambda _argv: 1)
        self.assertIsNone(selection.selected)
        text = selection.reason
        self.assertIn("claude", text)
        self.assertIn("ANTHROPIC_API_KEY", text)
        self.assertIn("agent", text)
        self.assertIn("cursor-agent", text)
        self.assertIn("CURSOR_API_KEY", text)

    def test_forced_provider_does_not_fall_back(self):
        settings = load_settings(None, {}, provider="cursor")
        selection = select_provider(
            settings,
            {"ANTHROPIC_API_KEY": "present"},
            which=which_of({"claude": "/bin/claude"}),
            run_probe=lambda _argv: 0,
        )
        self.assertIsNone(selection.selected)
        self.assertEqual("forced", selection.via)
        self.assertIn("CURSOR_API_KEY", selection.reason)

    def test_order_flag_can_prefer_cursor(self):
        settings = load_settings(None, {}, order="cursor,anthropic")
        selection = select_provider(
            settings,
            {"ANTHROPIC_API_KEY": "a", "CURSOR_API_KEY": "c"},
            which=which_of({"claude": "/bin/claude", "agent": "/bin/agent"}),
        )
        self.assertEqual("cursor", selection.selected)
        self.assertEqual("preferred", selection.via)

    def test_login_probe_counts_when_no_api_key_is_set(self):
        settings = load_settings(None, {})
        seen: list[list[str]] = []

        def run_probe(argv: list[str]) -> int:
            seen.append(argv)
            return 0 if argv[0] == "claude" else 1

        selection = select_provider(
            settings, {}, which=which_of({"claude": "/bin/claude", "agent": "/bin/agent"}), run_probe=run_probe
        )
        self.assertEqual("anthropic", selection.selected)
        self.assertEqual("login", selection.statuses[0].auth)
        self.assertEqual(["claude", "auth", "status"], seen[0])

    def test_failed_login_probe_is_unavailable(self):
        settings = load_settings(None, {})
        selection = select_provider(
            settings, {}, which=which_of({"claude": "/bin/claude"}), run_probe=lambda _argv: 1
        )
        self.assertFalse(selection.statuses[0].available)
        self.assertIn("exited 1", selection.statuses[0].reason)

    def test_cursor_argv_keeps_the_prompt_last_and_mcp_on_the_graph_arm(self):
        baseline = build_argv("cursor", "agent", "where is login", model="composer", arm="baseline")
        graph = build_argv("cursor", "agent", "where is login", model="composer", arm="graph", mcp=__import__("pathlib").Path("mcp.json"))
        self.assertEqual("where is login", baseline[-1])
        self.assertNotIn("--mcp-config", baseline)
        self.assertIn("--mcp-config", graph)
        self.assertEqual("mcp.json", graph[graph.index("--mcp-config") + 1])
        self.assertIn("--mode", baseline)
        self.assertIn("ask", baseline)

    def test_anthropic_argv_matches_the_headless_claude_session(self):
        argv = build_argv("anthropic", "claude", "find the port", model="sonnet", budget=0.5)
        self.assertEqual(["claude", "-p", "find the port"], argv[:3])
        self.assertIn("--output-format", argv)
        self.assertIn("stream-json", argv)
        self.assertIn("--allowedTools", argv)

    def test_usage_counts_cache_tokens(self):
        stream = json.dumps({
            "type": "result",
            "usage": {"input_tokens": 3, "cache_read_input_tokens": 4, "output_tokens": 2},
        })
        self.assertEqual({"input_tokens": 7, "output_tokens": 2}, usage_from_stream(stream))

    def test_cli_dry_run_uses_cursor_when_anthropic_is_absent(self):
        repo = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\n')
        bindir = repo.root / "bin"
        bindir.mkdir()
        agent = bindir / "agent"
        agent.write_text("#!/bin/sh\necho should-not-run\n", encoding="utf-8")
        agent.chmod(agent.stat().st_mode | stat.S_IEXEC)
        env = {key: value for key, value in ENV.items() if key not in {"ANTHROPIC_API_KEY", "CURSOR_API_KEY"}}
        env["PATH"] = str(bindir)
        env["CURSOR_API_KEY"] = "present"
        import subprocess

        result = subprocess.run(
            [sys.executable, str(BIN), "--root", str(repo.root),
             "agent", "run", "--provider", "cursor", "--prompt", "locate login", "--dry-run", "--json"],
            cwd=repo.root, env=env, capture_output=True, text=True,
        )
        self.assertEqual(0, result.returncode, output(result))
        payload = json.loads(result.stdout)
        self.assertEqual("cursor", payload["provider"])
        self.assertIs(False, payload["executes"])
        self.assertEqual("locate login", payload["argv"][-1])
        self.assertNotIn("should-not-run", result.stdout)

    def test_cli_probe_exits_2_and_names_the_missing_credentials(self):
        repo = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\n')
        env = {key: value for key, value in os.environ.items() if not key.startswith("GIT_")}
        env.pop("ANTHROPIC_API_KEY", None)
        env.pop("CURSOR_API_KEY", None)
        env["PATH"] = str(repo.root / "empty-path")
        (repo.root / "empty-path").mkdir()
        import subprocess

        result = subprocess.run(
            [sys.executable, str(BIN), "--root", str(repo.root), "agent", "probe", "--json"],
            cwd=repo.root, env=env, capture_output=True, text=True,
        )
        self.assertEqual(2, result.returncode, output(result))
        payload = json.loads(result.stdout)
        self.assertIsNone(payload["selected"])
        reasons = " ".join(item["reason"] for item in payload["providers"])
        self.assertIn("ANTHROPIC_API_KEY", reasons)
        self.assertIn("CURSOR_API_KEY", reasons)


if __name__ == "__main__":
    unittest.main()
