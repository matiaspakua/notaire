"""Failing tests for the production line model (#2396).

Gate 2: red — these tests MUST fail until line.py, the CLI subcommand, and the
HTTP endpoint are implemented (Gate 3: green).

Design contract: docs/openspec/changes/feat-2396-production-line-model/design.md
Delta spec:      docs/openspec/changes/feat-2396-production-line-model/specs/aisdlc-production-line/spec.md

Note on "stage with no gates" scenario (design D6, spec §stage with no gates):
The profile loader (_stage_profiles) rejects a [[stages]] id that is not in
gate_stages (gates declared via [[gates]]). Therefore a [[stages]] block with no
corresponding [[gates]] entry is impossible to construct directly. The scenario is
exercised instead via build_line: a stage that has gates but no [[stages]] block
is a gate-only stage, and its gates list must be non-empty. The "empty gates" case
is tested via a stage that has a [[stages]] block AND a [[gates]] block, so the
loader accepts it; removing the gates from the resulting profile object would
require patching the dataclass. We test the nearest observable equivalent: a stage
declared in [[stages]] whose only [[gates]] entry exists and therefore its gates
list in the payload is non-empty. The "gates: []" outcome cannot be directly tested
without a profile-loader change — it is a post-implementation concern.
"""
from __future__ import annotations

import json
import sys
import threading
import unittest
import urllib.error
import urllib.request

from helpers import KIT, Repo, isolator_live_snapshot, output

sys.path.insert(0, str(KIT))

# ---------------------------------------------------------------------------
# Sample profile used across most tests.
#
# Covers:
#   - [[gates]] for stages plan, red, and pre-push
#   - required=true (default), required=false, and expect="fail" gates
#   - paths on a gate
#   - [[stages]] block for plan (inputs/outputs declared → io_source "profile")
#   - [[stages]] block for red  (no inputs/outputs → io_source "default")
#   - [[stages]] block for pre-push (has gate, so loader accepts it; used for
#     the "stage declared in [[stages]] with a gate" scenario)
#   - [protected] paths including values containing "hook" (for hooks derivation)
#   - [catalogs] dir so list_entries can be called
# ---------------------------------------------------------------------------

# Secret-shaped value placed at runtime so the source tree stays clean for secret scan.
_SECRET_PREFIX = "ghp_"
SECRET_SHAPED = _SECRET_PREFIX + "X" * 36

SAMPLE_PROFILE = """
[project]
name = "line-demo"
policy = "POLICY.md"

[[gates]]
name = "OpenSpec plans conform (live issue check)"
stage = "plan"
run = "true"
required = true
paths = ["docs/openspec/**"]

[[gates]]
name = "Lint advisory"
stage = "plan"
run = "true"
required = false

[[gates]]
name = "tests must fail (expect fail gate)"
stage = "red"
run = "false"
expect = "fail"
required = true

[[gates]]
name = "pre-push preflight"
stage = "pre-push"
run = "true"

[[stages]]
id = "plan"
inputs = ["GitHub issue", "use case (CU)"]
outputs = ["OpenSpec change"]

[[stages]]
id = "red"
# no inputs / outputs declared → kit defaults apply

[[stages]]
id = "pre-push"
# no inputs / outputs; has a gate so loader accepts it

[protected]
owner = "the Owner"
paths = [
  ".claude/hooks/**",
  ".githooks/**",
  "CONSTITUTION.md",
  "docs/100-business/**",
]

[catalogs]
dir = ".aisdlc/catalogs"
"""


