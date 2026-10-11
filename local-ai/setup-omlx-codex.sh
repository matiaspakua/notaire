#!/bin/bash
# setup-omlx-codex.sh — oMLX + a local model (MLX) + Codex CLI, fully local on Apple Silicon
# Idempotent: running it again is safe. Applies performance settings for coding agents.
#   PRESET=qwen3-coder (default) | gpt-oss   — each preset has its own Codex profile
set -euo pipefail

LOCAL_AI_DIR="$(cd "$(dirname "$0")" && pwd)"
MODEL_INPUT="${1:-${MODEL_REPO:-${PRESET:-qwen3-coder}}}"
RESOLVE_OUT=$(python3 "$LOCAL_AI_DIR/models_config.py" resolve "$MODEL_INPUT") || exit 1
eval "$RESOLVE_OUT"

MODEL_DIR="$HOME/.omlx/models/$MODEL_REPO"
WIRED_LIMIT_MB="${WIRED_LIMIT_MB:-20480}"
MEMORY_CEILING_GB=$((WIRED_LIMIT_MB / 1024))   # oMLX ceiling = Metal limit
OMLX_PORT="${OMLX_PORT:-8000}"
BASE_URL="http://localhost:$OMLX_PORT/v1"
CODEX_CONFIG="$HOME/.codex/config.toml"
CODEX_CATALOG="$HOME/.codex/$CODEX_PROFILE.models.json"
CODEX_INSTRUCTIONS="$LOCAL_AI_DIR/codex-local-instructions.md"
SSD_CACHE_DIR="$HOME/.omlx/cache"

log() { printf '\n\033[1;36m==> %s\033[0m\n' "$*"; }
ok()  { printf '\033[32m OK %s\033[0m\n' "$*"; }
warn(){ printf '\033[33m !! %s\033[0m\n' "$*"; }
die() { printf '\033[31m ERROR: %s\033[0m\n' "$*" >&2; exit 1; }

[[ "$(uname -s)" == "Darwin" ]] || die "macOS only"
[[ "$(uname -m)" == "arm64"   ]] || die "Apple Silicon only"
command -v brew >/dev/null || die "Homebrew required: https://brew.sh"

server_healthy() { curl -sf "http://localhost:$OMLX_PORT/v1/models" >/dev/null 2>&1; }

# ---------------------------------------------------------------- 1. oMLX
install_omlx_app() {
  log "Downloading oMLX.app (stable release, precompiled kernels)"
  local url tmp mnt
  url=$(curl -s https://api.github.com/repos/jundot/omlx/releases/latest \
        | python3 -c "import json,sys;a=json.load(sys.stdin).get('assets',[]);print(next((x['browser_download_url'] for x in a if x['name'].endswith('.dmg')),''))") \
        || die "could not query jundot/omlx releases"
  [ -n "$url" ] || die "no DMG in the latest release"
  tmp=$(mktemp -d)
  curl -L "$url" -o "$tmp/omlx.dmg" || die "DMG download failed"
  mnt=$(mktemp -d)
  hdiutil attach "$tmp/omlx.dmg" -nobrowse -quiet -mountpoint "$mnt" || die "DMG mount failed"
  cp -R "$mnt"/oMLX*.app /Applications/ || { hdiutil detach "$mnt" -quiet || true; die "copy of oMLX.app failed"; }
  hdiutil detach "$mnt" -quiet || true
  rm -rf "$tmp"
  ok "oMLX.app installed in /Applications"
}

if [ -d /Applications/oMLX.app ] || command -v omlx >/dev/null || [ -x "$HOME/.omlx/bin/omlx" ]; then
  ok "oMLX already installed"
else
  install_omlx_app
fi

mkdir -p "$HOME/.omlx/models" "$SSD_CACHE_DIR"

