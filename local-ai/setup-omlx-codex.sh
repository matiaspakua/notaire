#!/bin/bash
# setup-omlx-codex.sh — oMLX + un modelo local (MLX) + Codex CLI, todo local en Apple Silicon
# Idempotente: re-ejecutar es seguro. Aplica ajustes de performance para agentes de código.
#   PRESET=qwen3-coder (default) | gpt-oss   — cada preset tiene su perfil de Codex propio
set -euo pipefail

LOCAL_AI_DIR="$(cd "$(dirname "$0")" && pwd)"
PRESET="${PRESET:-qwen3-coder}"
case "$PRESET" in
  qwen3-coder)
    # 30B-A3B 4-bit: 16 GiB de pesos + ~96 KB/token de KV (48 capas x 4 KV heads x 128 x fp16).
    # 32K de contexto = ~3 GiB de KV -> necesita ~20 GiB de Metal (default de Apple en 24 GB: ~17.8 GB).
    PRESET_REPO="mlx-community/Qwen3-Coder-30B-A3B-Instruct-4bit"
    PRESET_CONTEXT=32768
    # temperature 0.3, no el 0.7 de Qwen: con 0.7 el modelo erró el nombre del parámetro o
    # repitió la llamada en 5 de 20 primeras tool calls de un prompt de triage, con 0.3 en 0 de 20.
    # Sin repetition_penalty (1.0, no el 1.05 de Qwen): penaliza las etiquetas de cierre de
    # tool call, que se repiten en todo el contexto de un agente; con 1.05 el modelo escribió
    # texto en vez de `</tool_call>` en 1 de 6 llamadas, con 1.0 en 0 de 18.
    PRESET_SAMPLING='{"temperature": 0.3, "top_p": 0.8, "top_k": 20, "repetition_penalty": 1.0}'
    PRESET_REASONING=""                     # modelo sin modo thinking
    CODEX_PROFILE="omlx"
    PRESET_INSTRUCTIONS=""
    PRESET_OMLX_PATCH=0
    PRESET_IS_DEFAULT=1
    PRESET_SUMMARY="MoE 30B / 3B activos, ~17 GB, tools XML"
    ;;
  gpt-oss)
    # MXFP4 original de OpenAI en los expertos + 8 bits en atención: 12.1 GB. El KV es chico
    # (24 capas, la mitad con ventana deslizante de 128), así que 64K de contexto caben.
    PRESET_REPO="mlx-community/gpt-oss-20b-MXFP4-Q8"
    PRESET_CONTEXT=65536
    # los valores que recomienda OpenAI; 0.6/0.95 no dio más tool calls limpias (14/14 vs 17/18)
    PRESET_SAMPLING='{"temperature": 1.0, "top_p": 1.0, "top_k": 0, "repetition_penalty": 1.0}'
    PRESET_REASONING="medium"               # harmony: low | medium | high
    CODEX_PROFILE="omlx-gptoss"
    PRESET_INSTRUCTIONS="$LOCAL_AI_DIR/codex-local-instructions-gpt-oss.md"
    PRESET_OMLX_PATCH=1                     # omlx/patch_omlx.py: tool calls de gpt-oss (#1099)
    PRESET_IS_DEFAULT=0
    PRESET_SUMMARY="MoE 21B / 3.6B activos, ~12 GB, harmony"
    ;;
  *) printf '\033[31m ERROR: PRESET=%s desconocido (qwen3-coder | gpt-oss)\033[0m\n' "$PRESET" >&2; exit 1 ;;
esac

MODEL_REPO="${MODEL_REPO:-$PRESET_REPO}"
MODEL_DIR="$HOME/.omlx/models/$MODEL_REPO"
MODEL_ID="$(basename "$MODEL_REPO")"   # oMLX expone el modelo por nombre de directorio
CONTEXT_WINDOW="${CONTEXT_WINDOW:-$PRESET_CONTEXT}"
WIRED_LIMIT_MB="${WIRED_LIMIT_MB:-20480}"
MEMORY_CEILING_GB=$((WIRED_LIMIT_MB / 1024))   # techo oMLX = límite Metal
OMLX_PORT="${OMLX_PORT:-8000}"
BASE_URL="http://localhost:$OMLX_PORT/v1"
CODEX_CONFIG="$HOME/.codex/config.toml"
# Codex solo conoce los modelos de OpenAI: sin entrada propia en el catálogo usa metadata
# genérica (prompt GPT de ~5K tokens, apply_patch, sin límites de contexto) y avisa.
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
  python3 - "$HOME/.omlx" "$SSD_CACHE_DIR" "$MODEL_ID" "$CONTEXT_WINDOW" "$MEMORY_CEILING_GB" \
    "$PRESET_SAMPLING" "$PRESET_IS_DEFAULT" <<'PYEOF'
