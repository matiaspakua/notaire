#!/usr/bin/env python3
"""models_config.py — Extensible and dynamic model configuration engine for Notaire Local AI.

Supports:
  - Downloading and using ANY HuggingFace model dynamically (by repo name or HF URL).
  - Applying custom model-specific settings (context window, sampling, reasoning, Codex profile, OpenCode integration).
  - Merging with defaults and hardware limits (Apple Silicon Metal ceiling, KV cache on SSD).

Usage:
  python3 models_config.py resolve <model_or_preset_or_url> [--env-format | --json]
  python3 models_config.py apply-all <model_or_preset_or_url>
  python3 models_config.py apply-omlx <model_or_preset_or_url>
  python3 models_config.py apply-opencode <model_or_preset_or_url>
  python3 models_config.py apply-codex <model_or_preset_or_url>
  python3 models_config.py list
"""

import json
import os
import re
import sys
from pathlib import Path
from typing import Any, Dict, Optional, Tuple
import yaml

HERE = Path(__file__).resolve().parent
MODELS_YAML = HERE / "models.yaml"


def normalize_hf_ref(ref: str) -> str:
    """Normalize a Hugging Face URL or repository reference to 'owner/model'."""
    text = (ref or "").strip()
    if not text:
        return ""
    # Strip URL schemes
    text = re.sub(r"^https?://(www\.)?huggingface\.co/", "", text)
    text = re.sub(r"^https?://(www\.)?hf\.co/", "", text)
    # Strip tree / blob / commit suffixes if pasted from browser
    text = re.sub(r"/(tree|blob|commit)/.*$", "", text)
    # Strip leading/trailing slashes
    text = text.strip("/")
    return text


def load_registry(yaml_path: Optional[Path] = None) -> Dict[str, Any]:
    """Load models registry YAML file."""
    path = yaml_path or MODELS_YAML
    if not path.is_file():
        return {"defaults": {}, "presets": {}}
    with open(path, "r", encoding="utf-8") as f:
        return yaml.safe_load(f) or {"defaults": {}, "presets": {}}


def find_preset(query: str, presets: Dict[str, Any]) -> Optional[Tuple[str, Dict[str, Any]]]:
    """Find a preset by key, repo, or alias."""
    norm_query = normalize_hf_ref(query).lower()
    raw_query = query.strip().lower()

    # Exact match on preset key
    for key, data in presets.items():
        if key.lower() == raw_query or key.lower() == norm_query:
            return key, data

    # Match on repo
    for key, data in presets.items():
        repo = normalize_hf_ref(data.get("repo", "")).lower()
        if repo and (repo == norm_query or repo == raw_query):
            return key, data

    # Match on aliases
    for key, data in presets.items():
        aliases = data.get("aliases", [])
        for alias in aliases:
            norm_alias = normalize_hf_ref(str(alias)).lower()
            if norm_alias == norm_query or str(alias).lower() == raw_query:
                return key, data

    return None


def infer_model_specs(repo: str, omlx_dir: Path) -> Dict[str, Any]:
    """Inspect local model files or infer sensible defaults from model naming/architecture."""
    model_id = repo.split("/")[-1]
    specs = {
        "context_window": 32768,
        "reasoning": "",
    }

    # Check local config.json if downloaded
    local_cfg_path = omlx_dir / "models" / repo / "config.json"
    if local_cfg_path.is_file():
        try:
            with open(local_cfg_path, "r", encoding="utf-8") as f:
                cfg = json.load(f)
            # Try to read context length
            ctx = (
                cfg.get("max_position_embeddings")
                or cfg.get("text_config", {}).get("max_position_embeddings")
                or cfg.get("seq_length")
            )
            if ctx and isinstance(ctx, int) and ctx > 0:
                # Clamp to 65536 for local memory efficiency on Apple Silicon 24GB
                specs["context_window"] = min(ctx, 65536)
        except Exception:
            pass

    # Infer reasoning mode
    lower_id = model_id.lower()
    if any(k in lower_id for k in ("r1", "qwq", "thinking", "reasoning", "gpt-oss")):
        specs["reasoning"] = "medium"

    return specs


