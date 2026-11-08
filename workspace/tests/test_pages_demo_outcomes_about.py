#!/usr/bin/env python3
"""Guards #1985: the Pages demo is read-only and secret-free, outcome numbers come from GitHub with
their window and method, and the About section states only sourced facts.

Run with: python3 workspace/tests/test_pages_demo_outcomes_about.py
"""
from __future__ import annotations

import json
import re
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
PAGE = REPO / "github-page"
FIXTURES = PAGE / "demo" / "fixtures.json"
METRICS = PAGE / "lib" / "delivery-metrics.json"
ABOUT = PAGE / "lib" / "about.json"
SECRET_KEY = re.compile(r"pass(word)?|token|secret|hash|jwt|credential", re.I)
JWT = re.compile(r"eyJ[\w-]{10,}\.[\w-]{10,}")
BCRYPT = re.compile(r"\$2[aby]\$\d\d\$")
METRIC_KEYS = ("lead_time_hours", "throughput", "change_failure", "restore_main_hours", "first_push_ci",
               "issues_closed", "releases")


def walk(node, key=""):
    if isinstance(node, dict):
        for k, v in node.items():
            yield from walk(v, k)
    elif isinstance(node, list):
        for v in node:
            yield from walk(v, key)
    else:
        yield key, node


class DemoFixtures(unittest.TestCase):
    def setUp(self):
        self.data = json.loads(FIXTURES.read_text())

    def test_recorded_by_the_script(self):
        self.assertTrue((PAGE / "demo" / "record-fixtures.mjs").exists())
        self.assertIn("record-fixtures.mjs", self.data["about"])
        self.assertRegex(self.data["recordedAt"], r"^\d{4}-\d\d-\d\d")

    def test_covers_login_and_the_dashboard(self):
        self.assertIn("POST /users/login", self.data["responses"])
        self.assertTrue(any(k.startswith("GET /managements") for k in self.data["responses"]))

    def test_holds_no_secrets(self):
        text = FIXTURES.read_text()
        self.assertIsNone(JWT.search(text), "fixtures contain a JWT")
        self.assertIsNone(BCRYPT.search(text), "fixtures contain a BCrypt hash")
        for key, value in walk(self.data["responses"]):
            if SECRET_KEY.search(key) and isinstance(value, str):
                self.assertEqual(value, "demo", f"{key} is not scrubbed")

    def test_never_records_a_401(self):
        for name, r in self.data["responses"].items():
            self.assertNotEqual(r["status"], 401, name)


class DemoBuild(unittest.TestCase):
    def test_shim_refuses_writes_and_never_answers_401(self):
        shim = (PAGE / "demo" / "demo-shim.js").read_text()
        self.assertIn("read-only", shim)
        self.assertNotRegex(shim, r"status:\s*401")

    def test_build_script_patches_with_assertions_and_adds_the_banner(self):
        build = (PAGE / "demo" / "build-demo.mjs").read_text()
        self.assertIn("/notaire/demo", build)
        self.assertIn("Demo – sample data, read-only", build)
        self.assertIn("demo-shim.js", build)
        self.assertRegex(build, r"throw new Error\(`build-demo: patch")

    def test_workflows_build_the_demo(self):
        for wf in ("pages-ci.yml", "deploy-github-page.yml"):
            text = (REPO / ".github" / "workflows" / wf).read_text()
            self.assertIn("npm run build:demo", text, wf)
            self.assertIn("frontend/package-lock.json", text, wf)
        pkg = json.loads((PAGE / "package.json").read_text())
        self.assertIn("build:demo", pkg["scripts"])
        # Local preflight must match pages-ci so /notaire/demo/ links resolve (#576 hygiene).
        preflight = (REPO / "workspace" / "sdlc" / "preflight.sh").read_text()
        self.assertIn("npm run build:demo", preflight)

    def test_demo_is_documented(self):
        doc = (PAGE / "docs" / "demo.md").read_text()
        for word in ("MSW", "fixtures", "read-only", "record-fixtures.mjs"):
            self.assertIn(word, doc)


class DeliveryMetrics(unittest.TestCase):
    def setUp(self):
        self.data = json.loads(METRICS.read_text())

    def test_snapshot_has_window_method_and_generator(self):
        self.assertRegex(self.data["window"]["from"], r"^\d{4}-\d\d-\d\d$")
        self.assertRegex(self.data["window"]["to"], r"^\d{4}-\d\d-\d\d$")
        self.assertIn("generate-delivery-metrics.py", self.data["generated_by"])
        for key in ("lead_time", "throughput", "change_failure", "restore_main", "first_push_ci", "issues_closed",
                    "releases"):
            self.assertTrue(self.data["method"].get(key), key)
        for key in METRIC_KEYS:
            self.assertIn(key, self.data)

    def test_unavailable_metrics_carry_a_reason_and_no_value(self):
        for key in ("restore_main_hours", "first_push_ci"):
            m = self.data[key]
            if m.get("available") is False:
                self.assertTrue(m.get("reason"), key)
                self.assertEqual(set(m), {"available", "reason"}, key)

    def test_deploy_refreshes_the_snapshot(self):
        deploy = (REPO / ".github" / "workflows" / "deploy-github-page.yml").read_text()
        self.assertIn("generate-delivery-metrics.py", deploy)
        self.assertLess(deploy.index("generate-delivery-metrics.py"), deploy.index("npm run build\n"))

    def test_site_reads_the_snapshot(self):
        lib = (PAGE / "lib" / "outcomes.ts").read_text()
        self.assertIn("delivery-metrics.json", lib)
        self.assertIn("<OutcomesSection", (PAGE / "app" / "ai-sdlc" / "results" / "page.tsx").read_text())
        component = (PAGE / "components" / "Outcomes.tsx").read_text()
        self.assertNotRegex(component, r"\b(584|598|0\.5|5\.5)\b", "outcome numbers typed into the component")


class About(unittest.TestCase):
    def setUp(self):
        self.data = json.loads(ABOUT.read_text())

    def test_every_fact_names_a_source(self):
        self.assertTrue(self.data["facts"])
        for f in self.data["facts"]:
            self.assertTrue(f["value"], f["label"])
            self.assertTrue(f["source"]["label"], f["label"])
            self.assertTrue(f["source"]["href"].startswith("https://"), f["label"])

    def test_placeholders_are_marked_and_say_what_to_fill(self):
        self.assertTrue(self.data["placeholders"])
        for p in self.data["placeholders"]:
            self.assertTrue(p["todo"].startswith("TODO(Owner):"), p)
            self.assertTrue(p["field"], p)

    def test_page_and_landing_use_the_config(self):
        self.assertIn("about.json", (PAGE / "lib" / "about.ts").read_text())
        self.assertIn("<AboutFacts", (PAGE / "app" / "about" / "page.tsx").read_text())
        self.assertIn('href: "/about/"', (PAGE / "lib" / "site.ts").read_text())


if __name__ == "__main__":
    unittest.main()
