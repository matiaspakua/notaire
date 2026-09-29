import os
import tempfile
import unittest

import helpers  # noqa: F401  (puts bin/ on sys.path)
import triage_check


def env_file(text):
    path = os.path.join(tempfile.mkdtemp(), "triage.env")
    with open(path, "w") as f:
        f.write(text)
    return path


class RestoreTest(unittest.TestCase):
    def test_derived_value_restored(self):
        seed = env_file("ISSUE=7\nUSE_CASE=CU76\nTYPE=docs\nKIND=?\n")
        work = env_file("ISSUE=7\nUSE_CASE=CU76\nTYPE=fix\nKIND=code\n")
        self.assertEqual(triage_check.restore(work, seed), ["TYPE"])
        with open(work) as f:
            self.assertEqual(f.read(), "ISSUE=7\nUSE_CASE=CU76\nTYPE=docs\nKIND=code\n")

    def test_underivable_value_kept(self):
        seed = env_file("ISSUE=7\nTYPE=?\n")
        work = env_file("ISSUE=7\nTYPE=fix\n")
        self.assertEqual(triage_check.restore(work, seed), [])
        with open(work) as f:
            self.assertIn("TYPE=fix", f.read())

    def test_deleted_derived_key_is_added_back(self):
        seed = env_file("ISSUE=7\nUSE_CASE=CU76\n")
        work = env_file("ISSUE=7\n")
        self.assertEqual(triage_check.restore(work, seed), ["USE_CASE"])
        with open(work) as f:
            self.assertIn("USE_CASE=CU76", f.read())


class BadProofsTest(unittest.TestCase):
    def test_search_command_rejected(self):
        crit = "1. TODO — names current — proven by: command bash grep -r X docs/"
        self.assertEqual(triage_check.bad_proofs(crit), [crit])

    def test_git_read_command_rejected(self):
        crit = "2. TODO — present — proven by: command git ls-tree -r HEAD -- docs/"
        self.assertEqual(triage_check.bad_proofs(crit), [crit])

    def test_script_command_accepted(self):
        self.assertEqual(triage_check.bad_proofs("1. TODO — clean — proven by: command bash scripts/preflight.sh"), [])

    def test_test_proof_accepted(self):
        self.assertEqual(triage_check.bad_proofs("1. TODO — x — proven by: new test XTest#shouldY"), [])


class KindConflictTest(unittest.TestCase):
    def test_docs_kind_with_new_test_rejected(self):
        self.assertTrue(triage_check.kind_conflict("docs", "1. TODO — x — proven by: new test XTest#shouldY"))

    def test_code_kind_with_new_test_accepted(self):
        self.assertFalse(triage_check.kind_conflict("code", "1. TODO — x — proven by: new test XTest#shouldY"))

    def test_docs_kind_with_command_accepted(self):
        self.assertFalse(triage_check.kind_conflict("docs", "1. TODO — x — proven by: command bash scripts/x.sh"))


if __name__ == "__main__":
    unittest.main()
