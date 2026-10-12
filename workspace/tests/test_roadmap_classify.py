#!/usr/bin/env python3
"""Gate 2 tests for roadmap milestone and lane classification (CU76, #1248).

Run: python3 -m unittest workspace.tests.test_roadmap_classify
"""

from __future__ import annotations

import importlib.util
import json
import os
import sys
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
MODULE = os.path.join(REPO_ROOT, "workspace", "sdlc", "roadmap_classify.py")


def load_module():
    spec = importlib.util.spec_from_file_location("roadmap_classify", MODULE)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    sys.modules[spec.name] = mod
    spec.loader.exec_module(mod)
    return mod


def issue(**kwargs):
    base = {
        "number": 1,
        "title": "example",
        "state": "open",
        "milestone": "",
        "closed": "",
        "created": "2026-10-01",
        "labels": [],
    }
    base.update(kwargs)
    return base


class RoadmapClassifyTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.m = load_module()

    def test_closed_issue_keeps_existing_milestone(self):
        got = self.m.classify(
            issue(
                state="closed",
                milestone="Phase 0 - Foundations",
                closed="2026-04-01",
                labels=["phase:4-frontend"],
            )
        )
        self.assertEqual(got.milestone, "Phase 0 - Foundations")
        self.assertEqual(got.reason, "preserve-assigned")
        self.assertFalse(got.claimable)

    def test_closed_unassigned_uses_phase_label_before_date(self):
        got = self.m.classify(
            issue(
                state="closed",
                closed="2026-10-02",
                labels=["phase:5-observability", "DEVOPS"],
            )
        )
        self.assertEqual(got.milestone, self.m.PHASE5_HIST)
        self.assertEqual(got.reason, "historical-phase-label")

    def test_closed_unassigned_date_cohorts(self):
        cases = [
            ("2026-05-15", self.m.PHASE1_HIST),
            ("2026-06-20", self.m.PHASE3_HIST),
            ("2026-08-01", self.m.PHASE4_HIST),
            ("2026-09-24", self.m.PHASE5_HIST),
            ("2026-10-11", self.m.CLOUD),
        ]
        for closed, expected in cases:
            with self.subTest(closed=closed):
                got = self.m.classify(issue(state="closed", closed=closed, labels=["bug"]))
                self.assertEqual(got.milestone, expected)
                self.assertEqual(got.reason, "historical-close-date")

    def test_functional_requirement_is_scope_not_delivery(self):
        got = self.m.classify(
            issue(
                number=3,
                title="RF-01: Gestionar Trámites",
                labels=["requerimiento-funcional", "BACKEND", "FRONTEND"],
            )
        )
        self.assertEqual(got.milestone, self.m.SCOPE_RF)
        self.assertEqual(got.lane, "lane:fullstack")
        self.assertFalse(got.claimable)

    def test_use_case_catalog_is_scope(self):
        got = self.m.classify(
            issue(
                number=153,
                title="CU01: Preparar Presupuesto",
                labels=["DOC", "CASO-DE-USO"],
            )
        )
        self.assertEqual(got.milestone, self.m.SCOPE_CU)
        self.assertEqual(got.lane, "lane:docs")
        self.assertFalse(got.claimable)

    def test_nfr_catalog_records_delivery_assessment(self):
        security = self.m.classify(
            issue(number=82, title="RNF-12: Seguridad y privacidad", labels=["requerimiento-no-funcional"])
        )
        perf = self.m.classify(
            issue(number=72, title="RNF-03: Tiempo de respuesta", labels=["requerimiento-no-funcional"])
        )
        ux = self.m.classify(
            issue(number=75, title="RNF-05: Aspecto Visual", labels=["FRONTEND", "requerimiento-no-funcional"])
        )
        platform = self.m.classify(
            issue(number=85, title="RNF-17: Java VM", labels=["requerimiento-no-funcional"])
        )
        self.assertEqual(security.milestone, self.m.SCOPE_NFR)
        self.assertEqual(security.nfr_theme, "security")
        self.assertEqual(security.nfr_train, self.m.V10)
        self.assertEqual(perf.nfr_theme, "performance")
        self.assertEqual(perf.nfr_train, self.m.V11)
        self.assertEqual(ux.nfr_theme, "experience")
        self.assertEqual(ux.nfr_train, self.m.PHASE4)
        self.assertEqual(platform.nfr_theme, "platform")
        self.assertEqual(platform.nfr_train, self.m.BACKLOG)
        self.assertFalse(security.claimable)

    def test_phase4_roadmap_umbrella_stays_on_phase4(self):
        got = self.m.classify(
            issue(
                number=898,
                title="📋 [ROADMAP] Phase 4 — Modern Frontend: complete 73 use cases in Next.js",
                milestone=self.m.V10,
                labels=["FRONTEND", "CASO-DE-USO", "priority:high", "phase:4-frontend", "roadmap"],
            )
        )
        self.assertEqual(got.milestone, self.m.PHASE4)
        self.assertTrue(got.claimable)
        self.assertEqual(got.lane, "lane:frontend")

    def test_phase6_and_jpa_debt_leave_priority_bucket(self):
        umbrella = self.m.classify(
            issue(
                number=899,
                title="📋 [ROADMAP] Phase 6 — Deprecation: retire Swing + jpa package cleanup",
                milestone=self.m.V11,
                labels=["DEVOPS", "REFACTOR", "priority:medium", "phase:6-deprecation", "tech-debt", "roadmap"],
            )
        )
        jpa = self.m.classify(
            issue(
                number=576,
                title="arch(jpa): retire legacy jpa package (~10k LOC / 27 classes)",
                milestone=self.m.V10,
                labels=["BACKEND", "TAREA", "priority:high", "tech-debt"],
            )
        )
        critical = self.m.classify(
            issue(
                number=574,
                title="arch(api): controllers instantiate JPA controllers directly",
                labels=["BACKEND", "TAREA", "priority:critical", "tech-debt"],
            )
        )
        self.assertEqual(umbrella.milestone, self.m.PHASE6)
        self.assertEqual(jpa.milestone, self.m.PHASE6)
        self.assertEqual(critical.milestone, self.m.V10)

    def test_frontend_product_slice_is_phase4_even_without_priority(self):
        got = self.m.classify(
            issue(
                number=1553,
                title="[UI] feat(managements): show the current status card",
                labels=["enhancement", "FRONTEND"],
            )
        )
        self.assertEqual(got.milestone, self.m.PHASE4)
        self.assertEqual(got.lane, "lane:frontend")
        self.assertTrue(got.claimable)

    def test_critical_frontend_security_stays_on_v10(self):
        got = self.m.classify(
            issue(
                title="security(frontend): JWT in localStorage",
                labels=["FRONTEND", "priority:high", "security", "audit-2026-09"],
            )
        )
        self.assertEqual(got.milestone, self.m.V10)
        self.assertEqual(got.lane, "lane:security")

    def test_priority_buckets_for_non_frontend_work(self):
        high = self.m.classify(issue(title="docs(ops): runbook", labels=["DOC", "priority:high"]))
        medium = self.m.classify(issue(title="perf(api): pagination", labels=["BACKEND", "priority:medium"]))
        low = self.m.classify(issue(title="chore: tls between metrics", labels=["DEVOPS", "priority:low"]))
        self.assertEqual(high.milestone, self.m.V10)
        self.assertEqual(medium.milestone, self.m.V11)
        self.assertEqual(low.milestone, self.m.BACKLOG)
        self.assertEqual(high.lane, "lane:docs")
        self.assertEqual(medium.lane, "lane:backend")
        self.assertEqual(low.lane, "lane:devops")

    def test_audit_finding_is_the_security_lane(self):
        got = self.m.classify(
            issue(
                title="security(deps): fix open Dependabot alerts",
                labels=["DEVOPS", "priority:high", "audit-2026-09"],
            )
        )
        self.assertEqual(got.milestone, self.m.V10)
        self.assertEqual(got.lane, "lane:security")

    def test_unscoped_noise_is_backlog_owner_lane(self):
        got = self.m.classify(issue(number=1440, title="test-permission", labels=[]))
        self.assertEqual(got.milestone, self.m.BACKLOG)
        self.assertEqual(got.lane, "lane:owner")

    def test_exactly_one_lane_from_known_set(self):
        got = self.m.classify(
            issue(title="both", labels=["BACKEND", "FRONTEND", "priority:medium", "enhancement"])
        )
        self.assertIn(got.lane, self.m.LANES)
        self.assertEqual(got.lane, "lane:fullstack")

    def test_summarize_counts_open_and_closed(self):
        rows = [
            self.m.classify(issue(number=1, state="closed", closed="2026-06-02", labels=["BACKEND"])),
            self.m.classify(issue(number=2, title="RF-01: X", labels=["requerimiento-funcional"])),
            self.m.classify(issue(number=3, title="ui", labels=["FRONTEND", "enhancement"])),
        ]
        summary = self.m.summarize(rows)
        self.assertEqual(summary["issues"], 3)
        self.assertEqual(summary["by_milestone"][self.m.PHASE3_HIST]["closed"], 1)
        self.assertEqual(summary["by_milestone"][self.m.SCOPE_RF]["open"], 1)
        self.assertEqual(summary["by_milestone"][self.m.PHASE4]["open"], 1)
        self.assertEqual(summary["claimable_open"], 1)

    def test_snapshot_matches_pages_copy(self):
        docs = os.path.join(
            REPO_ROOT,
            "docs",
            "300-development",
            "305-project-management",
            "roadmap-snapshot.json",
        )
        pages = os.path.join(REPO_ROOT, "github-page", "lib", "roadmap-snapshot.json")
        with open(docs, encoding="utf-8") as handle:
            docs_text = handle.read()
        with open(pages, encoding="utf-8") as handle:
            pages_text = handle.read()
        self.assertEqual(docs_text, pages_text)
        snapshot = json.loads(docs_text)
        self.assertEqual(snapshot["issues"], snapshot["open"] + snapshot["closed"])
        lane_total = sum(snapshot["by_lane_claimable_open"].values())
        self.assertEqual(lane_total, snapshot["claimable_open"])
        milestone_total = sum(
            row["open"] + row["closed"] for row in snapshot["by_milestone"]
        )
        self.assertEqual(milestone_total, snapshot["issues"])
        page = os.path.join(REPO_ROOT, "github-page", "app", "docs", "roadmap", "page.tsx")
        chrome = os.path.join(REPO_ROOT, "github-page", "components", "DocsChrome.tsx")
        with open(page, encoding="utf-8") as handle:
            page_text = handle.read()
        with open(chrome, encoding="utf-8") as handle:
            chrome_text = handle.read()
        self.assertIn("/docs/roadmap/", chrome_text)
        self.assertIn("roadmap-snapshot.json", page_text)
        self.assertIn("lane:security", page_text)
        self.assertGreater(snapshot["by_lane_claimable_open"]["lane:frontend"], 0)
        agent = os.path.join(REPO_ROOT, ".claude", "agents", "project-manager.md")
        skill = os.path.join(REPO_ROOT, ".claude", "skills", "project-management", "SKILL.md")
        self.assertTrue(os.path.isfile(agent))
        self.assertTrue(os.path.isfile(skill))


if __name__ == "__main__":
    unittest.main()
