"""Failing tests for live factory status (aisdlc.status module) — #2397.

These tests must fail with ImportError until aisdlc.status exists.
"""
from __future__ import annotations

import json
import sys
import unittest

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"

[[gates]]
stage = "pre-push"
name = "lint"
run = "true"

[[gates]]
stage = "pre-merge"
name = "test"
run = "true"

[[harness.adapters]]
id = "local-worker"
runtime = "local"
source = ".aisdlc/harness/local-worker.yml"
"""


class StatusEmptyTest(unittest.TestCase):
    """status(ctx) returns the empty shape when there are no runs."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        for kind, cid in (("agents", "implementer"), ("skills", "java")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def test_empty_shape_has_no_runs(self) -> None:
        from aisdlc.status import status

        ctx = self._ctx()
        result = status(ctx)
        self.assertIsNone(result["intent"])
        self.assertIsNone(result["run"])
        self.assertIsNone(result["workflow"])
        self.assertIsNone(result["run_status"])
        self.assertIsNone(result["stage"])
        self.assertIsNone(result["step"])
        self.assertEqual([], result["previous"])
        self.assertEqual([], result["next"])
        self.assertIsNone(result["updated_at"])


class StatusWithRunTest(unittest.TestCase):
    """status(ctx) derives from ledger evidence, not invented state."""

    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        for kind, cid in (("agents", "implementer"), ("skills", "java")):
            self.repo.run(
                "catalog", "add", "--kind", kind, "--id", cid,
                "--ref", f".aisdlc/{kind}/{cid}", "--summary", cid,
            )
        self.repo.run(
            "project", "create", "--id", "alpha",
            "--profile-ref", "aisdlc.toml",
            "--repo-id", "notaire",
            "--forge", "github",
            "--url", "https://github.com/example/notaire",
            "--default-branch", "main",
            "--json",
        )
        self.repo.run(
            "workflow", "create", "--id", "feature-delivery",
            "--summary", "Feature delivery",
            "--agent", "implementer", "--skill", "java",
            "--json",
        )
        for action, extra in (
            ("define", []),
            ("request_validation", ["--waive-reason", "unit"]),
            ("approve", ["--owner-override", "--reason", "unit"]),
        ):
            result = self.repo.run(
                "workflow", "transition", "--id", "feature-delivery",
                "--action", action, *extra, "--json",
            )
            self.assertEqual(0, result.returncode, output(result))

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def test_status_after_run_start_shows_queued(self) -> None:
        from aisdlc.status import status

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run = json.loads(started.stdout)
        run_id = run["id"]

        ctx = self._ctx()
        result = status(ctx)
        self.assertEqual(run_id, result["run"])
        self.assertIn(result["run_status"], ("requested", "queued"))
        self.assertIsNotNone(result["workflow"])
        self.assertEqual("feature-delivery", result["workflow"]["id"])

    def test_intent_propagation_from_run_request(self) -> None:
        """A run.request ledger row with 'intent' key propagates to status."""
        from aisdlc import evidence
        from aisdlc.status import status

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run = json.loads(started.stdout)
        run_id = run["id"]

        # Append a run.request with intent key to the ledger
        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "run.request",
            "run_id": run_id,
            "intent": "int-1",
            "ts": "2026-11-22T10:00:00+00:00",
        })

        result = status(ctx)
        self.assertEqual("int-1", result["intent"])

    def test_previous_steps_with_duration(self) -> None:
        """Completed steps appear in 'previous' with correct duration_s."""
        from aisdlc import evidence
        from aisdlc.status import status

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        ctx = self._ctx()
        # Append step.started and step.finished with explicit timestamps
        evidence.append_record(ctx, {
            "kind": "step.started",
            "run_id": run_id,
            "step": "implement",
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.finished",
            "run_id": run_id,
            "step": "implement",
            "step_status": "pass",
            "ts": "2026-11-22T10:00:42+00:00",
        })

        result = status(ctx)
        prev = result["previous"]
        self.assertEqual(1, len(prev))
        self.assertEqual("implement", prev[0]["step"])
        self.assertEqual("pass", prev[0]["status"])
        self.assertEqual(42.0, prev[0]["duration_s"])
        self.assertEqual("2026-11-22T10:00:00+00:00", prev[0]["started_at"])
        self.assertEqual("2026-11-22T10:00:42+00:00", prev[0]["finished_at"])

    def test_previous_step_usd_from_telemetry_store(self) -> None:
        """A step's usd is pulled from telemetry store when available."""
        from aisdlc import evidence
        from aisdlc.status import status
        from aisdlc.telemetry_store import record as record_telemetry

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "step.started",
            "run_id": run_id,
            "step": "implement",
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.finished",
            "run_id": run_id,
            "step": "implement",
            "step_status": "pass",
            "ts": "2026-11-22T10:00:42+00:00",
        })
        # Record USD telemetry for this step
        record_telemetry(
            ctx, run_id=run_id, step="implement",
            metric="usd", value=0.25,
        )

        result = status(ctx)
        prev = result["previous"]
        self.assertEqual(1, len(prev))
        self.assertEqual(0.25, prev[0]["usd"])

    def test_previous_step_usd_none_without_telemetry(self) -> None:
        """A step with no telemetry usd rows has usd=None, not 0."""
        from aisdlc import evidence
        from aisdlc.status import status

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "step.started",
            "run_id": run_id,
            "step": "implement",
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.finished",
            "run_id": run_id,
            "step": "implement",
            "step_status": "pass",
            "ts": "2026-11-22T10:00:42+00:00",
        })

        result = status(ctx)
        prev = result["previous"]
        self.assertEqual(1, len(prev))
        self.assertIsNone(prev[0]["usd"])

    def test_in_flight_step_shows_running(self) -> None:
        """A step.started with no step.finished yields run_status=='running' and step non-null."""
        from aisdlc import evidence
        from aisdlc.status import status

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "run.started",
            "run_id": run_id,
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.started",
            "run_id": run_id,
            "step": "implement",
            "ts": "2026-11-22T10:01:00+00:00",
        })

        result = status(ctx)
        self.assertEqual("running", result["run_status"])
        self.assertEqual("implement", result["step"])
        self.assertEqual([], result["previous"])

    def test_stage_from_step_ledger_event(self) -> None:
        """When the step's ledger event carries a 'stage' key, status['stage'] reflects it."""
        from aisdlc import evidence
        from aisdlc.status import status

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "run.started",
            "run_id": run_id,
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.started",
            "run_id": run_id,
            "step": "lint-check",
            "stage": "pre-push",
            "ts": "2026-11-22T10:01:00+00:00",
        })

        result = status(ctx)
        self.assertEqual("pre-push", result["stage"])
        self.assertEqual("lint-check", result["step"])

    def test_queued_run_never_invents_running(self) -> None:
        """A queued run (only run.request) has step=None and stage=None."""
        from aisdlc.status import status

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))

        ctx = self._ctx()
        result = status(ctx)
        self.assertIsNone(result["step"])
        self.assertIsNone(result["stage"])

    def test_next_from_workflow_and_line(self) -> None:
        """next items: workflow steps first (source='workflow'), then profile stages (source='line')."""
        from aisdlc import catalogs, evidence, workflows
        from aisdlc.status import status

        # Create a v2 workflow body with multiple steps directly
        ctx = self._ctx()
        wf = workflows.show(ctx, "feature-delivery")
        # Build a multi-step v2 body
        multi_body = {
            "steps": [
                {"id": "plan", "stage": "plan", "agent": None, "skills": [],
                 "mcp": [], "provider": None, "gates": [],
                 "budget": {"tokens": None, "time_seconds": None, "usd": None},
                 "on_fail": "stop"},
                {"id": "implement", "stage": "implement", "agent": None, "skills": [],
                 "mcp": [], "provider": None, "gates": [],
                 "budget": {"tokens": None, "time_seconds": None, "usd": None},
                 "on_fail": "stop"},
                {"id": "test", "stage": "green", "agent": None, "skills": [],
                 "mcp": [], "provider": None, "gates": [],
                 "budget": {"tokens": None, "time_seconds": None, "usd": None},
                 "on_fail": "stop"},
            ],
            "inputs": [],
            "outputs": [],
        }
        # Update the workflow to v2 with multi-step body
        payload = dict(wf)
        payload["body"] = multi_body
        payload["schema_version"] = 2
        payload["governance"] = "draft"
        catalogs.write_new_revision(
            ctx, "workflows", "feature-delivery", payload,
            actor="test", schema_version=2,
        )
        # Re-approve the updated workflow
        for action, extra in (
            ("define", []),
            ("request_validation", ["--waive-reason", "unit"]),
            ("approve", ["--owner-override", "--reason", "unit"]),
        ):
            result = self.repo.run(
                "workflow", "transition", "--id", "feature-delivery",
                "--action", action, *extra, "--json",
            )
            self.assertEqual(0, result.returncode, output(result))

        # Start a run
        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        # Finish the first workflow step ("plan")
        evidence.append_record(ctx, {
            "kind": "step.started",
            "run_id": run_id,
            "step": "plan",
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.finished",
            "run_id": run_id,
            "step": "plan",
            "step_status": "pass",
            "ts": "2026-11-22T10:00:30+00:00",
        })

        result = status(ctx)
        # "plan" should be in previous
        prev_ids = [p["step"] for p in result["previous"]]
        self.assertIn("plan", prev_ids)

        # "implement" and "test" should be in next with source="workflow"
        next_items = result["next"]
        workflow_next = [n for n in next_items if n.get("source") == "workflow"]
        line_next = [n for n in next_items if n.get("source") == "line"]

        # Workflow steps come before line items
        wf_ids = [n["step"] for n in workflow_next]
        self.assertIn("implement", wf_ids)
        self.assertIn("test", wf_ids)
        # "plan" should NOT be in next
        self.assertNotIn("plan", wf_ids)

        # Line items should be profile stages (pre-push, pre-merge)
        # that are not already named as workflow steps
        line_ids = [n["step"] for n in line_next]
        for lid in line_ids:
            self.assertIn(lid, ctx.profile.stages())
            self.assertNotIn(lid, wf_ids)

    def test_usd_summed_across_multiple_telemetry_rows(self) -> None:
        """Two usd rows (0.25 + 0.5) for the same run/step sum to 0.75."""
        from aisdlc import evidence
        from aisdlc.status import status
        from aisdlc.telemetry_store import record as record_telemetry

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "step.started",
            "run_id": run_id,
            "step": "implement",
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.finished",
            "run_id": run_id,
            "step": "implement",
            "step_status": "pass",
            "ts": "2026-11-22T10:00:42+00:00",
        })
        # Two USD telemetry rows for the same run/step
        record_telemetry(ctx, run_id=run_id, step="implement",
                         metric="usd", value=0.25)
        record_telemetry(ctx, run_id=run_id, step="implement",
                         metric="usd", value=0.5)

        result = status(ctx)
        prev = result["previous"]
        self.assertEqual(1, len(prev))
        self.assertAlmostEqual(0.75, prev[0]["usd"], places=4)

    def test_usd_not_mixed_with_another_run(self) -> None:
        """USD from another run's telemetry is not included."""
        from aisdlc import evidence
        from aisdlc.status import status
        from aisdlc.telemetry_store import record as record_telemetry

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "step.started",
            "run_id": run_id,
            "step": "implement",
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.finished",
            "run_id": run_id,
            "step": "implement",
            "step_status": "pass",
            "ts": "2026-11-22T10:00:42+00:00",
        })
        # Record USD for a different run
        record_telemetry(ctx, run_id="other-run", step="implement",
                         metric="usd", value=99.99)

        result = status(ctx)
        prev = result["previous"]
        self.assertEqual(1, len(prev))
        # Should be None (no telemetry for this run), not 99.99
        self.assertIsNone(prev[0]["usd"])

    def test_run_selection_prefers_not_finished(self) -> None:
        """Status picks a not-finished run over a later finished one."""
        from aisdlc import evidence
        from aisdlc.status import status

        # Create first run (queued)
        started1 = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started1.returncode, output(started1))
        run_id1 = json.loads(started1.stdout)["id"]

        # Create second run and finish it
        started2 = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started2.returncode, output(started2))
        run_id2 = json.loads(started2.stdout)["id"]

        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "run.started",
            "run_id": run_id2,
            "ts": "2026-11-22T11:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "run.finished",
            "run_id": run_id2,
            "ok": True,
            "ts": "2026-11-22T11:01:00+00:00",
        })

        result = status(ctx)
        # Should pick run1 (not finished) over run2 (finished)
        self.assertEqual(run_id1, result["run"])

    def test_skip_is_never_pass(self) -> None:
        """step_status skip must not become pass in previous."""
        from aisdlc import evidence
        from aisdlc.status import status

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "step.started",
            "run_id": run_id,
            "step": "test",
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.finished",
            "run_id": run_id,
            "step": "test",
            "step_status": "skip",
            "ts": "2026-11-22T10:00:05+00:00",
        })

        result = status(ctx)
        prev = result["previous"]
        self.assertEqual(1, len(prev))
        self.assertEqual("skip", prev[0]["status"])
        self.assertNotEqual("pass", prev[0]["status"])

    def test_finished_run_step_and_stage_are_none(self) -> None:
        """A finished run with a dangling step.started shows step=None, stage=None."""
        from aisdlc import evidence
        from aisdlc.status import status

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "run.started",
            "run_id": run_id,
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.started",
            "run_id": run_id,
            "step": "implement",
            "ts": "2026-11-22T10:01:00+00:00",
        })
        # Run finishes without step.finished for implement
        evidence.append_record(ctx, {
            "kind": "run.finished",
            "run_id": run_id,
            "ok": True,
            "ts": "2026-11-22T10:02:00+00:00",
        })

        result = status(ctx)
        self.assertIsNone(result["step"])
        self.assertIsNone(result["stage"])

    def test_blocked_run_step_and_stage_are_none(self) -> None:
        """A blocked run shows step=None, stage=None even with step.started."""
        from aisdlc import evidence
        from aisdlc.status import status

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))
        run_id = json.loads(started.stdout)["id"]

        ctx = self._ctx()
        evidence.append_record(ctx, {
            "kind": "run.started",
            "run_id": run_id,
            "ts": "2026-11-22T10:00:00+00:00",
        })
        evidence.append_record(ctx, {
            "kind": "step.started",
            "run_id": run_id,
            "step": "implement",
            "ts": "2026-11-22T10:01:00+00:00",
        })
        # Budget overrun blocks the run
        evidence.append_record(ctx, {
            "kind": "step.finished",
            "run_id": run_id,
            "step": "implement",
            "step_status": "pass",
            "budget_overrun": True,
            "ts": "2026-11-22T10:02:00+00:00",
        })

        result = status(ctx)
        self.assertEqual("blocked", result["run_status"])
        self.assertIsNone(result["step"])
        self.assertIsNone(result["stage"])


if __name__ == "__main__":
    unittest.main()
