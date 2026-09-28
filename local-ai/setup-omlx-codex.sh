#!/bin/bash
# setup-omlx-codex.sh — oMLX + Qwen3.5-9B (MLX) + Codex CLI, todo local en Apple Silicon
# Idempotente: re-ejecutar es seguro. Aplica ajustes de performance para agentes de código.
set -euo pipefail

MODEL_REPO="${MODEL_REPO:-lmstudio-community/Qwen3.5-9B-MLX-4bit}"
MODEL_DIR="$HOME/.omlx/models/$MODEL_REPO"
OMLX_PORT="${OMLX_PORT:-8000}"
BASE_URL="http://localhost:$OMLX_PORT/v1"
CODEX_CONFIG="$HOME/.codex/config.toml"
SSD_CACHE_DIR="$HOME/.omlx/cache"

log() { printf '\n\033[1;36m==> %s\033[0m\n' "$*"; }
ok()  { printf '\033[32m OK %s\033[0m\n' "$*"; }
warn(){ printf '\033[33m !! %s\033[0m\n' "$*"; }
die() { printf '\033[31m ERROR: %s\033[0m\n' "$*" >&2; exit 1; }

[[ "$(uname -s)" == "Darwin" ]] || die "solo macOS"
[[ "$(uname -m)" == "arm64"   ]] || die "solo Apple Silicon"
command -v brew >/dev/null || die "Homebrew requerido: https://brew.sh"

server_healthy() { curl -sf "http://localhost:$OMLX_PORT/v1/models" >/dev/null 2>&1; }

# ---------------------------------------------------------------- 1. oMLX
install_omlx_app() {
  log "Descargando oMLX.app (release estable, kernels precompilados)"
  local url tmp mnt
  url=$(curl -s https://api.github.com/repos/jundot/omlx/releases/latest \
        | python3 -c "import json,sys;a=json.load(sys.stdin).get('assets',[]);print(next((x['browser_download_url'] for x in a if x['name'].endswith('.dmg')),''))") \
        || die "no pude consultar releases de jundot/omlx"
  [ -n "$url" ] || die "no hay DMG en el último release"
  tmp=$(mktemp -d)
  curl -L "$url" -o "$tmp/omlx.dmg" || die "descarga DMG falló"
  mnt=$(mktemp -d)
  hdiutil attach "$tmp/omlx.dmg" -nobrowse -quiet -mountpoint "$mnt" || die "montaje DMG falló"
  cp -R "$mnt"/oMLX*.app /Applications/ || { hdiutil detach "$mnt" -quiet || true; die "copia de oMLX.app falló"; }
  hdiutil detach "$mnt" -quiet || true
  rm -rf "$tmp"
  ok "oMLX.app instalada en /Applications"
}

if [ -d /Applications/oMLX.app ] || command -v omlx >/dev/null || [ -x "$HOME/.omlx/bin/omlx" ]; then
  ok "oMLX ya instalado"
else
  install_omlx_app
fi

mkdir -p "$HOME/.omlx/models" "$SSD_CACHE_DIR"

# ---------------------------------------------------------------- 2. perf settings
# Lecciones de uso con agentes (Codex): sesiones largas acumulan KV cache y el guard
# conservador estrangula el prefill. Tier agresivo + techo 20 GB + KV en SSD.
apply_perf_settings() {
  python3 - "$HOME/.omlx/settings.json" "$SSD_CACHE_DIR" <<'PYEOF'
import json, os, sys
path, ssd_dir = sys.argv[1], sys.argv[2]
defaults = {
    ("memory", "memory_guard_tier"): "aggressive",
    ("memory", "memory_guard_custom_ceiling_gb"): 17.5,
    ("cache", "enabled"): True,
    ("cache", "hot_cache_only"): False,
    ("cache", "ssd_cache_dir"): ssd_dir,
    ("scheduler", "chunked_prefill"): True,
    ("server", "sse_keepalive_mode"): "chunk",
}
try:
    cfg = json.load(open(path))
except FileNotFoundError:
    sys.exit("ERROR: ~/.omlx/settings.json no existe; abre oMLX.app una vez para generarla")
changed = []
for (section, key), val in defaults.items():
    if cfg.get(section, {}).get(key) != val:
        cfg.setdefault(section, {})[key] = val
        changed.append(f"{section}.{key}={val}")
json.dump(cfg, open(path, "w"), indent=4)
print("\n".join(f"  {c}" for c in changed) if changed else "  (ya aplicado)")
PYEOF
}

