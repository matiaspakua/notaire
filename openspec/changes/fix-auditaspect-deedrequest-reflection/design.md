> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

PR #1124 changed `DeedController.create` / `update` from `@RequestBody Deed` to
`@Valid @RequestBody DeedRequest`. `AuditAspectTest` still called
`getDeclaredMethod("create", Deed.class)` / `update(Integer, Deed.class)`, causing
five `NoSuchMethodException` errors and red Unit Tests on `main`.

## Goals / Non-Goals

**Goals:**

- Align AuditAspectTest reflection with current DeedController signatures.
- Restore green `mvn test -pl backend-api -Dtest=AuditAspectTest`.
- Satisfy Process Checks via an OpenSpec change folder.

**Non-Goals:**

- Fixing the broader #1124 integration-test fallout (other worker).
- Changing production audit or controller code.
- Using the human-only `sdlc-exception` label.

## Decisions

1. **Class.forName for nested DeedRequest** — same package-private nested-record
   pattern already used for `UserController$UserRequest` in this test class.
2. **Thin PR scope** — only `AuditAspectTest` + OpenSpec folder; no integration suite.

## Riesgos / Trade-offs

- [Nested type rename later] → Same fragility as UserRequest path; acceptable for
  unit reflection helpers.
- [Other controllers still broken in integration] → Explicitly out of scope.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Create mutation reflection resolves | unit | `AuditAspectTest` |
| Update mutation reflection resolves | unit | `AuditAspectTest` |
| Skip-path create lookups still resolve | unit | `AuditAspectTest` |

- New unit tests: none — existing five tests become green again
- New integration tests: n/a
- Coverage impact (JaCoCo): unchanged (test-only)

## Regression Strategy

- Existing tests affected: `AuditAspectTest` only
- Full suite command: `mvn test -pl backend-api -Dtest=AuditAspectTest`
- HTTP/Bruno API suite: n/a for this PR
- Legacy paths at risk: none in production

## Playwright Strategy

n/a — no UI surface. Backend unit test only.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge via PR; no runtime behavior change
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): Unit Tests job green on `main`

## Rollback Strategy

- Revert safe: yes — revert the PR; Unit Tests may go red again until a better fix
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: none (test-only)
