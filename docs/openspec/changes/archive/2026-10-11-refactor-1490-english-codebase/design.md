> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

See proposal.md — Objetivo. Epic #973 closed the entity and table rename and deliberately left some foreign-key columns, `identificaciones`, workflow column names, and the Spanish HTTP paths. Those leftovers are the surface this change finishes. CU75 requires the rename to be a new Flyway version.

## Goals / Non-Goals

**Goals:**

- One English path for each public resource and dashboard screen.
- JPA `@Column` / `@JoinColumn` / `@Table` names match the database after `V45`.
- Spanish type names in live Java become English without changing behavior.
- In-repo clients and the CU-API matrix move with the paths.

**Non-Goals:**

- Translating business-document prose, archived trees, or `es.json` values.
- Changing stored catalog and status strings.
- Keeping Spanish paths as aliases. An alias would leave the Spanish surface in the source.

## Decisions

1. **Replace path segments, not prose.** A longest-first replacement with a trailing boundary (`(?![A-Za-z0-9_-])`) rewrites `/api/v1/...` and `/dashboard/...` without turning `/folios` into `/folioss` or rewriting changelog history. Historical Flyway files, `docs/000-archive`, `deprecated/`, and `CHANGELOG.md` are excluded; the changelog gets a new `[Unreleased]` entry instead.
2. **New migration only.** `V45__rename_remaining_spanish_identifiers.sql` uses `ALTER TABLE ... RENAME COLUMN` / `RENAME TO`. PostgreSQL keeps constraints and indexes attached to the renamed columns, including `uq_people_identification_type_number` and the workflow node check.
3. **Breaking HTTP change is accepted under #1490.** The frontend, Bruno, and Playwright in this pull request are the working clients. `backend-api/openapi/accepted-breaking-changes.d/1490-english-http-surface.txt` lists the `oasdiff` lines after the spec is regenerated.
4. **Message keys stay.** `labelKey: "gestiones"` still matches `es.json` / `en.json`. The href next to it becomes `/dashboard/managements`. Translating the keys would be a second catalog migration with no user-visible English gain.
5. **Branch name.** Cloud agent policy requires `cursor/english-codebase-01e0`. Traceability records that name. The constitution pattern `refactor/1490_english_codebase` is not used because this run cannot push a branch outside the `cursor/` prefix.

## Riesgos / Trade-offs

- [Risk] External scripts still call `/api/v1/usuarios` or `/api/v1/gestiones` → Mitigation: CHANGELOG breaking entry and the accepted-breaking file. No alias, so the break is visible.
- [Risk] A path replacement rewrites a non-path occurrence → Mitigation: leading slash plus trailing boundary, and an exclusion list. The ratchet test scans controller mappings after the edit.
- [Risk] H2 tests build the schema from entities while Testcontainers apply Flyway → Mitigation: annotations and `V45` use the same names; `data.sql` and `cleanup-test-data.sql` follow the annotations.
- [Risk] `V40`'s unique index still names the old column in its historical script → Mitigation: `RENAME COLUMN` updates the index expression. The integration test that queries the column is updated to the new name.
- [Risk] OpenAPI drift → Mitigation: path rewrite of `openapi.yaml` plus `accepted-breaking-changes.d`, then `export-openapi.sh` when the app can boot.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Spanish resource segment is not mapped | unit | `EnglishSurfaceRatchetTest` |
| Navigation hrefs do not use Spanish segments | unit | `EnglishSurfaceRatchetTest` reads `dashboard-nav.ts` |
| Identifications table is English | unit | `EnglishSurfaceRatchetTest` scans `@Table` / `@Column` |
| Users login path is English | unit | `EnglishSurfaceRatchetTest` plus existing `UserController` login tests after the path rewrite |
| Spanish catalog is still the Spanish locale | unit | existing `frontend/src/tests/unit/i18n.test.ts` (unchanged catalogs) |
| Applied migrations are immutable | review | `git diff origin/main -- backend-api/src/main/resources/db/migration/V{1..44}*` empty |

- New unit tests: `backend-api/src/test/java/com/licensis/notaire/unit/EnglishSurfaceRatchetTest.java`.
- New integration tests: none. Existing Testcontainers tests exercise `V45` when they boot Flyway.
- Coverage impact: ratchet is a source scan, so JaCoCo line coverage of production code is unchanged. The floor must still hold.

TDD: `EnglishSurfaceRatchetTest` is written and run first. It fails while Spanish mappings and column names are still present. The rename is applied only after that failure is observed.

## Regression Strategy

- Existing tests affected: every test, Bruno file, and Playwright spec that embeds a Spanish path; `UseCaseRouteCatalog`; `SecurityAndCorsConfig` matchers; `CU-API-MATRIX.csv`; `validate-cu-api-matrix.py` required bases and `workspace/tests/test_validate_cu_api_matrix.py`; `PersonIdentificationUniquenessPgIntegrationTest`; `data.sql`; `cleanup-test-data.sql`.
- Assertions move to the English path. They are not weakened: a call that expected 200 on `/api/v1/gestiones` expects 200 on `/api/v1/managements`.
- Full suite command: `mvn test -pl backend-api`
- HTTP/Bruno API suite: `bash integration-test/scripts/test.sh` when that script is present; otherwise Bruno under `backend-api/api-test`.
- Legacy paths at risk: `jpa` controllers whose exception text still says `tramite` or `escritura`. Those strings are not the HTTP contract. They are translated when the ratchet or compiler touches the file.

## Playwright Strategy

- Specs to update under `testing/e2e/tests/`: every spec whose URL or `page.route` uses a Spanish API or dashboard segment. The codemod rewrites those strings.
- Golden path covered: login (`/api/v1/users/login`) and the people and managements screens at their English routes.
- Edge / error paths covered: existing specs keep their assertions; only the route and API strings change.
- Viewports: 320px / 768px / 1024px on the people screen and the dashboard navigation after the route move.
- Command: `cd testing/e2e && npx playwright test`

## Deployment Strategy

- Flyway migration required: yes, `V45__rename_remaining_spanish_identifiers.sql`.
- Deployment order / coupling: the migration and the new application image ship together. The old image must not run against a database that has already applied `V45`, and the new image must not run against a database that has not. Deploy them as one release.
- Configuration or `.env` keys to add: none.
- Feature flag: no.
- Smoke test after deploy (Gate 5): `GET /actuator/health` and `POST /api/v1/users/login`.

## Rollback Strategy

- Revert safe: only before `V45` is applied. After it is applied, a code revert without a database rollback fails to boot because Hibernate expects the new names.
- Database rollback: forward-fix only in production. A paired `R45` script is added so a DBA can rename the identifiers back if the release is rolled back before new writes depend on the names. The data itself is unchanged by the rename, so the down script is a reverse `RENAME`.
- Data written under the new behavior after revert: row values are identical; only identifier names differ. Reversing the renames restores the old names with the same rows.
- Blast radius if rollback is delayed: the API and the UI 404 on the old Spanish paths for every client that was not updated.

## Migration Plan

1. Observe `EnglishSurfaceRatchetTest` fail on `main`'s Spanish mappings.
2. Apply the path codemod outside the exclusion list, then move the dashboard folders.
3. Add `V45` and update the JPA names, `data.sql`, and the Postgres uniqueness test.
4. Rename the Spanish Java types and update imports.
5. Refresh the OpenAPI artifact and the accepted-breaking file from `oasdiff` output.
6. Run the ratchet, the matrix validator, and the backend unit tests that mention the renamed paths.

## Open Questions

None. The catalogs and the stored vocabulary stay, as recorded in the proposal.
