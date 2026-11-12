"""Factory control OpenAPI 3.1.0 contract (#2136)."""
from __future__ import annotations

import sys
import unittest

from helpers import KIT, Repo, output


class FactoryContractTest(unittest.TestCase):
    def setUp(self) -> None:
        sys.path.insert(0, str(KIT))
        from aisdlc import contract  # noqa: E402

        self.contract = contract

    def test_contract_is_openapi_3_1_0_with_required_paths(self):
        errors = self.contract.validate_contract(KIT)
        self.assertEqual([], errors)

    def test_contract_does_not_import_product_openapi(self):
        text = self.contract.load_contract_text(KIT)
        self.assertNotIn("backend" + "-api", text)
        self.assertNotIn("/v3/api-docs", text)
        self.assertIn("openapi: 3.1.0", text)

    def test_cli_config_json_covers_contract_required_keys(self):
        from aisdlc.factory import config_view  # noqa: E402
        from aisdlc.profile import Profile  # noqa: E402

        view = config_view(Profile(name="demo", max_open_prs=3), limit=3)
        self.assertEqual([], self.contract.config_keys_match_contract(view, KIT))

    def test_contract_check_cli(self):
        repo = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\n')
        repo.write("POLICY.md", "# P\n")
        result = repo.run("contract", "check")
        self.assertEqual(0, result.returncode, output(result))
        self.assertIn("3.1.0", output(result))

    def test_accept_dir_exists_for_oasdiff(self):
        path = self.contract.accept_dir(KIT)
        self.assertTrue(path.is_dir())
        self.assertTrue((path / "README.md").is_file())

    def test_phase_a_entity_and_workflow_schemas_present(self):
        """#2239: OpenAPI must describe FactoryEntity / EntityVersion / Workflow."""
        text = self.contract.load_contract_text(KIT)
        for marker in (
            "FactoryEntity",
            "EntityVersion",
            "Workflow",
            "WorkflowGovernanceState",
            "workflows",
            "providers",
            "integrations",
            "rules",
            "/factory/workflows",
        ):
            self.assertIn(marker, text)

    def test_phase_b_backoffice_uses_same_contract_paths(self):
        """#2240/#2241: contract notes backoffice adapts catalog/workflow/evidence paths (no second SoR)."""
        text = self.contract.load_contract_text(KIT)
        self.assertIn("backoffice", text.lower())
        self.assertIn("Phase C", text)
        self.assertIn("/factory/workflows", text)
        self.assertIn("/factory/catalogs/{kind}", text)
        # A.4 declares cancel; honesty is HTTP 501 while runner is false (#2255).
        self.assertIn("/factory/runs/{id}/cancel", text)
        repo = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\n')
        repo.write("POLICY.md", "# P\n")
        result = repo.run("contract", "check")
        self.assertEqual(0, result.returncode, output(result))

    def test_phase_a2_capabilities_path_single_tree(self):
        """#2253: additive capabilities path; still one OpenAPI document."""
        text = self.contract.load_contract_text(KIT)
        self.assertIn("/factory/capabilities", text)
        self.assertIn("FactoryCapabilities", text)
        self.assertIn("aisdlc serve", text)
        yaml_files = list((KIT / "openapi").glob("*.yaml")) + list((KIT / "openapi").glob("*.yml"))
        self.assertEqual(1, len(yaml_files))
        self.assertEqual([], self.contract.validate_contract(KIT))

    def test_phase_a3_if_match_and_schema_fields(self):
        """#2254: If-Match + schema_version/created_by; still one OpenAPI tree."""
        text = self.contract.load_contract_text(KIT)
        self.assertIn("If-Match", text)
        self.assertIn("schema_version", text)
        self.assertIn("created_by", text)
        self.assertIn("WorkflowBodyV2", text)
        yaml_files = list((KIT / "openapi").glob("*.yaml")) + list((KIT / "openapi").glob("*.yml"))
        self.assertEqual(1, len(yaml_files))

    def test_phase_a4_project_run_schemas(self):
        """#2255: Project / Run / RunRequest schemas and paths; still one tree."""
        text = self.contract.load_contract_text(KIT)
        for marker in (
            "Project",
            "Run",
            "RunRequest",
            "/factory/projects",
            "/factory/runs",
            "/factory/runs/{id}/events",
            "/factory/runs/{id}/cancel",
        ):
            self.assertIn(marker, text)
        yaml_files = list((KIT / "openapi").glob("*.yaml")) + list((KIT / "openapi").glob("*.yml"))
        self.assertEqual(1, len(yaml_files))
        self.assertEqual([], self.contract.validate_contract(KIT))


if __name__ == "__main__":
    unittest.main()
