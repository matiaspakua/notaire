> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Measured in a fresh Claude Code cloud session: JDK 21, no `openspec`, `kustomize`, `trivy`; `LC_ALL` unset (Spotless fails on non-ASCII file names). `.cursor/install.sh` solves this for Cursor Cloud only and assumes sudo, apt and Docker.

## Goals / Non-Goals

**Goals:** a green start for backend build, Gate 1 tooling, Spotless and the SCA gate.
**Non-Goals:** Docker, Playwright browsers (preinstalled), replacing `.cursor/install.sh`.

## Decisions

1. **User-space installs from release tarballs** into `$HOME/.cache/notaire-tools`, no sudo or apt: the cloud sandbox may lack both.
2. **Pins are read, not duplicated by hand:** the guard test compares the hook's pins with the files that own them, so a bump in one place fails the test in the other.
3. **Hook always exits 0.** A missing tool degrades one gate; blocking session start would block everything.
4. **Env through `CLAUDE_ENV_FILE`** so every later Bash call sees the tools without per-command exports.

## Riesgos / Trade-offs

- [Downloads slow session start on first run] → cached; later sessions reuse the cache dir if the container persists.
- [Upstream URL layout changes] → the warning names the tool; the guard test pins versions, not URLs.

## Testing Strategy

`workspace/tests/test_cloud_toolchain_bootstrap.py`: registration, pin equality, no-op outside cloud, exit 0 with a failing downloader (stub `curl`), env file content (stub tools pre-seeded in the cache).

## Regression Strategy

`python3 -m unittest discover -s workspace/tests`, `check-agent-rules.sh`, context budget check.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Active on the next Claude Code cloud session.

## Rollback Strategy

Revert the PR; remove the settings entry.

## Migration Plan

None.

## Open Questions

None.
