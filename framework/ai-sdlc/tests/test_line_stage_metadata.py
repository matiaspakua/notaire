"""Failing tests for stage metadata in the production line (#2404).

Gate 2: red — these tests MUST fail because build_line() currently returns
stages with only id, gates, inputs, outputs, io_source. The missing keys are:
agents, skills, mcp, tools, model, effort, guardrails.

Design decisions: docs/openspec/changes/fix-2404-line-stage-metadata/design.md
Delta spec:      docs/openspec/changes/fix-2404-line-stage-metadata/specs/aisdlc-production-line/spec.md
"""
from __future__ import annotations

import json
import sys
import threading
import unittest
import urllib.error
import urllib.request

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))

# ---------------------------------------------------------------------------
# Test profile mirroring the Notaire .aisdlc/aisdlc.toml shape:
#   - Four [[stages]] blocks: plan, red, pre-push, pre-pr
#   - Each with agents, skills, mcp, model, effort
#   - [[gates]] for plan, red, pre-push, pre-pr
#   - Gate-only stages: signal (2 gates), merge (2 gates) — no [[stages]] block
#   - [[grants]] for plan and pre-push
#   - [catalogs] dir and [skills] dir so validation passes
# ---------------------------------------------------------------------------

STAGE_PROFILE = """
[project]
name = "line-metadata-demo"
policy = "POLICY.md"

[skills]
dir = "skills"

[catalogs]
dir = ".aisdlc/catalogs"

# ── Stages with full metadata ──────────────────────────────────────────────

[[stages]]
id = "plan"
agents = ["openspec-planner"]
skills = ["openspec-propose", "openspec-update-change"]
mcp = ["repolens"]
model = "claude-sonnet-5-5-high"
effort = { tokens = 200000, time_seconds = 3600, questions = 20, review_lines = 400 }
inputs = ["GitHub issue", "use case"]
outputs = ["OpenSpec change"]

[[stages]]
id = "red"
agents = ["testing-qa", "backend-implementer", "frontend-design"]
skills = ["test-master", "qa-automation-strategy"]
mcp = ["repolens"]
model = "claude-sonnet-5-5-medium"
effort = { tokens = 200000, time_seconds = 3600, questions = 15, review_lines = 400 }
inputs = ["OpenSpec change"]
outputs = ["failing tests"]

[[stages]]
id = "pre-push"
agents = ["cloud-foreman", "devops-engineer"]
skills = ["ai-agent-workflow", "ci-cd-quality-gates"]
mcp = ["repolens"]
model = "claude-sonnet-5-5-medium"
effort = { tokens = 150000, time_seconds = 3600, questions = 10, review_lines = 400 }
inputs = ["branch with passing tests"]
outputs = ["preflight evidence"]

[[stages]]
id = "pre-pr"
agents = ["cloud-foreman", "code-reviewer", "devops-engineer"]
skills = ["steward", "ci-cd-quality-gates"]
mcp = ["repolens"]
model = "claude-sonnet-5-5-high"
effort = { tokens = 200000, time_seconds = 7200, questions = 15, review_lines = 400 }
inputs = ["preflight evidence"]
outputs = ["pull request"]

# ── Gates for configured stages ────────────────────────────────────────────

[[gates]]
name = "OpenSpec plans conform"
stage = "plan"
run = "true"

[[gates]]
name = "tests must fail"
stage = "red"
run = "false"
expect = "fail"

[[gates]]
name = "preflight"
stage = "pre-push"
run = "true"

[[gates]]
name = "pipeline"
stage = "pre-pr"
run = "true"

# ── Gate-only stages (no [[stages]] block) ─────────────────────────────────

[[gates]]
name = "baseline ratchet"
stage = "signal"
run = "true"

[[gates]]
name = "skills lint"
stage = "signal"
run = "true"

[[gates]]
name = "protected paths"
stage = "merge"
run = "true"

[[gates]]
name = "evidence chain"
stage = "merge"
run = "true"

# ── Grants (per-stage guardrails) ──────────────────────────────────────────

[[grants]]
id = "plan-sample"
stage = "plan"
worktree = "."
write_set = ["docs/openspec/**", "framework/ai-sdlc/**"]
required_checks = ["OpenSpec plans conform"]
budget = { tokens = 200000, time_seconds = 3600, questions = 20, review_lines = 400 }

[[grants]]
id = "pre-push-sample"
stage = "pre-push"
worktree = "."
write_set = ["backend-api/**", "frontend/**"]
required_checks = ["preflight"]
budget = { tokens = 150000, time_seconds = 3600, questions = 10, review_lines = 400 }
"""

