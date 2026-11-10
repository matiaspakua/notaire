"""Grant expire / kill-switch (#2198) — fail closed; never Isolator OS alone."""
from __future__ import annotations

import json
import sys
import unittest
from datetime import datetime, timedelta, timezone

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

BASE = """
[project]
name = "demo"
policy = "POLICY.md"
base = "HEAD~1"

[[gates]]
name = "plan check"
stage = "plan"
run = "true"
"""


class GrantExpireTest(unittest.TestCase):
    def test_grant_expire_fields_parse_and_show(self):
        """#2198: expires_at / ttl_seconds load and surface in grant show JSON."""
        repo = Repo(
            self,
            BASE
            + """
            [[grants]]
            id = "plan-build"
            stage = "plan"
            worktree = "."
            write_set = ["docs/**"]
            required_checks = ["plan check"]
            expires_at = "2099-01-01T00:00:00Z"
            ttl_seconds = 3600
            """,
        )
        (repo.root / "POLICY.md").write_text("# policy\n", encoding="utf-8")
        shown = repo.run("grant", "show", "--id", "plan-build", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        data = json.loads(shown.stdout)
        self.assertEqual("plan-build", data["id"])
        self.assertEqual("2099-01-01T00:00:00Z", data["expires_at"])
        self.assertEqual(3600, data["ttl_seconds"])
        blob = json.dumps(data).lower()
        self.assertNotRegex(blob, r'"sandbox"\s*:\s*true')

    def test_expired_grant_fails_closed(self):
        """#2198: past expires_at → expire evaluation fail closed."""
        from aisdlc.grants import is_expired  # noqa: E402
        from aisdlc.profile import Grant  # noqa: E402

        grant = Grant(
            id="dead",
            worktree=".",
            write_set=("docs/**",),
            expires_at=datetime(2020, 1, 1, tzinfo=timezone.utc),
        )
        self.assertTrue(is_expired(grant, now=datetime(2020, 1, 2, tzinfo=timezone.utc)))

        repo = Repo(
            self,
            BASE
            + """
            [[grants]]
            id = "plan-build"
            worktree = "."
            write_set = ["docs/**"]
            required_checks = ["plan check"]
            expires_at = "2020-01-01T00:00:00Z"
            """,
        )
        repo.write("docs/a.md", "x\n")
        repo.commit("docs: touch")
        result = repo.run("grant", "check", "--base", repo.base, "--json")
        self.assertEqual(1, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertEqual("fail", data["status"])
        self.assertIn("expir", (data.get("detail") or data.get("note") or "").lower())
        self.assertIs(False, data.get("sandbox", False))

    def test_ttl_seconds_past_fails_closed(self):
        """#2198: ttl_seconds past loaded_at → expired fail closed."""
        from aisdlc.grants import is_expired  # noqa: E402
        from aisdlc.profile import Grant  # noqa: E402

        loaded = datetime(2020, 1, 1, 12, 0, 0, tzinfo=timezone.utc)
        grant = Grant(
            id="ttl",
            worktree=".",
            write_set=("docs/**",),
            ttl_seconds=60,
            loaded_at=loaded,
        )
        self.assertTrue(is_expired(grant, now=loaded + timedelta(seconds=61)))
        self.assertFalse(is_expired(grant, now=loaded + timedelta(seconds=30)))


if __name__ == "__main__":
    unittest.main()