def resolve_model_config(
    model_input: str,
    env: Optional[Dict[str, str]] = None,
    registry_path: Optional[Path] = None,
    omlx_dir: Optional[Path] = None,
) -> Dict[str, Any]:
    """Resolve full configuration for a model, whether preset or dynamic."""
    env = env or os.environ
    reg = load_registry(registry_path)
    defaults = reg.get("defaults", {})
    presets = reg.get("presets", {})

    target = model_input.strip() if model_input else "qwen3-coder"
    preset_match = find_preset(target, presets)

    omlx_path = omlx_dir or Path(env.get("HOME", "/tmp")) / ".omlx"

    if preset_match:
        preset_key, preset_data = preset_match
        config = json.loads(json.dumps(preset_data))  # deep copy
        config["preset_key"] = preset_key
    else:
        # Dynamic unlisted model
        repo = normalize_hf_ref(target)
        if not repo or "/" not in repo:
            # If user passed a single word that isn't a preset and doesn't have an org,
            # check if default preset was intended
            if target in ("default", ""):
                preset_key, preset_data = find_preset("qwen3-coder", presets)
                config = json.loads(json.dumps(preset_data))
                config["preset_key"] = preset_key
            else:
                repo = f"mlx-community/{target}"

        model_id = repo.split("/")[-1]
        inferred = infer_model_specs(repo, omlx_path)

        safe_slug = re.sub(r"[^a-zA-Z0-9]+", "-", model_id).strip("-").lower()
        profile_name = f"omlx-{safe_slug[:24]}"

        config = {
            "preset_key": "custom",
            "repo": repo,
            "name": model_id,
            "display_name": f"{model_id} (oMLX)",
            "summary": f"Dynamic HuggingFace model {repo}",
            "context_window": inferred["context_window"],
            "sampling": dict(defaults.get("sampling", {
                "temperature": 0.3,
                "top_p": 0.8,
                "top_k": 20,
                "repetition_penalty": 1.0,
                "turboquant_kv_enabled": False,
            })),
            "reasoning": inferred["reasoning"],
            "codex_profile": profile_name,
            "codex_instructions": "",
            "omlx_patch": False,
            "is_default": False,
            "opencode": {
                "tool_call": True,
                "reasoning": bool(inferred["reasoning"]),
                "limit": {
                    "context": inferred["context_window"],
                    "output": 4096,
                },
            },
        }

    # Ensure model_id is set
    config["model_id"] = config.get("name") or config["repo"].split("/")[-1]

    # Environment variable overrides
    if "MODEL_REPO" in env and env["MODEL_REPO"].strip():
        config["repo"] = normalize_hf_ref(env["MODEL_REPO"])
        config["model_id"] = config["repo"].split("/")[-1]

    if "CONTEXT_WINDOW" in env and env["CONTEXT_WINDOW"].strip():
        try:
            ctx = int(env["CONTEXT_WINDOW"])
            config["context_window"] = ctx
            config.setdefault("opencode", {}).setdefault("limit", {})["context"] = ctx
        except ValueError:
            pass

    if "CODEX_PROFILE" in env and env["CODEX_PROFILE"].strip():
        config["codex_profile"] = env["CODEX_PROFILE"].strip()

    if "REASONING" in env:
        config["reasoning"] = env["REASONING"].strip()
        config.setdefault("opencode", {})["reasoning"] = bool(config["reasoning"])

    if "TEMPERATURE" in env and env["TEMPERATURE"].strip():
        try:
            config.setdefault("sampling", {})["temperature"] = float(env["TEMPERATURE"])
        except ValueError:
            pass

    if "TOP_P" in env and env["TOP_P"].strip():
        try:
            config.setdefault("sampling", {})["top_p"] = float(env["TOP_P"])
        except ValueError:
            pass

    if "TOP_K" in env and env["TOP_K"].strip():
        try:
            config.setdefault("sampling", {})["top_k"] = int(env["TOP_K"])
        except ValueError:
            pass

    if "IS_DEFAULT" in env:
        config["is_default"] = env["IS_DEFAULT"] in ("1", "true", "True")

    # Hardware limits defaults
    config["wired_limit_mb"] = int(env.get("WIRED_LIMIT_MB", defaults.get("wired_limit_mb", 20480)))
    config["memory_ceiling_gb"] = float(env.get("MEMORY_CEILING_GB", defaults.get("memory_ceiling_gb", 20.0)))

    return config


