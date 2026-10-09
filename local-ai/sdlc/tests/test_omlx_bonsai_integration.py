"""Integration tests for Ternary-Bonsai-2-27B-mlx-2bit on oMLX.

These tests require the oMLX server to be running on localhost:8000 with the
Ternary-Bonsai-2-27B-mlx-2bit model loaded.  They are skipped automatically
when the server is unreachable so the offline test suite stays green.

Coverage:
  ServerReachabilityTest  — /v1/models endpoint responds, model is registered
  ModelRegistrationTest   — API fields, context window, max_model_len match models.yaml
  ChatCompletionTest      — echo reply, arithmetic, code generation, multi-turn context
  ToolCallTest            — single-tool selection, multi-tool selection, argument shape
  HarnessConfigTest       — models_config.py resolve/apply wiring for ternary-bonsai
"""

import json
import os
import sys
import tempfile
import unittest
import urllib.error
import urllib.request
from pathlib import Path

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------
TESTS_DIR = os.path.dirname(os.path.abspath(__file__))
LOCAL_AI_DIR = os.path.abspath(os.path.join(TESTS_DIR, "..", ".."))
sys.path.insert(0, LOCAL_AI_DIR)
import models_config  # noqa: E402

OMLX_BASE = os.environ.get("OMLX_BASE_URL", "http://localhost:8000/v1")
MODEL_ID = "Ternary-Bonsai-2-27B-mlx-2bit"
API_KEY = os.environ.get("OMLX_API_KEY", "1234")
# Generous timeout: first call after context switch can take a few seconds.
REQUEST_TIMEOUT = int(os.environ.get("OMLX_TEST_TIMEOUT", "120"))


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
def _server_available() -> bool:
    """Return True if the oMLX /v1/models endpoint is reachable."""
    try:
        req = urllib.request.Request(
            f"{OMLX_BASE}/models",
            headers={"Authorization": f"Bearer {API_KEY}"},
        )
        with urllib.request.urlopen(req, timeout=5):
            return True
    except Exception:
        return False


def _model_loaded() -> bool:
    """Return True if MODEL_ID appears in the /v1/models list."""
    try:
        req = urllib.request.Request(
            f"{OMLX_BASE}/models",
            headers={"Authorization": f"Bearer {API_KEY}"},
        )
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.load(resp)
        ids = [m["id"] for m in data.get("data", [])]
        return MODEL_ID in ids
    except Exception:
        return False


_SERVER_UP = _server_available()
_MODEL_UP = _model_loaded()

requires_server = unittest.skipUnless(_SERVER_UP, "oMLX server not reachable at localhost:8000")
requires_model = unittest.skipUnless(
    _MODEL_UP, f"Model {MODEL_ID} not loaded in oMLX (server up={_SERVER_UP})"
)


def _chat(messages, *, tools=None, tool_choice=None, max_tokens=128, temperature=0.0):
    """POST /v1/chat/completions and return the parsed JSON response."""
    payload = {
        "model": MODEL_ID,
        "messages": messages,
        "max_tokens": max_tokens,
        "temperature": temperature,
    }
    if tools is not None:
        payload["tools"] = tools
    if tool_choice is not None:
        payload["tool_choice"] = tool_choice

    data = json.dumps(payload).encode()
    req = urllib.request.Request(
        f"{OMLX_BASE}/chat/completions",
        data=data,
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {API_KEY}",
        },
    )
    with urllib.request.urlopen(req, timeout=REQUEST_TIMEOUT) as resp:
        return json.load(resp)


# ---------------------------------------------------------------------------
# 1. Server Reachability
# ---------------------------------------------------------------------------
@requires_server
class ServerReachabilityTest(unittest.TestCase):
    """oMLX server must be up and serving the OpenAI-compatible models list."""

    def test_models_endpoint_returns_200(self):
        req = urllib.request.Request(
            f"{OMLX_BASE}/models",
            headers={"Authorization": f"Bearer {API_KEY}"},
        )
        with urllib.request.urlopen(req, timeout=10) as resp:
            self.assertEqual(resp.status, 200)

    def test_models_response_has_data_list(self):
        req = urllib.request.Request(
            f"{OMLX_BASE}/models",
            headers={"Authorization": f"Bearer {API_KEY}"},
        )
        with urllib.request.urlopen(req, timeout=10) as resp:
            body = json.load(resp)
        self.assertIn("data", body)
        self.assertIsInstance(body["data"], list)
        self.assertGreater(len(body["data"]), 0)

    def test_bonsai_model_present_in_models_list(self):
        req = urllib.request.Request(
            f"{OMLX_BASE}/models",
            headers={"Authorization": f"Bearer {API_KEY}"},
        )
        with urllib.request.urlopen(req, timeout=10) as resp:
            body = json.load(resp)
        ids = [m["id"] for m in body["data"]]
        self.assertIn(MODEL_ID, ids, f"Expected {MODEL_ID} in {ids}")


