"""Static receiver types of Java calls, from what one file declares.

A call is emitted with a *receiver hint* the linker resolves across files:

  T:Folio            a type by simple name (a field, parameter, local, cast, `new`, or a static `Folio.x()`)
  C:Outer.Inner      the enclosing class of this file (`this`, or no receiver at all)
  S:Outer.Inner      its superclass (`super`)
  ...#getFolio       the return type of `getFolio()` on the type so far (chains: `a.getFolio().getDto()`)

An empty hint means the receiver is not known (a lambda parameter, an array element, a generic): the linker
then falls back to name-based resolution. The environment is per method (parameters, then locals in source
order) over the class's fields.
"""

from __future__ import annotations

import re

_PRIMITIVES = frozenset({"int", "long", "short", "byte", "char", "boolean", "float", "double", "void"})
_GENERICS = re.compile(r"<[^<>]*>")
_ANNOTATIONS = re.compile(r"@[\w.]+(\([^)]*\))?\s*")
LOCAL_DECLARATIONS = frozenset({
    "local_variable_declaration", "enhanced_for_statement", "catch_formal_parameter", "resource",
    "instanceof_expression",
})
CLASS_KINDS = frozenset({"class", "interface", "enum"})


def generic_type(text: str) -> str:
    """`java.util.List<com.x.Folio>` -> `List<Folio>` (the first type argument is what element methods and
    lambdas need); without type arguments the same as `simple_type`."""
    t = _ANNOTATIONS.sub("", text or "").strip().replace("final ", "")
    if "<" not in t or t.endswith("]"):
        return simple_type(t)
    base, _, rest = t.partition("<")
    depth, arg = 0, ""
    for ch in rest:
        if ch == "<":
            depth += 1
        elif ch == ">" and depth == 0 or ch == "," and depth == 0:
            break
        elif ch == ">":
            depth -= 1
        arg += ch
    outer, inner = simple_type(base), simple_type(arg.replace("? extends ", "").replace("? super ", ""))
    if not outer:
        return ""
    return f"{outer}<{inner}>" if inner and inner != "?" else outer


def simple_type(text: str) -> str:
    """`java.util.List<Foo>` -> `List`, `Foo[]` / primitives -> '' (no repository methods), `var` kept."""
    t = _ANNOTATIONS.sub("", text or "").strip()
    while "<" in t:
        stripped = _GENERICS.sub("", t)
        if stripped == t:
            break
        t = stripped
    t = t.replace("final ", "").strip()
    if not t or t.endswith("]") or t in _PRIMITIVES:
        return ""
    return t.rsplit(".", 1)[-1].strip()


