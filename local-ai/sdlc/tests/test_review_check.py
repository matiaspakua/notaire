import tempfile
import unittest

import helpers  # noqa: F401
import review_check

NOTE = """## Fix
OLD: x
NEW: y
CHECK: echo 0
EXPECTED: 0
CHECK: echo 2
EXPECTED: 0
CHECK: echo hello
EXPECTED: fails on the 80/65 assertion, not on compile
"""


class ReviewCheckTest(unittest.TestCase):
    def setUp(self):
        self.results = review_check.run_checks(review_check.parse_checks(NOTE), tempfile.mkdtemp())

    def test_parses_every_check_in_order(self):
        self.assertEqual([r.cmd for r in self.results], ["echo 0", "echo 2", "echo hello"])

    def test_literal_expectation_met_passes(self):
        self.assertEqual(self.results[0].verdict, "PASS")

    def test_literal_expectation_missed_fails(self):
        self.assertEqual(self.results[1].verdict, "FAIL")
        self.assertEqual(self.results[1].output, "2")

    def test_free_text_expectation_is_left_to_the_foreman(self):
        self.assertEqual(self.results[2].verdict, "JUDGE")

    def test_exit_code_is_non_zero_when_any_check_fails(self):
        self.assertEqual(review_check.exit_code(self.results), 1)
        self.assertEqual(review_check.exit_code(self.results[:1] + self.results[2:]), 0)


if __name__ == "__main__":
    unittest.main()
