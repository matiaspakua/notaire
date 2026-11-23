"""``aisdlc serve`` HTTP host implementing factory OpenAPI paths (#2253–#2255).

Stdlib only (``http.server``). GET is always available with Bearer ``read``.
Mutating methods answer 405 unless started with ``--write``; then PUT catalogs
requires write scope, ``X-AISDLC-CSRF`` matching the bearer token, and ``If-Match``.
Phase A.4 adds GET/POST projects and runs; cancel returns 501 while runner is false.
Static UI mounts at ``/ui/`` (placeholder only).
"""
from __future__ import annotations

import json
import os
import re
import secrets
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Any
from urllib.parse import parse_qs, unquote, urlparse

from aisdlc import (
    approval,
    catalogs,
    coach,
    config_files,
    evidence,
    harness,
    isolator,
    projects,
    runs,
    workflows,
)
from aisdlc import intent_model as intent_model_mod
from aisdlc.actor import from_token_identity
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.factory import config_view, list_plugins, redact, resolve_max_open_prs
from aisdlc.kitroot import kit_root
from aisdlc.serve_token import (
    ServeToken,
    authenticate,
    ensure_token,
    load_token,
    load_tokens,
)

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
    "not_implemented": 501,
    "runner_unavailable": 501,
    "workflow_not_approved": 409,
    "literal_secret": 400,
    "already_exists": 409,
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


# The coach describes how the host's user works: only the owner reads it.
GET_SCOPE = {"/factory/coach": "owner"}


def _problem(code: str, detail: str, status: int | None = None) -> tuple[int, dict]:
    http = status or STATUS_FOR.get(code, 400)
    return http, {
        "type": "about:blank",
        "title": code.replace("_", " "),
        "status": http,
        "detail": detail,
        "code": code,
    }


def _handle_telemetry(ctx: Context, query: dict[str, list[str]]) -> dict:
    """GET /factory/telemetry handler (#2394)."""
    from aisdlc.telemetry_store import report as telemetry_report

    group = (query.get("group") or ["stage"])[0]
    # report() validates the group and raises KitError("invalid_group") itself
    return telemetry_report(ctx, group)


def _positive_int(raw: str | None) -> int | None:
    if raw is None or raw == "":
        return None
    if not raw.isdigit() or int(raw) < 1:
        raise KitError("invalid_body", f"expected a positive integer, got {raw!r}")
    return int(raw)


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
    if path == "/factory/metrics/factory":
        from aisdlc import factory_metrics

        return factory_metrics.build(ctx, usage_path=ctx.root / ".repolens" / "usage.jsonl",
                                     bench_dir=ctx.root / "framework" / "repolens" / "bench" / "results")
    if path == "/factory/line":
        from aisdlc import line as _line_mod

        return _line_mod.build_line(ctx)
    if path == "/factory/coach":
        days = (query.get("days") or [None])[0]
        return coach.report(coach.default_home(), root=ctx.root, days=_positive_int(days))
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
    if path == "/factory/projects":
        return projects.list_projects(ctx)
    m = re.fullmatch(r"/factory/projects/([^/]+)", path)
    if m:
        return projects.show(ctx, _assert_id(m.group(1)))
    if path == "/factory/runs":
        return runs.list_runs(ctx)
    m = re.fullmatch(r"/factory/runs/([^/]+)/events", path)
    if m:
        return runs.list_events(ctx, _assert_id(m.group(1)))
    m = re.fullmatch(r"/factory/runs/([^/]+)", path)
    if m:
        return runs.show(ctx, _assert_id(m.group(1)))
    # Telemetry endpoint (#2394)
    if path == "/factory/telemetry":
        return _handle_telemetry(ctx, query)
    # --- Intent routes (#2395) ---
    if path == "/factory/intents":
        return intent_model_mod.list_intents(ctx)
    m = re.fullmatch(r"/factory/intents/([^/]+)", path)
    if m:
        return intent_model_mod.show_intent(ctx, _assert_id(m.group(1)))
    # --- Config-as-code routes (#2398) ---
    if path == "/factory/config/files":
        return config_files.list_config_files(ctx)
    m = re.fullmatch(r"/factory/config/files/(.+)", path)
    if m:
        return config_files.read_config_file(ctx, m.group(1))
    # Live status endpoint (#2397)
    if path == "/factory/status":
        from aisdlc.status import status
        return status(ctx)
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
    wf = re.fullmatch(r"/factory/workflows/([^/]+)", path)
    if wf:
        return workflows.new_version(
            ctx, _assert_id(wf.group(1)), steps=body.get("steps") or [], summary=body.get("summary"),
            expected_version=expected_version, actor=actor,
        )
    m = re.fullmatch(r"/factory/catalogs/([^/]+)/([^/]+)", path)
    if not m:
        raise KitError("method_not_allowed", f"PUT not implemented for {path}")
    kind = m.group(1)
    entry_id = _assert_id(m.group(2))
    if kind not in catalogs.KINDS:
        raise KitError("unknown_kind", f"unknown catalog kind {kind!r}")
    # Body cannot set created_by / created_at / version / digest.
    return catalogs.update(
        ctx,
        kind,
        entry_id,
        ref=body.get("ref"),
        summary=body.get("summary"),
        expected_version=expected_version,
        actor=actor,
        fields=body.get("fields"),
    )


