"""``aisdlc serve`` HTTP host implementing factory OpenAPI paths (#2253 / #2254).

Stdlib only (``http.server``). GET is always available with Bearer ``read``.
Mutating methods answer 405 unless started with ``--write``; then PUT catalogs
requires write scope, ``X-AISDLC-CSRF`` matching the bearer token, and ``If-Match``.
Static UI mounts at ``/ui/`` (placeholder only).
"""
from __future__ import annotations

import json
import os
import re
import secrets
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Any
from urllib.parse import parse_qs, unquote, urlparse

from aisdlc import approval, catalogs, evidence, harness, isolator, workflows
from aisdlc.actor import from_token_identity
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.factory import config_view, list_plugins, redact, resolve_max_open_prs
from aisdlc.kitroot import kit_root
from aisdlc.serve_token import ServeToken, ensure_token, load_token, require_token

ID_RE = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._-]{0,127}$")
LOOPBACK = {"127.0.0.1", "::1", "localhost"}
MUTATING = {"POST", "PUT", "PATCH", "DELETE"}
CSRF_HEADER = "X-AISDLC-CSRF"
STATUS_FOR = {
    "unauthorized": 401,
    "forbidden": 403,
    "not_found": 404,
    "method_not_allowed": 405,
    "version_conflict": 409,
    "if_match_required": 428,
    "csrf_required": 403,
    "writes_disabled": 405,
}


def is_loopback(host: str) -> bool:
    return (host or "").strip().casefold() in LOOPBACK


def assert_bind_allowed(host: str, *, allow_remote: bool, token_configured: bool) -> None:
    if is_loopback(host):
        return
    if allow_remote and token_configured:
        return
    raise KitError(
        "bind_refused",
        "non-loopback bind requires --allow-remote and a configured .aisdlc/serve.token",
    )


def load_token_value(root: Path) -> str:
    tok = load_token(root)
    return tok.value if tok else ""


def capabilities(ctx: Context) -> dict[str, Any]:
    """Honest port truth — false when not proven (never invent sandbox/runner)."""
    approvals = approval.approval_status(ctx)
    iso = isolator.isolator_status()
    return {
        "approvals_live": bool(approvals.get("approvals_live")),
        "runner": False,
        "forge_write": False,
        "isolator": {
            "sandbox": bool(iso.get("sandbox")),
            "enforces_committer": bool(iso.get("enforces_committer")),
            "backend": iso.get("backend"),
            "status": iso.get("status"),
        },
        "detail": {
            "approvals": approvals.get("detail"),
            "isolator": iso.get("detail"),
            "runner": "no executable runner port proven — harness stubs are inventory only",
            "forge_write": "write_enabled defaults false until Approval verify + scoped --write",
        },
    }


def _assert_id(value: str) -> str:
    decoded = unquote(value)
    if (
        not decoded
        or not ID_RE.fullmatch(decoded)
        or "/" in decoded
        or "\\" in decoded
        or decoded.startswith(".")
        or ".." in decoded
    ):
        raise KitError("invalid_id", f"invalid id {decoded!r}")
    return decoded


def _problem(code: str, detail: str, status: int | None = None) -> tuple[int, dict]:
    http = status or STATUS_FOR.get(code, 400)
    return http, {
        "type": "about:blank",
        "title": code.replace("_", " "),
        "status": http,
        "detail": detail,
        "code": code,
    }


def _guard_path(path: str) -> None:
    """Reject traversal and encoded separators before routing."""
    if "%2f" in path.casefold() or "%5c" in path.casefold():
        raise KitError("invalid_id", "encoded path separator refused")
    for part in path.split("/"):
        if not part:
            continue
        if part == ".." or part.startswith("."):
            raise KitError("invalid_id", f"invalid path segment {part!r}")


