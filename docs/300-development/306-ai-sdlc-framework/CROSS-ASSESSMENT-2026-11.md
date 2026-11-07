# AI SDLC cross-repo assessment — Notaire against agentic-os, gentle-ai and factory-control

| Field | Value |
|-------|-------|
| Date | 2026-11-07 |
| Scope | The AI SDLC of four sibling repositories: process documents, agent configuration, hooks, gates, roles, evidence, tools |
| Baselines | `notaire` `3d890ef4` · `agentic-os` `d400825` (0.3.0) · `gentle-ai` `5b2168b5e` (v4) · `factory-control` `50d2a137` (v0.9.0) |
| Branch | `chore/ai-sdlc-cross-repo-improvements` (worktree, not pushed) — needs an Issue before Gate 1 (see §6) |
| Related | [`ai_sdlc_framework.md`](ai_sdlc_framework.md) (the framework report), [`local-ai/AUDIT.md`](../../../local-ai/AUDIT.md) (#1079) |

## 1. Verdict

Notaire has the most complete AI SDLC of the four: one policy (`CONSTITUTION.md`) above every
agent file, a ≤3 KB card, an OpenSpec schema that injects the policy into every agent, a live-issue
Gate 1, a red gate (`expect = "fail"`), shrink-only ratchets, a linted skills catalog, a context
budget, a claim protocol, release-only CI mirrored by a mandatory local preflight, and three
execution paths (Claude Code, Cursor Cloud fleet, local models).

Its weak points are the ones the other three repositories were built around:

1. **Authority is prose where it should be an exit code.** The Foreman merge rule says the Owner
   merges Constitution, harness and merge-path PRs; nothing checked it (agentic-os C14-004
   "agents cannot change their own rules"; factory-control `protectedPaths`).
2. **The plan does not bound the diff.** A proposal names modules, not paths; audit item S2
   ("every planned path appears in the diff") was still open (agentic-os C15-006; gentle-ai
   "the parent checks each writer's diff against its surface").
3. **Evidence can be rewritten by whoever can write the file.** The local worker was observed
   sed-editing the harness ledger (`local-ai/sdlc/AI-SDLC.md`); the kit ledger had no integrity
   check (agentic-os journal hash chain).
4. **No protocol for the moments an agent is unsure:** asking the Owner, deviating from the
   approved spec, retrying a correction, declaring risk (agentic-os decision packet and deviation
   boundary; gentle-ai bounded corrections, risk line, RED + PRESERVE tests).
5. **Review size is unbounded** (gentle-ai 400-line cognitive budget, chained PRs).
6. **Agent stop with unpublished work is unguarded** (audit E4 open; #1681 is mandatory but only
   reminded at session start).

This change closes 1–6 (§4). §5 lists what remains, ranked.

## 2. The four AI SDLCs at a glance

| | Notaire | agentic-os | gentle-ai | factory-control |
|---|---|---|---|---|
| **What it is** | A product (notary SaaS) built by AI agents under a policy | A tool-agnostic methodology + Claude Code plugin | A Go installer that configures 18 agent runtimes | A local control plane (runtime, CLI, dashboard, IDE extension) |
| **Process source** | `CONSTITUTION.md` (825 lines) + card + OpenSpec schema | `plugin/core/*.md` — numbered rules `[Cnn-nnn]`, never reused | `AGENTS.md` skill index, `AI_POLICY.md`, ODD canon in Go assets | `AGENTS.md` + `F1-plan-de-ejecucion.md` + `factory-core/` (process, gates, policies) |
| **Workflow** | Issue → OpenSpec G1 → red G2 → green → preflight G3 → merge G4 → smoke G5 | W1 phase · W2 knowledge/method · W3 bug, with 6 human gates | ODD: inline or delegated-for-a-reason; RDD review receipts optional | Intent → stages from `factory-core/process`; plan tasks via `f1.py` |
| **Roles** | Owner · Foreman · Workers (13 agent files) | Coordinator + planner · builder · checker (fresh agent per stage) | Parent + bounded writers + independent verifier | Foreman + 12 role agents in the CORE |
| **Who commits / merges** | Workers commit; Foreman merges under the rule; Owner for policy | Only the coordinator commits; push N2; PR and merge N1 (human) | The parent owns git; push/PR/merge are separate user decisions | The agent commits in its worktree; `f1.py merge` integrates |
| **Gates** | Shell commands with exit codes (`aisdlc`, `preflight.sh`, CI on tags) | Human gates (G-PLAN, G-CLOSE, G-PUSH…) + methodology programs | Go CLI decides; refusal and dead-code ratchets; PR size policy | Gate YAML in the CORE, clean-checkout runner, tamper scan |
| **Autonomy** | Project level 1–4 (`aisdlc doctor`) | Per operation N0–N4; raised by G-LEVEL with evidence, lowered by an incident | Delegation only for a named reason; verifier only for high risk | Risk tiers with budgets (`bajo`/`medio`) and stop-loss |
| **Evidence** | `.aisdlc/evidence` JSONL; harness `metrics.jsonl`; CI reports | Hash-chained journal (PostToolUse hook); four properties of evidence of record | Review receipts bound to a frozen candidate; `recoverytrace` | Event-sourced fact store with upcasters; in-toto statements |
| **Agent tests** | Kit and harness self-tests (temp git repos) | 592 unit tests on real repos/worktrees | Scripted-model E2E: real agent binary, mock model HTTP | 968 Vitest tests; real-harness test skipped without a key |
| **Context control** | Always-loaded ≤8k tokens gate; card ≤3 KB | Brief replaces the conversation for role agents | Skill registry with exact paths; prompt budgets | `context.maxChars` in policies |

## 3. Capability matrix

Legend: ● mechanical (a command or service enforces it) · ◐ documented or partial · ○ absent.
"After" is this branch.

| Capability | Source of the idea | Notaire before | Notaire after |
|---|---|---|---|
| One policy above every agent file | all four | ● | ● |
| Live issue check at Gate 1 | Notaire | ● | ● |
| Red gate (`expect = fail`) | Notaire | ● | ● |
| Shrink-only baselines, rising floors | Notaire, gentle-ai | ● | ● |
| Owner-only paths enforced at merge | agentic-os C14-004, factory-control `protectedPaths` | ◐ prose | ● `aisdlc protected` (merge stage) |
| Diff bounded by the plan's write set | agentic-os C15-006/C15-007, gentle-ai surfaces | ○ (audit S2 open) | ◐ `aisdlc scope` advisory + template section |
| Review-size budget, chained PRs | gentle-ai `pr-check.yml`, `chained-pr` skill | ○ | ◐ `aisdlc size` advisory |
| Tamper-evident evidence | agentic-os journal, factory-control F-05 | ○ | ● `aisdlc evidence verify` (kit ledger) |
| Decision packet when asking a human | agentic-os C04-047, C15-002 | ○ | ◐ rule + template |
| Deviation boundary (mechanical vs decision) | agentic-os C07-043…C07-053 | ○ | ◐ rule |
| Bounded correction rounds | gentle-ai ODD S8, `reviewtransaction` | ◐ "2–3 attempts" | ◐ one batch + one re-check, then packet |
| RED per rule + PRESERVE per touched behavior | gentle-ai ODD S6 | ◐ red only | ◐ rule |
| Risk line on every change | gentle-ai ODD S3 | ○ | ◐ PR template + rule |
| AI-assistance disclosure in the PR | gentle-ai `AI_POLICY.md` | ○ | ◐ PR template |
| Stop with unpublished work | Notaire #1681, audit E4 | ◐ SessionStart reminder | ● Stop hook, once per stop |
| Incident lowers autonomy | agentic-os C05-003 | ○ | ◐ kit policy template + README |
| Agent never commits; runtime/coordinator does | agentic-os C15-008 | ◐ local harness owns commits; fleet Workers commit | ◐ unchanged (§5 B2) |
| OS sandbox for the agent process | factory-control ARQ-01, F-01 | ○ local worker is `danger-full-access` | ○ (§5 B1) |
| Scripted-model E2E of the harness | gentle-ai `testing-agents-deterministically.md` | ○ (audit H5 open) | ○ (§5 B3) |
| Cold-start test (resume from published state only) | agentic-os C11 | ○ | ○ (§5 B5) |
| Per-operation autonomy matrix | agentic-os C05 N0–N4 | ○ project level only | ○ (§5 B6) |
| Computed protection profile | agentic-os C14-012, factory-control F-06 | ○ | ○ (§5 B7) |

## 4. What this change adds

| # | Addition | Where | How it is enforced | Evidence |
|---|---|---|---|---|
| A1 | **Owner-only paths.** 27 globs: policy, card, agent entry files, kit and profile, hooks and settings, Foreman playbook, preflight/pipeline/heavy-CI/exception/plan scripts, context and quality ratchet scripts, ruleset scripts and desired state, OpenSpec schema and config, fleet manifest and merge-gate doc, local foreman | `.aisdlc/aisdlc.toml` `[protected]`; `aisdlc protected` | `merge` stage, required; `doctor` warns at level ≥3 when an agent role may merge and no paths are declared | Replayed on `3d890ef4` (#2046): flags `AGENTS.md`, `CONSTITUTION.md`, the design template — a PR the Constitution already reserves for the Owner. Clean on `fee25f4b`, `7c970ff7`, `50105a83` |
| A2 | **Write set.** Optional `### Write set` in the proposal template; the kit reads the section from every change folder the diff touches (including `archive/`), ignoring HTML comments | `aisdlc scope`; proposal template | `pre-pr`, advisory; *not evaluable* (never a silent pass) until proposals carry it | 5 tests; on today's history it reports NOT EVALUABLE, as it should |
| A3 | **Review budget.** 400 counted lines; OpenSpec ceremony, lockfiles, snapshots, generated types and ratchet baselines excluded | `aisdlc size`; `[size]` | `pre-pr`, advisory | Last merged PRs: 6, 54, 224 counted lines (284–309 excluded) — the budget fits Notaire's real cadence |
| A4 | **Hash-chained evidence.** `prev` = SHA-256 of the previous line; legacy records accepted only as a prefix; head digest printed for anchoring | `aisdlc/evidence.py`; `aisdlc evidence verify` | `merge` stage, required | 6 tests: edit, delete, legacy-after-chain detected; cost log chained; `report` unchanged |
| A5 | **Decision packet, deviations, bounded corrections, RED + PRESERVE, risk line** | `.claude/rules/ai-agent-workflow.md`; CONSTITUTION §10; kit policy template; `cloud-foreman.md` | Prose (cannot be a command); the PR template carries Deviations, AI assistance, Review budget and `Risk:` so review sees them | — |
| A6 | **Stop hook for unpublished work** | `.claude/hooks/stop-unpublished-work.sh`; `.claude/settings.json` | Claude Code `Stop`; blocks once, retry passes | 6 tests in `workspace/tests/test_stop_hook_publication.py` |

Kit tests: 79 → 99, all passing. The 20 new tests were run against the unmodified kit: 18 failed
or errored. The other two pass there by design or by accident: `report` still reading chained
records is a preservation test, and the invalid-`limit` test passes vacuously because the old kit
also exits 2 on an unknown command.

## 5. Remaining gaps, ranked

| # | Gap | Source | Why it matters for Notaire | Proposal | Priority |
|---|---|---|---|---|---|
| B1 | The local worker runs with `sandbox_mode = "danger-full-access"`, `approvals never` | factory-control ARQ-01/F-01 (Seatbelt/bubblewrap, `.git` read-only, token dir hidden) | The worker was observed running `git reset`, `pkill -9 git`, `--no-verify` and sed-editing the ledger; it can read `~/.ssh` and `gh` credentials | Run the worker under a macOS Seatbelt profile: worktree writable, `.git/` and `$RUNS` read-only, `~/.config/gh` and `~/.ssh` denied, network to the model endpoint only | P1 |
| B2 | Fleet Workers commit their own work | agentic-os C15-008; factory-control F-04 | The local harness already owns commits (the ref guard caught violations); the cloud fleet does not | Fleet handoff returns a diff; the Foreman runs `aisdlc scope`, then commits. Start with the local harness rule written into `FLEET-ARCHITECTURE.md` | P2 |
| B3 | No deterministic E2E of the harness | gentle-ai scripted model over HTTP; audit H5 | Harness regressions are found by running real issues (the audit found five that way) | A fixture issue + a mock OpenAI-compatible server that replays a scripted worker; run `foreman.sh` end to end in `local-ai/sdlc/tests` | P1 |
| B4 | Harness ledger (`local-ai/sdlc/bin/ledger.py`) is not chained and is worker-writable | agentic-os journal | Same failure the worker already exhibited | Reuse the kit's chain; keep `$RUNS` outside the worker's sandbox (B1) | P2 |
| B5 | No cold-start test | agentic-os C11 | #1681 makes GitHub the state; nothing proves a fresh agent can resume from it | At Gate 5 for multi-PR changes: a fresh session gets only "resume #N" and must name stage, next action and branch; judged against an answer key written before the run | P2 |
| B6 | Autonomy is one project level | agentic-os C05 (N0–N4 per operation, incident lowers) | Merge and Flyway on staging deserve different levels than editing a test | `[autonomy]` table in the profile (operation → level, ceiling); `aisdlc report` flags operations whose evidence would justify a change; a revert of a merged PR lowers the merge level | P2 |
| B7 | No computed protection profile | agentic-os C14-012; factory-control F-06 | The ruleset, hooks and kit each enforce part of the policy; nobody sees which guarantee is enforced by a service, checked by the kit, or only the Owner's act | `aisdlc posture`: per guarantee, the level (service / kit / authority / blocked), read from the live ruleset (`assert-protect-main-ruleset.sh`), hooks and profile | P3 |
| B8 | Write set and size are advisory | this change | Advisory gates are where drift starts | After 10 proposals carry a write set: `validate-sdlc-plan.sh` requires it and `scope --strict` becomes required | P2 |
| B9 | 38 worktrees, 17 prunable, all as siblings of the repository | factory-control `.worktrees/<ID>` (gitignored, one per claimed task, released on merge) | Stale worktrees hold branches, disk and confusion for the next agent | `workspace/sdlc/worktree-hygiene.py`: report missing/merged/dirty/unpushed; prune only missing ones; claim protocol creates the worktree | P3 |
| B10 | Memory is spread across `.serena/`, `.remember/`, Claude auto-memory | factory-control F2MEM (agents propose, people confirm); gentle-ai Engram | Lessons land in tool-private stores instead of the repository (P12) | Lessons go to `FLEET-LESSONS-*` or a gate (P12); tool memories are caches, never the record | P3 |

## 6. Publishing this change

The Constitution requires an Issue + Use Case + OpenSpec change before code (Gate 1). This
branch was prepared in an isolated worktree without creating an Issue, so that no fleet agent
claims or races it. To publish:

1. Create the Issue (Use Case CU76 — Quality Assurance and Testing Infrastructure), `type:chore`,
   `area:devops`, owner-decision (not `status:ready`), so the fleet does not pick it.
2. Rename the branch to `chore/<n>_ai-sdlc-cross-repo-improvements`; seed the OpenSpec change
   (`seed-openspec-change.sh`) with this document as the proposal's evidence and a `### Write set`
   naming the files in §4.
3. `bash workspace/sdlc/preflight.sh --full`, push, open the PR. `aisdlc protected` fails on it by
   design: the Owner merges it.

## 7. Evidence and limits

- Read: the four repositories' policy files, agent definitions, hooks, kits and their tests;
  `local-ai/AUDIT.md`; `local-ai/sdlc/AI-SDLC.md`; agentic-os `plugin/core/03,04,05,07,08,11,14,15`;
  gentle-ai `AI_POLICY.md`, `docs/trigger-rules.md`, `docs/testing-agents-deterministically.md`,
  `odd/tasks/1731-parity-delegate-for-reason.md`, `.github/workflows/pr-check.yml`;
  factory-control `AUDIT.md`, `factory-core/policies.yaml`, `docs/auditoria/ASSESSMENT_AI_FACTORYS_V1.md`.
- Run: the kit suite (99 tests), the new hook suite (6 tests), `aisdlc doctor` on Notaire's
  profile, and the three new checks replayed on four merged commits of `origin/main`.
- Not run: `preflight.sh --full` (needs the Docker stack); gentle-ai's and agentic-os's suites.
- The Stop hook depends on Claude Code's `stop_hook_active` field; other runtimes keep the
  SessionStart reminder only.
