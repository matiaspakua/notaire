#!/usr/bin/env bash
# foreman.sh — drives the local worker (Codex + Qwen via oMLX) through the
# Notaire SDLC for ONE GitHub issue, phase by phase, with a deterministic gate
# after every phase. See local-ai/sdlc/AI-SDLC.md.
#
#   foreman.sh <issue>            run/resume all phases up to "review"
#   foreman.sh <issue> <phase>    run from <phase> (re-runs it even if done)
#   foreman.sh <issue> merge      after foreman review: merge + Gate 5
#   foreman.sh <issue> fix        worker applies $RUNS/<issue>/gate4.md (foreman review), then ci + review
#   foreman.sh <issue> check      run the CHECK/EXPECTED lines of the pending review notes (no worker run)
#
# Project values (paths, commands, surfaces, guards) come from the adapter
# .aisdlc/project.yml (AISDLC_PROJECT overrides its path), read via bin/adapter.py.
# Env: WT (worker worktree, default adapter paths.worktree),
#      RUNS (harness state, default adapter paths.runs),
#      AGENT (worker: codex | opencode, default adapter backend.agent), OPENCODE_MODEL (default backend.opencode_model),
#      PROFILE (codex profile, default adapter backend.profile), PROFILE_<PHASE> (per-phase override, e.g. PROFILE_SPEC),
#      MAX_ATTEMPTS (3),
#      WORKER_TIMEOUT (seconds per worker run, 3600),
#      SKIP_PIPELINE=1 (skip run_pipeline.sh; only for docs/ci-only changes),
#      STOP_AFTER=<phase> (pause after a phase, for foreman inspection).
set -uo pipefail
[ "${BASH_VERSINFO[0]}" -ge 4 ] || { echo "foreman.sh needs bash >= 4 (macOS: brew install bash)" >&2; exit 2; }

HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
ADAPTER="${AISDLC_PROJECT:-$REPO/.aisdlc/project.yml}"
cfg()  { python3 "$HERE/bin/adapter.py" --file "$ADAPTER" "$@"; }
need() { cfg get "$@" || { echo "foreman.sh: fix the adapter $ADAPTER" >&2; exit 2; }; }
repo_path() { python3 -c 'import os, sys; print(os.path.normpath(os.path.join(sys.argv[1], sys.argv[2])))' "$REPO" "$1"; }
# a broken adapter stops the run here, not in a late phase
cfg validate || exit 2
WT="${WT:-$(repo_path "$(need paths.worktree)")}"
RUNS="${RUNS:-$(repo_path "$(need paths.runs)")}"
PROFILE="${PROFILE:-$(need backend.profile)}"
AGENT="${AGENT:-$(need backend.agent)}"; OPENCODE_MODEL="${OPENCODE_MODEL:-$(need backend.opencode_model)}"
SPEC_SCHEMA="$(need spec.schema)"; TASKS_TEMPLATE="$(need spec.tasks_template)"
TEST_FILES="$(need test_files)"; SOURCE_ROOTS="$(need source_roots)"; DB_MIGRATIONS="$(need db_migrations)"
COMPOSE_PROJECT="$(need compose_project)"; HEALTH_URL="$(need gates.health_url)"
FORBIDDEN="$(need guards.forbidden)"
MAX_ATTEMPTS="${MAX_ATTEMPTS:-3}"
WORKER_TIMEOUT="${WORKER_TIMEOUT:-3600}"
PHASES=(triage setup spec tests implement docs quality pipeline pr ci review)

ISSUE="${1:?usage: foreman.sh <issue> [phase|merge]}"
FROM="${2:-}"
# STATE = harness-owned (logs, evidence, prompts) — outside the worker's reach.
# IO    = the only files the worker exchanges with the harness.
STATE="$RUNS/$ISSUE"
IO="$WT/.localai/$ISSUE"
mkdir -p "$STATE" "$IO"
# one foreman per issue: two runs on the same worktree corrupt each other's branch and gates
if ! mkdir "$STATE/.lock" 2>/dev/null; then
    echo "foreman #$ISSUE already running (pid $(cat "$STATE/.lock/pid" 2>/dev/null)); remove $STATE/.lock if stale" >&2
    exit 3
fi
echo $$ > "$STATE/.lock/pid"
trap 'rm -rf "$STATE/.lock"' EXIT

log()  { printf '\033[1;36m[foreman #%s] %s\033[0m\n' "$ISSUE" "$*" | tee -a "$STATE/foreman.log"; }
fail() { printf '\033[31m[foreman #%s] STOP: %s\033[0m\n' "$ISSUE" "$*" | tee -a "$STATE/foreman.log"; exit 1; }
kv()   { python3 "$HERE/bin/envfile.py" "$1" "$2"; }   # drops ' # comment' and surrounding quotes
tv()   { kv "$STATE/triage.env" "$1"; }   # normalized triage (written by gate_triage)
git_wt() { git -C "$WT" "$@"; }
has()    { grep "$@" > /dev/null; }   # like grep -q, but reads all input: grep -q + pipefail = SIGPIPE false negatives
is_code() { [ "$(tv KIND)" = code ]; }
is_done() { grep -qx "$1" "$STATE/phases.done" 2>/dev/null; }
mark_done() { echo "$1" >> "$STATE/phases.done"; log "phase $1: DONE"; }

# ------------------------------------------------------------------ worker
render() {  # render <template> [GATE_CMD] [GATE_OUTPUT_FILE]
    python3 - "$HERE/WORKER.md" "$HERE/prompts/$1" "$STATE" "$IO" "$ISSUE" "${2:-}" "${3:-}" <<'PY'
import os, sys, re
brief, tpl, state, io, issue, gate_cmd, gate_file = sys.argv[1:8]
sys.path.insert(0, os.path.join(os.path.dirname(brief), "bin"))
from envfile import read_env
env = {}
for p in (os.path.join(state, "triage.env"), os.path.join(io, "tests.env"), os.path.join(state, "tests.env")):
    env.update(read_env(p))
env.update(ISSUE=issue, IO=io, GATE_CMD=gate_cmd, EDIT=os.path.join(state, "edit.py"))
env["GATE_OUTPUT"] = open(gate_file).read()[-6000:] if gate_file else ""
it = os.path.join(state, "issue.md")
env["ISSUE_TEXT"] = open(it).read()[:12000] if os.path.exists(it) else ""
body = re.sub(r"\{\{([A-Z_]+)\}\}", lambda m: env.get(m.group(1), m.group(0)), open(tpl).read())
out = open(brief).read().replace("{{EDIT}}", env["EDIT"]) + "\n---\n\n" + body
# foreman review notes for this phase (review-<phase>.md) — the Gate 4 feedback channel
phase = os.path.basename(tpl).split("-", 1)[1].rsplit(".", 1)[0]
review = os.path.join(state, "review-%s.md" % phase)
if os.path.exists(review):
    out += ("\n\n---\n\n# Foreman review of your previous attempt — fix ALL of these\n\n"
            "The previous attempt was discarded. Redo the phase from the current files and apply:\n\n"
            + open(review).read())
retry = os.path.join(state, "retry.md")
if os.path.exists(retry):
    # at the end only, a 12K-token phase prompt won: #1049's retries redid the whole
    # investigation and ran out of turns before fixing the two files the gate named
    note = open(retry).read()
    out = note + "\n\n---\n\n# Reference: the phase instructions\n\n" + out + "\n\n---\n\n" + note
print(out)
PY
}

