"""Locate an OpenSpec change whether it is still active or already archived.

Gate 5 moves `changes/<name>` to `changes/archive/<date>-<name>` (#1623). Tests that
pin wording in a change must follow it there instead of breaking on archive.
"""

from pathlib import Path

CHANGES = Path(__file__).resolve().parents[1] / "openspec" / "changes"


def change(name: str) -> Path:
    active = CHANGES / name
    if active.is_dir():
        return active
    archived = sorted((CHANGES / "archive").glob(f"*-{name}"))
    if not archived:
        raise FileNotFoundError(f"OpenSpec change {name} is neither active nor archived")
    return archived[-1]
