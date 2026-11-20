"""Link pass: turn the unresolved refs of every file into typed edges and virtual nodes.

It runs over the whole store (all projects), so cross-project links such as a frontend repo calling a
backend repo's REST route resolve too. It is rebuilt from scratch on every index run; it is fast
because extraction (the expensive part) is cached per file.
"""

from __future__ import annotations

import bisect
import json
import math
import posixpath
import re
import subprocess
import sys
import zlib
from collections import Counter, defaultdict
from collections.abc import Iterator
from dataclasses import dataclass, field
from pathlib import Path

from .config import Config
from .model import SYMBOL_KINDS, Edge, Node, virtual_id

TYPE_KINDS = frozenset({"class", "interface", "enum", "type"})
SYMBOL_REFS = frozenset({"type", "call"})
# Library methods that hand back an element of their receiver (`list.get(0)`, `optional.orElseThrow()`), or the
# same elements in another container (`list.stream()`, `stream.filter(...)`), and Spring Data repository methods
# typed by the entity of `extends JpaRepository<Entity, Id>` ('' = the entity itself).
ELEMENT_METHODS = frozenset({
    "get", "getFirst", "getLast", "orElse", "orElseThrow", "orElseGet", "next", "join", "poll", "peek", "pop",
    "element",
})
SAME_ELEMENT_METHODS = {
    "stream": "Stream", "parallelStream": "Stream", "filter": "Stream", "sorted": "Stream", "distinct": "Stream",
    "limit": "Stream", "skip": "Stream", "toList": "List", "findFirst": "Optional", "findAny": "Optional",
    "min": "Optional", "max": "Optional", "iterator": "Iterator", "subList": "List", "reversed": "List",
    "values": "Collection", "copyOf": "List", "unmodifiableList": "List",
}
REPOSITORY_TYPES = frozenset({
    "JpaRepository", "CrudRepository", "ListCrudRepository", "PagingAndSortingRepository",
    "ListPagingAndSortingRepository", "Repository", "JpaSpecificationExecutor", "QueryByExampleExecutor",
})
REPOSITORY_METHODS = {
    "save": "", "saveAndFlush": "", "getReferenceById": "", "getById": "", "getOne": "",
    "findById": "Optional", "findOne": "Optional", "findAll": "List", "findAllById": "List", "saveAll": "List",
    "saveAllAndFlush": "List",
}
CALLABLE_KINDS = frozenset({"function", "method", "class", "variable"})
LANG_FAMILY = {
    "java": "jvm", "kotlin": "jvm", "scala": "jvm", "typescript": "js", "tsx": "js", "javascript": "js",
}
_EMITTED = {".js": (".ts", ".tsx"), ".jsx": (".tsx",), ".mjs": (".mts",), ".cjs": (".cts",)}
TS_EXTS = ("", ".ts", ".tsx", ".js", ".jsx", ".mjs", ".cjs", ".d.ts", "/index.ts", "/index.tsx", "/index.js")
TEST_PATH = re.compile(
    r"(^|/)(tests?|__tests__|spec|e2e|it)/|(Test|Tests|IT|Spec)\.(java|kt|cs|scala)$|"
    r"[._-](test|spec)\.[cm]?[jt]sx?$|(^|/)test_[^/]+\.py$|_test\.(py|go)$|_spec\.rb$"
)
_TEST_SUFFIX = re.compile(r"(Pg)?(Integration|Unit|Characterization|Contract|Web|Mvc|E2e)?(Tests?|IT|Spec)$")


def is_test_path(path: str) -> bool:
    return bool(TEST_PATH.search(path))


def test_stem(path: str) -> str:
    base = path.rsplit("/", 1)[-1]
    base = re.sub(r"\.(test|spec)(?=\.[^.]+$)", "", base)
    stem = base.split(".", 1)[0]
    stem = re.sub(r"^test_|_test$", "", stem)
    return _TEST_SUFFIX.sub("", stem) or stem


@dataclass
class _Index:
    nodes: dict[str, Node] = field(default_factory=dict)
    files: dict[tuple[str, str], str] = field(default_factory=dict)
    by_name: dict[str, list[str]] = field(default_factory=lambda: defaultdict(list))
    by_qual: dict[str, list[str]] = field(default_factory=lambda: defaultdict(list))
    by_stem: dict[str, list[str]] = field(default_factory=lambda: defaultdict(list))
    by_fqn: dict[str, str] = field(default_factory=dict)
    by_suffix: dict[str, list[str]] = field(default_factory=lambda: defaultdict(list))
    dirs: dict[tuple[str, str], str] = field(default_factory=dict)
    dir_by_suffix: dict[str, list[str]] = field(default_factory=lambda: defaultdict(list))
    names: set[str] = field(default_factory=set)  # the last segment of every file and directory path
    file_of: dict[str, str] = field(default_factory=dict)
    by_file: dict[str, list[str]] = field(default_factory=lambda: defaultdict(list))
    stems_sorted: list[str] = field(default_factory=list)
    meta: dict[tuple[str, str], dict] = field(default_factory=dict)
    lang: dict[tuple[str, str], str] = field(default_factory=dict)


