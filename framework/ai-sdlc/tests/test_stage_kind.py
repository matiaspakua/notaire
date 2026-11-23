"""Tests for stage kind in the production line (#2411).

Gate 2: red — these tests MUST fail because `kind` is not yet part of
`StageProfile`, `stage_dicts()`, or `build_line()`.

Design: docs/openspec/changes/feat-2411-stage-kind/design.md
Delta spec: docs/openspec/changes/feat-2411-stage-kind/specs/aisdlc-production-line/spec.md
"""
from __future__ import annotations

import sys
import unittest

from helpers import KIT, Repo

sys.path.insert(0, str(KIT))

# Minimal profile with [[stages]] blocks and [[gates]].
KIND_PROFILE = """
[project]
name = "kind-demo"
policy = "POLICY.md"

[[stages]]
id = "plan"
kind = "agent"
agents = ["planner"]

[[stages]]
id = "red"
agents = ["tester"]

[[stages]]
id = "pre-push"

[[gates]]
name = "plan gate"
stage = "plan"
run = "true"

[[gates]]
name = "red gate"
stage = "red"
run = "false"
expect = "fail"

[[gates]]
name = "pre-push gate"
stage = "pre-push"
run = "true"

[[gates]]
name = "merge gate"
stage = "merge"
run = "true"
"""


class TestDeclaredKind(unittest.TestCase):
    """Stage with explicit kind returns that kind."""

    def setUp(self) -> None:
        self.repo = Repo(self, KIND_PROFILE)

    def test_declared_kind_is_returned_in_line(self) -> None:
        from aisdlc.cli import load_context
        from aisdlc.line import build_line

        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        plan = stages_by_id["plan"]
        self.assertEqual("agent", plan["kind"])


class TestDefaultKind(unittest.TestCase):
    """Default kind heuristic when kind is not declared."""

    def setUp(self) -> None:
        self.repo = Repo(self, KIND_PROFILE)

    def test_default_kind_agent_when_agents_present(self) -> None:
        """Stage with agents but no kind → 'agent'."""
        from aisdlc.cli import load_context
        from aisdlc.line import build_line

        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        red = stages_by_id["red"]
        self.assertEqual("agent", red["kind"])

    def test_default_kind_human_for_merge(self) -> None:
        """Stage id 'merge' with no [[stages]] block → 'human'."""
        from aisdlc.cli import load_context
        from aisdlc.line import build_line

        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        merge = stages_by_id["merge"]
        self.assertEqual("human", merge["kind"])

    def test_default_kind_deterministic_for_other_stages(self) -> None:
        """Stage with no agents and not 'merge' → 'deterministic'."""
        from aisdlc.cli import load_context
        from aisdlc.line import build_line

        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        prepush = stages_by_id["pre-push"]
        self.assertEqual("deterministic", prepush["kind"])


class TestInvalidKind(unittest.TestCase):
    """Unknown kind value is a profile error."""

    def setUp(self) -> None:
        bad_profile = """
[project]
name = "bad-kind"
policy = "POLICY.md"

[[stages]]
id = "plan"
kind = "robot"

[[gates]]
name = "plan gate"
stage = "plan"
run = "true"
"""
        self.repo = Repo(self, bad_profile)

    def test_invalid_kind_raises_profile_error(self) -> None:
        from aisdlc.profile import ProfileError, load_profile

        with self.assertRaises(ProfileError) as cm:
            load_profile(self.repo.root / "aisdlc.toml")
        self.assertIn("robot", str(cm.exception))


class TestStageDictsKind(unittest.TestCase):
    """stage_dicts() includes kind."""

    def setUp(self) -> None:
        self.repo = Repo(self, KIND_PROFILE)

    def test_stage_dicts_includes_kind(self) -> None:
        from aisdlc.cli import load_context
        from aisdlc.stages import stage_dicts

        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        rows = stage_dicts(ctx.profile)
        rows_by_id = {r["id"]: r for r in rows}
        plan = rows_by_id["plan"]
        self.assertEqual("agent", plan["kind"])


class TestGateOnlyStageKind(unittest.TestCase):
    """Gate-only stage (no [[stages]] block) gets deterministic default."""

    def setUp(self) -> None:
        profile = """
[project]
name = "gate-only"
policy = "POLICY.md"

[[gates]]
name = "signal gate"
stage = "signal"
run = "true"
"""
        self.repo = Repo(self, profile)

    def test_gate_only_stage_gets_deterministic(self) -> None:
        from aisdlc.cli import load_context
        from aisdlc.line import build_line

        ctx = load_context(self.repo.root, self.repo.root / "aisdlc.toml")
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        signal = stages_by_id["signal"]
        self.assertEqual("deterministic", signal["kind"])


if __name__ == "__main__":
    unittest.main()
