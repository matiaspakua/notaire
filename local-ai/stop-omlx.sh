#!/bin/bash
# stop-omlx.sh — detiene el stack IA local (servidor oMLX + app menú)
set -u

log() { printf '==> %s\n' "$*"; }
ok()  { printf ' OK %s\n' "$*"; }

log "Deteniendo servidor oMLX"
[ -x "$HOME/.omlx/bin/omlx" ] && "$HOME/.omlx/bin/omlx" stop >/dev/null 2>&1
pkill -f omlx-server 2>/dev/null && sleep 2
osascript -e 'quit app "oMLX"' >/dev/null 2>&1

sleep 2
if curl -sf -m 3 localhost:8000/v1/models >/dev/null 2>&1; then
  pkill -9 -f omlx-server 2>/dev/null
  sleep 1
fi

if curl -sf -m 3 localhost:8000/v1/models >/dev/null 2>&1; then
  echo " ERROR: el servidor sigue respondiendo en :8000"
  exit 1
fi
ok "stack detenido (puerto 8000 libre)"
