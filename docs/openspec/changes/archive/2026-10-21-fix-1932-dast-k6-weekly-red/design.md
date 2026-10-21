# Design — weekly DAST ZAP and k6 stay green

## Context

ZAP 10049 (*Non-Storable Content*, informational) fires on 404 JSON answers that carry
`Cache-Control: no-store`/no validators. For an API that must not be stored by shared caches
this is the intended behaviour. The artifact quota is account-wide and recovers in 6–12 h
(Pages artifact uploaded fine on 2026-10-21).

## Goals / Non-Goals

**Goals:** weekly ZAP and k6 red only for real findings. **Non-Goals:** quota management; ZAP authenticated scan.

## Decisions

- Ignore 10049 only, by id, with a reason; a test pins the reviewed set so a new IGNORE needs review.
- `continue-on-error: true` only on `actions/upload-artifact` steps; scan/test steps keep failing the job.

## Riesgos / Trade-offs

- A quota outage drops one week of report files; the job log and step summary keep the result.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| 10049 is allowlisted | unit | `test_rules_allowlist_ignores_non_storable_content_informational` |
| Every allowlist entry is reviewed and explained | unit | `test_every_allowlist_entry_is_reviewed_and_has_a_reason` |
| Report uploads tolerate the artifact quota | unit | `test_report_uploads_do_not_fail_a_scan_on_artifact_quota` |

## Regression Strategy

`security/tests/` (gate surface, ratchet) and `workspace/tests/test_dast_contract_backup_assets.py`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

- Flyway migration required: no
- Feature flag: no
- Effective on the next weekly schedule or a manual `workflow_dispatch`.

## Rollback Strategy

- Revert safe: yes
- Database rollback: none needed

## Migration Plan

1. Merge. 2. Dispatch `DAST — OWASP ZAP Baseline` and `Performance - k6 Load Test` once; both green.

## Open Questions

None.
