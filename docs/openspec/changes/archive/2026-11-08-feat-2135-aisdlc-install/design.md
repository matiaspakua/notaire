> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2134. After TelemetryPort #2125.

## Goals / Non-Goals

**Goals:** One reversible lifecycle for adopting the kit.
**Non-Goals:** npm/pypi registry; relocating workspace/sdlc.

## Decisions

Default dest tools/ai-sdlc; manifest at .aisdlc/kit-install.toml; reuse adopt.init for scaffold.

## Riesgos / Trade-offs

Accidental purge of evidence — require explicit --purge.

## Testing Strategy

test_lifecycle.py; bash framework/ai-sdlc/verify.sh; Notaire doctor

## Regression Strategy

init/doctor/independence

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