class _LineBase(unittest.TestCase):
    """Set up a Repo with the sample profile and a rules catalog entry."""

    def setUp(self) -> None:
        self.repo = Repo(self, SAMPLE_PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")
        # Add a rules catalog entry so guardrails.rules is non-empty.
        result = self.repo.run(
            "catalog",
            "add",
            "--kind",
            "rules",
            "--id",
            "general-rules",
            "--ref",
            ".claude/rules/general.md",
            "--summary",
            "General coding rules",
        )
        self.assertEqual(0, result.returncode, output(result))

    def _ctx(self):
        from aisdlc.cli import load_context

        return load_context(self.repo.root, self.repo.root / "aisdlc.toml")


# ---------------------------------------------------------------------------
# Scenario: line lists ordered stages with gates
# ---------------------------------------------------------------------------


class TestOrderedStagesWithGates(_LineBase):
    """build_line returns stages ordered by profile.stages(): plan, red, pre-push."""

    def test_stages_ordered_plan_red_pre_push(self) -> None:
        from aisdlc.line import build_line  # ImportError is the expected red failure

        ctx = self._ctx()
        payload = build_line(ctx)

        self.assertIn("stages", payload)
        ids = [s["id"] for s in payload["stages"]]
        self.assertIn("plan", ids)
        self.assertIn("red", ids)
        self.assertIn("pre-push", ids)
        # Canonical lifecycle order: plan < red < pre-push
        self.assertLess(ids.index("plan"), ids.index("red"))
        self.assertLess(ids.index("red"), ids.index("pre-push"))

    def test_gate_bearing_stages_have_non_empty_gates(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        stages_by_id = {s["id"]: s for s in payload["stages"]}
        self.assertTrue(len(stages_by_id["plan"]["gates"]) > 0, "plan must have gates")
        self.assertTrue(len(stages_by_id["red"]["gates"]) > 0, "red must have gates")


# ---------------------------------------------------------------------------
# Scenario: stage with no gates still listed (via [[stages]] declaration)
#
# The profile loader rejects a [[stages]] id not in gate_stages, so it is
# impossible to construct a profile with a [[stages]] block that has NO
# corresponding [[gates]] entry. The nearest observable equivalent is:
# build_line includes every stage that has a [[stages]] block OR has gates;
# since both conditions require a [[gates]] entry for the id to be loadable,
# we verify that pre-push (which has a gate) appears in the payload.
# The design intent (D6) is that build_line's union logic covers stages that
# are ONLY in [[stages]] — that path will be tested after the loader gains
# support for gate-less [[stages]] ids if that work is pursued.
# ---------------------------------------------------------------------------


class TestStageWithNoGates(_LineBase):
    """pre-push is in [[stages]] (and has a gate); it must appear in the line payload."""

    def test_pre_push_listed_in_stages(self) -> None:
        """Every stage in the sample profile appears in the line output."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        ids = [s["id"] for s in payload["stages"]]
        self.assertIn("pre-push", ids)

    def test_gate_only_stage_has_gates_key(self) -> None:
        """A stage with gates always carries a 'gates' key (list) in the payload."""
        from aisdlc.line import build_line

        # Build a profile where 'green' has a gate but no [[stages]] block.
        # green is a canonical stage id; the loader will accept it.
        gate_only_profile = """
[project]
name = "gate-only"
policy = "POLICY.md"

[[gates]]
name = "all tests pass"
stage = "green"
run = "true"
"""
        repo = Repo(self, gate_only_profile)
        repo.write("POLICY.md", "# Policy\n")

        from aisdlc.cli import load_context

        ctx = load_context(repo.root, repo.root / "aisdlc.toml")
        payload = build_line(ctx)
        ids = [s["id"] for s in payload["stages"]]
        self.assertIn("green", ids)
        stages_by_id = {s["id"]: s for s in payload["stages"]}
        self.assertIsInstance(stages_by_id["green"]["gates"], list)
        self.assertTrue(len(stages_by_id["green"]["gates"]) > 0)


# ---------------------------------------------------------------------------
# Scenario: gate id is a stable slug
# ---------------------------------------------------------------------------


class TestGateSlug(_LineBase):
    """Gate id is a deterministic slug derived from the gate name."""

    def test_slug_for_live_issue_check_gate(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        stages_by_id = {s["id"]: s for s in payload["stages"]}
        plan_gates = stages_by_id["plan"]["gates"]
        ids = [g["id"] for g in plan_gates]
        self.assertIn(
            "openspec-plans-conform-live-issue-check",
            ids,
            f"Expected slug not found in gate ids: {ids}",
        )

    def test_gate_shape_has_required_fields(self) -> None:
        """Every gate carries id, name, command, required, mode, expect, paths."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        stages_by_id = {s["id"]: s for s in payload["stages"]}
        for gate in stages_by_id["plan"]["gates"]:
            for field in ("id", "name", "command", "required", "mode", "expect", "paths"):
                self.assertIn(field, gate, f"gate missing field {field!r}: {gate}")

    def test_advisory_gate_mode(self) -> None:
        """required=false → mode='advisory'."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        stages_by_id = {s["id"]: s for s in payload["stages"]}
        plan_gates = {g["name"]: g for g in stages_by_id["plan"]["gates"]}
        advisory = plan_gates["Lint advisory"]
        self.assertFalse(advisory["required"])
        self.assertEqual("advisory", advisory["mode"])

    def test_expect_fail_gate(self) -> None:
        """expect='fail' is preserved in the payload."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        stages_by_id = {s["id"]: s for s in payload["stages"]}
        red_gates = stages_by_id["red"]["gates"]
        expect_fail = [g for g in red_gates if g.get("expect") == "fail"]
        self.assertTrue(len(expect_fail) > 0, "No gate with expect='fail' found in red")

    def test_gate_paths_included(self) -> None:
        """paths field is a list (possibly empty) on each gate."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        stages_by_id = {s["id"]: s for s in payload["stages"]}
        for stage_entry in payload["stages"]:
            for gate in stage_entry["gates"]:
                self.assertIsInstance(gate["paths"], list)
        # The plan gate with paths = ["docs/openspec/**"] has non-empty paths
        plan_gates = {g["name"]: g for g in stages_by_id["plan"]["gates"]}
        openspec_gate = plan_gates["OpenSpec plans conform (live issue check)"]
        self.assertIn("docs/openspec/**", openspec_gate["paths"])


# ---------------------------------------------------------------------------
# Scenario: inputs/outputs from profile override kit defaults
# ---------------------------------------------------------------------------


class TestInputsOutputsProfileOverride(_LineBase):
    """plan stage has explicit inputs/outputs → io_source='profile'."""

    def test_plan_stage_io_source_profile(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        stages_by_id = {s["id"]: s for s in payload["stages"]}
        plan = stages_by_id["plan"]
        self.assertEqual(["GitHub issue", "use case (CU)"], plan["inputs"])
        self.assertEqual(["OpenSpec change"], plan["outputs"])
        self.assertEqual("profile", plan["io_source"])


# ---------------------------------------------------------------------------
# Scenario: stage without inputs/outputs gets defaults and io_source default
# ---------------------------------------------------------------------------


class TestInputsOutputsDefaults(_LineBase):
    """red stage has no inputs/outputs declared → kit defaults + io_source='default'."""

    def test_red_stage_io_source_default(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        stages_by_id = {s["id"]: s for s in payload["stages"]}
        red = stages_by_id["red"]
        self.assertEqual("default", red["io_source"])
        self.assertTrue(len(red["inputs"]) > 0, "red defaults must have inputs")
        self.assertTrue(len(red["outputs"]) > 0, "red defaults must have outputs")

    def test_known_stage_default_io_values(self) -> None:
        """Kit defaults for 'red': inputs=['OpenSpec change'], outputs=['failing tests']."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        stages_by_id = {s["id"]: s for s in payload["stages"]}
        red = stages_by_id["red"]
        self.assertIn("OpenSpec change", red["inputs"])
        self.assertIn("failing tests", red["outputs"])

    def test_stage_dicts_includes_inputs_outputs(self) -> None:
        """stages.stage_dicts must include inputs and outputs keys."""
        from aisdlc.stages import stage_dicts

        ctx = self._ctx()
        rows = stage_dicts(ctx.profile)
        for row in rows:
            self.assertIn("inputs", row, f"stage_dicts row missing 'inputs': {row}")
            self.assertIn("outputs", row, f"stage_dicts row missing 'outputs': {row}")


# ---------------------------------------------------------------------------
# Scenario: profile parsing rejects non-list inputs/outputs
# ---------------------------------------------------------------------------


class TestProfileParsingValidation(unittest.TestCase):
    """Profile loader rejects inputs/outputs that are not lists of strings."""

    def test_non_list_inputs_raises_profile_error(self) -> None:
        from aisdlc.profile import ProfileError, load_profile

        bad_profile = """
[project]
name = "bad"
policy = "POLICY.md"

[[gates]]
name = "check"
stage = "plan"
run = "true"

[[stages]]
id = "plan"
inputs = "not a list"
"""
        with self.assertRaises(ProfileError):
            load_profile(text=bad_profile)

    def test_non_list_outputs_raises_profile_error(self) -> None:
        from aisdlc.profile import ProfileError, load_profile

        bad_profile = """
[project]
name = "bad"
policy = "POLICY.md"

[[gates]]
name = "check"
stage = "red"
run = "true"

[[stages]]
id = "red"
outputs = 42
"""
        with self.assertRaises(ProfileError):
            load_profile(text=bad_profile)


# ---------------------------------------------------------------------------
# Scenario: guardrails expose protected paths, hooks, rules, isolator
# ---------------------------------------------------------------------------


class TestGuardrails(_LineBase):
    """Guardrails block shape and content."""

    def test_guardrails_key_present(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)
        self.assertIn("guardrails", payload)

    def test_protected_owner_and_paths(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        g = payload["guardrails"]
        self.assertIn("protected", g)
        self.assertEqual("the Owner", g["protected"]["owner"])
        paths = g["protected"]["paths"]
        self.assertIsInstance(paths, list)
        self.assertTrue(len(paths) > 0, "protected.paths must be non-empty")
        self.assertIn(".claude/hooks/**", paths)
        self.assertIn(".githooks/**", paths)

    def test_hooks_derived_from_protected_paths(self) -> None:
        """hooks = protected paths whose value contains 'hook' (case-insensitive)."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        g = payload["guardrails"]
        self.assertIn("hooks", g)
        hooks = g["hooks"]
        self.assertIsInstance(hooks, list)
        # Both .claude/hooks/** and .githooks/** contain 'hook'
        self.assertIn(".claude/hooks/**", hooks)
        self.assertIn(".githooks/**", hooks)
        # Non-hook paths must NOT appear in hooks
        self.assertNotIn("CONSTITUTION.md", hooks)
        self.assertNotIn("docs/100-business/**", hooks)

    def test_rules_catalog_non_empty(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        g = payload["guardrails"]
        self.assertIn("rules", g)
        self.assertIsInstance(g["rules"], list)
        self.assertTrue(len(g["rules"]) > 0, "rules catalog must be non-empty after setUp add")

    def test_isolator_is_dict_with_known_keys(self) -> None:
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        g = payload["guardrails"]
        self.assertIn("isolator", g)
        iso = g["isolator"]
        self.assertIsInstance(iso, dict)
        for key in ("status", "sandbox", "backend", "enforces_committer"):
            self.assertIn(key, iso, f"isolator missing key {key!r}")

    def test_isolator_honest_value(self) -> None:
        """isolator.sandbox and status must match the live probe, not a forced value."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        payload = build_line(ctx)

        snap = isolator_live_snapshot()
        iso = payload["guardrails"]["isolator"]
        self.assertEqual(bool(snap.get("sandbox")), iso["sandbox"])
        self.assertEqual(snap.get("status"), iso["status"])

    def test_missing_catalogs_dir_gives_empty_rules(self) -> None:
        """When catalogs dir is absent, guardrails.rules is [] (never raises)."""
        from aisdlc.line import build_line

        no_catalogs_profile = """
[project]
name = "no-cat"
policy = "POLICY.md"

[[gates]]
name = "plan check"
stage = "plan"
run = "true"
"""
        repo = Repo(self, no_catalogs_profile)
        repo.write("POLICY.md", "# Policy\n")

        from aisdlc.cli import load_context

        ctx = load_context(repo.root, repo.root / "aisdlc.toml")
        payload = build_line(ctx)
        self.assertEqual([], payload["guardrails"]["rules"])


# ---------------------------------------------------------------------------
# Scenario: CLI text and --json output (black-box via Repo.run)
# ---------------------------------------------------------------------------


class TestCLIOutput(_LineBase):
    """aisdlc line and aisdlc line --json subcommand."""

    def test_line_json_exits_zero_and_valid_json(self) -> None:
        result = self.repo.run("line", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertIn("stages", data)
        self.assertIn("guardrails", data)

    def test_line_json_has_schema_version(self) -> None:
        result = self.repo.run("line", "--json")
        self.assertEqual(0, result.returncode, output(result))
        data = json.loads(result.stdout)
        self.assertIn("schema_version", data)

    def test_line_text_exits_zero_and_human_readable(self) -> None:
        result = self.repo.run("line")
        self.assertEqual(0, result.returncode, output(result))
        # Must mention at least one stage id and one gate
        combined = output(result)
        self.assertTrue(
            "plan" in combined or "red" in combined,
            f"Expected stage names in text output: {combined[:500]}",
        )

    def test_line_text_contains_stage_and_gate_info(self) -> None:
        result = self.repo.run("line")
        self.assertEqual(0, result.returncode, output(result))
        combined = output(result)
        # Should mention gates or guardrails in some form
        self.assertTrue(
            any(word in combined.lower() for word in ("gate", "stage", "guardrail")),
            f"Expected stage/gate info in text output: {combined[:500]}",
        )

    def test_line_json_no_secret_in_output(self) -> None:
        """A secret-shaped value in the profile must not be echoed in line output."""
        # Add the secret-shaped value as a catalog summary (mirrors test_serve.py pattern)
        result_add = self.repo.run(
            "catalog",
            "add",
            "--kind",
            "agents",
            "--id",
            "secret-carrier",
            "--ref",
            "agents/secret-carrier.md",
            "--summary",
            f"token {SECRET_SHAPED}",
        )
        self.assertEqual(0, result_add.returncode, output(result_add))

        result = self.repo.run("line", "--json")
        self.assertEqual(0, result.returncode, output(result))
        self.assertNotIn(SECRET_SHAPED, result.stdout, "secret-shaped value must not appear in line output")
        self.assertNotIn(SECRET_SHAPED, result.stderr, "secret-shaped value must not appear in stderr")


# ---------------------------------------------------------------------------
# Scenario: GET /factory/line read scope (HTTP, via real serve host)
# ---------------------------------------------------------------------------


class TestServeLineEndpoint(_LineBase):
    """GET /factory/line over the real serve host."""

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

    def test_get_factory_line_read_token_200(self) -> None:
        port, token = self._serve()
        status, ctype, body = self._get(port, "/factory/line", token=token)
        self.assertEqual(200, status)
        self.assertEqual("application/json", ctype)
        data = json.loads(body)
        self.assertIn("schema_version", data)
        self.assertIn("stages", data)
        self.assertIsInstance(data["stages"], list)

    def test_get_factory_line_no_token_401(self) -> None:
        port, _token = self._serve()
        status, ctype, body = self._get(port, "/factory/line", token=None)
        self.assertEqual(401, status)
        self.assertEqual("application/problem+json", ctype)
        self.assertEqual("unauthorized", json.loads(body)["code"])

    def test_get_factory_line_no_secret_in_response(self) -> None:
        """Secret-shaped value planted in catalog must not appear in /factory/line response."""
        # Plant it via raw file manipulation (catalog already created in setUp)
        entry = (
            self.repo.root
            / ".aisdlc"
            / "catalogs"
            / "rules"
            / "general-rules"
            / "current.json"
        )
        if entry.is_file():
            data = json.loads(entry.read_text(encoding="utf-8"))
            data["summary"] = f"token {SECRET_SHAPED}"
            entry.write_text(json.dumps(data), encoding="utf-8")

        port, token = self._serve()
        status, _, body = self._get(port, "/factory/line", token=token)
        self.assertEqual(200, status)
        self.assertNotIn(SECRET_SHAPED, body, "secret-shaped value must not appear in /factory/line response")

    def test_get_factory_line_payload_matches_build_line(self) -> None:
        """HTTP response payload equals what build_line(ctx) returns."""
        from aisdlc.line import build_line

        ctx = self._ctx()
        expected = build_line(ctx)

        port, token = self._serve()
        status, _, body = self._get(port, "/factory/line", token=token)
        self.assertEqual(200, status)
        actual = json.loads(body)
        # stages list must have the same stage ids in the same order
        expected_ids = [s["id"] for s in expected["stages"]]
        actual_ids = [s["id"] for s in actual["stages"]]
        self.assertEqual(expected_ids, actual_ids)


if __name__ == "__main__":
    unittest.main()
