#!/usr/bin/env bash
#
# stack_lock.sh — exclusive lock for the preflight server-backed Docker stack.
#
# WHY
# ---
# Multiple preflight runs (worktrees, sandboxes) share the same Docker host.
# Without a lock they collide on container names, ports, and the database.
# macOS has no `flock`, so we use atomic `mkdir` as the lock primitive.
#
# USAGE
#   bash workspace/sdlc/stack_lock.sh acquire   # blocks until acquired or timeout
#   bash workspace/sdlc/stack_lock.sh release   # drop the lock
#
# ENV
#   GIT_COMMON_DIR       — override the git common dir discovery (tests only)
#   PREFLIGHT_STACK_WAIT — max seconds to wait (default 3600)
#
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"

# Resolve the git common directory (shared across worktrees).
if [ -n "${GIT_COMMON_DIR:-}" ]; then
    _GIT_COMMON="$GIT_COMMON_DIR"
else
    _GIT_COMMON="$(git rev-parse --path-format=absolute --git-common-dir)"
fi

LOCK_DIR="$_GIT_COMMON/preflight-stack.lock"
PREFLIGHT_STACK_WAIT="${PREFLIGHT_STACK_WAIT:-3600}"
PREFLIGHT_STACK_POLL="${PREFLIGHT_STACK_POLL:-30}"

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

_pid_alive() {
    local pid="$1"
    kill -0 "$pid" 2>/dev/null
}

_holder_info() {
    # Read the holder file; prints "PID branch start_time" or empty.
    local hf="$LOCK_DIR/holder"
    [ -f "$hf" ] && cat "$hf" | tr '\n' ' '
}

# ---------------------------------------------------------------------------
# acquire — take the lock (block until success or timeout)
# ---------------------------------------------------------------------------

acquire() {
    local start=$SECONDS
    local waited=0

    while true; do
        # Try atomic mkdir — succeeds only for the first caller.
        if mkdir "$LOCK_DIR" 2>/dev/null; then
            # We got it. Write holder info.
            local branch
            branch="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo unknown)"
            printf '%s\n%s\n%s\n' "$$" "$branch" "$start" > "$LOCK_DIR/holder"
            echo "stack lock acquired (pid $$, branch $branch)"
            # Stay alive so the PID in the holder file remains valid.
            # The caller is expected to run this in the background or use it
            # within a subshell that persists for the locked section.
            # `read -t` blocks until killed; portable on macOS + Linux.
            read -r -t 999999999 2>/dev/null || true
            return 0
        fi

        # Lock exists — check if the holder is still alive.
        local holder_pid=""
        if [ -f "$LOCK_DIR/holder" ]; then
            holder_pid="$(sed -n '1p' "$LOCK_DIR/holder")"
        fi

        if [ -n "$holder_pid" ] && _pid_alive "$holder_pid"; then
            # Holder is alive — wait.
            if [ "$waited" -ge "$PREFLIGHT_STACK_WAIT" ]; then
                local holder_branch="" holder_start=""
                if [ -f "$LOCK_DIR/holder" ]; then
                    holder_branch="$(sed -n '2p' "$LOCK_DIR/holder")"
                    holder_start="$(sed -n '3p' "$LOCK_DIR/holder")"
                fi
                echo "stack lock timeout after ${PREFLIGHT_STACK_WAIT}s — holder: pid $holder_pid (branch ${holder_branch:-?}, started ${holder_start:-?}s ago)" >&2
                return 1
            fi

            local holder_info_str
            holder_info_str="$(_holder_info)"
            echo "stack lock held (pid $holder_pid); waited ${waited}s / ${PREFLIGHT_STACK_WAIT}s — $holder_info_str"
            sleep "$PREFLIGHT_STACK_POLL"
            waited=$((SECONDS - start))
            continue
        fi

        # Holder is dead or missing — stale lock. Take it over.
        echo "stale lock detected (pid $holder_pid no longer alive); taking over"
        rm -rf "$LOCK_DIR"
        # Loop will retry mkdir on the next iteration.
    done
}

# ---------------------------------------------------------------------------
# release — drop the lock
# ---------------------------------------------------------------------------

release() {
    if [ -d "$LOCK_DIR" ]; then
        rm -rf "$LOCK_DIR"
        echo "stack lock released"
    else
        echo "stack lock was not held"
    fi
    return 0
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

case "${1:-}" in
    acquire)  acquire ;;
    release)  release ;;
    *)
        echo "Usage: $0 {acquire|release}" >&2
        exit 2
        ;;
esac
