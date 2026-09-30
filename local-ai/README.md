# IA 100% local en macOS — oMLX + Qwen3-Coder-30B-A3B / gpt-oss-20b + Codex CLI

Stack de desarrollo con IA que corre íntegramente en tu Mac (sin nube, sin API keys de LLM):

```text
┌──────────────────────────────────────────────┐
│  MacBook Pro M5 / 24 GB                      │
│                                              │
│  Terminal → codex --profile omlx             │
│      │  (agente: lee/edita archivos,         │
│      │   ejecuta comandos, compila, testa)   │
│      ▼                                       │
│  oMLX  ·  http://localhost:8000/v1           │
│      ·  API OpenAI Responses compatible      │
│      ·  KV cache en SSD (sesiones largas)    │
│      ▼                                       │
│  Qwen3-Coder-30B-A3B-Instruct-4bit           │
│      MoE 30B (3B activos) · ~16 GiB en RAM   │
│      contexto 32K (256K nativo) · tools XML  │
└──────────────────────────────────────────────┘
```

## Requisitos

- Apple Silicon (M1+) con macOS 15+, ideal 24 GB+ RAM
- Homebrew, Python 3 y Codex CLI (`npm i -g @openai/codex`)

## Instalación / actualización (idempotente)

```bash
~/workspace/local-ai/setup-omlx-codex.sh
```

El script:

