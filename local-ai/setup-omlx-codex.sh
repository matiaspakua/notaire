#!/bin/bash
# setup-omlx-codex.sh — oMLX + un modelo local (MLX) + Codex CLI, todo local en Apple Silicon
# Idempotente: re-ejecutar es seguro. Aplica ajustes de performance para agentes de código.
#   PRESET=qwen3-coder (default) | gpt-oss   — cada preset tiene su perfil de Codex propio
set -euo pipefail

LOCAL_AI_DIR="$(cd "$(dirname "$0")" && pwd)"
MODEL_INPUT="${1:-${MODEL_REPO:-${PRESET:-qwen3-coder}}}"
RESOLVE_OUT=$(python3 "$LOCAL_AI_DIR/models_config.py" resolve "$MODEL_INPUT") || exit 1
eval "$RESOLVE_OUT"

MODEL_DIR="$HOME/.omlx/models/$MODEL_REPO"
WIRED_LIMIT_MB="${WIRED_LIMIT_MB:-20480}"
MEMORY_CEILING_GB=$((WIRED_LIMIT_MB / 1024))   # techo oMLX = límite Metal
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
# conservador estrangula el prefill. KV en SSD + techo fijo = límite Metal.
# Tier "custom": los tiers safe/balanced/aggressive topan en la RAM reclamable del
# momento (~16 GB con navegador abierto) y rechazan el 30B (16.8 GB). Con techo fijo
# macOS comprime/swapea las otras apps en vez de que oMLX se niegue a cargar.
apply_perf_settings() {
  python3 "$LOCAL_AI_DIR/models_config.py" apply-omlx "$MODEL_INPUT"
  python3 "$LOCAL_AI_DIR/models_config.py" apply-opencode "$MODEL_INPUT"
}

# Metal: Apple limita la memoria GPU a ~75% de la RAM salvo que se suba iogpu.wired_limit_mb.
# oMLX usa min(techo custom, ese límite): con el default el modelo 30B no entra con contexto.
check_wired_limit() {
  local current
  current=$(sysctl -n iogpu.wired_limit_mb 2>/dev/null || echo 0)
  if [ "${current:-0}" -ge "$WIRED_LIMIT_MB" ]; then
    ok "iogpu.wired_limit_mb=$current (>= $WIRED_LIMIT_MB)"
    return 0
  fi
  warn "iogpu.wired_limit_mb=$current: Metal queda en ~75% de la RAM y $MODEL_ID no entra con ${CONTEXT_WINDOW} de contexto."
  warn "Ejecuta (requiere contraseña, se pierde al reiniciar):  sudo sysctl iogpu.wired_limit_mb=$WIRED_LIMIT_MB"
  warn "Persistente: echo 'iogpu.wired_limit_mb=$WIRED_LIMIT_MB' | sudo tee -a /etc/sysctl.conf"
  return 1
}

settings_hash() {
  cat "$HOME/.omlx/settings.json" "$HOME/.omlx/model_settings.json" 2>/dev/null | md5 -q
}

SETTINGS_CHANGED=0
if server_healthy || pgrep -f omlx-server >/dev/null 2>&1; then
  log "Aplicando settings de performance a ~/.omlx/"
else
  log "Escribiendo settings de performance en ~/.omlx/"
fi
BEFORE=$(settings_hash)
apply_perf_settings || warn "settings.json no disponible aún; se aplicará en la próxima ejecución"
AFTER=$(settings_hash)
[ "$BEFORE" != "$AFTER" ] && SETTINGS_CHANGED=1

# oMLX 0.7.0 pierde tool calls de gpt-oss y Codex rechaza las que llegan mal formadas (#1099).
# Una actualización de oMLX deshace el parche: re-ejecutar este script lo vuelve a aplicar.
if [ "$OMLX_PATCH" = "1" ]; then
  log "Parcheando oMLX para las tool calls de $MODEL_ID (local-ai/omlx/patch_omlx.py)"
  PATCH_OUT=$(python3 "$LOCAL_AI_DIR/omlx/patch_omlx.py") \
    || warn "parte del parche no aplica (¿oMLX cambió ese código?):"
  printf '%s\n' "$PATCH_OUT"
  grep -q ": applied" <<< "$PATCH_OUT" && SETTINGS_CHANGED=1
