import json
import os
import shutil
import sys
import tempfile
import unittest
from pathlib import Path

TESTS_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, TESTS_DIR)
import helpers  # noqa: F401
LOCAL_AI_DIR = os.path.abspath(os.path.join(TESTS_DIR, "..", ".."))
sys.path.insert(0, LOCAL_AI_DIR)
import models_config


class NormalizeHfRefTest(unittest.TestCase):
    def test_full_url(self):
        url = "https://huggingface.co/prism-ml/Ternary-Bonsai-2-27B-mlx-2bit"
        self.assertEqual(models_config.normalize_hf_ref(url), "prism-ml/Ternary-Bonsai-2-27B-mlx-2bit")

    def test_url_with_trailing_slash(self):
        url = "https://huggingface.co/org/model/"
        self.assertEqual(models_config.normalize_hf_ref(url), "org/model")

    def test_url_with_tree_branch(self):
        url = "https://huggingface.co/org/model/tree/main"
        self.assertEqual(models_config.normalize_hf_ref(url), "org/model")

    def test_plain_repo(self):
        self.assertEqual(models_config.normalize_hf_ref("mlx-community/gpt-oss-20b-MXFP4-Q8"), "mlx-community/gpt-oss-20b-MXFP4-Q8")

    def test_hf_co_short_url(self):
        self.assertEqual(models_config.normalize_hf_ref("http://hf.co/user/test-model"), "user/test-model")


class ResolveModelConfigTest(unittest.TestCase):
    def test_resolve_known_preset(self):
        cfg = models_config.resolve_model_config("qwen3-coder")
        self.assertEqual(cfg["preset_key"], "qwen3-coder")
        self.assertEqual(cfg["model_id"], "Qwen3-Coder-30B-A3B-Instruct-4bit")
        self.assertEqual(cfg["codex_profile"], "omlx")
        self.assertEqual(cfg["context_window"], 32768)
        self.assertFalse(cfg["opencode"]["reasoning"])

    def test_resolve_gpt_oss_preset(self):
        cfg = models_config.resolve_model_config("gpt-oss")
        self.assertEqual(cfg["preset_key"], "gpt-oss")
        self.assertEqual(cfg["model_id"], "gpt-oss-20b-MXFP4-Q8")
        self.assertEqual(cfg["codex_profile"], "omlx-gptoss")
        self.assertEqual(cfg["context_window"], 65536)
        self.assertEqual(cfg["reasoning"], "medium")
        self.assertTrue(cfg["opencode"]["reasoning"])

    def test_resolve_ternary_bonsai_preset_and_url(self):
        cfg1 = models_config.resolve_model_config("ternary-bonsai")
        self.assertEqual(cfg1["preset_key"], "ternary-bonsai")
        self.assertEqual(cfg1["model_id"], "Ternary-Bonsai-2-27B-mlx-2bit")
        self.assertEqual(cfg1["codex_profile"], "omlx-bonsai")

        cfg2 = models_config.resolve_model_config("https://huggingface.co/prism-ml/Ternary-Bonsai-2-27B-mlx-2bit")
        self.assertEqual(cfg2["preset_key"], "ternary-bonsai")
        self.assertEqual(cfg2["model_id"], "Ternary-Bonsai-2-27B-mlx-2bit")

        cfg3 = models_config.resolve_model_config("bonsai")
        self.assertEqual(cfg3["preset_key"], "ternary-bonsai")

    def test_resolve_dynamic_unlisted_model(self):
        url = "https://huggingface.co/meta-llama/Llama-3.2-3B-Instruct"
        cfg = models_config.resolve_model_config(url)
        self.assertEqual(cfg["preset_key"], "custom")
        self.assertEqual(cfg["repo"], "meta-llama/Llama-3.2-3B-Instruct")
        self.assertEqual(cfg["model_id"], "Llama-3.2-3B-Instruct")
        self.assertEqual(cfg["codex_profile"], "omlx-llama-3-2-3b-instruct")
        self.assertEqual(cfg["context_window"], 32768)

    def test_env_overrides(self):
        env = {
            "CONTEXT_WINDOW": "16384",
            "TEMPERATURE": "0.5",
            "CODEX_PROFILE": "custom-prof",
            "REASONING": "low",
        }
        cfg = models_config.resolve_model_config("qwen3-coder", env=env)
        self.assertEqual(cfg["context_window"], 16384)
        self.assertEqual(cfg["sampling"]["temperature"], 0.5)
        self.assertEqual(cfg["codex_profile"], "custom-prof")
        self.assertEqual(cfg["reasoning"], "low")


class ApplyConfigTest(unittest.TestCase):
    def setUp(self):
        self.tmpdir = tempfile.mkdtemp()
        self.tmppath = Path(self.tmpdir)

    def tearDown(self):
        shutil.rmtree(self.tmpdir)

    def test_apply_opencode_config(self):
        opencode_path = self.tmppath / "opencode.json"
        initial_data = {
            "$schema": "https://opencode.ai/config.json",
            "provider": {
                "omlx": {
                    "models": {
                        "old-model": {"name": "Old", "tool_call": True, "limit": {"context": 8192, "output": 4096}}
                    }
                }
            }
        }
        with open(opencode_path, "w", encoding="utf-8") as f:
            json.dump(initial_data, f)

        cfg = models_config.resolve_model_config("ternary-bonsai")
        updated = models_config.apply_opencode_config(cfg, opencode_json_path=opencode_path)
        self.assertTrue(updated)

        with open(opencode_path, "r", encoding="utf-8") as f:
            saved = json.load(f)

        models = saved["provider"]["omlx"]["models"]
        self.assertIn("Ternary-Bonsai-2-27B-mlx-2bit", models)
        bonsai_entry = models["Ternary-Bonsai-2-27B-mlx-2bit"]
        self.assertEqual(bonsai_entry["limit"]["context"], 65536)
        self.assertTrue(bonsai_entry["tool_call"])

    def test_apply_omlx_settings(self):
        omlx_dir = self.tmppath / ".omlx"
        ssd_cache = omlx_dir / "cache"
        cfg = models_config.resolve_model_config("ternary-bonsai")
        changed = models_config.apply_omlx_settings(cfg, omlx_dir=omlx_dir, ssd_cache_dir=ssd_cache)
        self.assertTrue(changed)

        model_settings_file = omlx_dir / "model_settings.json"
        self.assertTrue(model_settings_file.is_file())
        with open(model_settings_file, "r", encoding="utf-8") as f:
            models_data = json.load(f)
        self.assertIn("Ternary-Bonsai-2-27B-mlx-2bit", models_data["models"])

    def test_apply_codex_profile(self):
        codex_dir = self.tmppath / ".codex"
        cfg = models_config.resolve_model_config("ternary-bonsai")
        catalog_path, profile_path = models_config.apply_codex_profile(cfg, codex_dir=codex_dir)
        self.assertTrue(catalog_path.is_file())
        self.assertTrue(profile_path.is_file())

        with open(catalog_path, "r", encoding="utf-8") as f:
            catalog_data = json.load(f)
        self.assertEqual(catalog_data["models"][0]["slug"], "Ternary-Bonsai-2-27B-mlx-2bit")

        profile_content = profile_path.read_text(encoding="utf-8")
        self.assertIn('model = "Ternary-Bonsai-2-27B-mlx-2bit"', profile_content)
        self.assertIn("model_context_window = 65536", profile_content)


if __name__ == "__main__":
    unittest.main()
