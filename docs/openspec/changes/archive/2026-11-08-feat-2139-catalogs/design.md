> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2134. Parallel to OpenAPI #2136.

## Goals / Non-Goals

**Goals:** Repo-local inventory/certify loop for factory catalogs.
**Non-Goals:** Marketplace; running MCP servers.

## Decisions

JSON + sha256 digest; optional [catalogs] dir; empty list when unset.

## Riesgos / Trade-offs

Digest bypass via hand edits — validate detects mismatch.

## Testing Strategy

test_catalogs.py; verify.sh; Notaire catalog list

## Regression Strategy

skills lint

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