def _dispatch_get(ctx: Context, path: str, query: dict[str, list[str]]) -> Any:
    _guard_path(path)
    if path == "/factory/capabilities":
        return capabilities(ctx)
    if path == "/factory/config":
        limit = resolve_max_open_prs(ctx.profile, os.environ, None)
        return config_view(ctx.profile, limit=limit)
    if path == "/factory/plugins":
        return list_plugins(ctx.root, ctx.profile)
    if path == "/factory/harness":
        return harness.list_adapters(ctx.root, ctx.profile)
    if path == "/factory/report":
        return evidence.report_summary(ctx)
    if path == "/factory/workflows":
        approved = (query.get("approved") or [None])[0]
        flag = str(approved).casefold() in {"1", "true", "yes"} if approved is not None else False
        return workflows.list_workflows(ctx, approved_only=flag)
    m = re.fullmatch(r"/factory/workflows/([^/]+)", path)
    if m:
        return workflows.show(ctx, _assert_id(m.group(1)))
    m = re.fullmatch(r"/factory/catalogs/([^/]+)", path)
    if m:
        return catalogs.list_entries(ctx, m.group(1))
    m = re.fullmatch(r"/factory/catalogs/([^/]+)/([^/]+)", path)
    if m:
        return catalogs.show(ctx, m.group(1), _assert_id(m.group(2)), redacted=True)
    m = re.fullmatch(r"/factory/catalogs/([^/]+)/([^/]+)/versions", path)
    if m:
        return catalogs.history(ctx, m.group(1), _assert_id(m.group(2)))
    raise KitError("not_found", f"unknown path {path}")


def _parse_if_match(raw: str | None) -> int:
    if raw is None or not str(raw).strip():
        raise KitError("if_match_required", "PUT requires If-Match with the expected version")
    value = str(raw).strip()
    if value.startswith("W/"):
        value = value[2:].strip()
    if value.startswith('"') and value.endswith('"'):
        value = value[1:-1]
    try:
        return int(value)
    except ValueError as exc:
        raise KitError("if_match_required", f"If-Match must be an integer version, got {raw!r}") from exc


def _require_csrf(headers, token: ServeToken) -> None:
    presented = headers.get(CSRF_HEADER) or headers.get(CSRF_HEADER.casefold())
    # BaseHTTPRequestHandler lowercases header keys in some paths; scan casefold.
    if presented is None:
        for key, value in headers.items():
            if key.casefold() == CSRF_HEADER.casefold():
                presented = value
                break
    if presented is None or not secrets.compare_digest(str(presented).strip(), token.value):
        raise KitError("csrf_required", f"mutating requests require {CSRF_HEADER} matching the bearer token")


def _dispatch_put(ctx: Context, path: str, body: dict, *, expected_version: int, actor: str) -> Any:
    _guard_path(path)
    m = re.fullmatch(r"/factory/catalogs/([^/]+)/([^/]+)", path)
    if not m:
        raise KitError("method_not_allowed", f"PUT not implemented for {path}")
    kind = m.group(1)
    entry_id = _assert_id(m.group(2))
    if kind not in catalogs.KINDS:
        raise KitError("unknown_kind", f"unknown catalog kind {kind!r}")
    # Body cannot set created_by / created_at / version / digest.
    summary = body.get("summary") if isinstance(body, dict) else None
    ref = body.get("ref") if isinstance(body, dict) else None
    return catalogs.update(
        ctx,
        kind,
        entry_id,
        ref=ref,
        summary=summary,
        expected_version=expected_version,
        actor=actor,
    )


def static_root() -> Path:
    return kit_root() / "ui"