SETTINGS_CHANGED=0
if server_healthy || pgrep -f omlx-server >/dev/null 2>&1; then
  log "Aplicando settings de performance a ~/.omlx/settings.json"
else
  log "Escribiendo settings de performance en ~/.omlx/settings.json"
fi
BEFORE=$(python3 -c "
import json,os,hashlib
try: print(hashlib.md5(open(os.path.expanduser('~/.omlx/settings.json')).read().encode()).hexdigest())
except FileNotFoundError: print('none')")
apply_perf_settings || warn "settings.json no disponible aún; se aplicará en la próxima ejecución"
AFTER=$(python3 -c "
import json,os,hashlib
try: print(hashlib.md5(open(os.path.expanduser('~/.omlx/settings.json')).read().encode()).hexdigest())
except FileNotFoundError: print('none')")
[ "$BEFORE" != "$AFTER" ] && SETTINGS_CHANGED=1

restart_server() {
  log "Reiniciando servidor oMLX para aplicar los nuevos settings"
  pkill -f omlx-server 2>/dev/null || true
  sleep 3
  if [ -d /Applications/oMLX.app ]; then open -a oMLX || true; fi
  for i in $(seq 1 90); do server_healthy && break; sleep 2; done
  if ! server_healthy && command -v omlx >/dev/null && ! lsof -i ":$OMLX_PORT" >/dev/null 2>&1; then
    nohup omlx serve --model-dir "$HOME/.omlx/models" --port "$OMLX_PORT" \
      --memory-guard aggressive --memory-guard-gb 20 \
      --paged-ssd-cache-dir "$SSD_CACHE_DIR" \
      >"$HOME/.omlx-serve.log" 2>&1 &
    for i in $(seq 1 90); do server_healthy && break; sleep 2; done
  fi
  server_healthy || die "servidor no levantó tras reinicio (ver ~/.omlx/logs y ~/.omlx-serve.log)"
  ok "servidor reiniciado con perf settings activos"
}

start_server() {
  if server_healthy; then
    if [ "$SETTINGS_CHANGED" = "1" ]; then restart_server; else ok "servidor oMLX activo en :$OMLX_PORT"; fi
    return 0
  fi
  if [ -d /Applications/oMLX.app ]; then
    log "Arrancando oMLX.app (cold start puede tardar ~2 min)"
    open -a oMLX || true
  elif [ -x "$HOME/.omlx/bin/omlx" ]; then
    "$HOME/.omlx/bin/omlx" start >/dev/null 2>&1 || true
  fi
  for i in $(seq 1 90); do server_healthy && break; sleep 2; done
  if ! server_healthy && command -v omlx >/dev/null && ! lsof -i ":$OMLX_PORT" >/dev/null 2>&1; then
    warn "app no respondió; lanzando servidor CLI directo"
    nohup omlx serve --model-dir "$HOME/.omlx/models" --port "$OMLX_PORT" \
      --memory-guard aggressive --memory-guard-gb 20 \
      --paged-ssd-cache-dir "$SSD_CACHE_DIR" \
      >"$HOME/.omlx-serve.log" 2>&1 &
    for i in $(seq 1 90); do server_healthy && break; sleep 2; done
  fi
  server_healthy || die "servidor oMLX no levantó en :$OMLX_PORT (ver ~/.omlx/logs y ~/.omlx-serve.log)"
  ok "oMLX sirviendo en $BASE_URL"
}

# ---------------------------------------------------------------- 3. modelo
if [ -f "$MODEL_DIR/config.json" ] && [ -n "$(ls "$MODEL_DIR"/*.safetensors 2>/dev/null | head -1)" ]; then
  ok "modelo presente: $MODEL_REPO ($(du -sh "$MODEL_DIR" | cut -f1))"
else
  log "Descargando modelo $MODEL_REPO (~6 GB)"
  if ! command -v hf >/dev/null && ! command -v huggingface-cli >/dev/null; then
    python3 -m pip install -q -U "huggingface_hub[cli]" || die "instalando huggingface_hub"
  fi
  mkdir -p "$MODEL_DIR"
  if command -v hf >/dev/null; then HF_CMD="hf download"; else HF_CMD="huggingface-cli download"; fi
  $HF_CMD "$MODEL_REPO" --local-dir "$MODEL_DIR" || die "descarga del modelo falló"
  ok "modelo descargado en $MODEL_DIR"
fi

start_server

log "Modelos visibles en la API:"
curl -s "$BASE_URL/models" | python3 -c "import json,sys;[print('  -',m['id']) for m in json.load(sys.stdin)['data']]"

log "Smoke test chat completion (esto carga el modelo, puede tardar ~1 min)"
curl -s "$BASE_URL/chat/completions" -H 'Content-Type: application/json' -d '{
  "model": "'"$MODEL_REPO"'",
  "messages": [{"role":"user","content":"Responde solo con: LISTO"}],
  "max_tokens": 2000, "temperature": 0
}' | python3 -c "import json,sys;r=json.load(sys.stdin);print('  respuesta:',r['choices'][0]['message']['content'].strip()[:80])" \
  || die "smoke test falló"
ok "inferencia local funcionando"

# ---------------------------------------------------------------- 4. Codex CLI
log "Configurando Codex CLI (perfil 'omlx' en ~/.codex/omlx.config.toml)"
command -v codex >/dev/null || [ -x "/Applications/ChatGPT.app/Contents/Resources/codex" ] \
  || die "Codex CLI no encontrado (npm i -g @openai/codex)"

cp "$CODEX_CONFIG" "$CODEX_CONFIG.bak-$(date +%Y%m%d-%H%M%S)" 2>/dev/null || true

python3 - "$CODEX_CONFIG" "$OMLX_PORT" "$MODEL_REPO" <<'PYEOF'
import sys, re, os
path, port, model = sys.argv[1], sys.argv[2], sys.argv[3]
block_start = "# >>> omlx-local-managed >>>"
block_end = "# <<< omlx-local-managed <<<"
profile_path = os.path.join(os.path.dirname(path), "omlx.config.toml")
profile = f"""# perfil local: codex --profile omlx
# MLX-powered local inference via oMLX (Apple Silicon, 24 GB)
model = "{model}"
model_provider = "omlx"
approval_policy = "never"
# Full access: the agent must run mvn (~/.m2), npm, gh (keychain), docker (socket
# in ~/.docker) and git worktrees (.git outside -C). workspace-write blocks all of
# these. Guardrails live in the SDLC harness (local-ai/sdlc) and git hooks instead.
sandbox_mode = "danger-full-access"
model_reasoning_effort = "medium"
# M5 Pro / 24 GB: limit ctx window so Codex auto-compresses before KV cache pressure.
# Native model ctx is 256K but 65536 keeps RAM headroom for long agent sessions.
# NOTE: model_max_output_tokens is NOT valid in codex 0.156.1+; output tokens
# are governed by oMLX server sampling.max_tokens (set in ~/.omlx/settings.json).
model_context_window = 65536

[model_providers.omlx]
name = "oMLX"
base_url = "http://localhost:{port}/v1"
env_key = "OMLX_API_KEY"


# ── MCP server overrides ────────────────────────────────────────────────────
# CRITICAL: config/batchWrite (approvals, permissions, model changes) validates
# this profile file in isolation. Every [mcp_servers.*] block MUST have a valid
# transport (command or url) even when disabled — otherwise the whole batch is
# rejected and the TUI shows "Failed save approvals reviewer: config/batchWrite failed".
# Root cause: codex-rs/app-server/src/config_manager_service.rs::apply_edits ->
#   validate_config(&user_config) on the profile layer only (no base merge).
# Fix: provide full transport + startup_timeout_sec for node_repl.
# Omit computer-use (base config already has enabled=false; duplicate bare entry
# would trigger the same validation failure).

[mcp_servers.node_repl]
command = "/Applications/ChatGPT.app/Contents/Resources/cua_node/bin/node_repl"
args = []
startup_timeout_sec = 120
enabled = false

# headroom MCP: disable in local-model sessions. Headroom's built-in Copilot
# integration tries to connect to api.githubcopilot.com which logs a non-fatal
# ERROR at startup. Full transport required for standalone batchWrite validation.
[mcp_servers.headroom]
command = "headroom"
args = ["mcp", "serve"]
enabled = false
"""
open(profile_path, "w").write(profile)
print(f"  escrito {profile_path}")
try:
    original = open(path).read()
except FileNotFoundError:
    original = ""
cfg = re.sub(re.escape(block_start) + r".*?" + re.escape(block_end) + r"\n?", "", original, flags=re.S)
if cfg != original:
    open(path, "w").write(cfg.rstrip() + "\n")
    print("  limpiado bloque legado de", path)
PYEOF
ok "Codex configurado: codex --profile omlx  |  codex exec --profile omlx ..."

# ---------------------------------------------------------------- 5. GitHub token (reutiliza gh CLI)
log "Configurando GitHub token para MCP (reutiliza autenticación de gh CLI)"
if command -v gh >/dev/null && gh auth status >/dev/null 2>&1; then
  SHELL_RC="$HOME/.zshrc"
  [[ "$SHELL" == */bash ]] && SHELL_RC="$HOME/.bashrc"
  TOKEN_LINE='export GITHUB_PERSONAL_ACCESS_TOKEN=$(gh auth token)'
  if ! grep -qF "GITHUB_PERSONAL_ACCESS_TOKEN" "$SHELL_RC" 2>/dev/null; then
    echo "" >> "$SHELL_RC"
    echo "# GitHub MCP — used by Codex plugin (auto-added by setup-omlx-codex.sh)" >> "$SHELL_RC"
    echo "$TOKEN_LINE" >> "$SHELL_RC"
    ok "Agregado a $SHELL_RC — recarga el shell o ejecuta: source $SHELL_RC"
  else
    ok "GitHub token ya configurado en $SHELL_RC"
  fi
  export GITHUB_PERSONAL_ACCESS_TOKEN=$(gh auth token)
else
  warn "gh CLI no autenticado — ejecuta 'gh auth login' y vuelve a correr este script"
fi

# ---------------------------------------------------------------- 5b. oMLX API key for Codex
# oMLX acepta cualquier string como API key. Codex lee el secret via env_key
# del ENVIRONMENT del proceso, NO del archivo de config. Un `export` en .zshrc
# solo alcanza a shells interactivas nuevas; procesos lanzados desde la GUI,
# agentes o `resume` (que rehidrata sesiones) pueden heredar un env sin la var
# y fallar con "Missing environment variable: OMLX_API_KEY".
# Solucion: ademas del export en .zshrc, registrarla en launchd (launchctl
# setenv) para que TODO proceso de macOS la herede.
log "Configurando OMLX_API_KEY para Codex (shell + launchctl global)"
OMLX_API_KEY_LINE='export OMLX_API_KEY="1234"'
if ! grep -qF "OMLX_API_KEY" "$SHELL_RC" 2>/dev/null; then
  echo "" >> "$SHELL_RC"
  echo "# oMLX local model API key (auto-added by setup-omlx-codex.sh)" >> "$SHELL_RC"
  echo "$OMLX_API_KEY_LINE" >> "$SHELL_RC"
  ok "Agregado OMLX_API_KEY a $SHELL_RC"
else
  ok "OMLX_API_KEY ya configurado en $SHELL_RC"
fi
export OMLX_API_KEY="1234"
launchctl setenv OMLX_API_KEY "1234" && ok "OMLX_API_KEY registrado en launchd (global: cualquier proceso lo hereda)" || warn "launchctl setenv falló"

# ---------------------------------------------------------------- 6. resumen
log "SETUP COMPLETO"
cat <<EOF
  Servidor : oMLX en http://localhost:$OMLX_PORT/v1  (admin: http://localhost:$OMLX_PORT/admin)
  Modelo   : $MODEL_REPO  (~6 GB, ctx nativo 256K, function calling XML)
  Perf     : memory guard aggressive (20 GB) + KV cache en SSD ($SSD_CACHE_DIR)
             + chunked prefill -> sesiones largas de agente sin estrangulamiento
  Codex    : interactivo : codex --profile omlx
             autónomo    : codex exec --profile omlx --full-auto "<tarea>"
  Notas    : sandbox danger-full-access (mvn/npm/gh/docker); aprobaciones nunca.
             Si el servidor ya corría con settings viejos, este script lo reinicia.
EOF
