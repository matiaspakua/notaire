"""Factory store schema and migration tests (#2394).

Tests the aisdlc.store module: SQLite database creation, idempotent migrations,
and .gitignore side-effect.  These tests must fail with ImportError until
the production module exists.
"""
from __future__ import annotations

import os
import sqlite3
import shutil
import sys
import tempfile
import unittest
from pathlib import Path

from helpers import KIT

sys.path.insert(0, str(KIT))


class StoreSchemaTest(unittest.TestCase):
    """Verify store.py creates the correct schema and is idempotent."""

    def setUp(self) -> None:
        self.root = Path(tempfile.mkdtemp(prefix="aisdlc-store-"))
        self.addCleanup(shutil.rmtree, self.root, ignore_errors=True)

    # ------------------------------------------------------------------
    # Schema creation
    # ------------------------------------------------------------------
    def test_schema_creates_all_tables(self) -> None:
        """First migration creates all five data tables and schema_migrations."""
        from aisdlc.store import open_store, store_path

        db = open_store(self.root)
        db_path = store_path(self.root)
        self.assertTrue(db_path.is_file(), "factory.db should be created")

        conn = sqlite3.connect(str(db_path))
        conn.row_factory = sqlite3.Row
        tables = [r["name"] for r in conn.execute(
            "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name"
        ).fetchall()]
        expected = sorted([
            "events", "intents", "telemetry", "benchmarks",
            "config_snapshots", "schema_migrations",
        ])
        self.assertEqual(expected, sorted(tables))
        conn.close()

    def test_events_table_columns(self) -> None:
        """events table has the columns defined in #2393."""
        from aisdlc.store import open_store, store_path

        db = open_store(self.root)
        conn = sqlite3.connect(str(store_path(self.root)))
        conn.row_factory = sqlite3.Row
        cols = [r["name"] for r in conn.execute("PRAGMA table_info(events)").fetchall()]
        for col in ("ts", "kind", "run_id", "intent_id", "stage", "step", "actor", "data"):
            self.assertIn(col, cols, f"events missing column {col}")
        conn.close()

    def test_telemetry_table_columns(self) -> None:
        """telemetry table has the columns defined in #2393."""
        from aisdlc.store import open_store, store_path

        db = open_store(self.root)
        conn = sqlite3.connect(str(store_path(self.root)))
        conn.row_factory = sqlite3.Row
        cols = [r["name"] for r in conn.execute("PRAGMA table_info(telemetry)").fetchall()]
        for col in ("ts", "intent_id", "run_id", "stage", "step", "agent", "model",
                     "skill", "tool", "metric", "value", "ok"):
            self.assertIn(col, cols, f"telemetry missing column {col}")
        conn.close()

    def test_benchmarks_table_columns(self) -> None:
        from aisdlc.store import open_store, store_path

        db = open_store(self.root)
        conn = sqlite3.connect(str(store_path(self.root)))
        conn.row_factory = sqlite3.Row
        cols = [r["name"] for r in conn.execute("PRAGMA table_info(benchmarks)").fetchall()]
        for col in ("name", "ts", "data"):
            self.assertIn(col, cols, f"benchmarks missing column {col}")
        conn.close()

    def test_config_snapshots_table_columns(self) -> None:
        from aisdlc.store import open_store, store_path

        db = open_store(self.root)
        conn = sqlite3.connect(str(store_path(self.root)))
        conn.row_factory = sqlite3.Row
        cols = [r["name"] for r in conn.execute("PRAGMA table_info(config_snapshots)").fetchall()]
        for col in ("ts", "digest", "files"):
            self.assertIn(col, cols, f"config_snapshots missing column {col}")
        conn.close()

    def test_schema_migrations_table(self) -> None:
        from aisdlc.store import open_store, store_path, SCHEMA_VERSION

        db = open_store(self.root)
        conn = sqlite3.connect(str(store_path(self.root)))
        conn.row_factory = sqlite3.Row
        rows = conn.execute("SELECT version FROM schema_migrations").fetchall()
        self.assertEqual(1, len(rows))
        self.assertEqual(SCHEMA_VERSION, rows[0]["version"])
        conn.close()

    # ------------------------------------------------------------------
    # Idempotent migration
    # ------------------------------------------------------------------
    def test_migration_idempotent(self) -> None:
        """Calling migrate() twice produces no error and the same schema."""
        from aisdlc.store import open_store, store_path, migrate, SCHEMA_VERSION

        open_store(self.root)
        db_path = store_path(self.root)

        conn = sqlite3.connect(str(db_path))
        migrate(conn)
        migrate(conn)
        rows = conn.execute("SELECT version FROM schema_migrations").fetchall()
        self.assertEqual(1, len(rows))
        self.assertEqual(SCHEMA_VERSION, rows[0]["version"])
        conn.close()

    # ------------------------------------------------------------------
    # Gitignore
    # ------------------------------------------------------------------
    def test_gitignore_contains_factory_db(self) -> None:
        """open_store writes .aisdlc/.gitignore containing factory.db."""
        from aisdlc.store import open_store

        open_store(self.root)
        gitignore = self.root / ".aisdlc" / ".gitignore"
        self.assertTrue(gitignore.is_file(), ".gitignore should be created")
        content = gitignore.read_text(encoding="utf-8")
        self.assertIn("factory.db", content)


class StorePathTest(unittest.TestCase):
    """store_path returns the correct path without creating the DB."""

    def test_store_path_returns_correct_path(self) -> None:
        from aisdlc.store import store_path

        root = Path("/tmp/test-root")
        self.assertEqual(root / ".aisdlc" / "factory.db", store_path(root))


class StoreOpenStoreTest(unittest.TestCase):
    """open_store creates .aisdlc dir and returns a connection."""

    def setUp(self) -> None:
        self.root = Path(tempfile.mkdtemp(prefix="aisdlc-store-"))
        self.addCleanup(shutil.rmtree, self.root, ignore_errors=True)

    def test_open_store_creates_directory(self) -> None:
        from aisdlc.store import open_store

        conn = open_store(self.root)
        self.assertTrue((self.root / ".aisdlc").is_dir())
        self.assertIsNotNone(conn)


if __name__ == "__main__":
    unittest.main()
