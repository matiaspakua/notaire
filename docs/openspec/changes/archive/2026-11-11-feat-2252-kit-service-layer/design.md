> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Handlers in `workflows.py` and `catalogs.py` mix domain logic with printing; `add`/`update` return exit codes. The HTTP host in #2253 needs the same logic without text output.

## Goals / Non-Goals

**Goals:** One service layer, one error type with stable codes, zero CLI output change.
**Non-Goals:** Any HTTP code, new fields, new commands.

## Decisions

`KitError` subclasses `ProfileError` so unconverted raise sites and the global handler in `main` still work and messages stay identical. CLI maps `already_exists`/`not_found` on catalog add/update to exit 1 (as today) and everything else to 2. Code list: no_catalogs, unknown_kind, invalid_id, not_found, already_exists, revision_exists, invalid_entry, unknown_transition, illegal_transition, invalid_governance, invalid_body, refs_unresolved, waive_reason_required, approve_failed, reason_required, agent_approve_forbidden, signature_required, approve_fail_closed, immutable_approved.

## Riesgos / Trade-offs

Output drift. Mitigation: goldens for success and every error path, recorded from main before any change.

## Testing Strategy

`test_cli_goldens` (PRESERVE, 57 cases) and `test_service_layer` (service returns data, prints nothing, raises coded KitError).

## Regression Strategy

Existing `test_catalogs` and workflow tests stay green unchanged.

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
