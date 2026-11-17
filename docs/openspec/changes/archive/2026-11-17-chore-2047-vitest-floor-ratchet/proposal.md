# chore: ratchet the Vitest statement floor and sync the documented floors

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2047 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2047-vitest-floor-ratchet-47dc` |
| Gate 1 status | passed |

## Objetivo

The enforced Vitest floors on `main` are statements 86 / branches 79 / functions 85 / lines 88, but `CONSTITUTION.md`, `TEST-PLAN.md` and the testing README still say 76 / 68 / 72 / 78. A fresh coverage run on 2026-11-17 has enough statement headroom to raise that one floor by one point. This change records the real floors and applies that ratchet.

## What Changes

- Raise the Vitest statement threshold from 86 to 87. Branches stay 79, functions stay 85, lines stay 88.
- Point the raise-only guard, `CONSTITUTION.md`, `TEST-PLAN.md`, the testing README and `.claude/rules/code-quality.md` at the same four numbers and at the 2026-11-17 measurement.
- Leave the Owner **>90%** target unchanged. This slice does not claim the pyramid is at 90%.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Vitest floors ratchet raise-only and stay at least two points under a fresh measurement | CU76; #2047; #2046 | Made explicit |
| Permanent docs name the floors the tool enforces, and name the >90% target separately | CU76; #2046 | Changed |

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `frontend-vitest-coverage-floor`: the enforced statement floor and the permanent-doc numbers move to the 2026-11-17 ratchet.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | |
| `frontend` | yes | Vitest thresholds and the guard test |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | Frontend CI already runs the same thresholds |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- Not **BREAKING** for API clients. A pull request whose frontend coverage falls under the new statement floor fails Vitest.

### Write set

- `frontend/vitest.config.ts`
- `frontend/src/tests/unit/vitest-coverage-thresholds.test.ts`
- `CONSTITUTION.md`
- `docs/300-development/303-testing/TEST-PLAN.md`
- `docs/300-development/303-testing/README.md`
- `.claude/rules/code-quality.md`
- `docs/openspec/changes/chore-2047-vitest-floor-ratchet/**`

### Architecture review

No architectural change. No ADR. The existing raise-only Vitest policy stays; only the statement floor and the documents that quote it move.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CONSTITUTION.md` | Vitest floor row: 87 / 79 / 85 / 88 |
| `docs/300-development/303-testing/TEST-PLAN.md` | Same four floors |
| `docs/300-development/303-testing/README.md` | Same four floors |
| `.claude/rules/code-quality.md` | Floor table and the 2026-11-17 measurement |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- Raising branches, functions or lines: the 2026-11-17 measurement does not leave two full points under a higher integer floor.
- JaCoCo, Bruno, Playwright, security and performance floors (#2048, #2046).
- Closing #2046 or #2047. The Owner target is still above the enforced floors.
- New product tests whose only purpose is to manufacture headroom.
