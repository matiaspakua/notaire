"""Scheduling policy for Notaire milestones and parallel-agent lanes.

Pure functions. ``apply_milestone_policy.py`` is the only piece that talks to GitHub.
"""

from __future__ import annotations

import re

# Nomenclature: docs/300-development/305-project-management/MILESTONE-NOMENCLATURE.md
# Open trains are version first: ``v<MAJOR>.<MINOR> · <Theme> (<YYYY-MM>)``.
RELEASE = "v0.3 · Hexagonal core & security (2026-11)"
FRONTEND = "v0.4 · Product completeness (2026-12)"
QUALITY = "v0.5 · Quality & traceability (2027-01)"
PLATFORM = "v0.6 · Platform & operability (2027-02)"
GA = "v1.0 · GA: legacy-free (2027-03)"
BACKLOG = "Backlog · Unscheduled"
CATALOG = "Catalog · Requirements & use cases"

V01 = "v0.1 · First release cuts (2026-10)"
V02 = "v0.2 · Release identity & product forms (2026-10)"

H0 = "H0 · Swing monolith (2014–2018)"
H1 = "H1 · Revival & foundations (2026-03–2026-04)"
H2 = "H2 · REST API migration (2026-05)"
H3 = "H3 · Platform & observability (2026-06)"
H4 = "H4 · Web frontend slice (2026-07)"
H5 = "H5 · Web frontend wave (2026-07–2026-08)"
H6 = "H6 · Production slice (2026-08–2026-09)"
H7 = "H7 · Swing deprecation start (2026-09)"
H8 = "H8 · Pre-release hardening (2026-09–2026-10)"

HISTORY_Q2 = "H · 2026 Q2 cohort: foundations & API"
HISTORY_Q3 = "H · 2026 Q3 cohort: product build & UI/UX"
HISTORY_Q4 = "H · 2026 Q4 cohort: cloud AI SDLC"

SCHEDULED = (RELEASE, FRONTEND, QUALITY, PLATFORM, GA)
CONTINUOUS = (BACKLOG, CATALOG)
SHIPPED = (V01, V02)
HISTORY_STAGES = (H0, H1, H2, H3, H4, H5, H6, H7, H8)
HISTORY_ERAS = (HISTORY_Q2, HISTORY_Q3, HISTORY_Q4)

# Last tag of each shipped minor, in UTC. A closed issue up to that instant
# (and after the previous cut) shipped in that train.
CUT_WINDOWS = (
    (V01, "2026-10-01T00:00:00Z", "2026-10-10T14:45:19Z"),  # notaire-v0.1.1, notaire-v0.1.2
    (V02, "2026-10-10T14:45:20Z", "2026-10-16T11:26:32Z"),  # v0.2.0 … v0.2.3
)


def is_history(title: str) -> bool:
    """History milestones start with ``H<n> ·``, ``H ·`` or the retired ``History ·``."""
    return bool(re.match(r"^(H\d* · |History · )", title or ""))


CATALOG_EXCLUSIONS = (
    "-label:requerimiento-funcional "
    "-label:requerimiento-no-funcional "
    "-label:CASO-DE-USO"
)
FULL_LIFECYCLE_FILTER = CATALOG_EXCLUSIONS
DELIVERY_FILTER = f"is:open {CATALOG_EXCLUSIONS}"
TRIAGE_FILTER = (
    f"{DELIVERY_FILTER} "
    "-label:priority:critical -label:priority:high "
    "-label:priority:medium -label:priority:low"
)
TODO_FILTER = f"is:open status:Todo {CATALOG_EXCLUSIONS}"
VIEW_CONTRACTS = {
    "Roadmap": ("ROADMAP_LAYOUT", FULL_LIFECYCLE_FILTER),
    "KANBAN": ("BOARD_LAYOUT", DELIVERY_FILTER),
    "Agent queue": ("TABLE_LAYOUT", DELIVERY_FILTER),
    "Needs triage": ("TABLE_LAYOUT", TRIAGE_FILTER),
    "Todo by priority": ("TABLE_LAYOUT", TODO_FILTER),
}
VIEW_FIELDS = {
    "KANBAN": ("Title", "Assignees", "Labels", "Milestone", "Priority", "Area"),
    "Agent queue": (
        "Title",
        "Assignees",
        "Status",
        "Labels",
        "Milestone",
        "Priority",
        "Area",
        "Start",
        "Target",
    ),
    "Needs triage": ("Title", "Status", "Labels", "Milestone", "Priority", "Area"),
    "Todo by priority": ("Title", "Assignees", "Status", "Labels", "Milestone", "Priority", "Area"),
}

