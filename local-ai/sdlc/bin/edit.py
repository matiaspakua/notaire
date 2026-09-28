#!/usr/bin/env python3
"""Exact-once search/replace for the worker (Codex apply_patch does not work with the local model).

usage: python3 edit.py FILE <<'EOF'
<<<<<<< OLD
exact existing lines
=======
replacement lines (empty = delete)
>>>>>>> NEW
EOF
Several OLD/NEW blocks may follow each other. Fails, changing nothing, unless every OLD text occurs
exactly once — so a repeated run can never delete more than intended.
"""
import re
import sys

MARKER = re.compile(r"^(<{7}|={7}|>{7})", re.M)
BLOCK = re.compile(r"^<<<<<<< OLD\n(.*?)^=======\n(.*?)^>>>>>>> NEW\n?", re.S | re.M)


def main():
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    path, spec = sys.argv[1], sys.stdin.read()
    blocks = BLOCK.findall(spec)
    if not blocks:
        sys.exit("edit.py: no '<<<<<<< OLD / ======= / >>>>>>> NEW' block on stdin")
    text = open(path, encoding="utf-8").read()
    for old, new in blocks:
        if MARKER.search(old) or MARKER.search(new):
            sys.exit("edit.py: a block contains a stray marker line (<<<<<<<, =======, >>>>>>>) — nothing changed. "
                     "Each block is exactly: <<<<<<< OLD, old lines, =======, new lines, >>>>>>> NEW.")
        count = text.count(old)
        if count != 1:
            sys.exit("edit.py: OLD found %d times in %s (must be exactly 1) — nothing changed:\n%s" % (count, path, old))
        text = text.replace(old, new)
    open(path, "w", encoding="utf-8").write(text)
    print("edit.py: %s — %d block(s) applied" % (path, len(blocks)))


main()