import json, os, sys
omlx_dir, ssd_dir, model_id, ctx = sys.argv[1], sys.argv[2], sys.argv[3], int(sys.argv[4])
ceiling_gb, sampling, is_default = float(sys.argv[5]), json.loads(sys.argv[6]), sys.argv[7] == "1"
defaults = {
    ("memory", "memory_guard_tier"): "custom",
    ("memory", "memory_guard_custom_ceiling_gb"): ceiling_gb,
    ("cache", "enabled"): True,
    ("cache", "hot_cache_only"): False,
    ("cache", "ssd_cache_dir"): ssd_dir,
    ("scheduler", "chunked_prefill"): True,
    # fp16 KV leaves room for one full-window sequence: a second concurrent request ran
    # the GPU out of memory (#1049). Codex sends one request at a time anyway.
    ("scheduler", "max_concurrent_requests"): 1,
    # The prefill guard samples memory mid-chunk (a ~1.1 GB MoE transient) and adds a
    # transient again, so it rejected every prompt over ~12K tokens that fits: measured
    # 27.5K tokens with fp16 KV stay under the Metal limit.
    ("memory", "prefill_memory_guard"): False,
    ("server", "sse_keepalive_mode"): "chunk",
    # A fresh app launch otherwise leaves the server stopped and the worker's requests refused.
    ("server", "auto_start_on_launch"): True,
    # A tool call that misses its closing </tool_call> runs on inside the envelope, where oMLX
    # streams nothing, until this cap: at ~35 tok/s 4096 wastes ~2 min before Codex retries.
    # A heredoc write of a whole test or spec file still fits.
    ("sampling", "max_tokens"): 4096,
}
# the server-wide model and window belong to the default preset; another preset only adds its model
if is_default:
    defaults[("sampling", "max_context_window")] = ctx
    defaults[("integrations", "codex_model")] = model_id
# sampling por modelo (ver el preset): cada modelo conserva la suya
model_defaults = {
    "max_context_window": ctx,
    **sampling,
    # TurboQuant KV rompe las tool calls XML de Qwen3-Coder: limpias 9 de 20 con 4 bits y
    # 9 de 12 con 8 bits, frente a 20 de 20 en fp16. Una ventana de 32K en fp16 son ~3 GB y
    # cabe junto a los 16 GB de pesos bajo el límite Metal de 20 GB.
    "turboquant_kv_enabled": False,
    "is_default": is_default,
}

def apply(path, section_of, wanted, missing_msg):
    try:
        cfg = json.load(open(path))
    except FileNotFoundError:
        if missing_msg:
            sys.exit(missing_msg)
        cfg = {"version": 1, "models": {}}
    changed = []
    for key, val in wanted.items():
        section = section_of(cfg, key)
        name = key[1] if isinstance(key, tuple) else key
        if section.get(name) != val:
            section[name] = val
            changed.append(f"{os.path.basename(path)}: {'.'.join(key) if isinstance(key, tuple) else model_id + '.' + key}={val}")
    # only on a real change: oMLX saves these files compact, so a rewrite alone restarted the server
    if changed:
        json.dump(cfg, open(path, "w"), indent=4)
    return changed

changed = apply(os.path.join(omlx_dir, "settings.json"),
                lambda c, k: c.setdefault(k[0], {}), defaults,
                "ERROR: ~/.omlx/settings.json no existe; abre oMLX.app una vez para generarla")
changed += apply(os.path.join(omlx_dir, "model_settings.json"),
                 lambda c, k: c.setdefault("models", {}).setdefault(model_id, {}), model_defaults, None)
print("\n".join(f"  {c}" for c in changed) if changed else "  (ya aplicado)")
PYEOF
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
if [ "$PRESET_OMLX_PATCH" = "1" ]; then
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
  log "Descargando modelo $MODEL_REPO ($PRESET_SUMMARY)"
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

[ -f "$CODEX_INSTRUCTIONS" ] || die "falta $CODEX_INSTRUCTIONS"
[ -z "$PRESET_INSTRUCTIONS" ] || [ -f "$PRESET_INSTRUCTIONS" ] || die "falta $PRESET_INSTRUCTIONS"
python3 - "$CODEX_CATALOG" "$CODEX_INSTRUCTIONS" "$MODEL_ID" "$CONTEXT_WINDOW" "$PRESET_INSTRUCTIONS" \
  "$PRESET_REASONING" <<'PYEOF'
import json, sys
path, instructions, model, ctx = sys.argv[1], sys.argv[2], sys.argv[3], int(sys.argv[4])
extra, reasoning = sys.argv[5], sys.argv[6]
prompt = open(instructions).read()
if extra:
    prompt = prompt.rstrip() + "\n\n" + open(extra).read()
