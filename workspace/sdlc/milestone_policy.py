"""Scheduling policy for Notaire milestones and parallel-agent lanes.

Pure functions. ``apply_milestone_policy.py`` is the only piece that talks to GitHub.
"""

from __future__ import annotations

import re

RELEASE = "2026-11 · Release blockers"
FRONTEND = "2026-12 · Frontend product"
QUALITY = "2027-01 · Quality & traceability"
PLATFORM = "2027-02 · Platform & operability"
LEGACY = "2027-03 · Legacy retirement"
BACKLOG = "2027-06 · Backlog"

HISTORY_Q2 = "History · 2026 Q2 — Foundations & API"
HISTORY_Q3 = "History · 2026 Q3 — Product build"
HISTORY_Q4 = "History · 2026 Q4 — Cloud AI SDLC"

SCHEDULED = (RELEASE, FRONTEND, QUALITY, PLATFORM, LEGACY, BACKLOG)
HISTORY_ERAS = (HISTORY_Q2, HISTORY_Q3, HISTORY_Q4)

# Closed milestone titles that already group a curated set. Renamed, not emptied.
RENAMES = {
    "Phase 0 - Foundations": "History · Phase 0 Foundations",
    "Phase 1 - Backend Complete": "History · Phase 1 Backend API",
    "Phase 2 - Kubernetes & Observability": "History · Phase 2 Platform & observability",
    "Phase 3 - Modern Frontend": "History · Phase 3 Frontend slice",
    "Phase 4 - Production Ready": "History · Phase 4 Production slice",
    "Phase 4 — Modern Frontend": "History · Phase 4 Frontend wave",
    "Phase 6 — Deprecation": "History · Phase 6 Deprecation start",
    "v1.0 — Production Ready": "History · v1.0 priority bucket",
    "v1.1 — Hardening": "History · v1.1 priority bucket",
    "Backlog": "History · Backlog priority bucket",
}

# Due dates are what the board ROADMAP plots. History eras close after backfill.
MILESTONE_DUE = {
    RELEASE: "2026-11-30T00:00:00Z",
    FRONTEND: "2026-12-31T00:00:00Z",
    QUALITY: "2027-01-31T00:00:00Z",
    PLATFORM: "2027-02-28T00:00:00Z",
    LEGACY: "2027-03-31T00:00:00Z",
    BACKLOG: "2027-06-30T00:00:00Z",
    HISTORY_Q2: "2026-06-30T00:00:00Z",
    HISTORY_Q3: "2026-09-30T00:00:00Z",
    HISTORY_Q4: "2026-10-12T00:00:00Z",
}

# Bars on the roadmap for open delivery work (catalog anchors are filtered out).
ROADMAP_WINDOW = {
    RELEASE: ("2026-10-13", "2026-11-30"),
    FRONTEND: ("2026-10-13", "2026-12-31"),
    QUALITY: ("2027-01-01", "2027-01-31"),
    PLATFORM: ("2027-02-01", "2027-02-28"),
    LEGACY: ("2027-03-01", "2027-03-31"),
    BACKLOG: ("2027-04-01", "2027-06-30"),
}

MILESTONE_DESCRIPTION = {
    RELEASE: "Open priority:critical work, security fixes, and security NFRs (RNF-11–16). Exit: no open critical issue.",
    FRONTEND: "Phase 4 product scope: open use cases, functional requirements, and Next.js behavior (#898). Visual NFRs (RNF-05–10) live here.",
    QUALITY: "Tests, traceability, documentation, performance NFRs (RNF-01–04) and process NFRs (RNF-21–22).",
    PLATFORM: "CI, observability, and operability implementation that is not a critical release blocker.",
    LEGACY: "jpa retirement and the Phase 6 deprecation epic (#899). Critical legacy defects stay on Release blockers until they land.",
    BACKLOG: "priority:low work and environment NFRs (RNF-17–20, RNF-23–24) with no earlier train.",
    HISTORY_Q2: "Closed issues that had no milestone and were closed before 2026-07-01.",
    HISTORY_Q3: "Closed issues that had no milestone and were closed from 2026-07-01 through 2026-09-30.",
    HISTORY_Q4: "Closed issues that had no milestone and were closed on or after 2026-10-01.",
}

# jpa / Phase 6 items. Critical ones are pulled forward by the critical rule.
LEGACY_NUMBERS = frozenset({571, 576, 577, 578, 579, 580, 584, 595, 899})
FRONTEND_NUMBERS = frozenset({611, 771, 774, 898, 1224, 1250, 1545, 1546, 1547, 1553, 1554, 1555, 1556, 1557})
QUALITY_NUMBERS = frozenset({902, 921})

CATALOG_LABELS = frozenset({"requerimiento-funcional", "requerimiento-no-funcional", "CASO-DE-USO"})
WORK_LABELS = frozenset(
    {"bug", "enhancement", "tech-debt", "TAREA", "REFACTOR", "chore", "in-progress", "ux", "roadmap"}
)
PRIORITIES = ("priority:critical", "priority:high", "priority:medium", "priority:low")

_RNF = re.compile(r"RNF-(\d+)")
SECURITY_RNF = frozenset({11, 12, 13, 14, 15, 16})
VISUAL_RNF = frozenset({5, 6, 7, 8, 9, 10})
QUALITY_RNF = frozenset({1, 2, 3, 4, 21, 22})
ENV_RNF = frozenset({17, 18, 19, 20, 23, 24})

# Mis-tagged as use cases; they are epics / audits.
STRIP_USE_CASE_LABEL = frozenset({898, 921})


def priority_of(labels: set[str] | list[str]) -> str | None:
    found = [name for name in PRIORITIES if name in labels]
    return found[0] if len(found) == 1 else (found[0] if found else None)


def rnf_number(title: str) -> int | None:
    match = _RNF.search(title or "")
    return int(match.group(1)) if match else None


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

    rnf = rnf_number(title)
    if rnf is not None and "requerimiento-no-funcional" in names:
        if rnf in SECURITY_RNF:
            return RELEASE
        if rnf in VISUAL_RNF:
            return FRONTEND
        if rnf in QUALITY_RNF:
            return QUALITY
        if rnf in ENV_RNF:
            return BACKLOG

    pri = priority_of(names)
    if pri == "priority:low":
        return BACKLOG
    if pri == "priority:critical" or "security" in names:
        return RELEASE
    if pri == "priority:high" and "security(" in (title or "").lower():
        return RELEASE
    if number in LEGACY_NUMBERS or "phase:6-deprecation" in names:
        return LEGACY
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
    return HISTORY_Q4


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
