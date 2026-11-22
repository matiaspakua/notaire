> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU76`)
- [x] 1.2 Use Case documentation exists and is accurate
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR not required — additive internal observability, no architectural change
- [x] 1.6 Move the Issue to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh 2396`

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/2396_production_line_model`
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: all seven acceptance scenarios plus edge cases (empty
      catalogs dir, slug collision, stage with neither `[[stages]]` nor gates entry)
- [x] 3.2 Write `tests/test_line.py` covering all `#### Scenario:` entries in the delta spec
- [x] 3.3 `tests/test_line.py` also covers `GET /factory/line` read scope (consolidated in
      one file per commit 48eff25d; no separate `test_serve_line.py` was created — mechanical
      deviation: same test surface, same coverage, simpler layout; does not change scope/contract)
- [x] 3.4 Run them and observe them fail: `python3 -m pytest framework/ai-sdlc/tests/test_line.py`
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Add `inputs: tuple[str,...]` and `outputs: tuple[str,...]` to `StageProfile`
      in `profile.py`, parsed with `_string_list` in `_stage_profiles`
- [x] 4.2 Add `inputs`/`outputs` keys to `stage_dicts` output in `stages.py`;
      add `DEFAULT_IO` dict for the seven known stage ids (placed in `line.py` per design D1)
- [x] 4.3 Create `framework/ai-sdlc/aisdlc/line.py` with `build_line(ctx)` composing
      all sources; include `_gate_slug` with collision deduplication
- [x] 4.4 Add `line` subcommand to `cli.py` (`aisdlc line [--json]`) with human text
      rendering and JSON output
- [x] 4.5 Add `GET /factory/line` block to `serve.py` `_dispatch_get`
- [x] 4.6 Add `inputs`/`outputs` to the four existing `[[stages]]` blocks in
      `.aisdlc/aisdlc.toml` (`plan`, `red`, `pre-push`, `pre-pr`)
- [x] 4.7 Document optional `inputs`/`outputs` keys in
      `framework/ai-sdlc/templates/project/aisdlc.toml`
- [x] 4.8 Add `/factory/line` path and new schemas (`ProductionLine`, `LineStage`,
      `LineGate`, `LineGuardrails`) to `framework/ai-sdlc/openapi/factory-control.openapi.yaml`

## 5. Actualizar tests existentes

- [x] 5.1 Update `tests/test_stages.py` to assert `inputs` and `outputs` keys are
      present in `stage_dicts` output (additive; no assertion is weakened)
- [x] 5.2 Update any existing profile-parsing tests that construct `StageProfile`
      directly, if needed (the new fields have default `()` so construction is unchanged)
- [x] 5.3 No tests made genuinely obsolete

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m pytest framework/ai-sdlc/tests/` — all green (31 tests in
      `test_line.py`, full suite green commit f6f20264 / 909d292d)
- [x] 6.2 Coverage check: kit uses its own test runner; no JaCoCo floor applies
- [x] 6.3 `python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml doctor` exits 0
- [x] 6.4 `python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml line --json` exits 0
- [x] 6.5 No skipped tests without documented justification

## 7. Ejecutar Playwright

- [x] 7.4 n/a — no UI surface; this change adds CLI and HTTP API surface only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `framework/ai-sdlc/CHANGELOG.md` with `[Unreleased]` entry for
      `aisdlc line` and `GET /factory/line`
- [x] 8.2 Update `framework/ai-sdlc/README.md` to document the `aisdlc line` command
- [x] 8.3 Update `framework/ai-sdlc/openapi/factory-control.openapi.yaml` (covered in 4.8)
- [x] 8.4 No documents to archive
- [x] 8.5 Confirm no content duplicated — permanent docs are the single source of truth
- [x] 8.6 `bash workspace/sdlc/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [x] 9.1 Commits follow Conventional Commits format; each body ends with `Refs #2396`;
      the PR closes `#2396` after Gate 5
- [x] 9.2 No secrets, no commented-out code, no unrelated changes
- [x] 9.3 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 Archive this change: `cd docs && openspec archive feat-2396-production-line-model --yes`
- [ ] 10.2 `git push -u origin feat/2396_production_line_model`
- [ ] 10.3 Open PR titled `[#2396] feat(aisdlc): production line model — stages, gates,
      guardrails, inputs and outputs`, referencing Issue and Use Case CU76
- [ ] 10.4 Wait for required workflows to pass
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Record PR number in archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via Pull Request only
- [ ] 11.2 Confirm CD pipeline published the image to GHCR
- [ ] 11.3 Record merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run smoke test: `aisdlc line --json` on the deployed profile exits 0
- [ ] 12.2 Rollback path confirmed: revert is safe (additive only)
- [ ] 12.3 Close GitHub Issue #2396, referencing the PR
- [ ] 12.4 Record deploy and smoke evidence in archived `traceability.md`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E
- [x] Coverage at or above the kit floor
- [x] Playwright E2E: n/a — no UI surface
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release

## Design decisions recorded as implemented

**DD-A — `[[stages]]` id must match a gate stage**
A `[[stages]]` block whose `id` does not appear in any `[[gates]]` entry is rejected
by the profile loader (`profile.py` line 732: `if stage_id not in gate_stages`). Therefore
a stage with no `[[gates]]` entry cannot be declared via `[[stages]]`. Design D6 describes
the union logic in `build_line`; the "purely `[[stages]]`-only" branch of that union is
unreachable under the current loader. The test suite documents this constraint in the
class docstring of `TestStageWithNoGates`. This is a mechanical constraint, not a scope
change; the loader behaviour predates this change.

**DD-B — Gate id is a slug of the gate name**
The gate `id` field in the payload is derived from the gate name: lowercased, all
non-alphanumeric characters replaced with `-`, repeated `-` collapsed, leading/trailing
`-` stripped (`_gate_slug` in `line.py`). Collisions are resolved by appending `-2`,
`-3`, … (see `_unique_slugs`). This matches design D2 exactly.

**DD-C — `tools` is an alias of `mcp`**
The payload-facing kit convention uses `mcp` as the canonical field; `tools` is the
equivalent API-facing name for MCP tool catalogs. `StageProfile` stores MCP entries
in `mcp` and `stage_dicts` serializes that field as `"mcp"`; the implementation does
not accept a separate `tools` key in TOML. This prevents a second, divergent parser
and matches the shared line contract's MCP/tool terminology.