# ---------------------------------------------------------------- 2. perf settings
# Lessons from agent use (Codex): long sessions accumulate KV cache and the
# conservative guard throttles prefill. KV on SSD + a fixed ceiling = the Metal limit.
# Tier "custom": the safe/balanced/aggressive tiers cap at the RAM reclaimable at
# that moment (~16 GB with a browser open) and reject the 30B model (16.8 GB). With a fixed
# ceiling macOS compresses or swaps the other apps instead of oMLX refusing to load.
apply_perf_settings() {
  python3 "$LOCAL_AI_DIR/models_config.py" apply-omlx "$MODEL_INPUT"
  python3 "$LOCAL_AI_DIR/models_config.py" apply-opencode "$MODEL_INPUT"
}

# Metal: Apple limits GPU memory to ~75% of RAM unless iogpu.wired_limit_mb is raised.
# oMLX uses min(custom ceiling, that limit): with the default the 30B model does not fit with context.
check_wired_limit() {
  local current
  current=$(sysctl -n iogpu.wired_limit_mb 2>/dev/null || echo 0)
  if [ "${current:-0}" -ge "$WIRED_LIMIT_MB" ]; then
    ok "iogpu.wired_limit_mb=$current (>= $WIRED_LIMIT_MB)"
    return 0
  fi
  warn "iogpu.wired_limit_mb=$current: Metal stays at ~75% of RAM and $MODEL_ID does not fit with ${CONTEXT_WINDOW} of context."
  warn "Run (needs a password, lost on reboot):  sudo sysctl iogpu.wired_limit_mb=$WIRED_LIMIT_MB"
  warn "Persistent: echo 'iogpu.wired_limit_mb=$WIRED_LIMIT_MB' | sudo tee -a /etc/sysctl.conf"
  return 1
}

settings_hash() {
  cat "$HOME/.omlx/settings.json" "$HOME/.omlx/model_settings.json" 2>/dev/null | md5 -q
}

SETTINGS_CHANGED=0
if server_healthy || pgrep -f omlx-server >/dev/null 2>&1; then
  log "Applying performance settings to ~/.omlx/"
else
  log "Writing performance settings to ~/.omlx/"
fi
BEFORE=$(settings_hash)
apply_perf_settings || warn "settings.json is not available yet; it will be applied on the next run"
AFTER=$(settings_hash)
[ "$BEFORE" != "$AFTER" ] && SETTINGS_CHANGED=1

# oMLX 0.7.0 drops gpt-oss tool calls, and Codex rejects the ones that arrive malformed (#1099).
# An oMLX update undoes the patch: running this script again reapplies it.
if [ "$OMLX_PATCH" = "1" ]; then
  log "Patching oMLX for $MODEL_ID tool calls (local-ai/omlx/patch_omlx.py)"
  PATCH_OUT=$(python3 "$LOCAL_AI_DIR/omlx/patch_omlx.py") \
    || warn "part of the patch does not apply (did oMLX change that code?):"
  printf '%s\n' "$PATCH_OUT"
  grep -q ": applied" <<< "$PATCH_OUT" && SETTINGS_CHANGED=1
fi

log "Metal memory limit"
check_wired_limit || true

restart_server() {
  log "Restarting the oMLX server to apply the new settings"
  # the whole app: killing only omlx-server leaves the app open without a server, and `open -a` does not relaunch it
  pkill -x oMLX 2>/dev/null || true
  pkill -f omlx-server 2>/dev/null || true
  sleep 3
  if [ -d /Applications/oMLX.app ]; then open -a oMLX || true; fi
  for i in $(seq 1 90); do server_healthy && break; sleep 2; done
  if ! server_healthy && command -v omlx >/dev/null && ! lsof -i ":$OMLX_PORT" >/dev/null 2>&1; then
    nohup omlx serve --model-dir "$HOME/.omlx/models" --port "$OMLX_PORT" \
      --memory-guard custom --memory-guard-gb "$MEMORY_CEILING_GB" \
      --paged-ssd-cache-dir "$SSD_CACHE_DIR" \
      >"$HOME/.omlx-serve.log" 2>&1 &
    for i in $(seq 1 90); do server_healthy && break; sleep 2; done
  fi
  server_healthy || die "server did not come up after restart (see ~/.omlx/logs and ~/.omlx-serve.log)"
  ok "server restarted with performance settings active"
}