# Old title -> new title. Several buckets merge into H8; renames are one hop.
RENAMES = {
    "2026-11 · Release blockers": RELEASE,
    "2026-12 · Frontend product": FRONTEND,
    "2027-01 · Quality & traceability": QUALITY,
    "2027-02 · Platform & operability": PLATFORM,
    "2027-03 · Legacy retirement": GA,
    "2027-06 · Backlog": BACKLOG,
    "Phase 0 - Foundations": H1,
    "History · Phase 0 Foundations": H1,
    "Phase 1 - Backend Complete": H2,
    "History · Phase 1 Backend API": H2,
    "Phase 2 - Kubernetes & Observability": H3,
    "History · Phase 2 Platform & observability": H3,
    "Phase 3 - Modern Frontend": H4,
    "History · Phase 3 Frontend slice": H4,
    "Phase 4 — Modern Frontend": H5,
    "History · Phase 4 Frontend wave": H5,
    "Phase 4 - Production Ready": H6,
    "History · Phase 4 Production slice": H6,
    "Phase 6 — Deprecation": H7,
    "History · Phase 6 Deprecation start": H7,
    "v1.0 — Production Ready": H8,
    "History · v1.0 priority bucket": H8,
    "v1.1 — Hardening": H8,
    "History · v1.1 priority bucket": H8,
    "Backlog": H8,
    "History · Backlog priority bucket": H8,
    "History · 2026 Q2 — Foundations & API": HISTORY_Q2,
    "History · 2026 Q3 — Product build": HISTORY_Q3,
    "History · 2026 Q4 — Cloud AI SDLC": HISTORY_Q4,
}

# Start and end of each milestone (Project Start/Target fields). Due = end.
MILESTONE_WINDOW = {
    H0: ("2014-03-01", "2018-12-31"),
    H1: ("2026-03-01", "2026-04-30"),
    H2: ("2026-05-01", "2026-05-31"),
    H3: ("2026-06-01", "2026-06-30"),
    H4: ("2026-07-01", "2026-07-31"),
    H5: ("2026-07-01", "2026-08-31"),
    H6: ("2026-08-01", "2026-09-30"),
    H7: ("2026-09-01", "2026-09-30"),
    H8: ("2026-09-01", "2026-10-12"),
    HISTORY_Q2: ("2026-03-01", "2026-06-30"),
    HISTORY_Q3: ("2026-07-01", "2026-09-30"),
    HISTORY_Q4: ("2026-10-01", "2026-10-12"),
    V01: ("2026-10-01", "2026-10-10"),
    V02: ("2026-10-10", "2026-10-16"),
    RELEASE: ("2026-10-13", "2026-11-30"),
    FRONTEND: ("2026-10-13", "2026-12-31"),
    QUALITY: ("2027-01-01", "2027-01-31"),
    PLATFORM: ("2027-02-01", "2027-02-28"),
    GA: ("2027-03-01", "2027-03-31"),
}
ROADMAP_WINDOW = {title: MILESTONE_WINDOW[title] for title in SCHEDULED}

# Backlog and Catalog are continuous: no due date.
MILESTONE_DUE = {title: f"{end}T12:00:00Z" for title, (_start, end) in MILESTONE_WINDOW.items()}


def _describe(goal: str, window: str, deliverables: str, exit_: str, nxt: str) -> str:
    return f"Goal: {goal}. Window: {window}. Key deliverables: {deliverables}. Exit: {exit_}. Next: {nxt}."


