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
        from aisdlc.status import status

        from aisdlc import evidence

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
        from aisdlc.status import status

        from aisdlc import evidence

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
        from aisdlc.status import status

        from aisdlc import evidence
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
        from aisdlc.status import status

        from aisdlc import evidence

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

    def test_next_steps_from_workflow(self) -> None:
        """Next steps are derived from the workflow body."""
        from aisdlc.status import status

        started = self.repo.run(
            "run", "start", "--project", "alpha",
            "--workflow", "feature-delivery", "--json",
        )
        self.assertEqual(0, started.returncode, output(started))

        ctx = self._ctx()
        result = status(ctx)
        # Next should contain workflow steps or profile stages
        self.assertIsInstance(result["next"], list)

    def test_run_selection_prefers_not_finished(self) -> None:
        """Status picks a not-finished run over a later finished one."""
        from aisdlc.status import status

        from aisdlc import evidence

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
        from aisdlc.status import status

        from aisdlc import evidence

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


if __name__ == "__main__":
    unittest.main()
