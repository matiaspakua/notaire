# Cursor Cloud AI SDLC Fleet Architecture

> **Scope:** Cursor Cloud agents only. The macOS `local-ai/` harness is a separate
> execution path and must **not** be invoked, extended, or required by this fleet.
> Ideas borrowed from `local-ai/sdlc/AI-SDLC.md` (phased SDLC, deterministic gates,
> foreman-owned merge) are restated here for cloud agents; the shell harness itself
> is out of scope.

---

## 1. Goals

1. Drive a full Constitution-compliant loop on Cursor Cloud:
   **pick issue → OpenSpec Gate 1 → branch → TDD → implement → docs → preflight → PR → CI → review → merge → next**.
2. Split work across a **foreman** (orchestrator) and **specialized sub-agents** mapped to
   `.claude/skills/` and `.claude/agents/`.
3. Prefer **cost-to-value** Cursor model slugs per role (strong models only where judgment fails cheaply).
4. Keep gates **mechanical** (`openspec validate`, `validate-sdlc-plan.sh`, `preflight.sh`,
   `gh pr checks`) — never trust an agent’s “all green” claim without a command exit code.
5. Respect **in-repo module ownership** ([MODULE-OWNERSHIP.md](../MODULE-OWNERSHIP.md), ADR-026):
   pick work by `python3 workspace/modules.py affected <path>` and keep #1197 Phase 0
   (path-scoped CI, metrics, context trim, OpenAPI types) ahead of any repository split.
6. Sync with sibling agents via **GitHub issue status** (board Status / `in-progress` when the
   token allows; otherwise PR `Closes #n` and issue comments). Do not invent parallel trackers.

---

## 2. High-level topology

```text
┌─────────────────────────────────────────────────────────────┐
│  FOREMAN (cloud-foreman)                                    │
│  issue pick · OpenSpec gate · dispatch · PR · CI · merge    │
└─────────────┬───────────────────────────────────────────────┘
              │ handoff contracts (JSON/Markdown briefs)
     ┌────────┼────────┬──────────┬──────────┬─────────┐
     ▼        ▼        ▼          ▼          ▼         ▼
  openspec  backend  frontend  testing    devops   security
  planner   impl     design    / QA       eng      auditor
     │        │        │          │          │         │
     └────────┴────────┴──────────┴──────────┴─────────┘
                    ▲
                    │ Gate 4
              code-reviewer + sync_issues_and_code
```

All agents read `CONSTITUTION-AGENT-CARD.md` (full `CONSTITUTION.md` on demand), slim
`AGENTS.md`, and the skill(s) listed in
[`fleet-manifest.yaml`](fleet-manifest.yaml). Product authority stays in permanent docs;
OpenSpec artifacts describe **only the change**.

---

## 3. Foreman responsibilities

The foreman (`cloud-foreman`) owns the loop. Specialists do not pick the next issue,
merge, or skip gates.

| Phase | Foreman action | Deterministic check |
|-------|----------------|---------------------|
| **Pick** | Select one open issue that is not an epic/roadmap umbrella; verify Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) in body | `gh issue view <n> --json title,body,labels,state` — state `OPEN`, UC present |
| **Triage** | Dispatch analyst / openspec-planner for refine + surface map (`backend` / `frontend` / both / `none`) | Written brief: issue #, UC, TYPE, SURFACE, acceptance criteria list |
| **Gate 1** | Prefer `bash workspace/sdlc/seed-openspec-change.sh <change> --issue N --use-case "CU…" --branch … --create` then fill; ensure OpenSpec change exists (`schema: notaire-sdlc`); run plan validation | `openspec validate <change> --strict` + `bash workspace/sdlc/validate-sdlc-plan.sh <change>` |
| **Branch** | Create `<type>/<issue-number>_<description>` from updated `main`; label `in-progress` | Branch name regex + `gh issue edit … --add-label in-progress` |
| **Dispatch implement** | Route by SURFACE to backend / frontend / both; require TDD-first | Specialist commits show failing tests before green commits |
| **Quality** | Run local CI mirror before push | `bash workspace/sdlc/preflight.sh` (and `--full` when stack is up) |
| **PR** | Open/update PR; body links issue with **`Closes #n`**; title `[#n] type(scope): …` | `gh pr view` / ManagePullRequest; every closing commit must end with `Closes #n` (not merely `Issue: #n`) |
| **CI watch** | Subscribe or poll checks; on failure dispatch CI-fix specialist with failing job logs | Heavy CI + Playwright terminal success on last non-`[skip ci]` commit — **not** light-only (~12) green; see [`CI-MERGE-GATE.md`](CI-MERGE-GATE.md) |
| **Gate 4** | Dispatch `code-reviewer` (+ `security-auditor` when auth/secrets/schema) | Review verdict PASS or concrete FIX notes |
| **Gate 5** | Merge via PR only; wait CI/CD on `main`; smoke `/actuator/health`; close issue; archive OpenSpec change | Health UP; issue CLOSED (proves `Closes #` worked); `openspec archive` when applicable |
| **Next** | Update run ledger; pick next eligible issue | Do not start a second concurrent product issue on the same worktree without isolation |

