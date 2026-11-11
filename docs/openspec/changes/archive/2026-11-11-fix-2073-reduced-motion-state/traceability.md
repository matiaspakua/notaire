# Traceability

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md) — P4 Traceability.
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
| Specification | `docs/openspec/changes/archive/2026-11-11-fix-2073-reduced-motion-state/` | archived |
| Branch | `fix/2073_reduced-motion-state` | created |
| Tasks | `tasks.md` | groups 1–8 complete |
| Commits | `e89f9703` · `6d51d664` · `8818c57c` | recorded |
| Pull Request | blocked — ambient token 403 createPullRequest; ManagePullRequest forge SoT ENOENT | pending Owner |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| State transitions use duration.fast | `motion-tokens.test.ts` | passing |
| Transform travel is removed | `motion-tokens.test.ts` | passing |
| Login seal has no transform under reduced motion | `login-page.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | `6d51d664` |
| `frontend/src/theme/README.md` | yes | `6d51d664` |
| `docs/openspec/specs/ui-reduced-motion/spec.md` | yes | archive commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | archived change; `validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | yes | `foreman-ops-asli-66/tdd-red.txt` |
| 3 | Suite green, coverage held, docs updated | yes | `preflight.sh --full` PASSED (41) — `foreman-ops-asli-66/preflight-full-final.txt` |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

Gate 1.6: `add-issue-wip-label.sh 2073` printed honest SKIPPED (403
`addLabelsToLabelable` / issues write). Continued without faking in-progress.
