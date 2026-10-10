# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1260 | open |
| Parent | #1197 Phase 0.5 | open |
| Related | OpenAPI contract CI, #1257 path-scoped CI | — |
| Use Case | CU76 | linked |
| Specification | `docs/openspec/changes/feat-1260-openapi-generated-types/` | Gate 1 |
| Branch | `cursor/feat-1260-openapi-ts-types-cf98` | created |
| Tasks | `tasks.md` | in progress |
| Pull Request | pending | — |

## Requirement coverage

| Scenario (Acceptance Criterion) | Verification | Status |
|---------------------------------|--------------|--------|
| One-command regen | `npm run openapi:types` | pending |
| CI fails on drift | `openapi:types:check` in frontend-ci | pending |
| Renamed field breaks tsc | hooks import `@/types/api` aliases | pending |
| Dashboard/gestiones/documentos/presupuestos use generated types | hooks + Vitest | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| FRONTEND-OPENAPI-TYPES.md | pending | this PR |
| frontend/README.md | pending | this PR |
| CHANGELOG.md | pending | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + AC | pending | this change |
| 2 | Vitest + typecheck | pending | |
| 3 | Docs | pending | |
| 4 | CI / PR | pending | |
| 5 | Issue close | pending | |

## Exceptions

- Issue label `in-progress` may 403 — sync via PR body + `Closes #1260`.
- Remaining hand-written types in `index.ts` stay until later migration slices.