def _dispatch_delete(ctx: Context, path: str, *, expected_version: int, actor: str) -> Any:
    _guard_path(path)
    m = re.fullmatch(r"/factory/catalogs/([^/]+)/([^/]+)", path)
    if not m:
        raise KitError("method_not_allowed", f"DELETE not implemented for {path}")
    kind = m.group(1)
    entry_id = _assert_id(m.group(2))
    if kind not in catalogs.KINDS:
        raise KitError("unknown_kind", f"unknown catalog kind {kind!r}")
    return catalogs.delete(
        ctx, kind, entry_id, expected_version=expected_version, actor=actor,
    )


def _dispatch_post(ctx: Context, path: str, body: dict, *, actor: str) -> Any:
    _guard_path(path)
    if path == "/factory/projects":
        return projects.create(
            ctx,
            project_id=str(body.get("id") or ""),
            profile_ref=str(body.get("profile_ref") or "aisdlc.toml"),
            repos=list(body.get("repos") or []),
            actor=actor,
        )
    # --- Intent routes (#2395) ---
    if path == "/factory/intents":
        intent = intent_model_mod.submit(
            ctx,
            text=body.get("text"),
            ref=body.get("ref"),
            spec=body.get("spec"),
            title=body.get("title"),
            actor=actor,
        )
        return intent
    m = re.fullmatch(r"/factory/catalogs/([^/]+)", path)
    if m:
        return _create_catalog_entry(ctx, m.group(1), body, actor=actor)
    m = re.fullmatch(r"/factory/projects/([^/]+)/repos", path)
    if m:
        return projects.add_repo(
            ctx,
            project_id=_assert_id(m.group(1)),
            repo=body if isinstance(body, dict) else {},
            actor=actor,
        )
    if path == "/factory/runs":
        # --- Intent binding (#2395) ---
        intent_id = body.get("intent")
        if intent_id is not None:
            try:
                intent_model_mod.show_intent(ctx, str(intent_id))
            except KitError as exc:
                if exc.code == "not_found":
                    raise KitError("not_found", f"intent not found: {intent_id}") from exc
                raise
        workflow = body.get("workflow")
        if isinstance(workflow, dict):
            workflow_id = str(workflow.get("id") or "")
        else:
            workflow_id = str(workflow or "")
        run = runs.start(
            ctx,
            project_id=str(body.get("project") or ""),
            workflow_id=workflow_id,
            actor=actor,
            run_id=str(body["id"]) if body.get("id") else None,
            intent=str(intent_id) if intent_id is not None else None,
        )
        # Attach run to intent if present
        if intent_id is not None:
            intent_model_mod.attach_run(ctx, str(intent_id), run["id"])
        return run
    m = re.fullmatch(r"/factory/runs/([^/]+)/cancel", path)
    if m:
        runs.cancel_unavailable(ctx, _assert_id(m.group(1)))
    raise KitError("method_not_allowed", f"POST not implemented for {path}")


def _create_catalog_entry(ctx: Context, kind: str, body: dict, *, actor: str) -> dict:
    if kind not in catalogs.KINDS:
        raise KitError("unknown_kind", f"unknown catalog kind {kind!r}")
    entry_id = _assert_id(str(body.get("id") or ""))
    fields = catalogs.guard_fields(body.get("fields"))
    ref = body.get("ref")
    return catalogs.add(
        ctx, kind, entry_id, ref=str(ref) if ref else None, summary=str(body.get("summary") or ""),
        extra=fields, actor=actor,
    )


