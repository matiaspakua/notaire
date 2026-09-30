import unittest

import helpers  # noqa: F401  (puts bin/ on sys.path)
import ledger

BASE = """## 4. Implementación

- [x] 4.1 write the fix
- [ ] 4.2 wire it

## 8. Gate 3

- [ ] 8.1 update AI-SDLC.md
- [ ] 8.2 CHANGELOG.md: n/a
"""

TRACE = """| Link | Reference | Status |
|------|-----------|--------|
| Tasks | `tasks.md` | done |
| Commits | pending | pending |
| Pull Request | pending | pending |
"""


class RestoreTicksTest(unittest.TestCase):
    def test_tick_on_base_item_is_kept(self):
        new = BASE.replace("- [ ] 8.1", "- [x] 8.1")
        self.assertEqual(ledger.restore_ticks(BASE, new), new)

    def test_added_item_is_dropped(self):
        new = BASE.replace("- [ ] 4.2 wire it\n", "- [x] 4.2 wire it\n- [x] 4.3 extra step\n")
        self.assertEqual(ledger.restore_ticks(BASE, new), BASE.replace("- [ ] 4.2", "- [x] 4.2"))

    def test_not_applicable_mark_is_dropped(self):
        new = BASE.replace("- [ ] 8.2 CHANGELOG.md: n/a", "- [n/a] 8.2 CHANGELOG.md skipped")
        self.assertEqual(ledger.restore_ticks(BASE, new), BASE)

    def test_base_tick_survives_an_untick(self):
        new = BASE.replace("- [x] 4.1", "- [ ] 4.1")
        self.assertEqual(ledger.restore_ticks(BASE, new), BASE)


class MissingRowsTest(unittest.TestCase):
    def test_rows_present(self):
        self.assertEqual(ledger.missing_rows(TRACE, ["Commits", "Pull Request"]), [])

    def test_missing_row_is_named(self):
        text = TRACE.replace("| Commits | pending | pending |\n", "")
        self.assertEqual(ledger.missing_rows(text, ["Commits", "Pull Request"]), ["Commits"])

    def test_duplicated_row_is_named(self):
        text = TRACE + "| Pull Request | #1 | open |\n"
        self.assertEqual(ledger.missing_rows(text, ["Commits", "Pull Request"]), ["Pull Request"])


class UntickAfterTest(unittest.TestCase):
    def test_premature_tick_removed(self):
        text, unticked = ledger.untick_after(BASE, 2)
        self.assertIn("- [ ] 4.1 write the fix", text)
        self.assertEqual(unticked, ["4.1"])

    def test_prerequisite_tick_kept(self):
        tasks = "## 1. Gate 1\n\n- [x] 1.1 GitHub Issue exists\n"
        self.assertEqual(ledger.untick_after(tasks, 2), (tasks, []))


if __name__ == "__main__":
    unittest.main()
