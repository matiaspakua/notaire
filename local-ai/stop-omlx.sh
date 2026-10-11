#!/bin/bash
# stop-omlx.sh — stops the local AI stack (oMLX server + menu-bar app)
set -u

log() { printf '==> %s\n' "$*"; }
ok()  { printf ' OK %s\n' "$*"; }

log "Stopping the oMLX server"
[ -x "$HOME/.omlx/bin/omlx" ] && "$HOME/.omlx/bin/omlx" stop >/dev/null 2>&1
pkill -f omlx-server 2>/dev/null && sleep 2
osascript -e 'quit app "oMLX"' >/dev/null 2>&1

sleep 2
if curl -sf -m 3 localhost:8000/v1/models >/dev/null 2>&1; then
  pkill -9 -f omlx-server 2>/dev/null
  sleep 1
fi

if curl -sf -m 3 localhost:8000/v1/models >/dev/null 2>&1; then
  echo " ERROR: the server is still responding on :8000"
  exit 1
fi
ok "stack stopped (port 8000 is free)"