MILESTONE_DESCRIPTION = {
    RELEASE: _describe("Ports & Adapters core and release-blocking security", "v0.3.z cuts, 2026-10-13 → 2026-11-30",
                       "#1693 hexagonal slices, jpa retirement #571–#580, #1683, #1684, #1640, #655, #254, RNF-11–16",
                       "no open priority:critical issue; ArchUnit baseline shrinking", FRONTEND),
    FRONTEND: _describe("every use case complete end to end in the web client", "v0.4.z cuts, 2026-10-13 → 2026-12-31",
                        "#771, #774, #611, UI E2E for remaining CUs, RNF-05–10", "no open product gap against CU01–CU87", QUALITY),
    QUALITY: _describe("tests, traceability and documentation at target", "v0.5.z cuts, 2027-01-01 → 2027-01-31",
                       "#1246 coverage 80%, #1251 a11y, #1244 CU-API matrix, #1535 Sonar, #288, #306, #292",
                       "quality gates green at target values", PLATFORM),
    PLATFORM: _describe("CI, observability and operability enforced", "v0.6.z cuts, 2027-02-01 → 2027-02-28",
                        "#1315 OpenAPI coherence, #566/#710/#711 static analysis gates, #1707 observability",
                        "Checkstyle, SpotBugs and Trivy fail the build", GA),
    GA: _describe("general availability with no legacy code", "v1.0.z cuts, 2027-03-01 → 2027-03-31",
                  "#899 Phase 6 deprecation, legacy packages removed, Owner GA decision",
                  "Owner tags v1.0.0", "next minor train"),
    BACKLOG: "Continuous. priority:low work and Owner decisions not yet scheduled. Scheduling = moving the issue into a vX.Y train.",
    CATALOG: "Continuous. RF / RNF / CU requirement anchors (SRS traceability). Closed per anchor with evidence, never by a release.",
    V01: _describe("first semver cuts of the three-tier product", "notaire-v0.1.1 (2026-10-07), notaire-v0.1.2 (2026-10-10)",
                   "release-please, product identity, CHANGELOG", "shipped", V02),
    V02: _describe("release identity and the missing product forms", "v0.2.0 (2026-10-12) … v0.2.3 (2026-10-16)",
                   "#1547 missing forms, #1573 workflow editor, #1593 PDF design system, SBOM and version surfaces",
                   "shipped", RELEASE),
    H0: _describe("academic Swing + MySQL desktop monolith", "2014–2018",
                  "SRS, RF/RNF/CU catalog, Swing client (removed)", "superseded by the three-tier rewrite", H1),
    H1: _describe("revive the codebase and set foundations", "2026-03 → 2026-04",
                  "docs, standards, base CI, Java 21 / Spring Boot upgrade", "closed", H2),
    H2: _describe("REST API over the legacy domain", "2026-05", "REST endpoints, JWT, Swagger, Flyway", "closed", H3),
    H3: _describe("platform and observability", "2026-06", "k3d, Prometheus/Grafana, OpenTelemetry", "closed", H4),
    H4: _describe("first Swing→Next.js slice", "2026-07", "Next.js frontend module", "closed", H5),
    H5: _describe("web frontend wave", "2026-07 → 2026-08", "Next.js 16 / React 19 screens for the use cases", "closed", H6),
    H6: _describe("production slice", "2026-08 → 2026-09", "E2E, OWASP review, DR", "closed", H7),
    H7: _describe("start Swing deprecation", "2026-09", "Swing client removal", "closed", H8),
    H8: _describe("pre-release hardening by priority (merged v1.0/v1.1/backlog buckets)", "2026-09 → 2026-10-12",
                  "critical/high, medium and low hardening items; priority labels keep the split", "closed", V01),
    HISTORY_Q2: "Close-date cohort: closed issues that had no milestone and were closed before 2026-07-01.",
    HISTORY_Q3: "Close-date cohort: closed issues that had no milestone and were closed from 2026-07-01 through 2026-09-30.",
    HISTORY_Q4: "Close-date cohort: closed issues that had no milestone, closed from 2026-10-01 and not part of a cut.",
}

# jpa retirement moved into the hexagonal train (#1693). Phase 6 is GA.
JPA_NUMBERS = frozenset({571, 576, 577, 578, 579, 580, 584, 595})
GA_NUMBERS = frozenset({899})
FRONTEND_NUMBERS = frozenset({611, 771, 774, 898, 1224, 1250, 1545, 1546, 1547, 1553, 1554, 1555, 1556, 1557})
QUALITY_NUMBERS = frozenset({902, 921})

CATALOG_LABELS = frozenset({"requerimiento-funcional", "requerimiento-no-funcional", "CASO-DE-USO"})
WORK_LABELS = frozenset(
    {"bug", "enhancement", "tech-debt", "TAREA", "REFACTOR", "chore", "in-progress", "ux", "roadmap"}
)
PRIORITIES = ("priority:critical", "priority:high", "priority:medium", "priority:low")


# Mis-tagged as use cases; they are epics / audits.
STRIP_USE_CASE_LABEL = frozenset({898, 921})


def priority_of(labels: set[str] | list[str]) -> str | None:
    found = [name for name in PRIORITIES if name in labels]
    return found[0] if len(found) == 1 else (found[0] if found else None)



