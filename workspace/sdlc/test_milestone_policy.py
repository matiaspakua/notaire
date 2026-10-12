"""Scheduling rules for milestones, catalog assessment, and agent lanes."""

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from milestone_policy import (
    BACKLOG,
    DELIVERY_FILTER,
    FRONTEND,
    FULL_LIFECYCLE_FILTER,
    HISTORY_ERAS,
    LEGACY,
    MILESTONE_WINDOW,
    PLATFORM,
    QUALITY,
    RELEASE,
    RENAMES,
    SCHEDULED,
    VIEW_CONTRACTS,
    VIEW_FIELDS,
    classify_closed,
    classify_open,
    lane,
    lanes_blocked_by,
    suggested_priority,
)
from apply_milestone_policy import missing_project_issues


class ClassifyOpenTests(unittest.TestCase):
    def test_security_rnf_is_a_release_blocker(self):
        self.assertEqual(
            classify_open(81, "RNF-12: Seguridad y privacidad", ["requerimiento-no-funcional"]),
            RELEASE,
        )

    def test_visual_rnf_belongs_to_the_frontend_product(self):
        self.assertEqual(
            classify_open(74, "RNF-05: Aspecto Visual", ["requerimiento-no-funcional"]),
            FRONTEND,
        )

    def test_performance_and_process_rnfs_are_quality(self):
        self.assertEqual(
            classify_open(70, "RNF-01: Uso de Memoria RAM", ["requerimiento-no-funcional"]),
            QUALITY,
        )
        self.assertEqual(
            classify_open(89, "RNF-21: Metodología de desarrollo", ["requerimiento-no-funcional"]),
            QUALITY,
        )

    def test_environment_rnfs_stay_on_the_backlog(self):
        self.assertEqual(
            classify_open(85, "RNF-17: Java VM", ["requerimiento-no-funcional"]),
            BACKLOG,
        )
        self.assertEqual(
            classify_open(92, "RNF-24: Motor de Base de datos", ["requerimiento-no-funcional"]),
            BACKLOG,
        )

    def test_functional_requirements_and_use_cases_are_frontend_scope(self):
        self.assertEqual(
            classify_open(3, "RF-01: Gestionar Trámites", ["requerimiento-funcional"]),
            FRONTEND,
        )
        self.assertEqual(
            classify_open(153, "CU01: Preparar Presupuesto", ["CASO-DE-USO"]),
            FRONTEND,
        )

    def test_low_priority_wins_over_area(self):
        self.assertEqual(
            classify_open(254, "Configure HTTPS/TLS for production", ["DEVOPS", "priority:low"]),
            BACKLOG,
        )

    def test_critical_work_is_a_release_blocker_even_when_it_is_documentation(self):
        self.assertEqual(
            classify_open(264, "docs(db): migration guide", ["DOC", "priority:critical"]),
            RELEASE,
        )

    def test_critical_preempts_legacy_theme(self):
        self.assertEqual(
            classify_open(
                574,
                "arch(api): controllers instantiate JPA controllers directly",
                ["BACKEND", "priority:critical", "tech-debt"],
            ),
            RELEASE,
        )

    def test_jpa_retirement_is_the_legacy_milestone(self):
        self.assertEqual(
            classify_open(576, "arch(jpa): retire legacy jpa package", ["BACKEND", "priority:high", "tech-debt"]),
            LEGACY,
        )
        self.assertEqual(
            classify_open(899, "Phase 6 — Deprecation", ["roadmap", "phase:6-deprecation", "priority:medium"]),
            LEGACY,
        )

    def test_high_security_title_is_a_release_blocker(self):
        self.assertEqual(
            classify_open(655, "security(validation): Bean Validation", ["BACKEND", "bug", "priority:high"]),
            RELEASE,
        )

    def test_requirements_tracker_and_docs_audit_are_quality(self):
        self.assertEqual(
            classify_open(
                902,
                "Requirements tracker",
                ["requerimiento-funcional", "requerimiento-no-funcional", "DOC", "priority:high"],
            ),
            QUALITY,
        )
        self.assertEqual(
            classify_open(921, "Audit documentation", ["DOC", "CASO-DE-USO", "priority:high", "roadmap"]),
            QUALITY,
        )

    def test_frontend_product_issues_and_pure_frontend_work(self):
        self.assertEqual(
            classify_open(898, "Phase 4 frontend", ["FRONTEND", "roadmap", "priority:high"]),
            FRONTEND,
        )
        self.assertEqual(
            classify_open(611, "login page bypasses FormContainer", ["FRONTEND", "priority:medium"]),
            FRONTEND,
        )

    def test_frontend_tests_are_quality_not_product(self):
        self.assertEqual(
            classify_open(615, "form validation E2E", ["FRONTEND", "TEST", "priority:medium"]),
            QUALITY,
        )

    def test_devops_implementation_is_platform(self):
        self.assertEqual(
            classify_open(680, "Trivy is advisory-only", ["DEVOPS", "bug", "priority:medium"]),
            PLATFORM,
        )

    def test_devops_documentation_is_quality(self):
        self.assertEqual(
            classify_open(301, "Kubernetes deployment guide", ["DEVOPS", "DOC", "priority:medium"]),
            QUALITY,
        )

    def test_unlabeled_issues_are_backlog_and_not_pickable(self):
        self.assertEqual(classify_open(1440, "test-permission", []), BACKLOG)
        self.assertEqual(lane([], 1440, "test-permission"), "catalog")

    def test_legacy_documentation_label_counts_as_doc(self):
        self.assertEqual(classify_open(1418, "claim protocol", ["documentation"]), QUALITY)


