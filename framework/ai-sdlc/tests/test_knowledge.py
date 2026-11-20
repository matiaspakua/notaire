"""RepoLens is the default code-knowledge CLI. The kit does not import it."""
from __future__ import annotations

import json
import os
import stat
import subprocess
import sys
import unittest

from helpers import BIN, ENV, KIT, Repo, output

sys.path.insert(0, str(KIT))
from aisdlc.profile import load_profile  # noqa: E402


class KnowledgeDefaultTest(unittest.TestCase):
    def test_profile_defaults_to_repolens_without_a_knowledge_table(self):
        profile = load_profile(text='[project]\nname = "demo"\n')
        self.assertEqual("repolens", profile.knowledge.provider)
        self.assertEqual("repolens", profile.knowledge.command)
        self.assertEqual("search", profile.knowledge.default_tool)
        self.assertEqual("impact", profile.knowledge.callers_tool)
        self.assertEqual("miss", profile.knowledge.fallback_tool)

    def test_dry_run_prints_the_configured_command_and_does_not_execute(self):
        repo = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\n')
        result = repo.run("knowledge", "search", "--dry-run", "where is login")
        self.assertEqual(0, result.returncode, output(result))
        payload = json.loads(result.stdout)
        self.assertEqual(["repolens", "search", "where is login"], payload["argv"])
        self.assertEqual("repolens", payload["provider"])
        self.assertIs(False, payload["executes"])
        self.assertFalse((repo.root / ".aisdlc" / "evidence" / "knowledge.jsonl").exists())

    def test_a_real_call_records_latency_without_the_query(self):
        repo = Repo(
            self,
            """
            [project]
            name = "demo"
            policy = "POLICY.md"
            [knowledge]
            provider = "repolens"
            command = "tool"
            """,
        )
        bindir = repo.root / "bin"
        bindir.mkdir()
        tool = bindir / "tool"
        tool.write_text("#!/bin/sh\necho found\n", encoding="utf-8")
        tool.chmod(tool.stat().st_mode | stat.S_IEXEC)
        env = dict(ENV)
        env["PATH"] = str(bindir) + os.pathsep + env.get("PATH", "")
        result = subprocess.run(
            [sys.executable, str(BIN), "--root", str(repo.root), "knowledge", "search", "secret-query-token"],
            cwd=repo.root, env=env, capture_output=True, text=True,
        )
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("found", result.stdout)
        log = (repo.root / ".aisdlc" / "evidence" / "knowledge.jsonl").read_text(encoding="utf-8")
        self.assertNotIn("secret-query-token", log)
        event = json.loads(log)
        self.assertEqual("search", event["tool"])
        self.assertEqual("repolens", event["provider"])
        self.assertIn("latency_ms", event)
        self.assertGreaterEqual(event["output_bytes"], 5)
        self.assertEqual(0, event["exit_code"])

    def test_foreman_plan_names_the_knowledge_provider_without_executing(self):
        repo = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\n')
        result = repo.run("foreman", "plan", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("repolens", data["knowledge"]["provider"])
        self.assertIs(False, data["knowledge"]["executes"])
        self.assertIs(False, data["executes"])


if __name__ == "__main__":
    unittest.main()
