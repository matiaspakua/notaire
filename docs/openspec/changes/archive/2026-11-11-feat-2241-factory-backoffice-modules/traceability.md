# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2241 | open (WIP label SKIPPED if issues:write 403) |
| Use Case | CU76 — factory control | exists |
| Specification | `docs/openspec/changes/feat-2241-factory-backoffice-modules/` | drafted |
| Branch | `feat/2241_factory-backoffice-modules` | created |
| Tasks | `tasks.md` | 0/N complete (Gate 1 filled) |
| Commits | `fd52256f` Gate 1; `073a7722` feat Phase C | recorded |
| Pull Request | pending push | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| nav lists Phase C catalog modules | `tests/test_backoffice_phase_c.py` | pending |
| list and detail for mcp and providers | `tests/test_backoffice_phase_c.py` | pending |
| create and edit allocate versions for knowledge | `tests/test_backoffice_phase_c.py` | pending |
| runs monitor shows evidence summary | `tests/test_backoffice_phase_c.py` | pending |
| cancel and retry honestly empty when unsupported | `tests/test_backoffice_phase_c.py` | pending |
| runs mutations rejected | `tests/test_backoffice_phase_c.py` | pending |
| provider secrets redacted | `tests/test_backoffice_phase_c.py` | pending |
| shell has right nav and kit brand | `tests/test_backoffice_html.py` / phase_c | pending |
| no second OpenAPI write tree in Phase C | `tests/test_contract.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | pending | — |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | pending | — |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md` | pending | — |
| `docs/openspec/specs/factory-backoffice-ui/spec.md` | pending (on archive) | — |
| `docs/openspec/specs/factory-contract/spec.md` | pending (on archive) | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | validate-sdlc-plan.sh |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Gate 1.6 WIP label SKIPPED when `addLabelsToLabelable` / issues:write 403 (FLEET-IDENTITY).
A.2 `#2253` / A.3 `#2254` tips not stolen — Phase C ships kit HTML adapters over CLI/ports.
A.4 `#2255` not implemented — runs monitor is evidence-derived with honest empty cancel/retry.
