# Local Cursor handoff — recorded, not the active path

Factory note for epic [#1693](https://github.com/matiaspakua/notaire/issues/1693).
This is not product design, and it is not the oMLX harness
(`local-ai/sdlc/foreman.sh`).

## Active path (Owner, 2026-11-08)

The Owner superseded the local-machine requirement (#2086): continue repository
work on Cursor Cloud. Do **not** access the Owner's Mac. Do **not** start a private worker (`agent worker start`) and do **not** target My Machines.

Measured earlier the same day, before that direction: the cloud run's worker
list returned **no** connected private machines. That measurement is historical.
It is not a reason to launch a worker.

Cloud execution still follows the Constitution: one issue-sized slice, claim
the free lane, `bash workspace/sdlc/preflight.sh --full` on the cloud VM before
push, merge `origin/main` (never rebase), and the Owner merges.

## Recorded commands (do not run)

Kept so the trigger is identifiable. Official page:
[My Machines](https://cursor.com/docs/cloud-agent/self-hosted/my-machines).
Do not execute this block on the Owner's Mac or from a cloud run.

```bash
agent login
agent worker start --name "notaire-local" --worker-dir "$PWD"
```

The page also describes `agent worker debug`, `--computer-use`, and a chat
trigger `@cursoragent worker=notaire-local`. `self_hosted` or `pool=` alone
does not select My Machines. None of those are in use for #1693.

## What to continue

Do not claim a second backend writer. [#577](https://github.com/matiaspakua/notaire/issues/577)
is the only open issue with `status:in-progress`. It has **no claim marker**,
so `claim-issue.py --release-stale` will not clear it (`no claim marker`; the
lane stays held). [#2085](https://github.com/matiaspakua/notaire/pull/2085)
merged on 2026-11-08 without a closing keyword, so GitHub left #577 open.
Remaining work on #577 is the loose-return ratchet, not the original
"controllers return entities" wording.

| Issue | State on 2026-11-08 | Role |
|-------|---------------------|------|
| [#1693](https://github.com/matiaspakua/notaire/issues/1693) | OPEN epic | Not claimable. Parent of the slices |
| [#577](https://github.com/matiaspakua/notaire/issues/577) | OPEN, `status:in-progress`, no open PR | Continue this slice. Do not add a second claim |
| [#580](https://github.com/matiaspakua/notaire/issues/580) | OPEN, no WIP label | Next backend slice only after #577 leaves the lane |

[#576](https://github.com/matiaspakua/notaire/issues/576),
[#578](https://github.com/matiaspakua/notaire/issues/578), and
[#579](https://github.com/matiaspakua/notaire/issues/579) are CLOSED.

## Specialists

The cloud foreman coordinates. It does not re-implement the slice.

| Phase | Role | Skills |
|-------|------|--------|
| Slice shape | `java-architect` | `hexagonal-arch`, ADR-030, `.claude/rules/hexagonal-architecture.md` |
| Tests then code | `backend-implementer` | those rules, `java`, `programming`, `maven-build` |
| Gate 2 | `testing-qa` | `testing`, `qa-automation-strategy` |
| Review | `code-reviewer` | `ai-agent-workflow` |
| Auth or PII in the diff | `security-auditor` | `notaire-cybersec`, `secure-threat-modeling` |

One domain per PR. Merge `origin/main` into the branch. Do not rebase or force-push.
`bash workspace/sdlc/preflight.sh --full` is the gate before push. Only the Owner
merges. Open product pull requests stay serialized (max three open).

## Refresh the queue

```bash
gh issue view 1693 --json state,title
gh issue list --state open --label "status:in-progress" --limit 20
gh issue view 577 --json state,title,labels
gh issue view 580 --json state,title,labels
```