# ---------------------------------------------------------------------------
# 2. Model Registration
# ---------------------------------------------------------------------------
@requires_server
class ModelRegistrationTest(unittest.TestCase):
    """API model entry must match the models.yaml preset exactly."""

    @classmethod
    def setUpClass(cls):
        req = urllib.request.Request(
            f"{OMLX_BASE}/models",
            headers={"Authorization": f"Bearer {API_KEY}"},
        )
        with urllib.request.urlopen(req, timeout=10) as resp:
            body = json.load(resp)
        cls.entry = next(
            (m for m in body["data"] if m["id"] == MODEL_ID), None
        )

    def test_entry_exists(self):
        self.assertIsNotNone(
            self.entry, f"{MODEL_ID} not found in /v1/models"
        )

    def test_object_field_is_model(self):
        self.assertEqual(self.entry["object"], "model")

    def test_max_model_len_matches_preset_context_window(self):
        cfg = models_config.resolve_model_config("ternary-bonsai")
        expected_ctx = cfg["context_window"]  # 65536 per models.yaml
        self.assertEqual(
            self.entry["max_model_len"],
            expected_ctx,
            f"max_model_len={self.entry['max_model_len']} != preset context_window={expected_ctx}",
        )

    def test_owned_by_omlx(self):
        self.assertEqual(self.entry["owned_by"], "omlx")


# ---------------------------------------------------------------------------
# 3. Chat Completion
# ---------------------------------------------------------------------------
@requires_model
class ChatCompletionTest(unittest.TestCase):
    """Basic chat completions: echo, arithmetic, code generation, multi-turn."""

    def test_echo_reply_pong(self):
        resp = _chat([{"role": "user", "content": "Reply with exactly: PONG"}], max_tokens=20)
        content = resp["choices"][0]["message"]["content"].strip()
        self.assertIn("PONG", content, f"Expected PONG in {content!r}")

    def test_arithmetic_7_times_8(self):
        # max_tokens=60: Bonsai may emit a reasoning preamble ("We need to answer…")
        # before the actual answer on the first call after a context switch.
        # The answer "56" must appear somewhere in the output.
        resp = _chat(
            [{"role": "user", "content": "What is 7 * 8? Reply with only the number."}],
            max_tokens=60,
        )
        content = resp["choices"][0]["message"]["content"].strip()
        self.assertIn("56", content, f"Expected 56 in {content!r}")

    def test_code_generation_simple_function(self):
        resp = _chat(
            [{"role": "user", "content": "Write a Python function `add(a, b)` that returns a+b. No explanation."}],
            max_tokens=100,
        )
        content = resp["choices"][0]["message"]["content"]
        self.assertIn("def add", content, "Expected function definition in response")
        self.assertIn("return", content, "Expected return statement in response")

    def test_multiturn_remembers_name(self):
        # max_tokens=80: enough to clear Bonsai's reasoning preamble and reach the answer.
        resp = _chat(
            [
                {"role": "user", "content": "My name is Alice."},
                {"role": "assistant", "content": "Hello Alice! Nice to meet you."},
                {"role": "user", "content": "What is my name? Reply with just the name."},
            ],
            max_tokens=80,
        )
        content = resp["choices"][0]["message"]["content"].strip()
        self.assertIn("Alice", content, f"Expected Alice in multi-turn reply, got {content!r}")

    def test_finish_reason_stop_on_short_answer(self):
        # With max_tokens=80 Bonsai has enough room to clear its reasoning preamble
        # and end with finish_reason=stop on a trivially short answer.
        resp = _chat(
            [{"role": "user", "content": "What is 2 + 2? Reply with only the number."}],
            max_tokens=80,
        )
        finish = resp["choices"][0]["finish_reason"]
        self.assertEqual(finish, "stop", f"Expected finish_reason=stop, got {finish!r}")

    def test_response_has_usage_stats(self):
        resp = _chat([{"role": "user", "content": "Say hi."}], max_tokens=10)
        usage = resp.get("usage", {})
        self.assertIn("prompt_tokens", usage)
        self.assertIn("completion_tokens", usage)
        self.assertGreater(usage["prompt_tokens"], 0)
        self.assertGreater(usage["completion_tokens"], 0)

    def test_generation_speed_above_minimum_tps(self):
        """Ternary 2-bit 27B on Apple Silicon should sustain > 10 tok/s."""
        resp = _chat(
            [{"role": "user", "content": "Count from 1 to 10, one number per line."}],
            max_tokens=60,
        )
        usage = resp.get("usage", {})
        tps = usage.get("generation_tokens_per_second", 0)
        self.assertGreater(
            tps, 10,
            f"generation_tokens_per_second={tps} is below 10 tok/s minimum",
        )


