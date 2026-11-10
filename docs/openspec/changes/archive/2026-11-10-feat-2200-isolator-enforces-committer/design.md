> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

CU76 factory deferred work still lists Isolator **sole-committer enforcement** after
#2199 (Isolator OS + Grant expire). `enforces_committer` is always false; axiom 2 is
only a policy dry-run (`aisdlc committer`). Owner `[committer] required=false` must
not flip. This slice adds real commit custody with an honest probe.

## Goals / Non-Goals

**Goals:**
- Block agent `git commit` unless Owner/runtime holds `AISDLC_COMMIT_GRANT`
- Prove interception with a failing probe before setting `enforces_committer=true`
- Work without `bwrap` (hooks/wrap custody); keep `sandbox` independent
- Extend IsolatorPort / CLI honesty; update ADR-004 + FACTORY + README

**Non-Goals:**
- Flipping `[committer] required=true`
- Portal / Context Lake / Wilson vanity / ForgePort write / ApprovalPort mint
- Faking `sandbox=true` or `enforces_committer=true`
- Mega-PR finishing the whole factory

## Decisions

1. **Custody mechanism (KISS, no bwrap required)** — install Isolator hooks under
   `.aisdlc/isolator/hooks/` and point `git config core.hooksPath` at that directory.
   A `pre-commit` hook refuses unless env `AISDLC_COMMIT_GRANT` is non-empty.
   Agent wrap sessions never set that env. Owner/runtime may export it to commit.
   This is **not** OS isolation and MUST NOT set `sandbox=true`.

2. **Enforcement probe** — in a temporary git repo: install hooks, attempt
   `git commit` without grant → must fail; optionally with grant → may succeed.
   `enforces_committer=true` only when the without-grant attempt fails closed.
   Probe runs as part of `isolator probe` / feeds `isolator status`. Override /
   force-off via env if needed for tests (`AISDLC_COMMIT_GRANT` for grant path;
   no fake-true without running the probe).

3. **CLI surface** — extend existing `aisdlc isolator status|probe` honesty fields.
   Add `aisdlc isolator wrap -- <cmd>`: install hooks on the worktree, unset
   commit grant, run argv (optionally under bwrap when sandbox available — wrap
   itself does not require bwrap). `isolator run` may also install custody hooks
   before executing when sandbox is available; wrap covers the no-bwrap path.

4. **Independence** — `sandbox` and `enforces_committer` are orthogonal:
   - no bwrap, hooks work → `sandbox=false`, `enforces_committer=true` (after probe)
   - bwrap works, hooks broken → `sandbox=true`, `enforces_committer=false`
   - neither → both false / no-evaluable for sandbox

5. **Forge mutations** — out of scope unless a tiny PATH shim is required; this
   slice focuses on `git commit` interception only (axiom 2 core).

6. **Doctor** — Isolator INFO reflects both fields honestly; never claims
   `enforces_committer=true` without probe proof.

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Readers treat hooks as OS sandbox | ADR + JSON notes; `sandbox` stays false without bwrap |
| Local `core.hooksPath` surprises developers | Document wrap as opt-in session mode; probe uses temp repo |
| Bypass via `git -c core.hooksPath=...` | Document honesty limit; bwrap RO + wrap is stronger when available; do not claim unbreakable |
| CI without git hooks support | Probe fail → `enforces_committer=false` (honest) |

## Testing Strategy

| Scenario | Test |
|----------|------|
| commit without grant fails under Isolator custody | IsolatorCommitterTest |
| enforces_committer true only after proven probe | IsolatorCommitterTest |
| enforces_committer works without bwrap | IsolatorCommitterTest |
| sandbox stays false without containment probe | IsolatorCommitterTest |
| wrap blocks agent git commit | IsolatorCommitterTest |
| grant allows Owner/runtime commit | IsolatorCommitterTest |
| doctor never claims enforces_committer without probe | IsolatorCommitterTest |

TDD: write IsolatorCommitterTest first and observe fail, then implement.

## Regression Strategy

Existing `test_isolator.py` / `test_isolator_os.py` remain green. Update expectations
where status/probe now may report `enforces_committer=true` after a real probe
(hosts with working git hooks). Without hooks working, stays false. Do not weaken
sandbox honesty assertions.

## Playwright Strategy

n/a — no UI surface (kit CLI only)

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

Additive CLI (`wrap`) + probe field semantics. Existing profiles unchanged.
`[committer] required` remains false.

## Open Questions

None.
