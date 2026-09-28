#!/usr/bin/env python3
"""Append one JSON line per gate result to metrics.jsonl (AUDIT.md H3).

gates.log stays the human-readable log; this is the machine-readable twin,
so pass rates per phase and gate can be counted instead of read by hand.

    metrics.py <metrics.jsonl> <issue> <gate> <detail>   detail is 'RESULT[ ...] :: text'
"""
import json
import sys
from datetime import datetime, timezone


def append(path, issue, gate, detail):
    row = {"ts": datetime.now(timezone.utc).isoformat(timespec="seconds"), "issue": issue, "gate": gate,
           "result": detail.split(" ", 1)[0], "detail": detail}
    with open(path, "a") as f:
        f.write(json.dumps(row) + "\n")


if __name__ == "__main__":
    append(*sys.argv[1:5])
