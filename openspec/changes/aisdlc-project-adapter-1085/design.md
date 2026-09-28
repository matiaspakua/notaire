# Design — `.aisdlc/project.yml` adapter

## Context

`foreman.sh` (736 lines) mixes the generic phase/gate machinery with Notaire
values: `backend-api`, `frontend`, `mvn … -pl backend-api`, `npx vitest`,
`notaire-sdlc`, `validate-sdlc-plan.sh`, `preflight.sh`, `run_pipeline.sh`,
`start.sh`, `notaire-localai`, `localhost:8080/actuator/health`, `ci.yml`/`cd.yml`
and the forbidden-path regex. AUDIT §4.2 sketches the adapter file.

## Goals / Non-Goals

**Goals:** one adapter file with every project value `foreman.sh` uses; a
tested loader; no Notaire literal left in `foreman.sh` outside comments and
worker-facing messages built from adapter values; the TEST_CMD form check.

**Non-Goals:** splitting `foreman.sh`, rewriting prompts, model routing, a
schema validator for the adapter (a missing key already fails loudly).

## Decisions

- **YAML, read by Python.** Comments and lists matter for a hand-edited file;
  PyYAML is on the harness host. Bash never parses YAML: it calls
  `adapter.py`.
- **Read once at start-up.** `foreman.sh` loads the scalars it needs into
  variables before the first phase, so a broken adapter stops the run at once,
  not in phase 9.
- **Surfaces are a map in file order.** `SURFACE` becomes a comma list of their
  names; the suite runs them in adapter order, each in a subshell so a `cd`
  cannot leak. `both` maps to every surface, so the stored #1063 state still
  works.
- **The TEST_CMD check is a prefix match** on `test_one` up to `{test}`. It is
  strict on purpose: the prompts show that exact form, and a clear rejection
  costs one worker retry, while a wrong command cost a foreman round on #1063.
- **Location:** `$REPO/.aisdlc/project.yml`, where `$REPO` is the checkout that
  holds `foreman.sh`; `AISDLC_PROJECT` overrides it (used by the self-tests).
  Relative paths in the adapter are relative to `$REPO`.

## Riesgos / Trade-offs

- A typo in a regex value (`test_files`, `forbidden`) changes guard behaviour
  silently. Mitigation: the values move verbatim, and a self-test compiles every
  regex in the real adapter.
- Two sources of commands remain: the adapter and the prompt examples. The
  prompts are out of scope; the TEST_CMD check catches the drift that matters.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Scalar with placeholder; List value; Missing key | unit | `local-ai/sdlc/tests/test_adapter.py` |
| Two surfaces; No surface; Suite for two surfaces; Legacy value both | unit | `local-ai/sdlc/tests/test_adapter.py` |
| Missing module flag; Correct command | unit | `local-ai/sdlc/tests/test_adapter.py` |
| Real adapter loads and its regexes compile | unit | `local-ai/sdlc/tests/test_adapter.py` |

## Regression Strategy

`python3 -m unittest discover -s local-ai/sdlc/tests`, `bash -n foreman.sh`,
and a check run on the #1063 state (`foreman.sh 1063 check`), which loads the
adapter and exercises start-up. The adapter values are copied verbatim from
today's literals, and a `grep` for them in `foreman.sh` must come back empty.

## Playwright Strategy

n/a — no UI change.

## Deployment Strategy

Merged through the PR. The next `foreman.sh` run reads the adapter.

## Rollback Strategy

`git revert` of the merge commit.

## Migration Plan

None. Running issues keep their `triage.env`; `SURFACE=both` is still accepted.

## Open Questions

None.
