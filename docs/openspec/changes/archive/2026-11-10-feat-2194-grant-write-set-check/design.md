> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Grant profile schema (#2192/#2193) shipped validate/list/show. Remaining gaps call for Grant runtime lite = write-set check before Isolator OS. Mirror committer dry-run honesty.

## Goals / Non-Goals

**Goals:** grant check CLI with pass/fail/no-evaluable; doctor sandbox denial; Notaire plan-sample exercised in tests; docs honesty
**Non-Goals:** OS Isolator, expire/kill-switch, flipping committer.required, Approval/Forge write runtimes

## Decisions

1. Reuse repo.changed_files + write-set matching like scope.
2. Union of all non-empty Grant.write_set patterns.
3. Exit 1 on fail (check command); 0 on pass/no-evaluable; 2 on git/config error.
4. Doctor INFO line for grant policy check; Isolator stub unchanged.
5. Keep Notaire plan-sample Grant; never required=true.

## Riesgos / Trade-offs

Readers treat grant check as Isolator shipped → mitigated by JSON note, doctor INFO, ADR-004 language.

## Testing Strategy

| Scenario | Test |
| no-evaluable | GrantWriteSetCheckTest |
| pass in-set | GrantWriteSetCheckTest |
| fail out-of-set | GrantWriteSetCheckTest |
| doctor denies sandbox | GrantWriteSetCheckTest |

## Regression Strategy

Existing grant list/show/validate tests; isolator stub tests; Notaire doctor.

## Playwright Strategy

n/a — no UI surface (kit CLI only)

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
