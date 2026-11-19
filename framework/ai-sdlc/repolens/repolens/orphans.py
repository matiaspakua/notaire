"""Files and symbols that relate to nothing: candidates for a person to review (unused, misplaced or missing a link).

Two files are related when an edge joins a node of one to a node of the other (calls, imports, mentions,
tests...), or when both touch the same route, table or issue tag. Structure is not a relation: a file contains
its symbols and sits in a directory whatever it is used for. A shared library import is not one either: every
file importing a framework would otherwise relate to every other. Git history (co-change, the issues a commit
names) is a weaker relation, reported apart: such a file changes with others but nothing in the content names it.

Computed on demand from the engine's adjacency arrays; nothing is stored.
"""

from __future__ import annotations

import numpy as np

from .model import SYMBOL_KINDS, is_constructor

STRUCTURE = frozenset({"contains", "dir_contains"})
HISTORY = frozenset({"co_change", "changed_for"})
NOT_HUBS = frozenset({"external", "project", "dir"})  # shared by too much to relate anything
REVIEWED_SYMBOLS = SYMBOL_KINDS - {"variable"}


def find(engine, symbols: bool = False, include_demoted: bool = False) -> dict:
    """Orphan files (`isolated`), files related only through git history (`history_only`), the counts left out
    under demoted paths, and with `symbols` the symbols with no relation at all."""
    n = len(engine.kind)
    files = np.array([i for i in range(n) if engine.kind[i] == "file"], dtype=np.int64)
    file_of = _file_of(engine, files, n)
    src = np.repeat(np.arange(n, dtype=np.int64), np.diff(engine.out_ptr))
    dst = np.asarray(engine.out_nbr, dtype=np.int64)
    kinds = np.asarray(engine.out_kind)
    codes = {name: code for code, name in enumerate(engine.kind_names)}
    keep = ~np.isin(kinds, [codes[k] for k in STRUCTURE if k in codes])
    dir_files = _dir_files(engine, file_of, src[~keep], dst[~keep], kinds[~keep], codes.get("dir_contains"))
    src, dst, kinds = src[keep], dst[keep], kinds[keep]
    history = np.isin(kinds, [codes[k] for k in HISTORY if k in codes])
    content, through_history = _related(engine, file_of, src, dst, history)
    for f, d in zip(file_of[src].tolist(), dst.tolist(), strict=True):  # text naming a directory
        if f >= 0 and d in dir_files and dir_files[d] - {f}:
            content.add(f)
            content.update(dir_files[d])
    boost = engine.boost
    isolated = [int(f) for f in files if f not in content and f not in through_history]
    history_only = [int(f) for f in files if f not in content and f in through_history]
    result = {
        "files": int(len(files)),
        "isolated": [_entry(engine, f) for f in isolated if include_demoted or boost[f] >= 1.0],
        "history_only": [_entry(engine, f) for f in history_only if include_demoted or boost[f] >= 1.0],
        "demoted": {"isolated": 0 if include_demoted else sum(boost[f] < 1.0 for f in isolated),
                    "history_only": 0 if include_demoted else sum(boost[f] < 1.0 for f in history_only)},
    }
    result["demoted"] = {k: int(v) for k, v in result["demoted"].items()}
    if symbols:
        degree = np.bincount(src, minlength=n) + np.bincount(dst, minlength=n)
        result["symbols"] = [
            {**_entry(engine, i), "name": engine.name[i], "qualname": engine.qual[i]}
            for i in range(n)
            if degree[i] == 0 and engine.kind[i] in REVIEWED_SYMBOLS and not is_constructor(engine.qual[i])
            and (include_demoted or boost[i] >= 1.0)
        ]
    return result


def counts(engine) -> dict:
    found = find(engine)
    return {"isolated": len(found["isolated"]), "history_only": len(found["history_only"]), **{
        f"demoted_{k}": v for k, v in found["demoted"].items()}}


def _file_of(engine, files: np.ndarray, n: int) -> np.ndarray:
    index = {engine.path[f]: f for f in files}
    return np.array([index.get(engine.path[i], -1) if engine.path[i] else -1 for i in range(n)], dtype=np.int64)


def _dir_files(engine, file_of, src, dst, kinds, dir_contains) -> dict[int, set[int]]:
    """Directory node -> the files directly in it."""
    out: dict[int, set[int]] = {}
    if dir_contains is None:
        return out
    mask = (kinds == dir_contains) & (file_of[dst] >= 0)
    for d, f in zip(src[mask].tolist(), file_of[dst][mask].tolist(), strict=True):
        if engine.kind[d] == "dir":
            out.setdefault(d, set()).add(f)
    return out


def _related(engine, file_of: np.ndarray, src: np.ndarray, dst: np.ndarray, history: np.ndarray):
    """(files related through content, files related through history), each a set of file indexes."""
    fa, fb = file_of[src], file_of[dst]
    content: set[int] = set()
    through_history: set[int] = set()
    direct = (fa >= 0) & (fb >= 0) & (fa != fb)
    for mask, bucket in ((direct & ~history, content), (direct & history, through_history)):
        bucket.update(fa[mask].tolist())
        bucket.update(fb[mask].tolist())
    # A virtual node (route, table, tag) relates the files that touch it, when there are at least two.
    hub = np.where(fa < 0, src, dst)
    member = np.where(fa < 0, fb, fa)
    via_hub = ((fa < 0) ^ (fb < 0)) & (member >= 0)
    hubs: dict[int, list[tuple[int, bool]]] = {}
    for h, f, hist in zip(hub[via_hub].tolist(), member[via_hub].tolist(), history[via_hub].tolist(), strict=True):
        if engine.kind[h] not in NOT_HUBS:
            hubs.setdefault(h, []).append((f, hist))
    for members in hubs.values():
        if len({f for f, _h in members}) >= 2:
            for f, hist in members:
                (through_history if hist else content).add(f)
    return content, through_history


def _entry(engine, i: int) -> dict:
    return {"id": engine.ids[i], "path": engine.path[i], "kind": engine.kind[i], "lang": engine.lang[i],
            "line": engine.start[i], "end": engine.end[i]}
