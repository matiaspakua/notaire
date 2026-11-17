"""Non-code extractors: Markdown, SQL, structured config, tabular data, PDF and plain text."""

from __future__ import annotations

import csv
import io
import json
import re

from ..model import Edge, Extraction, Node, Ref, file_id
from .common import (
    Options,
    add_chunks,
    add_identifier_mention,
    add_path_mentions,
    add_route_mentions,
    add_tags,
    new_file,
)

_HEADING = re.compile(r"^(#{1,6})\s+(.+?)\s*#*\s*$")
_FENCE = re.compile(r"^\s*(```|~~~)")
_LINK = re.compile(r"!?\[[^\]]*\]\(\s*<?([^)\s>]+)>?(?:\s+\"[^\"]*\")?\s*\)")
_CODE_SPAN = re.compile(r"`([^`\n]{3,160})`")
_SLUG = re.compile(r"[^\w\- ]+")
_SQL_CREATE = re.compile(
    r"\bCREATE\s+(?:OR\s+REPLACE\s+)?(?:UNLOGGED\s+|TEMP(?:ORARY)?\s+)?(TABLE|VIEW|MATERIALIZED\s+VIEW)\s+"
    r"(?:IF\s+NOT\s+EXISTS\s+)?([\w.\"`\[\]]+)", re.I)
_SQL_USE = re.compile(
    r"\b(?:ALTER\s+TABLE(?:\s+IF\s+EXISTS)?(?:\s+ONLY)?|REFERENCES|INSERT\s+INTO|UPDATE|DELETE\s+FROM|"
    r"CREATE\s+(?:UNIQUE\s+)?INDEX(?:\s+CONCURRENTLY)?(?:\s+IF\s+NOT\s+EXISTS)?\s+\w+\s+ON|DROP\s+TABLE"
    r"(?:\s+IF\s+EXISTS)?|JOIN|FROM)\s+([\w.\"`\[\]]+)", re.I)
_SQL_KEYWORDS = {"select", "where", "set", "values", "only", "lateral", "unnest", "generate_series", "dual"}


def _table_name(raw: str) -> str:
    return raw.strip("\"`[]").rsplit(".", 1)[-1].strip("\"`[]").lower()


def _slug(text: str) -> str:
    return _SLUG.sub("", text.lower()).strip().replace(" ", "-")[:80] or "section"


def extract_markdown(opts: Options, path: str, lang: str, text: str) -> Extraction:
    lines = text.splitlines()
    ex = new_file(opts, path, lang, "markdown", lines=len(lines))
    fid = ex.nodes[0].id
    sections: list[tuple[int, str, int, str]] = []  # (level, title, start line, id)
    stack: list[tuple[int, str, str]] = []  # (level, id, qual)
    in_fence = False
    slugs: dict[str, int] = {}
    for i, line in enumerate(lines, 1):
        if _FENCE.match(line):
            in_fence = not in_fence
            continue
        m = None if in_fence else _HEADING.match(line)
        if not m:
            continue
        level, title = len(m.group(1)), m.group(2).strip()
        slug = _slug(title)
        slugs[slug] = slugs.get(slug, 0) + 1
        if slugs[slug] > 1:
            slug = f"{slug}-{slugs[slug] - 1}"
        while stack and stack[-1][0] >= level:
            stack.pop()
        parent_id = stack[-1][1] if stack else fid
        qual = f"{stack[-1][2]} > {title}" if stack else title
        sid = f"{fid}#{slug}"
        sections.append((level, title, i, sid))
        ex.nodes.append(Node(
            id=sid, project=opts.project, kind="section", name=title[:120], path=path, qualname=qual[:300],
            lang=lang, start=i, end=i,
        ))
        ex.edges.append(Edge(parent_id, sid, "contains"))
        stack.append((level, sid, qual))

    bounds = [(s[2], sections[k + 1][2] - 1 if k + 1 < len(sections) else len(lines), s[3])
              for k, s in enumerate(sections)]
    by_id = {n.id: n for n in ex.nodes}
    first_heading = sections[0][2] if sections else len(lines) + 1
    owners = [(1, first_heading - 1, fid)] + bounds
    for start, end, owner in owners:
        if end < start:
            continue
        body = "\n".join(lines[start - 1:end])
        node = by_id[owner]
        if owner != fid:
            node.end = end
            node.text = _plain(body)[:1200]
        _doc_refs(ex, owner, body, path, opts)
    title = sections[0][1] if sections else path.rsplit("/", 1)[-1]
    ex.nodes[0].text = (title + " — " + _plain("\n".join(lines[: min(len(lines), 40)])))[:1200]
    if not sections and len(text) > 4000:
        add_chunks(ex, opts, path, lines, lang)
    add_tags(ex, fid, text, opts)
    return ex


