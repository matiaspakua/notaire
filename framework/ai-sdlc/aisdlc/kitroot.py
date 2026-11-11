"""Where the kit's data (templates, profiles, openapi, plugins, bin) lives.

In a source checkout it sits beside the package; an installed wheel carries it in `aisdlc/_kit`."""
from __future__ import annotations

from pathlib import Path

PACKAGE = Path(__file__).resolve().parent
WHEEL_DATA = PACKAGE / "_kit"


def kit_root() -> Path:
    return WHEEL_DATA if WHEEL_DATA.is_dir() else PACKAGE.parent
