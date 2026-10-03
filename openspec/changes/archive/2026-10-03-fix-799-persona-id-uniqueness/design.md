> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

\#835 already validates duplicate type+number in `PersonService` and returns
HTTP 409 with `existingPersonId`. Table `people` (renamed from `personas` in
V25) still has no UNIQUE on
`(fk_id_tipo_identificacion, identification_number)` — tip migration on main
is V39. Column name remains Spanish `fk_id_tipo_identificacion` after leaf
renames; do not rename in this change.

## Goals / Non-Goals

**Goals:**

- Add Flyway unique index/constraint matching the #835 composite rule.
- Fail-fast (or agreed lowest-`id` cleanup) before creating the constraint.
- Prove DB rejection with TDD (red → green) via repository/JDBC insert.
- Optionally map race `DataIntegrityViolationException` → 409 for FE compatibility.
- Update CU17/CU18 docs and CHANGELOG.

**Non-Goals:**

- Rebuild #835 service/FE happy path.
- Rename FK column to English.
- Merge duplicate people as a product feature beyond migration pre-check/cleanup.

## Decisions

1. **Composite unique on `(fk_id_tipo_identificacion, identification_number)`** —
   matches #835; number-alone would wrongly collide across document types.
2. **Fail-fast pre-check in V40** — raise a clear exception listing that
   duplicate groups exist; seed data has a single person so fresh envs pass.
   Prefer fail-fast over silent cleanup unless an environment reports duplicates.
3. **Also declare `@UniqueConstraint` on `Person`** — keeps H2 ddl-auto tests
   aligned and documents intent in JPA.
4. **Race mapping in `PersonService.save`** — catch
   `DataIntegrityViolationException`, re-resolve existing person, throw
   `DuplicatePersonException` so controller/FE keep the #835 409 body.
5. **English in touched code** — translate Spanish comments/strings in files we
   touch; leave Spanish column names until a rename ADR.

## Riesgos / Trade-offs

- [Existing duplicate rows in a long-lived DB] → Migration fails with clear
  message; ops must dedupe (keep lowest `id`) before retry.
- [NULL `fk_id_tipo_identificacion`] → PostgreSQL treats NULLs as distinct in
  unique indexes; JPA marks type required — acceptable residual.
- [H2 vs Postgres constraint names] → Integration test asserts exception type,
  not a specific constraint name string alone.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Second insert with same type and number is rejected by the database | integration | `PersonIdentificationUniquenessIntegrationTest` |
| Migration refuses to proceed when duplicate groups exist | integration (pg) | `PersonIdentificationUniquenessPgIntegrationTest` or migration SQL unit via DO block exercised on empty DB + documented fail-fast |
| Concurrent create race still surfaces as HTTP 409 | integration / unit | `PersonServiceTest` or controller IT with forced integrity violation |
| Alta exitosa con documento no registrado | existing | `PersonServiceTest` / #835 coverage |
| Rechazo de alta con documento ya registrado | existing | `PersonServiceTest` / #835 coverage |

- New unit tests: race → `DuplicatePersonException` if mapping added
- New integration tests: `PersonIdentificationUniquenessIntegrationTest` (H2 +
  entity uniqueConstraints); optional `@Tag("pg-integration")` Flyway proof
- Coverage impact: small; ratchet floor held

## Regression Strategy

- Existing tests affected: `PersonServiceTest`, `PersonServiceIntegrationTest`,
  `PersonRepositoryIntegrationTest` — must still create unique numbers; no
  assertion weakening.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: confirm people create duplicate still 409 if Bruno covers it
- Legacy paths at risk: direct `jpa` inserts — constrained by DB after V40

## Playwright Strategy

- n/a — no UI surface. Confirm existing #835 / #945 persona duplicate toast
  specs remain the FE coverage; do not rebuild.

## Deployment Strategy

- Flyway migration required: yes (`V40__unique_people_identification_type_number.sql`)
- Deployment order / coupling: apply migration with backend deploy; code race
  mapping may ship in same deploy
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): create person A; second create with same
  type+number → 409; verify index exists in `pg_indexes`

## Rollback Strategy

- Revert safe: code revert yes; dropping the unique index is safe if duplicates
  must be re-allowed temporarily
- Database rollback: `DROP INDEX IF EXISTS uq_people_identification_type_number;`
  (or drop constraint) — forward-fix preferred over rewriting V40
- Data written under the new behavior after revert: none structural
- Blast radius if rollback is delayed: creates keep failing on duplicates (desired)

## Migration Plan

1. Ship V40 with fail-fast duplicate pre-check + unique index.
2. Deploy backend with optional race mapping.
3. Smoke-test 409 on duplicate create.
4. Archive OpenSpec change after merge.

## Open Questions

None — fail-fast chosen over silent cleanup for Gate 1.
