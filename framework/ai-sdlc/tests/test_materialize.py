"""Tests for catalog AI configuration import and materialize (#2399 / CU76)."""
from __future__ import annotations

import json
import shutil
import sys
import tempfile
import unittest
import unittest.mock
from pathlib import Path

from helpers import KIT, Repo, output

sys.path.insert(0, str(KIT))
from aisdlc import catalogs, evidence
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.telemetry import MemoryTelemetry

PROFILE = """
[project]
name = "demo"
policy = "POLICY.md"

[skills]
dir = ".claude/skills"

[catalogs]
dir = ".aisdlc/catalogs"
"""


class CatalogMaterializeTest(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = Repo(self, PROFILE)
        self.repo.write("POLICY.md", "# Policy\n")

    def test_scenario_import_stores_files_in_revision(self):
        """Scenario: import stores files in a revision."""
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-claude-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        skill_dir = fixture_dir / ".claude" / "skills" / "code-reviewer"
        skill_dir.mkdir(parents=True)
        (skill_dir / "SKILL.md").write_text(
            "---\nname: code-reviewer\ndescription: Code reviewer skill\n---\n# Reviewer\n",
            encoding="utf-8",
        )
        (skill_dir / "rules.txt").write_text("Rule 1: be kind\n", encoding="utf-8")

        agent_file = fixture_dir / ".claude" / "agents" / "planner.md"
        agent_file.parent.mkdir(parents=True)
        agent_file.write_text(
            "---\nname: planner\ndescription: Planner agent\n---\n# Planner\n",
            encoding="utf-8",
        )

        result = self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))
        self.assertEqual(0, result.returncode, output(result))

        skill_current = self.repo.root / ".aisdlc" / "catalogs" / "skills" / "code-reviewer" / "current.json"
        self.assertTrue(skill_current.is_file(), f"{skill_current} should exist")
        skill_data = json.loads(skill_current.read_text(encoding="utf-8"))
        self.assertEqual("code-reviewer", skill_data["id"])
        self.assertEqual("Code reviewer skill", skill_data.get("summary"))
        self.assertEqual(1, skill_data["version"])
        self.assertIn("files", skill_data)
        self.assertEqual("Rule 1: be kind\n", skill_data["files"].get("rules.txt"))
        self.assertIn("SKILL.md", skill_data["files"])

        agent_current = self.repo.root / ".aisdlc" / "catalogs" / "agents" / "planner" / "current.json"
        self.assertTrue(agent_current.is_file(), f"{agent_current} should exist")
        agent_data = json.loads(agent_current.read_text(encoding="utf-8"))
        self.assertEqual("planner", agent_data["id"])
        self.assertEqual("Planner agent", agent_data.get("summary"))
        self.assertEqual(1, agent_data["version"])
        self.assertIn("files", agent_data)
        self.assertIn("planner.md", agent_data["files"])

    def test_scenario_import_is_idempotent(self):
        """Scenario: import is idempotent."""
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-claude-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        skill_dir = fixture_dir / ".claude" / "skills" / "demo"
        skill_dir.mkdir(parents=True)
        (skill_dir / "SKILL.md").write_text("# Demo\n", encoding="utf-8")

        res1 = self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))
        self.assertEqual(0, res1.returncode, output(res1))

        res2 = self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))
        self.assertEqual(0, res2.returncode, output(res2))

        entry_dir = self.repo.root / ".aisdlc" / "catalogs" / "skills" / "demo"
        current_data = json.loads((entry_dir / "current.json").read_text(encoding="utf-8"))
        self.assertEqual(1, current_data["version"])
        self.assertFalse((entry_dir / "versions" / "2.json").exists())

    def test_scenario_import_rejects_literal_secret(self):
        """Scenario: import rejects a literal secret."""
        token = "gh" + "p_" + "SecretToken1234567890"
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-claude-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        agent_file = fixture_dir / ".claude" / "agents" / "leaky.md"
        agent_file.parent.mkdir(parents=True)
        agent_file.write_text(f"# Agent\ntoken: {token}\n", encoding="utf-8")

        result = self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))
        self.assertEqual(1, result.returncode, output(result))
        out = output(result)
        self.assertIn("leaky.md", out)
        self.assertNotIn(token, out)

        catalog_entry = self.repo.root / ".aisdlc" / "catalogs" / "agents" / "leaky" / "current.json"
        self.assertFalse(catalog_entry.exists())

    def test_scenario_materialize_writes_layout_plus_manifest(self):
        """Scenario: materialize writes layout plus manifest."""
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-claude-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        skill_dir = fixture_dir / ".claude" / "skills" / "builder"
        skill_dir.mkdir(parents=True)
        (skill_dir / "SKILL.md").write_text("# Builder skill\n", encoding="utf-8")
        (skill_dir / "sub.txt").write_text("sub-content\n", encoding="utf-8")

        self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))

        dest_dir = Path(tempfile.mkdtemp(prefix="dest-claude-"))
        self.addCleanup(shutil.rmtree, dest_dir, ignore_errors=True)

        res = self.repo.run("catalog", "materialize", "--harness", "claude-code", "--dest", str(dest_dir))
        self.assertEqual(0, res.returncode, output(res))

        self.assertTrue((dest_dir / ".claude" / "skills" / "builder" / "SKILL.md").is_file())
        self.assertTrue((dest_dir / ".claude" / "skills" / "builder" / "sub.txt").is_file())

        manifest_path = dest_dir / ".aisdlc-materialized.json"
        self.assertTrue(manifest_path.is_file())
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        self.assertEqual(1, manifest["schema"])
        self.assertEqual("claude-code", manifest["harness"])
        self.assertEqual(1, len(manifest["entries"]))
        entry = manifest["entries"][0]
        self.assertEqual("skills", entry["kind"])
        self.assertEqual("builder", entry["id"])
        self.assertIn(".claude/skills/builder/SKILL.md", entry["files"])
        self.assertIn(".claude/skills/builder/sub.txt", entry["files"])

    def test_scenario_check_reports_drift_and_exits_1(self):
        """Scenario: check reports drift and exits 1."""
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-claude-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        rule_file = fixture_dir / ".claude" / "rules" / "style.md"
        rule_file.parent.mkdir(parents=True)
        rule_file.write_text("# Style\n", encoding="utf-8")

        self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))

        dest_dir = Path(tempfile.mkdtemp(prefix="dest-claude-"))
        self.addCleanup(shutil.rmtree, dest_dir, ignore_errors=True)

        res = self.repo.run("catalog", "materialize", "--harness", "claude-code", "--dest", str(dest_dir))
        self.assertEqual(0, res.returncode, output(res))

        # 1. Clean check exits 0
        chk_clean = self.repo.run(
            "catalog", "materialize", "--harness", "claude-code", "--dest", str(dest_dir), "--check"
        )
        self.assertEqual(0, chk_clean.returncode, output(chk_clean))

        # 2. Modified file exits 1 and does not overwrite disk
        target_rule = dest_dir / ".claude" / "rules" / "style.md"
        target_rule.write_text("# Drifted Style\n", encoding="utf-8")
        chk_mod = self.repo.run(
            "catalog", "materialize", "--harness", "claude-code", "--dest", str(dest_dir), "--check"
        )
        self.assertEqual(1, chk_mod.returncode, output(chk_mod))
        self.assertIn("modified", output(chk_mod).lower())
        self.assertEqual("# Drifted Style\n", target_rule.read_text(encoding="utf-8"))

        # 3. Missing file exits 1 and does not recreate disk file
        target_rule.unlink()
        chk_missing = self.repo.run(
            "catalog", "materialize", "--harness", "claude-code", "--dest", str(dest_dir), "--check"
        )
        self.assertEqual(1, chk_missing.returncode, output(chk_missing))
        self.assertIn("missing", output(chk_missing).lower())
        self.assertFalse(target_rule.exists())

        # 4. Missing manifest exits 1
        (dest_dir / ".aisdlc-materialized.json").unlink()
        chk_no_manifest = self.repo.run(
            "catalog", "materialize", "--harness", "claude-code", "--dest", str(dest_dir), "--check"
        )
        self.assertEqual(1, chk_no_manifest.returncode, output(chk_no_manifest))

    def test_scenario_materialize_is_evidence_and_telemetry(self):
        """Scenario: materialize is evidence and telemetry."""
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-claude-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        agent_file = fixture_dir / ".claude" / "agents" / "agent1.md"
        agent_file.parent.mkdir(parents=True)
        secret_body = "SuperSecretPromptBodyContent"
        agent_file.write_text(f"# Agent 1\n{secret_body}\n", encoding="utf-8")

        res_imp = self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))
        self.assertEqual(0, res_imp.returncode, output(res_imp))

        dest_dir = Path(tempfile.mkdtemp(prefix="dest-claude-"))
        self.addCleanup(shutil.rmtree, dest_dir, ignore_errors=True)

        res_mat = self.repo.run("catalog", "materialize", "--harness", "claude-code", "--dest", str(dest_dir))
        self.assertEqual(0, res_mat.returncode, output(res_mat))

        ctx = Context(self.repo.root)
        records = evidence.ledger_records(ctx)
        kinds = [r.get("kind") for r in records]
        self.assertIn("catalog.import", kinds)
        self.assertIn("catalog.materialize", kinds)

        for r in records:
            if r.get("kind") in ("catalog.import", "catalog.materialize"):
                self.assertEqual("claude-code", r.get("harness"))
                self.assertIn("entries", r)
                self.assertIn("files", r)
                self.assertIn("digest", r)
                self.assertIn("actor", r)
                raw = json.dumps(r)
                self.assertNotIn(secret_body, raw)
                self.assertNotIn(str(fixture_dir), raw)
                self.assertNotIn(str(dest_dir), raw)

    def test_scenario_round_trip_byte_identical(self):
        """Scenario: round trip byte identical."""
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-rt-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        # multi-file skill folder
        skill_dir = fixture_dir / ".claude" / "skills" / "rt-skill"
        skill_dir.mkdir(parents=True)
        (skill_dir / "SKILL.md").write_bytes(b"# RT Skill\n")
        (skill_dir / "sub.txt").write_bytes(b"sub content\n")

        # agent
        agent_file = fixture_dir / ".claude" / "agents" / "rt-agent.md"
        agent_file.parent.mkdir(parents=True)
        agent_file.write_bytes(b"# RT Agent\n")

        # rule
        rule_file = fixture_dir / ".claude" / "rules" / "rt-rule.md"
        rule_file.parent.mkdir(parents=True)
        rule_file.write_bytes(b"# RT Rule\n")

        # CRLF rule
        crlf_file = fixture_dir / ".claude" / "rules" / "crlf-rule.md"
        crlf_file.write_bytes(b"# CRLF Rule\r\nsecond line\r\nthird line\r\n")

        # Import
        imp = self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))
        self.assertEqual(0, imp.returncode, output(imp))

        # Materialize to clean destination
        dest_dir = Path(tempfile.mkdtemp(prefix="dest-rt-"))
        self.addCleanup(shutil.rmtree, dest_dir, ignore_errors=True)
        mat = self.repo.run("catalog", "materialize", "--harness", "claude-code", "--dest", str(dest_dir))
        self.assertEqual(0, mat.returncode, output(mat))

        # Assert byte-for-byte identity
        for src_path in fixture_dir.rglob("*"):
            if src_path.is_file():
                rel = src_path.relative_to(fixture_dir)
                dst_path = dest_dir / rel
                self.assertTrue(dst_path.is_file(), f"Expected {dst_path} to exist")
                self.assertEqual(src_path.read_bytes(), dst_path.read_bytes(), f"Mismatch at {rel}")

    def test_layouts_per_harness_materialize_to_right_paths(self):
        """Layouts per harness materialize to their designated paths."""
        # Setup catalog entries for skills, agents, rules
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-layouts-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        skill_dir = fixture_dir / ".claude" / "skills" / "shared-skill"
        skill_dir.mkdir(parents=True)
        (skill_dir / "SKILL.md").write_text("# Shared Skill\n", encoding="utf-8")
        (skill_dir / "helper.py").write_text("print('helper')\n", encoding="utf-8")

        agent_file = fixture_dir / ".claude" / "agents" / "shared-agent.md"
        agent_file.parent.mkdir(parents=True)
        agent_file.write_text("# Shared Agent\n", encoding="utf-8")

        rule_file = fixture_dir / ".claude" / "rules" / "shared-rule.md"
        rule_file.parent.mkdir(parents=True)
        rule_file.write_text("# Shared Rule\n", encoding="utf-8")

        imp = self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))
        self.assertEqual(0, imp.returncode, output(imp))

        # 1. claude-code: skills -> dir, agents -> file, rules -> file
        d_claude = Path(tempfile.mkdtemp(prefix="dest-claude-"))
        self.addCleanup(shutil.rmtree, d_claude, ignore_errors=True)
        self.assertEqual(
            0,
            self.repo.run("catalog", "materialize", "--harness", "claude-code", "--dest", str(d_claude)).returncode,
        )
        self.assertTrue((d_claude / ".claude" / "skills" / "shared-skill" / "SKILL.md").is_file())
        self.assertTrue((d_claude / ".claude" / "skills" / "shared-skill" / "helper.py").is_file())
        self.assertTrue((d_claude / ".claude" / "agents" / "shared-agent.md").is_file())
        self.assertTrue((d_claude / ".claude" / "rules" / "shared-rule.md").is_file())

        # 2. cursor: rules -> file .cursor/rules/<id>.mdc
        d_cursor = Path(tempfile.mkdtemp(prefix="dest-cursor-"))
        self.addCleanup(shutil.rmtree, d_cursor, ignore_errors=True)
        self.assertEqual(
            0,
            self.repo.run("catalog", "materialize", "--harness", "cursor", "--dest", str(d_cursor)).returncode,
        )
        self.assertTrue((d_cursor / ".cursor" / "rules" / "shared-rule.mdc").is_file())
        self.assertFalse((d_cursor / ".cursor" / "agents").exists())

        # 3. codex: skills -> dir .codex/skills/<id>/
        d_codex = Path(tempfile.mkdtemp(prefix="dest-codex-"))
        self.addCleanup(shutil.rmtree, d_codex, ignore_errors=True)
        self.assertEqual(
            0,
            self.repo.run("catalog", "materialize", "--harness", "codex", "--dest", str(d_codex)).returncode,
        )
        self.assertTrue((d_codex / ".codex" / "skills" / "shared-skill" / "SKILL.md").is_file())
        self.assertTrue((d_codex / ".codex" / "skills" / "shared-skill" / "helper.py").is_file())

        # 4. junie: rules -> file .junie/guidelines/<id>.md
        d_junie = Path(tempfile.mkdtemp(prefix="dest-junie-"))
        self.addCleanup(shutil.rmtree, d_junie, ignore_errors=True)
        self.assertEqual(
            0,
            self.repo.run("catalog", "materialize", "--harness", "junie", "--dest", str(d_junie)).returncode,
        )
        self.assertTrue((d_junie / ".junie" / "guidelines" / "shared-rule.md").is_file())

        # 5. kiro: rules -> file .kiro/steering/<id>.md
        d_kiro = Path(tempfile.mkdtemp(prefix="dest-kiro-"))
        self.addCleanup(shutil.rmtree, d_kiro, ignore_errors=True)
        self.assertEqual(
            0,
            self.repo.run("catalog", "materialize", "--harness", "kiro", "--dest", str(d_kiro)).returncode,
        )
        self.assertTrue((d_kiro / ".kiro" / "steering" / "shared-rule.md").is_file())

        # 6. agy: skills -> dir .agents/skills/<id>/
        d_agy = Path(tempfile.mkdtemp(prefix="dest-agy-"))
        self.addCleanup(shutil.rmtree, d_agy, ignore_errors=True)
        self.assertEqual(
            0,
            self.repo.run("catalog", "materialize", "--harness", "agy", "--dest", str(d_agy)).returncode,
        )
        self.assertTrue((d_agy / ".agents" / "skills" / "shared-skill" / "SKILL.md").is_file())
        self.assertTrue((d_agy / ".agents" / "skills" / "shared-skill" / "helper.py").is_file())

    def test_crlf_preserved(self):
        """CRLF newlines are strictly preserved through import and materialize."""
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-crlf-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        crlf_rule = fixture_dir / ".claude" / "rules" / "windows.md"
        crlf_rule.parent.mkdir(parents=True)
        raw_crlf = b"# Windows Rule\r\nline1\r\nline2\r\n"
        crlf_rule.write_bytes(raw_crlf)

        imp = self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))
        self.assertEqual(0, imp.returncode, output(imp))

        rule_current = self.repo.root / ".aisdlc" / "catalogs" / "rules" / "windows" / "current.json"
        data = json.loads(rule_current.read_text(encoding="utf-8"))
        self.assertEqual("# Windows Rule\r\nline1\r\nline2\r\n", data["files"]["windows.md"])

        dest_dir = Path(tempfile.mkdtemp(prefix="dest-crlf-"))
        self.addCleanup(shutil.rmtree, dest_dir, ignore_errors=True)
        mat = self.repo.run("catalog", "materialize", "--harness", "claude-code", "--dest", str(dest_dir))
        self.assertEqual(0, mat.returncode, output(mat))
        self.assertEqual(raw_crlf, (dest_dir / ".claude" / "rules" / "windows.md").read_bytes())

    def test_non_utf8_file_skipped_and_reported(self):
        """Non-UTF-8 files are skipped and reported during import."""
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-nonutf8-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        skill_dir = fixture_dir / ".claude" / "skills" / "binary-skill"
        skill_dir.mkdir(parents=True)
        (skill_dir / "SKILL.md").write_text("# Valid\n", encoding="utf-8")
        (skill_dir / "invalid.bin").write_bytes(b"\xff\xfe\x00\x12\x80")

        imp = self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))
        out = output(imp)
        self.assertIn("invalid.bin", out)

        current = self.repo.root / ".aisdlc" / "catalogs" / "skills" / "binary-skill" / "current.json"
        self.assertTrue(current.is_file())
        data = json.loads(current.read_text(encoding="utf-8"))
        self.assertIn("SKILL.md", data["files"])
        self.assertNotIn("invalid.bin", data["files"])

    def test_changed_file_creates_revision_2_on_reimport(self):
        """A modified file creates revision 2 on re-import."""
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-rev2-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        rule_file = fixture_dir / ".claude" / "rules" / "simple.md"
        rule_file.parent.mkdir(parents=True)
        rule_file.write_text("# Simple v1\n", encoding="utf-8")

        self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))
        entry_dir = self.repo.root / ".aisdlc" / "catalogs" / "rules" / "simple"
        current_v1 = json.loads((entry_dir / "current.json").read_text(encoding="utf-8"))
        self.assertEqual(1, current_v1["version"])

        rule_file.write_text("# Simple v2\n", encoding="utf-8")
        self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))
        current_v2 = json.loads((entry_dir / "current.json").read_text(encoding="utf-8"))
        self.assertEqual(2, current_v2["version"])
        self.assertTrue((entry_dir / "versions" / "1.json").is_file())
        self.assertTrue((entry_dir / "versions" / "2.json").is_file())

    def test_check_stale_entry_reported_and_exits_1(self):
        """--check detects stale entries from prior manifest and exits 1 without deleting files."""
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-stale-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        agent_file = fixture_dir / ".claude" / "agents" / "retiring.md"
        agent_file.parent.mkdir(parents=True)
        agent_file.write_text("# Retiring Agent\n", encoding="utf-8")

        self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))

        dest_dir = Path(tempfile.mkdtemp(prefix="dest-stale-"))
        self.addCleanup(shutil.rmtree, dest_dir, ignore_errors=True)
        mat = self.repo.run("catalog", "materialize", "--harness", "claude-code", "--dest", str(dest_dir))
        self.assertEqual(0, mat.returncode, output(mat))

        # Remove from catalog
        self.repo.run("catalog", "remove", "--kind", "agents", "--id", "retiring")

        # --check detects stale entry
        chk = self.repo.run(
            "catalog", "materialize", "--harness", "claude-code", "--dest", str(dest_dir), "--check"
        )
        self.assertEqual(1, chk.returncode, output(chk))
        self.assertIn("stale", output(chk).lower())
        # Stale file must NOT be deleted
        self.assertTrue((dest_dir / ".claude" / "agents" / "retiring.md").is_file())

    def test_manifest_content_shape(self):
        """The manifest schema version 1, harness name, and sorted entries with per-file SHA256."""
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-shape-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        rule_b = fixture_dir / ".claude" / "rules" / "beta.md"
        rule_b.parent.mkdir(parents=True)
        rule_b.write_text("# Rule Beta\n", encoding="utf-8")

        rule_a = fixture_dir / ".claude" / "rules" / "alpha.md"
        rule_a.write_text("# Rule Alpha\n", encoding="utf-8")

        self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))

        dest_dir = Path(tempfile.mkdtemp(prefix="dest-shape-"))
        self.addCleanup(shutil.rmtree, dest_dir, ignore_errors=True)
        self.repo.run("catalog", "materialize", "--harness", "claude-code", "--dest", str(dest_dir))

        manifest_file = dest_dir / ".aisdlc-materialized.json"
        manifest = json.loads(manifest_file.read_text(encoding="utf-8"))
        self.assertEqual(1, manifest["schema"])
        self.assertEqual("claude-code", manifest["harness"])
        entries = manifest["entries"]
        self.assertEqual(["alpha", "beta"], [e["id"] for e in entries])
        for e in entries:
            self.assertEqual("rules", e["kind"])
            self.assertEqual(1, e["version"])
            self.assertTrue(isinstance(e["digest"], str) and len(e["digest"]) == 64)
            for path, h in e["files"].items():
                self.assertTrue(path.startswith(".claude/rules/"))
                self.assertEqual(64, len(h))

    def test_secret_fixture_rejected_while_other_entries_import(self):
        """A secret entry is rejected and not stored, while clean entries are imported."""
        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-secret-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        good_agent = fixture_dir / ".claude" / "agents" / "good-agent.md"
        good_agent.parent.mkdir(parents=True)
        good_agent.write_text("# Good Agent\n", encoding="utf-8")

        bad_token = "gh" + "p_" + "Z" * 30
        bad_agent = fixture_dir / ".claude" / "agents" / "bad-agent.md"
        bad_agent.write_text(f"# Bad Agent\nsecret: {bad_token}\n", encoding="utf-8")

        imp = self.repo.run("catalog", "import", "--harness", "claude-code", "--from", str(fixture_dir))
        self.assertEqual(1, imp.returncode, output(imp))

        # Good agent IS stored
        self.assertTrue((self.repo.root / ".aisdlc" / "catalogs" / "agents" / "good-agent" / "current.json").is_file())
        # Bad agent is NOT stored
        self.assertFalse((self.repo.root / ".aisdlc" / "catalogs" / "agents" / "bad-agent" / "current.json").exists())

    def test_guard_files_unit_tests(self):
        """catalogs.guard_files validates relative paths, types, and rejects literal secrets."""
        self.assertTrue(hasattr(catalogs, "guard_files"), "catalogs.guard_files must exist")

        # Escaping relative path
        with self.assertRaises(KitError):
            catalogs.guard_files({"../escaped.txt": "valid text"})

        # Absolute path
        with self.assertRaises(KitError):
            catalogs.guard_files({"/abs/path.txt": "valid text"})

        # Non-string value
        with self.assertRaises(KitError):
            catalogs.guard_files({"file.txt": 12345})  # type: ignore[arg-type]

        with self.assertRaises(KitError):
            catalogs.guard_files({"file.txt": b"bytes"})  # type: ignore[arg-type]

        # Non-string key
        with self.assertRaises(KitError):
            catalogs.guard_files({123: "text"})  # type: ignore[dict-item]

        # Secret value
        secret = "gh" + "p_" + "Q" * 25
        with self.assertRaises(KitError) as ctx:
            catalogs.guard_files({"file.txt": f"token = {secret}"})
        self.assertEqual("literal_secret", ctx.exception.code)
        self.assertIn("file.txt", str(ctx.exception))
        self.assertNotIn(secret, str(ctx.exception))

        # Valid files
        catalogs.guard_files({"SKILL.md": "# Clean", "nested/doc.txt": "Hello"})

    def test_catalogs_validate_flags_malformed_files(self):
        """catalogs.validate flags entries with malformed files dictionary."""
        self.repo.run("catalog", "add", "--kind", "rules", "--id", "malformed-rule", "--summary", "demo")
        entry_file = self.repo.root / ".aisdlc" / "catalogs" / "rules" / "malformed-rule" / "current.json"
        data = json.loads(entry_file.read_text(encoding="utf-8"))
        data["files"] = {"../escaped.txt": "malicious"}
        data["digest"] = catalogs.compute_digest(data)
        entry_file.write_text(json.dumps(data), encoding="utf-8")

        val = self.repo.run("catalog", "validate", "--kind", "rules")
        self.assertNotEqual(0, val.returncode, output(val))
        self.assertIn("malformed-rule", output(val))

    def test_materialize_layouts_table_and_kinds(self):
        """materialize.LAYOUTS defines required harness layouts and excludes prompts/hooks."""
        from aisdlc import materialize

        self.assertTrue(hasattr(materialize, "LAYOUTS"))
        layouts = materialize.LAYOUTS
        expected_harnesses = {"claude-code", "cursor", "codex", "junie", "kiro", "agy"}
        self.assertEqual(expected_harnesses, set(layouts.keys()))

        self.assertIn("skills", layouts["claude-code"])
        self.assertIn("agents", layouts["claude-code"])
        self.assertIn("rules", layouts["claude-code"])
        self.assertIn("rules", layouts["cursor"])
        self.assertIn("skills", layouts["codex"])
        self.assertIn("rules", layouts["junie"])
        self.assertIn("rules", layouts["kiro"])
        self.assertIn("skills", layouts["agy"])

        for h, kinds in layouts.items():
            self.assertNotIn("prompts", kinds, f"prompts must not have layout in {h}")
            self.assertNotIn("hooks", kinds, f"hooks must not have layout in {h}")

    def test_telemetry_events_on_import_and_materialize(self):
        """Telemetry: import and materialize emit exactly-named events with safe attributes only."""
        # Import aisdlc.materialize inside the test body so that a missing module raises
        # ImportError here rather than at collection time (the failure must be the feature gap).
        import aisdlc.materialize  # noqa: F401 — intentional: fail here if module missing

        # Import main after the probe above so the error location is unambiguous.
        from aisdlc.cli import main

        fixture_dir = Path(tempfile.mkdtemp(prefix="fixture-tel-"))
        self.addCleanup(shutil.rmtree, fixture_dir, ignore_errors=True)

        agent_file = fixture_dir / ".claude" / "agents" / "tel-agent.md"
        agent_file.parent.mkdir(parents=True)
        secret_body = "SuperSecretTelemetryBodyShouldNotLeak"
        agent_file.write_text(f"# Tel Agent\n{secret_body}\n", encoding="utf-8")

        dest_dir = Path(tempfile.mkdtemp(prefix="dest-tel-"))
        self.addCleanup(shutil.rmtree, dest_dir, ignore_errors=True)

        mem = MemoryTelemetry()

        profile_path = self.repo.root / "aisdlc.toml"

        # Run import in-process, intercepting get_telemetry inside materialize.py
        with unittest.mock.patch("aisdlc.materialize.get_telemetry", return_value=mem):
            rc_import = main([
                "--root", str(self.repo.root),
                "--profile", str(profile_path),
                "catalog", "import",
                "--harness", "claude-code",
                "--from", str(fixture_dir),
            ])

        self.assertEqual(0, rc_import, "catalog import must exit 0 for telemetry test")

        # Run materialize in-process
        with unittest.mock.patch("aisdlc.materialize.get_telemetry", return_value=mem):
            rc_mat = main([
                "--root", str(self.repo.root),
                "--profile", str(profile_path),
                "catalog", "materialize",
                "--harness", "claude-code",
                "--dest", str(dest_dir),
            ])

        self.assertEqual(0, rc_mat, "catalog materialize must exit 0 for telemetry test")

        events = mem.events()
        event_names = [e["name"] for e in events]

        self.assertIn("aisdlc.catalog.import", event_names, f"events: {event_names}")
        self.assertIn("aisdlc.catalog.materialize", event_names, f"events: {event_names}")

        for ev in events:
            if ev["name"] not in ("aisdlc.catalog.import", "aisdlc.catalog.materialize"):
                continue
            attrs = ev["attributes"]
            # Must carry count/digest/harness shape
            self.assertIn("harness", attrs, f"harness missing in {ev['name']} attrs")
            self.assertEqual("claude-code", attrs["harness"])
            # Allowed numeric attributes: entries, files
            for key in ("entries", "files"):
                if key in attrs:
                    self.assertIsInstance(attrs[key], int, f"{key} must be int")
            # No file body text anywhere in the JSON payload
            raw = json.dumps(ev)
            self.assertNotIn(secret_body, raw, f"{ev['name']} leaks file body content")
            # No absolute paths in the event payload
            self.assertNotIn(str(fixture_dir), raw, f"{ev['name']} leaks source absolute path")
            self.assertNotIn(str(dest_dir), raw, f"{ev['name']} leaks dest absolute path")


if __name__ == "__main__":
    unittest.main()
