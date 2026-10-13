> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`docker_run` skips when no daemon exists. The Claude Code cloud sandbox has none, and neither do plain runners, so the promtool/amtool checks never run there. The pinned binaries already accept the files (verified 2026-10-13: 13 rules OK, `amtool check-config` OK).

## Goals / Non-Goals

**Goals:** real validation without Docker. **Non-Goals:** running the stack.

## Decisions

1. **Prefer Docker, fall back to a local binary.** CI keeps the exact image; the fallback only fills a gap.
2. **The hook installs the binaries from the release tarballs at the image-tag versions.** The guard test compares versions so a compose bump fails the hook pin.
3. **One helper `tool_run` replaces `docker_run`** and returns the completed process, keeping call sites unchanged.

## Riesgos / Trade-offs

- [Local and image versions diverge] → pinned and compared by a test.

## Testing Strategy

`infra/tests/test_alertmanager.py` gains negative tests using temporary invalid files; `workspace/tests/test_cloud_toolchain_bootstrap.py` gains the version-equality test. Both written first.

## Regression Strategy

`python3 -m unittest discover -s infra/tests` and `workspace/tests`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Active in the next cloud session and CI run.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
