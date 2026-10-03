# Implement #799 status

| Field | Value |
|-------|-------|
| Issue | #799 |
| Branch | `cursor/fix-799-persona-id-uniqueness-69d3` |
| Head SHA | `67aa18369c89482c2934089b22e986dfa57052ab` |
| PR | https://github.com/matiaspakua/notaire/pull/1202 (draft) |
| OpenSpec | `openspec/changes/fix-799-persona-id-uniqueness/` |
| Base | `origin/main` @ `31caed44` |
| Store OpenSpec copy | not mounted (`/cursor/stores/self` missing) |

## Delivered

- Flyway `V40__unique_people_identification_type_number.sql`: fail-fast duplicate pre-check + unique index on `(fk_id_tipo_identificacion, identification_number)`.
- JPA `@UniqueConstraint` on `Person`; race `DataIntegrityViolationException` → `DuplicatePersonException` (HTTP 409 / `existingPersonId`).
- TDD: H2 uniqueness IT + pg-integration IT (index, JDBC reject, pre-check) + unit race mapping.
- Docs: CU17, CU18, CHANGELOG, main + delta `persona-validacion-duplicados`.
- Unrelated #804 fallout: `shouldReturnLaterHistoryRowWhenDatesTie` now seeds History directly (same pattern as sibling test).

## Test results

| Command | Result |
|---------|--------|
| TDD red uniqueness IT | FAIL as expected (second insert not rejected) |
| TDD red race unit | FAIL as expected (`DataIntegrityViolationException`) |
| `PersonIdentificationUniquenessIntegrationTest` | 1/1 pass |
| `PersonServiceTest` | 21/21 pass |
| `mvn test -Ppg-integration -Dtest=PersonIdentificationUniquenessPgIntegrationTest` | 3/3 pass |
| `mvn verify -pl backend-api` | **BUILD SUCCESS** — 1973 tests, 0 failures |
| JaCoCo | instruction 85.04%, branch 73.64%, line 85.50% |
| `bash scripts/validate-sdlc-plan.sh fix-799-persona-id-uniqueness` | pass |
| Bruno | skipped (no API contract change; #835 409 path preserved) |
| Playwright | n/a — no UI surface |

## Commits

1. `94f18c22` docs(openspec): Gate 1 plan for person ID uniqueness (#799)
2. `f52a4b90` fix(db): unique index on people identification type+number
3. `56d3bbbd` docs: CU17/CU18 and CHANGELOG for DB person ID uniqueness
4. `67aa1836` test: seed History for estado-actual tie-break after #804

## Coordinator next

- Do **not** merge from this worker.
- Run `bash scripts/check-heavy-ci.sh 1202` and merge when exit 0.
