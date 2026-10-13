> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`workspace/stack/start.sh` drives Docker. CI starts PostgreSQL as a service container and passes the backend flags inline. Playwright wants the `chrome` channel and a headless-shell revision that differ from the browsers preinstalled in the cloud image.

## Goals / Non-Goals

**Goals:** one command to a CI-equivalent stack; no Docker.
**Non-Goals:** observability stack; replacing CI.

## Decisions

1. **Same flags as CI, pinned by a test**, so a workflow change fails the guard instead of silently diverging.
2. **Own data dir and PID files** under `NOTAIRE_NATIVE_DIR`; `down` reads them, so it never kills unrelated processes.
3. **Run PostgreSQL as the `postgres` user when root**, as the Debian packages require; otherwise as the current user.
4. **Browser shims are symlinks created only when missing**, derived from Playwright's `browsers.json` revision.

## Riesgos / Trade-offs

- [Port 5432/8080/3000 already used] → `up` fails fast naming the port.
- [Shims mask a real browser mismatch] → they only point to an installed Chromium of the same family; failures still surface in tests.

## Testing Strategy

`workspace/tests/test_native_stack.py` (flags equal the workflow, usage, syntax, status). Written first. The script itself was proven by running the whole Playwright, Bruno and SQL-check suites through it.

## Regression Strategy

`python3 -m unittest discover -s workspace/tests`.

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