# ---------------------------------------------------------------------------
# 4. Tool Calls
# ---------------------------------------------------------------------------
@requires_model
class ToolCallTest(unittest.TestCase):
    """Tool-call protocol: selection, argument shape, finish_reason."""

    _WEATHER_TOOL = {
        "type": "function",
        "function": {
            "name": "get_weather",
            "description": "Get current temperature for a city.",
            "parameters": {
                "type": "object",
                "properties": {"city": {"type": "string", "description": "City name"}},
                "required": ["city"],
            },
        },
    }

    _READ_FILE_TOOL = {
        "type": "function",
        "function": {
            "name": "read_file",
            "description": "Read the content of a file.",
            "parameters": {
                "type": "object",
                "properties": {"path": {"type": "string"}},
                "required": ["path"],
            },
        },
    }

    _RUN_CMD_TOOL = {
        "type": "function",
        "function": {
            "name": "run_command",
            "description": "Execute a shell command.",
            "parameters": {
                "type": "object",
                "properties": {"cmd": {"type": "string"}},
                "required": ["cmd"],
            },
        },
    }

    def test_single_tool_called_for_weather_query(self):
        resp = _chat(
            [{"role": "user", "content": "What's the weather in Paris?"}],
            tools=[self._WEATHER_TOOL],
            tool_choice="auto",
            max_tokens=128,
        )
        choice = resp["choices"][0]
        self.assertEqual(choice["finish_reason"], "tool_calls")
        tool_calls = choice["message"]["tool_calls"]
        self.assertEqual(len(tool_calls), 1)
        self.assertEqual(tool_calls[0]["function"]["name"], "get_weather")

    def test_tool_arguments_are_valid_json(self):
        resp = _chat(
            [{"role": "user", "content": "What's the weather in Tokyo?"}],
            tools=[self._WEATHER_TOOL],
            tool_choice="auto",
            max_tokens=128,
        )
        tc = resp["choices"][0]["message"]["tool_calls"][0]
        args = json.loads(tc["function"]["arguments"])
        self.assertIsInstance(args, dict, "Tool arguments must be a JSON object")

    def test_tool_arguments_contain_city(self):
        resp = _chat(
            [{"role": "user", "content": "What's the weather in Berlin?"}],
            tools=[self._WEATHER_TOOL],
            tool_choice="auto",
            max_tokens=128,
        )
        tc = resp["choices"][0]["message"]["tool_calls"][0]
        args = json.loads(tc["function"]["arguments"])
        self.assertIn("city", args, f"Expected 'city' in args, got {args}")
        self.assertIn("Berlin", args["city"])

    def test_multi_tool_selects_correct_tool(self):
        resp = _chat(
            [{"role": "user", "content": "Read the file at /tmp/notes.txt"}],
            tools=[self._READ_FILE_TOOL, self._RUN_CMD_TOOL],
            tool_choice="auto",
            max_tokens=128,
        )
        choice = resp["choices"][0]
        self.assertEqual(choice["finish_reason"], "tool_calls")
        fn_name = choice["message"]["tool_calls"][0]["function"]["name"]
        self.assertEqual(fn_name, "read_file", f"Expected read_file, got {fn_name!r}")

    def test_tool_call_id_is_present_and_non_empty(self):
        resp = _chat(
            [{"role": "user", "content": "What's the weather in London?"}],
            tools=[self._WEATHER_TOOL],
            tool_choice="auto",
            max_tokens=128,
        )
        tc = resp["choices"][0]["message"]["tool_calls"][0]
        self.assertIn("id", tc)
        self.assertTrue(tc["id"], "tool_call id must not be empty")

    def test_tool_call_type_is_function(self):
        resp = _chat(
            [{"role": "user", "content": "What's the weather in Madrid?"}],
            tools=[self._WEATHER_TOOL],
            tool_choice="auto",
            max_tokens=128,
        )
        tc = resp["choices"][0]["message"]["tool_calls"][0]
        self.assertEqual(tc["type"], "function")


