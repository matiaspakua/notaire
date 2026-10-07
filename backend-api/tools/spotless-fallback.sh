#!/usr/bin/env bash
#
# Portable fallback for `mvn spotless:{check,apply}` on backend-api.
#
# WHY THIS EXISTS
# ---------------
# The Spotless config in backend-api/pom.xml uses <ratchetFrom>origin/main</ratchetFrom>,
# which makes the plugin resolve the repository through JGit. JGit walks parent
# directories looking for a `.git` *directory*; inside a git worktree `.git` is a
# *file* containing a `gitdir:` pointer, so the plugin aborts with
# "Cannot find git repository in any parent directory" and no formatting can be
# checked locally. CI checks out normally, so it only ever fails there — exactly
# the local/CI gap preflight.sh exists to close.
#
# This script implements the four rules configured in the pom, so it produces the
# same result as Spotless for the file set Spotless would consider:
#   <removeUnusedImports/>  <trimTrailingWhitespace/>  <endWithNewline/>
#   <indent><spaces>true</spaces><spacesPerTab>4</spacesPerTab></indent>
#
# Scope matches ratchetFrom: only .java files under backend-api/ that differ from
# the ratchet ref (default origin/main). Spotless ratchets at file granularity —
# a touched file is formatted in full — so this does the same.
#
# Usage: spotless-fallback.sh check|apply [ratchet-ref]
set -uo pipefail

MODE="${1:-check}"
RATCHET_REF="${2:-origin/main}"
REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"

if ! git rev-parse --verify --quiet "$RATCHET_REF" >/dev/null; then
    echo "spotless-fallback: ratchet ref '$RATCHET_REF' not found; run 'git fetch origin main' first." >&2
    exit 2
fi

mapfile -t FILES < <(git diff --name-only "${RATCHET_REF}...HEAD" -- 'backend-api/**/*.java' 'backend-api/*.java'; \
                     git diff --name-only -- 'backend-api/**/*.java' 'backend-api/*.java'; \
                     git diff --name-only --cached -- 'backend-api/**/*.java' 'backend-api/*.java')
# de-dup, keep only files that still exist
mapfile -t FILES < <(printf '%s\n' "${FILES[@]}" | sort -u | while read -r f; do [ -n "$f" ] && [ -f "$f" ] && echo "$f"; done)

if [ "${#FILES[@]}" -eq 0 ]; then
    echo "spotless-fallback: no changed Java files under backend-api/ — nothing to do."
    exit 0
fi

# Rewrites one file per the four configured rules; prints nothing.
# Unused-import removal is intentionally conservative: an import is dropped only
# when its simple name appears nowhere else in the file (outside the import
# block). Wildcard and static imports are never touched -- checkstyle.xml already
# bans wildcards, and static-import usage is hard to detect textually.
format_one() {
    python3 - "$1" <<'PY'
import re, sys

path = sys.argv[1]
with open(path, encoding="utf-8") as fh:
    original = fh.read()

lines = original.split("\n")

# --- removeUnusedImports -------------------------------------------------
import_re = re.compile(r'^\s*import\s+(static\s+)?([\w.]+)\s*;\s*$')
import_idx = [i for i, l in enumerate(lines) if import_re.match(l)]
body = "\n".join(l for i, l in enumerate(lines) if i not in set(import_idx))

drop = set()
for i in import_idx:
    m = import_re.match(lines[i])
    is_static, fqn = m.group(1), m.group(2)
    if is_static or fqn.endswith(".*"):
        continue
    simple = fqn.rsplit(".", 1)[-1]
    if not re.search(r'\b' + re.escape(simple) + r'\b', body):
        drop.add(i)

lines = [l for i, l in enumerate(lines) if i not in drop]

# --- indent: tabs -> 4 spaces (leading whitespace only) ------------------
def untab(line):
    m = re.match(r'^[ \t]+', line)
    if not m:
        return line
    lead = m.group(0)
    col = 0
    for ch in lead:
        col = col + 4 - (col % 4) if ch == "\t" else col + 1
    return " " * col + line[len(lead):]

lines = [untab(l) for l in lines]

# --- trimTrailingWhitespace ---------------------------------------------
lines = [l.rstrip() for l in lines]

# --- endWithNewline (exactly one trailing newline) -----------------------
while lines and lines[-1] == "":
    lines.pop()
result = "\n".join(lines) + "\n"

if result != original:
    with open(path, "w", encoding="utf-8") as fh:
        fh.write(result)
    sys.exit(1)   # signals "file was not compliant"
sys.exit(0)
PY
}

VIOLATIONS=()
for f in "${FILES[@]}"; do
    if [ "$MODE" = "check" ]; then
        before="$(mktemp)"; cp "$f" "$before"
        format_one "$f" || VIOLATIONS+=("$f")
        cp "$before" "$f"; rm -f "$before"       # check must not mutate
    else
        format_one "$f" && true
        [ $? -eq 1 ] && VIOLATIONS+=("$f")
    fi
done

if [ "${#VIOLATIONS[@]}" -eq 0 ]; then
    echo "spotless-fallback: all ${#FILES[@]} changed Java file(s) are correctly formatted."
    exit 0
fi

if [ "$MODE" = "apply" ]; then
    printf 'spotless-fallback: reformatted %d file(s):\n' "${#VIOLATIONS[@]}"
    printf '  %s\n' "${VIOLATIONS[@]}"
    exit 0
fi

printf 'spotless-fallback: %d file(s) violate the format rules:\n' "${#VIOLATIONS[@]}"
printf '  %s\n' "${VIOLATIONS[@]}"
echo "Run: bash scripts/preflight.sh --fix   (or: mvn spotless:apply -pl backend-api)"
exit 1
