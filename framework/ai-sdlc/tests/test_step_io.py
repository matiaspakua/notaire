"""A step records what it received and what it produced (#2435)."""
from __future__ import annotations

import json
import unittest

from helpers import Repo, output

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""

INPUTS = {
    "request": "Fix the pages metrics",
    "prompt": "Write the failing tests first.",
    "files": ["workspace/ci/generate.sh", "docs/README.md"],
    "links": [{"label": "Issue #760", "url": "https://github.com/org/repo/issues/760"}],
}
OUTPUTS = {
    "summary": "8 failing tests",
    "commits": ["0123abcd", "89ef4567aa"],
    "files": ["workspace/tests/test_generate.py"],
    "links": [{"label": "PR", "url": "https://github.com/org/repo/pull/1"}],
}


class StepIoTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        steps = [
            ("catalog", "add", "--kind", "agents", "--id", "implementer", "--ref", ".aisdlc/agents/x", "--summary", "x"),
            ("workflow", "create", "--id", "feature", "--summary", "v1", "--agent", "implementer"),
            ("migrate", "--json"),
            ("workflow", "transition", "--id", "feature", "--action", "define"),
            ("workflow", "transition", "--id", "feature", "--action", "request_validation", "--waive-reason", "unit"),
            ("workflow", "transition", "--id", "feature", "--action", "approve", "--owner-override", "--reason", "unit"),
            ("project", "create", "--id", "web", "--repo-id", "web", "--url", "https://example.invalid/web",
             "--forge", "github"),
            ("run", "start", "--project", "web", "--workflow", "feature", "--id", "r1"),
            ("harness", "report", "--run", "r1", "--event", "run.started"),
        ]
        for args in steps:
            result = self.repo.run(*args)
            self.assertEqual(0, result.returncode, output(result))

    def report(self, event: str, io: dict, *extra: str):
        self.repo.write("io.json", json.dumps(io))
        return self.repo.run("harness", "report", "--run", "r1", "--event", event, "--step", "red",
                             "--io-file", "io.json", *extra)

    def step(self) -> dict:
        shown = json.loads(self.repo.run("run", "show", "--id", "r1", "--json").stdout)
        return next(s for s in shown["steps"] if s["id"] == "red")

    def events(self) -> int:
        return len(json.loads(self.repo.run("run", "events", "--id", "r1", "--json").stdout)["events"])

    def test_the_step_keeps_its_inputs_and_outputs(self):
        started = self.report("step.started", {"inputs": INPUTS})
        self.assertEqual(0, started.returncode, output(started))
        finished = self.report("step.finished", {"outputs": OUTPUTS}, "--step-status", "pass")
        self.assertEqual(0, finished.returncode, output(finished))
        step = self.step()
        self.assertEqual("pass", step["status"])
        self.assertEqual(INPUTS, step["inputs"])
        self.assertEqual(OUTPUTS, step["outputs"])

    def test_a_step_without_io_has_no_fields(self):
        plain = self.repo.run("harness", "report", "--run", "r1", "--event", "step.started", "--step", "red")
        self.assertEqual(0, plain.returncode, output(plain))
        self.assertNotIn("inputs", self.step())

    def assert_refused(self, io: dict, needle: str):
        before = self.events()
        refused = self.report("step.started", io)
        self.assertEqual(2, refused.returncode, output(refused))
        self.assertIn("invalid_body", output(refused))
        self.assertIn(needle, output(refused))
        self.assertEqual(before, self.events())

    def test_a_script_link_is_refused(self):
        self.assert_refused({"inputs": {"links": [{"label": "x", "url": "javascript:alert(1)"}]}}, "http")

    def test_an_absolute_or_escaping_path_is_refused(self):
        self.assert_refused({"inputs": {"files": ["/etc/passwd"]}}, "relative")
        self.assert_refused({"outputs": {"files": ["docs/../../secret"]}}, "relative")

    def test_a_non_hex_commit_is_refused(self):
        self.assert_refused({"outputs": {"commits": ["main; rm -rf"]}}, "commit")

    def test_an_unknown_key_is_refused(self):
        self.assert_refused({"inputs": {"stdout": "x"}}, "stdout")
        self.assert_refused({"extra": {}}, "extra")

    def test_an_oversized_prompt_is_refused(self):
        self.assert_refused({"inputs": {"prompt": "x" * 20_001}}, "prompt")


if __name__ == "__main__":
    unittest.main()