fi

log "Límite de memoria Metal"
check_wired_limit || true

restart_server() {
  log "Reiniciando servidor oMLX para aplicar los nuevos settings"
  # la app entera: matar solo omlx-server deja la app abierta sin servidor, y `open -a` no lo relanza
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
  server_healthy || die "servidor no levantó tras reinicio (ver ~/.omlx/logs y ~/.omlx-serve.log)"
  ok "servidor reiniciado con perf settings activos"
}

start_server() {
  if server_healthy; then
    if [ "$SETTINGS_CHANGED" = "1" ] || ! curl -s "$BASE_URL/models" | grep -q "\"$MODEL_ID\""; then
      restart_server
    else
      ok "servidor oMLX activo en :$OMLX_PORT"
    fi
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
      --memory-guard custom --memory-guard-gb "$MEMORY_CEILING_GB" \
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
  log "Descargando modelo $MODEL_REPO ($SUMMARY)"
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
  "model": "'"$MODEL_ID"'",
  "messages": [{"role":"user","content":"Responde solo con: LISTO"}],
  "max_tokens": 2000, "temperature": 0
}' | python3 -c "
import json,sys;r=json.load(sys.stdin)
if 'choices' not in r: sys.exit('  '+r.get('error',{}).get('message',str(r)))
print('  respuesta:',r['choices'][0]['message']['content'].strip()[:80])" \
  || die "smoke test falló (si es memoria: iogpu.wired_limit_mb y cerrar apps, ver arriba)"
ok "inferencia local funcionando"

# ---------------------------------------------------------------- 4. Codex CLI
log "Configurando Codex CLI (perfil '$CODEX_PROFILE' en ~/.codex/$CODEX_PROFILE.config.toml)"
command -v codex >/dev/null || [ -x "/Applications/ChatGPT.app/Contents/Resources/codex" ] \
  || die "Codex CLI no encontrado (npm i -g @openai/codex)"

cp "$CODEX_CONFIG" "$CODEX_CONFIG.bak-$(date +%Y%m%d-%H%M%S)" 2>/dev/null || true

python3 "$LOCAL_AI_DIR/models_config.py" apply-codex "$MODEL_INPUT"
ok "Codex configurado: codex --profile $CODEX_PROFILE  |  codex exec --profile $CODEX_PROFILE ..."
CODEX_SMOKE=$(OMLX_API_KEY=1234 codex exec --profile "$CODEX_PROFILE" --skip-git-repo-check -C "$(mktemp -d)" \
  "Reply with exactly: OK" </dev/null 2>&1) || die "codex exec falló: $(tail -3 <<< "$CODEX_SMOKE")"
if grep -q "fallback metadata" <<< "$CODEX_SMOKE"; then
  warn "Codex ignora el catálogo $CODEX_CATALOG (¿cambió su formato en esta versión de Codex?)"
else
  ok "Codex responde con la metadata del catálogo para $MODEL_ID"
fi

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
  Preset   : ${PRESET_KEY:-custom}
  Modelo   : $MODEL_REPO  ($SUMMARY, ctx ${CONTEXT_WINDOW})
  Perf     : memory guard custom (${MEMORY_CEILING_GB} GB) + Metal wired ${WIRED_LIMIT_MB} MB + KV cache en SSD ($SSD_CACHE_DIR)
             + chunked prefill -> sesiones largas de agente sin estrangulamiento
  Codex    : interactivo : codex --profile $CODEX_PROFILE
             autónomo    : codex exec --profile $CODEX_PROFILE --full-auto "<tarea>"
  Notas    : sandbox danger-full-access (mvn/npm/gh/docker); aprobaciones nunca.
             Si el servidor ya corría con settings viejos, este script lo reinicia.
EOF
