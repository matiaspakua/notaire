"""Graph vocabulary: nodes, edges and the unresolved references extractors emit."""

from __future__ import annotations

import hashlib
import re
from dataclasses import dataclass, field

SYMBOL_KINDS = frozenset({"class", "interface", "enum", "function", "method", "variable", "type"})
DOC_KINDS = frozenset({"section", "chunk"})
VIRTUAL_KINDS = frozenset({"dir", "route", "table", "tag", "external", "project"})


@dataclass(slots=True)
class Node:
    id: str
    project: str
    kind: str
    name: str
    path: str = ""
    qualname: str = ""
    lang: str = ""
    start: int = 0
    end: int = 0
    signature: str = ""
    text: str = ""


@dataclass(slots=True)
class Edge:
    src: str
    dst: str
    kind: str
    weight: float = 1.0


@dataclass(slots=True)
class Ref:
    """A reference an extractor saw but cannot resolve alone (needs the whole graph)."""

    src: str
    kind: str
    target: str
    extra: str = ""


@dataclass
class Extraction:
    path: str
    lang: str
    nodes: list[Node] = field(default_factory=list)
    edges: list[Edge] = field(default_factory=list)
    refs: list[Ref] = field(default_factory=list)
    meta: dict = field(default_factory=dict)
    aux: list[tuple] = field(default_factory=list)  # per node: (tok_name, tok_path, tok_body, vhash)


def file_id(project: str, path: str) -> str:
    return f"{project}:{path}"


def symbol_id(project: str, path: str, qualname: str) -> str:
    return f"{project}:{path}#{qualname}"


def virtual_id(project: str, kind: str, key: str) -> str:
    return f"{project}:{kind}:{key}"


_CAMEL = re.compile(r"[A-Z]+(?=[A-Z][a-z])|[A-Z]?[a-z]+|[A-Z]+|\d+")
_WORD = re.compile(r"[A-Za-z0-9]+")


def split_identifier(text: str) -> list[str]:
    """`MfaEnrollmentJpaEntity` -> ['mfa', 'enrollment', 'jpa', 'entity']."""
    out: list[str] = []
    for word in _WORD.findall(text):
        parts = _CAMEL.findall(word)
        out.extend(p.lower() for p in parts if p)
    return out


def search_tokens(*texts: str) -> str:
    """Words plus their identifier splits, so `TotpVerificationService` matches `totp verification`."""
    seen: dict[str, None] = {}
    for text in texts:
        for word in _WORD.findall(text or ""):
            low = word.lower()
            seen.setdefault(low, None)
            for part in split_identifier(word):
                seen.setdefault(part, None)
    return " ".join(seen)


def text_hash(text: str) -> int:
    """Stable signed 64-bit content hash (the key of the vector cache)."""
    return int.from_bytes(hashlib.blake2b(text.encode("utf-8"), digest_size=8).digest(), "big", signed=True)
