> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`openspec new change` (OpenSpec 1.14) creates only
`openspec/changes/<name>/.openspec.yaml`. Agents must write proposal / design /
tasks / traceability from `openspec instructions` + schema templates. Weaker
models omit headings or leave `<!-- ... -->` placeholders. `validate-sdlc-plan.sh`
already checks heading presence and Issue/Use Case format, but a section that
exists with only the template HTML comment still passes those heading checks.

The local-ai issue text refers to `phase_setup` seeding; for Cursor Cloud this
change implements the same contract on the generic path: a seed script plus a
stronger plan validator. No dependency on `local-ai/`.

## Goals / Non-Goals

**Goals:**

- Seed the four notaire-sdlc templates into a change folder when absent.
- Pre-fill Issue, Use Case, Branch, and change-name cells the seeder knows.
- Reject leftover template-only HTML-comment section bodies in Gate 1.
- Cover both behaviours with self-tests under `scripts/tests/`.

**Non-Goals:**

- Rewriting the OpenSpec CLI itself.
- Driving local-ai `foreman.sh` / `03-spec.md`.
- Auto-writing capability delta specs (still agent-authored).

## Decisions

1. **Seed script over CLI fork** — `scripts/seed-openspec-change.sh` wraps or
   follows `openspec new change` and copies
   `openspec/schemas/notaire-sdlc/templates/{proposal,design,tasks,traceability}.md`.
   Rationale: project-owned, agent-agnostic, no wait on upstream OpenSpec.
2. **Leftover-comment check in `validate-sdlc-plan.sh`** — for each `##` section
   in proposal / design / tasks / traceability, strip HTML comments and
   whitespace; if the body is empty, fail naming `file: heading`. Rationale:
   same gate already run by preflight and CI; one place for all agents.
3. **Docs in OpenSpec permanent docs, not `local-ai/sdlc/AI-SDLC.md`** — cloud
   fleet uses NOTAIRE-ADAPTATIONS + specification-template.

## Riesgos / Trade-offs

- [False positive on intentionally empty optional sections] → Optional sections
  (`Migration Plan`, `Open Questions`) may be omitted entirely; if present they
  must have real text. Document in `--list`.
- [Filled tables that still contain HTML comments in cells] → Check strips
  comments then requires remaining non-whitespace content; table headers alone
  with empty cells after strip still fail — agents must replace cell
  placeholders.
- [Existing incomplete active changes] → Archive CLOSED-issue changes first
  (done for #1062); open incomplete plans must be filled or archived separately.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Seed copies four templates when absent | unit (script) | `scripts/tests/test_validate_sdlc_plan.py` |
| Seed does not overwrite an existing file | unit (script) | same |
| Seed fills Issue / Use Case / Branch | unit (script) | same |
| Leftover HTML-comment body rejected | unit (script) | same |
| Filled section passes | unit (script) | same |
| Error names file and heading | unit (script) | same |

- New unit tests (`scripts/tests/`): Python unittest invoking the bash scripts
- New integration tests: n/a — no Java surface
- Coverage impact (JaCoCo): n/a — scripts only

## Regression Strategy

- Existing tests affected: `scripts/tests/test_pr_checks.py` unchanged;
  validator must still pass filled active changes (`gestion-workflow-reingreso-testimonio`).
- Full suite command: `python3 -m unittest discover -s scripts/tests -v`
- HTTP/Bruno API suite: n/a
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. This change only touches bash scripts, OpenSpec artifacts,
and Markdown docs.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge via PR; no runtime deploy
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `bash scripts/validate-sdlc-plan.sh` and
  `python3 -m unittest discover -s scripts/tests -v`

## Rollback Strategy

- Revert safe: yes — revert the PR; seeding becomes advisory again and leftover
  comments stop failing Gate 1
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: stricter Gate 1 may block incomplete plans
  (desired behaviour)

## Migration Plan

1. Archive stale CLOSED-issue active changes blocking the validator.
2. Land seed script + validator check + tests + docs.
3. Agents use `openspec new change` then `bash scripts/seed-openspec-change.sh`.

## Open Questions

None — scope is the generic OpenSpec path equivalent of the issue's local-ai
`phase_setup` / leftover-comment acceptance criteria.
