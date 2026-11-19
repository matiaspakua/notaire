"""Resident per-worktree service behind the CLI.

A CLI query is a new process: Python, numpy, the engine (~0.5 s for Notaire) and, for prose, the embedding
model load again on every call, which made `repolens search` 1-4 s against milliseconds for `rg`. The first
query in a worktree answers in-process as before and starts this service in the background; later queries
send their arguments over a Unix socket in `.repolens/` and get the same text back from the warm engine.

The client side (`ask`, `start`, `status`, `stop`) imports the standard library only. The service:
  - is unique per state folder (a lock), listens on a socket in a private per-user folder
    (`/tmp/repolens-<uid>/`, mode 0700: Unix socket paths are limited to ~104 bytes, too short for deep
    worktrees), named by a hash of the state folder
  - answers one request at a time with the same code path as the CLI (refresh when HEAD moved, usage log)
  - starts the throttled background refresh the MCP server uses, so working-tree edits are picked up
  - exits after `REPOLENS_DAEMON_IDLE` seconds without a request (default 1800), and when its own code changes
    on disk (a pull): the next query then starts a fresh one
`REPOLENS_DAEMON=0` disables it everywhere; hosts without Unix sockets never use it.
"""

from __future__ import annotations

import contextlib
import hashlib
import json
import os
import signal
import socket
import subprocess
import sys
import time
from pathlib import Path

INFO = "daemon.json"
LOG = "daemon.log"
PACKAGE = Path(__file__).resolve().parent
GLOBAL_OPTIONS = ("-c", "--config", "--db")


def socket_path(state: Path) -> Path | None:
    """The service's socket for a state folder, in a private per-user folder; None when that folder is not
    ours (another user could otherwise intercept the queries)."""
    folder = Path(os.environ.get("REPOLENS_SOCKET_DIR") or f"/tmp/repolens-{os.getuid()}")  # noqa: S108
    try:
        folder.mkdir(mode=0o700, exist_ok=True)
        info = folder.stat()
    except OSError:
        return None
    if info.st_uid != os.getuid() or info.st_mode & 0o077:
        return None
    digest = hashlib.sha1(str(state.resolve()).encode(), usedforsecurity=False).hexdigest()[:16]
    return folder / f"{digest}.sock"


def enabled() -> bool:
    return os.environ.get("REPOLENS_DAEMON", "1") != "0" and os.name == "posix" and hasattr(socket, "AF_UNIX")


def code_version() -> str:
    """Version plus the newest source mtime: a service started from older code is replaced."""
    from . import __version__

    newest = max((p.stat().st_mtime_ns for p in PACKAGE.rglob("*.py")), default=0)
    return f"{__version__}:{newest}"


# -- client -----------------------------------------------------------------------------------
def ask(state: Path, argv: list[str], timeout: float = 900.0, cwd: Path | None = None) -> dict | None:
    """The service's answer to one CLI query, or None (then the caller answers in-process). A missing or
    outdated service is started in the background for the next query."""
    path = socket_path(state) if enabled() and state.is_dir() else None
    if path is None:
        return None
    reply = _request(path, {"argv": argv, "version": code_version()}, timeout)
    if reply is None or reply.get("restart"):
        start(state, argv, cwd)
        return None
    return reply


def start(state: Path, argv: list[str], cwd: Path | None = None) -> None:
    env = dict(os.environ)
    env["PYTHONPATH"] = str(PACKAGE.parent) + (os.pathsep + env["PYTHONPATH"] if env.get("PYTHONPATH") else "")
    with open(state / LOG, "a", encoding="utf-8") as log:
        subprocess.Popen([sys.executable, "-m", "repolens", *_global_options(argv), "daemon", "run"],  # noqa: S603
                         cwd=str(cwd or os.getcwd()), env=env, stdout=log, stderr=log, stdin=subprocess.DEVNULL,
                         start_new_session=True)


def status(state: Path) -> dict | None:
    info = _info(state)
    if info is None:
        return None
    path = socket_path(state)
    reply = _request(path, {"ping": True}, 5.0) if path is not None else None
    return {**info, "alive": bool(reply and reply.get("pong"))}


def stop(state: Path) -> bool:
    path = socket_path(state)
    reply = _request(path, {"stop": True}, 10.0) if path is not None else None
    return bool(reply and reply.get("stopping"))


