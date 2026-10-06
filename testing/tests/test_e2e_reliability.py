#!/usr/bin/env python3
"""
Static Gate 2 checks for #1066 / CU76, moved from the frontend Vitest suite with the
Playwright suite (#1192): reliability rules asserted on the E2E sources and config so
false-green skips and sleep/retry budgets fail CI.

Plain stdlib unittest. Run with: python3 testing/tests/test_e2e_reliability.py
"""
import re
import unittest
from pathlib import Path

E2E_ROOT = Path(__file__).resolve().parents[1] / "e2e"
TESTS_DIR = E2E_ROOT / "tests"

HOTSPOT_SPECS = (
    "TS-0040-l10n-language-switching-qa.spec.ts",
    "TS-0043-icons-ux-qa.spec.ts",
    "TS-0021-workflow-editor-admin.spec.ts",
    "TS-0022-workflow-assignment-admin.spec.ts",
)
DEMO_SPECS = (
    "TS-0071-first-case-tutorial-onboarding.spec.ts",
    "TS-0090-demo-two-full-cases.spec.ts",
)
FEATURE_GAP_SKIPS = {
    "TS-0014-pagos-workflow.spec.ts": 2,
    "TS-0016-usuarios-escribanos-workflow.spec.ts": 3,
    "TS-0017-suplencias-workflow.spec.ts": 2,
    "TS-0020-reportes-admin-workflow.spec.ts": 7,
}
FEATURE_GAP_TOTAL = 14
MAX_DEFAULT_TIMEOUT_MS = 120_000

SKIP_BLOCK = re.compile(r"test\.skip\s*\(\s*[\"'`][\s\S]*?[\"'`]\s*,")
BARE_SKIP = re.compile(r"test\.skip\s*\(\s*\)")


def read_spec(name):
    return (TESTS_DIR / name).read_text(encoding="utf-8")


def read_config():
    return (E2E_ROOT / "playwright.config.ts").read_text(encoding="utf-8")


class RetryAndArtifactsTest(unittest.TestCase):
    def test_ci_retries_are_at_most_one(self):
        config = read_config()
        self.assertRegex(config, r"retries:\s*process\.env\.CI\s*\?\s*1\s*:\s*0")
        self.assertNotRegex(config, r"retries:\s*process\.env\.CI\s*\?\s*[2-9]")

    def test_flake_triage_artifacts_are_retained(self):
        config = read_config()
        self.assertRegex(config, r"""trace:\s*["']on-first-retry["']""")
        self.assertRegex(config, r"""screenshot:\s*["']only-on-failure["']""")
        self.assertRegex(config, r"""video:\s*["']retain-on-failure["']""")

    def test_default_timeout_stays_below_the_hang_mask(self):
        match = re.search(r"^\s*timeout:\s*(\d+)", read_config(), re.MULTILINE)
        self.assertIsNotNone(match)
        self.assertLessEqual(int(match.group(1)), MAX_DEFAULT_TIMEOUT_MS)


class SleepAndSkipRulesTest(unittest.TestCase):
    def test_hotspot_specs_do_not_use_wait_for_timeout(self):
        for name in HOTSPOT_SPECS:
            with self.subTest(spec=name):
                self.assertNotRegex(read_spec(name), r"waitForTimeout\s*\(")

    def test_demo_pauses_are_gated_behind_headed_or_slow_mo(self):
        for name in DEMO_SPECS:
            source = read_spec(name)
            if "waitForTimeout" not in source:
                continue
            with self.subTest(spec=name):
                self.assertRegex(source, r"HEADED|SLOW_MO")

    def test_workflow_admin_specs_arrange_data_and_do_not_skip(self):
        editor = read_spec("TS-0021-workflow-editor-admin.spec.ts")
        assignment = read_spec("TS-0022-workflow-assignment-admin.spec.ts")
        self.assertRegex(editor, r"createWorkflowDefinition")
        self.assertNotRegex(editor, BARE_SKIP)
        self.assertRegex(assignment, r"createTipoTramite")
        self.assertNotRegex(assignment, BARE_SKIP)

    def test_editor_validation_assert_avoids_the_toast_union(self):
        editor = read_spec("TS-0021-workflow-editor-admin.spec.ts")
        self.assertRegex(editor, r"""getByTestId\(\s*["']validation-errors["']\s*\)""")
        self.assertNotRegex(editor, r"""locator\(\s*["']\[data-sonner-toast\]["']\s*\)\s*\.or\(""")
        self.assertNotRegex(editor, r"""\.or\(\s*page\.getByTestId\(\s*["']validation-errors["']""")


class FeatureGapSkipInventoryTest(unittest.TestCase):
    def test_skip_counts_match_the_documented_inventory(self):
        for name, expected in FEATURE_GAP_SKIPS.items():
            with self.subTest(spec=name):
                self.assertEqual(expected, len(SKIP_BLOCK.findall(read_spec(name))))

    def test_total_feature_gap_skips_is_fourteen(self):
        total = sum(len(SKIP_BLOCK.findall(read_spec(name))) for name in FEATURE_GAP_SKIPS)
        self.assertEqual(FEATURE_GAP_TOTAL, total)

    def test_every_skip_cites_an_issue(self):
        for name in FEATURE_GAP_SKIPS:
            for block in SKIP_BLOCK.findall(read_spec(name)):
                with self.subTest(spec=name):
                    self.assertRegex(block, r"#\d+")

    def test_cu21_skip_does_not_reappear_in_ts_0016(self):
        source = read_spec("TS-0016-usuarios-escribanos-workflow.spec.ts")
        self.assertNotRegex(source, r"""test\.skip\s*\(\s*["'`][^"'`]*CU21[^"'`]*["'`]""")


class ApiPayloadFieldsTest(unittest.TestCase):
    def test_no_e2e_source_sends_the_stale_notary_field(self):
        offenders = [str(path.relative_to(E2E_ROOT)) for path in TESTS_DIR.rglob("*.ts")
                     if "fkIdNotaryPerson" in path.read_text(encoding="utf-8")]
        self.assertEqual([], offenders, "the gestiones API reads notaryPersonId, not fkIdNotaryPerson")

    def test_gestion_helper_sends_the_notary_field_the_api_reads(self):
        helpers = (TESTS_DIR / "setup" / "api-helpers.ts").read_text(encoding="utf-8")
        body = helpers[helpers.index("export async function createGestionSinTramite"):]
        self.assertIn("notaryPersonId", body[: body.index("\n}\n")])


class InventorySanityTest(unittest.TestCase):
    def test_hotspot_files_exist(self):
        missing = [name for name in HOTSPOT_SPECS if not (TESTS_DIR / name).is_file()]
        self.assertEqual([], missing)


if __name__ == "__main__":
    unittest.main()