# ---------------------------------------------------------------------------
# 5. Harness Config Wiring
# ---------------------------------------------------------------------------
class HarnessConfigTest(unittest.TestCase):
    """models_config.py must correctly resolve and apply ternary-bonsai settings.
    These run offline (no server required).
    """

    def test_resolve_by_preset_key(self):
        cfg = models_config.resolve_model_config("ternary-bonsai")
        self.assertEqual(cfg["preset_key"], "ternary-bonsai")
        self.assertEqual(cfg["model_id"], MODEL_ID)

    def test_resolve_by_short_alias(self):
        cfg = models_config.resolve_model_config("bonsai")
        self.assertEqual(cfg["preset_key"], "ternary-bonsai")
        self.assertEqual(cfg["model_id"], MODEL_ID)

    def test_resolve_by_hf_url(self):
        cfg = models_config.resolve_model_config(
            "https://huggingface.co/prism-ml/Ternary-Bonsai-2-27B-mlx-2bit"
        )
        self.assertEqual(cfg["preset_key"], "ternary-bonsai")
        self.assertEqual(cfg["model_id"], MODEL_ID)

    def test_context_window_is_65536(self):
        cfg = models_config.resolve_model_config("ternary-bonsai")
        self.assertEqual(cfg["context_window"], 65536)

    def test_codex_profile_is_omlx_bonsai(self):
        cfg = models_config.resolve_model_config("ternary-bonsai")
        self.assertEqual(cfg["codex_profile"], "omlx-bonsai")

    def test_tool_call_enabled_in_opencode(self):
        cfg = models_config.resolve_model_config("ternary-bonsai")
        self.assertTrue(cfg["opencode"]["tool_call"])

    def test_reasoning_is_disabled(self):
        # Bonsai is not a harmony reasoning model; reasoning should be empty.
        cfg = models_config.resolve_model_config("ternary-bonsai")
        self.assertEqual(cfg["reasoning"], "")
        self.assertFalse(cfg["opencode"]["reasoning"])

    def test_omlx_patch_not_required(self):
        cfg = models_config.resolve_model_config("ternary-bonsai")
        self.assertFalse(cfg.get("omlx_patch", False))

    def test_apply_opencode_config_registers_model(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            ocp = Path(tmpdir) / "opencode.json"
            initial = {
                "$schema": "https://opencode.ai/config.json",
                "provider": {"omlx": {"models": {}}},
            }
            ocp.write_text(json.dumps(initial), encoding="utf-8")

            cfg = models_config.resolve_model_config("ternary-bonsai")
            changed = models_config.apply_opencode_config(cfg, opencode_json_path=ocp)
            self.assertTrue(changed)

            saved = json.loads(ocp.read_text(encoding="utf-8"))
            models = saved["provider"]["omlx"]["models"]
            self.assertIn(MODEL_ID, models)
            entry = models[MODEL_ID]
            self.assertTrue(entry["tool_call"])
            self.assertEqual(entry["limit"]["context"], 65536)
            self.assertEqual(entry["limit"]["output"], 4096)

    def test_apply_omlx_settings_writes_model_entry(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            omlx_dir = Path(tmpdir) / ".omlx"
            ssd_dir = omlx_dir / "cache"
            cfg = models_config.resolve_model_config("ternary-bonsai")
            changed = models_config.apply_omlx_settings(cfg, omlx_dir=omlx_dir, ssd_cache_dir=ssd_dir)
            self.assertTrue(changed)

            model_settings = json.loads(
                (omlx_dir / "model_settings.json").read_text(encoding="utf-8")
            )
            self.assertIn(MODEL_ID, model_settings["models"])
            ms = model_settings["models"][MODEL_ID]
            self.assertEqual(ms["max_context_window"], 65536)
            self.assertAlmostEqual(ms["temperature"], 0.7, places=5)

    def test_apply_codex_profile_generates_correct_toml(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            codex_dir = Path(tmpdir) / ".codex"
            cfg = models_config.resolve_model_config("ternary-bonsai")
            _, profile_path = models_config.apply_codex_profile(cfg, codex_dir=codex_dir)

            content = profile_path.read_text(encoding="utf-8")
            self.assertIn(f'model = "{MODEL_ID}"', content)
            self.assertIn("model_context_window = 65536", content)
            self.assertIn('model_provider = "omlx"', content)
            self.assertIn('approval_policy = "never"', content)

    def test_env_override_context_window(self):
        cfg = models_config.resolve_model_config(
            "ternary-bonsai", env={"CONTEXT_WINDOW": "32768"}
        )
        self.assertEqual(cfg["context_window"], 32768)

    def test_env_override_temperature(self):
        cfg = models_config.resolve_model_config(
            "ternary-bonsai", env={"TEMPERATURE": "0.5"}
        )
        self.assertAlmostEqual(cfg["sampling"]["temperature"], 0.5, places=5)


if __name__ == "__main__":
    unittest.main(verbosity=2)