class Linker:
    def __init__(self, cfg: Config, roots: dict[str, str]):
        self.cfg = cfg
        self.roots = roots
        self.ix = _Index()
        self.edges: dict[tuple[str, str, str], float] = {}
        self.virtual: dict[str, Node] = {}
        self.path_mentions: dict[tuple[str, str, str], tuple[list[str], bool]] = {}
        self.imported: dict[str, set[str]] = defaultdict(set)
        self.git_cache: dict[str, tuple[str, str]] = {}
        self.family: dict[str, str] = {}
        self.buckets: dict[tuple[str, bool, str], _Bucket] = {}

    # -- entry -----------------------------------------------------------------------------
    def run(self, nodes, refs, file_meta) -> tuple[list[Node], Iterator[Edge]]:
        """`refs` returns a fresh iterator over every unresolved reference. Symbol references (most of
        them) are resolved in a second pass straight from that iterator instead of being held in memory;
        they need every import resolved first, and edge weights add up the same in any order."""
        self._index(nodes, file_meta)
        by_kind: dict[str, list] = defaultdict(list)
        for row in refs():
            if row[2] not in SYMBOL_REFS:
                by_kind[row[2]].append(self._canonical(row))
        self._dirs()
        for row in by_kind["import"]:
            self._import(*row)
        for row in by_kind["inherit"]:  # the class hierarchy first: typed calls resolve through it
            self._symbol_ref(*row)
        self._index_types(by_kind["returns"], by_kind["supertype"])
        for row in refs():
            if row[2] in SYMBOL_REFS:
                self._symbol_ref(*self._canonical(row))
        self._overrides()
        self._tests()
        self._routes(by_kind["route"], by_kind["http_call"])
        self._tables(by_kind["table"])
        self._tags(by_kind["tag"])
        for row in by_kind["link"]:
            self._link(*row)
        for row in by_kind["mention"]:
            self._mention(*row)
        for _project, src, _k, dst, kind in by_kind["edge"]:
            self._edge(src, dst, kind)
        for row in by_kind["depends"]:
            self._edge(row[1], self._external(row[0], row[3]), "depends_on")
        for project, root in self.roots.items():
            self._git(project, Path(root))
        edges = (Edge(s, d, k, round(w, 4)) for (s, d, k), w in self.edges.items() if s != d)
        return list(self.virtual.values()), edges

    def _canonical(self, row: tuple) -> tuple:
        """The reference with its source id replaced by the node's own id string (one object, not one
        decoded copy per reference)."""
        project, src, kind, target, extra = row
        node = self.ix.nodes.get(src)
        return sys.intern(project), node.id if node is not None else src, kind, target, extra

    def _index(self, nodes, file_meta) -> None:
        ix = self.ix
        for n in nodes:
            ix.nodes[n.id] = n
            if n.kind == "file":
                ix.files[(n.project, n.path)] = n.id
                stem = n.name.split(".", 1)[0]
                ix.by_stem[stem.lower()].append(n.id)
                parts = n.path.split("/")
                ix.names.update(parts)
                for i in range(len(parts)):
                    ix.by_suffix["/".join(parts[i:])].append(n.id)
            elif n.kind in SYMBOL_KINDS:
                ix.by_name[n.name].append(n.id)
                ix.by_qual[n.qualname].append(n.id)
                ix.file_of[n.id] = n.id.split("#", 1)[0]
                ix.by_file[ix.file_of[n.id]].append(n.id)
        for project, path, lang, meta in file_meta:
            ix.meta[(project, path)] = meta
            ix.lang[(project, path)] = lang
            pkg = meta.get("package")
            if pkg:
                fid = ix.files.get((project, path))
                for sid in self._symbols_in(fid):
                    n = ix.nodes[sid]
                    if n.kind in TYPE_KINDS:
                        ix.by_fqn[f"{pkg}.{n.qualname}"] = sid
        ix.stems_sorted = sorted(ix.by_stem)
        self.ts_aliases = self._ts_aliases()

    def _symbols_in(self, fid: str | None) -> list[str]:
        return self.ix.by_file.get(fid, []) if fid else []

    def _stems_with_prefix(self, stem: str, max_extra: int = 12) -> list[str]:
        stems = self.ix.stems_sorted
        i = bisect.bisect_left(stems, stem)
        out = []
        while i < len(stems) and stems[i].startswith(stem):
            if len(stems[i]) - len(stem) <= max_extra:
                out.append(stems[i])
            i += 1
        return out

    # -- helpers ---------------------------------------------------------------------------
    def _edge(self, src: str, dst: str | None, kind: str, weight: float = 1.0) -> None:
        if not dst or src == dst:
            return
        key = (src, dst, kind)
        self.edges[key] = min(self.edges.get(key, 0.0) + weight, 3.0)

    def _vnode(self, project: str, kind: str, key: str, name: str, text: str = "") -> str:
        vid = virtual_id(project, kind, key)
        node = self.virtual.get(vid)
        if node is None:
            node = Node(id=vid, project=project, kind=kind, name=name, qualname=key, text=text)
            self.virtual[vid] = node
        elif text and not node.text:
            node.text = text
        return vid

    def _external(self, project: str, key: str) -> str:
        return self._vnode(project, "external", key, key)

    @staticmethod
    def _split(node_id: str) -> tuple[str, str]:
        project, rest = node_id.split(":", 1)
        return project, rest.split("#", 1)[0]

    # -- directories -----------------------------------------------------------------------
    def _dirs(self) -> None:
        for (project, path), fid in list(self.ix.files.items()):
            proj = self._vnode(project, "project", project, project)
            parent = posixpath.dirname(path)
            child = fid
            while True:
                if not parent:
                    self._edge(proj, child, "dir_contains")
                    break
                did = self._vnode(project, "dir", parent + "/", parent.rsplit("/", 1)[-1] + "/")
                if (project, parent) not in self.ix.dirs:
                    self.virtual[did].path = parent
                    self.ix.dirs[(project, parent)] = did
                    parts = parent.split("/")
                    for i in range(len(parts)):
                        self.ix.dir_by_suffix["/".join(parts[i:])].append(did)
                existed = (did, child, "dir_contains") in self.edges
                self._edge(did, child, "dir_contains")
                if existed:
                    break
                child, parent = did, posixpath.dirname(parent)

    # -- imports ---------------------------------------------------------------------------
    def _import(self, project: str, src: str, _kind: str, target: str, extra: str) -> None:
        _, path = self._split(src)
        lang = self.ix.lang.get((project, path), "")
        targets: list[str] = []
        if lang in ("java", "kotlin", "scala"):
            targets = self._import_jvm(target, extra)
        elif lang in ("typescript", "tsx", "javascript"):
            targets = self._import_ts(project, path, target)
        elif lang == "python":
            targets = self._import_py(project, path, target, extra)
        elif lang == "go":
            targets = self._import_by_dir(project, target)
        else:
            last = re.split(r"[.:/\\]+", target.strip("{}; "))[-1]
            targets = [s for s in self.ix.by_name.get(last, []) if self.ix.nodes[s].kind in TYPE_KINDS][:2]
        if not targets:
            ext = self._external_key(lang, target)
            if ext:
                self._edge(src, self._external(project, ext), "depends_on")
            return
        for t in targets:
            fid = self.ix.file_of.get(t, t)
            self._edge(src, t, "imports")
            if fid != t:
                self._edge(src, fid, "imports", 0.5)
            self.imported[src].add(fid)

    def _import_jvm(self, target: str, extra: str) -> list[str]:
        target = target.strip()
        if target.endswith(".*"):
            return []
        hit = self.ix.by_fqn.get(target)
        if hit is None and "." in target:
            hit = self.ix.by_fqn.get(target.rsplit(".", 1)[0])
        return [hit] if hit else []

    def _import_ts(self, project: str, path: str, target: str) -> list[str]:
        bases = []
        if target.startswith("."):
            bases.append(posixpath.normpath(posixpath.join(posixpath.dirname(path), target)))
        else:
            for alias_dir, prefix, repl in self.ts_aliases.get(project, []):
                if target.startswith(prefix) and (path.startswith(alias_dir) or not alias_dir):
                    bases.append(posixpath.normpath(posixpath.join(alias_dir, repl + target[len(prefix):])))
        for base in bases:
            stem, ext = posixpath.splitext(base)
            # ESM TypeScript imports the emitted file (`./format.js`): its source is `./format.ts`
            sources = [stem + e for e in _EMITTED.get(ext, ())]
            for candidate in (*sources, *(base + e for e in TS_EXTS)):
                fid = self.ix.files.get((project, candidate))
                if fid:
                    return [fid]
        return []

    def _ts_aliases(self) -> dict[str, list[tuple[str, str, str]]]:
        out: dict[str, list[tuple[str, str, str]]] = defaultdict(list)
        for (project, path), meta in self.ix.meta.items():
            paths = meta.get("ts_paths")
            if not paths:
                continue
            cfg_dir = posixpath.dirname(path)
            base = posixpath.normpath(posixpath.join(cfg_dir, meta.get("base_url", ".")))
            base = "" if base == "." else base
            for alias, targets in paths.items():
                if not targets:
                    continue
                prefix = alias.rstrip("*")
                repl = targets[0].rstrip("*")
                out[project].append((cfg_dir + "/" if cfg_dir else "", prefix,
                                     posixpath.join(posixpath.relpath(base or ".", cfg_dir or "."), repl)))
        for project in out:
            out[project].sort(key=lambda t: len(t[1]), reverse=True)
        return out

    def _import_py(self, project: str, path: str, target: str, extra: str) -> list[str]:
        if target.startswith("."):
            dots = len(target) - len(target.lstrip("."))
            base = posixpath.dirname(path)
            for _ in range(dots - 1):
                base = posixpath.dirname(base)
            rel = target.lstrip(".").replace(".", "/")
            mod_path = posixpath.join(base, rel) if rel else base
            cands = [mod_path]
        else:
            cands = [target.replace(".", "/")]
        out = []
        for mod in cands:
            names = [n.strip() for n in extra.split(",") if n.strip()] if extra else []
            for candidate in [f"{mod}/{n}" for n in names] + [mod]:
                for suffix in (".py", "/__init__.py"):
                    full = (candidate + suffix).lstrip("/")
                    fid = self.ix.files.get((project, full))
                    hits = [fid] if fid else [h for h in self.ix.by_suffix.get(full, [])
                                              if h.startswith(project + ":")][:1]
                    if hits:
                        out.extend(hits)
                        break
        return list(dict.fromkeys(out))

    def _import_by_dir(self, project: str, target: str) -> list[str]:
        segs = target.strip("\"").split("/")
        for i in range(len(segs)):
            suffix = "/".join(segs[i:])
            did = virtual_id(project, "dir", suffix + "/")
            if did in self.virtual:
                return [did]
        return []

    @staticmethod
    def _external_key(lang: str, target: str) -> str:
        target = target.strip().strip("\"'")
        if not target or target.startswith("."):
            return ""
        if lang in ("java", "kotlin", "scala", "c_sharp"):
            return "jvm:" + ".".join(target.split(".")[:2]) if lang != "c_sharp" else "nuget:" + target.split(".")[0]
        if lang in ("typescript", "tsx", "javascript"):
            parts = target.split("/")
            return "npm:" + ("/".join(parts[:2]) if target.startswith("@") else parts[0])
        if lang == "python":
            return "py:" + target.split(".")[0]
        if lang == "go":
            return "go:" + "/".join(target.split("/")[:3])
        return ""

    # -- symbol references -----------------------------------------------------------------
    def _symbol_ref(self, project: str, src: str, kind: str, target: str, extra: str) -> None:
        name = target.split("<", 1)[0].strip()
        if name not in self.ix.by_name:
            return
        if kind == "call" and extra and self._typed_call(src, name, extra):
            return
        types = kind in ("type", "inherit")
        bucket = self._bucket(name, types, self._family(src))
        chosen = self._scope(project, src, bucket, name, strict=(kind == "call"))
        if not chosen:
            return
        edge_kind = {"inherit": "inherits", "type": "references"}.get(kind, "calls")
        if kind == "call" and all(self.ix.nodes[c].kind == "class" for c in chosen):
            edge_kind = "references"
        w = 1.0 / len(chosen)
        for c in chosen:
            self._edge(src, c, edge_kind, w)

    def _bucket(self, name: str, types: bool, family: str) -> _Bucket:
        """Same-named candidates of one kind class and language family, grouped once (2M references
        resolve against the same few thousand popular names such as `get` or `toString`)."""
        key = (name, types, family)
        bucket = self.buckets.get(key)
        if bucket is None:
            allowed = TYPE_KINDS if types else CALLABLE_KINDS
            nodes = self.ix.nodes
            cands = [c for c in self.ix.by_name[name] if nodes[c].kind in allowed and self._family(c) == family]
            bucket = self.buckets[key] = _Bucket(cands, self.ix.file_of)
        return bucket

    def _scope(self, project: str, src: str, bucket: _Bucket, name: str, strict: bool) -> list[str]:
        """Nearest candidates: same file, then imported files, then same directory, then same project."""
        if not bucket.cands or bucket.cands == [src]:
            return []
        src_file = src.split("#", 1)[0]
        same_file = bucket.in_file(src_file, src)
        if same_file:
            return same_file[:3]
        via_import = bucket.in_files(self.imported.get(src_file, ()), src)
        if via_import:
            return via_import[:3]
        same_dir = bucket.in_dir(posixpath.dirname(src_file), src)
        if same_dir and (not strict or len(same_dir) == 1):
            return same_dir[:3]
        pool = bucket.in_project(project, src) or (
            bucket.cands if len(bucket.cands) > 4 else [c for c in bucket.cands if c != src])
        if strict:
            return pool if len(pool) == 1 and len(name) >= 6 else []
        return pool if len(pool) <= 3 else []

    def _overrides(self) -> None:
        """A method overrides the same-named method of each supertype it inherits from."""
        supers = [(s, d) for (s, d, k) in self.edges if k == "inherits"]
        for cls, sup in supers:
            mine = {self.ix.nodes[m].name: m for m in self._members(cls)}
            for m in self._members(sup):
                own = mine.get(self.ix.nodes[m].name)
                if own:
                    self._edge(own, m, "inherits")
                    self.edges.pop((own, m, "calls"), None)

    # -- typed calls (receiver hints from the extractor, see extractors/jtypes.py) -------------
    def _index_types(self, returns, supertypes=()) -> None:
        self.supers: dict[str, list[str]] = defaultdict(list)
        for (s, d, k) in self.edges:
            if k == "inherits" and self._is_type(s) and self._is_type(d):
                self.supers[s].append(d)
        self.returns = {src: target for _p, src, _k, target, _e in returns}
        self.generic_supers: dict[str, list[tuple[str, str]]] = defaultdict(list)
        for _p, src, _k, target, _e in supertypes:
            base, _, element = target.partition("<")
            if element:
                self.generic_supers[src].append((base, element.rstrip(">")))
        self.member_index: dict[str, dict[str, list[str]]] = {}

    def _is_type(self, node_id: str) -> bool:
        node = self.ix.nodes.get(node_id)
        return node is not None and node.kind in TYPE_KINDS

    def _typed_call(self, src: str, name: str, hint: str) -> bool:
        """Resolve a call through its receiver's static type. Returns False when the receiver is unknown
        (the caller falls back to name-based resolution). A known receiver whose type hierarchy in this
        repository has no such method is a library call (`List.get`, `JpaRepository.save`): no edge."""
        terms = self._resolve_hint(hint, src)
        if terms is None:
            return False
        owners = [t[1] for t in terms if t[0] == "C"]
        chosen = self._find_method(owners, name) if owners else []
        if not chosen and hint.startswith("C:") and "#" not in hint:
            chosen = self._enclosing_or_static(src, hint[2:], name)
        chosen = [c for c in chosen if c != src][:3]
        for c in chosen:
            self._edge(src, c, "calls", 1.0 / len(chosen))
        return True

    def _resolve_hint(self, hint: str, src: str) -> list[tuple] | None:
        """Type terms the receiver can be: ("C", class id) for a repository type, ("X", name, element) for
        a library type (with its first type argument); None when the hint names no class of this file.
        A chain step that cannot be followed (a library method, a generic return) yields []: no edge.
        Measured against javac on one Java module: name-based guessing there instead raised recall
        0.954 -> 0.964 but cut precision 0.989 -> 0.953 (bench/results/callers-typed-v3-*.json)."""
        src_file = src.split("#", 1)[0]
        head, *steps = hint.split("#")
        kind, _, name = head.partition(":")
        if kind in ("C", "S"):
            cls = f"{src_file}#{name}"
            if cls not in self.ix.nodes:
                return None
            terms = [("C", cls)] if kind == "C" else [("C", s) for s in self.supers.get(cls, ())]
        elif kind == "T":
            terms = self._terms(name, src_file)
        else:
            return None
        for step in steps:
            following: list[tuple] = []
            for term in terms:
                following.extend(self._step(term, step, src_file))
            terms = list(dict.fromkeys(following))
            if not terms:
                return []
        return terms

    def _terms(self, typ: str, context_file: str) -> list[tuple]:
        base, _, rest = typ.partition("<")
        element = rest.rstrip(">")
        found = self._types_named(base, context_file)
        return [("C", c) for c in found] if found else [("X", base, element)]

    def _step(self, term: tuple, step: str, context_file: str) -> list[tuple]:
        """The type after `.step()` (or `#@`: an element) on one term."""
        if term[0] == "X":
            _, base, element = term
            if not element:
                return []
            if step == "@" or step in ELEMENT_METHODS:
                return self._terms(element, context_file)
            container = SAME_ELEMENT_METHODS.get(step)
            return [("X", container, element)] if container else []
        if step == "@":
            return []
        out: list[tuple] = []
        methods = self._find_method([term[1]], step)
        for method in methods:
            returned = self.returns.get(method)
            if returned:
                out.extend(self._terms(returned, self.ix.file_of.get(method, context_file)))
        if not methods:  # inherited from a library supertype with a known entity (Spring Data)
            for base, entity in self.generic_supers.get(term[1], ()):
                result = REPOSITORY_METHODS.get(step) if base in REPOSITORY_TYPES else None
                if result == "":
                    out.extend(self._terms(entity, self.ix.file_of.get(term[1], context_file)))
                elif result:
                    out.append(("X", result, entity))
        return out

    def _types_named(self, name: str, context_file: str) -> list[str]:
        """Types with this simple name as seen from a file: same file, imported, same package, unique."""
        cands = [c for c in self.ix.by_name.get(name, ()) if self.ix.nodes[c].kind in TYPE_KINDS]
        if len(cands) <= 1:
            return cands
        family = self._family(context_file) if context_file in self.ix.nodes else ""
        cands = [c for c in cands if not family or self._family(c) == family] or cands
        for pick in (
            lambda c: self.ix.file_of.get(c) == context_file,
            lambda c: self.ix.file_of.get(c) in self.imported.get(context_file, ()),
            lambda c: posixpath.dirname(self.ix.file_of.get(c, "")) == posixpath.dirname(context_file),
        ):
            hits = [c for c in cands if pick(c)]
            if hits:
                return hits[:3]
        return cands if len(cands) <= 2 else []

    def _find_method(self, owners: list[str], name: str, depth: int = 8) -> list[str]:
        """Methods named `name` on the nearest level of the owners' type hierarchy that declares one."""
        level, seen = list(owners), set(owners)
        for _ in range(depth):
            found = [m for cls in level for m in self._members_by_name(cls).get(name, ())]
            if found:
                return list(dict.fromkeys(found))
            nxt = [s for cls in level for s in self.supers.get(cls, ()) if s not in seen]
            if not nxt:
                return []
            seen.update(nxt)
            level = nxt
        return []

    def _members_by_name(self, cls: str) -> dict[str, list[str]]:
        table = self.member_index.get(cls)
        if table is None:
            table = defaultdict(list)
            for m in self._members(cls):
                table[self.ix.nodes[m].name].append(m)
            self.member_index[cls] = table
        return table

    def _enclosing_or_static(self, src: str, qual: str, name: str) -> list[str]:
        """An unqualified call not declared in its class: an enclosing class, else a static import."""
        src_file = src.split("#", 1)[0]
        while "." in qual:
            qual = qual.rsplit(".", 1)[0]
            found = self._find_method([f"{src_file}#{qual}"], name)
            if found:
                return found
        imported = [c for f in self.imported.get(src_file, ()) for c in self._symbols_in(f)
                    if self.ix.nodes[c].kind in TYPE_KINDS]
        return self._find_method(imported, name, depth=1)

    def _members(self, cls: str) -> list[str]:
        node = self.ix.nodes.get(cls)
        if node is None:
            return []
        prefix = node.qualname + "."
        return [m for m in self._symbols_in(self.ix.file_of.get(cls))
                if self.ix.nodes[m].kind == "method" and self.ix.nodes[m].qualname.startswith(prefix)
                and "." not in self.ix.nodes[m].qualname[len(prefix):]]

    def _family(self, node_id: str) -> str:
        fam = self.family.get(node_id)
        if fam is None:
            lang = self.ix.nodes[node_id].lang if node_id in self.ix.nodes else ""
            fam = self.family[node_id] = LANG_FAMILY.get(lang, lang)
        return fam

    # -- tests -----------------------------------------------------------------------------
    def _tests(self) -> None:
        for (project, path), fid in self.ix.files.items():
            if not is_test_path(path) or self.ix.lang.get((project, path)) in ("md", "markdown", "mdx"):
                continue
            stem = test_stem(path).lower()
            if len(stem) < 3:
                continue
            cands = [c for c in self.ix.by_stem.get(stem, [])
                     if c.startswith(project + ":") and not is_test_path(self._split(c)[1])]
            if not cands:
                cands = [c for s in self._stems_with_prefix(stem) for c in self.ix.by_stem[s]
                         if c.startswith(project + ":") and not is_test_path(self._split(c)[1])]
            if not cands:
                continue
            mirror = path.replace("/test/", "/main/").replace("/tests/", "/")
            cands.sort(key=lambda c: _common_prefix(self._split(c)[1], mirror), reverse=True)
            for c in cands[:3]:
                self._edge(fid, c, "tests", 1.0 if len(cands) == 1 else 0.6)

    # -- routes ----------------------------------------------------------------------------
    def _routes(self, defs, calls) -> None:
        table: dict[int, list[tuple[list[str], str, str]]] = defaultdict(list)
        for project, src, _k, path, verb in defs:
            verb = (verb or "").upper()
            key = f"{verb or 'ANY'} {path}"
            rid = self._vnode(project, "route", key, key, text=f"HTTP endpoint {key}")
            self.virtual[rid].path = self.ix.nodes[src].path if src in self.ix.nodes else ""
            self._edge(rid, src, "handles")
            segs = path.strip("/").split("/")
            table[len(segs)].append((segs, verb, rid))
            for prefix in self.cfg.route_prefixes:
                if path.startswith(prefix + "/"):
                    short = path[len(prefix):].strip("/").split("/")
                    table[len(short)].append((short, verb, rid))
        doc_langs = {"md", "mdx", "markdown", "txt", "rst", "adoc", "pdf", "html"}
        for _project, src, _k, path, verb in calls:
            segs = path.strip("/").split("/")
            verb = (verb or "").upper()
            node = self.ix.nodes.get(src)
            kind = "mentions" if node is not None and node.lang in doc_langs else "http_call"
            for rsegs, rverb, rid in table.get(len(segs), []):
                if verb and rverb and verb != rverb:
                    continue
                if all(a == b or a == "{}" or b == "{}" for a, b in zip(segs, rsegs, strict=False)):
                    self._edge(src, rid, kind)

    # -- tables ----------------------------------------------------------------------------
    def _tables(self, refs) -> None:
        for project, src, _k, name, extra in refs:
            mode, _, columns = (extra or "uses").partition("\t")
            tid = self._vnode(project, "table", name, name, text=f"table {name} {columns}".strip())
            self._edge(src, tid, "defines_table" if mode == "defines" else "uses_table")

    # -- tags ------------------------------------------------------------------------------
    def _tags(self, refs) -> None:
        for project, src, _k, key, _extra in refs:
            self._edge(src, self._tag(project, key), "tagged")
        regexes = [(name, re.compile(rx)) for name, rx in self.cfg.tags.items()]
        for (project, path), fid in self.ix.files.items():
            base = path.rsplit("/", 1)[-1]
            for name, rx in regexes:
                m = rx.search(base)
                if m and name != "issue":
                    self._edge(fid, self._tag(project, f"{name}:{m.group(1) if m.groups() else m.group(0)}"),
                               "tagged", 3.0)

    def _tag(self, project: str, key: str) -> str:
        kind, _, value = key.partition(":")
        value = str(int(value)) if value.isdigit() else value
        label = f"#{value}" if kind == "issue" else f"{kind.upper()}-{value}"
        return self._vnode(project, "tag", f"{kind}:{value}", label)

    # -- links and mentions ----------------------------------------------------------------
    def _link(self, project: str, src: str, _k: str, target: str, _extra: str) -> None:
        target = posixpath.normpath(target) if target else target
        fid = self.ix.files.get((project, target))
        if fid is None:
            did = virtual_id(project, "dir", target.rstrip("/") + "/")
            fid = did if did in self.virtual else None
        if fid:
            self._edge(src, fid, "links")

    def _mention(self, project: str, src: str, _k: str, token: str, _extra: str) -> None:
        """A file or directory the text names, else a type or function it names."""
        if self._mention_path(project, src, token) or "/" in token:
            return
        hits = self.ix.by_qual.get(token) or [
            s for s in self.ix.by_name.get(token, []) if self.ix.nodes[s].kind in TYPE_KINDS | {"function"}]
        hits = [h for h in hits if h.startswith(project + ":")] or hits
        if 0 < len(hits) <= 3:
            for h in hits:
                self._edge(src, h, "mentions", 1.0 / len(hits))

    def _mention_path(self, project: str, src: str, token: str) -> bool:
        """Link a path the text names; True when the token named a file or directory (linked or ambiguous)."""
        here = self._split(src)[1]
        key = (project, here, token)
        resolved = self.path_mentions.get(key)
        if resolved is None:
            resolved = self.path_mentions[key] = self._resolve_path(project, here, token)
        targets, named = resolved
        for h in targets:
            self._edge(src, h, "mentions", 1.0 / len(targets))
        return named

    def _resolve_path(self, project: str, here: str, token: str) -> tuple[list[str], bool]:
        """The targets a path names, resolved once per file (a document's sections repeat its tokens):
        relative to the mentioning file (`./`, `../`, or a sibling), from the repository root (`/icons/x.png`
        is `public/icons/x.png` by suffix), with the mentioning file's extension when the name has none
        (`require "../lib/x"`), else by suffix. Several candidates: the ones sharing the longest path prefix
        with the mentioning file, and none when more than three remain."""
        name = token.rstrip("/")
        last = name.rsplit("/", 1)[-1]
        ext = posixpath.splitext(here)[1]
        if last not in self.ix.names and last + ext not in self.ix.names:
            return [], False  # names no file or directory of any store: most candidates end here
        base = posixpath.dirname(here)
        relative = name.startswith(("./", "../"))
        exact = [posixpath.normpath(posixpath.join(base, name))] if relative else \
            [posixpath.normpath(name.lstrip("/")), posixpath.normpath(posixpath.join(base, name.lstrip("/")))]
        if ext and "." not in last:
            exact += [e + ext for e in exact]
        for target in exact:
            hit = self.ix.files.get((project, target)) or self.ix.dirs.get((project, target))
            if hit and target != here:
                return [hit], True
        if relative or ("/" not in name and "." not in name):
            return [], False
        key = posixpath.normpath(name.lstrip("/"))
        hits = [h for h in (*self.ix.by_suffix.get(key, ()), *self.ix.dir_by_suffix.get(key, ()))
                if h.startswith(project + ":") and self._target_path(h) != here]
        if not hits:
            return [], False
        nearest = _nearest(here, hits, self._target_path)
        return (nearest if len(nearest) <= 3 else []), True  # more than three equally near: named, not linked

    def _target_path(self, node_id: str) -> str:
        node = self.ix.nodes.get(node_id) or self.virtual.get(node_id)
        return node.path if node is not None else ""

    # -- git co-change ---------------------------------------------------------------------
    def _git_log(self, project: str, root: Path) -> str:
        """`git log --name-only` output, cached in the store by HEAD (history only changes with HEAD)."""
        head = _git_head(root)
        key = f"{head}:{self.cfg.git_commits}"
        cached = self.git_cache.get(project)
        if head and cached and cached[0] == key:
            return cached[1]
        try:
            out = subprocess.run(
                ["git", "-C", str(root), "log", "--relative", "--no-merges", "--name-only",
                 f"-n{self.cfg.git_commits}", "--format=\x1e%s"],
                capture_output=True, check=True, timeout=180,
            ).stdout.decode("utf-8", "replace")
        except (OSError, subprocess.SubprocessError):
            return ""
        if head:
            self.git_cache[project] = (key, out)
        return out

    def _git(self, project: str, root: Path) -> None:
        if self.cfg.git_commits <= 0 or not root.exists():
            return
        out = self._git_log(project, root)
        if not out:
            return
        pair_counts: Counter = Counter()
        file_counts: Counter = Counter()
        tag_rx = re.compile(self.cfg.tags["issue"]) if "issue" in self.cfg.tags else None
        for block in out.split("\x1e")[1:]:
            lines = [ln for ln in block.split("\n") if ln.strip()]
            if not lines:
                continue
            subject, paths = lines[0], lines[1:]
            fids = sorted({self.ix.files[(project, p)] for p in paths if (project, p) in self.ix.files})
            if not fids or len(fids) > self.cfg.cochange_max_files:
                continue
            file_counts.update(fids)
            for i in range(len(fids)):
                for j in range(i + 1, len(fids)):
                    pair_counts[(fids[i], fids[j])] += 1
            if tag_rx:
                for m in tag_rx.finditer(subject):
                    tag = self._tag(project, f"issue:{m.group(1)}")
                    for fid in fids:
                        self._edge(fid, tag, "changed_for", 0.5)
        for (a, b), count in pair_counts.items():
            if count < self.cfg.cochange_min_count:
                continue
            strength = count / math.sqrt(file_counts[a] * file_counts[b])
            self._edge(a, b, "co_change", round(min(1.0, strength) * 2, 4))


