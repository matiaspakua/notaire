"""Failing CLI tests for the intent entry point (#2395).

Black-box tests using helpers.Repo and BIN, same style as test_intents.py.
Expected to fail with unknown command since cli.py has no submit/list/show yet.
"""
from __future__ import annotations

import json
import sys
import textwrap
import unittest

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""

SPEC_FIXTURE = textwrap.dedent("""\
    # Add rate limiting to factory API

    The factory API currently has no rate limiting.

    ## Acceptance criteria

    - [ ] New endpoint returns 429 after 60 requests in a minute
    - [ ] Rate limit header is included in every response

    ## References

    - [ADR-030 hexagonal](docs/200-architecture/202-ADR/ADR-030.md) (architecture)
    - [Threat model](https://example.org/tm) (security)
""")


class IntentCLITest(unittest.TestCase):
    """CLI submit, list, show — and regression for old intent start/end."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)

    # --- Scenario: CLI submit text, ref, or file ---
    def test_cli_submit_text(self):
        result = self.repo.run("intent", "submit", "--text", "Do something", "--json")
        self.assertEqual(0, result.returncode, output(result))
        intent = json.loads(result.stdout)
        self.assertEqual("prompt", intent["kind"])
        self.assertTrue(intent["id"].startswith("int-"))

    def test_cli_submit_ref(self):
        result = self.repo.run("intent", "submit", "--ref", "github:#12", "--json")
        self.assertEqual(0, result.returncode, output(result))
        intent = json.loads(result.stdout)
        self.assertEqual("reference", intent["kind"])
        self.assertEqual("github", intent["source"]["system"])

    def test_cli_submit_file(self):
        spec_path = self.repo.root / "spec.md"
        spec_path.write_text(SPEC_FIXTURE, encoding="utf-8")
        result = self.repo.run("intent", "submit", "--file", str(spec_path), "--json")
        self.assertEqual(0, result.returncode, output(result))
        intent = json.loads(result.stdout)
        self.assertEqual("spec", intent["kind"])
        self.assertEqual("Add rate limiting to factory API", intent["title"])
        self.assertGreater(len(intent["acceptance"]), 0)

    # --- Scenario: CLI list intents ---
    def test_cli_list(self):
        self.repo.run("intent", "submit", "--text", "First")
        self.repo.run("intent", "submit", "--text", "Second")
        result = self.repo.run("intent", "list", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        entries = data.get("entries", data) if isinstance(data, dict) else data
        self.assertGreaterEqual(len(entries), 2)

    # --- Scenario: CLI show intent by id ---
    def test_cli_show(self):
        create = self.repo.run("intent", "submit", "--text", "Show me", "--json")
        self.assertEqual(0, create.returncode, output(create))
        intent_id = json.loads(create.stdout)["id"]

        result = self.repo.run("intent", "show", intent_id, "--json")
        self.assertEqual(0, result.returncode, output(result))
        shown = json.loads(result.stdout)
        self.assertEqual(intent_id, shown["id"])
        self.assertEqual("Show me", shown["title"])

    # --- Regression: old intent start/end still works ---
    def test_old_intent_start_end_still_works(self):
        started = self.repo.run(
            "intent", "start", "--id", "implement", "--type", "implement",
            "--issue", "12", "--harness", "cursor", "--provider", "cursor",
            "--model", "sonnet", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["run_id"]

        ended = self.repo.run(
            "intent", "end", "--run", run_id, "--outcome", "success", "--json",
        )
        self.assertEqual(0, ended.returncode, output(ended))
        body = json.loads(ended.stdout)
        self.assertEqual("success", body["outcome"])


if __name__ == "__main__":
    unittest.main()
