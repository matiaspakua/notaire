"""Source code extractor: one generic tree-sitter walker driven by per-language specs.

Emits symbols (classes, interfaces, functions, methods, top-level constants), containment edges and
unresolved refs: imports, calls, type references, inheritance, HTTP routes served (Spring, JAX-RS,
FastAPI/Flask, NestJS, Express), HTTP calls made (fetch/axios/api helpers, MockMvc) and DB tables.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field

from ..model import Edge, Extraction, Node, Ref, file_id, symbol_id
from .common import Options, add_tags, new_file, normalize_route
from .jtypes import CLASS_KINDS, LOCAL_DECLARATIONS, JavaTypes, generic_type, return_type

IDENTS = frozenset({
    "identifier", "type_identifier", "property_identifier", "field_identifier", "simple_identifier",
    "constant", "name", "shorthand_property_identifier", "private_property_identifier",
})


@dataclass(frozen=True)
class Spec:
    defs: dict[str, str] = field(default_factory=dict)
    imports: frozenset = frozenset()
    calls: dict[str, str] = field(default_factory=dict)
    type_refs: frozenset = frozenset({"type_identifier"})
    comments: frozenset = frozenset({"comment"})
    package: frozenset = frozenset()
    strings: frozenset = frozenset({"string", "string_literal", "template_string"})
    decorators: frozenset = frozenset({"decorator", "annotation", "marker_annotation"})
    heritage: frozenset = frozenset()
    var_decl: frozenset = frozenset()
    scope_only: dict[str, str] = field(default_factory=dict)
    generic: bool = False


_TS = Spec(
    defs={
        "class_declaration": "class", "abstract_class_declaration": "class", "interface_declaration": "interface",
        "type_alias_declaration": "type", "enum_declaration": "enum", "function_declaration": "function",
        "generator_function_declaration": "function", "method_definition": "method",
        "method_signature": "method", "abstract_method_signature": "method",
    },
    imports=frozenset({"import_statement", "export_statement"}),
    calls={"call_expression": "function", "new_expression": "constructor",
           "jsx_opening_element": "name", "jsx_self_closing_element": "name"},
    heritage=frozenset({"class_heritage", "extends_type_clause"}),
    var_decl=frozenset({"variable_declarator"}),
)

SPECS: dict[str, Spec] = {
    "java": Spec(
        defs={
            "class_declaration": "class", "interface_declaration": "interface", "enum_declaration": "enum",
            "record_declaration": "class", "annotation_type_declaration": "interface",
            "method_declaration": "method", "constructor_declaration": "method",
        },
        imports=frozenset({"import_declaration"}),
        calls={"method_invocation": "name", "object_creation_expression": "type"},
        comments=frozenset({"line_comment", "block_comment"}),
        package=frozenset({"package_declaration"}),
        heritage=frozenset({"superclass", "super_interfaces", "extends_interfaces"}),
    ),
    "kotlin": Spec(
        defs={"class_declaration": "class", "object_declaration": "class", "function_declaration": "function"},
        imports=frozenset({"import_header"}),
        calls={"call_expression": "function"},
        comments=frozenset({"line_comment", "multiline_comment"}),
        package=frozenset({"package_header"}),
        heritage=frozenset({"delegation_specifier", "delegation_specifiers"}),
    ),
    "c_sharp": Spec(
        defs={
            "class_declaration": "class", "interface_declaration": "interface", "struct_declaration": "class",
            "enum_declaration": "enum", "record_declaration": "class", "method_declaration": "method",
            "constructor_declaration": "method",
        },
        imports=frozenset({"using_directive"}),
        calls={"invocation_expression": "function", "object_creation_expression": "type"},
        type_refs=frozenset(),
        heritage=frozenset({"base_list"}),
        decorators=frozenset({"attribute"}),
    ),
    "typescript": _TS,
    "tsx": _TS,
    "javascript": _TS,
    "python": Spec(
        defs={"class_definition": "class", "function_definition": "function"},
        imports=frozenset({"import_statement", "import_from_statement"}),
        calls={"call": "function"},
        type_refs=frozenset(),
        heritage=frozenset({"argument_list"}),
    ),
    "go": Spec(
        defs={"function_declaration": "function", "method_declaration": "method", "type_spec": "type"},
        imports=frozenset({"import_spec"}),
        calls={"call_expression": "function"},
        package=frozenset({"package_clause"}),
        strings=frozenset({"interpreted_string_literal", "raw_string_literal"}),
    ),
    "rust": Spec(
        defs={
            "function_item": "function", "struct_item": "class", "enum_item": "enum", "trait_item": "interface",
            "mod_item": "type", "function_signature_item": "function",
        },
        imports=frozenset({"use_declaration"}),
        calls={"call_expression": "function", "macro_invocation": "macro"},
        comments=frozenset({"line_comment", "block_comment"}),
        strings=frozenset({"string_literal"}),
        scope_only={"impl_item": "type"},
    ),
}
_GENERIC = Spec(generic=True, type_refs=frozenset({"type_identifier"}))
_GENERIC_DEF = re.compile(r"(class|struct|interface|trait|enum|module|function|method)_(declaration|definition|item)$")
_GENERIC_KIND = {"struct": "class", "trait": "interface", "module": "type"}

_ROUTE_VERBS = {
    "GetMapping": "GET", "PostMapping": "POST", "PutMapping": "PUT", "DeleteMapping": "DELETE",
    "PatchMapping": "PATCH", "RequestMapping": "", "Get": "GET", "Post": "POST", "Put": "PUT",
    "Delete": "DELETE", "Patch": "PATCH", "All": "",
}
_CLASS_ROUTE = {"RequestMapping", "Controller", "Path", "Route"}
_JAXRS_VERBS = {"GET", "POST", "PUT", "DELETE", "PATCH"}
_PY_ROUTE = {"get": "GET", "post": "POST", "put": "PUT", "delete": "DELETE", "patch": "PATCH", "route": "",
             "api_route": ""}
_SERVER_OBJECTS = {"app", "router", "server", "fastify", "api_router"}
_HTTP_CALLEE = re.compile(
    r"^(fetch|request|get|post|put|patch|delete|api\w*|http\w*|\w*(get|post|put|patch|delete)\w*)$", re.I
)
_NOT_HTTP = {
    "push", "replace", "href", "redirect", "navigate", "prefetch", "join", "resolve", "require", "startswith",
    "endswith", "includes", "split", "match", "test", "describe", "it", "goto", "readfile", "readfilesync",
    "existssync", "getbyrole", "getbytext", "getbylabel", "waitforurl", "tohaveurl", "searchparams", "get_item",
    "getitem", "getproperty", "getenv", "getattribute", "getresource", "getparameter", "getheader",
}
_STRING_RX = re.compile(r'"((?:[^"\\\n]|\\.)*)"|\'((?:[^\'\\\n]|\\.)*)\'|`([^`]*)`')
_KEYED_PATH = re.compile(r"\b(?:value|path)\s*=\s*(\{[^}]*\}|\"[^\"]*\"|'[^']*')")
_TABLE_ANNO = re.compile(r"\bname\s*=\s*\"([^\"]+)\"")
_PY_TABLE = re.compile(r"__tablename__\s*=\s*[\"']([\w.]+)[\"']")
_LICENSE = re.compile(r"copyright|licen[sc]ed\s+under|spdx-license|all rights reserved|apache license|"
                      r"mit license|gnu (?:general|lesser)", re.I)
_LEADING_COMMENT = re.compile(r"\s*(/\*\*?[\s\S]*?\*/|(?:[ \t]*(?://|#)[^\n]*\n){1,12}|\"\"\"[\s\S]*?\"\"\")")
_DOC_WRAPPERS = frozenset({"export_statement", "decorated_definition", "ambient_declaration"})
_RULE = re.compile(r"[-=*_#/]{4,}")
_COMMENT_MARKS = re.compile(r"^\s*(/\*\*?|\*/|\*|//+|#+|\"\"\"|''')\s?", re.M)


def _parser(lang: str):
    from tree_sitter_language_pack import get_parser

    cache = _parser.__dict__.setdefault("cache", {})
    if lang not in cache:
        cache[lang] = get_parser(lang)
    return cache[lang]


# Bodies of anonymous functions and blocks at module level (test-suite callbacks, IIFEs): their variables
# are locals, not module symbols. Named definitions inside them are still extracted.
_LOCAL_BODIES = frozenset({"arrow_function", "function_expression", "function", "statement_block"})


@dataclass
class _Scope:
    sid: str
    qual: str
    kind: str
    route_prefix: str = ""


class _Walker:
    def __init__(self, opts: Options, path: str, lang: str, src: bytes):
        self.opts, self.path, self.lang, self.src = opts, path, lang, src
        self.spec = SPECS.get(lang, _GENERIC)
        self.fid = file_id(opts.project, path)
        self.ex: Extraction | None = None
        self.seen_ids: set[str] = set()
        self.seen_refs: set[tuple[str, str, str]] = set()
        self.top_names: list[str] = []
        self.types = JavaTypes(self.text) if lang == "java" else None

    def text(self, node) -> str:
        return self.src[node.start_byte:node.end_byte].decode("utf-8", "replace")

    def ref(self, src: str, kind: str, target: str, extra: str = "") -> None:
        key = (src, kind, target + "|" + extra)
        if target and key not in self.seen_refs:
            self.seen_refs.add(key)
            self.ex.refs.append(Ref(src, kind, target, extra))

    # -- walk ------------------------------------------------------------------------------
    def run(self, ex: Extraction) -> None:
        self.ex = ex
        root = _parser(self.lang).parse(self.src).root_node
        file_scope = _Scope(self.fid, "", "file")
        stack = [(child, file_scope) for child in reversed(root.named_children)]
        while stack:
            node, scope = stack.pop()
            child_scope = self.visit(node, scope)
            if child_scope is not None:
                stack.extend((c, child_scope) for c in reversed(node.named_children))

    def visit(self, node, scope: _Scope) -> _Scope | None:
        t, spec = node.type, self.spec
        kind = spec.defs.get(t)
        if kind is None and spec.generic:
            m = _GENERIC_DEF.search(t)
            kind = _GENERIC_KIND.get(m.group(1), m.group(1)) if m else None
        if kind is not None:
            return self.define(node, scope, kind)
        if self.types is not None:
            if t in LOCAL_DECLARATIONS:
                self.types.declare(node, scope)
            elif t == "lambda_expression":
                self.types.lambda_params(node, scope)
            elif t == "method_reference":
                self.method_reference(node, scope)
                return None
        if t in spec.scope_only:
            target = node.child_by_field_name("type")
            name = self.text(target) if target is not None else ""
            return _Scope(scope.sid, f"{scope.qual}.{name}" if scope.qual else name, "class") if name else scope
        if scope.kind == "file" and spec.var_decl and t in _LOCAL_BODIES:
            return _Scope(scope.sid, scope.qual, "block", scope.route_prefix)
        if t in spec.var_decl and scope.kind == "file":
            return self.variable(node, scope)
        if t == "pair" and spec.var_decl and scope.kind in ("variable", "class"):
            value = node.child_by_field_name("value")
            if value is not None and value.type in ("arrow_function", "function_expression", "function"):
                return self.define(node, scope, "method")
        if t in spec.imports:
            self.import_ref(node, scope)
            return None if t != "export_statement" else scope
        if t in spec.package:
            self.ex.meta["package"] = re.sub(r"^\s*(package|namespace)\s+|;\s*$", "", self.text(node)).strip()
            return None
        if t in spec.calls or (spec.generic and ("call" in t or "invocation" in t)):
            self.call(node, scope, spec.calls.get(t, "function"))
            return scope
        if t in spec.type_refs:
            self.ref(scope.sid, "type", self.text(node))
            return None
        if t in spec.comments:
            return None
        return scope

    def define(self, node, scope: _Scope, kind: str) -> _Scope | None:
        name = self.def_name(node)
        if not name:
            return scope
        if kind == "function" and scope.kind in ("class", "interface", "enum"):
            kind = "method"
        qual = f"{scope.qual}.{name}" if scope.qual else name
        sid = symbol_id(self.opts.project, self.path, qual)
        n = 2
        while sid in self.seen_ids:
            sid = symbol_id(self.opts.project, self.path, f"{qual}~{n}")
            n += 1
        self.seen_ids.add(sid)
        self.ex.nodes.append(Node(
            id=sid, project=self.opts.project, kind=kind, name=name, path=self.path, qualname=qual,
            lang=self.lang, start=node.start_point[0] + 1, end=node.end_point[0] + 1,
            signature=self.signature(node), text=self.doc(node),
        ))
        self.ex.edges.append(Edge(scope.sid, sid, "contains"))
        if scope.kind == "file":
            self.top_names.append(name)
        new_scope = _Scope(sid, qual, kind, scope.route_prefix)
        if self.types is not None:
            if kind in CLASS_KINDS:
                self.types.enter_class(node, sid, qual)
            elif kind == "method":
                self.types.enter_method(node, sid, scope)
                returned = return_type(node, self.text)
                if returned:
                    self.ref(sid, "returns", returned)
        for child in node.children:
            if child.type in self.spec.heritage:
                for sup in self.supertypes(child):
                    self.ref(sid, "inherit", sup)
                if self.types is not None:  # `extends JpaRepository<Folio, Integer>`: the entity type matters
                    for typ in child.named_children:
                        for g in [typ, *typ.named_children]:
                            if g.type == "generic_type":
                                self.ref(sid, "supertype", generic_type(self.text(g)))
        self.annotations(node, new_scope, scope)
        if kind == "class" and self.lang == "python":
            m = _PY_TABLE.search(self.text(node))
            if m:
                self.ref(sid, "table", m.group(1).lower(), "uses")
        return new_scope

    def variable(self, node, scope: _Scope) -> _Scope:
        name_node = node.child_by_field_name("name")
        if name_node is None or name_node.type not in IDENTS:
            return scope
        value = node.child_by_field_name("value")
        is_fn = value is not None and value.type in ("arrow_function", "function_expression", "function")
        kind = "function" if is_fn else "variable"
        name = self.text(name_node)
        sid = symbol_id(self.opts.project, self.path, name)
        if sid in self.seen_ids:
            return scope
        self.seen_ids.add(sid)
        decl = node.parent if node.parent is not None else node
        self.ex.nodes.append(Node(
            id=sid, project=self.opts.project, kind=kind, name=name, path=self.path, qualname=name,
            lang=self.lang, start=decl.start_point[0] + 1, end=decl.end_point[0] + 1,
            signature=self.signature(node), text=self.doc(decl if decl.type != "program" else node),
        ))
        self.ex.edges.append(Edge(scope.sid, sid, "contains"))
        self.top_names.append(name)
        annotation = node.child_by_field_name("type")
        if annotation is not None and value is not None and value.type == "object":
            for sup in self.supertypes(annotation)[:1]:
                self.ref(sid, "inherit", sup)
        return _Scope(sid, name, kind)

    # -- names, signatures, docs -----------------------------------------------------------
    def def_name(self, node) -> str | None:
        n = node.child_by_field_name("name")
        if n is None:
            d = node.child_by_field_name("declarator")
            while d is not None and d.type not in IDENTS:
                d = d.child_by_field_name("declarator") or next(
                    (c for c in d.named_children if c.type in IDENTS), None)
            n = d
        if n is None:
            n = next((c for c in node.named_children if c.type in IDENTS), None)
        return self.text(n) if n is not None else None

    def signature(self, node) -> str:
        body = node.child_by_field_name("body")
        end = body.start_byte if body is not None else node.end_byte
        start = node.start_byte
        for mods in (c for c in node.children if c.type == "modifiers"):
            annos = [c for c in mods.children if c.type in self.spec.decorators]
            if annos:
                start = max(start, annos[-1].end_byte)
        raw = self.src[start:end].decode("utf-8", "replace")
        if body is None:
            raw = raw.split("\n", 1)[0]
        return " ".join(raw.split())[:240]

    def doc(self, node) -> str:
        parts = []
        anchor = node
        while anchor.prev_named_sibling is None and anchor.parent is not None \
                and anchor.parent.type in _DOC_WRAPPERS:  # `export function f` keeps its JSDoc on the export
            anchor = anchor.parent
        prev = anchor.prev_named_sibling
        while prev is not None and prev.type in self.spec.comments and len(parts) < 4:
            comment = self.text(prev)
            if _LICENSE.search(comment):
                break
            parts.append(comment)
            prev = prev.prev_named_sibling
        if not parts and self.lang == "python":
            body = node.child_by_field_name("body")
            first = body.named_children[0] if body is not None and body.named_children else None
            if first is not None and first.type == "expression_statement" and first.named_children:
                first = first.named_children[0]
            if first is not None and first.type == "string":
                parts.append(self.text(first))
        return " ".join(_clean_comment(p) for p in reversed(parts))[:500]

    def supertypes(self, node) -> list[str]:
        out, stack = [], [node]
        while stack:
            cur = stack.pop()
            if cur.type in ("type_arguments", "type_parameters", "keyword_argument"):
                continue
            if cur.type in IDENTS:
                out.append(self.text(cur))
                continue
            if cur.type in ("attribute", "member_expression", "scoped_type_identifier", "qualified_name"):
                out.append(self.text(cur).rsplit(".", 1)[-1])
                continue
            stack.extend(cur.named_children)
        return out

    # -- refs ------------------------------------------------------------------------------
    def import_ref(self, node, scope: _Scope) -> None:
        t = node.type
        if t in ("import_statement", "export_statement") and self.lang in ("typescript", "tsx", "javascript"):
            source = node.child_by_field_name("source")
            if source is not None:
                self.ref(self.fid, "import", _unquote(self.text(source)))
            return
        if t == "import_from_statement":
            module = node.child_by_field_name("module_name")
            mod = self.text(module) if module is not None else ""
            names = [self.text(c) for c in node.children_by_field_name("name")]
            self.ref(self.fid, "import", mod, ",".join(n.split(" as ")[0] for n in names))
            return
        if t == "import_spec":
            path = node.child_by_field_name("path")
            self.ref(self.fid, "import", _unquote(self.text(path if path is not None else node)))
            return
        raw = self.text(node)
        if t == "import_statement":
            for part in re.sub(r"^\s*import\s+", "", raw).split(","):
                self.ref(self.fid, "import", part.split(" as ")[0].strip())
            return
        raw = re.sub(r"^\s*(import|using|use)\s+(static\s+)?|;\s*$", "", raw).strip()
        self.ref(self.fid, "import", raw, "static" if " static " in f" {self.text(node)} " else "")

    def call(self, node, scope: _Scope, field_name: str) -> None:
        fn = node.child_by_field_name(field_name)
        if fn is None:
            fn = next((c for c in node.named_children), None)
        if fn is None:
            return
        callee, receiver = self.callee(fn)
        if not callee:
            return
        first = self.first_string_arg(node)
        if callee in ("require", "import") and first is not None:
            self.ref(self.fid, "import", first)
            return
        hint = self.types.receiver(node, scope) if self.types is not None and node.type == "method_invocation" else ""
        self.ref(scope.sid, "call", callee, hint)
        if first is None or not first.startswith(("/", "http")):
            return
        low = callee.lower()
        verb = next((v for v in ("delete", "patch", "post", "put", "get") if v in low), "")
        if receiver in _SERVER_OBJECTS and low in (*_PY_ROUTE, "all", "use") and self.arg_count(node) >= 2:
            self.ref(scope.sid, "route", normalize_route(first), verb.upper())
            return
        url = first.split("://", 1)[-1]
        url = url[url.find("/"):] if "://" in first else url
        prefixed = any(url.startswith(p) for p in self.opts.route_prefixes)
        if low in _NOT_HTTP or "." in url.rsplit("/", 1)[-1]:
            return
        if prefixed or _HTTP_CALLEE.match(callee):
            self.ref(scope.sid, "http_call", normalize_route(url), verb.upper())

    def method_reference(self, node, scope: _Scope) -> None:
        """`Folio::getDto`, `catalog::findById` are calls of that method (not of `::new`)."""
        parts = list(node.named_children)
        if len(parts) < 2 or parts[-1].type != "identifier":
            return
        target = parts[0]
        if target.type in ("type_identifier", "scoped_type_identifier", "generic_type"):
            hint = "T:" + self.text(target).split("<", 1)[0].rsplit(".", 1)[-1]
        else:
            hint = self.types.type_of(target, scope)
        self.ref(scope.sid, "call", self.text(parts[-1]), hint)

    def callee(self, fn) -> tuple[str, str]:
        if fn.type in IDENTS:
            return self.text(fn), ""
        for fld in ("property", "field", "attribute", "name"):
            sub = fn.child_by_field_name(fld)
            if sub is not None:
                obj = fn.child_by_field_name("object") or fn.child_by_field_name("operand")
                return self.text(sub), (self.text(obj).rsplit(".", 1)[-1] if obj is not None else "")
        if fn.type in ("generic_type", "scoped_type_identifier", "scoped_identifier", "qualified_name"):
            return self.text(fn).split("<", 1)[0].rsplit(".", 1)[-1], ""
        idents = [c for c in fn.named_children if c.type in IDENTS]
        return (self.text(idents[-1]), "") if idents else ("", "")

    def first_string_arg(self, node) -> str | None:
        args = node.child_by_field_name("arguments")
        if args is None:
            return None
        first = next((c for c in args.named_children if c.type not in self.spec.comments), None)
        if first is None or first.type not in self.spec.strings and first.type not in (
                "interpreted_string_literal", "raw_string_literal", "string_literal"):
            return None
        return _unquote(self.text(first))

    def arg_count(self, node) -> int:
        args = node.child_by_field_name("arguments")
        return len(args.named_children) if args is not None else 0

    def annotations(self, node, scope: _Scope, parent: _Scope) -> None:
        annos = []
        holders = [node]
        holders.extend(c for c in node.children if c.type == "modifiers")
        if node.parent is not None and node.parent.type == "decorated_definition":
            holders.append(node.parent)
        for holder in holders:
            annos.extend(c for c in holder.children if c.type in self.spec.decorators)
        prev = node.prev_named_sibling
        while prev is not None and prev.type == "decorator":
            annos.append(prev)
            prev = prev.prev_named_sibling
        if not annos:
            return
        parsed = [self.parse_annotation(a) for a in annos]
        markers = {name for name, _ in parsed}
        for name, args in parsed:
            if name == "Table":
                m = _TABLE_ANNO.search(args)
                if m:
                    self.ref(scope.sid, "table", m.group(1).lower(), "uses")
            if scope.kind in ("class", "interface") and name in _CLASS_ROUTE:
                paths = _annotation_paths(args)
                if paths:
                    scope.route_prefix = paths[0]
                continue
            verb = None
            if name in _ROUTE_VERBS:
                verb = _ROUTE_VERBS[name]
                if name == "RequestMapping":
                    m = re.search(r"RequestMethod\.(\w+)", args)
                    verb = m.group(1) if m else ""
            elif name.rsplit(".", 1)[-1] in _PY_ROUTE and "." in name:
                verb = _PY_ROUTE[name.rsplit(".", 1)[-1]]
            elif name == "Path" and markers & _JAXRS_VERBS:
                verb = next(iter(markers & _JAXRS_VERBS))
            if verb is None:
                continue
            for path in _annotation_paths(args) or [""]:
                full = normalize_route("/" + "/".join(p.strip("/") for p in (parent.route_prefix, path) if p))
                self.ref(scope.sid, "route", full, verb)
        if scope.kind == "method" and markers & _JAXRS_VERBS and "Path" not in markers and parent.route_prefix:
            self.ref(scope.sid, "route", normalize_route(parent.route_prefix), next(iter(markers & _JAXRS_VERBS)))

    def parse_annotation(self, anno) -> tuple[str, str]:
        name_node = anno.child_by_field_name("name")
        args_node = anno.child_by_field_name("arguments")
        if name_node is None:
            inner = anno.named_children[0] if anno.named_children else None
            if inner is not None and inner.type in ("call", "call_expression"):
                fn = inner.child_by_field_name("function")
                args_node = inner.child_by_field_name("arguments")
                return (self.text(fn) if fn is not None else ""), (self.text(args_node) if args_node else "")
            return (self.text(inner) if inner is not None else ""), ""
        name = self.text(name_node).rsplit(".", 1)[-1]
        return name, self.text(args_node) if args_node is not None else ""


def _unquote(text: str) -> str:
    text = re.sub(r"^[rbuf]+(?=[\"'])", "", text.strip(), flags=re.I)
    if len(text) >= 2 and text[0] in "\"'`" and text[-1] == text[0]:
        return text[1:-1]
    return text


def _annotation_paths(args: str) -> list[str]:
    if not args:
        return []
    keyed = _KEYED_PATH.search(args)
    scope = keyed.group(1) if keyed else re.split(r"\b\w+\s*=", args, maxsplit=1)[0]
    out = []
    for m in _STRING_RX.finditer(scope):
        value = next(g for g in m.groups() if g is not None)
        if "/" in value and not value.startswith("/") and " " not in value and value.count("/") == 1 \
                and re.match(r"^[a-z]+/[a-z0-9.+-]+$", value):
            continue
        out.append(value)
    return out


def extract_code(opts: Options, path: str, lang: str, text: str) -> Extraction:
    lines = text.count("\n") + 1
    ex = new_file(opts, path, lang, "code", lines=lines)
    walker = _Walker(opts, path, lang, text.encode("utf-8"))
    walker.run(ex)
    header = _header_comment(text)
    ex.nodes[0].text = (header + " | defines: " + ", ".join(walker.top_names[:40])).strip(" |")
    add_tags(ex, ex.nodes[0].id, text, opts)
    return ex


def _header_comment(text: str) -> str:
    """The file's leading doc comment. License headers and shebangs are skipped: in a licensed codebase
    they open every file and would make every file node read (and embed) the same."""
    pos = 0
    for _ in range(4):
        m = _LEADING_COMMENT.match(text, pos)
        if not m:
            return ""
        pos = m.end()
        if m.group(1).lstrip().startswith("#!"):
            continue
        cleaned = _clean_comment(m.group(1))
        if cleaned and not _LICENSE.search(cleaned):
            return cleaned[:400]
    return ""


def _clean_comment(raw: str) -> str:
    return " ".join(_RULE.sub(" ", _COMMENT_MARKS.sub("", raw.replace("*/", " "))).split())
