# AI SDLC Audit — foreman/worker harness and Notaire guardrails

| Field | Value |
|-------|-------|
| Issue | #1079 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Date | 2026-09-28 |
| Scope | `CONSTITUTION.md`, OpenSpec `notaire-sdlc` schema, `.claude/` (rules, skills, agents, hooks), `.github/workflows/`, `scripts/preflight.sh`, `local-ai/sdlc/` |
| Evidence | Harness runs #1069 (PR #1073) and #1063 (in flight), PRs #1076 and #1078, last 40 merged PRs |

## 1. Verdict

**Notaire's process is well defined. A local model cannot yet run it on its own.**
The foreman/worker harness is a good *reference implementation* of the pattern.
It is not yet a reusable *factory*.

| Question | Answer |
|----------|--------|
| Is the process well defined? | **Mostly.** CONSTITUTION + OpenSpec schema + `validate-sdlc-plan.sh` give a strict, machine-checkable Gate 1. Parts of the rule set are stale or contradict it (§3.1). |
| Can a local 9B model do it alone? | **No.** The foreman stepped in during every phase of both observed runs (§2). |
| Is it a good foreman/worker example? | **Yes, as a design.** One phase per worker run, deterministic gates, retry feedback, scope/ref guards, a harness-owned ledger and a human merge are the right shape. |
| Is it portable to other projects? | **No.** Paths, Maven/npm, `gh`, oMLX and the Notaire schema are hardcoded in a single 717-line `foreman.sh` (§3.5). |

## 2. Evidence: how autonomous is the worker?

| Run | Worker runs | Retries | Gate failures | Foreman notes | Foreman STOPs | Wall time |
|-----|-------------|---------|---------------|---------------|---------------|-----------|
| #1069 → PR #1073 | — | — | 6 of 37 gate runs | 8 review files | — | ~54 min PR open → merge |
| #1063 (through the tests phase) | 16 | 17 | 10 | 5 | 3 | still running |

These are typical failures. The gates caught every one, but a human-grade reviewer had to explain the fix:

- **Spec drift:** wrong config key names, invented requirements (scope creep), traceability rows that point at files the change never touches, `CLAUDE.md` edits that were not asked for.
- **Test design:** in #1063 the worker created a *duplicate* `JacocoCoverageFloorTest` instead of extending the existing `JacocoCoverageConfigConsistencyTest` named in `design.md`. It also hardcoded `/Users/<user>/workspace/notaire-localai` as the base path. The red gate would pass; review had to catch it.
- **Process violations:** committing in phases where the harness owns commits (caught by the ref guard), mislabelled commit types (`feat(test)` on a spec commit), and three spec commits that later had to be squashed.
- **Harness defects found by running it:** ref guard vs. worktree-checked-out branches, a FORBIDDEN-pattern bug, `RECHECK=1` skipping the worker so it never saw review notes, bash 3.2 empty-array expansion, no auto-archive after merge.

**Conclusion:** the gates reliably tell *pass* from *fail*. They cannot tell a *correct* pass from a *cheap* pass (duplicate test, absolute path, spec that validates but says the wrong thing). The foreman fills that gap by hand today.

## 3. Findings

Severity: **H** = blocks autonomy or allows wrong work to merge; **M** = costs foreman time; **L** = hygiene.

### 3.1 Policy layer (CONSTITUTION, rules, skills)

