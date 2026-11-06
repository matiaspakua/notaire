> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Post-#2001 local preflight --full is the only product gate on PRs. Folio #579 tip is blocked by this flake after host midnight.

## Goals / Non-Goals

**Goals:** ['Docker Compose passes TZ into postgres, backend, and frontend', 'start.sh derives host IANA TZ when unset', 'TS-0016 CU23-GW01 passes on a Europe/Madrid host after local midnight']
**Non-Goals:** ['Redesigning audit timestamps to UTC-only API', 'Changing production Kustomize TZ defaults beyond compose local stack']

## Decisions

['Default `${TZ:-UTC}` keeps CI/Linux agents deterministic when no host zone is available', 'Derive from `/etc/localtime` symlink target under zoneinfo — works on macOS and typical Linux']

## Riesgos / Trade-offs

['Images without tzdata would ignore TZ — Alpine backend image already ships zoneinfo (verified)']

## Testing Strategy

['Unit/script: start.sh exports TZ from a fake localtime symlink in a temp dir (or document manual docker exec date check)', 'E2E: TS-0016 CU23-GW01 with stack started via start.sh / compose with TZ=Europe/Madrid']

## Regression Strategy

['docker compose config still validates', 'preflight docker smoke still ups and downs the stack']

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