start_server() {
  if server_healthy; then
    if [ "$SETTINGS_CHANGED" = "1" ] || ! curl -s "$BASE_URL/models" | grep -q "\"$MODEL_ID\""; then
      restart_server
    else
      ok "oMLX server active on :$OMLX_PORT"
    fi
    return 0
  fi
  if [ -d /Applications/oMLX.app ]; then
    log "Starting oMLX.app (cold start can take about 2 min)"
    open -a oMLX || true
  elif [ -x "$HOME/.omlx/bin/omlx" ]; then
    "$HOME/.omlx/bin/omlx" start >/dev/null 2>&1 || true
  fi
  for i in $(seq 1 90); do server_healthy && break; sleep 2; done
  if ! server_healthy && command -v omlx >/dev/null && ! lsof -i ":$OMLX_PORT" >/dev/null 2>&1; then
    warn "app did not respond; launching the CLI server directly"
    nohup omlx serve --model-dir "$HOME/.omlx/models" --port "$OMLX_PORT" \
      --memory-guard custom --memory-guard-gb "$MEMORY_CEILING_GB" \
      --paged-ssd-cache-dir "$SSD_CACHE_DIR" \
      >"$HOME/.omlx-serve.log" 2>&1 &
    for i in $(seq 1 90); do server_healthy && break; sleep 2; done
  fi
  server_healthy || die "oMLX server did not come up on :$OMLX_PORT (see ~/.omlx/logs and ~/.omlx-serve.log)"
  ok "oMLX serving at $BASE_URL"
}

# ---------------------------------------------------------------- 3. model
if [ -f "$MODEL_DIR/config.json" ] && [ -n "$(ls "$MODEL_DIR"/*.safetensors 2>/dev/null | head -1)" ]; then
  ok "model present: $MODEL_REPO ($(du -sh "$MODEL_DIR" | cut -f1))"
else
  log "Downloading model $MODEL_REPO ($SUMMARY)"
  if ! command -v hf >/dev/null && ! command -v huggingface-cli >/dev/null; then
    python3 -m pip install -q -U "huggingface_hub[cli]" || die "installing huggingface_hub"
  fi
  mkdir -p "$MODEL_DIR"
  if command -v hf >/dev/null; then HF_CMD="hf download"; else HF_CMD="huggingface-cli download"; fi
  $HF_CMD "$MODEL_REPO" --local-dir "$MODEL_DIR" || die "model download failed"
  ok "model downloaded to $MODEL_DIR"
fi

start_server

log "Models visible on the API:"
curl -s "$BASE_URL/models" | python3 -c "import json,sys;[print('  -',m['id']) for m in json.load(sys.stdin)['data']]"

log "Smoke test chat completion (this loads the model and can take about 1 min)"
curl -s "$BASE_URL/chat/completions" -H 'Content-Type: application/json' -d '{
  "model": "'"$MODEL_ID"'",
  "messages": [{"role":"user","content":"Reply with only: READY"}],
  "max_tokens": 2000, "temperature": 0
}' | python3 -c "
import json,sys;r=json.load(sys.stdin)
if 'choices' not in r: sys.exit('  '+r.get('error',{}).get('message',str(r)))
print('  reply:',r['choices'][0]['message']['content'].strip()[:80])" \
  || die "smoke test failed (if it is memory: iogpu.wired_limit_mb and close apps, see above)"
ok "local inference working"

# ---------------------------------------------------------------- 4. Codex CLI
log "Configuring Codex CLI (profile '$CODEX_PROFILE' in ~/.codex/$CODEX_PROFILE.config.toml)"
command -v codex >/dev/null || [ -x "/Applications/ChatGPT.app/Contents/Resources/codex" ] \
  || die "Codex CLI not found (npm i -g @openai/codex)"

cp "$CODEX_CONFIG" "$CODEX_CONFIG.bak-$(date +%Y%m%d-%H%M%S)" 2>/dev/null || true

python3 "$LOCAL_AI_DIR/models_config.py" apply-codex "$MODEL_INPUT"
ok "Codex configured: codex --profile $CODEX_PROFILE  |  codex exec --profile $CODEX_PROFILE ..."
CODEX_SMOKE=$(OMLX_API_KEY=1234 codex exec --profile "$CODEX_PROFILE" --skip-git-repo-check -C "$(mktemp -d)" \
  "Reply with exactly: OK" </dev/null 2>&1) || die "codex exec failed: $(tail -3 <<< "$CODEX_SMOKE")"
