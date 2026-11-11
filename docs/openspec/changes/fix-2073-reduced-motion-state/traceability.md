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
| Issue | #2073 | open (Gate 1.6 SKIPPED — 403 labels) |
| Use Case | RNF-05 — Aspecto visual | exists |
| Specification | `docs/openspec/changes/fix-2073-reduced-motion-state/` | drafted |
| Branch | `fix/2073_reduced-motion-state` | created |
| Tasks | `tasks.md` | groups 1–2 complete |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| State transitions use duration.fast | `motion-tokens.test.ts` | pending |
| Transform travel is removed | `motion-tokens.test.ts` | pending |
| Login seal has no transform under reduced motion | `login-page.test.tsx` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | pending | pending |
| `frontend/src/theme/README.md` | pending | pending |
| `docs/openspec/specs/ui-reduced-motion/spec.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | `foreman-ops-asli-66/tdd-red.txt` |
| 3 | Suite green, coverage held, docs updated | pending | `preflight.sh --full` |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

Gate 1.6: `add-issue-wip-label.sh 2073` printed honest SKIPPED (403
`addLabelsToLabelable` / issues write). Continued without faking in-progress.
