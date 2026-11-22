"""Persistent SQLite store for factory telemetry (#2394).

Creates ``.aisdlc/factory.db`` with versioned schema migrations.  The file is
git-ignored via ``.aisdlc/.gitignore``.  Migration is idempotent — safe to call
on every startup.
"""
from __future__ import annotations

import sqlite3
from pathlib import Path

SCHEMA_VERSION = 1

STORE_REL = Path(".aisdlc") / "factory.db"


def store_path(root: Path) -> Path:
    return root / STORE_REL


def keep_out_of_git(root: Path, names: list[str]) -> None:
    """Generalized gitignore helper — keeps *names* out of commits.

    Used by ``store.open_store`` (factory.db) and ``serve_token.ensure_token``
    (serve.token, serve.tokens).
    """
    ignore = root / ".aisdlc" / ".gitignore"
    text = ignore.read_text(encoding="utf-8") if ignore.is_file() else ""
    existing = set(text.splitlines())
    missing = [n for n in names if n not in existing]
    if missing:
        ignore.parent.mkdir(parents=True, exist_ok=True)
        prefix = "\n" if text and not text.endswith("\n") else ""
        ignore.write_text(text + prefix + "".join(f"{n}\n" for n in missing), encoding="utf-8")


def _keep_out_of_git(root: Path) -> None:
    """Thin wrapper kept for backward compatibility."""
    keep_out_of_git(root, ["factory.db"])


def migrate(conn: sqlite3.Connection) -> None:
    """Apply V1 schema if not already applied. Idempotent."""
    conn.row_factory = sqlite3.Row
    conn.execute("CREATE TABLE IF NOT EXISTS schema_migrations "
                 "(version INTEGER PRIMARY KEY)")
    current = conn.execute(
        "SELECT version FROM schema_migrations WHERE version=?", (SCHEMA_VERSION,)
    ).fetchone()
    if current is not None:
        return

    conn.execute("CREATE TABLE IF NOT EXISTS events ("
                 "ts TEXT, kind TEXT, run_id TEXT, intent_id TEXT, "
                 "stage TEXT, step TEXT, actor TEXT, data TEXT)")
    conn.execute("CREATE TABLE IF NOT EXISTS telemetry ("
                 "ts TEXT, intent_id TEXT, run_id TEXT, stage TEXT, "
                 "step TEXT, agent TEXT, model TEXT, skill TEXT, "
                 "tool TEXT, metric TEXT, value REAL, ok INTEGER)")
    conn.execute("CREATE TABLE IF NOT EXISTS benchmarks ("
                 "name TEXT, ts TEXT, data TEXT)")
    conn.execute("CREATE TABLE IF NOT EXISTS config_snapshots ("
                 "ts TEXT, digest TEXT, files TEXT)")
    conn.execute("CREATE TABLE IF NOT EXISTS intents ("
                 "id TEXT PRIMARY KEY, ts TEXT, data TEXT)")
    conn.execute("CREATE TABLE IF NOT EXISTS sync_keys ("
                 "source TEXT, hash TEXT, PRIMARY KEY(source, hash))")
    conn.execute("INSERT OR IGNORE INTO schema_migrations VALUES (?)",
                 (SCHEMA_VERSION,))
    conn.commit()


def open_store(root: Path) -> sqlite3.Connection:
    """Create the store directory, gitignore, and return a connection."""
    db_path = store_path(root)
    db_path.parent.mkdir(parents=True, exist_ok=True)
    _keep_out_of_git(root)
    conn = sqlite3.connect(str(db_path))
    conn.execute("PRAGMA journal_mode=WAL")
    migrate(conn)
    return conn