if grep -q "fallback metadata" <<< "$CODEX_SMOKE"; then
  warn "Codex ignores the catalog $CODEX_CATALOG (did its format change in this Codex version?)"
else
  ok "Codex replies with the catalog metadata for $MODEL_ID"
fi

# ---------------------------------------------------------------- 5. GitHub token (reuses the gh CLI)
log "Configuring the GitHub token for MCP (reuses gh CLI authentication)"
if command -v gh >/dev/null && gh auth status >/dev/null 2>&1; then
  SHELL_RC="$HOME/.zshrc"
  [[ "$SHELL" == */bash ]] && SHELL_RC="$HOME/.bashrc"
  TOKEN_LINE='export GITHUB_PERSONAL_ACCESS_TOKEN=$(gh auth token)'
  if ! grep -qF "GITHUB_PERSONAL_ACCESS_TOKEN" "$SHELL_RC" 2>/dev/null; then
    echo "" >> "$SHELL_RC"
    echo "# GitHub MCP — used by Codex plugin (auto-added by setup-omlx-codex.sh)" >> "$SHELL_RC"
    echo "$TOKEN_LINE" >> "$SHELL_RC"
    ok "Added to $SHELL_RC — reload the shell or run: source $SHELL_RC"
  else
    ok "GitHub token already configured in $SHELL_RC"
  fi
  export GITHUB_PERSONAL_ACCESS_TOKEN=$(gh auth token)
else
  warn "gh CLI is not authenticated — run 'gh auth login' and run this script again"
fi

# ---------------------------------------------------------------- 5b. oMLX API key for Codex
# oMLX accepts any string as an API key. Codex reads the secret via env_key
# from the process ENVIRONMENT, not from the config file. An `export` in .zshrc
# only reaches new interactive shells; processes launched from the GUI,
# agents, or `resume` (which rehydrates sessions) can inherit an env without the var
# and fail with "Missing environment variable: OMLX_API_KEY".
# Fix: besides the export in .zshrc, register it in launchd (launchctl
# setenv) so every macOS process inherits it.
log "Configuring OMLX_API_KEY for Codex (shell + global launchctl)"
OMLX_API_KEY_LINE='export OMLX_API_KEY="1234"'
if ! grep -qF "OMLX_API_KEY" "$SHELL_RC" 2>/dev/null; then
  echo "" >> "$SHELL_RC"
  echo "# oMLX local model API key (auto-added by setup-omlx-codex.sh)" >> "$SHELL_RC"
  echo "$OMLX_API_KEY_LINE" >> "$SHELL_RC"
  ok "Added OMLX_API_KEY to $SHELL_RC"
else
  ok "OMLX_API_KEY already configured in $SHELL_RC"
fi
export OMLX_API_KEY="1234"
launchctl setenv OMLX_API_KEY "1234" && ok "OMLX_API_KEY registered in launchd (global: every process inherits it)" || warn "launchctl setenv failed"

# ---------------------------------------------------------------- 6. summary
log "SETUP COMPLETE"
cat <<EOF
  Server   : oMLX at http://localhost:$OMLX_PORT/v1  (admin: http://localhost:$OMLX_PORT/admin)
  Preset   : ${PRESET_KEY:-custom}
  Model    : $MODEL_REPO  ($SUMMARY, ctx ${CONTEXT_WINDOW})
  Perf     : memory guard custom (${MEMORY_CEILING_GB} GB) + Metal wired ${WIRED_LIMIT_MB} MB + KV cache on SSD ($SSD_CACHE_DIR)
             + chunked prefill -> long agent sessions without throttling
  Codex    : interactive : codex --profile $CODEX_PROFILE
             autonomous  : codex exec --profile $CODEX_PROFILE --full-auto "<task>"
  Notes    : sandbox danger-full-access (mvn/npm/gh/docker); approvals never.
             If the server was already running with old settings, this script restarts it.
EOF