class ClassifyClosedTests(unittest.TestCase):
    def test_keeps_an_existing_milestone(self):
        self.assertIsNone(classify_closed("2026-05-01T00:00:00Z", True))

    def test_groups_unmilestoned_history_by_close_date(self):
        self.assertEqual(classify_closed("2026-06-30T12:00:00Z", False), "History · 2026 Q2 — Foundations & API")
        self.assertEqual(classify_closed("2026-07-01T00:00:00Z", False), "History · 2026 Q3 — Product build")
        self.assertEqual(classify_closed("2026-09-30T00:00:00Z", False), "History · 2026 Q3 — Product build")
        self.assertEqual(classify_closed("2026-10-01T00:00:00Z", False), "History · 2026 Q4 — Cloud AI SDLC")


class RoadmapContractTests(unittest.TestCase):
    def test_full_lifecycle_view_requires_every_issue_on_the_project(self):
        issues = [{"number": 2}, {"number": 226}, {"number": 499}]
        self.assertEqual(
            [issue["number"] for issue in missing_project_issues(issues, {2: "item"})],
            [226, 499],
        )

    def test_every_history_and_delivery_milestone_has_a_roadmap_window(self):
        expected = set(SCHEDULED) | set(HISTORY_ERAS) | set(RENAMES.values())
        self.assertEqual(set(MILESTONE_WINDOW), expected)
        for start, target in MILESTONE_WINDOW.values():
            self.assertLessEqual(start, target)

    def test_full_lifecycle_and_delivery_filters_have_distinct_scope(self):
        self.assertNotIn("is:open", FULL_LIFECYCLE_FILTER)
        self.assertTrue(DELIVERY_FILTER.startswith("is:open "))
        for catalog_label in (
            "requerimiento-funcional",
            "requerimiento-no-funcional",
            "CASO-DE-USO",
        ):
            self.assertIn(f"-label:{catalog_label}", FULL_LIFECYCLE_FILTER)
            self.assertIn(f"-label:{catalog_label}", DELIVERY_FILTER)

    def test_view_contract_separates_history_from_execution(self):
        self.assertEqual(
            VIEW_CONTRACTS["Roadmap"],
            ("ROADMAP_LAYOUT", FULL_LIFECYCLE_FILTER),
        )
        self.assertEqual(
            VIEW_CONTRACTS["KANBAN"],
            ("BOARD_LAYOUT", DELIVERY_FILTER),
        )
        self.assertEqual(
            VIEW_CONTRACTS["Agent queue"],
            ("TABLE_LAYOUT", DELIVERY_FILTER),
        )

    def test_views_expose_only_the_fields_needed_for_their_job(self):
        self.assertNotIn("Roadmap", VIEW_FIELDS)
        self.assertIn("Priority", VIEW_FIELDS["Agent queue"])
        self.assertIn("Area", VIEW_FIELDS["Agent queue"])
        self.assertNotIn("Start", VIEW_FIELDS["KANBAN"])


