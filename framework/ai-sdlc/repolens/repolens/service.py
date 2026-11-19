"""Application facade shared by the CLI, the HTTP API and the MCP server."""

from __future__ import annotations

import os
import threading
import time
from pathlib import Path

from .config import Config, Source, load_config
from .engine import Engine
from .indexer import index
from .store import Store


class KnowledgeGraph:
    def __init__(self, cfg: Config):
        self.cfg = cfg
        self._store: Store | None = None
        self._engine: Engine | None = None
        self._lock = threading.RLock()
        self._refresh_lock = threading.Lock()
        self._refreshing = False
        self._last_refresh = float("-inf")

    @classmethod
    def open(cls, config: Path | None = None, db: Path | None = None) -> KnowledgeGraph:
        return cls(load_config(config, db))

    @property
    def store(self) -> Store:
        if self._store is None:
            self._store = Store(self.cfg.db_path)
        return self._store

    @property
    def engine(self) -> Engine:
        with self._lock:
            if self._engine is None:
                self._engine = Engine(self.store, self.cfg)
            else:
                self._engine.refresh()
            return self._engine

    def add_paths(self, paths: list[str], name: str | None = None) -> list[str]:
        names = []
        for raw in paths:
            path = Path(raw).expanduser().resolve()
            src_name = name if (name and len(paths) == 1) else path.name
            existing = next((s for s in self.cfg.sources if s.name == src_name), None)
            if existing is None:
                self.cfg.sources.append(Source(src_name, path))
            names.append(src_name)
        return names

    def sources_from_store(self) -> None:
        known = {s.name for s in self.cfg.sources}
        for project in self.store.projects():
            if project["name"] not in known:
                self.cfg.sources.append(Source(project["name"], Path(project["root"])))

    def index(self, only: list[str] | None = None, full: bool = False, embed: bool = True, log=print,
              wait: bool = True) -> dict:
        with self._lock:
            if not self.cfg.sources:
                self.sources_from_store()
            if not self.cfg.sources:
                raise ValueError("nothing to index: pass folders or configure [[source]] entries")
            report = index(self.cfg, self.store, only=only, full=full, embed=embed, log=log, wait=wait)
            if self._engine is not None:
                self._engine.load()
            return report

    @staticmethod
    def refresh_interval() -> float:
        """Seconds between automatic refreshes (`$REPOLENS_REFRESH_SECONDS`, default 15; negative disables)."""
        try:
            return float(os.environ.get("REPOLENS_REFRESH_SECONDS", "15"))
        except ValueError:
            return 15.0

    @property
    def refreshing(self) -> bool:
        return self._refreshing

    def maybe_refresh(self, on_done=None, force: bool = False) -> bool:
        """Start an incremental index in the background when the interval has passed and none is running.
        Queries keep answering from the last published graph meanwhile (the engine reloads when the run
        publishes); the run uses its own connection and the store's index lock, so concurrent agents and
        git hooks never index the same store twice at once."""
        interval = self.refresh_interval()
        if interval < 0 and not force:
            return False
        from .extractors import EXTRACTOR_VERSION
        from .freshness import status

        if status(self.store, extractors=EXTRACTOR_VERSION)[0]:
            force = True
        now = time.monotonic()
        with self._refresh_lock:
            if self._refreshing or (not force and now - self._last_refresh < interval):
                return False
            self._refreshing = True
            self._last_refresh = now
        threading.Thread(target=self._refresh_worker, args=(on_done,), daemon=True, name="repolens-refresh").start()
        return True

    def _refresh_worker(self, on_done) -> None:
        clock = time.perf_counter()
        result: dict = {}
        store = None
        try:
            if not self.cfg.sources:
                self.sources_from_store()
            if self.cfg.sources:
                store = Store(self.cfg.db_path)
                report = index(self.cfg, store, log=lambda *_a, **_k: None, wait=False)
                result = {"busy": report.get("busy", False), "changed": report.get("changed", False),
                          "extracted": sum(s.get("extracted", 0) + s.get("removed", 0)
                                           for s in report.get("sources", {}).values())}
        except Exception as exc:  # noqa: BLE001 - a failed refresh must not take the server down
            result = {"error": f"{type(exc).__name__}: {exc}"[:300]}
        finally:
            if store is not None:
                store.close()
            with self._refresh_lock:
                self._refreshing = False
            result["ms"] = round((time.perf_counter() - clock) * 1000)
            if on_done is not None:
                on_done(result)

    def close(self) -> None:
        if self._store is not None:
            self._store.close()
