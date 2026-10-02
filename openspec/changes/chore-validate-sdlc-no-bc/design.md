> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`scripts/validate-sdlc-plan.sh` counted `#### Scenario:` lines by piping
per-file `grep -c` output through `paste -sd+ - | bc`. On Cloud Agent VMs that
had not run `.cursor/install.sh`, `bc` was missing; the pipeline failed and the
`|| echo 0` fallback reported zero scenarios, falsely failing Gate 1 even when
Acceptance Criteria existed. `.cursor/install.sh` on `main` already installs
`bc` and OpenSpec; workers that skip install still need Gate 1 to work.

## Goals / Non-Goals

**Goals:**

- Sum scenario counts without requiring `bc` (awk arithmetic).
- Keep the same count when `bc` is present.
- Cover the bc-unavailable path with a unit test.
- Document that Cloud Agents must run `bash .cursor/install.sh` until the
  Environment card is Saved.

**Non-Goals:**

- Changing `.cursor/install.sh` (already installs openspec + bc).
- Relying on the human-only `sdlc-exception` label.
- Product / API / UI changes.

## Decisions

1. **awk sum instead of paste|bc** — `awk '{ s += $1 } END { print s + 0 }'` is
   available on Ubuntu CI and Cloud VMs without apt. Rationale: removes the false
   Gate 1 failure mode without changing install.sh.
2. **Broken-bc unit test** — put a failing `bc` stub first on PATH and assert a
   filled plan still validates. Rationale: proves the awk path, not merely that
   `/usr/bin/bc` happens to exist on the runner.
3. **OpenSpec change folder over sdlc-exception** — agents cannot set the label;
   a filled `openspec/changes/chore-validate-sdlc-no-bc/` satisfies
   `check-sdlc-exception.sh`.

## Riesgos / Trade-offs

- [awk unavailable on exotic shells] → Notaire agents and CI run Ubuntu bash;
  awk is part of the base image.
- [Empty grep with pipefail] → Same as before: empty/failed grep falls through
  to `|| echo 0`, correctly failing Gate 1 when no scenarios exist.
- [Docs still mention installing bc] → Install remains recommended for other
  tooling; Gate 1 no longer hard-depends on it.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Scenario count works without bc | unit (script) | `scripts/tests/test_validate_sdlc_plan.py` |
| Scenario count still works when bc exists | unit (script) | same (`test_accepts_filled_section_body`) |
| Zero scenarios still fails Gate 1 | unit (script) | existing leftover / empty-spec paths |

- New unit tests (`scripts/tests/`): Python unittest invoking the bash validator
- New integration tests: n/a — no Java surface
- Coverage impact (JaCoCo): n/a — scripts only

## Regression Strategy

- Existing tests affected: `scripts/tests/test_validate_sdlc_plan.py` extended;
  filled active changes must still validate structurally.
- Full suite command: `python3 -m unittest discover -s scripts/tests -v`
- HTTP/Bruno API suite: n/a
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. Scripts and Markdown docs only.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge via PR; no runtime deploy
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `bash scripts/validate-sdlc-plan.sh` (structure)
  and `python3 -m unittest scripts.tests.test_validate_sdlc_plan -v`

## Rollback Strategy

- Revert safe: yes — revert the PR; scenario counting returns to paste|bc
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: Cloud Agents without bc keep working for Gate 1