run_worker() {  # run_worker <label> <template> [GATE_CMD] [GATE_OUTPUT_FILE]
    local label="$1" prompt="$STATE/prompt-$1.md" rc pre_ref pre_sha pre_branches hooks="$STATE/githooks"
    render "$2" "${3:-}" "${4:-}" > "$prompt"
    # a block is one run's message to the foreman (kept in $STATE/io-<label>/): a stale one fails a later, successful run
    rm -f "$IO/BLOCKED.md"
    pre_ref="$(git_wt symbolic-ref -q --short HEAD || git_wt rev-parse HEAD)"
    pre_sha="${PHASE_BASE:-$(git_wt rev-parse HEAD)}"
    pre_branches="$(git_wt for-each-ref --format='%(refname:short)' refs/heads)"
    # Git-level guardrails, active only inside the worker's shell (the harness's own git
    # calls do not see them). Codex strips env vars named *KEY* from the shell tool, so they
    # go in through shell_environment_policy.set, which is applied after that filter.
    rm -rf "$hooks" && cp -R "$HERE/githooks" "$hooks"
    cp "$HERE/bin/edit.py" "$STATE/edit.py"   # the worker's edit tool (Codex apply_patch fails with the local model)
    { echo "BASE=$pre_sha"; echo "BRANCH=$(git_wt symbolic-ref -q --short HEAD)"
      echo "ALLOW_COMMIT=${ALLOW_COMMIT:-1}"; printf "FORBIDDEN='%s'\n" "$FORBIDDEN"; printf "SCOPE='%s'\n" "${SCOPE:-.}"; } > "$hooks/state.env"
    local profile_var; profile_var="PROFILE_$(tr '[:lower:]' '[:upper:]' <<< "${label%%-*}")"
    local profile="${!profile_var:-$PROFILE}"
    log "worker → $label ($AGENT, profile $profile, timeout ${WORKER_TIMEOUT}s)"
    # watchdog: kills the worker's whole process group on timeout (a perl alarm did not stop codex)
    python3 "$HERE/bin/watchdog.py" "$WORKER_TIMEOUT" \
        python3 "$HERE/bin/worker.py" "$AGENT" "$WT" "$profile" "$OPENCODE_MODEL" "$hooks" \
        "$STATE/last-$label.md" "$prompt" < /dev/null >> "$STATE/worker-$label.log" 2>&1
    rc=$?
    log "worker ← $label exit $rc"
    mkdir -p "$STATE/io-$label" && cp -R "$IO/." "$STATE/io-$label/" 2>/dev/null
    ref_guard "$label" "$pre_ref" "$pre_sha" "$pre_branches"
    [ -n "${SCOPE:-}" ] && scope_guard "$label" "$SCOPE"
    [ -f "$IO/BLOCKED.md" ] && fail "worker blocked: $(head -8 "$IO/BLOCKED.md")"
    return 0
}

# scope_guard <label> <allowed-regex>: unstage everything, then revert uncommitted
# changes outside the phase's scope. Committed drift is caught by later gates/review.
ref_guard() {  # the worker never switches branches, creates branches or (with NO_COMMIT) commits
    local label="$1" pre_ref="$2" pre_sha="$3" pre_branches="$4" now b msg=""
    now="$(git_wt symbolic-ref -q --short HEAD || git_wt rev-parse HEAD)"
    if [ "$now" != "$pre_ref" ]; then
        msg+="switched HEAD from $pre_ref to $now; "
        git_wt checkout -q -f "$pre_ref"
    fi
    if [ "${NO_COMMIT:-0}" = 1 ] && [ "$(git_wt rev-parse HEAD)" != "$pre_sha" ]; then
        # harmless and the work is kept: undo silently instead of burning a retry
        log "ref guard ($label): undoing worker commit(s) — the harness commits this phase"
        git_wt reset -q "$pre_sha"   # mixed: the work stays in the tree, only the commit goes
    fi
    if ! git_wt merge-base --is-ancestor "$pre_sha" HEAD; then
        msg+="rewrote history below the phase start; "
        git_wt reset -q "$pre_sha"
    fi
    local junk; junk="$(git_wt diff --name-only "$pre_sha" HEAD | grep -E "$FORBIDDEN" || true)"
    if [ -n "$junk" ]; then
        msg+="committed forbidden paths ($(head -3 <<<"$junk" | tr '\n' ' ')…); "
        git_wt reset -q "$pre_sha"   # the files stay on disk, ignored again — harmless
    fi
    # worktrees share refs: a branch checked out in another worktree (e.g. the foreman's own PR branch) is not the worker's
    local wtp
    while read -r b wtp; do
        [ -n "$wtp" ] && [ "$wtp" != "$WT" ] && continue
        grep -qxF "$b" <<<"$pre_branches" || { msg+="created branch $b; "; git_wt branch -q -D "$b"; }
    done < <(git_wt for-each-ref --format='%(refname:short) %(worktreepath)' refs/heads)
    [ -z "$msg" ] && return 0
    log "ref guard ($label): $msg— reverted"
    printf 'GIT VIOLATION in %s — the foreman reverted it: %s\nThe foreman owns branches. Never git checkout/switch/branch/commit/push unless the phase says so.\n' \
        "$label" "$msg" >> "$STATE/scope.out"
}
scope_guard() {
    local label="$1" allowed="$2" bad
    git_wt reset -q
    bad="$(git_wt status --porcelain --untracked-files=all | cut -c4- | grep -vE "$allowed" || true)"
    [ -z "$bad" ] && return 0
    log "scope guard ($label): reverting $(tr '\n' ' ' <<<"$bad")"
    while read -r f; do
        [ -z "$f" ] && continue
        if git_wt cat-file -e "HEAD:$f" 2>/dev/null; then git_wt checkout -q HEAD -- "$f"; else rm -rf "${WT:?}/$f"; fi
    done <<<"$bad"
    printf 'SCOPE VIOLATION in %s — the foreman reverted these paths; this phase may not touch them:\n%s\n' \
        "$label" "$bad" >> "$STATE/scope.out"
}

