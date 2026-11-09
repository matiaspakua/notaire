"""Memory catalog pack ids in [context] ↔ aisdlc budget (#2186)."""
from __future__ import annotations

import json
import sys
import unittest

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

BASE = """
[project]
name = "demo"
policy = "POLICY.md"

[[gates]]
name = "push check"
stage = "pre-push"
run = "true"

[catalogs]
dir = ".aisdlc/catalogs"
"""


class MemoryContextBudgetTest(unittest.TestCase):
    def test_omitted_memory_is_noop(self):
        repo = Repo(
            self,
            BASE
            + """
            [context]
            max_tokens = 1000
            files = ["AGENTS.md"]
            """,
        )
        repo.write("AGENTS.md", "hello\n")
        validate = repo.run("config", "validate")
        self.assertEqual(0, validate.returncode, output(validate))
        doctor = repo.run("doctor")
        self.assertEqual(0, doctor.returncode, output(doctor))

    def test_unknown_memory_pack_fails_validate(self):
        repo = Repo(
            self,
            BASE
            + """
            [context]
            max_tokens = 1000
            memory = ["missing-pack"]
            """,
        )
        result = repo.run("config", "validate")
        self.assertNotEqual(0, result.returncode, output(result))
        self.assertIn("missing-pack", output(result))

    def test_unknown_memory_pack_fails_doctor(self):
        repo = Repo(
            self,
            BASE
            + """
            [context]
            max_tokens = 1000
            memory = ["ghost"]
            """,
        )
        result = repo.run("doctor")
        self.assertNotEqual(0, result.returncode, output(result))
        text = output(result)
        self.assertIn("ghost", text)
        self.assertTrue("ERROR" in text or "error" in text.lower())

    def test_missing_catalogs_dir_with_memory_fails(self):
        repo = Repo(
            self,
            """
            [project]
            name = "demo"
            policy = "POLICY.md"

            [[gates]]
            name = "push check"
            stage = "pre-push"
            run = "true"

            [context]
            max_tokens = 1000
            memory = ["agent-card"]
            """,
        )
        result = repo.run("config", "validate")
        self.assertNotEqual(0, result.returncode, output(result))
        self.assertTrue(
            "catalog" in output(result).lower() or "agent-card" in output(result),
            output(result),
        )

    def test_budget_via_memory_pack_only(self):
        repo = Repo(
            self,
            BASE
            + """
            [context]
            max_tokens = 1000
            memory = ["agent-card"]
            """,
        )
        repo.write("CONSTITUTION-AGENT-CARD.md", "x" * 100)
        add = repo.run(
            "catalog",
            "add",
            "--kind",
            "memory",
            "--id",
            "agent-card",
            "--ref",
            "CONSTITUTION-AGENT-CARD.md",
            "--summary",
            "always-loaded card",
        )
        self.assertEqual(0, add.returncode, output(add))
        validate = repo.run("config", "validate")
        self.assertEqual(0, validate.returncode, output(validate))
        budget = repo.run("budget")
        self.assertEqual(0, budget.returncode, output(budget))
        self.assertIn("CONSTITUTION-AGENT-CARD.md", output(budget))

    def test_budget_includes_pack_ref_not_in_files(self):
        repo = Repo(
            self,
            BASE
            + """
            [context]
            max_tokens = 1000
            files = ["AGENTS.md"]
            memory = ["extra-pack"]
            """,
        )
        repo.write("AGENTS.md", "a" * 40)
        repo.write("MEMORY.md", "b" * 400)
        add = repo.run(
            "catalog",
            "add",
            "--kind",
            "memory",
            "--id",
            "extra-pack",
            "--ref",
            "MEMORY.md",
            "--summary",
            "extra",
        )
        self.assertEqual(0, add.returncode, output(add))
        budget = repo.run("budget")
        self.assertEqual(0, budget.returncode, output(budget))
        text = output(budget)
        self.assertIn("MEMORY.md", text)
        self.assertIn("AGENTS.md", text)
        # 440 bytes → 110 tokens; must reflect both files
        self.assertRegex(text, r"110|tokens")

    def test_missing_pack_ref_fails_budget(self):
        repo = Repo(
            self,
            BASE
            + """
            [context]
            max_tokens = 1000
            memory = ["broken"]
            """,
        )
        add = repo.run(
            "catalog",
            "add",
            "--kind",
            "memory",
            "--id",
            "broken",
            "--ref",
            "DOES-NOT-EXIST.md",
            "--summary",
            "broken ref",
        )
        self.assertEqual(0, add.returncode, output(add))
        budget = repo.run("budget")
        self.assertNotEqual(0, budget.returncode, output(budget))
        self.assertTrue(
            "DOES-NOT-EXIST" in output(budget) or "broken" in output(budget),
            output(budget),
        )

    def test_hostile_pack_summary_never_executes(self):
        repo = Repo(
            self,
            BASE
            + """
            [context]
            max_tokens = 1000
            memory = ["hostile"]
            """,
        )
        marker = repo.root / "PWNED"
        repo.write("SAFE.md", "safe\n")
        add = repo.run(
            "catalog",
            "add",
            "--kind",
            "memory",
            "--id",
            "hostile",
            "--ref",
            "SAFE.md",
            "--summary",
            f"touch {marker}; rm -rf /",
        )
        self.assertEqual(0, add.returncode, output(add))
        for cmd in (
            ("budget",),
            ("config", "validate"),
            ("catalog", "list", "--kind", "memory"),
        ):
            result = repo.run(*cmd)
            self.assertEqual(0, result.returncode, output(result))
        self.assertFalse(marker.exists(), "hostile summary must not execute")

    def test_config_show_includes_context_memory(self):
        repo = Repo(
            self,
            BASE
            + """
            [context]
            max_tokens = 8000
            files = ["AGENTS.md"]
            memory = ["agent-card"]
            """,
        )
        repo.write("AGENTS.md", "hello\n")
        repo.write("CARD.md", "card\n")
        add = repo.run(
            "catalog",
            "add",
            "--kind",
            "memory",
            "--id",
            "agent-card",
            "--ref",
            "CARD.md",
            "--summary",
            "card",
        )
        self.assertEqual(0, add.returncode, output(add))
        shown = repo.run("config", "show", "--redacted", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        view = json.loads(shown.stdout)
        self.assertIn("context", view)
        self.assertEqual(8000, view["context"]["max_tokens"])
        self.assertEqual(["AGENTS.md"], view["context"]["files"])
        self.assertEqual(["agent-card"], view["context"]["memory"])


if __name__ == "__main__":
    unittest.main()
