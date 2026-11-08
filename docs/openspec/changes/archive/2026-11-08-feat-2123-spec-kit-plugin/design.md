> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2121. Follows #2122 harness port.

## Goals / Non-Goals

**Goals:** Working Spec Kit adapter; recorded API; out of allow on Notaire.
**Non-Goals:** Replace OpenSpec; install Spec Kit as the project method store.

## Decisions

trusted=false on Notaire; kit tests prove dry plan under allow; pin via uv tool install specify-cli==1.3.2.

## Riesgos / Trade-offs

specify requires a .specify project for live search; dry plan does not run it.

## Testing Strategy

test_spec_kit_plugin.py, test_spec_kit_adoption.py, framework/ai-sdlc/verify.sh

## Regression Strategy

OpenSpec and example-method plugins unchanged

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
