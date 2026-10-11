> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6, §7, §12.

## Context

See [proposal.md](proposal.md). Failures reproduced on a clean checkout of `origin/main`.

## Goals / Non-Goals

**Goals:** every gate green on a clean `main`, each fixed at its root cause. **Non-Goals:** further #1490 cleanup, renaming the `reportes` i18n namespace.

## Decisions

1. **Anchor the ignore rule** (`/reports/`) instead of un-ignoring the page, so no other source directory named `reports` can be hidden. A guard test pins both sides.
2. **Recreate the page with a `ReportCard` component** over the English hooks, instead of restoring seven copies of the old markup.
3. **Amend V45 instead of adding V46.** V45 fails on every fresh database, so a later migration can never run there. Alternatives rejected: out-of-order inserts or a pre-migrate stub (hide the drift). Owner approved the §12 exception; databases that applied the old V45 run `flyway repair`.
4. **Reachability and baseline**: rename-only changes; no assertion weakened, no list grown.

## Riesgos / Trade-offs

- [Checksum change on databases that applied V45] -> documented in CHANGELOG, issue and PR.
- [Restored page differs in test ids from the Spanish original] -> the translated e2e tests no longer reference the old ids.

## Testing Strategy

| Scenario (spec) | Test level | Test |
|-----------------|------------|------|
| Reports source is tracked | process | `workspace/tests/test_gitignore_keeps_source_dirs.py` |
| Reports page renders and downloads | unit | `frontend/src/tests/unit/reports-page.test.tsx`, `dashboard-nav.test.ts` |
| Fresh database migrates | integration | `FlywaySchemaValidationIntegrationTest` (`-Ppg-integration`) |

- Also: `contracts/tests/test_api_reachability.py`, `ControllerSignatureArchitectureTest`.

## Regression Strategy

- `bash workspace/sdlc/preflight.sh`, `mvn verify -pl backend-api`, `cd frontend && npm test`.

## Playwright Strategy

The restored page is covered by the existing `TS-0020-reports-admin-workflow` (sidebar discovery) and TS-0043/0070/0071/0107 route sweeps, run in CI.

## Deployment Strategy

- Flyway: V45 amended (§12 exception). Databases that applied the original V45: `flyway repair` once.
- Smoke (Gate 5): fresh Docker stack starts; `/dashboard/reports` loads.

## Rollback Strategy

- Revert safe for code; reverting V45 restores the fresh-database failure.

## Migration Plan

1. Failing tests. 2. Fixes. 3. Full gates. 4. PR.
