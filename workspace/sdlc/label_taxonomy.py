"""Label taxonomy (#1860): one table of legacy → new names.

Phase 1 readers call ``expand()`` so a label set means the same thing whether
it carries the legacy name (``BACKEND``) or the new one (``area:backend``).
``/workspace/notaire-labels-apply.sh`` (Owner) performs the renames; phase 2
drops the legacy names from code. See
docs/300-development/305-project-management/LABELS.md.
"""

from __future__ import annotations

from typing import Iterable

# Legacy name → new name. Two legacy names onto one new name is a merge.
RENAMES: dict[str, str] = {
    "BACKEND": "area:backend",
    "FRONTEND": "area:frontend",
    "DB": "area:db",
    "DEVOPS": "area:devops",
    "TEST": "area:test",
    "DOC": "area:docs",
    "enhancement": "type:feature",
    "bug": "type:bug",
    "REFACTOR": "type:refactor",
    "chore": "type:chore",
    "TAREA": "type:chore",
    "ci": "type:ci",
    "roadmap": "type:epic",
    "in-progress": "status:in-progress",
    "ready-for-dev": "status:ready",
    "in-review": "status:in-review",
    "blocked": "status:blocked",
    "needs-info": "status:needs-info",
    "security": "concern:security",
    "a11y": "concern:a11y",
    "i18n": "concern:i18n",
    "ux": "concern:ux",
    "risk": "concern:risk",
    "tech-debt": "concern:tech-debt",
    "requerimiento-funcional": "trace:rf",
    "requerimiento-no-funcional": "trace:rnf",
    "CASO-DE-USO": "trace:cu",
    "audit-2026-09": "source:audit-2026-09",
    "sdlc-exception": "process:sdlc-exception",
}

# Created if missing (no legacy equivalent).
CREATE: dict[str, tuple[str, str]] = {
    "release:breaking": ("b60205", "Breaking change: semver major, CHANGELOG Breaking section"),
}

# Deleted by the Owner script (phase:* and labels with no or negligible use).
DELETE: tuple[str, ...] = (
    "phase:1-analysis", "phase:2-foundation", "phase:3-backend-api",
    "phase:4-frontend", "phase:5-observability", "phase:6-deprecation",
    "Cursor", "maven", "duplicate", "invalid", "question",
    "good first issue", "help wanted",
)

LEGACY: dict[str, frozenset[str]] = {}
for _old, _new in RENAMES.items():
    LEGACY[_new] = LEGACY.get(_new, frozenset()) | {_old}

TYPE_LABELS = frozenset(new for new in RENAMES.values() if new.startswith("type:"))


def expand(labels: Iterable[str]) -> set[str]:
    """The label set plus every legacy/new equivalent of each member."""
    names = set(labels)
    for name in list(names):
        if name in RENAMES:
            new = RENAMES[name]
            names.add(new)
            names |= LEGACY[new]
        names |= LEGACY.get(name, frozenset())
    return names


def type_labels(labels: Iterable[str]) -> set[str]:
    """Distinct ``type:`` labels, legacy names folded in."""
    return {name for name in expand(labels) if name in TYPE_LABELS}


def has(labels: Iterable[str], new_name: str) -> bool:
    return new_name in expand(labels)


def wip_label(existing: Iterable[str]) -> str:
    """The in-progress label to write: the new name once the repo has it."""
    return RENAMES["in-progress"] if RENAMES["in-progress"] in set(existing) else "in-progress"
