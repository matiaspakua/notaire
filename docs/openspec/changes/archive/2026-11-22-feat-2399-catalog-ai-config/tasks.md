> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural
- [x] 1.6 Move the Issue to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh <n>` (uses `status:in-progress`; on `addLabelsToLabelable` / `issues: write` 403 print honest SKIPPED and continue — never fake in-progress; see `docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/2399_catalog_ai_config`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Write unit and integration tests in `framework/ai-sdlc/tests/test_materialize.py` for import, idempotency, secret guard, materialize, check drift, evidence, telemetry, and round-trip byte identity
- [x] 3.3 Write tests in `framework/ai-sdlc/tests/test_extra_harness_stubs.py` asserting `agy` and `kiro` are listed by `aisdlc harness list`
- [x] 3.4 Run them and **observe them fail** — `python3 -m unittest framework/ai-sdlc/tests/test_materialize.py`
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Add `agy` and `kiro` harness stub plugins under `framework/ai-sdlc/plugins/agy` and `framework/ai-sdlc/plugins/kiro` and register in `.aisdlc/aisdlc.toml`
- [x] 4.2 Implement `catalogs.guard_files(files)` rejecting relative path escaping and text matching `factory.SECRET_VALUES` with code `literal_secret`, and update `catalogs.validate`
- [x] 4.3 Update `catalogs.add` and `catalogs.write_new_revision` to support immutable `files` revision payloads
- [x] 4.4 Create `framework/ai-sdlc/aisdlc/materialize.py` defining the `LAYOUTS` table mapping harnesses to dir/file units
- [x] 4.5 Implement `catalog import` logic in `materialize.py` with UTF-8 byte preservation and idempotency
- [x] 4.6 Implement `catalog materialize` logic in `materialize.py` with `.aisdlc-materialized.json` manifest generation and drift checking (`--check`)
- [x] 4.7 Wire evidence records (`evidence.append_record`) and telemetry events (`telemetry.get_telemetry().emit`) for import and materialize
- [x] 4.8 Wire CLI commands `aisdlc catalog import` and `aisdlc catalog materialize` in `framework/ai-sdlc/aisdlc/cli.py`

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change (update `test_extra_harness_stubs.py` harness count/ids)
- [x] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest discover -s framework/ai-sdlc/tests` — 508 tests passing
- [x] 6.2 `python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml scope --base origin/main` — scope passed, 18 paths inside write set
- [x] 6.3 `python3 framework/ai-sdlc/bin/aisdlc harness list` — all harness stubs listed including agy and kiro
- [x] 6.4 No skipped or disabled tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface: CLI and framework changes only (CU76 backend/kit)
- [x] 7.2 Verify no UI components or browser flows were altered

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `framework/ai-sdlc/README.md` with `catalog import` and `catalog materialize` documentation
- [x] 8.2 Update `framework/ai-sdlc/CHANGELOG.md` with `[Unreleased]` entry for #2399
- [x] 8.3 Update `docs/openspec/specs/catalogs/spec.md` with the new capabilities
- [x] 8.4 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.5 `bash workspace/sdlc/preflight.sh --fix` — not run; full preflight requires Java/Maven infrastructure not exercised for this kit-only change

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every intermediate commit message body ends with `Refs #2399`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive feat-2399-catalog-ai-config --yes`
- [ ] 10.2 Open the PR titled `[#2399] feat(aisdlc): catalog-owned AI configuration and harness materialization`
- [ ] 10.3 Wait for every required workflow to pass
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm git worktree remains clean and updated
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run smoke tests (`aisdlc harness list`, `aisdlc catalog import`, `aisdlc catalog materialize --check`)
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] TDD followed: tests observed failing before implementation
- [x] Unit + integration tests passing with no regressions
- [x] Permanent documentation updated (`README.md`, `CHANGELOG.md`, `specs/catalogs/spec.md`)
- [x] No secrets or unredacted credentials stored or committed
- [ ] PR passes all CI quality gates and OpenSpec archived

## Deviations

- Mechanical: `--json` removed from `catalog import` and `catalog materialize` — never implemented, issue #2399 acceptance criteria do not list it.
