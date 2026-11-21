"""Helpers every extractor shares: decoding, tags, route and path mentions, chunking."""

from __future__ import annotations

import re
from dataclasses import dataclass, field

from ..model import Edge, Extraction, Node, Ref, file_id

HTTP_VERBS = ("GET", "POST", "PUT", "PATCH", "DELETE")
# A path is any run of path characters; everything else (spaces, quotes, brackets, `:`, `=`, `,`) ends one.
_NOT_PATH = re.compile(r"[^\w./@+~-]+")
_FILE_NAME = re.compile(r"^[\w@+~-][\w.@+~-]*\.[A-Za-z][A-Za-z0-9]{0,7}$")
_IDENT_TOKEN = re.compile(r"(?<![\w$./-])[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)*")
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


def path_tokens(text: str, limit: int = 200, known_names: bool = False) -> list[str]:
    """Tokens that can name a repository file or directory: with a `/` (full, `./`, `../` or web-root
    `/icons/x.png` paths, directories), or a bare file name with an extension (`docker-compose.yml`). The
    linker keeps only those that resolve to a real file or directory, so a false candidate costs a lookup."""
    out: dict[str, None] = {}
    for raw in _NOT_PATH.split(text):
        token = raw.rstrip(".-")
        if not 3 <= len(token) <= 200 or token.startswith("//") or not any(c.isalpha() for c in token):
            continue
        if "/" in token or _FILE_NAME.match(token) and (not known_names or _is_file_name(token)):
            out[token] = None
            if len(out) >= limit:
                break
    return list(out)


def _is_file_name(token: str) -> bool:
    """A bare name with an extension repolens recognizes: `Folio.java`, `logo.png`, not `this.store`."""
    from ..scanner import is_file_name

    return is_file_name(token)


def add_path_mentions(ex: Extraction, src: str, text: str, limit: int = 200, known_names: bool = False) -> None:
    """`known_names` (code): a bare name must carry a recognized extension, or `obj.field` floods the limit."""
    for token in path_tokens(text, limit, known_names):
        ex.refs.append(Ref(src, "mention", token))


def _code_like(token: str) -> bool:
    """`InvoiceLedger`, `invoice_ledger`, `Ledger.reconcile`: not a prose word, not an ALL_CAPS constant."""
    if not any(c.islower() for c in token):
        return False
    last = token.rsplit(".", 1)[-1]
    camel = any(c.isupper() for c in last[1:])
    snake = "_" in last.strip("_")
    qualified = "." in token and any(part[:1].isupper() for part in token.split("."))
    return camel or snake or qualified


def add_identifier_mentions(ex: Extraction, src: str, text: str, limit: int = 200) -> None:
    """Code identifiers named in any text: CamelCase with an inner capital, snake_case or dotted. The linker
    keeps those naming a type or function (at most three candidates)."""
    seen: set[str] = set()
    for m in _IDENT_TOKEN.finditer(text):
        token = m.group(0)
        if token in seen or not 4 <= len(token) <= 120:
            continue
        if _code_like(token):
            seen.add(token)
            ex.refs.append(Ref(src, "mention", token))
            if len(seen) >= limit:
                return


def add_identifier_mention(ex: Extraction, src: str, token: str) -> None:
    """One code span: a path (kept relative, the linker resolves it against the file) or an identifier."""
    token = token.strip().rstrip("()")
    is_path = "/" in token or "." in token.rsplit("/", 1)[-1][-6:]
    if is_path or _IDENT_MENTION.match(token) and _code_like(token):
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
