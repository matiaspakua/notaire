# Sourced by the harness git hooks. LOCALAI_STATE_FILE (baked in by foreman.sh)
# holds: BASE=<sha the phase started from>  BRANCH=<branch>  ALLOW_COMMIT=0|1
. "$(dirname "$0")/state.env"
deny() { echo "FOREMAN GUARD: $*" >&2; exit 1; }
chain() {  # run the repo's own hook of the same name, if any
    local h; h="$(git rev-parse --show-toplevel)/.githooks/$(basename "$0")"
    [ -x "$h" ] && exec "$h" "$@"
    exit 0
}
