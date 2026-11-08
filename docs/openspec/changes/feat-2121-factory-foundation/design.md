> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

`framework/ai-sdlc` is already a zero-dependency kit with an independence test. The shell preflight, the local harness, and the cloud prompts are separate trees. Issue #2121 asks for a foundation, not a move.

## Goals / Non-Goals

**Goals:**

- Config and plugins as library functions, with a thin CLI.
- An inventory that fails if it claims every row is migrated or none is.
- A second temporary project, used by the kit tests, with no adopter paths, run from another cwd.

**Non-Goals:**

- Executing OpenSpec, Spec Kit, or AWS AI-DLC from the new commands.
- Relocating generated `INVENTORY.md` (#2117).
- Distributed claim locking.

## Decisions

- Canonical root stays `framework/ai-sdlc` because the independence test and the CLI already live there. See ADR-001.
- Plugin commands are argv lists. `plugin plan` prints them. See ADR-002.
- Standards are constraints in ADR-003. No SDK is added. OpenSpec is the CLI already pinned at 1.14.0 in this environment, not the newer release named in an unverified memo.
- The pull-request cap cannot be raised by `AISDLC_MAX_OPEN_PRS` or `--max-open-prs`.

## Riesgos / Trade-offs

- [Readers treat planned plugin files as integrations] → They are not in `[plugins] allow` and not on `plugin list`. The inventory status is `planned`.
- [A path check is mistaken for a sandbox] → ADR-002 says it is not.
- [Review size] → The slice is one contract. Splitting the inventory from the checker would leave the map untested.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| partial failure | unit | `framework/ai-sdlc/tests/test_factory.py` |
| ceiling | unit | same |
| dry plan | unit | same |
| honest inventory | unit | `workspace/tests/test_migration_inventory.py` |

- New unit tests: those files. The factory tests failed until the commands existed, then passed.
- New integration tests: none.
- Coverage impact: no Java. JaCoCo unchanged.

## Regression Strategy

- Existing tests affected: `framework/ai-sdlc/tests` still pass, including the independence test.
- Full suite command: `bash workspace/sdlc/preflight.sh --full` before push.
- HTTP/Bruno API suite: inside that command. No API change.
- Legacy paths at risk: none.

## Playwright Strategy

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: n/a — no UI surface
- Edge / error paths covered: n/a — no UI surface
- Viewports: n/a — no UI surface
- Command: product Playwright still runs inside preflight. This change has no screen.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy: Owner, after merge. The CLI smoke is `config validate`.

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: new CLI subcommands disappear; existing `gate` and `doctor` stay

## Migration Plan

No file moves. The inventory is the plan for later issues.

## Open Questions

None that block this slice. Spec Kit's extensions API and AWS AI-DLC's CLI were not executed here.
