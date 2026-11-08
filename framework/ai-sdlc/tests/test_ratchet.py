"""Baselines only shrink against the base revision; floors only hold or rise (#1958)."""
from __future__ import annotations

import json
import unittest

from helpers import Repo, output

LINES = """
    [[ratchets]]
    name = "architecture baseline"
    kind = "baseline"
    file = "baseline.txt"
"""
JSON = """
    [[ratchets]]
    name = "lint suppressions"
    kind = "baseline"
    file = "suppressions.json"
"""


def suppressions(count: int) -> str:
    return json.dumps({"src/a.ts": {"no-fetch": {"count": count}}})


class LineBaselineTest(unittest.TestCase):
    def setUp(self):
        self.repo = Repo(self, LINES)
        self.repo.write("baseline.txt", "# known violations\nA -> B\nC -> D\n")
        self.base = self.repo.commit("test: baseline")

    def test_hand_added_line_fails_and_is_printed(self):
        self.repo.write("baseline.txt", "# known violations\nA -> B\nC -> D\nE -> F\n")
        result = self.repo.run("ratchet", "--base", self.base)
        self.assertEqual(1, result.returncode, output(result))
        self.assertIn("E -> F", output(result))

    def test_shrunk_baseline_passes(self):
        self.repo.write("baseline.txt", "# known violations\nA -> B\n")
        self.assertEqual(0, self.repo.run("ratchet", "--base", self.base).returncode)

    def test_comments_blank_lines_and_order_are_ignored(self):
        self.repo.write("baseline.txt", "C -> D\n\n# reordered\nA -> B\n")
        self.assertEqual(0, self.repo.run("ratchet", "--base", self.base).returncode)

    def test_swapping_an_entry_still_fails(self):
        self.repo.write("baseline.txt", "A -> B\nX -> Y\n")
        self.assertEqual(1, self.repo.run("ratchet", "--base", self.base).returncode)

    def test_missing_base_ref_is_a_configuration_error(self):
        result = self.repo.run("ratchet", "--base", "no-such-ref")
        self.assertEqual(2, result.returncode)
        self.assertIn("no-such-ref", output(result))

    def test_baseline_absent_at_base_is_reported_as_new(self):
        repo = Repo(self, LINES)
        repo.write("baseline.txt", "A -> B\n")
        result = repo.run("ratchet", "--base", repo.base)
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("new", output(result).lower())


class JsonBaselineTest(unittest.TestCase):
    def setUp(self):
        self.repo = Repo(self, JSON)
        self.repo.write("suppressions.json", suppressions(3))
        self.base = self.repo.commit("test: suppressions")

    def test_growing_count_fails(self):
        self.repo.write("suppressions.json", suppressions(4))
        result = self.repo.run("ratchet", "--base", self.base)
        self.assertEqual(1, result.returncode, output(result))
        self.assertIn("no-fetch", output(result))

    def test_new_key_fails(self):
        data = json.loads(suppressions(3))
        data["src/b.ts"] = {"no-fetch": {"count": 1}}
        self.repo.write("suppressions.json", json.dumps(data))
        self.assertEqual(1, self.repo.run("ratchet", "--base", self.base).returncode)

    def test_shrinking_count_and_empty_object_pass(self):
        self.repo.write("suppressions.json", suppressions(1))
        self.assertEqual(0, self.repo.run("ratchet", "--base", self.base).returncode)
        self.repo.write("suppressions.json", "{}")
        self.assertEqual(0, self.repo.run("ratchet", "--base", self.base).returncode)


class FloorTest(unittest.TestCase):
    def floor(self, value: str, floor: float, direction: str = "min") -> int:
        repo = Repo(self, f"""
            [[ratchets]]
            name = "line coverage"
            kind = "floor"
            run = "echo 'coverage: {value}%'"
            floor = {floor}
            direction = "{direction}"
        """)
        return repo.run("ratchet").returncode

    def test_value_below_floor_fails(self):
        self.assertEqual(1, self.floor("79.9", 80.0))

    def test_value_at_or_above_floor_passes(self):
        self.assertEqual(0, self.floor("80.0", 80.0))
        self.assertEqual(0, self.floor("91.5", 80.0))

    def test_max_direction_is_a_ceiling(self):
        self.assertEqual(0, self.floor("3", 5, "max"))
        self.assertEqual(1, self.floor("6", 5, "max"))

    def test_command_without_a_number_fails(self):
        repo = Repo(self, """
            [[ratchets]]
            name = "broken"
            kind = "floor"
            run = "echo nothing"
            floor = 1
        """)
        self.assertEqual(1, repo.run("ratchet").returncode)


class RatchetSelectionTest(unittest.TestCase):
    def test_name_selects_one_ratchet(self):
        repo = Repo(self, """
            [[ratchets]]
            name = "ok"
            kind = "floor"
            run = "echo 9"
            floor = 1

            [[ratchets]]
            name = "bad"
            kind = "floor"
            run = "echo 0"
            floor = 1
        """)
        self.assertEqual(0, repo.run("ratchet", "--name", "ok").returncode)
        self.assertEqual(1, repo.run("ratchet").returncode)


if __name__ == "__main__":
    unittest.main()
