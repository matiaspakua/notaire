> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Issue #554 added BCrypt with transparent MD5 upgrade-on-login. Sonar overall still flags MessageDigest MD5 in UserController as CRITICAL S4790. #1760 live inventory classified it FIX as #1762.

## Goals / Non-Goals

**Goals:** Remove MD5 verify path; migrate seed; keep admin/admin working via BCrypt; English-only artifacts; CHANGELOG-free under #1763.
**Non-Goals:** Hexagon users/roles rewrite (#1763); inventory refresh (#1764); password reset UX for arbitrary leftover MD5 rows.

## Decisions

1. Hard cutoff (reject non-BCrypt) over keeping upgrade-on-login — removes the Sonar finding at the source.
2. V52 updates only the known V2 MD5 hex; historical V2 script text stays for FlywayMigrationScriptTest.
3. Omit CHANGELOG to avoid conflict with open hex #1763.

## Riesgos / Trade-offs

Any non-seed account still on MD5 loses login until password reset. #1763 may reintroduce MD5 in PasswordHasherAdapter — must drop that path when it merges.

## Testing Strategy

TDD: invert UserControllerHashTest (RED while MD5 upgrade exists, GREEN after removal). AdditionalControllersTest uses BCrypt + encoder stub. FlywayMigrationScriptTest asserts V52 content. Database V&V asserts admin password LIKE `$2%`.

## Regression Strategy

Existing BCrypt login success and wrong-password rejection tests stay green. V2 historical MD5 string assertion remains.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