class _Bucket:
    """Candidates in their original order; large buckets are indexed by file, directory and project so
    each lookup costs O(result) instead of O(candidates). Results never include the referring node."""

    INDEX_FROM = 9

    def __init__(self, cands: list[str], file_of: dict[str, str]):
        self.cands = cands
        self.file_of = file_of
        self.pos: dict[str, int] = {}
        self.by_file: dict[str, list[str]] = {}
        self.by_dir: dict[str, list[str]] = {}
        self.by_project: dict[str, list[str]] = {}
        if len(cands) >= self.INDEX_FROM:
            for i, c in enumerate(cands):
                f = file_of.get(c, "")
                self.pos[c] = i
                self.by_file.setdefault(f, []).append(c)
                self.by_dir.setdefault(posixpath.dirname(f), []).append(c)
                self.by_project.setdefault(c.split(":", 1)[0], []).append(c)

    @property
    def indexed(self) -> bool:
        return bool(self.pos)

    def in_file(self, file_id: str, src: str) -> list[str]:
        if self.indexed:
            return [c for c in self.by_file.get(file_id, ()) if c != src]
        return [c for c in self.cands if c != src and self.file_of.get(c) == file_id]

    def in_files(self, files, src: str) -> list[str]:
        if not files:
            return []
        if self.indexed and len(files) < len(self.cands):
            hits = [c for f in files for c in self.by_file.get(f, ()) if c != src]
            return sorted(hits, key=self.pos.__getitem__)
        return [c for c in self.cands if c != src and self.file_of.get(c) in files]

    def in_dir(self, dir_id: str, src: str) -> list[str]:
        if self.indexed:
            return [c for c in self.by_dir.get(dir_id, ()) if c != src]
        return [c for c in self.cands if c != src and posixpath.dirname(self.file_of.get(c, "")) == dir_id]

    def in_project(self, project: str, src: str) -> list[str]:
        if self.indexed:
            members = self.by_project.get(project, ())
            if len(members) > 4:  # still more than three without src: only the size matters
                return members
            return [c for c in members if c != src]
        prefix = project + ":"
        return [c for c in self.cands if c != src and c.startswith(prefix)]


