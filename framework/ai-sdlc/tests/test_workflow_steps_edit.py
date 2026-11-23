"""Edit a workflow's v2 steps with their line stage (#2410)."""
from __future__ import annotations

import json
import sys
import threading
import unittest
import urllib.error
import urllib.request

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"
"""

STEPS = [
    {"id": "spec", "stage": "plan", "agent": "agents:implementer@1", "skills": ["skills:java@1"]},
    {"id": "green", "stage": "pre-push", "agent": "agents:implementer@1"},
]


class WorkflowStepsEditTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for kind, cid in (("agents", "implementer"), ("skills", "java")):
            added = self.repo.run("catalog", "add", "--kind", kind, "--id", cid,
                                  "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid)
            self.assertEqual(0, added.returncode, output(added))
        created = self.repo.run("workflow", "create", "--id", "feature", "--summary", "v1",
                                "--agent", "implementer", "--skill", "java", "--json")
        self.assertEqual(0, created.returncode, output(created))
        self.assertEqual(0, self.repo.run("migrate", "--json").returncode)

    def show(self) -> dict:
        shown = self.repo.run("workflow", "show", "--id", "feature", "--json")
        self.assertEqual(0, shown.returncode, output(shown))
        return json.loads(shown.stdout)

    def edit(self, steps):
        self.repo.write("steps.json", json.dumps(steps))
        return self.repo.run("workflow", "edit", "--id", "feature", "--steps-file", "steps.json", "--json")

    def transition(self, action, *extra):
        result = self.repo.run("workflow", "transition", "--id", "feature", "--action", action, *extra, "--json")
        self.assertEqual(0, result.returncode, output(result))

    def approve(self):
        self.transition("define")
        self.transition("request_validation", "--waive-reason", "unit")
        self.transition("approve", "--owner-override", "--reason", "unit")

    def test_steps_with_stages_are_saved_as_a_draft(self):
        before = self.show()["version"]
        edited = self.edit(STEPS)
        self.assertEqual(0, edited.returncode, output(edited))
        after = self.show()
        self.assertEqual("draft", after["governance"])
        self.assertGreater(after["version"], before)
        steps = after["body"]["steps"]
        self.assertEqual([("spec", "plan"), ("green", "pre-push")], [(s["id"], s["stage"]) for s in steps])
        self.assertEqual("stop", steps[1]["on_fail"])
        self.assertEqual([], steps[1]["skills"])
        self.transition("define")

    def test_a_step_without_a_stage_is_refused(self):
        before = self.show()["version"]
        edited = self.edit([{"id": "spec", "agent": "agents:implementer@1"}])
        self.assertNotEqual(0, edited.returncode)
        self.assertIn("step 'spec' needs a stage", output(edited))
        self.assertEqual(before, self.show()["version"])

    def test_duplicate_step_ids_are_refused(self):
        edited = self.edit([{"id": "red", "stage": "red"}, {"id": "red", "stage": "red"}])
        self.assertNotEqual(0, edited.returncode)
        self.assertIn("repeated", output(edited))

    def test_an_unknown_on_fail_is_refused(self):
        edited = self.edit([{"id": "red", "stage": "red", "on_fail": "retry"}])
        self.assertNotEqual(0, edited.returncode)
        self.assertIn("on_fail must be one of stop, continue", output(edited))

    def test_an_approved_workflow_is_immutable(self):
        self.approve()
        edited = self.edit(STEPS)
        self.assertNotEqual(0, edited.returncode)
        self.assertIn("approved workflows are immutable", output(edited))

    def test_status_reports_the_steps_stage(self):
        self.assertEqual(0, self.edit(STEPS).returncode)
        self.approve()
        project = self.repo.run("project", "create", "--id", "web", "--repo-id", "web",
                                "--url", "https://example.invalid/web", "--forge", "github")
        self.assertEqual(0, project.returncode, output(project))
        started = self.repo.run("run", "start", "--project", "web", "--workflow", "feature", "--id", "r1")
        self.assertEqual(0, started.returncode, output(started))
        for event in (("--event", "run.started"), ("--event", "step.started", "--step", "green")):
            reported = self.repo.run("harness", "report", "--run", "r1", "--adapter", "local-worker", *event)
            self.assertEqual(0, reported.returncode, output(reported))
        status = self.repo.run("status", "--json")
        self.assertEqual(0, status.returncode, output(status))
        self.assertEqual("pre-push", json.loads(status.stdout)["stage"])


class WorkflowStepsApiTest(unittest.TestCase):
    def setUp(self) -> None:
        from aisdlc import serve as serve_mod
        from aisdlc.cli import load_context
        from aisdlc.serve_token import add_token, ensure_token

        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        self.repo.run("catalog", "add", "--kind", "agents", "--id", "implementer",
                      "--ref", ".aisdlc/agents/implementer", "--summary", "x")
        self.repo.run("workflow", "create", "--id", "feature", "--summary", "v1", "--agent", "implementer")
        self.repo.run("migrate", "--json")
        ensure_token(self.repo.root, scope="owner")
        self.writer = add_token(self.repo.root, identity="alice", scope="write")
        self.reader = add_token(self.repo.root, identity="bob", scope="read")
        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        server = serve_mod.start_server(ctx, host="127.0.0.1", port=0, enable_write=True)
        threading.Thread(target=server.serve_forever, daemon=True).start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        self.port = server.server_address[1]

    def put(self, token, if_match, steps):
        req = urllib.request.Request(
            f"http://127.0.0.1:{self.port}/factory/workflows/feature",
            data=json.dumps({"steps": steps}).encode("utf-8"), method="PUT",
            headers={"Authorization": f"Bearer {token.value}", "X-AISDLC-CSRF": token.value,
                     "If-Match": str(if_match), "Content-Type": "application/json"})
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                return resp.status, json.loads(resp.read().decode("utf-8"))
        except urllib.error.HTTPError as exc:
            return exc.code, json.loads(exc.read().decode("utf-8"))

    def version(self) -> int:
        return json.loads(self.repo.run("workflow", "show", "--id", "feature", "--json").stdout)["version"]

    def test_put_saves_the_steps_for_a_writer(self):
        status, body = self.put(self.writer, self.version(), [{"id": "red", "stage": "red"}])
        self.assertEqual(200, status, body)
        self.assertEqual("red", body["body"]["steps"][0]["stage"])

    def test_put_needs_the_current_version(self):
        current = self.version()
        status, problem = self.put(self.writer, current - 1, [{"id": "red", "stage": "red"}])
        self.assertEqual(409, status, problem)
        self.assertEqual("version_conflict", problem["code"])
        self.assertEqual(current, self.version())

    def test_a_reader_cannot_edit_steps(self):
        status, problem = self.put(self.reader, self.version(), [{"id": "red", "stage": "red"}])
        self.assertEqual(403, status)
        self.assertEqual("forbidden", problem["code"])

    def test_put_refuses_a_step_without_a_stage(self):
        status, problem = self.put(self.writer, self.version(), [{"id": "red"}])
        self.assertEqual(400, status, problem)
        self.assertEqual("invalid_body", problem["code"])

    def test_the_contract_lists_the_route(self):
        from aisdlc import contract

        text = contract.load_contract_text(KIT)
        self.assertIn("editWorkflow", text)
        self.assertEqual([], contract.validate_contract(KIT))


if __name__ == "__main__":
    unittest.main()
