#!/usr/bin/env python3
"""Deterministic OpenSpec ledger bookkeeping, run by the foreman (never by the worker).

  ledger.py tick  TASKS_MD ID...              flip `- [ ] ID ` to `- [x] ID `
  ledger.py row   TRACE_MD LABEL CELL...      replace the table row whose first cell is LABEL
  ledger.py ticks-only OLD_FILE NEW_FILE      exit 1 unless NEW differs from OLD only by [ ] -> [x]

Every command changes nothing and exits non-zero when its target is not found exactly once.
"""
import difflib
import re
import sys


def die(msg):
    sys.exit(f"ledger.py: {msg}")


def tick(path, ids):
    text = open(path).read()
    for task_id in ids:
        pattern = re.compile(rf"^(\s*- )\[[ x]\]( {re.escape(task_id)} )", re.M)
        if len(pattern.findall(text)) != 1:
            die(f"task {task_id} not found exactly once in {path}")
        text = pattern.sub(r"\1[x]\2", text)
    open(path, "w").write(text)


def row(path, label, cells):
    lines = open(path).read().split("\n")
    hits = [i for i, line in enumerate(lines) if re.match(rf"^\|\s*{re.escape(label)}\s*\|", line)]
    if len(hits) != 1:
        die(f"row '{label}' not found exactly once in {path}")
    lines[hits[0]] = "| " + " | ".join([label, *cells]) + " |"
    open(path, "w").write("\n".join(lines))


def normalize(text):
    """Untick every box and drop what markdown lint may legitimately change: blank lines, fence languages."""
    text = re.sub(r"^(\s*- )\[x\]", r"\1[ ]", text, flags=re.M)
    text = re.sub(r"^(\s*```)\w+\s*$", r"\1", text, flags=re.M)
    return [line for line in text.split("\n") if line.strip()]


def ticks_only(old_path, new_path):
    old, new = normalize(open(old_path).read()), normalize(open(new_path).read())
    if old == new:
        sys.exit(0)
    # name the offending lines: a bare "restore the file" also throws away legitimate edits
    print("\n".join(difflib.unified_diff(old, new, "expected (ticks aside)", "yours", n=0, lineterm="")))
    sys.exit(1)


if __name__ == "__main__":
    if len(sys.argv) < 4:
        die(__doc__)
    cmd, target, *args = sys.argv[1:]
    if cmd == "tick":
        tick(target, args)
    elif cmd == "row":
        row(target, args[0], args[1:])
    elif cmd == "ticks-only":
        ticks_only(target, args[0])
    else:
        die(f"unknown command {cmd}")
