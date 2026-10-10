#!/usr/bin/env python3
"""
Guards issue #1250 (CU76, CONSTITUTION section 4 "Endpoints"): every REST endpoint in the committed
OpenAPI contract is called from the frontend, or is listed in contracts/api-reachability-allowlist.yaml
with a reason. A new endpoint without a UI consumer fails here, and so does an allowlist entry that is
stale (the endpoint was removed or the UI now calls it).

The scan is static and heuristic. It reads string and template literals in frontend/src (tests and
comments excluded), turns `${...}` into a wildcard segment and drops query strings. A literal passed
straight to apiGet/apiGetPaged/apiGetBytes/apiPost/apiPut/apiDelete counts only for that HTTP method;
any other literal (for example a path kept in a variable) counts for every method. A wildcard binds to
the endpoints whose path parameter sits in the same place; only when none exists does it also cover
fixed segments (`/movimiento-testimonio/${id}/${action}`).

Run with: python3 contracts/tests/test_api_reachability.py
"""
import re
import unittest
from pathlib import Path

import yaml

REPO_ROOT = Path(__file__).resolve().parents[2]
OPENAPI = REPO_ROOT / "backend-api" / "openapi" / "openapi.yaml"
FRONTEND_SRC = REPO_ROOT / "frontend" / "src"
ALLOWLIST = REPO_ROOT / "contracts" / "api-reachability-allowlist.yaml"

API_PREFIX = "/api/v1"
HTTP_METHODS = ("get", "post", "put", "delete", "patch")
ANY = "ANY"
WILDCARD = "{}"
CLIENT_METHODS = {
    "Get": "GET", "GetPaged": "GET", "GetBytes": "GET",
    "Post": "POST", "Put": "PUT", "Delete": "DELETE", "Patch": "PATCH",
}
CALL = re.compile(r"\bapi(GetPaged|GetBytes|Get|Post|Put|Delete|Patch)\b(?:<[^()]*?>)?\(\s*(?=[\"'`])")
LITERAL = re.compile(r"\"([^\"\n]*)\"|'([^'\n]*)'|`([^`]*)`")
BLOCK_COMMENT = re.compile(r"/\*.*?\*/", re.S)
LINE_COMMENT = re.compile(r"(?m)^\s*//.*$")
PATH_LIKE = re.compile(r"^/[a-z][a-z0-9-]*(/|$)")
TEST_FILE = re.compile(r"(^|/)(tests|__tests__)/|\.test\.tsx?$")


def segments(path):
    """'/api/v1/pagos/{id}' -> ('pagos', '{}')."""
    if path.startswith(API_PREFIX + "/"):
        path = path[len(API_PREFIX):]
    return tuple(WILDCARD if s.startswith("{") else s for s in path.strip("/").split("/"))


def openapi_endpoints(spec):
    """Set of 'METHOD /api/v1/path' keys declared by an OpenAPI document."""
    return {
        f"{method.upper()} {path}"
        for path, operations in (spec.get("paths") or {}).items()
        for method in operations
        if method in HTTP_METHODS
    }


def frontend_calls(sources):
    """Set of (METHOD or ANY, segments) for every API-looking literal in {relative path: text}."""
    calls = set()
    for name, text in sources.items():
        if TEST_FILE.search(name):
            continue
        text = LINE_COMMENT.sub("", BLOCK_COMMENT.sub("", text))
        verbs = {m.end(): CLIENT_METHODS[m.group(1)] for m in CALL.finditer(text)}
        for m in LITERAL.finditer(text):
            value = next(g for g in m.groups() if g is not None)
            value = re.sub(r"\$\{[^}]*\}", WILDCARD, value).split("?")[0]
            if value.startswith(API_PREFIX + "/"):
                value = value[len(API_PREFIX):]
            if value.startswith(WILDCARD + "/"):
                value = value[len(WILDCARD):]
            if PATH_LIKE.match(value):
                calls.add((verbs.get(m.start(), ANY), tuple(value.strip("/").split("/"))))
    return calls


def _fits(template, literal, fixed_ok):
    if len(template) != len(literal):
        return False
    for t, l in zip(template, literal):
        if t == l or t == WILDCARD:
            continue
        if l == WILDCARD and fixed_ok:
            continue
        return False
    return True


def reachable(endpoints, calls):
    """Endpoints covered by at least one frontend literal."""
    parsed = [(key, key.split(" ", 1)[0], segments(key.split(" ", 1)[1])) for key in endpoints]
    covered = set()
    for verb, literal in calls:
        same_verb = [(k, s) for k, m, s in parsed if verb in (m, ANY)]
        exact = [k for k, s in same_verb if _fits(s, literal, fixed_ok=False)]
        covered.update(exact or [k for k, s in same_verb if _fits(s, literal, fixed_ok=True)])
    return covered


def read_frontend_sources():
    return {
        str(p.relative_to(FRONTEND_SRC)): p.read_text(encoding="utf-8")
        for p in FRONTEND_SRC.rglob("*")
        if p.suffix in (".ts", ".tsx") and p.is_file()
    }


def unreferenced_endpoints():
    endpoints = openapi_endpoints(yaml.safe_load(OPENAPI.read_text(encoding="utf-8")))
    return endpoints, endpoints - reachable(endpoints, frontend_calls(read_frontend_sources()))


def load_allowlist():
    return (yaml.safe_load(ALLOWLIST.read_text(encoding="utf-8")) or {}).get("endpoints") or {}


