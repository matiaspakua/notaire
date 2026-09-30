import os
import shutil
import sys
import tempfile
import unittest

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "omlx"))
import patch_omlx  # noqa: E402

# the oMLX 0.7.0 snippets the patches anchor on, nothing else
HARMONY = '''import json
import logging


def parse_tool_calls_from_tokens(token_ids, prepend_start=True):
    try:
        messages = encoding.parse_messages_from_completion_tokens(
            full_token_ids,
            role=Role.ASSISTANT,
            strict=False,
        )
        for msg in messages:
            if _is_tool_call_message(msg):
                name = msg.recipient[10:]  # Remove "functions." prefix
                tool_calls.append(
                    {"name": name, "arguments": _message_content_text(msg)}
                )

        return output_text, analysis_text, tool_calls

    except Exception as e:
        logger.warning(f"Error parsing tool calls from tokens: {e}")
        return "", "", []


def _is_tool_call_message(msg):
    recipient = getattr(msg, "recipient", None)
    try:
        return isinstance(json.loads(_message_content_text(msg)), dict)
    except ValueError:
        return False


class HarmonyStreamingParser:
    def get_tool_calls(self):
        try:
            for msg in messages:
                name = msg.recipient[10:]  # Remove "functions." prefix
                content = _message_content_text(msg)

                tool_calls.append({"name": name, "arguments": content})
        except Exception:
            pass
'''


class PatchTest(unittest.TestCase):
    def setUp(self):
        self.root = tempfile.mkdtemp()
        self.adapter = os.path.join(self.root, "adapter")
        os.makedirs(self.adapter)
        self.harmony = os.path.join(self.adapter, "harmony.py")
        with open(self.harmony, "w") as f:
            f.write(HARMONY)
        self.addCleanup(shutil.rmtree, self.root)

    def read(self):
        with open(self.harmony) as f:
            return f.read()

    def test_first_run_applies_every_patch(self):
        report = patch_omlx.apply(self.root)
        self.assertEqual(set(report.values()), {"applied"})
        self.assertIn("role=None if has_header else Role.ASSISTANT", self.read())
        self.assertTrue(os.path.exists(os.path.join(self.adapter, "notaire_harmony_repair.py")))

    def test_second_run_changes_nothing(self):
        patch_omlx.apply(self.root)
        once = self.read()
        report = patch_omlx.apply(self.root)
        self.assertEqual(set(report.values()), {"already"})
        self.assertEqual(self.read(), once)

    def test_missing_anchor_skipped(self):
        with open(self.harmony, "w") as f:
            f.write("import json\n")
        report = patch_omlx.apply(self.root)
        self.assertIn("skipped", report.values())
        self.assertEqual(self.read(), "import json\n")

    def test_restore_puts_back_the_original(self):
        patch_omlx.apply(self.root)
        patch_omlx.restore(self.root)
        self.assertEqual(self.read(), HARMONY)
        self.assertFalse(os.path.exists(os.path.join(self.adapter, "notaire_harmony_repair.py")))

    def test_analysis_call_check_uses_the_repair(self):
        patch_omlx.apply(self.root)
        self.assertIn("_, arguments = repair_tool_call(recipient[10:], _message_content_text(msg))", self.read())

    def test_lost_call_is_raised_for_a_retry(self):
        patch_omlx.apply(self.root)
        self.assertIn("raise ToolCallLost", self.read())

    def test_patched_module_compiles(self):
        patch_omlx.apply(self.root)
        compile(self.read(), self.harmony, "exec")


if __name__ == "__main__":
    unittest.main()
