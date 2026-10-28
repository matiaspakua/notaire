# AI SDLC framework kit

A stack-agnostic kit for running a software delivery lifecycle in which AI agents do most of
the work and **machines, not agents, decide whether the work is done**. One policy document,
one TOML profile, and a zero-dependency CLI (`aisdlc`, Python 3.11+ standard library).

It was extracted from the process that builds Notaire (#1958). The design, the evidence behind
every rule, and what remains open are in the report:
[`docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md`](../../docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md).

## Quick start (any stack)

```bash
# 1. Vendor the kit (copy, submodule or subtree) — it has no dependencies.
cp -R framework/ai-sdlc /path/to/your-repo/tools/ai-sdlc
cd /path/to/your-repo

# 2. Scaffold the policy, the profile, the always-loaded card and the role agents.
python3 tools/ai-sdlc/bin/aisdlc init --name your-product --level 2

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
| **Evidence** | Append-only JSONL of every stage run and every priced agent run; `aisdlc report` reads it. | `.aisdlc/evidence/` |

## Commands

| Command | Does | Exit 1 when |
|---------|------|-------------|
| `init --name N [--level 1-4] [--force]` | Scaffolds policy, card, `AGENTS.md`, profile, role agents, change folder. Never overwrites without `--force`. | — |
| `doctor` | Validates the profile and the files it names; warns on placeholders and on what the level still lacks. | the policy, a baseline, a context file, a skills dir or a gate script is missing |
| `gate <stage> [--base R] [--var K=V] [--stream]` · `gate --list` | Runs every gate of the stage; path-scoped gates skip when nothing they cover changed; writes one ledger record. | a required gate does not exit as expected |
| `ratchet [--base R] [--name N]` | Baselines (lines, or JSON leaf counts) may not gain entries versus the base revision; floors must hold. | an entry was added or a floor missed |
| `skills` | Every skill folder has a `SKILL.md` with a description, names are unique, generic skills carry no forbidden project pattern. | any of those |
| `budget` | Always-loaded files ≤ `max_tokens` (bytes/4). | over budget or a file is missing |
| `commits [--base R]` | Commit subjects match `[commits] pattern` (Conventional Commits by default). | a subject does not match |
| `tdd [--base R]` | Production globs changed ⇒ test globs changed too. | code without tests |
| `change new NAME --issue N` · `change check [NAME…]` | Built-in change records; `check` needs every section filled and the issue resolvable by `[tracker] verify`. | an empty section, no issue, an unresolvable issue |
| `report [--json] [--since DATE]` | Pass rate, median duration and last status per gate; cost per model and agent. | — |
| `cost --agent A --model M --input N --output N [--usd X] [--issue N]` | Records an agent run's tokens, priced from `[cost.prices]` when `--usd` is omitted. | — |

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

[skills]
dir = ".claude/skills"
generic = ["qa-automation-strategy"]
forbid_in_generic = ['acme', 'services/billing/']

[evidence]
dir = ".aisdlc/evidence"

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