def _git_head(root: Path) -> str:
    try:
        return subprocess.run(["git", "-C", str(root), "rev-parse", "HEAD"], capture_output=True, check=True,
                              timeout=30).stdout.decode().strip()
    except (OSError, subprocess.SubprocessError):
        return ""


def _nearest(here: str, ids: list[str], path_of) -> list[str]:
    """The ids whose path shares the most leading directories with `here`."""
    mine = here.split("/")[:-1]

    def shared(node_id: str) -> int:
        n = 0
        for a, b in zip(mine, path_of(node_id).split("/"), strict=False):
            if a != b:
                break
            n += 1
        return n

    scores = {h: shared(h) for h in dict.fromkeys(ids)}
    best = max(scores.values())
    return [h for h, v in scores.items() if v == best]


def _common_prefix(a: str, b: str) -> int:
    n = 0
    for x, y in zip(a, b, strict=False):
        if x != y:
            break
        n += 1
    return n


def link(store, cfg: Config) -> tuple[int, int]:
    linker = Linker(cfg, store.project_roots())
    blob = store.get_meta("git_cache")
    if blob:
        linker.git_cache = {p: tuple(v) for p, v in json.loads(zlib.decompress(blob)).items()}
    nodes, edges = linker.run(store.nodes(extracted_only=True, with_text=False), store.refs, store.file_meta())
    store.replace_derived(nodes, edges)
    store.set_meta("git_cache", zlib.compress(json.dumps(linker.git_cache).encode("utf-8")))
    return len(nodes), len(linker.edges)
