import os
import re
import tempfile
import unittest

import helpers  # noqa: F401  (puts bin/ on sys.path)
import scope

TRIAGE = """## Files to Edit

- backend-api/pom.xml
- `.claude/rules/code-quality.md`

## Risks
"""

TRACEABILITY = """## Planned Files

| File | Purpose | Action |
|------|---------|--------|
| `backend-api/pom.xml` | JaCoCo configuration | raise floor |
| `CONSTITUTION.md` | Documented floor | update cell |
| `FloorTest.java` | Consistency test | add tests |

## Gate log
"""


def _worktree():
    root = tempfile.mkdtemp(prefix="aisdlc-scope-")
    for path in ("backend-api/pom.xml", ".claude/rules/code-quality.md", "CONSTITUTION.md", "README.md"):
        full = os.path.join(root, path)
        os.makedirs(os.path.dirname(full), exist_ok=True)
        open(full, "w").close()
    return root


class ImplementScopeTest(unittest.TestCase):
    def setUp(self):
        self.regex = re.compile(scope.implement_scope(
            _worktree(), "raise-floor-1", "^(backend-api|frontend)/", TRIAGE, TRACEABILITY))

    def test_planned_file_outside_source_roots_is_allowed(self):
        self.assertTrue(self.regex.search("CONSTITUTION.md"))
        self.assertTrue(self.regex.search(".claude/rules/code-quality.md"))

    def test_unplanned_file_is_not_allowed(self):
        self.assertFalse(self.regex.search("README.md"))

    def test_planned_path_is_exact_not_a_prefix(self):
        self.assertFalse(self.regex.search("CONSTITUTION.md.bak"))
        self.assertFalse(self.regex.search(".claude/rules/code-quality.mdx"))

    def test_non_path_cell_adds_nothing(self):
        self.assertFalse(self.regex.search("FloorTest.java"))

    def test_existing_scope_is_kept(self):
        for path in (".localai/1/tests.env", "openspec/changes/raise-floor-1/tasks.md", "frontend/src/a.tsx"):
            self.assertTrue(self.regex.search(path), path)
        self.assertFalse(self.regex.search("openspec/changes/other-2/tasks.md"))


if __name__ == "__main__":
    unittest.main()
