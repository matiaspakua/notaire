"""What a run step received and what it produced (#2435).

Both sides share one shape, validated before anything reaches the evidence ledger:

    {"request": str, "prompt": str, "summary": str,
     "files": [repo-relative path], "commits": [hex sha], "links": [{"label": str, "url": http(s)}]}

The ledger is evidence, not a secret store: callers must not put credentials in a prompt.
"""
from __future__ import annotations

import re
from urllib.parse import urlsplit

from aisdlc.errors import KitError

SIDES = ("inputs", "outputs")
TEXT_LIMITS = {"request": 4_000, "prompt": 20_000, "summary": 4_000}
LIST_LIMIT = 200
PATH_LIMIT = 512
LABEL_LIMIT = 200
URL_LIMIT = 2_000
COMMIT = re.compile(r"[0-9a-f]{7,40}")


def _refuse(detail: str) -> KitError:
    return KitError("invalid_body", detail)


def _text(side: str, key: str, value: object) -> str:
    if not isinstance(value, str):
        raise _refuse(f"{side}.{key} must be text")
    if len(value) > TEXT_LIMITS[key]:
        raise _refuse(f"{side}.{key} is longer than {TEXT_LIMITS[key]} characters")
    return value


def _items(side: str, key: str, value: object) -> list:
    if not isinstance(value, list):
        raise _refuse(f"{side}.{key} must be a list")
    if len(value) > LIST_LIMIT:
        raise _refuse(f"{side}.{key} has more than {LIST_LIMIT} items")
    return value


def _path(side: str, value: object) -> str:
    if not isinstance(value, str) or not value or len(value) > PATH_LIMIT:
        raise _refuse(f"{side}.files entries must be relative paths up to {PATH_LIMIT} characters")
    parts = value.replace("\\", "/").split("/")
    if value.startswith(("/", "\\")) or re.match(r"[A-Za-z]:", value) or ".." in parts:
        raise _refuse(f"{side}.files entry {value!r} must be a relative path inside the repository")
    return value


def _commit(side: str, value: object) -> str:
    if not isinstance(value, str) or not COMMIT.fullmatch(value):
        raise _refuse(f"{side}.commits entry {value!r} is not a commit SHA (7-40 lowercase hex)")
    return value


def _link(side: str, value: object) -> dict:
    if not isinstance(value, dict) or set(value) - {"label", "url"} or not isinstance(value.get("url"), str):
        raise _refuse(f"{side}.links entries are {{label, url}}")
    url = value["url"]
    parts = urlsplit(url)
    if parts.scheme not in ("http", "https") or not parts.netloc or len(url) > URL_LIMIT:
        raise _refuse(f"{side}.links url {url[:80]!r} must be an http or https URL")
    label = value.get("label", url)
    if not isinstance(label, str) or len(label) > LABEL_LIMIT:
        raise _refuse(f"{side}.links label must be text up to {LABEL_LIMIT} characters")
    return {"label": label, "url": url}


def normalize_side(side: str, raw: object) -> dict:
    if not isinstance(raw, dict):
        raise _refuse(f"{side} must be an object")
    unknown = sorted(set(raw) - set(TEXT_LIMITS) - {"files", "commits", "links"})
    if unknown:
        raise _refuse(f"{side} has unknown keys: {', '.join(unknown)}")
    clean: dict = {}
    for key in TEXT_LIMITS:
        if key in raw:
            clean[key] = _text(side, key, raw[key])
    if "files" in raw:
        clean["files"] = [_path(side, v) for v in _items(side, "files", raw["files"])]
    if "commits" in raw:
        clean["commits"] = [_commit(side, v) for v in _items(side, "commits", raw["commits"])]
    if "links" in raw:
        clean["links"] = [_link(side, v) for v in _items(side, "links", raw["links"])]
    return clean


def normalize(raw: object) -> dict:
    """Validate a ``{"inputs": ..., "outputs": ...}`` document; either side may be absent."""
    if not isinstance(raw, dict):
        raise _refuse("step io must be an object with inputs and/or outputs")
    unknown = sorted(set(raw) - set(SIDES))
    if unknown:
        raise _refuse(f"step io has unknown keys: {', '.join(unknown)}; use inputs and outputs")
    return {side: normalize_side(side, raw[side]) for side in SIDES if side in raw}


def merge(step: dict, event: dict) -> dict:
    """Fold an event's inputs/outputs into a run step; later events overwrite the keys they carry."""
    for side in SIDES:
        if isinstance(event.get(side), dict):
            step[side] = {**step.get(side, {}), **event[side]}
    return step
