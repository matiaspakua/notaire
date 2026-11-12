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
| Issue | #2243 | open (WIP label SKIPPED — issues:write 403) |
| Use Case | CU76 — factory control | exists |
| Specification | `docs/openspec/changes/feat-2243-factory-adopter-demo/` | drafted |
| Branch | `cursor/2243_factory-adopter-demo-2731` | created |
| Tasks | `tasks.md` | 0/N complete (Gate 1 filled) |
| Commits | pending | pending |
| Pull Request | pending push | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| demo seed creates approved workflow on temp project | `tests/test_adopter_demo.py` | pending |
| from-stages pins Foreman agent/skill ids | `tests/test_adopter_demo.py` | pending |
| doctor / catalog validate / report green after seed | `tests/test_adopter_demo.py` | pending |
| kit independence holds (no adopter markers in kit) | `tests/test_independence.py` | pending |
| Notaire has ≥1 approved workflow with stage refs | `tests/test_notaire_adopter_demo.py` | pending |
| Notaire doctor stays clean | `tests/test_notaire_adopter_demo.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/docs/ADOPTER-DEMO.md` | pending | — |
| `framework/ai-sdlc/README.md` | pending | — |
| `framework/ai-sdlc/MODULE.md` | pending | — |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ADOPTER-DEMO.md` | pending | — |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md` | pending | — |
| `docs/openspec/specs/factory-adopter-demo/spec.md` | pending (on archive) | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | validate-sdlc-plan.sh |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green + review + merge-ready | pending | — |

## Decisions log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-11-12 | Kit-neutral `demo seed`; Notaire uses `--from-stages` | Keeps independence; matches Foreman refs |
| 2026-11-12 | Public wheel sample stays on #2256 | Board: packaging remainders separate from E |
| 2026-11-12 | Omit CHANGELOG Unreleased | Fence #2271 |

## Exceptions

Gate 1.6 WIP label SKIPPED when `addLabelsToLabelable` / issues:write 403
(FLEET-IDENTITY). Public second-adopter sample + conformance matrix remain
#2256 packaging remainders — Phase E proves a kit temp-project fixture instead.
CHANGELOG Unreleased omit while fence held (#2271). Do not contend with MFA
docs tip #2271. Leave `[committer] required=true` alone.
