# IA 100% local en macOS — oMLX + Qwen3.5-9B + Codex CLI

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
│  Qwen3.5-9B-MLX-4bit  (~5.6 GB en RAM)       │
│      contexto nativo 256K · tool calling     │
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
2. Descarga el modelo `lmstudio-community/Qwen3.5-9B-MLX-4bit` a `~/.omlx/models/` si falta
3. Aplica ajustes de performance en `~/.omlx/settings.json` y reinicia el servidor si cambió algo:
   - `memory_guard_tier = aggressive`, techo 17.5 GB
   - KV cache paginado a SSD en `~/.omlx/cache`
   - `chunked_prefill` activado
4. Crea el perfil de Codex en `~/.codex/omlx.config.toml` (no toca tu config global)
5. Smoke test de inferencia end-to-end

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

Codex queda conectado al modelo local con sandbox `workspace-write`: puede leer/escribir
archivos del proyecto y ejecutar comandos (compilar, testear, git…).

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
  verifica compilación, luego rutas…") — un modelo 9B rinde mucho mejor con pasos pequeños.
- **`AGENTS.md` en la raíz del proyecto**: fija stack obligatorio, comandos y criterios de
  aceptación; Codex lo lee automáticamente al empezar.
- **Sesiones largas**: el perfil limita la ventana a 64K para que Codex auto-comprima antes;
  pasados ~100K tokens acumulados un modelo local en 24 GB empieza a sufrir.

## Solución de problemas

| Síntoma | Causa / solución |
|---|---|
| `prefill_memory_exceeded` o *Prefill throttled* en logs | Memoria justa: cierra apps, reinicia servidor, o divide la tarea. El techo físico de Metal en 24 GB es ~17.8 GB. |
| `wire_api = "chat" is no longer supported` | Codex moderno exige `"responses"` (ya configurado así). |
| `--profile 'x' cannot be used … [profiles.x]` | Los perfiles viven ahora en `~/.codex/<perfil>.config.toml` (ya migrado). |
| El shim dice *"Complete the oMLX first-run setup"* | Abre `oMLX.app` una vez manualmente y completa el asistente. |
| El servidor no aplica cambios de settings | Re-ejecuta el setup: detecta settings cambiados y reinicia solo. |

## Archivos de referencia

| Ruta | Qué es |
|---|---|
| `~/workspace/local-ai/setup-omlx-codex.sh` | Instalador/configurador idempotente |
| `~/.omlx/settings.json` | Settings del servidor (memoria, caché, scheduler) |
| `~/.omlx/models/` | Modelos descargados |
| `~/.omlx/cache/` | Bloques KV fríos en SSD |
| `~/.omlx/logs/server.log` | Log estructurado del servidor |
| `~/.codex/omlx.config.toml` | Perfil Codex para el modelo local |
| `local-ai/sdlc/AI-SDLC.md` | Proceso foreman/worker (fases, gates, guards) |
| `local-ai/AUDIT.md` | Auditoría del AI SDLC: hallazgos, brechas y plantilla genérica |
