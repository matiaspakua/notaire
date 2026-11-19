"""Extractor registry. Add a family by registering `family -> callable` in EXTRACTORS.

Text extractors take (opts, path, lang, text); `pdf` takes bytes; `binary` takes no content.
Third-party extractors can register through the `repolens.extractors` entry-point group: each entry
point loads a callable `register(EXTRACTORS)`.
"""

from __future__ import annotations

from pathlib import Path

from ..model import Extraction
from .code import extract_code
from .common import Options, decode
from .docs import (
    extract_binary,
    extract_markdown,
    extract_pdf,
    extract_sql,
    extract_structured,
    extract_tabular,
    extract_text,
)

EXTRACTOR_VERSION = "6"

EXTRACTORS = {
    "code": extract_code,
    "markdown": extract_markdown,
    "sql": extract_sql,
    "structured": extract_structured,
    "tabular": extract_tabular,
    "text": extract_text,
}


def _load_plugins() -> None:
    try:
        from importlib.metadata import entry_points
    except ImportError:  # pragma: no cover
        return
    for ep in entry_points(group="repolens.extractors"):
        ep.load()(EXTRACTORS)


_load_plugins()


def extract(opts: Options, root: Path, path: str, lang: str, family: str) -> Extraction:
    full = root / path
    if family == "binary":
        return extract_binary(opts, path, lang)
    data = full.read_bytes()
    if family == "pdf":
        return extract_pdf(opts, path, lang, data)
    text = decode(data)
    if text is None:
        return extract_binary(opts, path, lang)
    fn = EXTRACTORS.get(family, extract_text)
    try:
        return fn(opts, path, lang, text)
    except Exception as exc:  # noqa: BLE001 - one bad file must not stop an index run
        ex = extract_text(opts, path, lang, text)
        ex.meta["error"] = f"{type(exc).__name__}: {exc}"[:300]
        return ex


def extract_job(job: tuple) -> tuple[str, str, int, int, Extraction]:
    """Process-pool entry point: job = (opts, root, path, lang, family, digest, size, mtime)."""
    from ..store import node_aux

    opts, root, path, lang, family, digest, size, mtime = job
    ex = extract(opts, Path(root), path, lang, family)
    ex.aux = [node_aux(n) for n in ex.nodes]
    return path, digest, size, mtime, ex


__all__ = ["EXTRACTORS", "EXTRACTOR_VERSION", "Options", "extract", "extract_job"]