class JavaTypes:
    def __init__(self, text):
        self.text = text                      # node -> source text (the walker's)
        self.vars: dict[str, dict[str, str]] = {}
        self.owner: dict[str, str] = {}       # scope id -> qualified name of its class
        self.fields: dict[str, dict[str, str]] = {}

    # -- declarations ----------------------------------------------------------------------
    def enter_class(self, node, sid: str, qual: str) -> None:
        self.owner[sid] = qual
        fields: dict[str, str] = {}
        params = node.child_by_field_name("parameters")  # record components
        for p in params.named_children if params is not None else ():
            self._bind(fields, p.child_by_field_name("type"), p.child_by_field_name("name"))
        body = node.child_by_field_name("body")
        members = list(body.named_children) if body is not None else []
        for child in list(members):
            if child.type == "enum_body_declarations":
                members.extend(child.named_children)
        for child in members:
            if child.type in ("field_declaration", "constant_declaration"):
                for d in child.children_by_field_name("declarator"):
                    self._bind(fields, child.child_by_field_name("type"), d.child_by_field_name("name"))
            elif child.type == "enum_constant":
                name = child.child_by_field_name("name")
                if name is not None:
                    fields[self.text(name)] = qual.rsplit(".", 1)[-1]
        self.fields[qual] = fields

    def enter_method(self, node, sid: str, parent) -> None:
        self.owner[sid] = parent.qual if parent.kind in CLASS_KINDS else ""
        env: dict[str, str] = {}
        params = node.child_by_field_name("parameters")
        for p in params.named_children if params is not None else ():
            if p.type in ("formal_parameter", "spread_parameter"):
                name = p.child_by_field_name("name") or next(
                    (c for c in p.named_children if c.type == "variable_declarator"), None)
                if name is not None and name.type == "variable_declarator":
                    name = name.child_by_field_name("name")
                typ = generic_type(self.text(p.child_by_field_name("type")) if p.child_by_field_name("type")
                                   else "")
                if name is not None and typ:
                    env[self.text(name)] = "T:" + typ
        self.vars[sid] = env

    def declare(self, node, scope) -> None:
        """A local declaration seen in source order inside a method (or a field initializer)."""
        env = self.vars.setdefault(scope.sid, {})
        t = node.type
        if t == "local_variable_declaration":
            typ = node.child_by_field_name("type")
            for d in node.children_by_field_name("declarator"):
                name, value = d.child_by_field_name("name"), d.child_by_field_name("value")
                if name is None:
                    continue
                if typ is not None and self.text(typ) == "var":
                    hint = self.type_of(value, scope) if value is not None else ""
                else:
                    hint = self._hint(typ)
                if hint:
                    env[self.text(name)] = hint
        elif t in ("enhanced_for_statement", "resource"):
            name, typ = node.child_by_field_name("name"), node.child_by_field_name("type")
            if name is None or typ is None:
                return
            if self.text(typ) != "var":
                if self._hint(typ):
                    env[self.text(name)] = self._hint(typ)
            elif t == "enhanced_for_statement":
                source = self.type_of(node.child_by_field_name("value"), scope)
                if source:
                    env[self.text(name)] = source + "#@"
        elif t == "catch_formal_parameter":
            name = node.child_by_field_name("name")
            types = [c for c in node.named_children if c.type == "catch_type"]
            if name is not None and types and types[0].named_children:
                hint = self._hint(types[0].named_children[0])
                if hint:
                    env[self.text(name)] = hint
        elif t == "instanceof_expression":
            name, typ = node.child_by_field_name("name"), node.child_by_field_name("right")
            if name is not None and typ is not None and self._hint(typ):
                env[self.text(name)] = self._hint(typ)

    def lambda_params(self, node, scope) -> None:
        """`xs.forEach(x -> ...)`, `stream.map(x -> ...)`: an untyped lambda parameter is an element of the
        receiver of the call the lambda is passed to."""
        params = node.child_by_field_name("parameters")
        if params is None:
            return
        names = [params] if params.type == "identifier" else [
            c for c in params.named_children if c.type == "identifier"]
        args = node.parent
        call = args.parent if args is not None and args.type == "argument_list" else None
        if call is None or call.type != "method_invocation" or len(names) != 1:
            return
        source = self.receiver(call, scope)
        if source:
            self.vars.setdefault(scope.sid, {})[self.text(names[0])] = source + "#@"

    # -- expressions -----------------------------------------------------------------------
    def receiver(self, call, scope) -> str:
        """Hint for the receiver of a `method_invocation`."""
        obj = call.child_by_field_name("object")
        if obj is None:
            cls = self.owner.get(scope.sid, "")
            return f"C:{cls}" if cls else ""
        return self.type_of(obj, scope)

    def type_of(self, expr, scope) -> str:  # noqa: C901 - one case per expression kind
        if expr is None:
            return ""
        t = expr.type
        if t == "identifier":
            name = self.text(expr)
            hint = self.vars.get(scope.sid, {}).get(name) or self._field(name, scope)
            if hint:
                return hint
            return f"T:{name}" if name[:1].isupper() else ""
        if t == "this":
            cls = self.owner.get(scope.sid, "")
            return f"C:{cls}" if cls else ""
        if t == "super":
            cls = self.owner.get(scope.sid, "")
            return f"S:{cls}" if cls else ""
        if t == "field_access":
            obj, fld = expr.child_by_field_name("object"), expr.child_by_field_name("field")
            if obj is not None and fld is not None and obj.type == "this":
                return self._field(self.text(fld), scope)
            return ""
        if t == "method_invocation":
            base = self.receiver(expr, scope)
            name = expr.child_by_field_name("name")
            return f"{base}#{self.text(name)}" if base and name is not None else ""
        if t == "object_creation_expression":
            return self._hint(expr.child_by_field_name("type"))
        if t == "cast_expression":
            return self._hint(expr.child_by_field_name("type"))
        if t == "parenthesized_expression" and expr.named_children:
            return self.type_of(expr.named_children[0], scope)
        if t in ("string_literal", "text_block"):
            return "T:String"
        return ""

    # -- helpers ---------------------------------------------------------------------------
    def _field(self, name: str, scope) -> str:
        cls = self.owner.get(scope.sid, "")
        while cls:
            typ = self.fields.get(cls, {}).get(name)
            if typ:
                return "T:" + typ
            cls = cls.rsplit(".", 1)[0] if "." in cls else ""
        return ""

    def _hint(self, node) -> str:
        typ = generic_type(self.text(node)) if node is not None else ""
        return f"T:{typ}" if typ and typ != "var" else ""

    def _bind(self, table: dict, type_node, name_node) -> None:
        if type_node is None or name_node is None:
            return
        typ = generic_type(self.text(type_node))
        if typ:
            table[self.text(name_node)] = typ


def return_type(node, text) -> str:
    """Return type of a `method_declaration` with its first type argument ('' for void, primitives, arrays,
    constructors)."""
    typ = node.child_by_field_name("type")
    return generic_type(text(typ)) if typ is not None else ""
