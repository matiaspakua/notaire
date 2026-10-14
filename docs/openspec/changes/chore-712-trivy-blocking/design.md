> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`ignore-unfixed` keeps findings without a published fix out of the gate, so the build only fails on something that can be fixed by an update.

## Goals / Non-Goals

**Goals:** a dependency CVE gate that fails the build.
**Non-Goals:** the image scan; suppressions.

## Decisions

1. **Source scan only.** The image scan cannot be run without Docker here and base-image findings are outside the repo's control; it stays report-only.
2. **Keep `ignore-unfixed`.** The gate stays actionable.
3. **`if: always()` on the report steps** so a failing scan still publishes its JSON.

## Riesgos / Trade-offs

- [A new CVE turns unrelated PRs red] → Dependabot and the severity SLA in `security/policy`; a justified `.trivyignore` entry is the escape hatch.

## Testing Strategy

`test_trivy_blocking.py`, written first and failing; `security/run_gates.py --profile local` passes with the new flags.

## Regression Strategy

`python3 -m unittest discover -s security/tests` and `workspace/tests`.

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
