import os
import subprocess
import tempfile
import unittest

import helpers  # noqa: F401  (puts bin/ on sys.path)
import crawl_guard

SHIMS = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "bin", "shims")


class GrepArgsTest(unittest.TestCase):
    def test_recursive_grep_skips_dependency_trees(self):
        self.assertIn("--exclude-dir=node_modules", crawl_guard.grep_args(["-rn", "Jenkins", "."]))

    def test_plain_grep_unchanged(self):
        self.assertEqual(crawl_guard.grep_args(["-n", "x", "f.txt"]), ["-n", "x", "f.txt"])

    def test_long_recursive_option_detected(self):
        self.assertIn("--exclude-dir=target", crawl_guard.grep_args(["--recursive", "x"]))


class FindArgsTest(unittest.TestCase):
    def test_expression_without_action_gets_print(self):
        args = crawl_guard.find_args([".", "-name", "Jenkinsfile"])
        self.assertEqual(args[0], ".")
        self.assertEqual(args[-5:], ["(", "-name", "Jenkinsfile", ")", "-print"])

    def test_expression_with_exec_gets_no_extra_print(self):
        args = crawl_guard.find_args([".", "-name", "*.md", "-exec", "grep", "-l", "x", "{}", ";"])
        self.assertNotEqual(args[-1], "-print")

    def test_bare_find_defaults_to_current_dir(self):
        self.assertEqual(crawl_guard.find_args([])[0], ".")

    def test_leading_options_stay_first(self):
        self.assertEqual(crawl_guard.find_args(["-L", "src", "-type", "f"])[:2], ["-L", "src"])


class ShimTest(unittest.TestCase):
    def setUp(self):
        self.root = tempfile.mkdtemp()
        for d in ("src", "node_modules/pkg"):
            os.makedirs(os.path.join(self.root, d))
            with open(os.path.join(self.root, d, "a.txt"), "w") as f:
                f.write("jenkins\n")
        self.env = dict(os.environ, PATH=SHIMS + os.pathsep + os.environ["PATH"])

    def run_shim(self, *cmd):
        return subprocess.run(cmd, cwd=self.root, env=self.env, capture_output=True, text=True).stdout

    def test_find_shim_prunes_node_modules(self):
        self.assertEqual(self.run_shim("find", ".", "-name", "a.txt").split(), ["./src/a.txt"])

    def test_grep_shim_skips_node_modules(self):
        self.assertEqual(self.run_shim("grep", "-rl", "jenkins", ".").split(), ["./src/a.txt"])


if __name__ == "__main__":
    unittest.main()
