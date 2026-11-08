"""The skills catalog is linted: present, described, unique, and generic skills stay generic (#1958)."""
from __future__ import annotations

import unittest

from helpers import Repo, output

PROFILE = """
    [skills]
    dir = "skills"
    generic = ["qa"]
    forbid_in_generic = ['acme-internal', 'src/acme/']
"""


def skill(name: str, description: str = "Use when testing.") -> str:
    return f"---\nname: {name}\ndescription: {description}\n---\n\n# {name}\n"


class SkillsLintTest(unittest.TestCase):
    def setUp(self):
        self.repo = Repo(self, PROFILE)
        self.repo.write("skills/qa/SKILL.md", skill("qa"))
        self.repo.write("skills/backend/SKILL.md", skill("backend"))

    def test_clean_catalog_passes(self):
        result = self.repo.run("skills")
        self.assertEqual(0, result.returncode, output(result))

    def test_folder_without_skill_md_fails(self):
        self.repo.write("skills/empty/notes.md", "nothing\n")
        result = self.repo.run("skills")
        self.assertEqual(1, result.returncode)
        self.assertIn("empty", output(result))

    def test_skill_without_description_fails(self):
        self.repo.write("skills/backend/SKILL.md", "# backend\n\nNo frontmatter.\n")
        result = self.repo.run("skills")
        self.assertEqual(1, result.returncode)
        self.assertIn("description", output(result))

    def test_multiline_description_is_accepted(self):
        self.repo.write("skills/backend/SKILL.md", "---\nname: backend\ndescription: >\n  Use when building\n  APIs.\n---\n")
        self.assertEqual(0, self.repo.run("skills").returncode)

    def test_duplicate_names_fail_naming_both_folders(self):
        self.repo.write("skills/ui/SKILL.md", skill("backend"))
        result = self.repo.run("skills")
        self.assertEqual(1, result.returncode)
        self.assertIn("skills/ui", output(result))
        self.assertIn("skills/backend", output(result))

    def test_folder_name_counts_when_frontmatter_has_no_name(self):
        self.repo.write("skills/ui/SKILL.md", "---\nname: backend\ndescription: d\n---\n")
        self.repo.write("skills/backend/SKILL.md", "---\ndescription: d\n---\n")
        self.assertEqual(1, self.repo.run("skills").returncode)

    def test_project_path_in_generic_skill_fails_naming_the_file(self):
        self.repo.write("skills/qa/references/plan.md", "Run tests in src/acme/ first.\n")
        result = self.repo.run("skills")
        self.assertEqual(1, result.returncode)
        self.assertIn("skills/qa/references/plan.md", output(result))

    def test_project_path_in_project_skill_is_allowed(self):
        self.repo.write("skills/backend/SKILL.md", skill("backend", "Use for acme-internal services."))
        self.assertEqual(0, self.repo.run("skills").returncode)

    def test_generic_skill_listed_but_missing_fails(self):
        self.repo.remove("skills/qa/SKILL.md")
        self.assertEqual(1, self.repo.run("skills").returncode)


if __name__ == "__main__":
    unittest.main()