_AREAS = frozenset({"BACKEND", "FRONTEND", "DB", "DEVOPS", "TEST", "DOC", "security"})


def _names(labels: list[str] | set[str]) -> set[str]:
    names = set(labels)
    if "documentation" in names:
        names.add("DOC")
    return names


def classify_open(number: int, title: str, labels: list[str] | set[str]) -> str:
    """Return the scheduled milestone title for an open issue."""
    names = _names(labels)
    if number in QUALITY_NUMBERS:
        return QUALITY

    if names & CATALOG_LABELS and not names & WORK_LABELS:
        return CATALOG

    pri = priority_of(names)
    if pri == "priority:low":
        return BACKLOG
    if pri == "priority:critical" or "security" in names:
        return RELEASE
    if pri == "priority:high" and "security(" in (title or "").lower():
        return RELEASE
    if number in JPA_NUMBERS:
        return RELEASE
    if number in GA_NUMBERS or "phase:6-deprecation" in names:
        return GA
    if number in FRONTEND_NUMBERS:
        return FRONTEND
    if not names & _AREAS and not names & CATALOG_LABELS and priority_of(names) is None:
        return BACKLOG
    if "CASO-DE-USO" in names or "requerimiento-funcional" in names:
        return FRONTEND
    if "FRONTEND" in names and "TEST" not in names and "DOC" not in names:
        return FRONTEND
    if "DEVOPS" in names and "DOC" not in names:
        return PLATFORM
    return QUALITY


def classify_closed(closed_at: str, has_milestone: bool) -> str | None:
    """Era for a closed issue that has no milestone. None keeps the current one."""
    if has_milestone or not closed_at:
        return None
    day = closed_at[:10]
    if day < "2026-07-01":
        return HISTORY_Q2
    if day < "2026-10-01":
        return HISTORY_Q3
    return shipped_train(closed_at, None) or SCHEDULED[0]


_MOVABLE = frozenset({None, HISTORY_Q4, "History · 2026 Q4 — Cloud AI SDLC"}) | set(SCHEDULED) | set(CONTINUOUS)


def shipped_train(closed_at: str, current: str | None) -> str | None:
    """The shipped vX.Y train of an issue closed inside a cut window, else None.

    Only unmilestoned issues, the Q4 cohort and delivery trains move; curated
    history and shipped trains keep their milestone.
    """
    if not closed_at:
        return None
    if current is not None and current not in _MOVABLE and RENAMES.get(current) not in _MOVABLE - {None}:
        return None
    for train, start, end in CUT_WINDOWS:
        if start <= closed_at <= end:
            return train
    return None


def lane(labels: list[str] | set[str], number: int, title: str) -> str:
    """Who may pick the issue. catalog and epic are not pickable."""
    names = _names(labels)
    text = title or ""
    if not names:
        return "catalog"
    if (
        "roadmap" in names
        or text.startswith("📋")
        or text.startswith("[Epic]")
        or text.startswith("[ROADMAP]")
    ):
        return "epic"
    if names & CATALOG_LABELS and not (names & WORK_LABELS):
        return "catalog"
    if "security" in names or (priority_of(names) == "priority:critical" and "BACKEND" in names):
        return "security"
    if "BACKEND" in names and "FRONTEND" in names:
        return "cross"
    if "FRONTEND" in names:
        return "frontend"
    if "BACKEND" in names:
        return "backend"
    if "DOC" in names or "TEST" in names:
        return "quality"
    if "DEVOPS" in names:
        return "platform"
    return "quality"


def lanes_blocked_by(active_lane: str) -> frozenset[str]:
    """Lanes that must stay idle while ``active_lane`` has an in-progress issue."""
    if active_lane == "cross":
        return frozenset({"cross", "frontend", "backend"})
    if active_lane == "security":
        return frozenset({"security", "backend"})
    if active_lane in {"frontend", "backend", "platform", "quality"}:
        return frozenset({active_lane})
    return frozenset()


def suggested_priority(number: int, title: str, labels: list[str] | set[str], milestone: str) -> str | None:
    """A single priority for work that has none. Catalog anchors stay unlabeled."""
    names = set(labels)
    if any(name in names for name in PRIORITIES):
        return None
    if lane(names, number, title) in {"catalog", "epic"}:
        return None
    if milestone == BACKLOG:
        return "priority:low"
    text = title or ""
    if "in-progress" in names or text.startswith("[UI]") or text.startswith("[UX]"):
        return "priority:high"
    return "priority:medium"
