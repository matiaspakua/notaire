> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`ErrorResponses.build` already chooses WARN for non-5xx and ERROR for 5xx. `GlobalExceptionHandler.handleNotaireException` always calls `StructuredLogger.logError`, so hexagon adapters that rethrow as `NotaireException(409, …)` (identification types) pollute ERROR during Bruno. Several controllers still `log.error` inside catch blocks that return 409.

## Goals / Non-Goals

**Goals:**

- Align NotaireException logging with ErrorResponses severity.
- Downgrade FK-conflict delete logs that answer 409 to WARN.
- Prune merged accepted-breaking `.d` files on main.

**Non-Goals:**

- Changing HTTP status codes or response bodies.
- Full #579 controller migration to GlobalExceptionHandler.
- OTEL collector wiring for local compose.

## Decisions

1. **Severity by HTTP status, not exception type** — `statusCode >= 500` → ERROR; else WARN. Mirrors `ErrorResponses` and keeps 404/`BusinessValidationException` handlers consistent.
2. **Minimal controller touch** — only delete catch blocks that triage showed as ERROR during Bruno (`ManagementController` and siblings that share the same `log.error` + 409 pattern if they fire in the suite). Prefer WARN with the exception attached for diagnosis.
3. **Prune via checker** — `check-accepted-breaking-changes.py … --prune` deletes the four merged files; no OpenAPI yaml regen.

## Riesgos / Trade-offs

- [Risk] Operators lose ERROR alerts for genuine client abuse → Mitigation: WARN still carries stack/context; 5xx remains ERROR; metrics/alerts should key on status code.
- [Risk] Tests that assert log.error on 409 break → Mitigation: update those tests; they encoded the bug.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| 409 NotaireException logs WARN | unit | `GlobalExceptionHandlerLogLevelTest` |
| 500 NotaireException logs ERROR | unit | `GlobalExceptionHandlerLogLevelTest` |
| Management delete FK conflict logs WARN | unit | `ManagementControllerDeleteLogTest` (or extend existing controller test with ListAppender) |

- New unit tests (`src/test/java/.../config/` and adapter web): ListAppender / Mockito on logger where project already does
- New integration tests: none required (HTTP contract unchanged)
- Coverage impact: negligible; ratchet floor held

## Regression Strategy

- Existing tests affected: any that stub StructuredLogger expecting ERROR for 4xx — update.
- Full suite command: `mvn test -pl backend-api -Dtest=GlobalExceptionHandler*`
- HTTP/Bruno API suite: `bru run` against local stack (status codes unchanged)
- Legacy paths at risk: none

## Playwright Strategy

- n/a - no UI surface (log severity and OpenAPI ignore-file hygiene only)

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: code-only
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): Bruno identification-types duplicate/delete-in-use + managements delete; confirm no ERROR for those paths in backend logs

## Rollback Strategy

- Revert safe: yes (behavior for clients unchanged)
- Database rollback: none needed
- Data written under the new behavior after revert: n/a
- Blast radius if rollback is delayed: log noise only

## Migration Plan

1. Failing unit tests for log levels.
2. Implement handler + delete WARN.
3. Prune accepted-breaking files.
4. Bruno smoke + CHANGELOG.

## Open Questions

(none)
