#!/usr/bin/env python3
"""Orphans and content coverage on real repositories.

    python3 bench/orphan_bench.py --label NAME [--reindex] PROFILE...

Per profile (a store already indexed): files, orphans (isolated / only through git history / demoted), isolated
files by language, code files that yield at least one symbol, and a precision proxy for the orphan list: the
share of reported orphans that another tracked file names, with their parent directory or by a name no other
file shares (one pass over the tracked text files). Such a name is most often a link repolens missed, so
lower is better. `--reindex` re-extracts every file first and records the time (vectors come from the cache).
"""

from __future__ import annotations

import argparse
import json
import subprocess
import sys
import time
from collections import Counter
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))

from repolens import orphans  # noqa: E402
from repolens.extractors.common import _NOT_PATH  # noqa: E402
from repolens.service import KnowledgeGraph  # noqa: E402


def named_elsewhere(roots: dict[str, str], entries: list[dict]) -> int:
    """How many entries another tracked text file names: with their parent directory (`page-x/spec.md`), or by
    their bare name when no other tracked file shares it (`spec.md` or `1.json` alone name nothing in
    particular). One pass over the files; `git grep -F -f` with thousands of names timed out on vscode."""
    by_project: dict[str, list[str]] = {}
    for e in entries:
        by_project.setdefault(e["id"].split(":", 1)[0], []).append(e["path"])
    found = 0
    for project, paths in by_project.items():
        root = roots.get(project)
        if not root:
            continue
        listed = subprocess.run(["git", "-C", root, "ls-files", "-z"], capture_output=True, check=True).stdout
        tracked = [p for p in listed.decode("utf-8", "replace").split("\0") if p]
        base_count = Counter(p.rsplit("/", 1)[-1] for p in tracked)
        keys: dict[str, set[str]] = {}
        for path in paths:
            parts = path.split("/")
            if len(parts) >= 2:
                keys.setdefault("/".join(parts[-2:]), set()).add(path)
            if base_count[parts[-1]] == 1:
                keys.setdefault(parts[-1], set()).add(path)
        named: set[str] = set()
        for rel in tracked:
            text = _read_text(Path(root) / rel)
            if text is None:
                continue
            for token in set(_NOT_PATH.split(text)):
                token = token.rstrip(".-")
                parts = token.split("/")
                for key in {"/".join(parts[-2:]), parts[-1]}:
                    named.update(keys.get(key, set()) - {rel})
        found += len(named)
    return found


def _read_text(path: Path) -> str | None:
    try:
        if not path.is_file() or path.stat().st_size > 2_000_000:
            return None
        data = path.read_bytes()
    except OSError:
        return None
    return None if b"\0" in data[:8192] else data.decode("utf-8", "replace")


def measure(profile: Path, reindex: bool) -> dict:
    kg = KnowledgeGraph.open(profile)
    row: dict = {"profile": profile.name, "projects": sorted(kg.engine.roots)}  # no local paths in results
    if reindex:
        clock = time.perf_counter()
        report = kg.index(full=True, log=lambda *_: None)
        row["reindex_s"] = round(time.perf_counter() - clock, 1)
        row["timings"] = report["timings"]
    eng = kg.engine
    found = orphans.find(eng)
    conn = eng.store.conn
    code = conn.execute("SELECT path, lang FROM nodes WHERE kind = 'file' AND signature = 'code'").fetchall()
    with_symbols = {r[0] for r in conn.execute(
        "SELECT DISTINCT path FROM nodes WHERE kind IN ('class', 'interface', 'enum', 'type', 'function', 'method')")}
    langs = Counter(r[0] for r in conn.execute("SELECT lang FROM nodes WHERE kind = 'file'"))
    iso = found["isolated"]
    row.update({
        "files": found["files"],
        "nodes": len(eng.kind),
        "edges": int(len(eng.out_nbr)),
        "isolated": len(iso),
        "history_only": len(found["history_only"]),
        "demoted": found["demoted"],
        "conventional": found["conventional"],
        "isolated_named_elsewhere": named_elsewhere(eng.roots, iso),
        "isolated_by_lang": dict(Counter(e["lang"] for e in iso).most_common(15)),
        "languages": len(langs),
        "code_files": len(code),
        "code_languages": len({lang for _p, lang in code}),
        "code_files_with_symbols": sum(path in with_symbols for path, _l in code),
        "languages_with_symbols": len({lang for path, lang in code if path in with_symbols}),
    })
    row["isolated_named_elsewhere_share"] = round(row["isolated_named_elsewhere"] / max(1, len(iso)), 3)
    kg.close()
    return row


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--label", required=True)
    ap.add_argument("--reindex", action="store_true")
    ap.add_argument("profiles", nargs="+", type=Path)
    args = ap.parse_args()
    result = {"label": args.label, "stamp": time.strftime("%Y-%m-%d %H:%M"),
              "stores": [measure(p.expanduser().resolve(), args.reindex) for p in args.profiles]}
    out = HERE / "results" / f"orphans-{args.label}.json"
    out.write_text(json.dumps(result, indent=1), encoding="utf-8")
    print(json.dumps(result, indent=1))
    return 0


if __name__ == "__main__":
    sys.exit(main())
