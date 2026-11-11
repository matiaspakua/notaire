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
        self.assertNotIn("/factory/runs/{id}/cancel", text)  # cancel stays out until ports honest
        repo = Repo(self, '[project]\nname = "demo"\npolicy = "POLICY.md"\n')
        repo.write("POLICY.md", "# P\n")
        result = repo.run("contract", "check")
        self.assertEqual(0, result.returncode, output(result))


if __name__ == "__main__":
    unittest.main()
