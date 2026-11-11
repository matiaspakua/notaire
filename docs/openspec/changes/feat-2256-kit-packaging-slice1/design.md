> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`install` copies `KIT_ROOT`, derived as two levels above `lifecycle.py`; inside a wheel that is site-packages, so install-from-wheel needs the kit data resolved from the package.

## Goals / Non-Goals

**Goals:** Pinnable, installable kit; skew visible in doctor; no new runtime dependency.
**Non-Goals:** Publishing, provenance, release automation, conformance matrix, second adopter.

## Decisions

Kit data is copied into `aisdlc/_kit/` at build time by a `build_py` hook (setuptools only, no network) so the source layout and every existing path stay unchanged. `kit_root()` prefers `aisdlc/_kit` and falls back to the source tree. Skew: major mismatch WARN, otherwise INFO; never an error.

## Riesgos / Trade-offs

`framework/ai-sdlc/**` is an owner-only path, so the Owner merges. Build hook adds a small amount of packaging code. Wheel contents drift from the tree: guarded by the packaging test.

## Testing Strategy

`tests/test_packaging.py` builds wheel and sdist offline with setuptools (`pip wheel --no-deps --no-build-isolation`; `python -m build` is not available offline), installs into a temp venv, runs `aisdlc --version`, `aisdlc install` in a temp git repo and `aisdlc doctor`; skipped with a reason when venv or setuptools is unavailable. `tests/test_kit_version.py` covers skew.

## Regression Strategy

Kit independence and stdlib-only tests stay green; existing install/update/uninstall tests pass from the source tree.

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
