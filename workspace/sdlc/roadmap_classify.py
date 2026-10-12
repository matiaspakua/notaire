#!/usr/bin/env python3
"""Classify Notaire issues onto roadmap milestones and parallel-agent lanes.

Pure functions. The apply script (organize-roadmap.py) is the only piece that
talks to GitHub. Rules are the project-management contract in
docs/300-development/305-project-management/.

An issue has exactly one milestone (when it ships or which catalog it belongs
to) and exactly one lane (which agent may claim it). Scope milestones are not
claimable: they stay open as the requirements catalog.
"""

from __future__ import annotations

import re
from collections import defaultdict
from dataclasses import dataclass

# Delivery trains (open). Titles match GitHub milestones already on the repo
# where those milestones exist.
PHASE4 = "Phase 4 — Modern Frontend"
PHASE4_HIST = "Phase 4 — Frontend deliveries (history)"
PHASE6 = "Phase 6 — Deprecation"
V10 = "v1.0 — Production Ready"
V11 = "v1.1 — Hardening"
BACKLOG = "Backlog"

# Historical cohorts for closed issues that lost their milestone.
PHASE1_HIST = "Phase 1 — Analysis & foundation"
PHASE2_HIST = "Phase 2 — Foundation"
PHASE3_HIST = "Phase 3 — Backend API"
PHASE5_HIST = "Phase 5 — Observability & production audit"
CLOUD = "Cloud AI SDLC"

# Permanent catalogs. They stay open and are excluded from delivery progress.
SCOPE_RF = "Scope — Functional requirements"
SCOPE_CU = "Scope — Use cases"
SCOPE_NFR = "Scope — Non-functional requirements"

LANES = (
    "lane:backend",
    "lane:frontend",
    "lane:fullstack",
    "lane:devops",
    "lane:docs",
    "lane:test",
    "lane:security",
    "lane:owner",
)

PHASE_LABELS = {
    "phase:1-analysis": PHASE1_HIST,
    "phase:2-foundation": PHASE2_HIST,
    "phase:3-backend-api": PHASE3_HIST,
    "phase:4-frontend": PHASE4_HIST,
    "phase:5-observability": PHASE5_HIST,
    "phase:6-deprecation": PHASE6,
}

NFR_TRAIN = {
    "performance": V11,
    "experience": PHASE4,
    "security": V10,
    "platform": BACKLOG,
}

SCOPE_MILESTONES = frozenset({SCOPE_RF, SCOPE_CU, SCOPE_NFR})

_RNF = re.compile(r"\bRNF-(\d+)\b", re.IGNORECASE)
_RF = re.compile(r"\bRF-\d+\b", re.IGNORECASE)
_CU = re.compile(r"\bCU\d+\b", re.IGNORECASE)
_RF_BRACKET = re.compile(r"^\[RF\]\b", re.IGNORECASE)


@dataclass(frozen=True)
class Assignment:
    number: int
    state: str
    milestone: str
    reason: str
    lane: str
    claimable: bool
    nfr_theme: str | None = None
    nfr_train: str | None = None


def priority_of(labels: set[str]) -> str | None:
    for name in ("critical", "high", "medium", "low"):
        if f"priority:{name}" in labels:
            return name
    return None


def nfr_theme(title: str) -> str:
    """Map a catalog NFR title onto security, performance, experience, or platform."""
    match = _RNF.search(title or "")
    if not match:
        return "platform"
    number = int(match.group(1))
    if 1 <= number <= 4:
        return "performance"
    if 5 <= number <= 10:
        return "experience"
    if number in {11, 12, 13, 14, 15, 16}:
        return "security"
    return "platform"


def catalog_kind(title: str, labels: set[str]) -> str | None:
    """Return rf, cu, or nfr for a permanent catalog issue.

    Roadmap umbrellas keep their phase milestone even when they also carry
    CASO-DE-USO. A requirements tracker that merely mentions requirements,
    without an RF-/RNF-/CU id, is ordinary work.
    """
    if "roadmap" in labels:
        return None
    if _RNF.search(title):
        return "nfr"
    if _RF.search(title) or _RF_BRACKET.match(title.strip()):
        return "rf"
    if "CASO-DE-USO" in labels and _CU.search(title):
        return "cu"
    return None


def historical_milestone(title: str, labels: set[str], closed: str, created: str) -> tuple[str, str]:
    for label, milestone in PHASE_LABELS.items():
        if label in labels:
            return milestone, "historical-phase-label"
    when = (closed or created or "").strip()
    if when <= "2026-05-31":
        return PHASE1_HIST, "historical-close-date"
    if when <= "2026-06-30":
        return PHASE3_HIST, "historical-close-date"
    if when <= "2026-08-31":
        return PHASE4_HIST, "historical-close-date"
    if when <= "2026-09-30":
        return PHASE5_HIST, "historical-close-date"
    return CLOUD, "historical-close-date"


