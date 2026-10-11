# Fully local AI on macOS — oMLX + Qwen3-Coder-30B-A3B / gpt-oss-20b + Codex CLI

An AI development stack that runs entirely on your Mac (no cloud, no LLM API keys):

```text
┌──────────────────────────────────────────────┐
│  MacBook Pro M5 / 24 GB                      │
│                                              │
│  Terminal → codex --profile omlx             │
│      │  (agent: reads/edits files,           │
│      │   runs commands, compiles, tests)     │
│      ▼                                       │
│  oMLX  ·  http://localhost:8000/v1           │
│      ·  OpenAI Responses-compatible API      │
│      ·  KV cache on SSD (long sessions)      │
│      ▼                                       │
│  Qwen3-Coder-30B-A3B-Instruct-4bit           │
│      MoE 30B (3B active) · ~16 GiB in RAM    │
│      32K context (256K native) · XML tools   │
└──────────────────────────────────────────────┘
```

## Requirements

- Apple Silicon (M1+) with macOS 15+, ideally 24 GB+ RAM
- Homebrew, Python 3, and the Codex CLI (`npm i -g @openai/codex`)

## Install / update (idempotent)

```bash
~/workspace/local-ai/setup-omlx-codex.sh
```

The script:

1. Installs `oMLX.app` (official DMG with precompiled kernels) if it is missing
2. Downloads the model [`mlx-community/Qwen3-Coder-30B-A3B-Instruct-4bit`](https://huggingface.co/mlx-community/Qwen3-Coder-30B-A3B-Instruct-4bit)
   (~17 GB, with `hf download`) to `~/.omlx/models/` if it is missing
3. Applies performance settings in `~/.omlx/settings.json` and restarts the server if anything changed:
   - `memory_guard_tier = custom`, 20 GB ceiling (= Metal limit; the other tiers cap at the
     RAM reclaimable at that moment, ~16 GB with a browser open, and reject the model)
   - KV cache paged to SSD at `~/.omlx/cache`
   - `chunked_prefill` on, `max_context_window = 32768`, `prefill_memory_guard` off
     (it rejected prompts over 12K tokens that do fit), `max_tokens 4096` (it cuts a tool
     call short when the call does not close), `auto_start_on_launch` on
   - in `~/.omlx/model_settings.json`: `temperature 0.3`, `top_p 0.8`, `top_k 20`,
     `repetition_penalty 1.0`, and KV cache in fp16. Qwen recommends 0.7 and 1.05, and TurboQuant
     saves memory, but all three break the model's XML tool calls (see the script comments)
4. Checks `iogpu.wired_limit_mb` (see below) and warns if it needs to be raised
5. Creates the Codex profile at `~/.codex/omlx.config.toml` (it does not touch your global config)
6. Runs an end-to-end inference smoke test

Another model: `MODEL_REPO=<org>/<repo> CONTEXT_WINDOW=<n> bash setup-omlx-codex.sh`.

### Model presets

`PRESET` selects the model and everything that depends on it. Each preset has its own Codex
profile, so installing one does not touch the other.

| `PRESET` | Model | Window | Sampling | Codex profile |
|---|---|---|---|---|
| `qwen3-coder` (default) | `mlx-community/Qwen3-Coder-30B-A3B-Instruct-4bit` (~17 GB) | 32K | temp 0.3, top_p 0.8, top_k 20 | `omlx` |
| `gpt-oss` | `mlx-community/gpt-oss-20b-MXFP4-Q8` (12.1 GB) | 64K | temp 1.0, top_p 1.0 (OpenAI), reasoning `medium` | `omlx-gptoss` |

```bash
PRESET=gpt-oss bash local-ai/setup-omlx-codex.sh
codex --profile omlx-gptoss
```

Only the default preset sets the global oMLX model and window; the others only
add their model. In the SDLC harness, `PROFILE_SPEC=omlx-gptoss` uses gpt-oss for one
phase (see `sdlc/AI-SDLC.md`).

### Harness worker with OpenCode

The SDLC harness (`local-ai/sdlc/foreman.sh`) runs the worker with Codex by default.
`AGENT=opencode` uses OpenCode with the isolated config in `local-ai/opencode/` (see
`sdlc/AI-SDLC.md`):

```bash
AGENT=opencode PROFILE=omlx-gptoss ./local-ai/sdlc/foreman.sh <issue>
```

### oMLX patch for gpt-oss

`PRESET=gpt-oss` runs `local-ai/omlx/patch_omlx.py` against the installed app
(`/Applications/oMLX.app/.../omlx/adapter/harmony.py`):

- oMLX 0.7.0 reads the `<|start|>assistant` header twice and drops every tool call that
  opens the response.
- gpt-oss learned the classic Codex tools (`shell` with argv, and `apply_patch`
  separately); Codex 0.159 only offers `exec_command` with a `cmd` string. Malformed
  calls (argv as `cmd`, a leftover `]` after a heredoc, raw regex escapes, a
  `cmd` with no closing quote, header tokens in the name; also the ones gpt-oss
  sends on the `analysis` channel) are repaired before they reach Codex. `apply_patch`
  does work inside `exec_command` (Codex intercepts it), and the gpt-oss prompt
  (`codex-local-instructions-gpt-oss.md`) says so.

If gpt-oss aims a call at a tool but none can be parsed (for example the model
cuts off right after `<|constrain|>`, about 2.6% of responses), oMLX returns an
`exec_command` with an `echo` that asks it to resend: the Codex turn continues instead of
ending empty.

Each patch replaces an exact 0.7.0 fragment: if oMLX changed that code, the patch skips it
and warns. Each run rebuilds `harmony.py` from `harmony.py.orig`. An oMLX update undoes the patch; running setup again
reapplies it. `patch_omlx.py --check` shows the state and `--restore` restores the original
(`harmony.py.orig`). Known limit: an unescaped `"` inside a long patch is
not repairable (the intent is ambiguous); the call is lost and Codex ends the turn. In 8
runs of a short TDD task, 7 finished green.

### Metal memory (required for the 30B model on 24 GB)

macOS limits GPU memory to about 75% of RAM (~17.8 GB on 24 GB) and the weights already use
~16 GiB. Without raising the limit the model leaves no room for the KV cache:

```bash
sudo sysctl iogpu.wired_limit_mb=20480                               # until reboot
echo 'iogpu.wired_limit_mb=20480' | sudo tee -a /etc/sysctl.conf     # persistent
```

With 20 GiB for Metal, about 4 GB remain for macOS, Maven, and the rest: close heavy apps
while the worker runs.

## Daily use

### Server

```bash
open -a oMLX          # start (auto-starts the server on :8000)
~/.omlx/bin/omlx stop # stop (if the shim is available)
pkill -f omlx-server  # direct alternative
```

- Admin dashboard: <http://localhost:8000/admin> (models, chat, metrics)
- Health check: `curl localhost:8000/v1/models`

### Interactive development in any project

```bash
cd ~/workspace/my-project
codex --profile omlx
```

Codex stays connected to the local model with the `danger-full-access` sandbox: it can read and write
files and run commands (mvn, npm, gh, docker, git…).

### Autonomous tasks (no interactive session)

```bash
codex exec --profile omlx --skip-git-repo-check -C ~/workspace/my-project "Implement X and leave npm test green"
```

Useful for batches of work; the full output stays in the terminal.

## Practical tips (learned on this machine)

- **Dependencies before the agent**: the Codex sandbox cannot write the global npm
  cache (`~/.npm`). Run `npm install` yourself first and tell the agent not to install
  dependencies. One-off alternative: `npm_config_cache=/tmp/npm-cache codex exec …`
- **Bounded tasks**: ask file by file or for short milestones ("schema first,
  verify the build, then the routes…") — a local model does much better with small steps.
- **`AGENTS.md` at the project root**: pin the required stack, commands, and acceptance
  criteria; Codex reads it automatically at the start.
- **Long sessions**: the profile limits the window to 32K and compacts at 2/3 (~21K): with
  16 GiB of weights that is all the KV cache that fits in 24 GB.

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `prefill_memory_exceeded` or *Prefill throttled* in the logs | Tight memory: close apps, restart the server, or split the task. Without `iogpu.wired_limit_mb` the Metal ceiling on 24 GB is ~17.8 GB: raise it (see *Metal memory*). |
| `wire_api = "chat" is no longer supported` | Current Codex requires `"responses"` (already configured that way). |
| `--profile 'x' cannot be used … [profiles.x]` | Profiles now live in `~/.codex/<profile>.config.toml` (already migrated). |
| The shim says *"Complete the oMLX first-run setup"* | Open `oMLX.app` once by hand and finish the wizard. |
| The server does not apply settings changes | Run setup again: it detects changed settings and restarts on its own. |
| gpt-oss ends the turn without editing, or `failed to parse function arguments` | The oMLX patch is missing (did the app update?): `python3 local-ai/omlx/patch_omlx.py --check`, then run `PRESET=gpt-oss bash local-ai/setup-omlx-codex.sh` again. |

## Reference files

| Path | What it is |
|---|---|
| `~/workspace/local-ai/setup-omlx-codex.sh` | Idempotent installer and configurator |
| `~/.omlx/settings.json` | Server settings (memory, cache, scheduler) |
| `~/.omlx/models/` | Downloaded models |
| `~/.omlx/cache/` | Cold KV blocks on SSD |
| `~/.omlx/logs/server.log` | Structured server log |
| `~/.codex/omlx.config.toml`, `~/.codex/omlx-gptoss.config.toml` | Codex profiles per preset (setup writes them; do not edit them by hand) |
| `local-ai/omlx/` | gpt-oss tool-call repair and the oMLX patcher |
| `local-ai/sdlc/AI-SDLC.md` | Foreman/worker process (phases, gates, guards) |
| `local-ai/AUDIT.md` | AI SDLC audit: findings, gaps, and the generic template |
