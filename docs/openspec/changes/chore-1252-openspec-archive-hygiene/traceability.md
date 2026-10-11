# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1252 | open (apply in progress) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-1252-openspec-archive-hygiene/` | Gate 1 + apply |
| Branch | `cursor/chore-1252-openspec-archive-hygiene-69d3` | created from `fa7f0c03` |
| Tasks | `tasks.md` | groups 1–8 ticked; 9–12 pending PR/merge |
| Commits | `150b1127` test; `fe78914a` feat; `3fc6e9e2` docs; `c24beca5` archive; `53c6d18f` ledger | recorded |
| Pull Request | #1532 | open (draft) |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Closed issue still has an active change | `workspace/tests/test_openspec_archive_hygiene.py::test_closed_issue_still_active_fails` | covered |
| Archived after merge | `…::test_archived_after_merge_passes` | covered |
| Open issue WIP stays active | `…::test_open_issue_wip_stays_active` | covered |
| Unticked implementation task fails the gate | `…::test_unticked_implementation_task_fails_the_gate` | covered |
| Implementation groups complete | `…::test_implementation_groups_complete` | covered |
| Exception label bypass | `…::test_exception_label_bypass` | covered |
| Merge-gate document names archive verification | `…::test_merge_gate_document_names_archive_verification` | covered |
| Missing instruction fails the doc guard | `…::test_missing_instruction_fails_the_doc_guard` | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | yes | pending |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | yes | pending |
| `docs/openspec/specs/ai-sdlc-enforcement/spec.md` | yes | pending |
| `CHANGELOG.md` | no — held for this PR (foreman serialize) | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec validate --strict` + `validate-sdlc-plan.sh` exit 0 (#1520 / apply) |
| 2 | Failing tests written, test cases designed | yes | `/opt/cursor/artifacts/1252-tdd-red.log` then green suite |
| 3 | Suite green, coverage held, docs updated | yes | unittest + plan validate; no Java; CHANGELOG held |
| 4 | CI green, review approved, no conflicts | no | pending PR |
| 5 | Deployed, smoke test passed, Issue closed | no | pending |

## Related / Refs

- Assessment umbrella #1242; finding row in `docs/300-development/ASSESSMENT-2026-10.md`
- Gate 1 pack: PR #1520 @ `cfc59ea7`
- Prior archive hygiene under #1445 process PRs (pattern only; do not invent Owner decisions)
- TPF DevSecOps-from-QA practices adapted in `docs/300-development/INGENIERIA.md`
- CHANGELOG held: #1518 already merged; this PR still omits Unreleased per foreman "may hold CHANGELOG"

## Planned Files

| Path | Role |
|------|------|
| `workspace/sdlc/check-openspec-archive-hygiene.py` | process gate |
| `workspace/tests/test_openspec_archive_hygiene.py` | Gate 2 tests |
| `.github/workflows/sdlc-process.yml` | wire checker |
| `workspace/sdlc/preflight.sh` | local mirror |
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | agent instructions |
| `docs/openspec/changes/archive/2026-10-11-*` | reconciled shipped trees |
| `docs/openspec/specs/ai-sdlc-enforcement/spec.md` | synced requirements |

## Apply inventory (2026-10-11)

Archived (CLOSED issue leftovers + synced deltas):

- `chore-1413-local-tools-no-login` (#1413)
- `feat-1361-identification-type-catalog` (#1361)
- `feat-1361-person-identification-type` (#1361)
- `feat-1468-homer-all-services` (#1468)
- `feat-773-documentos-tramite` (#773)
- `feat-773-required-docs-on-confirm` (#773)

Left active (open WIP / not CLOSED leftovers):

- `gestion-archive-deuda-check` — orphan schema (`spec-driven`), issue #169 still OPEN; skipped by `validate-sdlc-plan.sh`; not force-archived
- Remaining `fix-*` / docs stockpile with OPEN issues

## Exceptions

- Cloud branch suffix `-69d3` per fleet assignment (Constitution default remains
  `<type>/<issue>_<description>` for local agents).
- `in-progress` label: GraphQL 403 for integration token on `gh issue edit 1252 --add-label "in-progress"`.
- `CHANGELOG.md` intentionally untouched for this PR (foreman serialize hold).
- Delta `specs/ai-sdlc-enforcement/spec.md` ADDED requirements already synced into
  main; later `openspec archive` of this change should use `--skip-specs` if the
  CLI refuses duplicate headers.