# All agent, skill, and MCP ids referenced in the profile.
_AGENTS = [
    "openspec-planner",
    "testing-qa",
    "backend-implementer",
    "frontend-design",
    "cloud-foreman",
    "devops-engineer",
    "code-reviewer",
]
_SKILLS = [
    "openspec-propose",
    "openspec-update-change",
    "test-master",
    "qa-automation-strategy",
    "ai-agent-workflow",
    "ci-cd-quality-gates",
    "steward",
]
_MCP = ["repolens"]


def _setup_catalogs(repo: Repo) -> None:
    """Register all agents and MCPs in the catalog, and create skill dirs."""
    for aid in _AGENTS:
        result = repo.run(
            "catalog", "add", "--kind", "agents", "--id", aid,
            "--ref", f".aisdlc/agents/{aid}", "--summary", aid,
        )
        if result.returncode != 0:
            raise RuntimeError(f"Failed to add agent {aid}: {output(result)}")

    for mid in _MCP:
        result = repo.run(
            "catalog", "add", "--kind", "mcp", "--id", mid,
            "--ref", f"mcp://{mid}", "--summary", mid,
        )
        if result.returncode != 0:
            raise RuntimeError(f"Failed to add mcp {mid}: {output(result)}")

    for sid in _SKILLS:
        folder = repo.root / "skills" / sid
        folder.mkdir(parents=True, exist_ok=True)
        (folder / "SKILL.md").write_text(
            f"---\nname: {sid}\ndescription: Test skill\n---\n# {sid}\n",
            encoding="utf-8",
        )