def session_view(token: ServeToken, *, enable_write: bool) -> dict:
    """Who the caller is, so a client can show read-only states instead of failing on submit."""
    return {
        "identity": token.identity,
        "scope": token.scope,
        "actor": from_token_identity(token.identity, token.scope),
        "write_enabled": enable_write and token.allows("write"),
    }


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
        def log_message(self, format: str, *args) -> None:
            return

        def _authenticate(self, need: str) -> ServeToken:
            # Reload on every request so token edits (add, revoke, scope) take effect without restart.
            tokens = load_tokens(ctx.root)
            if not tokens:
                raise KitError("unauthorized", "serve token missing")
            return authenticate(self.headers.get("Authorization"), tokens, need=need)

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

        def do_GET(self) -> None:
            self._handle("GET")

        def do_POST(self) -> None:
            self._handle("POST")

        def do_PUT(self) -> None:
            self._handle("PUT")

        def do_PATCH(self) -> None:
            self._handle("PATCH")

        def do_DELETE(self) -> None:
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

        def _handle_events(
            self, ctx: Context, method: str, parsed: Any,
        ) -> None:
            from aisdlc.events_stream import parse_last_event_id, stream

            if method != "GET":
                self._problem("method_not_allowed", f"{method} not allowed on events stream")
                return

            query = parse_qs(parsed.query, keep_blank_values=True)
            after_raw = (query.get("after") or [None])[0]
            try:
                after = parse_last_event_id(
                    self.headers.get("Last-Event-ID"), after_raw,
                )
            except KitError as exc:
                self._problem(exc.code, exc.detail)
                return

            self.send_response(200)
            self.send_header("Content-Type", "text/event-stream; charset=utf-8")
            self.send_header("Cache-Control", "no-store")
            self.send_header("Connection", "close")
            self.send_header("X-Accel-Buffering", "no")
            self.end_headers()
            self.close_connection = True

            def write(data: bytes) -> None:
                self.wfile.write(data)
                self.wfile.flush()

            stop_event = getattr(self.server, "stop_event", None)

            def should_stop() -> bool:
                return bool(stop_event and stop_event.is_set())

            try:
                stream(ctx, write, after, should_stop=should_stop)
            except (BrokenPipeError, ConnectionResetError, OSError):
                pass

        def _handle(self, method: str) -> None:
            parsed = urlparse(self.path)
            path = parsed.path or "/"
            try:
                caller = self._authenticate(GET_SCOPE.get(path, "read") if method == "GET" else "read")
            except KitError as exc:
                self._problem(exc.code, exc.detail)
                return
            if method == "GET" and path == "/factory/session":
                self._send_json(200, session_view(caller, enable_write=enable_write))
                return
            # SSE events stream (#2397) — before JSON dispatch
            if path == "/factory/events":
                self._handle_events(ctx, method, parsed)
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
                    active = self._authenticate("write")
                    _require_csrf(self.headers, active)
                    expected = _parse_if_match(self.headers.get("If-Match"))
                    body = self._read_json_body()
                    actor = from_token_identity(active.identity, active.scope)
                    data = _dispatch_put(
                        ctx, path, body, expected_version=expected, actor=actor,
                    )
                    self._send_json(200, data)
                    return
                if method == "POST":
                    active = self._authenticate("write")
                    _require_csrf(self.headers, active)
                    body = self._read_json_body()
                    actor = from_token_identity(active.identity, active.scope)
                    data = _dispatch_post(ctx, path, body, actor=actor)
                    # POST /factory/intents returns 201 (#2395)
                    status_code = 201 if path == "/factory/intents" else 200
                    self._send_json(status_code, data)
                    return
                if method == "DELETE":
                    active = self._authenticate("write")
                    _require_csrf(self.headers, active)
                    expected = _parse_if_match(self.headers.get("If-Match"))
                    actor = from_token_identity(active.identity, active.scope)
                    data = _dispatch_delete(
                        ctx, path, expected_version=expected, actor=actor,
                    )
                    self._send_json(200, data)
                    return
                self._problem("method_not_allowed", f"{method} not implemented")
            except KitError as exc:
                self._problem(exc.code, exc.detail)

    class _FactoryServer(ThreadingHTTPServer):
        daemon_threads = True

        def __init__(self, *args: Any, **kwargs: Any) -> None:
            super().__init__(*args, **kwargs)
            self.stop_event = threading.Event()

        def shutdown(self, request_handler=None) -> None:
            self.stop_event.set()
            super().shutdown()

    return _FactoryServer((host, port), Handler)


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
