# AI SDLC framework kit

A stack-agnostic kit for running a software delivery lifecycle in which AI agents do most of
the work and **machines, not agents, decide whether the work is done**. One policy document,
one TOML profile, and a zero-dependency CLI (`aisdlc`, Python 3.11+ standard library).

It was extracted from the process that builds Notaire (#1958). The design, the evidence behind
every rule, and what remains open are in the report:
[`docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md`](../../docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md).

## Quick start (any stack)

```bash
# 1. One-command install: copies the kit and scaffolds the profile (#2135).
#    From a checkout that already contains framework/ai-sdlc (or any aisdlc bin):
cd /path/to/your-repo
python3 /path/to/framework/ai-sdlc/bin/aisdlc install --path tools/ai-sdlc --name your-product --level 2

# Equivalent manual steps remain: copy the kit, then `aisdlc init`.

# 3. Replace the placeholder gates with your commands, then check the profile.
$EDITOR aisdlc.toml
python3 tools/ai-sdlc/bin/aisdlc doctor

# 4. Use the stages.
python3 tools/ai-sdlc/bin/aisdlc change new add-login --issue 42 --title "Add login"
python3 tools/ai-sdlc/bin/aisdlc gate plan
python3 tools/ai-sdlc/bin/aisdlc gate red --var test=tests/test_login.py
python3 tools/ai-sdlc/bin/aisdlc gate pre-push
python3 tools/ai-sdlc/bin/aisdlc report
```

Mirror the stages in CI with [`templates/integrations/github-actions.yml`](templates/integrations/github-actions.yml)
and locally with [`templates/integrations/pre-push`](templates/integrations/pre-push).



## Telemetry and flags (#2125)

`[telemetry]` defaults to off. When `enabled = true` and `exporter = "none"`, the kit binds
OpenTelemetry's `NoOpTracerProvider` if the optional packages in `optional/telemetry.txt` are
importable (`verify.sh` installs them into `.venv-telemetry` for tests when `ensurepip` /
`python3-venv` is available; otherwise it WARNs and continues stdlib-only — optional must not
block the kit suite, #2180). Prompts, diffs and
secrets are redacted. Stage duration and `aisdlc cost` feed lead-time/cost hooks; the evidence
ledger remains authoritative. `OTEL_SDK_DISABLED` is honored; `TRACEPARENT` is not injected on
the no-op path. GenAI semantic conventions are not claimed.

`aisdlc/flags.py` is the FeatureFlagPort (#2137): JSON/flagd file provider when `[flags] enabled`,
otherwise off. `core.*` / `security.*` cannot relax policy; required gates cannot be skipped;
`factory.max_open_prs` cannot be raised by a flag. Not OFREP or full SDK conformance.

## Concepts

| Concept | What it is | Where |
|---------|------------|-------|
| **Policy** | The one process document, above every agent file, rule and prompt. Names no tool. | `CONSTITUTION.md` (template) |
| **Card** | ≤3 KB always-loaded digest of the policy; everything else loads on demand. | `AGENT-CARD.md` |
| **Profile** | The only project-specific input: stages, gate commands, ratchets, globs, roles, prices. | `aisdlc.toml` or `.aisdlc/aisdlc.toml` |
| **Stage** | A lifecycle point with a hard gate: `intake` G0 · `plan` G1 · `red` G2 · `green`/`pre-push`/`pre-pr` G3 · `merge` G4 · `release` G5. | `[[gates]] stage` |
| **Gate** | A shell command, an exit code and an expectation (`pass` or `fail`). Nothing an agent says counts. | `[[gates]]` |
| **Ratchet** | Legacy debt as a baseline that may only shrink, or a metric with a floor that may only rise. | `[[ratchets]]` |
| **Change record** | Why, what, acceptance scenarios, test plan, traceability — for one change only, citing a real issue. | `.aisdlc/changes/` or your spec engine |
| **Roles** | Owner (human), Foreman (orchestrates, merges under the rule), Workers (implement), Reviewer. | `[roles.*]`, `.aisdlc/agents/` |
| **Evidence** | Append-only, hash-chained JSONL of every stage run and every priced agent run; `aisdlc report` reads it, `aisdlc evidence verify` checks it. | `.aisdlc/evidence/` |

## Commands

| Command | Does | Exit 1 when |
|---------|------|-------------|
| `install --path P --name N [--level 1-4] [--force]` | Copies this kit to relative in-repo `P` (default `tools/ai-sdlc`) and scaffolds the profile. Absolute/`~/` paths are refused (they must not strip into the repo). Refuses product trees. | destination exists without `--force`, path absolute/forbidden |
| `update [--path P]` | Re-copies the kit idempotently; leaves adopter policy/evidence alone. | kit path missing |
| `uninstall [--path P] [--purge]` | Removes the kit tree (+ install manifest). `--purge` also drops scaffold and evidence. | forbidden path |
| `init --name N [--level 1-4] [--force]` | Scaffolds policy, card, `AGENTS.md`, profile, role agents, change folder. Never overwrites without `--force`. | — |
| `contract show\|check` | Factory control OpenAPI 3.1.0 path and structural validation (#2136). | contract missing or not 3.1.0 |
| `doctor` | Validates the profile and the files it names; warns on placeholders and on what the level still lacks; INFO line for Isolator `no-evaluable` (ADR-004, never a sandbox claim). | the policy, a baseline, a context file, a skills dir or a gate script is missing |
| `config validate` | Checks factory knobs, trusted plugin manifests, the migration inventory when `[factory] inventory` is set, and stage→catalog refs when `[[stages]]` is declared (#2182). A partial failure is not success. | any of those checks fail |
| `config show --redacted [--json] [--max-open-prs N]` | Prints the resolved factory view (includes optional `stages` knobs). Refuses to run without `--redacted`. Cannot raise the profile pull-request ceiling. | — (exit 2 on usage or an attempt to raise the ceiling) |
| `stage list [--json]` | Lists optional per-stage knobs (`agents` / `skills` / `mcp` / `model` / `effort`). Read-only; never executes MCP or agents (#2182). | — |
| `plugin list` · `plugin capabilities` · `plugin plan --id ID --var K=V` | Reads trusted manifests. `plan` prints argv and does not run it. Untrusted ids are listed as unavailable. | — (exit 2 when the id is not available) |
| `inventory check` | Validates `[factory] inventory`. Requires both migrated and planned rows, so a file cannot claim the move is finished. | the inventory is missing, inconsistent, or secret-shaped |
| `harness list [--json]` · `harness show --id ID [--json]` | Lists profile `[harness.adapters]` or reads one source in place. Always reports `sandbox=false`. Not a commercial Harness product. | show: source missing or runtime mismatch (exit 1); unknown id (exit 2) |
| `gate <stage> [--base R] [--var K=V] [--stream]` · `gate --list` | Runs every gate of the stage; path-scoped gates skip when nothing they cover changed; writes one ledger record. | a required gate does not exit as expected |
| `ratchet [--base R] [--name N]` | Baselines (lines, or JSON leaf counts) may not gain entries versus the base revision; floors must hold. | an entry was added or a floor missed |
| `catalog list\|add\|update\|remove\|validate` | CRUD for skills/MCP/agents/memory catalogs (#2139); digests on write; never executes. `list` also surfaces `[skills] dir` folders as read-only `source=skills-dir` rows (#2155). | unknown kind / missing dir on write |
| `skills` | Every skill folder has a `SKILL.md` with a description, names are unique, generic skills carry no forbidden project pattern. | any of those |
| `budget` | Always-loaded `[context].files` ∪ memory catalog pack `ref`s ≤ `max_tokens` (bytes/4). Pack ids must exist; list/budget never execute packs (#2186). | over budget, missing file/pack/ref |
| `commits [--base R]` | Commit subjects match `[commits] pattern` (Conventional Commits by default). | a subject does not match |
| `tdd [--base R]` | Production globs changed ⇒ test globs changed too. | code without tests |
| `protected [--base R]` | Owner-only paths (`[protected] paths`) changed since the base: the policy, gates, harness and merge path. Agents may propose such a change; a person merges it. | an owner-only path changed |
| `committer [--base R] [--json]` | Sole-committer **dry-run** (#2167/#2169): flag agent-authored commits vs runtime allowlist. **no-evaluable** until `runtime_names`/`runtime_emails` set; exit 1 only if `required = true`. Never claims OS isolation. | required + runtime configured and agent commit found |
| `size [--base R] [--limit N]` | Changed lines since the base stay within the review budget (`[size] limit`, default 400); `exclude` globs and binary files do not count. | over the budget |
| `scope [--base R] [--strict]` | Changed paths stay inside the write set that the change record declares under `[scope] heading` (default `### Write set`, globs in backticks; HTML comments ignored). No record with a write set ⇒ *not evaluable* (exit 0, or 1 with `--strict`). | a path outside the write set |
| `evidence verify [--anchor D]` | The ledger and the cost log are hash-chained (`prev` = SHA-256 of the line before); reports any edited, deleted or reordered record and prints the head digest. A chain cannot see a truncated tail, so anchor the digest outside the agents' reach (PR body, commit) and pass it back with `--anchor`: the check fails if that record is gone. An empty ledger reports NO EVIDENCE, never PASSED. | the chain is broken, or an anchored record is missing |
| `change new NAME --issue N` · `change check [NAME…]` | Built-in change records; `check` needs every section filled and the issue resolvable by `[tracker] verify`. | an empty section, no issue, an unresolvable issue |
| `metrics lead-time (--fixture F \| --issue N) [--json]` | Lead time: `issue.created_at` → earliest linked PR `checks_green_at` (fixture #2138 or live read-only ForgePort #2184). | no evaluable PR |
| `metrics stages [--json] [--since DATE]` | Stage duration medians from the evidence ledger. | — |
| `report [--json] [--since DATE] [--html PATH] [--lead-time-fixture F]` | Pass rate, median duration and last status per gate; cost; Wilson L0 `autonomy` from allowlisted stages (#2165/#2171). `--html` writes a read-only static monitor (autonomy/stage/cost/lead-time); never renders prompts, diffs or secrets (#2142). | — |
| `improve propose [--json] [--since DATE]` | Evidence → proposal artifact under `.aisdlc/proposals/` (friction → candidate paths). Empty friction is `no-evaluable` and writes no file (#2157). Never commits; never mutates policy in place (#2141). | — |
| `improve validate --file F` | Proposal stays `proposed`/`commits=false`; agent cannot approve; protected candidates require Owner merge. | self-approval or wrong merge rule |
| `improve autonomy [--json] [--since DATE]` | Wilson L0 from allowlisted stages (`signal`, `pre-push`; skip ≠ pass); L1 stays no-evaluable until ≥2 stages have min_n (#2171); demote to L1 on first incident; never claims L3 (#2163). | — |
| `cost --agent A --model M --input N --output N [--usd X] [--issue N]` | Records an agent run's tokens, priced from `[cost.prices]` when `--usd` is omitted. Also feeds the TelemetryPort cost hook (no-op unless `[telemetry] enabled`). | — |

Exit code 2 always means usage or configuration error (no profile, invalid profile, unknown base revision).

### Gate commands

- `{base}` is the base revision, `{root}` the project root, `{profile}` the profile path; `--var key=value`
  fills any other `{key}`. Unknown braces are left alone, so `awk '{print}'` survives.
- A command that starts with `aisdlc` calls this kit with the same root and profile.
- `expect = "fail"` turns a gate into a red gate: it passes only when the command fails.
- `required = false` makes a gate advisory; `placeholder = true` reports it as TODO without running it.
- `paths = ["src/**"]` skips the gate when no file changed against the base matches.

## Profile reference

```toml
[project]
name = "acme"                 # required
policy = "CONSTITUTION.md"    # the single process document
level = 2                     # 1 guided · 2 gated · 3 fleet · 4 autonomous
base = "origin/main"          # default base revision

[tracker]
issue_pattern = '#(\d+)'      # group 1 is the issue id
verify = "gh issue view {issue} --json number"   # optional live check (Gate 1)

[spec]
dir = ".aisdlc/changes"
sections = ["Why", "What changes", "Acceptance scenarios", "Test plan", "Traceability"]

[commits]
pattern = '^(feat|fix|docs|chore|test|refactor|ci|build|perf|style|revert)(\(.+\))?!?: .+'

[tdd]
production = ["src/**"]
tests = ["tests/**", "**/*_test.go"]   # a leading **/ also matches at the root

[context]
max_tokens = 8000
files = ["AGENTS.md", "AGENT-CARD.md"]
memory = ["agent-card"]            # optional; pack ids from catalogs/memory (#2186)

[skills]
dir = ".claude/skills"
generic = ["qa-automation-strategy"]
forbid_in_generic = ['acme', 'services/billing/']

[evidence]
dir = ".aisdlc/evidence"

[protected]                   # owner-only paths: policy, gates, harness, merge path
paths = ["CONSTITUTION.md", "aisdlc.toml", ".github/workflows/**"]
owner = "the Owner"

[committer]                   # sole-committer dry-run (#2167); required=false is advisory
required = false
agent_names = ["cursor[bot]", "github-actions[bot]"]
agent_emails = ["*@users.noreply.github.com"]
agent_trailers = ["Made-with: Cursor"]

[size]                        # review budget
limit = 400
exclude = ["**/*.lock", "docs/changes/**"]

[scope]                       # write set declared by the change record
heading = "Write set"
always = ["CHANGELOG.md"]

[[harness.adapters]]          # agent runtimes; sources stay in place; never a sandbox claim
id = "cursor-cloud"
runtime = "cursor-cloud"
source = "docs/fleet-manifest.yaml"

[roles.foreman]
tier = "mid"                  # human | top | mid | low
model = "model-id"
may_merge = true

[[gates]]
name = "unit tests"
stage = "pre-push"
run = "make test"
expect = "pass"               # or "fail" (red gate)
required = true
timeout = 1800
paths = ["src/**", "tests/**"]

[[ratchets]]
name = "lint baseline"
kind = "baseline"             # file: lines (blank and # ignored) or .json leaf counts
file = "lint-baseline.txt"

[[ratchets]]
name = "coverage"
kind = "floor"                # the last number the command prints
run = "make coverage-percent"
floor = 80.0
direction = "min"             # "max" makes it a ceiling (e.g. warnings)

[cost.prices."model-id"]      # USD per million tokens
input = 3.0
output = 15.0
```

Worked examples for four stacks are in [`profiles/`](profiles/) (Python, TypeScript web, Java/Maven,
Go with Jira-style commits). Notaire's own profile is [`.aisdlc/aisdlc.toml`](../../.aisdlc/aisdlc.toml).

## Adoption levels

Pick the level that matches team size, risk and how much agents do. Each level adds to the one below;
`aisdlc doctor` warns about what the declared level still lacks.

| Level | Name | Fits | Adds |
|-------|------|------|------|
| 1 | Guided | One developer with an assistant; prototypes | Policy, card, profile, change records, `plan` and `pre-push` gates |
| 2 | Gated | Small team, agents write most code | Red gates, ratchets for legacy debt, CI mirror of the local gates, evidence report |
| 3 | Fleet | Several agents in parallel, one human Owner | Roles with a merge rule, `merge` gate on the full suite, serialized heavy CI, claim protocol, continuous publication |
| 4 | Autonomous | Bounded autonomous runs (e.g. local models) | `release` gate with smoke tests, cost prices, a harness that owns ledgers and pushes, measured autonomy per stage |

Do not adopt a level for prestige: Notaire's history shows the process tax is real (see the report, §7).

A level is earned and lost per operation: it rises only with ledger evidence, and an incident (an
operation outside its level, or a merge that had to be reverted) lowers it until its cause is fixed.
At level 3 and above, an agent role that may merge needs `[protected]` paths; `doctor` warns otherwise.

## Extending

- **Another stack:** write a profile; the kit does not change.
- **A spec engine** (OpenSpec, Spec Kit…): keep it; call its validator from a `plan` gate.
- **Another agent runtime** (Claude Code, Cursor, Codex, OpenCode, Copilot): point its entry file at
  `AGENTS.md`; role files in `.aisdlc/agents/` use plain frontmatter every runtime can read.
- **A custom stage:** any `kebab-case` name works; `doctor` notes it is not canonical.
- **New checks:** any command with an exit code is a gate. Prefer a gate over a sentence in the policy.

## Development

```bash
python3 -m unittest discover -s framework/ai-sdlc/tests   # temp git repos, no network
bash framework/ai-sdlc/verify.sh                          # tests + Notaire profile doctor
```