entry = {
    "slug": model,
    "display_name": f"{model} (oMLX)",
    "description": "Local model served by oMLX",
    # replaces Codex's GPT-tuned prompt: short, so the 32K window goes to the task
    "base_instructions": prompt,
    "supported_reasoning_levels": [{"effort": e, "description": e} for e in ("low", "medium", "high")]
                                  if reasoning else [],
    "shell_type": "unified_exec",
    # no apply_patch_tool_type: apply_patch fails with the local model, so it is not offered.
    # no tool_mode: "direct" makes the model print tool calls as text instead of running them.
    "visibility": "list",
    "supported_in_api": True,
    "priority": 1,
    "support_verbosity": False,
    "truncation_policy": {"mode": "tokens", "limit": 4000},
    "context_window": ctx,
    "max_context_window": ctx,
    "effective_context_window_percent": 90,
    "experimental_supported_tools": [],
    "input_modalities": ["text"],
}
if reasoning:
    entry["default_reasoning_level"] = reasoning
json.dump({"models": [entry]}, open(path, "w"), indent=2)
print(f"  escrito {path}")
PYEOF

python3 - "$CODEX_CONFIG" "$OMLX_PORT" "$MODEL_ID" "$CONTEXT_WINDOW" "$CODEX_CATALOG" "$CODEX_PROFILE" \
  "$PRESET_REASONING" <<'PYEOF'
import sys, re, os
path, port, model, ctx, catalog = sys.argv[1], sys.argv[2], sys.argv[3], int(sys.argv[4]), sys.argv[5]
profile_name, reasoning = sys.argv[6], sys.argv[7]
effort = (f'# harmony reasoning model: OpenAI recommends medium for agentic use.\nmodel_reasoning_effort = "{reasoning}"'
          if reasoning else '# non-thinking model: reasoning effort has no effect on it.\nmodel_reasoning_effort = "low"')
block_start = "# >>> omlx-local-managed >>>"
block_end = "# <<< omlx-local-managed <<<"
profile_path = os.path.join(os.path.dirname(path), f"{profile_name}.config.toml")
profile = f"""# perfil local: codex --profile {profile_name}  (written by setup-omlx-codex.sh)
# MLX-powered local inference via oMLX (Apple Silicon, 24 GB, iogpu.wired_limit_mb raised)
model = "{model}"
model_provider = "omlx"
approval_policy = "never"
# Full access: the agent must run mvn (~/.m2), npm, gh (keychain), docker (socket
# in ~/.docker) and git worktrees (.git outside -C). workspace-write blocks all of
# these. Guardrails live in the SDLC harness (local-ai/sdlc) and git hooks instead.
sandbox_mode = "danger-full-access"
{effort}
# Must match the model's oMLX max_context_window (M5 Pro / 24 GB: weights + fp16 KV
# under the 20 GB Metal limit — 32K for Qwen3-Coder-30B, 64K for gpt-oss-20b).
# Compact at 2/3: one turn adds up to max_tokens (4K) of output plus a 4K tool output,
# and at 75% (#1049) the next request reached 33.9K and oMLX refused it.
# NOTE: model_max_output_tokens is NOT valid in codex 0.156.1+; output tokens
# are governed by oMLX server sampling.max_tokens (set in ~/.omlx/settings.json).
model_context_window = {ctx}
model_auto_compact_token_limit = {ctx * 2 // 3}
# Model metadata for the local model (written by setup-omlx-codex.sh).
model_catalog_json = "{catalog}"

# The global config's plugins, hooks and skills cost ~5.5K of the 32K window on every
# run (memory handoffs, skill lists, a Copilot MCP that fails auth), and the global rtk
# hook rewrites the model's shell commands. A local model gets only the shell.
[features]
plugins = false
hooks = false
apps = false
multi_agent = false
skill_search = false
tool_suggest = false
goals = false
browser_use = false
computer_use = false
image_generation = false

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
  Preset   : $PRESET
  Modelo   : $MODEL_REPO  ($PRESET_SUMMARY, ctx ${CONTEXT_WINDOW})
  Perf     : memory guard custom (${MEMORY_CEILING_GB} GB) + Metal wired ${WIRED_LIMIT_MB} MB + KV cache en SSD ($SSD_CACHE_DIR)
             + chunked prefill -> sesiones largas de agente sin estrangulamiento
  Codex    : interactivo : codex --profile $CODEX_PROFILE
             autónomo    : codex exec --profile $CODEX_PROFILE --full-auto "<tarea>"
  Notas    : sandbox danger-full-access (mvn/npm/gh/docker); aprobaciones nunca.
             Si el servidor ya corría con settings viejos, este script lo reinicia.
EOF
