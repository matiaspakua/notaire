"""A fresh repository adopts the kit with `init`; `doctor` validates any profile (#1958)."""
from __future__ import annotations

import sys
import tomllib
import unittest

from helpers import KIT, Repo, git, output

sys.path.insert(0, str(KIT))
from aisdlc.profile import ProfileError, load_profile  # noqa: E402


class InitTest(unittest.TestCase):
    def test_init_scaffolds_policy_profile_card_and_agents(self):
        repo = Repo(self)
        result = repo.run("init", "--name", "shop", "--level", "2")
        self.assertEqual(0, result.returncode, output(result))
        for rel in ("aisdlc.toml", "CONSTITUTION.md", "AGENT-CARD.md", "AGENTS.md",
                    ".aisdlc/agents/foreman.md", ".aisdlc/changes/README.md"):
            self.assertTrue((repo.root / rel).is_file(), rel)
        profile = tomllib.loads((repo.root / "aisdlc.toml").read_text(encoding="utf-8"))
        self.assertEqual("shop", profile["project"]["name"])
        self.assertEqual(2, profile["project"]["level"])
        self.assertNotIn("{{", (repo.root / "CONSTITUTION.md").read_text(encoding="utf-8"))

    def test_init_never_overwrites_without_force(self):
        repo = Repo(self)
        repo.write("CONSTITUTION.md", "mine\n")
        result = repo.run("init", "--name", "shop")
        self.assertEqual(0, result.returncode)
        self.assertEqual("mine\n", (repo.root / "CONSTITUTION.md").read_text(encoding="utf-8"))
        self.assertIn("skip", output(result).lower())

    def test_fresh_repository_passes_doctor_and_pre_push_with_posix_tools_only(self):
        repo = Repo(self)
        self.assertEqual(0, repo.run("init", "--name", "shop").returncode)
        base = repo.commit("chore: adopt the AI SDLC framework")
        repo.write("README.md", "hello\n")
        repo.commit("docs: readme")
        doctor = repo.run("doctor")
        self.assertEqual(0, doctor.returncode, output(doctor))
        gate = repo.run("gate", "pre-push", "--base", base)
        self.assertEqual(0, gate.returncode, output(gate))

    def test_placeholder_gate_is_flagged_by_doctor(self):
        repo = Repo(self)
        repo.run("init", "--name", "shop")
        self.assertIn("placeholder", output(repo.run("doctor")).lower())


class DoctorTest(unittest.TestCase):
    def test_missing_policy_fails(self):
        repo = Repo(self, '[project]\nname = "x"\npolicy = "NOPE.md"\n')
        result = repo.run("doctor")
        self.assertEqual(1, result.returncode)
        self.assertIn("NOPE.md", output(result))

    def test_profile_is_found_under_dot_aisdlc(self):
        repo = Repo(self)
        repo.write(".aisdlc/aisdlc.toml", '[project]\nname = "x"\npolicy = "POLICY.md"\n')
        self.assertEqual(0, repo.run("doctor").returncode)

    def test_no_profile_is_a_configuration_error(self):
        repo = Repo(self)
        self.assertEqual(2, repo.run("doctor").returncode)

    def test_doctor_reports_missing_baseline_file(self):
        repo = Repo(self, """
            [[ratchets]]
            name = "b"
            kind = "baseline"
            file = "missing.txt"
        """)
        self.assertEqual(1, repo.run("doctor").returncode)

    def test_works_outside_git(self):
        repo = Repo(self, "")
        git(repo.root, "rev-parse", "--git-dir")
        (repo.root / ".git").rename(repo.root / ".git-off")
        self.assertEqual(0, repo.run("doctor").returncode)


class ProfileValidationTest(unittest.TestCase):
    def invalid(self, text: str) -> str:
        with self.assertRaises(ProfileError) as ctx:
            load_profile(text=text)
        return str(ctx.exception)

    def test_unknown_ratchet_kind(self):
        self.assertIn("kind", self.invalid('[project]\nname="x"\n[[ratchets]]\nname="r"\nkind="magic"\n'))

    def test_bad_regex(self):
        self.assertIn("pattern", self.invalid('[project]\nname="x"\n[commits]\npattern="(["\n'))

    def test_gate_without_run(self):
        self.assertIn("run", self.invalid('[project]\nname="x"\n[[gates]]\nname="g"\nstage="pre-push"\n'))

    def test_bad_expect(self):
        text = '[project]\nname="x"\n[[gates]]\nname="g"\nstage="red"\nrun="x"\nexpect="maybe"\n'
        self.assertIn("expect", self.invalid(text))

    def test_duplicate_gate_in_a_stage(self):
        gate = '[[gates]]\nname="g"\nstage="red"\nrun="x"\n'
        self.assertIn("duplicate", self.invalid('[project]\nname="x"\n' + gate + gate))

    def test_level_out_of_range(self):
        self.assertIn("level", self.invalid('[project]\nname="x"\nlevel=9\n'))

    def test_missing_project_name(self):
        self.assertIn("name", self.invalid("[project]\n"))

    def test_example_profiles_are_valid(self):
        examples = sorted((KIT / "profiles").glob("*.toml"))
        self.assertGreaterEqual(len(examples), 3)
        for path in examples:
            with self.subTest(path.name):
                load_profile(text=path.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