def _plain(md: str) -> str:
    md = re.sub(r"```[\s\S]*?```", " ", md)
    md = re.sub(r"!?\[([^\]]*)\]\([^)]*\)", r"\1", md)
    md = re.sub(r"[#>*_|`-]{1,}", " ", md)
    return " ".join(md.split())


def _doc_refs(ex: Extraction, owner: str, body: str, path: str, opts: Options) -> None:
    base = path.rsplit("/", 1)[0] if "/" in path else ""
    for m in _LINK.finditer(body):
        target = m.group(1)
        if re.match(r"^[a-z][a-z0-9+.-]*:", target, re.I) or target.startswith("#"):
            continue
        target = target.split("#", 1)[0].split("?", 1)[0]
        if target:
            ex.refs.append(Ref(owner, "link", _join(base, target)))
    for m in _CODE_SPAN.finditer(body):
        add_identifier_mention(ex, owner, m.group(1))
    add_path_mentions(ex, owner, body)
    add_route_mentions(ex, owner, body, opts)


def _join(base: str, rel: str) -> str:
    if rel.startswith("/"):
        return rel.lstrip("/")
    parts = base.split("/") if base else []
    for piece in rel.split("/"):
        if piece in ("", "."):
            continue
        if piece == "..":
            if parts:
                parts.pop()
        else:
            parts.append(piece)
    return "/".join(parts)


def extract_sql(opts: Options, path: str, lang: str, text: str) -> Extraction:
    lines = text.splitlines()
    ex = new_file(opts, path, lang, "sql", lines=len(lines))
    fid = ex.nodes[0].id
    clean = re.sub(r"--[^\n]*", " ", text)
    created = []
    for m in _SQL_CREATE.finditer(clean):
        name = _table_name(m.group(2))
        body = clean[m.end(): m.end() + 1500]
        depth, end = 0, len(body)
        for k, ch in enumerate(body):
            depth += ch == "("
            depth -= ch == ")"
            if ch == ";" and depth <= 0:
                end = k
                break
        columns = " ".join(body[:end].split())[:600]
        created.append(name)
        ex.refs.append(Ref(fid, "table", name, "defines\t" + columns))
    for m in _SQL_USE.finditer(clean):
        name = _table_name(m.group(1))
        if name and name not in created and name not in _SQL_KEYWORDS and not name.isdigit():
            ex.refs.append(Ref(fid, "table", name, "uses"))
    header = re.match(r"\s*((?:--[^\n]*\n)+)", text)
    ex.nodes[0].text = (" ".join((header.group(1) if header else "").replace("-", " ").split())[:500]
                        + " | tables: " + ", ".join(dict.fromkeys(created)))
    add_tags(ex, fid, text, opts)
    return ex


def extract_structured(opts: Options, path: str, lang: str, text: str) -> Extraction:
    lines = text.splitlines()
    ex = new_file(opts, path, lang, "config", lines=len(lines))
    fid = ex.nodes[0].id
    data = _parse_structured(lang, text)
    flat = _flatten(data) if data is not None else []
    summary = "; ".join(f"{k}={v}" for k, v in flat[:150]) if flat else " ".join(text.split())
    ex.nodes[0].text = summary[:2000]
    name = path.rsplit("/", 1)[-1]
    if name == "package.json" and isinstance(data, dict):
        for section in ("dependencies", "devDependencies", "peerDependencies"):
            for dep in (data.get(section) or {}):
                ex.refs.append(Ref(fid, "depends", f"npm:{dep}"))
    elif name == "pom.xml":
        for m in re.finditer(r"<dependency>\s*<groupId>([^<]+)</groupId>\s*<artifactId>([^<]+)</artifactId>", text):
            ex.refs.append(Ref(fid, "depends", f"maven:{m.group(1)}:{m.group(2)}"))
    elif name in ("tsconfig.json", "jsconfig.json") and isinstance(data, dict):
        options = data.get("compilerOptions") or {}
        ex.meta["ts_paths"] = options.get("paths") or {}
        ex.meta["base_url"] = options.get("baseUrl") or "."
    add_route_mentions(ex, fid, text, opts)
    add_path_mentions(ex, fid, text, limit=60)
    add_tags(ex, fid, text, opts)
    if len(text) > 4000:
        add_chunks(ex, opts, path, lines, lang)
    return ex


