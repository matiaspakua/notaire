import json
import os
import unittest

import helpers  # noqa: F401  (puts bin/ on sys.path)
import worker

HERE = "/repo/local-ai/sdlc"
ARGS = dict(wt="/wt", profile="omlx-gptoss", model="omlx/gpt-oss-20b-MXFP4-Q8", hooks="/runs/1/githooks",
            last="/runs/1/last-spec.md", prompt="do it", here=HERE)


class CommandTest(unittest.TestCase):
    def test_opencode_command(self):
        self.assertEqual(worker.command("opencode", **ARGS),
                         ["opencode", "run", "--pure", "--auto", "--format", "json", "--dir", "/wt",
                          "-m", "omlx/gpt-oss-20b-MXFP4-Q8", "do it"])

    def test_codex_command_unchanged(self):
        argv = worker.command("codex", **ARGS)
        self.assertEqual(argv[:6], ["codex", "exec", "--profile", "omlx-gptoss", "--skip-git-repo-check", "-C"])
        self.assertIn("project_doc_max_bytes=0", argv)
        self.assertEqual(argv[-3:], ["-o", "/runs/1/last-spec.md", "do it"])

    def test_unknown_agent_rejected(self):
        with self.assertRaises(ValueError):
            worker.command("aider", **ARGS)


class EnvironmentTest(unittest.TestCase):
    def test_opencode_config_is_isolated(self):
        env = worker.environment("opencode", {"PATH": "/usr/bin", "HOME": "/h"}, HERE, "/runs/1/githooks")
        self.assertEqual(env["OPENCODE_CONFIG_DIR"], "/repo/local-ai/opencode")
        self.assertEqual(env["OPENCODE_DISABLE_PROJECT_CONFIG"], "1")
        self.assertEqual(env["GH_CONFIG_DIR"], "/h/.config/gh")

    def test_guards_kept_for_opencode(self):
        env = worker.environment("opencode", {"PATH": "/usr/bin", "HOME": "/h"}, HERE, "/runs/1/githooks")
        self.assertEqual(env["GIT_CONFIG_VALUE_0"], "/runs/1/githooks")
        self.assertTrue(env["PATH"].startswith(HERE + "/bin/shims:"))

    def test_codex_environment_untouched(self):
        self.assertEqual(worker.environment("codex", {"PATH": "/usr/bin"}, HERE, "/h"), {"PATH": "/usr/bin"})


class LastMessageTest(unittest.TestCase):
    def test_last_text_part(self):
        events = [{"type": "text", "part": {"type": "text", "text": "\n"}},
                  {"type": "tool_use", "part": {"type": "tool"}},
                  {"type": "text", "part": {"type": "text", "text": "DONE"}}]
        self.assertEqual(worker.last_message("\n".join(json.dumps(e) for e in events) + "\nnot json\n"), "DONE")

    def test_no_text_is_empty(self):
        self.assertEqual(worker.last_message(""), "")


if __name__ == "__main__":
    unittest.main()
