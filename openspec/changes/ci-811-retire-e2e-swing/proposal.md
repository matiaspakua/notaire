# Retire Swing E2E CI leftovers and stale docs

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #811 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/ci-811-retire-e2e-swing-69d3` |
| Gate 1 status | passed |

## Objetivo

Issue #811 still tracks a Swing Robot E2E CI path after ADR-012 retired
`e2e-swing.yml` and #1046/#1083 removed the Swing modules. Leftover Robot suites
under `testing/e2e-swing/` and live docs that teach building or running Swing E2E
mislead operators and risk a workflow being reintroduced. Close #811 by making
retirement durable (hygiene + docs) without rebuilding Swing.

## What Changes

- Extend Dependabot/repo hygiene so CI fails if `.github/workflows/e2e-swing.yml`
  returns or any workflow text builds Swing (`-pl frontend-swing` /
  `deprecated-frontend-swing`).
- Hard-deprecate `testing/e2e-swing/` in place (README forbidding CI wiring;
  keep assets so existing ignore-rule hygiene stays valid).
- Clean live docs (`301-setup`, `303-testing`, `DEVELOPMENT-PLAN`, related
  testing scripts) that still teach Swing build/run/E2E.
- Update CU76 pointer and `CHANGELOG.md` for #811 closure.
- ADR-012 already records Swing E2E retirement — prose touch only if needed for
  #811 closure (link #811).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Swing desktop E2E is retired; do not rebuild a standalone Swing client or rewire Robot into CI | CU76, ADR-012, ADR-005 | Made explicit (#811) |
| GitHub Actions must not build `frontend-swing` or `deprecated-frontend-swing` | CU76 / CI integrity | New (hygiene) |
| Live operator docs must not teach Swing E2E as a supported path | CU76 | Changed |

## Capabilities

### New Capabilities

- `swing-e2e-retirement`: durable retirement of Swing E2E CI leftovers — workflow
  absence, no Swing Maven builds in workflows, hard-deprecated Robot suite,
  live docs aligned.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | Already absent; must not be recreated |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | Hygiene asserts absence of `e2e-swing.yml` and Swing builds; no new workflow |
| `testing/e2e-swing/` | yes | Hard-deprecate in place |
| `docs/300-development/` | yes | Remove Swing E2E run/build instructions from live docs |
| `scripts/` | yes | Extend `test_dependabot_hygiene.py` (+ discover wrapper) |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (Robot suite not deleted; CI must not invoke it)

### Architecture review

Follows ADR-005 (Next.js is the active client) and ADR-012 (Swing E2E workflow
retired). No new ADR — decision is RETIRE, already recorded.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Add #811 retirement pointer |
| `docs/300-development/303-testing/README.md` | Remove Swing E2E as runnable suite; note retired |
| `docs/300-development/301-setup/README.md` | Remove `deprecated-frontend-swing/` as present module; clarify absence |
| `docs/300-development/DEVELOPMENT-PLAN.md` | Fix stale Swing history wording |
| `testing/e2e-swing/README.md` | Hard-deprecation notice (new) |
| `docs/200-architecture/202-ADR/ADR-012-ci-cd-pipeline.md` | Optional: cite #811 next to #1083 retirement |
| `CHANGELOG.md` | Entry under `[Unreleased]` for #811 |
| Historical `docs/000-archive/` | Link fixes only if broken; no rewrite |

## Out of Scope

- Recreating `frontend-swing` / `deprecated-frontend-swing`
- Migrating Robot Framework suites to Playwright
- Deleting historical archive docs that mention Swing
- Changing Playwright E2E or Bruno suites
