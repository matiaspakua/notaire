> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`.cursor/install.sh` already installs frontend Playwright chromium and apt packages
for Docker tooling, but omits `python3-venv` and never installs browsers from
`testing/e2e` or `github-page` (different Playwright majors / revisions). Preflight
`--full` then fails in `security/run_gates.py` (ensurepip) or Playwright
(chromium_headless_shell / WebKit for the e2e `mobile` project using iPhone SE).

## Goals / Non-Goals

**Goals:**

- Idempotent cloud install provides `python3 -m venv` without mid-run apt.
- Idempotent cloud install downloads the Playwright browser set used by
  `github-page` (chromium) and `testing/e2e` (chromium + webkit) from each package
  directory so revisions match that package's Playwright version.
- Checklist documents the contract.

**Non-Goals:**

- Changing product tests, Playwright configs, or security gate logic.
- Fixing OpenSpec CLI friction (#2218) or issue-label GraphQL failures (#2221).
- Setting Isolator committer required=true.

## Decisions

- Add `python3-venv` to the existing `apt-get install` block (same place as
  `postgresql-client` / `bc`) rather than a separate apt round-trip.
- Install e2e/pages deps with `npm ci` (fallback `npm install`) then
  `npx playwright install --with-deps …` inside each package directory so browser
  revision pins follow that package's lockfile / Playwright version.
- Keep frontend chromium install; do not remove it.
- Guard with a stdlib unittest under `workspace/tests/` (static presence), same
  pattern as other CU76 install contracts.

## Riesgos / Trade-offs

- [Larger install / snapshot time for WebKit + dual chromium revisions] → Accept;
  install runs once per environment build; cheaper than mid-preflight failure.
- [Playwright majors drift again] → Installing from each package directory tracks
  lockfiles; static test asserts install commands remain present.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| install.sh installs python3-venv | unit (static) | `workspace/tests/test_cloud_env_preflight_deps.py` |
| install.sh installs e2e chromium+webkit | unit (static) | `workspace/tests/test_cloud_env_preflight_deps.py` |
| install.sh installs github-page chromium | unit (static) | `workspace/tests/test_cloud_env_preflight_deps.py` |
| checklist documents venv + browsers | unit (static) | `workspace/tests/test_cloud_env_preflight_deps.py` |

- New unit tests (`src/test/java/.../unit/`): n/a
- New integration tests: n/a
- Coverage impact: n/a (workspace stdlib tests)

## Regression Strategy

- Existing tests affected: none expected beyond new static file.
- Full suite command: `bash workspace/sdlc/preflight.sh --full`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. This change only ensures browsers exist for existing suites.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge → next Cloud RECURRING / MANUAL env build runs tip `install.sh`
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `python3 -m venv /tmp/notaire-venv-smoke && rm -rf /tmp/notaire-venv-smoke`; playwright browsers present under `~/.cache/ms-playwright`

## Rollback Strategy

Revert the PR. Cloud installs lose venv/webkit bootstrap; preflight can fail again
on fresh snapshots — acceptable rollback for install-only change.
