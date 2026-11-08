> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2134 after OpenFeature #2137.

## Goals / Non-Goals

**Goals:** Minimal useful shared contract for CLI/HTTP parity.
**Non-Goals:** Hosting an API gateway; OAS 3.2.

## Decisions

Document-first OpenAPI under kit; no PyYAML dependency for check; catalogs path reserved for #2139.

## Riesgos / Trade-offs

Schema drift — config_keys_match_contract test guards required keys.

## Testing Strategy

test_contract.py; verify.sh

## Regression Strategy

config show; independence

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
