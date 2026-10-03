#!/bin/bash
# Kept as the stable integration entry point: Constitution step 14, preflight, CLAUDE.md,
# AGENTS.md and the agent rules call this path. The logic lives in run.sh.
exec bash "$(dirname "${BASH_SOURCE[0]}")/run.sh" integration "$@"