def start_server(
    ctx: Context,
    *,
    host: str = "127.0.0.1",
    port: int = 8765,
    allow_remote: bool = False,
    create_token: bool = True,
    enable_write: bool = False,
) -> ThreadingHTTPServer:
    configured = load_token(ctx.root) is not None
    assert_bind_allowed(host, allow_remote=allow_remote, token_configured=configured)

    if not is_loopback(host):
        token = load_token(ctx.root)
        if token is None:
            raise KitError(
                "bind_refused",
                "non-loopback bind requires an existing .aisdlc/serve.token",
            )
    elif create_token:
        token = ensure_token(ctx.root)
    else:
        token = load_token(ctx.root)
        if token is None:
            raise KitError("unauthorized", "serve token missing")

    ui = static_root()
    ui.mkdir(parents=True, exist_ok=True)

    class Handler(BaseHTTPRequestHandler):
        def log_message(self, format: str, *args) -> None:  # noqa: A003
            return

        def _token(self) -> ServeToken:
            # Reload so scope/identity edits to serve.token take effect without restart.
            current = load_token(ctx.root)
            if current is None:
                raise KitError("unauthorized", "serve token missing")
            return current

        def _send(self, status: int, payload: bytes, content_type: str) -> None:
            self.send_response(status)
            self.send_header("Content-Type", content_type)
            self.send_header("Content-Length", str(len(payload)))
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(payload)

        def _send_json(self, status: int, data: Any, *, content_type: str = "application/json") -> None:
            body = json.dumps(redact("", data), sort_keys=True).encode("utf-8")
            self._send(status, body, content_type)

        def _problem(self, code: str, detail: str) -> None:
            http, doc = _problem(code, detail)
            self._send_json(http, doc, content_type="application/problem+json")

        def _serve_static(self, rel: str) -> None:
            base = ui.resolve()
            target = (base / rel).resolve() if rel else base
            if base != target and base not in target.parents:
                self._problem("invalid_id", "path traversal blocked")
                return
            if target.is_dir():
                readme = target / "README.md"
                if readme.is_file():
                    self._send(200, readme.read_bytes(), "text/plain; charset=utf-8")
                    return
                self._send(200, b"aisdlc ui mount\n", "text/plain; charset=utf-8")
                return
            if not target.is_file():
                self._problem("not_found", "static asset not found")
                return
            ctype = "text/css" if target.suffix == ".css" else "text/plain; charset=utf-8"
            self._send(200, target.read_bytes(), ctype)

        def do_GET(self) -> None:  # noqa: N802
            self._handle("GET")

        def do_POST(self) -> None:  # noqa: N802
            self._handle("POST")

        def do_PUT(self) -> None:  # noqa: N802
            self._handle("PUT")

        def do_PATCH(self) -> None:  # noqa: N802
            self._handle("PATCH")

        def do_DELETE(self) -> None:  # noqa: N802
            self._handle("DELETE")

        def _read_json_body(self) -> dict:
            length = int(self.headers.get("Content-Length") or 0)
            raw = self.rfile.read(length) if length > 0 else b"{}"
            if not raw:
                return {}
            try:
                data = json.loads(raw.decode("utf-8"))
            except (UnicodeDecodeError, json.JSONDecodeError) as exc:
                raise KitError("invalid_body", "request body must be JSON object") from exc
            if not isinstance(data, dict):
                raise KitError("invalid_body", "request body must be a JSON object")
            return data

        def _handle(self, method: str) -> None:
            parsed = urlparse(self.path)
            path = parsed.path or "/"
            try:
                active = self._token()
                require_token(self.headers.get("Authorization"), active, need="read")
            except KitError as exc:
                self._problem(exc.code, exc.detail)
                return
            if method in MUTATING and not enable_write:
                self._problem("method_not_allowed", f"{method} not allowed on read-only host")
                return
            if path == "/ui" or path.startswith("/ui/"):
                if method != "GET":
                    self._problem("method_not_allowed", f"{method} not allowed on static mount")
                    return
                rel = unquote(path[len("/ui/") :]) if path.startswith("/ui/") else ""
                if ".." in rel.split("/") or "%2f" in path.casefold():
                    self._problem("invalid_id", "path traversal blocked")
                    return
                self._serve_static(rel)
                return
            try:
                if method == "GET":
                    query = parse_qs(parsed.query, keep_blank_values=True)
                    data = _dispatch_get(ctx, path, query)
                    self._send_json(200, data)
                    return
                if method == "PUT":
                    active = self._token()
                    require_token(self.headers.get("Authorization"), active, need="write")
                    _require_csrf(self.headers, active)
                    expected = _parse_if_match(self.headers.get("If-Match"))
                    body = self._read_json_body()
                    actor = from_token_identity(active.identity, active.scope)
                    data = _dispatch_put(
                        ctx, path, body, expected_version=expected, actor=actor,
                    )
                    self._send_json(200, data)
                    return
                self._problem("method_not_allowed", f"{method} not implemented")
            except KitError as exc:
                self._problem(exc.code, exc.detail)

    return ThreadingHTTPServer((host, port), Handler)


def serve(
    ctx: Context,
    *,
    host: str = "127.0.0.1",
    port: int = 8765,
    allow_remote: bool = False,
    enable_write: bool = False,
) -> int:
    try:
        server = start_server(
            ctx,
            host=host,
            port=port,
            allow_remote=allow_remote,
            enable_write=enable_write,
        )
    except KitError as exc:
        print(f"error: {exc.detail}")
        return 2
    bound = server.server_address[1]
    mode = "write" if enable_write else "read-only"
    print(f"aisdlc serve ({mode}): http://{host}:{bound}/ (token: .aisdlc/serve.token; Ctrl+C to stop)")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\naisdlc serve: stopped")
    finally:
        server.server_close()
    return 0
