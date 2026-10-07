# Validation Plan — Cloud Fleet Ready Gate

Prove the Cursor Cloud AI SDLC fleet configuration is usable **before** the
foreman picks the first real GitHub product issue.

---

## Pass criteria (all must be true)

1. Docs and agent defs are on a branch and reviewable (this change).
2. Foreman + specialists are loadable (files exist; `AGENTS.md` indexes them).
3. Cloud toolchain checklist items that the current snapshot claims to provide
   actually work (or gaps are listed for environment.json).
4. Mechanical Constitution tools run: OpenSpec validate path + `validate-sdlc-plan.sh --list`,
   `preflight.sh --list`.
5. A **dry-run** foreman brief can be produced for a sample closed/docs issue
   without implementing product code.
6. No validation step depends on `local-ai/`.

---

## Step A — Artifact presence

```bash
test -f docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md
test -f docs/300-development/304-ai-sdlc-cloud/fleet-manifest.yaml
test -f docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md
test -f .claude/agents/cloud-foreman.md
test -f .claude/agents/openspec-planner.md
test -f .claude/agents/backend-implementer.md
test -f .claude/agents/frontend-design.md
test -f .claude/agents/testing-qa.md
grep -q cloud-foreman AGENTS.md
```

**Pass:** all exit 0.

---

## Step B — Toolchain smoke (environment)

```bash
java -version
mvn -version
node -v && npm -v
docker version
gh auth status
openspec --version || echo "GAP: install openspec in environment.json"
```

Record each GAP in the environment checklist / sibling env-mapping task.
Do **not** block fleet doc merge solely on missing Docker/Maven if the
environment build is still in progress — but **do** block picking a product
issue until GAPs for `java`, `mvn`, `node`, `gh`, `openspec` are closed.

---

## Step C — Constitution tooling smoke

```bash
bash workspace/sdlc/validate-sdlc-plan.sh --list
bash workspace/sdlc/preflight.sh --list
# Optional if openspec installed and a sample change exists:
# openspec list
```

**Pass:** scripts exit 0 and print gate mappings.

---

## Step D — Manifest coherence

```bash
# Every agent_file in fleet-manifest.yaml exists
python3 - <<'PY'
import pathlib, re, sys
text = pathlib.Path("docs/300-development/304-ai-sdlc-cloud/fleet-manifest.yaml").read_text()
files = re.findall(r"agent_file:\s*(\S+)", text)
missing = [f for f in files if not pathlib.Path(f).is_file()]
skills_root = pathlib.Path(".claude/skills")
skills = re.findall(r"^\s+-\s+([a-z0-9-]+)\s*$", text, re.M)
# filter known non-skill keys heuristically: only check under skills: blocks via simple existence
skill_missing = [s for s in set(skills) if s not in {
    "issue_pick","gate_orchestration","branch_lifecycle","pr_create_update","ci_watch","merge","next_issue"
} and not (skills_root / s).exists() and s not in {"ai-agent-workflow"}]
# ai-agent-workflow exists as dir
skill_missing = [s for s in set(skills) if not (skills_root / s).exists()]
# exclude owns: list noise — only check lines under skills: by reading yaml-ish
print("agent_files missing:", missing or "none")
print("skill dirs missing (heuristic):", [s for s in skill_missing if pathlib.Path('.claude/skills', s).exists() is False][:20])
sys.exit(1 if missing else 0)
PY
```

Prefer a real YAML parse if `pyyaml` is available; the heuristic above is enough for readiness.

---

## Step E — Dry-run handoff (no product coding)

Foreman produces a brief for a **docs/chore** sample (or a closed historical issue)
using the schema in `FLEET-ARCHITECTURE.md` §5.1, then a mocked specialist
result §5.2 with `status: BLOCKED` reason `validation dry-run only`.

**Pass:** brief contains `issue`, `use_case` or `UNKNOWN`, `phase`, `skills`, `done_when`;
result contains `status` and empty `commits`.

---

## Step F — Exclusion check

```bash
# Architecture must document exclusion; foreman may mention the harness only to forbid it
grep -Eiq 'forbidden|must \*\*not\*\*|do \*\*not\*\*.*local-ai' \
  docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md \
  .claude/agents/cloud-foreman.md
# No affirmative invoke (bash ./local-ai/sdlc/foreman.sh)
! grep -nE '(^|[^!])\s*(bash\s+|./)?local-ai/sdlc/foreman\.sh' .claude/agents/*.md \
  | grep -viE 'not |never |forbidden|do \*\*not\*\*|must \*\*not\*\*|no `local-ai'
```

**Pass:** exclusion is documented; no agent instructs executing the local harness.

---

## Go / No-go

| Decision | When |
|----------|------|
| **GO — pick first product issue** | A–F pass; `java`/`mvn`/`node`/`gh`/`openspec` present; Docker available if the issue needs Gate 3 `--full` |
| **NO-GO — setup only** | Any missing agent file; OpenSpec CLI absent; `gh` unauthenticated; Constitution scripts broken |

Until **GO**, the autonomous issue loop remains blocked (see project `notes.md`).
