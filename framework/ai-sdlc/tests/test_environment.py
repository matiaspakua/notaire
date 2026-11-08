"""Git hooks export GIT_DIR. The kit must still read the repository at --root, never the hook's (#1958).

Found while adopting the kit: a pre-push hook in a linked worktree exported GIT_DIR, a test's
`git init` under it rewrote the shared config to `core.bare = true`, and the user's checkout broke.
"""
from __future__ import annotations

import os
import subprocess
import sys
import unittest

from helpers import BIN, Repo, output


class InheritedGitEnvironmentTest(unittest.TestCase):
    def test_stray_git_dir_does_not_redirect_the_kit(self):
        repo = Repo(self, "")
        repo.commit("feat: something")
        other = Repo(self, "")
        env = {**os.environ, "GIT_DIR": str(other.root / ".git"), "GIT_WORK_TREE": str(other.root)}
        result = subprocess.run(
            [sys.executable, str(BIN), "--root", str(repo.root), "commits", "--base", repo.base],
            cwd=repo.root, env=env, capture_output=True, text=True,
        )
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("1 checked", result.stdout)


if __name__ == "__main__":
    unittest.main()
