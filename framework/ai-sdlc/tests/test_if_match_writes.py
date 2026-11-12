"""If-Match expected-version writes + server-derived created_by (#2254)."""
from __future__ import annotations

import json
import os
import sys
import unittest

from helpers import ENV, KIT, Repo, output

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""


class IfMatchWritesTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        add = self.repo.run(
            "catalog", "add", "--kind", "skills", "--id", "demo-skill",
            "--ref", "skills/demo", "--summary", "v1",
        )
        self.assertEqual(0, add.returncode, output(add))

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def test_if_match_mismatch_yields_conflict(self):
        conflict = self.repo.run(
            "catalog", "update", "--kind", "skills", "--id", "demo-skill",
            "--summary", "stale", "--if-match", "99",
        )
        self.assertNotEqual(0, conflict.returncode)
        self.assertIn("version_conflict", conflict.stderr + conflict.stdout)

        ok = self.repo.run(
            "catalog", "update", "--kind", "skills", "--id", "demo-skill",
            "--summary", "v2", "--if-match", "1",
        )
        self.assertEqual(0, ok.returncode, output(ok))
        shown = self.repo.run("catalog", "show", "--kind", "skills", "--id", "demo-skill", "--json")
        self.assertEqual(2, json.loads(shown.stdout)["version"])

    def test_two_writers_cannot_lose_an_update(self):
        from aisdlc import catalogs
        from aisdlc.errors import KitError

        ctx = self._ctx()
        first = catalogs.load_entry(ctx, "skills", "demo-skill")
        second = catalogs.load_entry(ctx, "skills", "demo-skill")
        self.assertEqual(1, first["version"])
        self.assertEqual(1, second["version"])

        payload = dict(first)
        payload["summary"] = "writer-a"
        catalogs.write_new_revision(
            ctx, "skills", "demo-skill", payload, expected_version=1,
        )

        payload_b = dict(second)
        payload_b["summary"] = "writer-b"
        with self.assertRaises(KitError) as raised:
            catalogs.write_new_revision(
                ctx, "skills", "demo-skill", payload_b, expected_version=1,
            )
        self.assertEqual("version_conflict", raised.exception.code)
        current = catalogs.load_entry(ctx, "skills", "demo-skill")
        self.assertEqual(2, current["version"])
        self.assertEqual("writer-a", current["summary"])

    def test_created_by_is_server_derived(self):
        env = dict(ENV)
        env["AISDLC_ACTOR"] = "matias"
        import subprocess

        result = subprocess.run(
            [
                sys.executable, str(KIT / "bin" / "aisdlc"),
                "--root", str(self.repo.root),
                "catalog", "update", "--kind", "skills", "--id", "demo-skill",
                "--summary", "actor-check", "--if-match", "1",
            ],
            cwd=self.repo.root,
            env=env,
            capture_output=True,
            text=True,
        )
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        shown = self.repo.run("catalog", "show", "--kind", "skills", "--id", "demo-skill", "--json")
        payload = json.loads(shown.stdout)
        self.assertEqual("cli:matias", payload["created_by"])

        from aisdlc import catalogs

        ctx = self._ctx()
        forged = catalogs.load_entry(ctx, "skills", "demo-skill")
        forged["summary"] = "forged"
        forged["created_by"] = "hacker:nope"
        written = catalogs.write_new_revision(
            ctx,
            "skills",
            "demo-skill",
            forged,
            expected_version=2,
            actor="cli:local",
        )
        self.assertEqual("cli:local", written["created_by"])
        self.assertNotEqual("hacker:nope", written["created_by"])


if __name__ == "__main__":
    unittest.main()