def apply_omlx_settings(
    cfg: Dict[str, Any],
    omlx_dir: Optional[Path] = None,
    ssd_cache_dir: Optional[Path] = None,
) -> bool:
    """Apply performance & model settings to ~/.omlx/settings.json and ~/.omlx/model_settings.json."""
    home = Path(os.path.expanduser("~"))
    omlx_path = omlx_dir or (home / ".omlx")
    ssd_path = ssd_cache_dir or (omlx_path / "cache")
    omlx_path.mkdir(parents=True, exist_ok=True)
    ssd_path.mkdir(parents=True, exist_ok=True)

    settings_file = omlx_path / "settings.json"
    model_settings_file = omlx_path / "model_settings.json"

    model_id = cfg["model_id"]
    ctx = cfg["context_window"]
    is_default = bool(cfg.get("is_default", False))
    sampling = cfg.get("sampling", {})
    ceiling_gb = cfg.get("memory_ceiling_gb", 20.0)

    # 1. Update settings.json
    server_changed = False
    try:
        with open(settings_file, "r", encoding="utf-8") as f:
            server_cfg = json.load(f)
    except FileNotFoundError:
        server_cfg = {"version": "1.0"}

    def set_server(section: str, key: str, val: Any):
        nonlocal server_changed
        sec = server_cfg.setdefault(section, {})
        if sec.get(key) != val:
            sec[key] = val
            server_changed = True

    set_server("memory", "memory_guard_tier", "custom")
    set_server("memory", "memory_guard_custom_ceiling_gb", ceiling_gb)
    set_server("cache", "enabled", True)
    set_server("cache", "hot_cache_only", False)
    set_server("cache", "ssd_cache_dir", str(ssd_path))
    set_server("scheduler", "chunked_prefill", True)
    set_server("scheduler", "max_concurrent_requests", 1)
    set_server("memory", "prefill_memory_guard", False)
    set_server("server", "sse_keepalive_mode", "chunk")
    set_server("server", "auto_start_on_launch", True)
    set_server("sampling", "max_tokens", 4096)

    if is_default:
        set_server("sampling", "max_context_window", ctx)
        set_server("integrations", "codex_model", model_id)

    if server_changed:
        with open(settings_file, "w", encoding="utf-8") as f:
            json.dump(server_cfg, f, indent=4)

    # 2. Update model_settings.json
    model_changed = False
    try:
        with open(model_settings_file, "r", encoding="utf-8") as f:
            models_cfg = json.load(f)
    except FileNotFoundError:
        models_cfg = {"version": 1, "models": {}}

    m_section = models_cfg.setdefault("models", {}).setdefault(model_id, {})
    m_target = {
        "max_context_window": ctx,
        **sampling,
        "is_default": is_default,
    }
    for k, v in m_target.items():
        if m_section.get(k) != v:
            m_section[k] = v
            model_changed = True

    if model_changed:
        with open(model_settings_file, "w", encoding="utf-8") as f:
            json.dump(models_cfg, f, indent=4)

    return server_changed or model_changed