1. Instala `oMLX.app` (DMG oficial con kernels precompilados) si falta
2. Descarga el modelo [`mlx-community/Qwen3-Coder-30B-A3B-Instruct-4bit`](https://huggingface.co/mlx-community/Qwen3-Coder-30B-A3B-Instruct-4bit)
   (~17 GB, con `hf download`) a `~/.omlx/models/` si falta
3. Aplica ajustes de performance en `~/.omlx/settings.json` y reinicia el servidor si cambió algo:
   - `memory_guard_tier = custom`, techo 20 GB (= límite Metal; los otros tiers topan en la
     RAM reclamable del momento, ~16 GB con un navegador abierto, y rechazan el modelo)
   - KV cache paginado a SSD en `~/.omlx/cache`
   - `chunked_prefill` activado, `max_context_window = 32768`, `prefill_memory_guard` apagado
     (rechazaba prompts de >12K tokens que sí caben), `max_tokens 4096` (corta pronto una tool
     call que no cierra), `auto_start_on_launch` activado
   - en `~/.omlx/model_settings.json`: `temperature 0.3`, `top_p 0.8`, `top_k 20`,
     `repetition_penalty 1.0` y KV cache en fp16. Qwen recomienda 0.7 y 1.05, y TurboQuant
     ahorra memoria, pero los tres rompen las tool calls XML del modelo (ver comentarios del script)
4. Verifica `iogpu.wired_limit_mb` (ver abajo) y avisa si hay que subirlo
5. Crea el perfil de Codex en `~/.codex/omlx.config.toml` (no toca tu config global)
6. Smoke test de inferencia end-to-end

Otro modelo: `MODEL_REPO=<org>/<repo> CONTEXT_WINDOW=<n> bash setup-omlx-codex.sh`.

### Presets de modelo

`PRESET` elige el modelo y todo lo que depende de él; cada preset tiene su perfil de
Codex, así que instalar uno no toca el otro.

| `PRESET` | Modelo | Ventana | Sampling | Perfil Codex |
|---|---|---|---|---|
| `qwen3-coder` (default) | `mlx-community/Qwen3-Coder-30B-A3B-Instruct-4bit` (~17 GB) | 32K | temp 0.3, top_p 0.8, top_k 20 | `omlx` |
| `gpt-oss` | `mlx-community/gpt-oss-20b-MXFP4-Q8` (12.1 GB) | 64K | temp 1.0, top_p 1.0 (OpenAI), reasoning `medium` | `omlx-gptoss` |

```bash
PRESET=gpt-oss bash local-ai/setup-omlx-codex.sh
codex --profile omlx-gptoss
```

Solo el preset default fija el modelo y la ventana globales de oMLX; los demás solo
agregan su modelo. En el harness SDLC, `PROFILE_SPEC=omlx-gptoss` usa gpt-oss en una
fase concreta (ver `sdlc/AI-SDLC.md`).

### Parche de oMLX para gpt-oss

`PRESET=gpt-oss` ejecuta `local-ai/omlx/patch_omlx.py` sobre la app instalada
(`/Applications/oMLX.app/.../omlx/adapter/harmony.py`):

- oMLX 0.7.0 lee dos veces la cabecera `<|start|>assistant` y pierde toda tool call que
  abre la respuesta.
- gpt-oss aprendió las tools clásicas de Codex (`shell` con argv y `apply_patch`
  aparte); Codex 0.159 solo ofrece `exec_command` con `cmd` string. Las llamadas mal
  formadas (argv como `cmd`, `]` sobrante tras un heredoc, escapes de regex crudos,
  tokens de cabecera en el nombre) se reparan antes de llegar a Codex. `apply_patch`
  sí funciona dentro de `exec_command` (Codex lo intercepta) y el prompt de gpt-oss
  (`codex-local-instructions-gpt-oss.md`) lo indica.

Cada parche reemplaza un fragmento exacto de 0.7.0: si oMLX cambió ese código, lo salta
y avisa. Una actualización de oMLX deshace el parche; re-ejecutar el setup lo vuelve a
aplicar. `patch_omlx.py --check` muestra el estado y `--restore` restaura el original
(`harmony.py.orig`). Límite conocido: una `"` sin escapar dentro de un parche largo no
es reparable (intención ambigua); la llamada se pierde y Codex termina el turno. En 8
corridas de una tarea TDD corta, 7 terminaron en verde.

### Memoria Metal (obligatorio para el 30B en 24 GB)

macOS limita la memoria GPU a ~75 % de la RAM (~17.8 GB en 24 GB) y los pesos ya ocupan
~16 GiB. Sin subir el límite el modelo no deja sitio al KV cache:

```bash
sudo sysctl iogpu.wired_limit_mb=20480                               # hasta reiniciar
echo 'iogpu.wired_limit_mb=20480' | sudo tee -a /etc/sysctl.conf     # persistente
```

Con 20 GiB para Metal quedan ~4 GB para macOS, Maven y el resto: cierra apps pesadas
mientras corre el worker.

## Uso diario

### Servidor

```bash
open -a oMLX          # arrancar (auto-inicia el servidor en :8000)
~/.omlx/bin/omlx stop # parar (si el shim está disponible)
pkill -f omlx-server  # alternativa directa
```

- Dashboard admin: <http://localhost:8000/admin> (modelos, chat, métricas)
- Comprobar salud: `curl localhost:8000/v1/models`

### Desarrollo interactivo en cualquier proyecto

```bash
cd ~/workspace/mi-proyecto
codex --profile omlx
```

Codex queda conectado al modelo local con sandbox `danger-full-access`: puede leer/escribir
archivos y ejecutar comandos (mvn, npm, gh, docker, git…).

### Tareas autónomas (sin sesión interactiva)

```bash
codex exec --profile omlx --skip-git-repo-check -C ~/workspace/mi-proyecto "Implementa X y deja npm test en verde"
```

Útil para lotes de trabajo; la salida completa queda en la terminal.

## Consejos prácticos (aprendidos en esta máquina)

- **Dependencias antes del agente**: el sandbox de Codex no puede escribir en el caché
  global de npm (`~/.npm`). Ejecuta `npm install` tú antes y dile al agente que NO instale
  dependencias. Alternativa puntual: `npm_config_cache=/tmp/npm-cache codex exec …`
- **Tareas acotadas**: pide archivo-por-archivo o hitos cortos ("primero el esquema,
  verifica compilación, luego rutas…") — un modelo local rinde mucho mejor con pasos pequeños.
- **`AGENTS.md` en la raíz del proyecto**: fija stack obligatorio, comandos y criterios de
  aceptación; Codex lo lee automáticamente al empezar.
- **Sesiones largas**: el perfil limita la ventana a 32K y compacta a 2/3 (~21K): con
  16 GiB de pesos es todo el KV cache que cabe en 24 GB.

## Solución de problemas

| Síntoma | Causa / solución |
|---|---|
| `prefill_memory_exceeded` o *Prefill throttled* en logs | Memoria justa: cierra apps, reinicia servidor, o divide la tarea. Sin `iogpu.wired_limit_mb` el techo de Metal en 24 GB es ~17.8 GB: súbelo (ver *Memoria Metal*). |
| `wire_api = "chat" is no longer supported` | Codex moderno exige `"responses"` (ya configurado así). |
| `--profile 'x' cannot be used … [profiles.x]` | Los perfiles viven ahora en `~/.codex/<perfil>.config.toml` (ya migrado). |
| El shim dice *"Complete the oMLX first-run setup"* | Abre `oMLX.app` una vez manualmente y completa el asistente. |
| El servidor no aplica cambios de settings | Re-ejecuta el setup: detecta settings cambiados y reinicia solo. |
| gpt-oss termina el turno sin editar, o `failed to parse function arguments` | Parche de oMLX ausente (¿se actualizó la app?): `python3 local-ai/omlx/patch_omlx.py --check`, y re-ejecuta `PRESET=gpt-oss bash local-ai/setup-omlx-codex.sh`. |

## Archivos de referencia

| Ruta | Qué es |
|---|---|
| `~/workspace/local-ai/setup-omlx-codex.sh` | Instalador/configurador idempotente |
| `~/.omlx/settings.json` | Settings del servidor (memoria, caché, scheduler) |
| `~/.omlx/models/` | Modelos descargados |
| `~/.omlx/cache/` | Bloques KV fríos en SSD |
| `~/.omlx/logs/server.log` | Log estructurado del servidor |
| `~/.codex/omlx.config.toml`, `~/.codex/omlx-gptoss.config.toml` | Perfiles Codex por preset (los escribe el setup; no editarlos a mano) |
| `local-ai/omlx/` | Reparación de tool calls de gpt-oss y el parcheador de oMLX |
| `local-ai/sdlc/AI-SDLC.md` | Proceso foreman/worker (fases, gates, guards) |
| `local-ai/AUDIT.md` | Auditoría del AI SDLC: hallazgos, brechas y plantilla genérica |