def _request(path: Path | None, payload: dict, timeout: float) -> dict | None:
    if path is None or not path.exists():
        return None
    try:
        with socket.socket(socket.AF_UNIX, socket.SOCK_STREAM) as conn:
            conn.settimeout(timeout)
            conn.connect(str(path))
            conn.sendall(json.dumps(payload).encode("utf-8") + b"\n")
            data = _read_line(conn)
        return json.loads(data) if data else None
    except (OSError, ValueError):
        return None


def _read_line(conn) -> bytes:
    chunks = []
    while True:
        chunk = conn.recv(1 << 16)
        if not chunk:
            break
        chunks.append(chunk)
        if chunk.endswith(b"\n"):
            break
    return b"".join(chunks)


def _global_options(argv: list[str]) -> list[str]:
    out, it = [], iter(argv)
    for token in it:
        if token in GLOBAL_OPTIONS:
            out += [token, next(it, "")]
        elif token.startswith(("--config=", "--db=")):
            out.append(token)
        elif not token.startswith("-"):
            break
    return out


def _info(state: Path) -> dict | None:
    try:
        return json.loads((state / INFO).read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return None


# -- service ----------------------------------------------------------------------------------
def run(args, state: Path) -> int:
    """`repolens daemon run`: serve until idle, outdated or stopped."""
    import fcntl

    from .cli import _parser, answer
    from .service import KnowledgeGraph

    state.mkdir(parents=True, exist_ok=True)
    lock = open(state / "daemon.lock", "a")  # noqa: SIM115 - held for the service's lifetime
    try:
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
    except BlockingIOError:
        return 0  # another service already runs for this worktree
    kg = KnowledgeGraph.open(args.config, args.db)
    version = code_version()
    idle = float(os.environ.get("REPOLENS_DAEMON_IDLE") or 1800)
    path = socket_path(state)
    if path is None:
        return 0
    path.unlink(missing_ok=True)
    server = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    old = os.umask(0o177)
    try:
        server.bind(str(path))
    finally:
        os.umask(old)
    server.listen(16)
    server.settimeout(2.0)
    signal.signal(signal.SIGTERM, lambda *_: sys.exit(0))
    (state / INFO).write_text(json.dumps({"pid": os.getpid(), "version": version, "started": time.time(),
                                          "db": str(kg.cfg.db_path), "socket": str(path)}), encoding="utf-8")
    try:
        kg.engine.warm()
        last = time.monotonic()
        while time.monotonic() - last < idle:
            try:
                conn, _ = server.accept()
            except TimeoutError:
                if code_version() != version:
                    break  # the code changed on disk: let the next query start a fresh service
                continue
            last = time.monotonic()
            with conn:
                if not _serve_one(conn, kg, version, _parser, answer):
                    break
    finally:
        server.close()
        path.unlink(missing_ok=True)
        (state / INFO).unlink(missing_ok=True)
        kg.close()
        lock.close()
    return 0


def _serve_one(conn, kg, version: str, parser, answer) -> bool:
    """Answer one request; False when the service should stop afterwards."""
    try:
        conn.settimeout(30)
        request = json.loads(_read_line(conn) or b"{}")
    except (OSError, ValueError):
        return True
    if request.get("ping"):
        _send(conn, {"pong": True})
        return True
    if request.get("stop"):
        _send(conn, {"stopping": True})
        return False
    if request.get("version") != version:
        _send(conn, {"restart": True})
        return False
    try:
        args = parser().parse_args(request["argv"])
        kg.maybe_refresh()
        code, text, notes = answer(args, kg)
        _send(conn, {"code": code, "stdout": text + "\n", "stderr": "".join(n + "\n" for n in notes)})
    except SystemExit as exc:  # argparse error: same exit as the CLI
        _send(conn, {"code": int(exc.code or 2), "stdout": "", "stderr": "repolens: invalid arguments\n"})
    except Exception as exc:  # noqa: BLE001 - one bad request must not take the service down
        _send(conn, {"code": 2, "stdout": "", "stderr": f"repolens: {type(exc).__name__}: {exc}\n"})
    return True


def _send(conn, payload: dict) -> None:
    with contextlib.suppress(OSError):  # the client gave up; nothing to report to
        conn.sendall(json.dumps(payload).encode("utf-8") + b"\n")