class ScannerTest(unittest.TestCase):
    SPEC = {"paths": {
        "/api/v1/pagos": {"get": {}, "post": {}},
        "/api/v1/pagos/{id}": {"get": {}, "put": {}, "delete": {}},
        "/api/v1/pagos/fecha": {"get": {}},
        "/api/v1/mov/{id}/ingresar": {"post": {}},
        "/api/v1/mov/{id}/retirar": {"post": {}},
        "/api/v1/usuarios/logout": {"post": {}},
    }}

    def covered(self, source, name="hooks/usePagos.ts"):
        endpoints = openapi_endpoints(self.SPEC)
        return reachable(endpoints, frontend_calls({name: source}))

    def test_openapi_endpoints_are_method_and_path(self):
        self.assertIn("DELETE /api/v1/pagos/{id}", openapi_endpoints(self.SPEC))
        self.assertEqual(len(openapi_endpoints(self.SPEC)), 9)

    def test_client_call_counts_only_for_its_method(self):
        self.assertEqual(self.covered("apiPut<void>(`/pagos/${id}`, data)"), {"PUT /api/v1/pagos/{id}"})

    def test_generic_type_arguments_are_skipped(self):
        self.assertEqual(self.covered("apiGet<Page<Pago>>('/pagos')"), {"GET /api/v1/pagos"})

    def test_wildcard_prefers_the_path_parameter_over_a_fixed_segment(self):
        self.assertNotIn("GET /api/v1/pagos/fecha", self.covered("apiGet(`/pagos/${id}`)"))

    def test_wildcard_covers_fixed_segments_when_no_parameter_fits(self):
        self.assertEqual(
            self.covered("apiPost(`/mov/${id}/${action}`, {})"),
            {"POST /api/v1/mov/{id}/ingresar", "POST /api/v1/mov/{id}/retirar"},
        )

    def test_bare_literal_counts_for_every_method_and_query_is_dropped(self):
        self.assertEqual(
            self.covered('const path = "/pagos?size=5";'), {"GET /api/v1/pagos", "POST /api/v1/pagos"}
        )

    def test_base_url_prefix_is_ignored(self):
        self.assertEqual(
            self.covered("fetch(`${BASE_URL}/usuarios/logout`, {})"), {"POST /api/v1/usuarios/logout"}
        )
        self.assertEqual(self.covered('fetch("/api/v1/usuarios/logout")'), {"POST /api/v1/usuarios/logout"})

    def test_comments_and_test_files_are_not_consumers(self):
        self.assertEqual(self.covered("// apiGet('/pagos')\n/* apiGet('/pagos/fecha') */"), set())
        self.assertEqual(self.covered("apiGet('/pagos')", name="tests/unit/pagos.test.tsx"), set())

    def test_literal_must_start_with_a_fixed_segment(self):
        self.assertEqual(self.covered("const href = `/${slug}`;"), set())


class ReachabilityTest(unittest.TestCase):
    def test_allowlist_exists_and_gives_a_reason_per_entry(self):
        self.assertTrue(ALLOWLIST.is_file(), "contracts/api-reachability-allowlist.yaml lists API-only endpoints")
        for key, reason in load_allowlist().items():
            self.assertRegex(key, r"^(GET|POST|PUT|DELETE|PATCH) /api/v1/", f"bad allowlist key {key!r}")
            self.assertTrue(str(reason or "").strip(), f"{key} needs a reason")

    def test_owner_decisions_are_recorded(self):
        """Owner decisions of 2026-10-09 (#1336): the expiring-documents report (#1363) and manual
        testimony-movement editing (#1364) stay API-only, so no entry still waits for a decision."""
        allowlist = load_allowlist()
        pending = sorted(k for k, r in allowlist.items() if "Owner to reclassify" in str(r))
        self.assertEqual(pending, [], "entries still waiting for an Owner decision:\n  " + "\n  ".join(pending))
        for key in (
            "GET /api/v1/reportes/documentos-por-vencer/{idDocumentoPresentado}",
            "POST /api/v1/movimiento-testimonio",
            "PUT /api/v1/movimiento-testimonio/{id}",
            "DELETE /api/v1/movimiento-testimonio/{id}",
        ):
            self.assertIn("API-only", str(allowlist.get(key)), f"{key} is API-only by Owner decision")

    def test_every_endpoint_is_called_from_the_ui_or_allowlisted(self):
        _, unreferenced = unreferenced_endpoints()
        missing = sorted(unreferenced - set(load_allowlist()))
        self.assertEqual(
            missing, [],
            "Endpoints with no frontend consumer (CONSTITUTION section 4). Call them from the UI, remove "
            "them, or add them to contracts/api-reachability-allowlist.yaml with a reason:\n  "
            + "\n  ".join(missing),
        )

    def test_allowlist_has_no_stale_entries(self):
        endpoints, unreferenced = unreferenced_endpoints()
        allowlist = set(load_allowlist())
        gone = sorted(allowlist - endpoints)
        now_called = sorted((allowlist & endpoints) - unreferenced)
        self.assertEqual(gone, [], "Allowlisted endpoints no longer in OpenAPI; remove them:\n  " + "\n  ".join(gone))
        self.assertEqual(
            now_called, [], "Allowlisted endpoints the UI now calls; remove them:\n  " + "\n  ".join(now_called)
        )


if __name__ == "__main__":
    unittest.main()