### Foreman must never

- Implement product code itself when a specialist is available (except trivial one-line CI fixes after two specialist retries).
- Call or depend on `local-ai/sdlc/foreman.sh`, oMLX, Codex local profiles, or `../notaire-localai` worktrees.
- Merge with failing CI or without Gate 4 PASS.
- Merge when only light CI is green (PR Validation + Frontend + SDLC ~12 checks) while `CI - Build, Test & Security` or Playwright is still pending — see [`CI-MERGE-GATE.md`](CI-MERGE-GATE.md).
- Fabricate Issue numbers or Use Cases.
- Close the loop with only `Issue: #n` in commits — **issues stay OPEN** unless commits/PR use a GitHub closing keyword (`Closes #n`).
- Commit PR Validation wiki reports onto PR heads (especially with `[skip ci]`). That pattern was removed from `pr-validation.yml` on `main` (#1111 / #1117); agents must not reintroduce it by hand.
- Assume bridge Docker networking works in Cloud VMs — use `docker-compose.cloud.yml` (host network). Until the Environment card is Saved, run `bash .cursor/install.sh` for `openspec` + `bc` (and the rest of the toolchain).
- Treat draft environment builds as a substitute for a **Saved** Environment card with `install=bash .cursor/install.sh` and `start=bash .cursor/start.sh`.
- Skip `workspace/sdlc/seed-openspec-change.sh` and hand-author empty OpenSpec templates (leftover `<!-- -->` bodies fail Gate 1 validation — #1108 / #1116).

---

## 4. Specialized agents ↔ skills ↔ models

Model slugs are Cursor Cloud / subagent slugs. Prefer the **recommended** column; use
**upgrade** when the role fails twice on the same gate. Avoid max/xhigh tiers unless Gate 1
architecture or security review is blocked.

| Role | Agent def | Primary skills | Recommended model | Upgrade | Rationale |
|------|-----------|----------------|-------------------|---------|-----------|
| **Foreman** | `cloud-foreman` | `ai-agent-workflow`, `devsecops-traceability`, `ci-cd-quality-gates` | `claude-sonnet-5-5-medium` | `claude-opus-5-5-medium` | Orchestration + tool use; Opus only when stuck on process judgment |
| **OpenSpec Planner** | `openspec-planner` | `openspec-propose`, `openspec-update-change`, `openspec-triage`, `analyst`, `product-owner` | `claude-sonnet-5-5-high` | `claude-opus-5-5-medium` | Spec quality dominates Gate 1; worth a stronger write model |
| **Analyst** | (via planner or `analyst` skill) | `analyst`, `delivery-maturity-roadmap` | `claude-sonnet-5-5-medium` | `gpt-5.6-terra-medium` | Requirements/UC mapping; medium is enough |
| **Java Architect** | `java-architect` | `java`, `hexagonal-arch`, `architecture-decision-design`, `flyway` | `claude-opus-5-5-medium` | `claude-opus-5-5-high` | Package/migration decisions are high leverage |
| **Backend Implementer** | `backend-implementer` (+ `efficiency_config_agent`) | `backend`, `java`, `programming`, `maven-build`, `flyway`, `openspec-apply-change` | `claude-sonnet-5-5-medium` | `claude-sonnet-5-5-high` | Bulk TDD/implement; Sonnet is cost-effective |
| **Frontend Design** | `frontend-design` | `frontend-design`, `motion-design` | `claude-sonnet-5-5-high` | `gpt-5.6-terra-medium` | Design-system fidelity; avoid weak coding-only models |
| **Testing / QA** | `testing-qa` | `testing`, `qa-automation-strategy`, `api-rest`, `api-contract-testing` | `claude-sonnet-5-5-medium` | `claude-sonnet-5-5-high` | Test design + red proof; medium default |
| **DevOps** | `devops-engineer` | `devops`, `ci-cd-quality-gates`, `operations-observability-readiness` | `claude-sonnet-5-5-medium` | `gpt-5.6-terra-medium` | Compose/CI scripts; Sonnet matches existing agent default |
| **Security** | `security-auditor` | `secure-threat-modeling`, `backend` | `claude-opus-5-5-medium` | `claude-opus-5-5-high` | Authz/secrets false negatives are expensive |
| **Code Reviewer** | `code-reviewer` | `ai-agent-workflow`, `programming` | `claude-sonnet-5-5-high` | `claude-opus-5-5-medium` | Gate 4 needs careful diff reading |
| **Issue Sync** | `sync_issues_and_code` | `devsecops-traceability`, `product-owner` | `composer-2.5-fast` | `claude-sonnet-5-5-low` | Label/link chores; cheapest reliable slug |
| **CI Fix loop** | `backend-implementer` or `devops-engineer` | `ci-cd-quality-gates`, `maven-build` | `composer-2.5` | `claude-sonnet-5-5-medium` | Narrow log→patch cycles; Composer for cost |

**General-purpose fallback:** `efficiency_config_agent` with `claude-sonnet-5-5-medium` when SURFACE is unclear or the change spans docs-only chores.

### Models to avoid for this fleet

| Avoid | Why |
|-------|-----|
| Local oMLX / Codex `omlx*` profiles | Wrong runtime (`local-ai/`) |
| Ultra-expensive max/xhigh as default | Poor cost-to-value for routine TDD/CI |
| Flash/minimal models for Gate 1 or Gate 4 | Spec and review regressions burn retries |

---

## 5. Handoff contracts

Every foreman → specialist dispatch uses a **brief** (Markdown or YAML frontmatter).
Every specialist → foreman return uses a **result** with exit evidence.

### 5.1 Brief (foreman → specialist)

```yaml
# handoff-brief
issue: 1234
use_case: CU-12
change: short-kebab-name          # OpenSpec change folder name
branch: feat/1234_short_desc
surface: backend                  # backend | frontend | backend,frontend | none
phase: implement                  # triage | spec | tests | implement | docs | ci-fix | review
skills:
  - backend
  - java
  - openspec-apply-change
constraints:
  - "TDD: failing tests committed before implementation"
  - "Do not edit local-ai/"
  - "Do not push without bash workspace/sdlc/preflight.sh"
inputs:
  issue_url: https://github.com/matiaspakua/notaire/issues/1234
  acceptance_criteria: []
  prior_gate_log: ""              # attach failing command output on retries
done_when:
  - "TEST_CMD green"
  - "tasks.md groups 4-5 ticked only as [ ]→[x]"
```

### 5.2 Result (specialist → foreman)

```yaml
# handoff-result
issue: 1234
phase: implement
status: PASS | FAIL | BLOCKED
summary: "one sentence"
commands_run:
  - cmd: "mvn -q -B test -pl backend-api -Dtest=FooTest"
    exit_code: 0
artifacts:
  - path: "docs/openspec/changes/.../tasks.md"
commits: ["abc1234"]
blockers: []                      # if BLOCKED: missing UC, flaky env, etc.
next_recommended: quality         # foreman decides
```

### 5.3 Phase ownership (cloud mapping of Constitution)

| Constitution / local phase idea | Cloud owner | Notes |
|---------------------------------|-------------|-------|
| Triage / refine | Foreman + OpenSpec Planner | No product commits |
| Spec (Gate 1) | OpenSpec Planner | Foreman runs validators |
| Tests red (Gate 2) | Testing/QA (+ Backend/Frontend) | Foreman verifies failure |
| Implement | Backend / Frontend / Architect | Foreman verifies suite |
| Docs | Implementer or Planner | Permanent docs only |
| Quality / pipeline | Foreman (+ DevOps if infra) | `preflight.sh` / `run_pipeline.sh` |
| PR | Foreman (+ Issue Sync) | Specialists may draft body |
| CI | Foreman watches; CI Fix specialist | Deterministic checks |
| Review (Gate 4) | Code Reviewer (+ Security) | Foreman merges only after PASS |
| Merge (Gate 5) | Foreman only | Archive OpenSpec after close |

Reusable idea from local-ai (without the shell harness): **one phase per specialist run**,
fresh brief, mechanical gate, retry with gate output attached — not a monolithic “do the issue” prompt.

---

## 6. Explicit exclusion of `local-ai/`

| Item | Cloud fleet policy |
|------|--------------------|
| `local-ai/setup-omlx-codex.sh`, oMLX, Codex `--profile omlx*` | **Forbidden** as runtime dependency |
| `local-ai/sdlc/foreman.sh` and `bin/*` harness | **Do not execute** for Cloud SDLC |
| `.aisdlc/project.yml` | Local-harness adapter only; Cloud uses `fleet-manifest.yaml` + this doc |
| Worktree `../notaire-localai` | Not used; Cloud agent uses its own checkout |
| Ideas OK to reuse in prose | Phased gates, scope discipline, foreman-owned merge, never trust model success claims |

If a specialist finds itself reading `local-ai/` for product work, stop and re-route to
`.claude/skills/` + `CONSTITUTION.md`.

---

## 7. Integration with existing project assets

| Asset | Use in cloud fleet |
|-------|--------------------|
| `CONSTITUTION.md` | Highest process authority |
| `docs/openspec/` + `notaire-sdlc` | Gate 1 artifacts |
| `workspace/sdlc/seed-openspec-change.sh` | Prefer before filling Gate 1 templates (#1108) |
| `workspace/sdlc/validate-sdlc-plan.sh` | Constitution checks on plans (scenario sum via awk; `bc` optional) |
| `workspace/sdlc/preflight.sh` | Pre-push CI mirror |
| `workspace/sdlc/run_pipeline.sh` | Full Gate 3 when stack is up |
| `.cursor/install.sh` / `.cursor/start.sh` | Saved Environment card install/start |
| `docker-compose.cloud.yml` | Host-network compose for nested Docker |
| `.claude/skills/*` | Specialist playbooks |
| `.claude/agents/*` | Role prompts (this fleet extends them) |
| `.github/workflows/*` | CI truth; foreman watches — do not push `[skip ci]` wiki commits onto PR heads |
| `.env` / `.env.example` | Secrets; never commit `.env` |

---

## 8. Concurrency and isolation

- Default: **one product issue per foreman run**.
- Parallel specialists on the **same** issue are allowed only for read-only review (security + code-review) or non-overlapping paths explicitly listed in the brief.
- Never two writers on the same branch without foreman serialization.

---

## 9. Process learnings (post #1111 / #1112 / #1116)

Operational failures while landing the fleet. Full table:
[`ENVIRONMENT-CHECKLIST.md` §7](ENVIRONMENT-CHECKLIST.md).

1. **`Closes #<issue>` is mandatory** on closing commits — `Issue: #N` does not auto-close.
2. **Never commit PR Validation wiki reports onto PR heads with `[skip ci]`** — fixed in `pr-validation.yml` on `main`; do not reintroduce.
3. **Nested Docker** needs host-network compose (`docker-compose.cloud.yml`). Run `bash .cursor/install.sh` until the Environment card is Saved (`openspec` + `bc` come from that script; Gate 1 no longer hard-depends on `bc`).
4. **Save** the Environment card with `.cursor/install.sh` / `.cursor/start.sh`; draft builds are not enough.
5. **Prefer** `workspace/sdlc/seed-openspec-change.sh` before filling Gate 1.
6. **Never merge on light-CI-only green** — Unit, Integration, Coverage Gate, Bruno, and Playwright must be terminal success. Gate: `bash workspace/sdlc/check-heavy-ci.sh <pr>` ([`CI-MERGE-GATE.md`](CI-MERGE-GATE.md); #1126 / #1128 / #1134). Do not trust CI subscription “all N checks success” alone.
7. **Stale PR: rebase first** — Budget/person / `undefined, undefined` Integration or Playwright failures on a tip behind `main` are usually fixed by rebasing onto #1132’s nested `BudgetResponse.person`, not by new product code.
8. **CodeQL: advanced XOR default setup** — do not enable GitHub Code Scanning default setup beside `.github/workflows/codeql.yml`; use `wait-for-processing: false` and/or `security/enable-gh-secure.sh --apply` ([DevSecOps](../../200-architecture/208-devsecops/README.md#codeql-advanced-vs-default-setup)).
9. **Serialize heavy CI** — prefer one heavy-CI PR at a time; docs/rebase tips wait; do not open new product PRs until the in-flight Integration/Playwright suite finishes ([`CI-MERGE-GATE.md` — Runner contention](CI-MERGE-GATE.md#runner-contention--serialize-heavy-ci)).

---

## 10. Related docs

- [`ENVIRONMENT-CHECKLIST.md`](ENVIRONMENT-CHECKLIST.md) — Cloud environment.json inputs + process learnings table
- [`CI-MERGE-GATE.md`](CI-MERGE-GATE.md) — light CI false-positive; required terminal checks before merge
- [`../CI-PREFLIGHT.md`](../CI-PREFLIGHT.md) — local gates mirroring CI (`preflight.sh`)
- [DevSecOps / CodeQL](../../200-architecture/208-devsecops/README.md#codeql-advanced-vs-default-setup) — advanced vs default setup
- [`VALIDATION-PLAN.md`](VALIDATION-PLAN.md) — readiness before first issue
- [`fleet-manifest.yaml`](fleet-manifest.yaml) — role map
- Local (reference only): `local-ai/sdlc/AI-SDLC.md`
