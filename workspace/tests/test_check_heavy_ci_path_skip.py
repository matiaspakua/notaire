#!/usr/bin/env python3
"""Guards #1257/#1197: check-heavy-ci accepts gh pr checks status skipping."""
from __future__ import annotations

import os
import re
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SCRIPT = os.path.join(REPO_ROOT, "workspace", "sdlc", "check-heavy-ci.sh")


class CheckHeavyCiPathSkipTest(unittest.TestCase):
    def test_accepts_gh_skipping_status(self):
        """gh pr checks prints skipping (not only skip/skipped) for path-filtered jobs."""
        with open(SCRIPT, encoding="utf-8") as fh:
            text = fh.read()
        # Case arm must include skipping so docs-only PRs are mergeable.
        self.assertRegex(
            text,
            r"skip\|skipped\|skipping",
            "check-heavy-ci.sh must treat gh status skipping as path-scoped success",
        )


if __name__ == "__main__":
    unittest.main()