class LaneTests(unittest.TestCase):
    def test_catalog_and_epics_are_not_pickable(self):
        self.assertEqual(lane(["requerimiento-funcional"], 3, "RF-01: Gestionar Trámites"), "catalog")
        self.assertEqual(
            lane(["requerimiento-funcional", "BACKEND", "FRONTEND"], 3, "RF-01: Gestionar Trámites"),
            "catalog",
        )
        self.assertEqual(lane(["roadmap", "FRONTEND"], 898, "Phase 4"), "epic")
        self.assertEqual(lane(["FRONTEND", "roadmap"], 1547, "[Epic] UI form gaps"), "epic")

    def test_cross_lane_when_both_products_change(self):
        self.assertEqual(
            lane(["BACKEND", "FRONTEND", "enhancement", "in-progress"], 1557, "filter payments"),
            "cross",
        )

    def test_single_area_lanes(self):
        self.assertEqual(lane(["FRONTEND", "enhancement"], 1546, "filter substitutions"), "frontend")
        self.assertEqual(lane(["BACKEND", "tech-debt"], 576, "retire jpa"), "backend")
        self.assertEqual(lane(["DEVOPS", "bug"], 680, "Trivy"), "platform")
        self.assertEqual(lane(["DOC", "priority:medium"], 603, "broken links"), "quality")

    def test_cross_and_security_block_the_backend_lane(self):
        self.assertEqual(lanes_blocked_by("cross"), frozenset({"cross", "frontend", "backend"}))
        self.assertEqual(lanes_blocked_by("security"), frozenset({"security", "backend"}))
        self.assertEqual(lanes_blocked_by("catalog"), frozenset())

    def test_documentation_with_devops_stays_on_the_quality_lane(self):
        self.assertEqual(
            lane(["DOC", "DEVOPS", "TAREA", "priority:high"], 1561, "docs(pm): schedule milestones"),
            "quality",
        )

    def test_security_lane_is_exclusive_of_backend(self):
        self.assertEqual(
            lane(["BACKEND", "priority:critical", "tech-debt"], 574, "JPA provider"),
            "security",
        )


class PriorityTests(unittest.TestCase):
    def test_keeps_an_existing_priority(self):
        self.assertIsNone(suggested_priority(3, "RF-01", ["requerimiento-funcional", "priority:low"], FRONTEND))

    def test_catalog_anchors_do_not_gain_a_priority(self):
        self.assertIsNone(suggested_priority(3, "RF-01", ["requerimiento-funcional"], FRONTEND))

    def test_active_ui_work_without_priority_becomes_high(self):
        self.assertEqual(
            suggested_priority(1546, "[UX] feat(substitutions)", ["FRONTEND", "enhancement", "in-progress"], FRONTEND),
            "priority:high",
        )

    def test_backlog_work_without_priority_becomes_low(self):
        self.assertEqual(
            suggested_priority(1440, "test-permission", ["chore"], BACKLOG),
            "priority:low",
        )


if __name__ == "__main__":
    unittest.main()
