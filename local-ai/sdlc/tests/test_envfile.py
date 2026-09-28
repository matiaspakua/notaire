import os
import tempfile
import unittest

import helpers  # noqa: F401  (puts bin/ on sys.path)
import envfile


class ParseValueTest(unittest.TestCase):
    def test_strips_double_quotes_around_command(self):
        self.assertEqual(envfile.parse_value('"mvn -q test -Dtest=FooTest"'), "mvn -q test -Dtest=FooTest")

    def test_strips_single_quotes_and_trailing_comment(self):
        self.assertEqual(envfile.parse_value("'code'  # triage"), "code")

    def test_keeps_inner_quotes(self):
        self.assertEqual(envfile.parse_value('bash -c "exit 1"'), 'bash -c "exit 1"')

    def test_unescapes_quotes_inside_a_quoted_value(self):
        self.assertEqual(envfile.parse_value('"bash -c \\"exit 3\\""'), 'bash -c "exit 3"')

    def test_keeps_hash_without_leading_space(self):
        self.assertEqual(envfile.parse_value("mvn test -Dtest=FooTest#bar"), "mvn test -Dtest=FooTest#bar")


class ReadEnvTest(unittest.TestCase):
    def test_reads_keys_first_occurrence_wins(self):
        path = os.path.join(tempfile.mkdtemp(), "tests.env")
        with open(path, "w") as f:
            f.write('# comment\nTEST_CMD="mvn test"\nKIND=code\nTEST_CMD=other\n')
        self.assertEqual(envfile.read_env(path), {"TEST_CMD": "mvn test", "KIND": "code"})

    def test_missing_file_is_empty(self):
        self.assertEqual(envfile.read_env("/nonexistent/tests.env"), {})


if __name__ == "__main__":
    unittest.main()
