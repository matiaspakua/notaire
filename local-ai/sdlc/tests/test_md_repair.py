import unittest

import helpers  # noqa: F401  (puts bin/ on sys.path)
import md_repair


class FenceTest(unittest.TestCase):
    def test_bare_opening_fence_gets_text(self):
        self.assertEqual(md_repair.repair("```\nIssue → PR\n```\n"), "```text\nIssue → PR\n```\n")

    def test_fence_with_language_unchanged(self):
        text = "```bash\nls\n```\n\n```\nx\n```\n"
        self.assertEqual(md_repair.repair(text), "```bash\nls\n```\n\n```text\nx\n```\n")


class TablePipeTest(unittest.TestCase):
    def test_row_without_trailing_pipe_gets_it(self):
        self.assertEqual(md_repair.repair("| Field | Value |\n|---|---|\n| Use Case | CU76\n"),
                         "| Field | Value |\n|---|---|\n| Use Case | CU76 |\n")

    def test_pipe_inside_code_block_unchanged(self):
        text = "```text\n| not a table\n```\n"
        self.assertEqual(md_repair.repair(text), text)


if __name__ == "__main__":
    unittest.main()