def _parse_structured(lang: str, text: str):
    try:
        if lang == "json":
            try:
                return json.loads(text)
            except ValueError:
                relaxed = re.sub(r"(?m)^\s*//[^\n]*$|/\*[\s\S]*?\*/", "", text)
                return json.loads(re.sub(r",(\s*[}\]])", r"\1", relaxed))
        if lang in ("yaml", "yml"):
            import yaml

            docs = [d for d in yaml.safe_load_all(text) if d is not None]
            return docs[0] if len(docs) == 1 else docs
        if lang == "toml":
            import tomllib

            return tomllib.loads(text)
        if lang in ("properties", "ini", "cfg", "conf"):
            pairs = {}
            for line in text.splitlines():
                m = re.match(r"^\s*([^#;=:\s][^=:]*?)\s*[=:]\s*(.*)$", line)
                if m:
                    pairs[m.group(1)] = m.group(2)
            return pairs
    except Exception:  # noqa: BLE001 - unparsable config still indexes as text
        return None
    return None


def _flatten(data, prefix: str = "", out: list | None = None, limit: int = 400) -> list:
    out = [] if out is None else out
    if len(out) >= limit:
        return out
    if isinstance(data, dict):
        for k, v in data.items():
            _flatten(v, f"{prefix}.{k}" if prefix else str(k), out, limit)
    elif isinstance(data, list):
        for i, v in enumerate(data[:20]):
            _flatten(v, f"{prefix}[{i}]", out, limit)
    else:
        out.append((prefix, str(data)[:120]))
    return out


def extract_tabular(opts: Options, path: str, lang: str, text: str) -> Extraction:
    lines = text.splitlines()
    ex = new_file(opts, path, lang, "data", lines=len(lines))
    dialect = "excel-tab" if lang == "tsv" else "excel"
    rows = list(csv.reader(io.StringIO("\n".join(lines[:6])), dialect=dialect))
    header = rows[0] if rows else []
    ex.nodes[0].signature = "columns: " + ", ".join(header)[:400]
    ex.nodes[0].text = f"{len(lines) - 1} rows; columns: " + ", ".join(header) + " | sample: " + " | ".join(
        ", ".join(r) for r in rows[1:4])[:1200]
    add_tags(ex, ex.nodes[0].id, text[:20000], opts)
    return ex


def extract_pdf(opts: Options, path: str, lang: str, data: bytes) -> Extraction:
    ex = new_file(opts, path, lang, "pdf")
    fid = ex.nodes[0].id
    try:
        from pypdf import PdfReader
    except ImportError:
        ex.nodes[0].text = "pdf (install repolens[pdf] to index its text)"
        return ex
    try:
        reader = PdfReader(io.BytesIO(data))
        pages = [(p.extract_text() or "") for p in reader.pages]
    except Exception as exc:  # noqa: BLE001 - corrupt PDFs keep their file node
        ex.nodes[0].text = f"pdf (unreadable: {type(exc).__name__})"
        return ex
    ex.nodes[0].end = len(pages)
    ex.nodes[0].text = " ".join(" ".join(pages[:2]).split())[:1500]
    for i, page in enumerate(pages, 1):
        body = " ".join(page.split())
        if not body:
            continue
        pid = f"{fid}#page-{i}"
        ex.nodes.append(Node(id=pid, project=opts.project, kind="chunk", name=f"{path.rsplit('/', 1)[-1]} p.{i}",
                             path=path, qualname=f"page {i}", lang=lang, start=i, end=i, text=body[:2000]))
        ex.edges.append(Edge(fid, pid, "contains"))
        add_tags(ex, pid, body, opts)
        add_path_mentions(ex, pid, body, limit=30)
    return ex


def extract_text(opts: Options, path: str, lang: str, text: str) -> Extraction:
    lines = text.splitlines()
    ex = new_file(opts, path, lang, "text", lines=len(lines))
    fid = ex.nodes[0].id
    ex.nodes[0].text = " ".join(text[:2000].split())
    add_tags(ex, fid, text, opts)
    add_route_mentions(ex, fid, text, opts)
    add_path_mentions(ex, fid, text, limit=60)
    if len(text) > 4000:
        add_chunks(ex, opts, path, lines, lang)
    return ex


def extract_binary(opts: Options, path: str, lang: str) -> Extraction:
    ex = new_file(opts, path, lang, "binary")
    ex.nodes[0].text = f"binary {lang} asset"
    return ex


__all__ = [
    "extract_binary", "extract_markdown", "extract_pdf", "extract_sql", "extract_structured",
    "extract_tabular", "extract_text", "file_id",
]
