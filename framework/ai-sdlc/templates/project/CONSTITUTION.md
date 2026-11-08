# {{project}} Engineering Constitution

> The mandatory development process for this repository, for humans and AI agents alike.
> It is **agent-agnostic** and **stack-agnostic**: commands live in `aisdlc.toml`, never here.
> Where any other document (agent files, rules, skills, prompts) contradicts this one, this one prevails.

## 1. Principles

| # | Principle | Meaning |
|---|-----------|---------|
| P1 | Spec before code | No implementation without an issue, acceptance scenarios and a change record. |
| P2 | Red before green | A test is written and observed failing before the code that makes it pass. |
| P3 | Machines check machines | A gate is a command with an exit code. An agent's claim of success is never evidence. |
| P4 | Traceable end to end | Issue → change record → tests → commits → pull request → release evidence. |
| P5 | Ratchet, do not big-bang | Legacy debt is listed in baselines that only shrink; quality floors only rise. |
| P6 | Simple and bounded | The simplest change that satisfies the scenarios; clean only what the change touches. |
| P7 | Docs are part of done | Permanent documentation changes in the same pull request, never duplicated. |
| P8 | Humans own decisions | The Owner approves specifications, grants exceptions and owns the merge rule. |
| P9 | Publish continuously | Work lives on the remote, not on one disk: push at branch creation and after each stage. |
| P10 | Least privilege, no secrets | Agents get the narrowest tools and tokens; secrets never enter the repository. |
| P11 | Self-improve while delivering | Every agent improves the process and the system as it works: capture friction as *what happened → rule → check*; fix it in the same change when Boy Scout–bounded (P6), or open a follow-on issue when not; prefer a gate or ratchet over prose-only advice. Covers product code, docs, QA, security, UI/UX, infra and the harness itself. |

## 2. Roles

| Role | Who | May | May not |
|------|-----|-----|---------|
| Owner | Human code owner | Approve specs, grant exceptions, amend this document, merge anything | — |
| Foreman | Orchestrating agent | Run gates, write review notes, merge under the merge rule (§5) | Implement the change it reviews; change its own harness without the Owner |
| Worker | Implementing agent | Implement one change phase by phase; answer review notes | Merge, push to the default branch, grant itself an exception |

Role models, tiers and the merge flag are declared in `aisdlc.toml` (`[roles]`).

## 3. Lifecycle and gates

Every change follows the stages below. A stage is done only when every required gate the
profile declares for it exits as expected (`aisdlc gate <stage>`).

| Stage | Gate | Done when |
|-------|------|-----------|
| intake | G0 ready | The issue exists, states the problem and links the requirement it serves. |
| plan | G1 specified | The change record is complete and cites a real, resolvable issue. |
| red | G2 tests fail first | New tests exist and fail for the right reason. |
| green / pre-push | G3 verified | The full suite, lint, ratchets and process checks pass locally. |
| pre-pr | G3 verified | The full local pipeline passes; documentation is updated. |
| merge | G4 reviewed | Required CI is green on the final commit, review notes are resolved, no conflicts. |
| release | G5 shipped | Deployed, smoke-tested, issue closed, evidence recorded. |

## 4. Conventions

- One issue per change, one branch per issue: `<type>/<issue>_<description>`.
- Conventional Commits; the closing commit carries `Closes #<issue>`, the others `Refs #<issue>`.
- Pull request titles: `[#<issue>] <type>(<scope>): <description>`.
- Change records live in the profile's spec directory (or the project's spec engine).

## 5. Merge rule

The Foreman may merge a Worker's pull request only when every required check is green on the
final commit, the `merge` stage passes, and no review note is open. The Owner alone merges
changes to this document, to the harness and merge path, and any change carrying an exception.
Those paths are listed in `[protected]` of `aisdlc.toml`; the `merge` stage runs `aisdlc protected`,
so the rule is an exit code, not a sentence.

## 6. Rules for AI agents

Agents must read this document and `AGENT-CARD.md` first, load rules and skills on demand only,
and state which stage and gate their work addresses. Agents must **self-improve while
delivering (P11)** — feed friction into gates, docs and follow-on issues. Agents must never:

- implement without a change record, or write code before its failing test;
- report a gate as passed without running it, or edit evidence, ledgers or baselines by hand;
- skip, weaken or disable a test or a gate to get green;
- commit to the default branch, rewrite pushed history, or merge outside the merge rule;
- invent issue numbers, requirements or scope the Owner did not ask for;
- move on from a process or tool failure without capturing the lesson (P11).

When an agent needs a person, it stops with a **decision packet**: the context in plain words,
two or three alternatives each with its consequence, and a recommendation marked as a proposal;
at most three related questions per message. A routine detail inside the task is not asked: it is
resolved with a sensible default and recorded.

A **deviation** from the approved change record is *mechanical* when it only applies what the
record, its tests or an already recorded decision say; it is recorded with what was checked. It is
a *decision*, and goes to the Owner before the commit, when it changes the objective, the scope, a
contract, the data, permissions or an accepted risk. Missing information never makes a deviation
mechanical.

Corrections are **bounded**: one batch for all blocking review notes and one re-check limited to
them; a second round only when the same blocker still fails; then the work stops with a decision
packet instead of a third attempt.

Every change closes with a **risk line**: `Risk: <item> (<reason>)` or `Risk: none`, checked
against existing behavior the request did not mention.

## 7. Learning loop (P11)

Self-improvement is mandatory while delivering (P11), not a post-mortem hobby.
Every incident, review note, false red, redundant gate run or near miss is written
down as *what happened → rule → check*. A rule that can be a command becomes a gate
or a ratchet in `aisdlc.toml`; a rule that cannot stays in this document. Prose-only
rules are reviewed each release for mechanization. Agents must not leave the lesson
only in chat.

Autonomy is earned and lost per operation: a level rises only with evidence from the ledger
(`aisdlc report`), and an incident (an operation outside its level, or a merge that had to be
reverted) lowers it until its cause is understood and corrected.

## 8. Exceptions

Only the Owner grants an exception (emergency security fix, one-time migration, trivial typo).
It is documented in the commit and the pull request and labelled so the process checks can see it.

*Adoption level {{level}} — see the framework README for what each level adds.*
