"""One indexer per store at a time, across processes (several agents and their MCP servers share a worktree).

Readers never take the lock: SQLite WAL lets them read the last published state while an index run writes.
"""

from __future__ import annotations

import time
from contextlib import contextmanager
from pathlib import Path

try:
    import fcntl
except ImportError:  # Windows: no advisory locks; concurrent runs fall back to SQLite's own locking
    fcntl = None


@contextmanager
def index_lock(db_path: Path, wait: bool = True, timeout: float = 900.0):
    """Yield True when this process may index the store, False when another holds it and `wait` is False
    (or `timeout` seconds passed)."""
    if fcntl is None:
        yield True
        return
    db_path.parent.mkdir(parents=True, exist_ok=True)
    fh = open(db_path.with_name(db_path.name + ".lock"), "a+")  # noqa: SIM115 - held for the whole block
    try:
        deadline = time.monotonic() + timeout
        while True:
            try:
                fcntl.flock(fh, fcntl.LOCK_EX | fcntl.LOCK_NB)
                break
            except BlockingIOError:
                if not wait or time.monotonic() >= deadline:
                    yield False
                    return
                time.sleep(0.2)
        try:
            yield True
        finally:
            fcntl.flock(fh, fcntl.LOCK_UN)
    finally:
        fh.close()
