import json
import os
import sys
import unittest

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "omlx"))
import harmony_repair  # noqa: E402


def repair(name, args):
    return harmony_repair.repair_tool_call(name, args)


class ArgumentsTest(unittest.TestCase):
    def test_argv_list_as_cmd_becomes_its_script(self):
        _, args = repair("exec_command", '{"cmd": ["bash", "-lc", "ls -R"]}')
        self.assertEqual(json.loads(args), {"cmd": "ls -R"})

    def test_argv_list_without_shell_is_joined(self):
        _, args = repair("exec_command", '{"cmd": ["sed", "-n", "1,80p", "a b.py"]}')
        self.assertEqual(json.loads(args), {"cmd": "sed -n 1,80p 'a b.py'"})

    def test_classic_command_key_becomes_cmd(self):
        _, args = repair("exec_command", '{"command": ["bash", "-lc", "pwd"], "workdir": "/tmp"}')
        self.assertEqual(json.loads(args), {"cmd": "pwd", "workdir": "/tmp"})

    def test_stray_bracket_after_heredoc_dropped(self):
        _, args = repair("exec_command", '{"cmd":"cat > a.py <<\'EOF\'\\nx = 1\\nEOF"]}')
        self.assertEqual(json.loads(args), {"cmd": "cat > a.py <<'EOF'\nx = 1\nEOF"})

    def test_invalid_escape_is_kept_literal(self):
        _, args = repair("exec_command", r'{"cmd": "grep -E \s+ a.py"}')
        self.assertEqual(json.loads(args), {"cmd": r"grep -E \s+ a.py"})

    def test_unterminated_cmd_string_closed(self):
        _, args = repair("exec_command", '{"cmd":"ls -R . | sed -e \'1p\' -e \'4p\'}')
        self.assertEqual(json.loads(args), {"cmd": "ls -R . | sed -e '1p' -e '4p'"})

    def test_valid_call_unchanged(self):
        self.assertEqual(repair("exec_command", '{"cmd": "ls"}'), ("exec_command", '{"cmd": "ls"}'))

    def test_unrepairable_arguments_pass_through(self):
        self.assertEqual(repair("exec_command", '{"cmd": "ls'), ("exec_command", '{"cmd": "ls'))

    def test_other_tool_arguments_untouched(self):
        self.assertEqual(repair("view_image", '{"path": ["a"]}'), ("view_image", '{"path": ["a"]}'))


class NameTest(unittest.TestCase):
    def test_header_tokens_after_name_dropped(self):
        self.assertEqual(repair("exec_command<|channel|>commentary", '{"cmd": "ls"}')[0], "exec_command")

    def test_constraint_word_after_name_dropped(self):
        self.assertEqual(repair("exec_command code", '{"cmd": "ls"}')[0], "exec_command")


class LostCallTest(unittest.TestCase):
    TEXT = "<|start|>assistant<|channel|>commentary to=functions.exec_command<|constrain|>"

    def test_empty_turn_that_addressed_a_tool_is_lost(self):
        self.assertTrue(harmony_repair.tool_call_lost(self.TEXT, [], ""))

    def test_turn_with_a_call_is_not_lost(self):
        self.assertFalse(harmony_repair.tool_call_lost(self.TEXT, [{"name": "exec_command"}], ""))

    def test_final_answer_is_not_lost(self):
        self.assertFalse(harmony_repair.tool_call_lost(self.TEXT, [], "3"))

    def test_recovery_call_is_a_valid_echo(self):
        call = harmony_repair.recovery_call()
        self.assertEqual(call["name"], "exec_command")
        self.assertTrue(json.loads(call["arguments"])["cmd"].startswith("echo "))

    def test_turn_without_tool_syntax_is_not_lost(self):
        self.assertFalse(harmony_repair.tool_call_lost("<|channel|>analysis<|message|>hm", [], ""))


if __name__ == "__main__":
    unittest.main()
