"""Sole-committer dry-run policy check (#2167) — axiom 2, not an OS Isolator."""
from __future__ import annotations

import json
import subprocess
import unittest

from helpers import ENV, Repo, git, output

POLICY = """
[committer]
required = true
runtime_names = ["Runtime Committer"]
runtime_emails = ["runtime@example.com"]
agent_names = ["Cursor Agent", "cursor[bot]"]
agent_emails = ["cursoragent@example.com"]
agent_trailers = ["Made-with: Cursor"]
"""


def _commit_as(repo: Repo, message: str, *, name: str, email: str) -> str:
    git(repo.root, "add", "-A")
    env = {**ENV, "GIT_AUTHOR_NAME": name, "GIT_AUTHOR_EMAIL": email,
           "GIT_COMMITTER_NAME": name, "GIT_COMMITTER_EMAIL": email}
    subprocess.run(
        ["git", "commit", "-q", "--allow-empty", "-m", message],
        cwd=repo.root,
        check=True,
        env=env,
        capture_output=True,
        text=True,
    )
    return git(repo.root, "rev-parse", "HEAD")


class CommitterPolicyTest(unittest.TestCase):
    def test_unconfigured_is_no_evaluable(self):
        repo = Repo(self, "")
        repo.write("src/a.py", "x = 1\n")
        repo.commit("feat: a")
        result = repo.run("committer", "--base", repo.base, "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("no-evaluable", data["status"])
        self.assertNotIn("sandbox", (result.stdout + result.stderr).lower())
        self.assertNotIn("isolated", (result.stdout + result.stderr).lower())

    def test_agent_author_fails_when_required(self):
        repo = Repo(self, POLICY)
        repo.write("src/a.py", "x = 1\n")
        _commit_as(repo, "feat: agent change", name="Cursor Agent", email="cursoragent@example.com")
        result = repo.run("committer", "--base", repo.base, "--json")
        self.assertEqual(1, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("fail", data["status"])
        self.assertTrue(data.get("violations"))
        self.assertNotIn("sandbox", json.dumps(data).lower())

    def test_advisory_mode_exits_zero_on_fail(self):
        body = POLICY.replace("required = true", "required = false")
        repo = Repo(self, body)
        repo.write("src/a.py", "x = 1\n")
        _commit_as(repo, "feat: agent change", name="Cursor Agent", email="cursoragent@example.com")
        result = repo.run("committer", "--base", repo.base, "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("fail", data["status"])
        self.assertFalse(data.get("required"))

    def test_runtime_allowed_author_passes(self):
        repo = Repo(self, POLICY)
        repo.write("src/a.py", "x = 1\n")
        _commit_as(repo, "feat: runtime change", name="Runtime Committer", email="runtime@example.com")
        result = repo.run("committer", "--base", repo.base, "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("pass", data["status"])
        self.assertEqual([], data.get("violations") or [])

    def test_agent_trailer_is_detected(self):
        repo = Repo(self, POLICY)
        repo.write("src/a.py", "x = 1\n")
        message = "feat: with trailer\n\nMade-with: Cursor\n"
        _commit_as(repo, message, name="Human", email="human@example.com")
        result = repo.run("committer", "--base", repo.base, "--json")
        self.assertEqual(1, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("fail", data["status"])
        self.assertTrue(any("trailer" in str(v).lower() or "Made-with" in str(v) for v in data["violations"]))


if __name__ == "__main__":
    unittest.main()
