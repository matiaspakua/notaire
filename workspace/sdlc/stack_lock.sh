#!/usr/bin/env bash
#
# stack_lock.sh — one stack-backed preflight at a time (#2414).
#
#   stack_lock.sh acquire <pid>   take the lock for the run <pid>; returns once held,
#                                 exits 1 naming the holder after PREFLIGHT_STACK_WAIT seconds
#   stack_lock.sh release <pid>   drop the lock if <pid> holds it
#
# The lock is a directory (mkdir is atomic; macOS has no flock) in the git common directory, so every
# worktree shares it. A lock whose holder pid is no longer alive is stale and taken over.
set -uo pipefail

COMMON="${GIT_COMMON_DIR:-$(git rev-parse --path-format=absolute --git-common-dir)}"
LOCK_DIR="$COMMON/preflight-stack.lock"
WAIT="${PREFLIGHT_STACK_WAIT:-3600}"
POLL="${PREFLIGHT_STACK_POLL:-30}"

holder_line() { sed -n "${1}p" "$LOCK_DIR/holder" 2>/dev/null; }

acquire() {
    local pid="$1" waited=0
    while ! mkdir "$LOCK_DIR" 2>/dev/null; do
        local holder; holder="$(holder_line 1)"
        if [ -n "$holder" ] && ! kill -0 "$holder" 2>/dev/null; then
            echo "stack lock: holder pid $holder is gone; taking over"
            rm -rf "$LOCK_DIR"
            continue
        fi
        if [ "$waited" -ge "$WAIT" ]; then
            echo "stack lock: still held after ${WAIT}s by pid ${holder:-?} (branch $(holder_line 2), since $(holder_line 3))" >&2
            return 1
        fi
        echo "stack lock: held by pid ${holder:-?} (branch $(holder_line 2), since $(holder_line 3)); waiting"
        sleep "$POLL"
        waited=$((waited + POLL))
    done
    printf '%s\n%s\n%s\n' "$pid" "$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo unknown)" \
        "$(date '+%Y-%m-%d %H:%M:%S')" > "$LOCK_DIR/holder"
    echo "stack lock: acquired for pid $pid"
}

release() {
    [ "$(holder_line 1)" = "$1" ] && rm -rf "$LOCK_DIR" && echo "stack lock: released"
    return 0
}

case "${1:-}" in
    acquire|release)
        [ -n "${2:-}" ] || { echo "usage: $0 $1 <pid>" >&2; exit 2; }
        "$1" "$2" ;;
    *) echo "usage: $0 {acquire|release} <pid>" >&2; exit 2 ;;
esac
