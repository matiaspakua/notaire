import unittest

from helpers import commit_file, git, new_repo
import static_checks

JAVA_OK = "class FooTest { @Test void works() { assertThat(1).isEqualTo(1); } }\n"


class StaticChecksTest(unittest.TestCase):
    def setUp(self):
        self.repo = new_repo()
        commit_file(self.repo, "backend-api/src/test/java/a/ExistingTest.java", JAVA_OK, "test: base")
        self.base = git(self.repo, "rev-parse", "HEAD").strip()

    def problems(self):
        return static_checks.find_problems(self.repo, self.base)

    def test_rejects_absolute_home_path(self):
        commit_file(self.repo, "backend-api/src/test/java/a/FooTest.java",
                    JAVA_OK + 'String p = "/Users/me/workspace/x";\n')
        self.assertTrue(any("/Users/" in p and "FooTest.java" in p for p in self.problems()))

    def test_rejects_duplicate_test_class_name(self):
        commit_file(self.repo, "backend-api/src/test/java/b/ExistingTest.java", JAVA_OK)
        found = [p for p in self.problems() if "a/ExistingTest.java" in p]
        self.assertEqual(len(found), 1)
        self.assertIn("b/ExistingTest.java", found[0])

    def test_rejects_test_change_without_assertion(self):
        commit_file(self.repo, "backend-api/src/test/java/a/FooTest.java", "class FooTest { void noop() {} }\n")
        self.assertTrue(any("assertion" in p for p in self.problems()))

    def test_accepts_clean_test_change(self):
        commit_file(self.repo, "backend-api/src/test/java/a/FooTest.java", JAVA_OK)
        self.assertEqual(self.problems(), [])

    def test_accepts_frontend_expect(self):
        commit_file(self.repo, "frontend/src/lib/x.test.ts", "it('x', () => { expect(1).toBe(1) })\n")
        self.assertEqual(self.problems(), [])


if __name__ == "__main__":
    unittest.main()
