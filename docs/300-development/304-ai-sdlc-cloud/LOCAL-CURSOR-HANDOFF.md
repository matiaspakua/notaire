# Local Cursor handoff — hexagonal continuation

Factory procedure for continuing epic [#1693](https://github.com/matiaspakua/notaire/issues/1693)
on the Owner's machine. This is not product design, and it is not the oMLX
harness (`local-ai/sdlc/foreman.sh`). Cloud agents do not implement this queue
as a substitute when the Owner asked for the local machine.

Measured from this factory change (#2086) on 2026-11-08: the cloud run's
worker list returned **no** connected private machines. A worker appears only
while `agent worker start` is running on that machine.

## Prerequisites

1. Cursor CLI installed (`agent --version`). Official quickstart:
   [My Machines](https://cursor.com/docs/cloud-agent/self-hosted/my-machines).
2. Same Cursor account as the GitHub user who will comment or pick the machine.
3. A checkout of `matiaspakua/notaire` whose `origin` remote is that repository.
4. Toolchain the hex slice needs (JDK, Maven, Docker for Testcontainers). The
   machine keeps its own tools. Cloud environment secrets are not copied there.
5. Outbound HTTPS to `api2.cursor.sh` and `api2direct.cursor.sh`.

## Start the worker

On the machine, in the notaire checkout:

```bash
agent login
agent worker start --name "notaire-local" --worker-dir "$PWD"
```

Leave that process running. Confirm with `agent worker debug`. Optional:
`--computer-use` when a later UI pass must drive the desktop on this machine.

## Trigger

Either:

- At [cursor.com/agents](https://cursor.com/agents), choose the machine under **My Machines** and send the task below, or
- On the issue, as a trusted commenter whose Cursor account owns the machine:
  `@cursoragent worker=notaire-local` plus the task. The worker name must match
  `--name`, and the checkout remote must be this repo. There is no fallback to
  a cloud VM when the machine is missing.

`self_hosted` or `pool=` alone does not select My Machines.

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

The foreman on the machine coordinates. It does not re-implement the slice.

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