| # | Sev | Finding | Fix |
|---|-----|---------|-----|
| P1 | H | CONSTITUTION, `CLAUDE.md`, `AGENTS.md` and `.claude/rules/ai-agent-workflow.md` never mention the foreman/worker model. Who may approve Gate 1, merge, or waive §12 when the author is a local model? Nothing says. | Add a "Roles" section to the CONSTITUTION: human owner, foreman agent, worker agent. List each role's allowed actions and the gates each may sign. |
| P2 | H | `.claude/rules/ai-agent-workflow.md` has no OpenSpec/Specification step, and its "Step 9: Create PR + Close Issue" conflicts with the CONSTITUTION (issue closes on merge). The file was also truncated to 0 bytes on main between 2026-09-25 and PR #1078. | Rewrite it as a short pointer to the CONSTITUTION's steps. Add a CI check that every always-loaded rule file is non-empty. |
| P3 | M | `.claude/agents/issue-loop.md` (2026-08-01) runs a self-merging loop with no OpenSpec step and no failing-test-first check. Two automation paths now compete. | Retire it or make it call `local-ai/sdlc/foreman.sh`. |
| P4 | M | The CONSTITUTION is stale: it still describes the removed Swing client (lines 37-39, 210), `docs/300-development/specifications/` does not exist, and it was last reviewed 2026-08-08. `.github/workflows/e2e-swing.yml` still builds `frontend-swing` and has failed on every run since 2026-07-23. | Refresh pass, delete `e2e-swing.yml`, add a "last reviewed" freshness check to CI. |
| P5 | M | An uncommitted rewrite of `.claude/rules/refactoring.md` (triaged in PR #1078) required Service/ServiceImpl pairs, Redis, Sentry and `@RequestAttribute actorId`, which the code contradicts. Always-loaded rules are the highest-leverage prompt a worker reads, and nothing checks them against the code. | Treat rule files like code: PR + review, plus a lint step that flags referenced paths that do not exist. |
| P6 | L | Two spec systems: OpenSpec and SpecKit validation both run in CI. The `speckit/` drafts found on main were stubs. | Pick OpenSpec (it has the schema and validator). Archive SpecKit. |
| P7 | L | `.claude/skills/README.md` says skills have evals. None were found to run anywhere. | Remove the claim, or add a skill-eval job. |

### 3.2 Specification layer (OpenSpec `notaire-sdlc`)

| # | Sev | Finding | Fix |
|---|-----|---------|-----|
| S1 | H | 17 of the last 40 merged PRs touched no `openspec/changes/` path. Some are legitimate §12 docs/chore work, but nothing records that the §12 exception was *approved*. | CI: a PR with no change folder must carry an `sdlc-exception` label set by a human, or fail. |
| S2 | M | `validate-sdlc-plan.sh` checks structure: 12 task groups, required headings, open issue. It does not check meaning: traceability files exist, planned tests name real or new classes, no requirement lacks a scenario. | Add `traceability-check`: every planned file/test path either exists or is marked NEW, and each one appears in the final diff. |
| S3 | M | No automatic archive after merge, so finished changes sit in `openspec/changes/` and fail the "issue CLOSED" check on the next PR. | Post-merge job (or `foreman.sh <n> merge`) runs `openspec archive`. |
| S4 | L | Changes created without the `schema:` line are silently skipped by the validator. | Fail instead of skip. |

### 3.3 Enforcement layer (hooks, CI, preflight)

| # | Sev | Finding | Fix |
|---|-----|---------|-----|
| E1 | H | **No branch protection on `main`** (API returns 404). The only push guard is a Claude Code `PreToolUse` hook plus the harness's own `pre-push` deny. Any other agent or terminal can push to `main`. | Enable protection: required checks, one approving review, no force-push. |
| E2 | H | **TDD is not enforced outside the harness.** The harness has a `red` gate. CI, preflight and the Claude hooks do not. A red→green history is not checked on PRs. | CI job: for PRs that change `src/main`, check that a commit touching only tests exists and fails on its own (or that the harness ledger shows `red PASS`). |
| E3 | M | Commit messages are not checked. Only PR titles are (`pr-validation.yml` lines 45-52). The worker's `feat(test)` spec commit would have merged. | `commitlint` over the PR range, mirrored in `preflight.sh`. |
| E4 | M | Claude-side hooks cover only `SessionStart` and `PreToolUse(Bash: git push)`. There are no `Stop` hooks (block "done" while the preflight is red) and no `PostToolUse` hooks (format/lint an edited file right away). | Add a `Stop` hook that runs `preflight.sh --quick`. Add a `PostToolUse` hook for Spotless/Prettier on the edited path only. |
| E5 | M | Review-quality defects (duplicate classes, absolute paths, tests that assert nothing new) have no automated check. | Cheap static gates in the harness `tests` phase: no absolute home paths in `src/`, no new test class whose name overlaps an existing one, and the diff must add at least one assertion. |
| E6 | L | CD (`cd.yml`) is not in the harness `pipeline` phase. The harness stops at a green PR. | Fine for now. Document it as the human's step. |

### 3.4 Harness layer (`local-ai/sdlc`)

| # | Sev | Finding | Fix |
|---|-----|---------|-----|
| H1 | H | The foreman's review protocol (`review-<phase>.md` with OLD/NEW + CHECK/EXPECTED) is the most valuable part, and it is manual. The CHECK lines are machine-runnable, but nothing runs them. | `foreman.sh <n> check` runs every CHECK in the active review file and diffs the output against EXPECTED before the gate re-runs. |
| H2 | H | Worker quality is capped by the model (Qwen3.5-9B). Every phase needed foreman notes. | Route by phase: keep the local model for mechanical phases (tests scaffold, docs, fix-gate). Escalate spec and review to a stronger model, or keep a human/foreman there. Record the pass rate per phase to decide. |
| H3 | M | No run metrics beyond `gates.log`. Retries, notes and wall time were counted by hand for this audit. | `ledger.py` writes one JSON line per phase (attempts, gate results, foreman notes, tokens, duration). |
| H4 | M | Spec review rounds became extra commits until a manual amend fix. Squash policy is undocumented. | The harness owns history: one commit per phase, amended on every round. Document it in `AI-SDLC.md`. |
| H5 | M | Fresh-clone smoke test (task 12.1 of the harness change) was never run. | CI job: `foreman.sh --dry-run` on a fixture issue. |
| H6 | L | bash 3.2 quirks (`${arr[@]+…}`) are fixed case by case. | Require bash ≥4 in `setup`, or port the orchestrator to Python (see §4). |

### 3.5 Portability

`foreman.sh` mixes four concerns in one file: phase orchestration, git guards, Notaire-specific gate commands (`mvn`, `npm`, `validate-sdlc-plan.sh`, Playwright), and the model backend (`codex --profile omlx`). Prompts hardcode Notaire paths and an archived OpenSpec example. Nothing is configurable without editing code.

## 4. Target: a generic, modular AI-SDLC configuration

The goal is a kit that any repo can adopt by writing **one adapter file** and **one policy document**, while the harness stays identical across projects.

### 4.1 Layers

| # | Layer | Owns | Generic artefact | Project-specific input |
|---|-------|------|------------------|------------------------|
| L0 | **Policy** | Roles, gates, what "done" means, exceptions | `policy.md` template (roles, gate list, exception rules) | Project CONSTITUTION filled from the template |
| L1 | **Work intake** | Issue → use case → change | Issue template + `intake` phase prompt | Label names, use-case catalogue path |
| L2 | **Specification** | Proposal, requirements/scenarios, design, traceability, tasks | OpenSpec schema + semantic validator (S2) | Capability names, doc paths |
| L3 | **Phases** | One bounded task per worker run | Phase prompts with `{{placeholders}}` only | None. Prompts read the adapter. |
| L4 | **Gates** | Deterministic pass/fail per phase | Gate contract: `name`, `cmd`, `expect` (pass/fail), `timeout` | Commands in the adapter (`test_one`, `suite`, `lint`, `preflight`) |
| L5 | **Guards** | What the worker must never do | git hooks (pre-commit FORBIDDEN, pre-push deny, reference-transaction), scope guard, path lint | FORBIDDEN globs, per-phase scope regexes |
| L6 | **Ledger & metrics** | Truth about what happened | Append-only JSONL per phase; worker cannot write it | None |
| L7 | **Foreman protocol** | Review, notes, escalation, merge | `review-<phase>.md` format + `check` runner (H1) + escalation rules | None |
| L8 | **CI mirror** | Same gates on the server, enforced by branch protection | Reusable workflow that calls the adapter's commands | Runner images, secrets |
| L9 | **Model backend** | Which model runs which phase | Backend interface: `run(prompt, workdir, timeout) → exit, log` | Model/profile per phase |

### 4.2 The adapter (one file per project)

```yaml
# .aisdlc/project.yml — the only project-specific harness input
project: notaire
policy: CONSTITUTION.md
spec:
  tool: openspec
  schema: notaire-sdlc
  validate: [openspec validate {change} --strict, bash scripts/validate-sdlc-plan.sh {change}]
intake:
  use_case_dir: docs/100-business/102-use-cases
  labels: {in_progress: in-progress, docs: DOC}
gates:
  test_one: mvn -q test -pl backend-api -Dtest={test}
  suite: mvn -q test -pl backend-api
  lint_docs: frontend/node_modules/.bin/markdownlint-cli2 --no-globs {files}
  preflight: bash scripts/preflight.sh
guards:
  forbidden: ['.env', '**/*.pem', 'CONSTITUTION.md']
  scope:
    spec: '^openspec/changes/{change}/'
    tests: '^(backend-api/src/test/|frontend/src/.*\.test\.tsx?$)'
  path_lint: ['/Users/', '/home/']
backend:
  default: {cli: codex, profile: omlx, timeout: 3600}
  phases: {spec: {cli: claude, model: claude-sonnet-5}}
ci:
  required_checks: [build, test, sdlc-plan, code-lint]
```

### 4.3 Phase contract

Every phase declares the same five things, so the orchestrator stays generic:

1. **Inputs:** issue, triage, change folder, previous gate output, review notes.
2. **Scope:** which paths the worker may change (the scope guard reverts the rest).
3. **Gates:** the ordered gate list and the expected result (`tests` expects *fail*).
4. **Commit owner:** `harness` (one amended commit per phase) or `none`.
5. **Exit:** `pass` → next phase; `fail` → retry with `retry.md` up to N; then `STOP` for the foreman.

Phases: `intake → setup → spec (Gate 1, human/foreman sign-off) → tests (red) → implement (green + suite) → docs → quality (preflight) → pr → ci → review (foreman) → merge (human) → archive`.

### 4.4 Foreman contract

The foreman never edits product code. It may:

- write `review-<phase>.md` (OLD/NEW + CHECK/EXPECTED),
- run `check`, `recheck` (with the worker) or `stop`,
- change harness files through its own PR,
- sign Gate 1 and the final review in the ledger.

The human owner alone merges and approves policy exceptions.

### 4.5 What makes it a "factory"

- **Throughput:** several issues in parallel, one worktree + one run directory each (the ref guard already assumes this).
- **Learning loop:** each foreman note is tagged with a category (spec-drift, test-design, scope, process). Categories that repeat become a new deterministic gate or a prompt rule. This is how E5 was derived from #1063.
- **Measured autonomy:** per-phase pass-without-foreman rate from the ledger. A phase counts as autonomous when it passes review with no notes on ≥80% of runs.

## 5. Prioritised backlog

| Priority | Item | Refs |
|----------|------|------|
| 1 | Enable branch protection on `main` with required checks | E1 |
| 2 | Add roles (human/foreman/worker) to the CONSTITUTION and rewrite `ai-agent-workflow.md` | P1, P2 |
| 3 | Add static review gates to the harness: absolute paths, duplicate test class, new assertion | E5 |
| 4 | Automate CHECK/EXPECTED review notes (`foreman.sh <n> check`) | H1 |
| 5 | Enforce red→green and commit messages in CI | E2, E3 |
| 6 | Require a human `sdlc-exception` label on PRs without a change folder | S1 |
| 7 | Automate `openspec archive` on merge | S3 |
| 8 | Per-phase JSONL metrics in the ledger | H3 |
| 9 | Extract the adapter (`.aisdlc/project.yml`) and split `foreman.sh` into orchestrator / gates / guards / backend | §3.5, §4 |
| 10 | Remove stale material: Swing references, `e2e-swing.yml`, SpecKit, `issue-loop.md` | P3, P4, P6 |

## 6. Housekeeping done during this audit

- PR #1078: cleared 55 uncommitted files from the main checkout. Triage and backup are kept outside the repo in `notaire-wip-backup-2026-09-28/`.
- #1063: rejected the tests-phase commit (duplicate test class, absolute path) with a review note. The run continues under the harness.
- #1028: an abandoned agent worktree held a complete threat-model draft. It was committed as found and opened as a draft PR for Gate 1 review.