def apply_opencode_config(
    cfg: Dict[str, Any],
    opencode_json_path: Optional[Path] = None,
) -> bool:
    """Register the model in local-ai/opencode/opencode.json under provider.omlx.models."""
    target_path = opencode_json_path or (HERE / "opencode" / "opencode.json")
    if not target_path.is_file():
        return False

    with open(target_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    omlx_models = (
        data.setdefault("provider", {})
        .setdefault("omlx", {})
        .setdefault("models", {})
    )

    model_id = cfg["model_id"]
    opencode_spec = cfg.get("opencode", {})

    new_entry = {
        "name": cfg.get("display_name", f"{model_id} (oMLX)"),
        "tool_call": opencode_spec.get("tool_call", True),
        "limit": {
            "context": cfg.get("context_window", 32768),
            "output": opencode_spec.get("limit", {}).get("output", 4096),
        },
    }
    if cfg.get("reasoning") or opencode_spec.get("reasoning"):
        new_entry["reasoning"] = True

    if omlx_models.get(model_id) != new_entry:
        omlx_models[model_id] = new_entry
        with open(target_path, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)
            f.write("\n")
        return True
    return False


def apply_codex_profile(
    cfg: Dict[str, Any],
    omlx_port: int = 8000,
    codex_dir: Optional[Path] = None,
    local_ai_dir: Optional[Path] = None,
) -> Tuple[Path, Path]:
    """Generate Codex catalog and profile config in ~/.codex/."""
    home = Path(os.path.expanduser("~"))
    c_dir = codex_dir or (home / ".codex")
    c_dir.mkdir(parents=True, exist_ok=True)
    l_dir = local_ai_dir or HERE

    profile_name = cfg.get("codex_profile", "omlx")
    model_id = cfg["model_id"]
    ctx = cfg["context_window"]
    reasoning = cfg.get("reasoning", "")
    instructions_file = l_dir / "codex-local-instructions.md"

    base_prompt = ""
    if instructions_file.is_file():
        base_prompt = instructions_file.read_text(encoding="utf-8")

    extra_instructions_rel = cfg.get("codex_instructions")
    if extra_instructions_rel:
        extra_file = l_dir.parent / extra_instructions_rel if not Path(extra_instructions_rel).is_absolute() else Path(extra_instructions_rel)
        if extra_file.is_file():
            base_prompt = base_prompt.rstrip() + "\n\n" + extra_file.read_text(encoding="utf-8")

    # 1. Catalog JSON
    catalog_path = c_dir / f"{profile_name}.models.json"
    entry = {
        "slug": model_id,
        "display_name": cfg.get("display_name", f"{model_id} (oMLX)"),
        "description": "Local model served by oMLX",
        "base_instructions": base_prompt,
        "supported_reasoning_levels": (
            [{"effort": e, "description": e} for e in ("low", "medium", "high")]
            if reasoning
            else []
        ),
        "shell_type": "unified_exec",
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

    with open(catalog_path, "w", encoding="utf-8") as f:
        json.dump({"models": [entry]}, f, indent=2)

    # 2. Profile TOML
    profile_path = c_dir / f"{profile_name}.config.toml"
    effort_line = (
        f'# harmony reasoning model\nmodel_reasoning_effort = "{reasoning}"'
        if reasoning
        else '# non-thinking model: reasoning effort has no effect on it.\nmodel_reasoning_effort = "low"'
    )

    profile_content = f"""# perfil local: codex --profile {profile_name}  (managed by local-ai/models_config.py)
# MLX-powered local inference via oMLX
model = "{model_id}"
model_provider = "omlx"
approval_policy = "never"
sandbox_mode = "danger-full-access"
{effort_line}
model_context_window = {ctx}
model_auto_compact_token_limit = {ctx * 2 // 3}
model_catalog_json = "{catalog_path}"

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
base_url = "http://localhost:{omlx_port}/v1"
env_key = "OMLX_API_KEY"

[mcp_servers.node_repl]
command = "/Applications/ChatGPT.app/Contents/Resources/cua_node/bin/node_repl"
args = []
startup_timeout_sec = 120
enabled = false

[mcp_servers.headroom]
command = "headroom"
args = ["mcp", "serve"]
enabled = false
"""
    with open(profile_path, "w", encoding="utf-8") as f:
        f.write(profile_content)

    return catalog_path, profile_path


def format_env_exports(cfg: Dict[str, Any]) -> str:
    """Format resolved config as sourceable bash variable assignments."""
    lines = [
        f'MODEL_REPO="{cfg["repo"]}"',
        f'MODEL_ID="{cfg["model_id"]}"',
        f'CONTEXT_WINDOW="{cfg["context_window"]}"',
        f'CODEX_PROFILE="{cfg["codex_profile"]}"',
        f'SAMPLING_JSON=\'{json.dumps(cfg["sampling"])}\'',
        f'REASONING="{cfg.get("reasoning", "")}"',
        f'SUMMARY="{cfg.get("summary", "")}"',
        f'OMLX_PATCH="{"1" if cfg.get("omlx_patch") else "0"}"',
        f'IS_DEFAULT="{"1" if cfg.get("is_default") else "0"}"',
        f'WIRED_LIMIT_MB="{cfg.get("wired_limit_mb", 20480)}"',
        f'MEMORY_CEILING_GB="{cfg.get("memory_ceiling_gb", 20.0)}"',
        f'PRESET_KEY="{cfg.get("preset_key", "custom")}"',
    ]
    return "\n".join(lines)


def main():
    if len(sys.argv) < 2 or sys.argv[1] in ("-h", "--help"):
        print(__doc__)
        sys.exit(0)

    cmd = sys.argv[1]

    if cmd == "list":
        reg = load_registry()
        print("Configured Presets in models.yaml:")
        for k, p in reg.get("presets", {}).items():
            print(f"  - {k}: {p.get('repo')} (ctx: {p.get('context_window')}, profile: {p.get('codex_profile')})")
        return

    if len(sys.argv) < 3:
        sys.exit(f"Error: command '{cmd}' requires a model, preset or HF URL argument")

    target = sys.argv[2]
    cfg = resolve_model_config(target)

    if cmd == "resolve":
        if "--json" in sys.argv:
            print(json.dumps(cfg, indent=2))
        else:
            print(format_env_exports(cfg))

    elif cmd == "apply-omlx":
        changed = apply_omlx_settings(cfg)
        print(f"oMLX settings {'updated' if changed else 'already up to date'} for {cfg['model_id']}")

    elif cmd == "apply-opencode":
        changed = apply_opencode_config(cfg)
        print(f"OpenCode config {'updated' if changed else 'already up to date'} for {cfg['model_id']}")

    elif cmd == "apply-codex":
        cat, prof = apply_codex_profile(cfg)
        print(f"Codex catalog: {cat}")
        print(f"Codex profile: {prof}")

    elif cmd == "apply-all":
        omlx_ch = apply_omlx_settings(cfg)
        open_ch = apply_opencode_config(cfg)
        cat, prof = apply_codex_profile(cfg)
        print(f"Configuration applied for {cfg['model_id']} ({cfg['repo']}):")
        print(f"  - oMLX settings: {'modified' if omlx_ch else 'unchanged'}")
        print(f"  - OpenCode model: {'registered' if open_ch else 'already registered'}")
        print(f"  - Codex profile: {prof}")

    else:
        sys.exit(f"Unknown command: {cmd}")


if __name__ == "__main__":
    main()
