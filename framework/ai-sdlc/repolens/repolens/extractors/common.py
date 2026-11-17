"""Helpers every extractor shares: decoding, tags, route and path mentions, chunking."""

from __future__ import annotations

import re
from dataclasses import dataclass, field

from ..model import Edge, Extraction, Node, Ref, file_id

HTTP_VERBS = ("GET", "POST", "PUT", "PATCH", "DELETE")
_PATH_MENTION = re.compile(r"(?<![\w/.:-])((?:[\w.-]+/)+[\w.-]+\.[A-Za-z]{1,6})\b")
_IDENT_MENTION = re.compile(r"^[A-Za-z_][A-Za-z0-9_.]{3,80}(?:\(\))?$")
_PARAM = re.compile(r"\$\{[^}]*\}|\{\{[^}]*\}\}|\{[^}/]*\}|:[A-Za-z_]\w*|<[^>/]*>")


@dataclass
class Options:
    """What workers need from the config (kept small: it is pickled to every worker)."""

    project: str
    tags: dict[str, str] = field(default_factory=dict)
    route_prefixes: list[str] = field(default_factory=list)

    def __post_init__(self) -> None:
        self._tag_res = [(name, re.compile(rx)) for name, rx in self.tags.items()]
        alts = "|".join(re.escape(p.rstrip("/")) for p in sorted(self.route_prefixes, key=len, reverse=True))
        self._route_re = re.compile(
            rf"(?:\b({'|'.join(HTTP_VERBS)})\b[\s`'\"]*)?(?:[\w{{}}.:$/-]*?)((?:{alts})/[\w{{}}$:.\-/]*)"
        ) if alts else None

    def __getstate__(self):
        return {"project": self.project, "tags": self.tags, "route_prefixes": self.route_prefixes}

    def __setstate__(self, state):
        self.__init__(**state)


def decode(data: bytes) -> str | None:
    if b"\0" in data[:8192]:
        return None
    try:
        return data.decode("utf-8")
    except UnicodeDecodeError:
        return data.decode("latin-1")


def new_file(opts: Options, path: str, lang: str, family: str, text: str = "", lines: int = 0) -> Extraction:
    ex = Extraction(path=path, lang=lang)
    name = path.rsplit("/", 1)[-1]
    ex.nodes.append(Node(
        id=file_id(opts.project, path), project=opts.project, kind="file", name=name, path=path,
        qualname=path, lang=lang, start=1, end=lines, signature=family, text=text,
    ))
    return ex


def add_tags(ex: Extraction, src: str, text: str, opts: Options) -> None:
    seen = set()
    for name, rx in opts._tag_res:
        for m in rx.finditer(text):
            key = f"{name}:{m.group(1) if m.groups() else m.group(0)}"
            if key not in seen:
                seen.add(key)
                ex.refs.append(Ref(src, "tag", key))


def normalize_route(path: str) -> str:
    path = path.split("?", 1)[0].split("#", 1)[0]
    path = _PARAM.sub("{}", path)
    path = re.sub(r"/+", "/", path).rstrip("/")
    return path or "/"


def add_route_mentions(ex: Extraction, src: str, text: str, opts: Options) -> None:
    """Endpoint paths written in docs, configs and API test collections (e.g. Bruno, Postman)."""
    if opts._route_re is None:
        return
    seen = set()
    for m in opts._route_re.finditer(text):
        verb, path = m.group(1) or "", normalize_route(m.group(2))
        if (verb, path) not in seen:
            seen.add((verb, path))
            ex.refs.append(Ref(src, "http_call", path, verb))


def add_path_mentions(ex: Extraction, src: str, text: str, limit: int = 200) -> None:
    seen = set()
    for m in _PATH_MENTION.finditer(text):
        target = m.group(1).lstrip("./")
        if target not in seen and "://" not in target:
            seen.add(target)
            ex.refs.append(Ref(src, "mention", target))
            if len(seen) >= limit:
                return


def add_identifier_mention(ex: Extraction, src: str, token: str) -> None:
    token = token.strip().rstrip("()")
    if "/" in token or "." in token.rsplit("/", 1)[-1][-6:]:
        ex.refs.append(Ref(src, "mention", token.lstrip("./")))
    elif _IDENT_MENTION.match(token) and (any(c.isupper() for c in token[1:]) or "_" in token):
        ex.refs.append(Ref(src, "mention", token))


def chunk_lines(lines: list[str], size: int = 1500) -> list[tuple[int, int, str]]:
    """Split text into ~size-char chunks on line boundaries; returns (start, end, text), 1-based."""
    chunks, buf, start, count = [], [], 1, 0
    for i, line in enumerate(lines, 1):
        buf.append(line)
        count += len(line) + 1
        if count >= size:
            chunks.append((start, i, "\n".join(buf)))
            buf, start, count = [], i + 1, 0
    if buf and any(s.strip() for s in buf):
        chunks.append((start, len(lines), "\n".join(buf)))
    return chunks


def add_chunks(ex: Extraction, opts: Options, path: str, lines: list[str], lang: str) -> None:
    fid = file_id(opts.project, path)
    for start, end, text in chunk_lines(lines):
        cid = f"{fid}#L{start}"
        ex.nodes.append(Node(
            id=cid, project=opts.project, kind="chunk", name=f"{path.rsplit('/', 1)[-1]}:{start}-{end}",
            path=path, qualname=f"L{start}-{end}", lang=lang, start=start, end=end, text=text[:1500],
        ))
        ex.edges.append(Edge(fid, cid, "contains"))
