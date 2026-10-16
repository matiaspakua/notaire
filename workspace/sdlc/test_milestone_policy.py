"""Scheduling rules for milestones, catalog assessment, and agent lanes."""

import re
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from milestone_policy import (
    BACKLOG,
    CATALOG,
    DELIVERY_FILTER,
    FRONTEND,
    FULL_LIFECYCLE_FILTER,
    HISTORY_ERAS,
    GA,
    HISTORY_STAGES,
    MILESTONE_DESCRIPTION,
    MILESTONE_DUE,
    MILESTONE_WINDOW,
    PLATFORM,
    QUALITY,
    RELEASE,
    RENAMES,
    SCHEDULED,
    SHIPPED,
    VIEW_CONTRACTS,
    VIEW_FIELDS,
    classify_closed,
    classify_open,
    is_history,
    lane,
    lanes_blocked_by,
    shipped_train,
    suggested_priority,
)
from apply_milestone_policy import (
    create_body,
    milestone_payload,
    missing_project_issues,
    retired_milestones,
    stale_views,
    target_for,
)


class ClassifyOpenTests(unittest.TestCase):
    def test_catalog_anchors_live_on_the_catalog_milestone(self):
        for number, title, label in (
            (81, "RNF-12: Seguridad y privacidad", "requerimiento-no-funcional"),
            (74, "RNF-05: Aspecto Visual", "requerimiento-no-funcional"),
            (70, "RNF-01: Uso de Memoria RAM", "requerimiento-no-funcional"),
            (3, "RF-01: Gestionar Trámites", "requerimiento-funcional"),
            (162, "CU09 – Registrar deudas documentos de Cliente", "CASO-DE-USO"),
        ):
            self.assertEqual(classify_open(number, title, [label]), CATALOG, title)

    def test_catalog_anchor_with_work_labels_is_delivery_work(self):
        self.assertEqual(
            classify_open(611, "login page bypasses FormContainer", ["CASO-DE-USO", "FRONTEND", "bug", "priority:medium"]),
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

    def test_jpa_retirement_moved_into_the_hexagonal_train(self):
        for number in (571, 576, 577, 578, 579, 580):
            self.assertEqual(
                classify_open(number, "arch(jpa): legacy layering", ["BACKEND", "priority:high", "tech-debt"]),
                RELEASE,
            )

    def test_phase_6_deprecation_epic_is_the_ga_train(self):
        self.assertEqual(
            classify_open(899, "Phase 6 — Deprecation", ["roadmap", "phase:6-deprecation", "priority:medium"]),
            GA,
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
        self.assertEqual(classify_closed("2026-06-30T12:00:00Z", False), "H · 2026 Q2 cohort: foundations & API")
        self.assertEqual(classify_closed("2026-07-01T00:00:00Z", False), "H · 2026 Q3 cohort: product build & UI/UX")
        self.assertEqual(classify_closed("2026-09-30T00:00:00Z", False), "H · 2026 Q3 cohort: product build & UI/UX")
        self.assertEqual(classify_closed("2026-10-01T00:00:00Z", False), "v0.1 · First release cuts (2026-10)")
        self.assertEqual(classify_closed("2026-10-12T00:00:00Z", False), "v0.2 · Release identity & product forms (2026-10)")

    def test_unmilestoned_work_closed_after_the_last_cut_ships_with_the_current_train(self):
        self.assertEqual(classify_closed("2026-10-17T00:00:00Z", False), RELEASE)


class NomenclatureTests(unittest.TestCase):
    TRAIN = re.compile(r"^v\d+\.\d+ · \S.* \(\d{4}-\d{2}\)$")
    STAGE = re.compile(r"^H\d+ · \S.* \(\d{4}(-\d{2})?–\d{4}(-\d{2})?\)$|^H\d+ · \S.* \(\d{4}-\d{2}\)$")

    def test_open_trains_are_version_first_and_ordered(self):
        self.assertEqual(
            SCHEDULED,
            (
                "v0.3 · Hexagonal core & security (2026-11)",
                "v0.4 · Product completeness (2026-12)",
                "v0.5 · Quality & traceability (2027-01)",
                "v0.6 · Platform & operability (2027-02)",
                "v1.0 · GA: legacy-free (2027-03)",
            ),
        )
        for title in SCHEDULED + SHIPPED:
            self.assertRegex(title, self.TRAIN)

    def test_shipped_cuts_have_their_own_closed_trains(self):
        self.assertEqual(
            SHIPPED,
            ("v0.1 · First release cuts (2026-10)", "v0.2 · Release identity & product forms (2026-10)"),
        )

    def test_history_stages_run_from_the_swing_monolith_to_pre_release_hardening(self):
        self.assertEqual(HISTORY_STAGES[0], "H0 · Swing monolith (2014–2018)")
        self.assertEqual(HISTORY_STAGES[-1], "H8 · Pre-release hardening (2026-09–2026-10)")
        self.assertEqual([t.split(" ")[0] for t in HISTORY_STAGES], [f"H{n}" for n in range(9)])
        for title in HISTORY_STAGES:
            self.assertRegex(title, self.STAGE)

    def test_backlog_and_catalog_are_continuous_without_due_date(self):
        self.assertEqual(BACKLOG, "Backlog · Unscheduled")
        self.assertEqual(CATALOG, "Catalog · Requirements & use cases")
        self.assertNotIn(BACKLOG, MILESTONE_DUE)
        self.assertNotIn(CATALOG, MILESTONE_DUE)
        self.assertNotIn(BACKLOG, MILESTONE_WINDOW)

    def test_every_train_and_stage_has_a_due_date_equal_to_its_window_end(self):
        for title in SCHEDULED + SHIPPED + HISTORY_STAGES + HISTORY_ERAS:
            self.assertEqual(MILESTONE_DUE[title][:10], MILESTONE_WINDOW[title][1], title)

    def test_descriptions_follow_the_template(self):
        for title in SCHEDULED + SHIPPED + HISTORY_STAGES:
            text = MILESTONE_DESCRIPTION[title]
            self.assertTrue(text.startswith("Goal: "), title)
            for part in ("Window: ", "Key deliverables: ", "Exit: "):
                self.assertIn(part, text, title)

    def test_old_titles_rename_to_the_new_nomenclature(self):
        self.assertEqual(RENAMES["2026-11 · Release blockers"], RELEASE)
        self.assertEqual(RENAMES["2027-03 · Legacy retirement"], GA)
        self.assertEqual(RENAMES["2027-06 · Backlog"], BACKLOG)
        self.assertEqual(RENAMES["History · Phase 0 Foundations"], HISTORY_STAGES[1])
        self.assertEqual(RENAMES["Phase 0 - Foundations"], HISTORY_STAGES[1])
        for bucket in ("History · v1.0 priority bucket", "History · v1.1 priority bucket", "History · Backlog priority bucket"):
            self.assertEqual(RENAMES[bucket], HISTORY_STAGES[8])
        self.assertEqual(RENAMES["History · 2026 Q4 — Cloud AI SDLC"], HISTORY_ERAS[2])
        for old, new in RENAMES.items():
            self.assertNotIn(new, RENAMES, f"{old} renames to a title that is itself renamed")

    def test_history_is_recognised_by_prefix(self):
        self.assertTrue(is_history("H0 · Swing monolith (2014–2018)"))
        self.assertTrue(is_history("H · 2026 Q3 cohort: product build & UI/UX"))
        self.assertTrue(is_history("History · Phase 0 Foundations"))
        self.assertFalse(is_history("v0.3 · Hexagonal core & security (2026-11)"))
        self.assertFalse(is_history(BACKLOG))


class ShippedTrainTests(unittest.TestCase):
    V01 = "v0.1 · First release cuts (2026-10)"
    V02 = "v0.2 · Release identity & product forms (2026-10)"
    Q4 = "H · 2026 Q4 cohort: cloud AI SDLC"

    def test_issues_closed_up_to_the_last_v0_1_tag_shipped_in_v0_1(self):
        self.assertEqual(shipped_train("2026-10-01T00:00:00Z", None), self.V01)
        self.assertEqual(shipped_train("2026-10-10T14:45:19Z", self.Q4), self.V01)

    def test_issues_closed_after_v0_1_up_to_v0_2_3_shipped_in_v0_2(self):
        self.assertEqual(shipped_train("2026-10-10T15:00:00Z", "2026-11 · Release blockers"), self.V02)
        self.assertEqual(shipped_train("2026-10-16T11:26:32Z", RELEASE), self.V02)

    def test_outside_the_cut_windows_nothing_moves(self):
        self.assertIsNone(shipped_train("2026-09-30T23:59:59Z", None))
        self.assertIsNone(shipped_train("2026-10-16T11:26:33Z", None))
        self.assertIsNone(shipped_train("", None))

    def test_curated_history_and_shipped_trains_are_kept(self):
        self.assertIsNone(shipped_train("2026-10-05T00:00:00Z", "H8 · Pre-release hardening (2026-09–2026-10)"))
        self.assertIsNone(shipped_train("2026-10-05T00:00:00Z", "History · v1.0 priority bucket"))
        self.assertIsNone(shipped_train("2026-10-14T00:00:00Z", self.V02))


class ApplyTests(unittest.TestCase):
    @staticmethod
    def issue(state, milestone=None, closed_at=None, labels=(), number=1000, title="x"):
        return {
            "number": number,
            "title": title,
            "state": state,
            "closedAt": closed_at,
            "milestone": {"title": milestone} if milestone else None,
            "labels": {"nodes": [{"name": name} for name in labels]},
        }

    def test_merged_bucket_issues_move_to_h8(self):
        for bucket in ("History · v1.1 priority bucket", "History · Backlog priority bucket"):
            self.assertEqual(
                target_for(self.issue("CLOSED", bucket, "2026-10-05T00:00:00Z")),
                "H8 · Pre-release hardening (2026-09–2026-10)",
            )

    def test_closed_cohort_issue_in_a_cut_window_moves_to_the_shipped_train(self):
        self.assertEqual(
            target_for(self.issue("CLOSED", "History · 2026 Q4 — Cloud AI SDLC", "2026-10-11T00:00:00Z")),
            "v0.2 · Release identity & product forms (2026-10)",
        )

    def test_curated_history_issue_keeps_its_renamed_stage(self):
        self.assertEqual(
            target_for(self.issue("CLOSED", "History · Phase 1 Backend API", "2026-05-02T00:00:00Z")),
            "H2 · REST API migration (2026-05)",
        )

    def test_open_catalog_anchor_targets_the_catalog(self):
        self.assertEqual(
            target_for(self.issue("OPEN", "2026-12 · Frontend product", labels=["CASO-DE-USO"], number=162, title="CU09")),
            CATALOG,
        )

    def test_payload_closes_history_and_shipped_and_leaves_continuous_undated(self):
        self.assertEqual(milestone_payload(HISTORY_STAGES[0])["state"], "closed")
        self.assertEqual(milestone_payload(SHIPPED[1])["state"], "closed")
        self.assertEqual(milestone_payload(RELEASE)["state"], "open")
        self.assertIsNone(milestone_payload(BACKLOG)["due_on"])
        self.assertIsNone(milestone_payload(CATALOG)["due_on"])
        self.assertEqual(milestone_payload(RELEASE)["due_on"], MILESTONE_DUE[RELEASE])


class CreateBodyTests(unittest.TestCase):
    """The first Owner run stopped creating Catalog, the only POST with ``"due_on": null``."""

    def test_continuous_milestone_is_created_without_a_due_on_key(self):
        body = create_body(CATALOG)
        self.assertNotIn("due_on", body)
        self.assertEqual(body["state"], "open")
        self.assertEqual(body["title"], CATALOG)

    def test_closed_milestones_are_created_open_with_their_due_date(self):
        body = create_body(SHIPPED[0])
        self.assertEqual(body["state"], "open")
        self.assertEqual(body["due_on"], MILESTONE_DUE[SHIPPED[0]])
        self.assertTrue(all(value is not None for value in body.values()))


class ConsolidationTests(unittest.TestCase):
    """#1847: merged-away milestones and pre-taxonomy views do not linger."""

    def row(self, number, title, open_issues=0, closed_issues=0):
        return {"number": number, "title": title, "open_issues": open_issues, "closed_issues": closed_issues}

    def test_merged_bucket_is_retired_once_its_target_exists(self):
        rows = [self.row(12, "H8 · Pre-release hardening (2026-09–2026-10)"),
                self.row(13, "History · v1.1 priority bucket")]
        self.assertEqual([r["number"] for r in retired_milestones(rows)], [13])

    def test_bucket_is_not_retired_before_its_target_exists(self):
        self.assertEqual(retired_milestones([self.row(13, "History · v1.1 priority bucket")]), [])

    def test_policy_milestones_are_never_retired(self):
        rows = [self.row(n, t) for n, t in enumerate(SCHEDULED, 1)]
        self.assertEqual(retired_milestones(rows), [])

    def test_views_outside_the_contract_are_stale(self):
        names = ["Kanban", "Casos de Uso", "RNF", "Catalog", "KANBAN"]
        self.assertEqual(stale_views(names), ["Casos de Uso", "RNF", "KANBAN"])

    def test_alias_is_not_stale_while_its_new_view_is_missing(self):
        self.assertEqual(stale_views(["KANBAN", "Requerimientos Func."]), [])


class RoadmapContractTests(unittest.TestCase):
    def test_full_lifecycle_view_requires_every_issue_on_the_project(self):
        issues = [{"number": 2}, {"number": 226}, {"number": 499}]
        self.assertEqual(
            [issue["number"] for issue in missing_project_issues(issues, {2: "item"})],
            [226, 499],
        )

    def test_every_history_and_delivery_milestone_has_a_roadmap_window(self):
        expected = set(SCHEDULED) | set(HISTORY_ERAS) | set(HISTORY_STAGES) | set(SHIPPED)
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
            "trace:rf",
            "trace:rnf",
            "trace:cu",
        ):
            self.assertIn(catalog_label, FULL_LIFECYCLE_FILTER)
            self.assertIn(catalog_label, DELIVERY_FILTER)
        self.assertTrue(FULL_LIFECYCLE_FILTER.startswith("-label:"))

    def test_view_contract_separates_history_from_execution(self):
        self.assertEqual(
            VIEW_CONTRACTS["Release roadmap"],
            ("ROADMAP_LAYOUT", f'{FULL_LIFECYCLE_FILTER} -milestone:"{BACKLOG}"'),
        )
        self.assertEqual(VIEW_CONTRACTS["Kanban"], ("BOARD_LAYOUT", DELIVERY_FILTER))
        self.assertIn("status:Todo", VIEW_CONTRACTS["Agent queue"][1])

    def test_nine_views_from_the_label_taxonomy(self):
        # #1860: the approved board views; filters name old AND new labels (comma = OR).
        self.assertEqual(
            list(VIEW_CONTRACTS),
            ["Kanban", "Current train", "Agent queue", "In flight", "Needs triage",
             "Blocked / needs info", "Release roadmap", "Shipped", "Catalog"],
        )
        self.assertIn("label:status:ready,ready-for-dev", VIEW_CONTRACTS["Agent queue"][1])
        self.assertIn(f'milestone:"{RELEASE}"', VIEW_CONTRACTS["Current train"][1])
        self.assertIn("is:closed", VIEW_CONTRACTS["Shipped"][1])
        self.assertIn(f'milestone:"{CATALOG}"', VIEW_CONTRACTS["Catalog"][1])
        for name in ("trace:rf", "requerimiento-funcional", "trace:cu", "CASO-DE-USO"):
            self.assertIn(name, DELIVERY_FILTER)

    def test_views_do_not_use_deleted_board_fields(self):
        # #1860: Priority/Area/Kind project fields are deleted; labels carry them.
        self.assertNotIn("Release roadmap", VIEW_FIELDS)
        for fields in VIEW_FIELDS.values():
            self.assertFalse({"Priority", "Area", "Kind"} & set(fields))
        self.assertIn("Labels", VIEW_FIELDS["Agent queue"])
        self.assertNotIn("Start", VIEW_FIELDS["Kanban"])


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