# the phase IO files (triage.env/md) live under the git-ignored .localai/: git status alone missed the
# worker's triage edits and review_ignored rejected an applied note (#1062)
wt_fingerprint() { { git_wt rev-parse HEAD; git_wt status --porcelain; git_wt diff; cat "$IO"/* 2>/dev/null; } | md5 -q; }

# review_ignored <phase> <fingerprint-before>: a pending foreman note the worker did not act on.
# The gate would pass on the previous artifacts and the note would be lost (#1049 spec, #1102).
review_ignored() {
    local note="$STATE/review-$1.md"
    [ -f "$note" ] && [ "${RECHECK:-0}" != 1 ] && [ "$(wt_fingerprint)" = "$2" ] || return 1
    gate_msg "You changed no file, but the foreman review below is pending. Apply every point of it now:\n\n$(cat "$note")\n" || true
}

# review_checks_pass <phase>: the CHECK/EXPECTED lines of a pending note gate the phase too.
# A gate cannot see what the note asked for; the #1049 spec worker edited one unrelated line and passed.
review_checks_pass() {
    local note="$STATE/review-$1.md"
    [ -f "$note" ] && grep -q '^CHECK:' "$note" || return 0
    python3 "$HERE/bin/review_check.py" "$WT" "$note" > "$STATE/gate-review.out" 2>&1 && return 0
    gate_msg "The foreman review is not applied yet. These checks from it fail:\n\n$(cat "$STATE/gate-review.out")\n\nThe review:\n\n$(cat "$note")\n"
}

# a note applied in a passing run is kept as evidence, out of the review-*.md glob `render` and `check` read
retire_review() {
    [ -f "$STATE/review-$1.md" ] && [ "${RECHECK:-0}" != 1 ] || return 0
    mv "$STATE/review-$1.md" "$STATE/applied-review-$1-$(date +%Y%m%d%H%M%S).md"
    gate_log review-applied "REVIEW :: review-$1.md applied in a passing run"
}

# with_retries <phase> <template> <gate-fn>: run phase, gate it, re-run the same
# phase with the gate output attached until it passes or attempts run out.
with_retries() {
    local phase="$1" tpl="$2" gate="$3" attempt
    rm -f "$STATE/retry.md"
    # retries fix the phase's own commits (amend): the guards measure from the phase start, not the attempt start
    PHASE_BASE="$(git_wt rev-parse HEAD)"
    # RECHECK=1: the work is already on the branch (foreman fixed a gate bug) — just re-run the gate
    local before; before="$(wt_fingerprint)"
    [ "${RECHECK:-0}" = 1 ] || run_worker "$phase" "$tpl"
    for attempt in $(seq 1 "$MAX_ATTEMPTS"); do
        gate_scope
        if review_ignored "$phase" "$before"; then
            :
        elif "$gate" && review_checks_pass "$phase"; then
            rm -f "$STATE/retry.md" "$STATE/scope.out"; retire_review "$phase"; return 0
        fi
        before="$(wt_fingerprint)"
        scope_to_gate_out
        log "gate $gate failed (attempt $attempt/$MAX_ATTEMPTS): $(head -3 "$STATE/gate.out" | tr '\n' ' ')"
        [ "$attempt" -eq "$MAX_ATTEMPTS" ] && break
        # RECHECK is a gate-only run: a failure goes back to the foreman, never to a worker retry
        [ "${RECHECK:-0}" = 1 ] && break
        { echo "# RETRY $attempt — the foreman gate REJECTED your previous attempt at this phase"
          echo
          echo "The gate output below is complete. Do not search for the gate, do not read"
          echo "scripts, do not move or delete files. Your earlier work is still in place:"
          echo "open the files the errors name, fix exactly those errors, keep the rest."
          echo; echo '```'; tail -c 6000 "$STATE/gate.out"; echo '```'; } > "$STATE/retry.md"
        run_worker "$phase-retry$attempt" "$tpl"
    done
    rm -f "$STATE/retry.md"
    fail "phase $phase: gate $gate still failing — foreman must intervene (see $STATE/gate.out)"
}

# ------------------------------------------------------------------ gates
gate_log() {
    echo "$(date '+%F %T') | $1 | $2" >> "$STATE/gates.log"
    python3 "$HERE/bin/metrics.py" "$STATE/metrics.jsonl" "$ISSUE" "$1" "$2"
}
gate_msg() { printf '%b' "$1" > "$STATE/gate.out"; return 1; }

run_gate() {  # run_gate <name> <cmd...>; output kept in $STATE/gate-<name>.out
    local name="$1"; shift
    ( cd "$WT" && "$@" ) > "$STATE/gate-$name.out" 2>&1
    local rc=$?
    gate_log "$name" "$( [ $rc -eq 0 ] && echo PASS || echo "FAIL rc=$rc" ) :: $*"
    return $rc
}

# a guard violation is already reverted: the gate judges what is left, the attempt is not lost for it.
# Gate 4 sees it in gates.log; the worker sees it only if the gate fails (the revert may be why)
gate_scope() {
    [ -s "$STATE/scope.out" ] || return 0
    gate_log scope-reverted "REVIEW :: $(grep -m1 VIOLATION "$STATE/scope.out")"
}
scope_to_gate_out() {
    [ -s "$STATE/scope.out" ] || return 0
    { cat "$STATE/scope.out"; echo; cat "$STATE/gate.out" 2>/dev/null; } > "$STATE/gate.out.new"
    mv "$STATE/gate.out.new" "$STATE/gate.out"; rm -f "$STATE/scope.out"
}

# md_fix <files>: markdownlint's mechanical fixes (blank lines, list markers) are the harness's job, not the worker's
md_fix() {
    local fix; fix="$(need gates.docs_lint_fix files="$1")"
    ( cd "$WT" && python3 "$HERE/bin/md_repair.py" $1 )   # MD040/MD055, which --fix cannot solve
    ( cd "$WT" && bash -c "$fix" ) > "$STATE/gate-lint-fix.out" 2>&1   # non-zero = errors left: md_lint names them
    git_wt diff --quiet -- $1 || gate_log lint-fix "FIXED :: $(git_wt diff --name-only -- $1 | tr '\n' ' ')"
}
md_lint() {  # md_lint <gate-name> <files>
    run_gate "$1" bash -c "$(need gates.docs_lint files="$2")" \
        || { { echo "markdown lint failed (fix only the reported lines; a closing fence stays a bare \`\`\`):"; tail -60 "$STATE/gate-$1.out"; } > "$STATE/gate.out"; return 1; }
}

require_clean_branch() {
    local branch dirty bad; branch="$(tv BRANCH)"
    [ "$(git_wt branch --show-current)" = "$branch" ] \
        || gate_msg "You are not on branch $branch. Run: git checkout $branch\n" || return 1
    dirty="$(git_wt status --porcelain | grep -v '^?? \.localai/' || true)"
    # the #1049 worker ran `git commit --amend --no-edit` three times without staging: spell out the add
    [ -z "$dirty" ] || gate_msg "Uncommitted changes — commit them (atomic, Conventional Commits) or remove them:\n$dirty\nA commit takes only staged files. To fold them into your last commit run exactly:\n  git add -A -- $(awk '{print $NF}' <<<"$dirty" | tr '\n' ' ')&& git commit --amend --no-edit\n" || return 1
    bad="$(git_wt log --format=%s origin/main..HEAD \
        | grep -vE '^(feat|fix|docs|refactor|test|chore|ci|style|perf|design)(\([a-z0-9._/-]+\))?!?: .+' || true)"
    [ -z "$bad" ] || gate_msg "These commit subjects are not Conventional Commits (type(scope): description):\n$bad\nFix with: git commit --amend (last commit) — never rewrite commits already pushed.\n"
}

gate_triage() {
    local f="$IO/triage.env" md="$IO/triage.md" e="" v
    iv() { kv "$f" "$1"; }
    # the worker overwrote the seeded TYPE in 2 of 3 #1064 attempts: what the harness derived, it restores
    local fixed; fixed="$(python3 "$HERE/bin/triage_check.py" restore "$f" "$STATE/triage.seed.env")"
    [ -z "$fixed" ] || gate_log triage-repaired "REVIEW :: restored seeded $fixed"
    [[ "$(iv TYPE)" =~ ^(feat|fix|refactor|test|docs|chore|ci|design)$ ]] \
        || e+="- triage.env: TYPE=$(iv TYPE) — write one of feat fix refactor test docs chore ci design (issue title prefix)\n"
    local tprefix; tprefix="$(sed -n '1s/^# #[0-9]* \([a-z]*\)[(:].*/\1/p' "$STATE/issue.md")"
    [ -z "$tprefix" ] || [ "$(iv TYPE)" = "$tprefix" ] \
        || e+="- triage.env: TYPE=$(iv TYPE) but the issue title starts with '$tprefix' — write TYPE=$tprefix\n"
    [[ "$(iv KIND)" =~ ^(code|docs|ci)$ ]] || e+="- triage.env: KIND=$(iv KIND) — write code, docs or ci\n"
    for v in UI_CHANGE API_CHANGE DB_CHANGE; do
        [[ "$(iv $v)" =~ ^(yes|no)$ ]] || e+="- triage.env: $v=$(iv $v) — write yes or no\n"
    done
    # SLUG separators are cosmetic (the model writes the change-name style, a-b-c): normalize, do not retry
    sed -i '' '/^SLUG=/s/[-[:space:]]/_/g' "$IO/triage.env"
    [[ "$(iv SLUG)" =~ ^[a-z0-9]+(_[a-z0-9]+){1,5}$ ]] \
        || e+="- triage.env: SLUG=$(iv SLUG) — write 2-6 lowercase words joined by _, e.g. remove_dead_default_credentials\n"
    for v in "## Evidence" "## Acceptance Criteria" "## Files to Edit" "## Risks"; do
        grep -q "^$v" "$md" || e+="- triage.md: heading '$v' is missing — restore it\n"
    done
    grep -q 'example' "$md" && e+="- triage.md: still contains skeleton 'example' lines — replace them with real content\n"
    # normalize cosmetics (backticks, bold, dash variants) — judge substance, not typography
    local crit; crit="$(sed -n '/^## Acceptance Criteria/,/^## /p' "$md" | grep -E '^[0-9]+\.' \
        | perl -CSD -pe 's/[`*]//g; s/\s+[-\x{2013}\x{2014}:]+\s+/ \x{2014} /g; s/proven by\s*[:\x{2014}]*\s*/proven by: /i; s/proven by: existing test /proven by: /i' || true)"
    [ -n "$crit" ] || e+="- triage.md: no numbered acceptance criteria\n"
    grep -vqE '^[0-9]+\. (TODO|DONE) — .+ — proven by: ((new test )?[A-Za-z0-9_.]+#[A-Za-z0-9_]+|command .+)' <<<"$crit" \
        && e+="- triage.md: these criteria lines do not match 'N. TODO|DONE — <criterion> — proven by: [new test ]<TestClass>#<method>' or '... — proven by: command <cmd>':\n$(grep -vE '^[0-9]+\. (TODO|DONE) — .+ — proven by: ((new test )?[A-Za-z0-9_.]+#[A-Za-z0-9_]+|command .+)' <<<"$crit")\n  Rewrite each whole line, e.g. '1. TODO — Jenkinsfile removed — proven by: command test ! -e Jenkinsfile'\n"
    grep -qE 'proven by: (git |bash |mvn |npm |npx |gh )' <<<"$crit" \
        && e+="- triage.md: a shell command as proof must start with the word 'command', e.g. 'proven by: command bash scripts/preflight.sh'. A removal (git rm) is not a proof — prove it with a test, e.g. 'new test BackendResourcesHygieneTest#shouldNotPackageLegacyConfigProperties' asserting getClass().getResource(\"/config.properties\") is null\n"
    grep -qiE 'proven by: .*(PR #|issue #|#[0-9]+)' <<<"$crit" \
        && e+="- triage.md: a PR/issue number is not a proof. Also: criteria come ONLY from the issue's '## Acceptance Criteria' checklist — 'Technical Notes'/'Related' items are out of scope, drop them\n"
    local search; search="$(python3 "$HERE/bin/triage_check.py" bad-proofs <<<"$crit")"
    [ -z "$search" ] || e+="- triage.md: a search/print command passes whether or not the criterion holds, so it is not a proof. Prove it with a new test instead (a test can read any repository file):\n$search\n"
    python3 "$HERE/bin/triage_check.py" kind-conflict "$(iv KIND)" <<<"$crit" \
        || e+="- triage.env: KIND=$(iv KIND) has no tests phase, so the 'new test' proofs would never be written — write KIND=code\n"
    grep -qE '^[0-9]+\. TODO' <<<"$crit" \
        || e+="- triage.md: no TODO criterion. If the issue is fully resolved, write $IO/BLOCKED.md with the evidence\n"
    local files p
    files="$(sed -n '/^## Files to Edit/,/^## /p' "$md" | grep -E '^- ' | sed 's/^- *//;s/`//g;s/[[:space:]].*$//')"
    [ -n "$files" ] || e+="- triage.md: '## Files to Edit' lists no '- path' lines\n"
    for p in $files; do
        [ -e "$WT/$p" ] || [[ "$p" == "$DB_MIGRATIONS"V* ]] \
            || e+="- triage.md: Files to Edit path '$p' does not exist in the repo (list existing paths; new files only for DB migrations under $DB_MIGRATIONS)\n"
    done
    # a code rule about a docs/data file is proven by a test that reads it: TEST_SURFACE says where that test runs
    local fallback=""; [ "$(iv KIND)" != code ] || fallback="$(iv TEST_SURFACE)"
    local surface; surface="$(cfg surfaces --fallback "$fallback" <<<"$files")" || fail "adapter: cannot derive surfaces"
    [ "$(iv DB_CHANGE)" != yes ] || has -F "$DB_MIGRATIONS"V <<<"$files" \
        || e+="- DB_CHANGE=yes but Files to Edit has no new migration (${DB_MIGRATIONS}V<n>__x.sql). Deleting/editing a .properties file is NOT a DB change — set DB_CHANGE=no unless a migration is really needed\n"
    [ "$(iv KIND)" != code ] || [ "$surface" != none ] \
        || e+="- KIND=code but no Files to Edit path is under a surface root and TEST_SURFACE=$(iv TEST_SURFACE) is not a surface — write TEST_SURFACE=backend (or frontend): where the test that proves the change runs\n"
    [ -z "$e" ] || gate_msg "Fix these problems in $IO/triage.env and $IO/triage.md:\n$e" || return 1

    local type slug
    type="$(iv TYPE)"; slug="$(iv SLUG)"
    local uct; uct="$(sed -n '/^## Use Case/,/^## /p' "$STATE/issue.md" | grep -m1 -E "$(iv USE_CASE)" \
        | perl -CSD -pe 's/^.*?(CU|RF|RNF)-?[0-9]+\s*[-\x{2013}\x{2014}:]*\s*//; s/[`*]//g; s/\s+$//')"
    { echo "ISSUE=$ISSUE"; echo "USE_CASE=$(iv USE_CASE)"; echo "USE_CASE_TITLE=$uct"; echo "TYPE=$type"; echo "KIND=$(iv KIND)"
      echo "SURFACE=$surface"; echo "UI_CHANGE=$(iv UI_CHANGE)"; echo "API_CHANGE=$(iv API_CHANGE)"
      echo "DB_CHANGE=$(iv DB_CHANGE)"; echo "BRANCH=$type/${ISSUE}_$slug"
      echo "CHANGE=$(tr _ - <<<"$slug")-$ISSUE"; } > "$STATE/triage.env"
    cp "$md" "$STATE/triage.md"
    log "triage: $(tr '\n' ' ' < "$STATE/triage.env")"
}

# repair_spec <change-dir>: fixes with one right answer never cost the worker an attempt (#1049)
repair_spec() {
    local d="$1" unticked
    # the worker sets skip_specs yet leaves a delta-less specs/ behind: openspec validate rejects the leftover
    if [ "$(tv KIND)" != code ] && grep -q 'skip_specs: *true' "$d/.openspec.yaml" 2>/dev/null && [ -d "$d/specs" ]; then
        rm -rf "$d/specs"; gate_log spec-repaired "REVIEW :: skip_specs set, removed leftover specs/"
    fi
    # nothing past the branch (groups 1-2) is done before Gate 2; the worker ticked 4.1/4.2 anyway
    [ -f "$d/tasks.md" ] || return 0
    unticked="$(python3 "$HERE/bin/ledger.py" untick-after "$d/tasks.md" 2)"
    [ -z "$unticked" ] || gate_log spec-repaired "REVIEW :: unticked $unticked"
}

gate_spec() {
    local c; c="$(tv CHANGE)"
    local validate plan; validate="$(need spec.validate change="$c")"; plan="$(need spec.plan_check change="$c")"
    repair_spec "$WT/openspec/changes/$c"
    run_gate spec-validate bash -c "$validate" \
        || { { echo "$validate failed:"; tail -60 "$STATE/gate-spec-validate.out"; } > "$STATE/gate.out"; return 1; }
    run_gate spec-sdlc bash -c "$plan" \
        || { { echo "$plan failed:"; tail -80 "$STATE/gate-spec-sdlc.out"; } > "$STATE/gate.out"; return 1; }
    local d="$WT/openspec/changes/$c" e="" t
    # without the schema line validate-sdlc-plan.sh skips the change and "passes": Constitution checks silently off
    # the ledger ticks template IDs (10.1, 10.2, ...): the 12 mandatory group headings must be the template's, verbatim
    local tpl_h; tpl_h="$(grep -E '^## [0-9]+\. ' "$WT/$TASKS_TEMPLATE")"
    [ "$(grep -E '^## [0-9]+\. ' "$d/tasks.md" 2>/dev/null)" = "$tpl_h" ] \
        || e+="- tasks.md: group headings must be exactly the template's ($TASKS_TEMPLATE), in order:\n$tpl_h\n  Copy the template over tasks.md and write your change-specific tasks as 4.1, 4.2, ... in group 4.\n"
    grep -qE '^- \[.\] 10\.1 ' "$d/tasks.md" 2>/dev/null \
        || e+="- tasks.md: keep the template's numbered items (e.g. '- [ ] 10.1 ...'): the foreman ticks them by ID\n"
    grep -qx "schema: $SPEC_SCHEMA" "$d/.openspec.yaml" 2>/dev/null \
        || e+="- .openspec.yaml must keep its first line 'schema: $SPEC_SCHEMA' (restore it: git checkout HEAD -- openspec/changes/$c/.openspec.yaml, then only remove skip_specs if needed)\n"
    # the harness writes these rows later (record_ledger, record_pr): a spec that dropped one fails the run at the docs phase
    python3 "$HERE/bin/ledger.py" rows "$d/traceability.md" Commits "Pull Request" 2>/dev/null \
        || e+="- traceability.md: keep the template's 'Commits' and 'Pull Request' rows, exactly once each (the harness fills them)\n"
    grep -q "#$ISSUE" "$d/proposal.md" || e+="- proposal.md must reference issue #$ISSUE in its header table\n"
    [ -z "$(tv USE_CASE_TITLE)" ] || has -iF "$(tv USE_CASE_TITLE)" "$d/proposal.md" \
        || e+="- proposal.md: the Use Case row must read '$(tv USE_CASE) — $(tv USE_CASE_TITLE)' (the title from the issue, do not invent one)\n"
    if [ "$(tv KIND)" = code ]; then
        grep -q 'skip_specs: *true' "$d/.openspec.yaml" 2>/dev/null \
            && e+="- .openspec.yaml: skip_specs is only for docs/ci changes; this change touches code — remove it and write specs/<capability>/spec.md\n"
        grep -rqs '^#### Scenario:' "$d/specs" \
            || e+="- specs/: no '#### Scenario:' found — write one '### Requirement:' per TODO criterion with WHEN/THEN scenarios\n"
        for t in $(grep -oE 'new test [A-Za-z0-9_]+' "$STATE/triage.md" | awk '{print $3}' | sort -u); do
            grep -q "$t" "$d/traceability.md" || e+="- traceability.md: planned test $t (from triage) is missing — add a row for it\n"
        done
    fi
    [ -z "$e" ] || gate_msg "Fix these in openspec/changes/$c/:\n$e" || return 1
    # lint here, not first in the docs gate: the errors go back to the phase that wrote them (the harness commits the fixes)
    local md; md="$(cd "$WT" && find "openspec/changes/$c" -name '*.md' | tr '\n' ' ')"
    md_fix "$md"
    md_lint spec-lint "$md" || return 1
    [ "$(tv USE_CASE)" = NONE ] || has -E "$(tv USE_CASE | sed 's/^\([A-Z]*\)-\{0,1\}/\1-?/')" "$WT/openspec/changes/$c/proposal.md" \
        || gate_msg "proposal.md must reference Use Case $(tv USE_CASE)\n" || return 1
}

test_cmd() { if [ -f "$STATE/tests.env" ]; then kv "$STATE/tests.env" TEST_CMD; else kv "$IO/tests.env" TEST_CMD; fi; }

gate_red() {
    local cmd; cmd="$(test_cmd)"
    [ -n "$cmd" ] || gate_msg "Missing TEST_CMD=... line in $IO/tests.env\n" || return 1
    local form; form="$(cfg check-test-cmd "$(tv SURFACE)" "$cmd")" || gate_msg "$form\n" || return 1
    git_wt diff --name-only origin/main...HEAD | has -E "$TEST_FILES" \
        || gate_msg "No committed test file on this branch. Write the tests, then git add + git commit them.\n" || return 1
    require_clean_branch || return 1
    local e="" t m
    for t in $(grep -oE 'new test [A-Za-z0-9_]+#[A-Za-z0-9_]+' "$STATE/triage.md" | awk '{print $3}' | sort -u); do
        m="${t#*#}"
        git_wt grep -q "void $m(" HEAD -- '*src/test/*' \
            || e+="- planned test ${t%%#*}#$m (triage) not found in committed tests — write it with exactly that method name\n"
    done
    git_wt diff --name-only origin/main...HEAD | has '^\.localai/' \
        && e+="- .localai/ files are committed — they are foreman scratch, never commit them: git rm -r --cached .localai && git commit --amend\n"
    git_wt log --format=%B origin/main..HEAD | has "Closes #" \
        && e+="- a commit says 'Closes #...' — only the final implementation commit may. Use 'Refs #$ISSUE' (git commit --amend is fine: the branch is not pushed)\n"
    local static; static="$(python3 "$HERE/bin/static_checks.py" "$WT" origin/main)" || e+="$static\n"
    [ -z "$e" ] || gate_msg "Fix before the red run:\n$e" || return 1
    if run_gate red bash -c "$cmd"; then
        { echo "TEST_CMD passed, but in this phase it MUST fail (TDD red): your tests do not"
          echo "exercise the missing behaviour. Make each test assert the NEW behaviour from"
          echo "the spec scenarios. Output:"; tail -40 "$STATE/gate-red.out"; } > "$STATE/gate.out"
        return 1
    fi
    grep -qiE 'COMPILATION ERROR|cannot find symbol|Tests run:.*(Failures|Errors): [1-9]|FAIL|AssertionError|expected' "$STATE/gate-red.out" \
        || { { echo "TEST_CMD failed, but not because a test failed — check that the command itself runs:"
               tail -40 "$STATE/gate-red.out"; } > "$STATE/gate.out"; return 1; }
    # every planned test must be red on its own: an overall red can hide a vacuous test that already passes
    for t in $(grep -oE 'new test [A-Za-z0-9_]+#[A-Za-z0-9_]+' "$STATE/triage.md" | awk '{print $3}' | sort -u); do
        grep -qE "${t%%#*}[.#]${t#*#}.*(FAIL|ERROR)|${t#*#}.*<<< (FAILURE|ERROR)|FAIL.*${t#*#}" "$STATE/gate-red.out" \
            || e+="- $t PASSES already — it does not test the missing behaviour (vacuous test). It must read the REAL resource/code and fail today.\n"
    done
    [ -z "$e" ] || gate_msg "TDD red is not real for every planned test:\n$e" || return 1
    git_wt rev-parse HEAD > "$STATE/red.sha"
    # the approved TEST_CMD is frozen harness-side: the worker cannot change it after red, and a wiped IO cannot lose it
    cp "$IO/tests.env" "$STATE/tests.env"
}

suite_cmd() { cfg suite "$(tv SURFACE)"; }

# a small model "edits" by rewriting a file from the part it read: flag modified files that lost a big share of lines
collateral_deletions() {
    local base="$1" f added removed total
    git_wt diff --numstat --diff-filter=M "$base"..HEAD | while read -r added removed f; do
        [ "$added" = - ] && continue
        [[ "$f" =~ $TEST_FILES ]] && continue
        total="$(git_wt show "$base:$f" | wc -l | tr -d " ")"
        [ "$removed" -gt 10 ] && [ $((removed * 4)) -gt "$total" ] && [ "$removed" -gt $((added * 2)) ] \
            && echo "- $f: $removed of $total lines removed (+$added). Restore it (git checkout $base -- $f) and delete ONLY the lines the spec names, with apply_patch or sed -i '' 'N,Md'. Never rewrite a whole file."
    done
}

# repair_tasks <base>: after Gate 2, tasks.md may only change by [ ] -> [x] (the worker kept rewriting it with
# invented results, and could not undo it in three retries) — the harness restores the plan, keeps the ticks
repair_tasks() {
    local f="openspec/changes/$(tv CHANGE)/tasks.md" old="$STATE/tasks-base.md" fixed="$STATE/tasks-fixed.md"
    git_wt show "$1:$f" > "$old" 2>/dev/null || return 0
    local diff; diff="$(python3 "$HERE/bin/ledger.py" ticks-only "$old" "$WT/$f")" && return 0
    python3 "$HERE/bin/ledger.py" restore-ticks "$old" "$WT/$f" > "$fixed" && cp "$fixed" "$WT/$f" \
        && git_wt commit -q -m "docs(openspec): restore $(tv CHANGE) tasks to plan plus ticks" -m "Refs #$ISSUE" -- "$f" \
        || gate_msg "could not repair $f — foreman must intervene\n" || return 1
    gate_log tasks-repaired "REPAIRED :: discarded (review at Gate 4): $(tr '\n' ' ' <<<"$diff")"
}

# green_base: Gate 2's commit, or for a non-code change the branch's fork point — origin/main itself has moved
# on since the branch was cut, and diffing against it counted main's newer lines as the worker's deletions (#1049)
green_base() { cat "$STATE/red.sha" 2>/dev/null || git_wt merge-base HEAD origin/main; }

gate_green() {
    require_clean_branch || return 1
    local base; base="$(green_base)"
    repair_tasks "$base" || return 1
    local lost; lost="$(collateral_deletions "$base")"
    git_wt diff --check "$base"..HEAD > "$STATE/diff-check.out" \
        || gate_msg "Leftover conflict markers or whitespace errors (git diff --check):\n$(head -20 "$STATE/diff-check.out")\n" || return 1
    [ -z "$lost" ] || gate_msg "Unrequested deletions — you destroyed content outside the change:\n$lost\n" || return 1
    git_wt diff --name-only origin/main...HEAD | grep -vE '^(openspec/|\.localai/)' | has . \
        || gate_msg "No implementation committed on this branch yet.\n" || return 1
    local stray; stray="$(git_wt diff --name-only --diff-filter=A origin/main...HEAD -- openspec/changes \
        | grep -v "^openspec/changes/$(tv CHANGE)/" || true)"
    [ -z "$stray" ] || gate_msg "Files added under openspec/changes/ outside this change ($(tv CHANGE)) — remove them (git rm -r) and amend:\n$stray\n" || return 1
    run_gate green bash -c "$(test_cmd)" \
        || { { echo "TEST_CMD ($(test_cmd)) fails:"; tail -120 "$STATE/gate-green.out"; } > "$STATE/gate.out"; return 1; }
    run_gate suite bash -c "$(suite_cmd)" \
        || { { echo "Full suite ($(suite_cmd)) fails — you broke something:"; tail -150 "$STATE/gate-suite.out"; } > "$STATE/gate.out"; return 1; }
    git_wt log --format=%B origin/main..HEAD | has "Closes #$ISSUE" \
        || gate_msg "No commit body contains 'Closes #$ISSUE'. Amend the last implementation commit (allowed: branch not pushed yet): git commit --amend\n" \
        || return 1
    # tests edited after the red run are legal only as genuine fixes — surface them for Gate 4 review
    local changed; [ -f "$STATE/red.sha" ] \
        && changed="$(git_wt diff --name-only "$(cat "$STATE/red.sha")"..HEAD | grep -E "$TEST_FILES")"
    [ -z "${changed:-}" ] || gate_log tests-edited-after-red "REVIEW :: $(tr '\n' ' ' <<<"$changed")"
}

gate_docs() {
    local c; c="$(tv CHANGE)"
    repair_tasks "$(cat "$STATE/red.sha" 2>/dev/null || echo origin/main)" || return 1
    if [[ "$(tv TYPE)" =~ ^(feat|fix)$ ]] && ! git_wt diff --name-only origin/main...HEAD | has -x CHANGELOG.md; then
        gate_msg "TYPE=$(tv TYPE) is user-visible: add a line under '## [Unreleased]' in CHANGELOG.md ending with (#$ISSUE), then commit\n"
        return 1
    fi
    # markdown lint runs in the pipeline too, but only after ~10 minutes: give the worker fast feedback here
    local md; md="$(git_wt diff --name-only --diff-filter=d origin/main...HEAD -- '*.md' | grep -vE '^docs/000-archive/' | tr '\n' ' ')"
    if [ -n "$md" ]; then
        # only committed files: a worker's uncommitted edit must not ride in the style commit
        git_wt diff --quiet HEAD -- $md && md_fix "$md"
        git_wt diff --quiet -- $md || git_wt commit -q -m "style(docs): fix markdown lint" -m "Refs #$ISSUE" -- $md \
            || fail "could not commit markdown lint fixes"
        md_lint docs-lint "$md" || return 1
    fi
    local plan; plan="$(need spec.plan_check change="$c")"
    run_gate docs-sdlc bash -c "$plan" \
        || { { echo "$plan failed:"; tail -80 "$STATE/gate-docs-sdlc.out"; } > "$STATE/gate.out"; return 1; }
    require_clean_branch
}

# harness_gate_loop <name> <cmd>: the harness runs the gate; on failure the worker fixes it
harness_gate_loop() {
    local name="$1" cmd="$2" attempt
    PHASE_BASE="$(git_wt rev-parse HEAD)"
    for attempt in $(seq 1 "$MAX_ATTEMPTS"); do
        if run_gate "$name" bash -c "$cmd" && require_clean_branch; then return 0; fi
        [ -s "$STATE/gate-$name.out" ] && tail -200 "$STATE/gate-$name.out" > "$STATE/gate.out"
        log "gate $name failed (attempt $attempt/$MAX_ATTEMPTS)"
        [ "$attempt" -eq "$MAX_ATTEMPTS" ] && break
        run_worker "$name-fix$attempt" 07-fix-gate.md "$cmd" "$STATE/gate.out"
    done
    fail "gate $name still failing — foreman must intervene (see $STATE/gate.out)"
}

gate_pr() {
    local pr title
    pr="$(cd "$WT" && gh pr view "$(tv BRANCH)" --json number,title,body 2>/dev/null)" \
        || gate_msg "No PR found for branch $(tv BRANCH). Push it and run gh pr create.\n" || return 1
    title="$(jq -r .title <<<"$pr")"
    [[ "$title" =~ ^\[#${ISSUE}\]\ (feat|fix|refactor|test|docs|chore|ci|design)(\(.+\))?:\ .+ ]] \
        || gate_msg "PR title '$title' must be '[#$ISSUE] $(tv TYPE)(scope): description'. Fix: gh pr edit --title ...\n" || return 1
    jq -r .body <<<"$pr" | has "#$ISSUE" \
        || gate_msg "PR body must reference #$ISSUE. Fix: gh pr edit --body-file $IO/pr-body.md\n" || return 1
    jq -r .number <<<"$pr" > "$STATE/pr.number"
    git_wt fetch -q origin "$(tv BRANCH)"
    [ "$(git_wt rev-parse HEAD)" = "$(git_wt rev-parse "origin/$(tv BRANCH)" 2>/dev/null)" ] \
        || gate_msg "This phase makes no commits: the foreman pushes and records the PR in the ledger. Undo yours: git reset --hard origin/$(tv BRANCH)\n" || return 1
    require_clean_branch
}

# ------------------------------------------------------------------ phases
seed_triage() {  # seed_triage <use-case>: fresh triage files in $IO
    local uc="$1"
    rm -rf "$IO" && mkdir -p "$IO"
    # pre-fill whatever is derivable: every decision taken from the model is one it cannot get wrong
    local tp; tp="$(sed -nE '1s/^# #[0-9]+ (feat|fix|refactor|test|docs|chore|ci|design)[(:].*/\1/p' "$STATE/issue.md")"
    sed "s/{{ISSUE}}/$ISSUE/g;s/{{USE_CASE}}/${uc:-NONE}/g;s/^TYPE=?$/TYPE=${tp:-?}/" \
        "$HERE/templates/triage.env" > "$IO/triage.env"
    cp "$IO/triage.env" "$STATE/triage.seed.env"
    sed "s/{{ISSUE}}/$ISSUE/g" "$HERE/templates/triage.md" > "$IO/triage.md"
}

phase_triage() {
    (cd "$WT" && gh issue view "$ISSUE" --json number,title,state,labels,body,comments \
        --jq '"# #\(.number) \(.title)\nstate: \(.state) | labels: \([.labels[].name]|join(", "))\n\n\(.body)\n\n## Comments\n\([.comments[] | "- \(.author.login): \(.body)"] | join("\n"))"') \
        > "$STATE/issue.md" || fail "cannot read issue #$ISSUE"
    grep -q '^state: OPEN' "$STATE/issue.md" || fail "issue #$ISSUE is not open"
    git_wt status --porcelain | grep -v '^?? \.localai/' | has . && fail "worktree $WT is dirty"
    git_wt fetch -q origin && git_wt checkout -q --detach origin/main
    local uc; uc="$(sed -n '/Use Case/,/^## /p' "$STATE/issue.md" | grep -oE '(CU|RF|RNF)-?[0-9]+' | head -1)"
    # RECHECK keeps the worker's triage: re-seeding would hand the gate an empty skeleton
    [ "${RECHECK:-0}" = 1 ] || seed_triage "$uc"
    NO_COMMIT=1 SCOPE='^\.localai/' with_retries triage 01-triage.md gate_triage
}

phase_setup() {
    local branch change; branch="$(tv BRANCH)"; change="$(tv CHANGE)"
    git_wt fetch -q origin
    if git_wt show-ref -q "refs/heads/$branch"; then
        git_wt checkout -q "$branch" || fail "cannot checkout $branch"
    else
        git_wt checkout -q -b "$branch" origin/main || fail "cannot create $branch"
    fi
    [ -d "$WT/openspec/changes/$change" ] || (cd "$WT" && openspec new change "$change" >> "$STATE/foreman.log" 2>&1) \
        || fail "openspec new change $change failed"
    (cd "$WT" && gh issue edit "$ISSUE" --add-label in-progress >/dev/null) || log "warn: could not add in-progress label"
}

phase_spec() {
    NO_COMMIT=1 SCOPE="^(\.localai/|openspec/changes/$(tv CHANGE)/)" with_retries spec 03-spec.md gate_spec
    # the harness commits: the worker kept writing "Closes #n" into spec commits
    local subject="docs(openspec): specify $(tv CHANGE)" amend=()
    git_wt add "openspec/changes/$(tv CHANGE)" || fail "could not stage the spec"
    # a RECHECK or a review round with no edits leaves nothing to commit
    git_wt diff --cached --quiet && { log "spec unchanged — nothing to commit"; return 0; }
    # review rounds fold into the one spec commit instead of stacking new ones
    [ "$(git_wt log -1 --format=%s)" = "$subject" ] && amend=(--amend)
    git_wt commit -q ${amend[@]+"${amend[@]}"} -m "$subject" -m "Refs #$ISSUE" || fail "could not commit the spec"
    require_clean_branch || fail "branch dirty after spec commit: $(cat "$STATE/gate.out")"
}

phase_tests() {
    is_code || { log "KIND=$(tv KIND): no tests phase"; return 0; }
    SCOPE="^(\.localai/|openspec/changes/$(tv CHANGE)/)|$TEST_FILES" \
        with_retries tests 04-tests.md gate_red
}

phase_implement() {
    is_code || { log "KIND=$(tv KIND): implementation is the docs/ci edit itself"; echo "TEST_CMD=true" | tee "$IO/tests.env" > "$STATE/tests.env"; }
    local scope=.
    is_code && scope="$(python3 "$HERE/bin/scope.py" implement "$WT" "$(tv CHANGE)" "$SOURCE_ROOTS" \
        "$STATE/triage.md" "$WT/openspec/changes/$(tv CHANGE)/traceability.md")"
    SCOPE="$scope" with_retries implement 05-implement.md gate_green
}

phase_docs()    { with_retries docs 06-docs.md gate_docs; record_ledger; }

# record_ledger: the harness writes the SHA bookkeeping in traceability.md — the worker copied SHAs from main
# squash_spec_churn: fold the trailing run of commits that touch only the change's openspec dir into one commit —
# the worker commits every retry separately, and `gh pr merge --merge` would put that churn on main
squash_spec_churn() {
    local dir="openspec/changes/$(tv CHANGE)/" floor base c n=0
    floor="$(cat "$STATE/red.sha" 2>/dev/null || git_wt merge-base origin/main HEAD)"
    base="$(git_wt rev-parse HEAD)"
    for c in $(git_wt rev-list "$floor"..HEAD); do
        git_wt diff-tree --no-commit-id --name-only -r "$c" | grep -v "^$dir" | has . && break
        base="$(git_wt rev-parse "$c^")"; n=$((n + 1))
    done
    [ "$n" -gt 1 ] || return 0
    log "squashing $n openspec-only commits into one"
    git_wt reset -q --soft "$base" \
        && git_wt commit -q -m "docs(openspec): complete $(tv CHANGE) docs tasks" -m "Refs #$ISSUE" \
        || fail "could not squash openspec commits"
}

record_ledger() {
    local f="openspec/changes/$(tv CHANGE)/traceability.md" shas cl
    squash_spec_churn
    shas="$(git_wt log --reverse --format=%h origin/main..HEAD | paste -sd, - | sed 's/,/, /g')"
    python3 "$HERE/bin/ledger.py" row "$WT/$f" Commits "$shas" done || fail "cannot record commits in $f"
    cl="$(git_wt log -1 --format=%h origin/main..HEAD -- CHANGELOG.md)"
    [ -z "$cl" ] || python3 "$HERE/bin/ledger.py" row "$WT/$f" CHANGELOG.md yes "$cl" || fail "cannot record CHANGELOG in $f"
    git_wt diff --quiet -- "$f" && return 0
    git_wt add "$f" && git_wt commit -q -m "docs(openspec): record $(tv CHANGE) commits in ledger" -m "Refs #$ISSUE" \
        || fail "could not commit the ledger"
}
phase_quality() { harness_gate_loop preflight "$(need gates.preflight)"; }

phase_pipeline() {
    if [ "${SKIP_PIPELINE:-0}" = 1 ]; then
        is_code && fail "SKIP_PIPELINE is not allowed for KIND=code"
        log "pipeline skipped (docs/ci only)"; gate_log pipeline "SKIPPED :: KIND=$(tv KIND)"; return 0
    fi
    # the stack reads the git-ignored .env; the worktree has none, so reuse the main checkout's (a symlink: never copied, never committed)
    local env="$REPO/.env"
    [ -e "$WT/.env" ] || { [ -f "$env" ] && ln -s "$(cd "$(dirname "$env")" && pwd)/.env" "$WT/.env"; } \
        || fail "no .env in $WT and none at $env to link — create it from .env.example"
    harness_gate_loop pipeline "COMPOSE_PROJECT_NAME=$COMPOSE_PROJECT $(need gates.pipeline)"
}

# push_branch: pushing is mechanics, so the harness does it. The local model marks `git push` as needing sandbox
# escalation, Codex refuses that under approval=never, and the worker then loops (sed-edits, duplicate commits,
# reset to the remote). The repo's pre-push preflight is skipped: the quality phase ran it on this code and CI
# re-runs it. CI bots commit reports to open PR branches, hence the rebase.
push_branch() {
    local b; b="$(tv BRANCH)"
    # a detached HEAD would push the stale branch ref, not the work in the tree
    [ "$(git_wt symbolic-ref -q --short HEAD)" = "$b" ] || fail "HEAD is not on $b — cannot push"
    if git_wt ls-remote --exit-code -q origin "refs/heads/$b" >/dev/null; then
        git_wt pull -q --rebase origin "$b" \
            || { git_wt rebase --abort; fail "cannot rebase $b onto origin/$b (rebase aborted)"; }
    fi
    PREFLIGHT_SKIP=1 git_wt push -q -u origin "$b" || fail "cannot push $b"
}

record_pr() {
    local d="openspec/changes/$(tv CHANGE)" pr; pr="$(cat "$STATE/pr.number")"
    python3 "$HERE/bin/ledger.py" row "$WT/$d/traceability.md" "Pull Request" "#$pr" open \
        && python3 "$HERE/bin/ledger.py" tick "$WT/$d/tasks.md" 10.1 10.2 || fail "cannot record PR #$pr in $d"
    git_wt diff --quiet -- "$d" && return 0
    git_wt add "$d" && git_wt commit -q -m "docs(openspec): record PR for $(tv CHANGE)" -m "Refs #$ISSUE" \
        || fail "could not commit PR record"
}

phase_pr() {
    # checked here, on full messages: the worker read `git log --oneline` subjects and blocked twice on #1063
    git_wt log --format=%B origin/main..HEAD | has "Closes #$ISSUE" \
        || fail "no commit on $(tv BRANCH) says 'Closes #$ISSUE' — the implement gate should have caught this"
    cp "$STATE/gates.log" "$IO/gates.log"
    push_branch
    ALLOW_COMMIT=0 with_retries pr 09-pr.md gate_pr
    record_pr
    push_branch
}

# CI bots push "[skip ci]" report commits onto the PR branch; those carry no check runs, so `gh pr checks`
# (which reads the PR head) calls the PR failed — judge the last commit CI actually ran on instead
ci_sha() {
    git_wt fetch -q origin "$(tv BRANCH)" || fail "cannot fetch $(tv BRANCH)"
    git_wt log -1 --format=%H --invert-grep --grep='\[skip ci\]' "origin/$(tv BRANCH)"
}

# ci_state SHA: prints pending | green | red, and the check list to $STATE/gate-ci.out
ci_state() {
    local runs="repos/{owner}/{repo}/commits/$1/check-runs?per_page=100"
    (cd "$WT" && gh api "$runs" --jq '.check_runs[] | "\(.name)\t\(.status)\t\(.conclusion)"') > "$STATE/gate-ci.out" \
        || { echo pending; return 0; }
    (cd "$WT" && gh api "$runs" --jq '.check_runs
        | if length == 0 or any(.[]; .status != "completed") then "pending"
          elif all(.[]; .conclusion == "success" or .conclusion == "skipped" or .conclusion == "neutral") then "green"
          else "red" end')
}

# wait_ci: blocks until the checks on ci_sha settle (60 min cap); returns 0 only when green
wait_ci() {
    local sha state; sha="$(ci_sha)"
    for _ in $(seq 1 60); do
        state="$(ci_state "$sha")"
        [ "$state" = pending ] || break
        sleep 60
    done
    log "CI on ${sha:0:7}: $state"
    [ "$state" = green ]
}

phase_ci() {
    local pr attempt; pr="$(cat "$STATE/pr.number")"
    for attempt in $(seq 1 "$MAX_ATTEMPTS"); do
        sleep 30
        if wait_ci; then
            gate_log ci "PASS :: PR #$pr"; return 0
        fi
        gate_log ci "FAIL :: PR #$pr"
        { cat "$STATE/gate-ci.out"; echo; echo "## Failed job logs (tail)"
          (cd "$WT" && gh run list --branch "$(tv BRANCH)" --status failure --limit 3 --json databaseId --jq '.[].databaseId' \
              | while read -r id; do gh run view "$id" --log-failed 2>/dev/null | tail -80; done); } > "$STATE/gate.out"
        [ "$attempt" -eq "$MAX_ATTEMPTS" ] && break
        PHASE_BASE="$(git_wt rev-parse HEAD)"   # already pushed: never rewrite
        run_worker "ci-fix$attempt" 07-fix-gate.md "GitHub Actions CI on PR #$pr" "$STATE/gate.out"
        require_clean_branch || fail "worker left the branch dirty after CI fix: $(cat "$STATE/gate.out")"
        push_branch
    done
    fail "CI still red on PR #$pr — foreman must intervene"
}

phase_review() {
    log "READY FOR FOREMAN REVIEW: PR #$(cat "$STATE/pr.number") — review, then: foreman.sh $ISSUE merge"
    exit 0
}

# main_run WF SHA: id of the newest main run of WF on SHA or a later commit. cd.yml is triggered by
# workflow_run, so it runs on main's head when CI ends — often a bot's "[skip ci]" report commit after SHA
main_run() {
    local id head
    git_wt fetch -q origin main
    (cd "$WT" && gh run list --workflow "$1" --branch main --limit 10 --json databaseId,headSha \
        --jq '.[] | "\(.databaseId) \(.headSha)"') | while read -r id head; do
        git_wt merge-base --is-ancestor "$2" "$head" 2>/dev/null && { echo "$id"; break; }
    done
}

merge_and_close() {
    local pr sha id ok=1; pr="$(cat "$STATE/pr.number")" || fail "no PR recorded"
    [ "$(ci_state "$(ci_sha)")" = green ] || fail "CI not green on PR #$pr (Gate 4): $(cat "$STATE/gate-ci.out")"
    (cd "$WT" && gh pr merge "$pr" --merge) || fail "merge failed"
    gate_log merge "PASS :: PR #$pr merged"
    sha="$(cd "$WT" && gh pr view "$pr" --json mergeCommit --jq .mergeCommit.oid)"
    log "Gate 5: waiting for CI + CD on $sha"
    for wf in $(need gates.main_workflows); do
        id=""
        for _ in $(seq 1 30); do
            id="$(main_run "$wf" "$sha")"
            [ -n "$id" ] && break; sleep 30
        done
        [ -n "$id" ] || { log "warn: no $wf run for $sha"; gate_log "$wf" "MISSING :: $sha"; continue; }
        (cd "$WT" && gh run watch "$id" --exit-status > "$STATE/gate-$wf.out" 2>&1) \
            && gate_log "$wf" "PASS :: run $id" || fail "$wf failed on main (run $id) — Gate 5 not met"
    done
    log "smoke test: stack from merged main"
    git_wt fetch -q origin && git_wt checkout -q --detach origin/main
    (cd "$WT" && COMPOSE_PROJECT_NAME=$COMPOSE_PROJECT bash -c "$(need gates.start)" > "$STATE/gate-smoke.out" 2>&1)
    for _ in $(seq 1 40); do curl -sf "$HEALTH_URL" | has UP && { ok=0; break; }; sleep 6; done
    [ $ok -eq 0 ] || fail "smoke test failed (see $STATE/gate-smoke.out)"
    gate_log smoke "PASS :: $HEALTH_URL UP on $sha"
    (cd "$WT" && gh issue view "$ISSUE" --json state --jq .state | has CLOSED) \
        || (cd "$WT" && gh issue close "$ISSUE" --comment "Merged in #$pr; CI+CD green on main, smoke test passed (Gate 5).")
    (cd "$WT" && gh issue edit "$ISSUE" --remove-label in-progress >/dev/null 2>&1)
    log "issue #$ISSUE DONE"
}

# review_fix: the Gate 4 feedback channel once a PR is open — the worker applies the foreman's
# notes (commits allowed, pushes not), the foreman pushes, and CI + review run again
review_fix() {
    local notes="$STATE/gate4.md"
    [ -f "$notes" ] || fail "no review notes: write $notes first"
    PHASE_BASE="$(git_wt rev-parse HEAD)"   # already pushed: never rewrite
    run_worker gate4-fix 10-review.md "foreman Gate 4 review" "$notes"
    require_clean_branch || fail "worker left the branch dirty after review fix: $(cat "$STATE/gate.out")"
    push_branch
    mv "$notes" "$STATE/gate4-applied-$(date +%s).md"
}

# review_notes_check: run the CHECK lines of every pending review note in the worker worktree, so the
# foreman sees which notes are already met before re-running the gate (no worker run)
review_notes_check() {
    local f notes=()
    for f in "$STATE"/review-*.md "$STATE/gate4.md"; do
        [ -f "$f" ] && [[ "$f" != *.done.md ]] && notes+=("$f")
    done
    [ ${#notes[@]} -gt 0 ] || fail "no pending review notes in $STATE"
    python3 "$HERE/bin/review_check.py" "$WT" "${notes[@]}"
}

# ------------------------------------------------------------------ main
[ "$FROM" = check ] && { review_notes_check; exit $?; }
[ "$FROM" = merge ] && { merge_and_close; exit 0; }
[ "$FROM" = fix ] && { review_fix; FROM=ci; }
if [ -n "$FROM" ]; then  # forget $FROM and every later phase
    [[ " ${PHASES[*]} " == *" $FROM "* ]] || fail "unknown phase $FROM (${PHASES[*]})"
    tmp="$(mktemp)"; keep=1
    for p in "${PHASES[@]}"; do [ "$p" = "$FROM" ] && keep=0; [ $keep = 1 ] && is_done "$p" && echo "$p"; done > "$tmp"
    mv "$tmp" "$STATE/phases.done"
fi
exclude="$(git_wt rev-parse --path-format=absolute --git-common-dir)/info/exclude"
grep -qx '.localai/' "$exclude" 2>/dev/null || echo '.localai/' >> "$exclude"

for p in "${PHASES[@]}"; do
    is_done "$p" && continue
    log "=== phase $p ==="
    "phase_$p"
    mark_done "$p"
    [ "${STOP_AFTER:-}" = "$p" ] && { log "STOP_AFTER=$p reached"; exit 0; }
done