def open_work_milestone(title: str, labels: set[str]) -> tuple[str, str]:
    low = title.lower()
    pri = priority_of(labels)

    if "phase:6-deprecation" in labels or ("roadmap" in labels and "phase 6" in low):
        return PHASE6, "phase-6"
    if "jpa" in low and "tech-debt" in labels and pri != "critical":
        return PHASE6, "legacy-jpa"
    if "phase:4-frontend" in labels or ("roadmap" in labels and "phase 4" in low):
        return PHASE4, "phase-4"
    if "security" in labels or "audit-2026-09" in labels:
        if pri == "low":
            return BACKLOG, "audit-low"
        if pri == "medium":
            return V11, "audit-medium"
        return V10, "production-gate"
    if "FRONTEND" in labels and "BACKEND" not in labels:
        if pri == "low":
            return BACKLOG, "frontend-low"
        if pri == "critical":
            return V10, "frontend-critical"
        return PHASE4, "frontend-slice"
    if (
        "FRONTEND" in labels
        and "BACKEND" in labels
        and "enhancement" in labels
        and pri != "critical"
    ):
        return PHASE4, "fullstack-feature"
    if pri in ("critical", "high"):
        return V10, "priority-high"
    if pri == "medium":
        return V11, "priority-medium"
    if pri == "low":
        return BACKLOG, "priority-low"
    if low.startswith("test") or "test create" in low or "test-agent" in low:
        return BACKLOG, "unscoped-noise"
    return V11, "needs-triage"


def assign_lane(title: str, labels: set[str], kind: str | None, theme: str | None) -> str:
    """One lane per issue so two agents do not claim the same surface."""
    if kind == "nfr" and theme:
        return {
            "security": "lane:security",
            "performance": "lane:backend",
            "experience": "lane:frontend",
            "platform": "lane:docs",
        }[theme]
    low = title.lower()
    if "owner" in low or low.startswith("test"):
        return "lane:owner"
    # Audit and security findings are one lane, including when they also
    # touch backend, frontend, or devops. Otherwise two agents claim them.
    if "security" in labels or "audit-2026-09" in labels:
        return "lane:security"
    has_backend = "BACKEND" in labels or "DB" in labels
    has_frontend = "FRONTEND" in labels
    if has_backend and has_frontend:
        return "lane:fullstack"
    if has_frontend:
        return "lane:frontend"
    if has_backend:
        return "lane:backend"
    if "DEVOPS" in labels:
        return "lane:devops"
    if "TEST" in labels:
        return "lane:test"
    if "DOC" in labels:
        return "lane:docs"
    return "lane:owner"


def classify(issue: dict) -> Assignment:
    labels = set(issue.get("labels") or [])
    title = issue.get("title") or ""
    state = (issue.get("state") or "open").lower()
    current = (issue.get("milestone") or "").strip()
    kind = catalog_kind(title, labels) if state == "open" else None
    theme = nfr_theme(title) if kind == "nfr" or _RNF.search(title) else None

    if state == "closed" and current:
        milestone, reason = current, "preserve-assigned"
    elif state == "closed":
        milestone, reason = historical_milestone(
            title, labels, issue.get("closed") or "", issue.get("created") or ""
        )
    elif kind == "rf":
        milestone, reason = SCOPE_RF, "scope-rf"
    elif kind == "cu":
        milestone, reason = SCOPE_CU, "scope-cu"
    elif kind == "nfr":
        milestone, reason = SCOPE_NFR, "scope-nfr"
    else:
        milestone, reason = open_work_milestone(title, labels)

    if state == "open" and kind != "nfr" and _RNF.search(title):
        theme = nfr_theme(title)

    lane = assign_lane(title, labels, kind, theme if kind == "nfr" else None)
    claimable = state == "open" and milestone not in SCOPE_MILESTONES
    train = NFR_TRAIN.get(theme) if kind == "nfr" and theme else None
    return Assignment(
        number=int(issue.get("number") or 0),
        state=state,
        milestone=milestone,
        reason=reason,
        lane=lane,
        claimable=claimable,
        nfr_theme=theme if kind == "nfr" else None,
        nfr_train=train,
    )


def summarize(rows: list[Assignment]) -> dict:
    by_milestone: dict[str, dict[str, int]] = defaultdict(lambda: {"open": 0, "closed": 0})
    by_lane: dict[str, int] = defaultdict(int)
    nfr_trains: dict[str, int] = defaultdict(int)
    claimable_open = 0
    for row in rows:
        by_milestone[row.milestone][row.state] += 1
        if row.claimable and row.state == "open":
            claimable_open += 1
            by_lane[row.lane] += 1
        if row.nfr_train:
            nfr_trains[row.nfr_train] += 1
    return {
        "issues": len(rows),
        "by_milestone": {key: dict(value) for key, value in by_milestone.items()},
        "by_lane_claimable_open": dict(by_lane),
        "nfr_trains": dict(nfr_trains),
        "claimable_open": claimable_open,
    }
