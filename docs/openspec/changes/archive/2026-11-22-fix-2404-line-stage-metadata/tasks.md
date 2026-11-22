> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU76`)
- [x] 1.2 Use Case documentation exists and is accurate
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR not required — additive enrichment of existing payload, no architectural change
- [x] 1.6 Move the Issue to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh 2404`

## 2. Crear branch

- [x] 2.1 Branch already created: `fix/2404_line_stage_metadata` (from origin/main)
- [x] 2.2 Branch recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: all four acceptance scenarios (all keys present with configured stage, gate-only stage empty values, stage guardrails list grants, real profile shape)
- [x] 3.2 Write `tests/test_line_stage_metadata.py` covering all `#### Scenario:` entries in the delta spec
- [x] 3.3 Run them and observe them fail: `python3 -m pytest framework/ai-sdlc/tests/test_line_stage_metadata.py`
- [x] 3.4 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 In `line.py`, after building each stage dict, merge metadata from `sd_by_id[sid]` if present: `agents`, `skills`, `mcp`, `model`, `effort`; set `tools` = copy of `mcp`
- [x] 4.2 In `line.py`, for gate-only stages (no `sd_by_id` entry), set defaults: `agents=[]`, `skills=[]`, `mcp=[]`, `tools=[]`, `model=None`, `effort={}`
- [x] 4.3 In `line.py`, compute per-stage guardrails: filter `profile.grants` by `grant.stage == sid`, serialize each with `grants.grant_dict(g)` plus `"kind": "grant"`; empty list when none
- [x] 4.4 Every stage dict appended to `stages_out` includes all keys: `id, gates, agents, skills, mcp, tools, model, effort, guardrails, inputs, outputs, io_source`
- [x] 4.5 Update `framework/ai-sdlc/openapi/factory-control.openapi.yaml`: expand `LineStage.required` to all keys, add `guardrails` array property, bump `info.version` to 1.4.0
- [x] 4.6 Update `framework/ai-sdlc/README.md` `aisdlc line` section
- [x] 4.7 Update `framework/ai-sdlc/CHANGELOG.md` with `[Unreleased]` entry

## 5. Actualizar tests existentes

- [x] 5.1 New keys are asserted in `tests/test_line_stage_metadata.py`; `tests/test_line.py` was left unchanged (additive coverage via dedicated test file)
- [x] 5.2 No tests made genuinely obsolete

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m pytest framework/ai-sdlc/tests/` — all green (738 passed, 4 skipped, run by supervisor)
- [x] 6.2 Coverage check: kit uses its own test runner; no JaCoCo floor applies
- [x] 6.3 `python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml line --json` exits 0 and every stage has all keys
- [x] 6.4 No skipped tests without documented justification

## 7. Ejecutar Playwright

- [x] 7.4 n/a — no UI surface; this change adds keys to a JSON payload

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `framework/ai-sdlc/CHANGELOG.md` with `[Unreleased]` entry
- [x] 8.2 Update `framework/ai-sdlc/README.md` to document the enriched per-stage keys
- [x] 8.3 Update `framework/ai-sdlc/openapi/factory-control.openapi.yaml` (covered in 4.5)
- [x] 8.4 No documents to archive
- [x] 8.5 Confirm no content duplicated — permanent docs are the single source of truth
- [x] 8.6 `bash workspace/sdlc/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [x] 9.1 Commits follow Conventional Commits format; each body ends with `Refs #2404`
- [x] 9.2 No secrets, no commented-out code, no unrelated changes
- [x] 9.3 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change: `cd docs && openspec archive fix-2404-line-stage-metadata --yes`
- [ ] 10.2 `git push -u origin fix/2404_line_stage_metadata`
- [ ] 10.3 Open PR titled `[#2404] fix(aisdlc): /factory/line stages carry agents, skills, tools, model, effort, guardrails`
- [ ] 10.4 Wait for required workflows to pass
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Record PR number in archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via Pull Request only
- [ ] 11.2 Confirm CD pipeline published the image to GHCR
- [ ] 11.3 Record merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run smoke test: `aisdlc line --json` on the deployed profile exits 0 and every stage has all keys
- [ ] 12.2 Rollback path confirmed: revert is safe (additive only)
- [ ] 12.3 Close GitHub Issue #2404, referencing the PR
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E (738 passed, 4 skipped)
- [x] Coverage at or above the kit floor
- [x] Playwright E2E: n/a — no UI surface
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4) — requires PR
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5) — requires PR
- [ ] `traceability.md` complete from Issue through Release