class _LineMetadataBase(unittest.TestCase):
    """Set up a Repo with the stage metadata profile and catalog fixtures."""

    def setUp(self) -> None:
        self.repo = Repo(self, STAGE_PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        _setup_catalogs(self.repo)

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")


# ---------------------------------------------------------------------------
# Scenario: every stage has ALL required keys
# ---------------------------------------------------------------------------


class TestAllKeysPresent(unittest.TestCase):
    """Every stage (configured and gate-only) carries all 12 keys."""

    def setUp(self) -> None:
        self.repo = Repo(self, STAGE_PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        _setup_catalogs(self.repo)

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")

    def test_every_stage_has_all_required_keys(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        required_keys = {
            "id", "gates", "agents", "skills", "mcp", "tools",
            "model", "effort", "guardrails", "inputs", "outputs", "io_source",
        }
        for stage in payload["stages"]:
            stage_keys = set(stage.keys())
            missing = required_keys - stage_keys
            self.assertEqual(
                set(), missing,
                f"Stage {stage['id']!r} missing keys: {sorted(missing)}",
            )


# ---------------------------------------------------------------------------
# Scenario: configured stage values match the profile
# ---------------------------------------------------------------------------


class TestConfiguredStageValues(_LineMetadataBase):
    """agents, skills, mcp, tools, model, effort from [[stages]] blocks."""

    def test_plan_stage_agents_skills_mcp_model_effort(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        plan = stages_by_id["plan"]

        self.assertEqual(["openspec-planner"], plan["agents"])
        self.assertEqual(["openspec-propose", "openspec-update-change"], plan["skills"])
        self.assertEqual(["repolens"], plan["mcp"])
        self.assertEqual("claude-sonnet-5-5-high", plan["model"])
        self.assertEqual(
            {"tokens": 200000, "time_seconds": 3600, "questions": 20, "review_lines": 400},
            plan["effort"],
        )

    def test_tools_equals_mcp(self) -> None:
        """tools key equals the mcp list (profile has no other tool source)."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}

        for stage_id in ("plan", "red", "pre-push", "pre-pr"):
            stage = stages_by_id[stage_id]
            self.assertEqual(
                stage["mcp"], stage["tools"],
                f"Stage {stage_id}: tools should equal mcp",
            )

    def test_red_stage_values(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        red = stages_by_id["red"]

        self.assertEqual(
            ["testing-qa", "backend-implementer", "frontend-design"],
            red["agents"],
        )
        self.assertEqual(["test-master", "qa-automation-strategy"], red["skills"])
        self.assertEqual(["repolens"], red["mcp"])
        self.assertEqual("claude-sonnet-5-5-medium", red["model"])


# ---------------------------------------------------------------------------
# Scenario: gate-only stage has empty defaults
# ---------------------------------------------------------------------------


class TestGateOnlyStageDefaults(_LineMetadataBase):
    """signal and merge have no [[stages]] block → empty lists, null model, {} effort."""

    def test_signal_stage_empty_defaults(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        signal = stages_by_id["signal"]

        self.assertEqual([], signal["agents"])
        self.assertEqual([], signal["skills"])
        self.assertEqual([], signal["mcp"])
        self.assertEqual([], signal["tools"])
        self.assertIsNone(signal["model"])
        self.assertEqual({}, signal["effort"])
        self.assertEqual([], signal["guardrails"])

    def test_merge_stage_empty_defaults(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        merge = stages_by_id["merge"]

        self.assertEqual([], merge["agents"])
        self.assertEqual([], merge["skills"])
        self.assertEqual([], merge["mcp"])
        self.assertEqual([], merge["tools"])
        self.assertIsNone(merge["model"])
        self.assertEqual({}, merge["effort"])


# ---------------------------------------------------------------------------
# Scenario: per-stage guardrails from [[grants]]
# ---------------------------------------------------------------------------


class TestPerStageGuardrails(_LineMetadataBase):
    """guardrails = grants targeting the stage, serialized with kind='grant'."""

    def test_plan_guardrails_has_grant_with_kind(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        plan_gr = stages_by_id["plan"]["guardrails"]

        self.assertIsInstance(plan_gr, list)
        self.assertEqual(1, len(plan_gr), f"plan should have 1 guardrail grant: {plan_gr}")
        grant = plan_gr[0]
        self.assertEqual("grant", grant["kind"])
        self.assertEqual("plan-sample", grant["id"])
        self.assertIn("write_set", grant)
        self.assertIn("required_checks", grant)
        self.assertIn("budget", grant)

    def test_pre_push_guardrails_has_own_grant(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        prepush_gr = stages_by_id["pre-push"]["guardrails"]

        self.assertIsInstance(prepush_gr, list)
        self.assertEqual(1, len(prepush_gr))
        self.assertEqual("pre-push-sample", prepush_gr[0]["id"])

    def test_stages_without_grants_have_empty_guardrails(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}

        for sid in ("red", "pre-pr", "signal", "merge"):
            self.assertEqual(
                [], stages_by_id[sid]["guardrails"],
                f"Stage {sid} should have empty guardrails",
            )

    def test_plan_guardrails_does_not_contain_pre_push_grant(self) -> None:
        """Each stage only sees its own grants."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        plan_gr_ids = {g["id"] for g in stages_by_id["plan"]["guardrails"]}
        self.assertNotIn("pre-push-sample", plan_gr_ids)


# ---------------------------------------------------------------------------
# Scenario: values match config_view stages (same source of truth)
# ---------------------------------------------------------------------------


class TestConsistencyWithConfigView(_LineMetadataBase):
    """build_line stage metadata matches stage_dicts(profile)."""

    def test_agents_match_stage_dicts(self) -> None:
        from aisdlc.line import build_line
        from aisdlc.stages import stage_dicts

        ctx = self._ctx()
        payload = build_line(ctx)
        sd_rows = {r["id"]: r for r in stage_dicts(ctx.profile)}
        stages_by_id = {s["id"]: s for s in payload["stages"]}

        for sid in ("plan", "red", "pre-push", "pre-pr"):
            sd = sd_rows[sid]
            line_stage = stages_by_id[sid]
            self.assertEqual(sd["agents"], line_stage["agents"], f"{sid} agents mismatch")
            self.assertEqual(sd["skills"], line_stage["skills"], f"{sid} skills mismatch")
            self.assertEqual(sd["mcp"], line_stage["mcp"], f"{sid} mcp mismatch")
            self.assertEqual(sd["model"], line_stage["model"], f"{sid} model mismatch")
            self.assertEqual(sd["effort"], line_stage["effort"], f"{sid} effort mismatch")


# ---------------------------------------------------------------------------
# Scenario: existing keys unchanged (inputs/outputs/io_source)
# ---------------------------------------------------------------------------


class TestExistingKeysUnchanged(_LineMetadataBase):
    """inputs, outputs, io_source still work as before."""

    def test_plan_io_source_profile(self) -> None:
        """plan has explicit inputs/outputs → io_source='profile'."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        plan = stages_by_id["plan"]
        self.assertEqual("profile", plan["io_source"])
        self.assertEqual(["GitHub issue", "use case"], plan["inputs"])
        self.assertEqual(["OpenSpec change"], plan["outputs"])

    def test_gate_only_stage_io_source_default(self) -> None:
        """signal has no [[stages]] → io_source='default' with kit defaults."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        signal = stages_by_id["signal"]
        self.assertEqual("default", signal["io_source"])


# ---------------------------------------------------------------------------
# Scenario: no secret-shaped value in serialized payload
# ---------------------------------------------------------------------------


class TestNoSecretsInPayload(_LineMetadataBase):
    """Secret-shaped values must not appear in the line output."""

    def test_line_json_no_secret_in_output(self) -> None:
        SECRET = "ghp_" + "X" * 36
        result_add = self.repo.run(
            "catalog", "add", "--kind", "agents", "--id", "secret-agent",
            "--ref", "agents/secret-agent.md",
            "--summary", f"token {SECRET}",
        )
        self.assertEqual(0, result_add.returncode, output(result_add))

        result = self.repo.run("line", "--json")
        self.assertEqual(0, result.returncode, output(result))
        self.assertNotIn(SECRET, result.stdout)


# ---------------------------------------------------------------------------
# Scenario: CLI --json includes the new keys
# ---------------------------------------------------------------------------


class TestCLIJsonMetadata(_LineMetadataBase):
    """aisdlc line --json includes agents, skills, mcp, tools, model, effort, guardrails."""

    def test_line_json_has_stage_metadata_keys(self) -> None:
        result = self.repo.run("line", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        stages_by_id = {s["id"]: s for s in data["stages"]}
        plan = stages_by_id["plan"]
        self.assertIn("agents", plan)
        self.assertIn("skills", plan)
        self.assertIn("mcp", plan)
        self.assertIn("tools", plan)
        self.assertIn("model", plan)
        self.assertIn("effort", plan)
        self.assertIn("guardrails", plan)


# ---------------------------------------------------------------------------
# Scenario: GET /factory/line includes the new keys (HTTP)
# ---------------------------------------------------------------------------


class TestServeLineMetadata(_LineMetadataBase):
    """GET /factory/line over the real serve host includes stage metadata."""

    def _serve(self):
        from aisdlc import serve as serve_mod

        ctx = self._ctx()
        server = serve_mod.start_server(ctx, host="127.0.0.1", port=0)
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        token = serve_mod.load_token_value(self.repo.root)
        return server.server_address[1], token

    def _get(self, port: int, path: str, *, token: str | None):
        headers = {}
        if token is not None:
            headers["Authorization"] = f"Bearer {token}"
        req = urllib.request.Request(
            f"http://127.0.0.1:{port}{path}",
            method="GET",
            headers=headers,
        )
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                body = resp.read().decode("utf-8")
                return resp.status, resp.headers.get_content_type(), body
        except urllib.error.HTTPError as exc:
            body = exc.read().decode("utf-8")
            return exc.code, exc.headers.get_content_type(), body

    def test_get_factory_line_has_stage_metadata(self) -> None:
        port, token = self._serve()
        status, _ctype, body = self._get(port, "/factory/line", token=token)
        self.assertEqual(200, status)
        data = json.loads(body)
        stages_by_id = {s["id"]: s for s in data["stages"]}
        plan = stages_by_id["plan"]
        for key in ("agents", "skills", "mcp", "tools", "model", "effort", "guardrails"):
            self.assertIn(key, plan, f"Stage plan missing key {key!r} in HTTP response")


if __name__ == "__main__":
    unittest.main()
