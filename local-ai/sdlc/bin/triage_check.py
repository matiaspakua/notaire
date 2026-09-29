#!/usr/bin/env python3
"""Triage gate rules that parse the worker's files (called by foreman.sh gate_triage).

    triage_check.py restore <triage.env> <seed.env>   restore harness-derived keys; print the repaired ones
    triage_check.py bad-proofs < criteria             print criteria whose command proof only searches or prints
    triage_check.py kind-conflict <KIND> < criteria   exit 1 if a non-code KIND promises new tests
"""
import re
import sys

from envfile import LINE, read_env

# facts the harness knows (issue, its Use Case, the title's type): the worker may not change them
DERIVED = ("ISSUE", "USE_CASE", "TYPE")
# these succeed whether or not the criterion holds (#1064: 'command bash grep ...', 'git ls-tree ...')
SEARCH = re.compile(r"proven by: command (?:(?:bash|sh) )?(?:grep|egrep|rg|find|cat|head|tail|ls|wc|echo"
                    r"|sed -n|git (?:ls-tree|ls-files|log|show|grep))\b")
NEW_TEST = re.compile(r"proven by: new test ")


def restore(env_path, seed_path):
    """Put back every DERIVED key the seed gives a real value; return the keys that changed."""
    seed = {k: v for k, v in read_env(seed_path).items() if k in DERIVED and v not in ("", "?")}
    if not seed:
        return []   # no seed (runs from before #1095): nothing to restore
    with open(env_path) as f:
        lines = f.read().splitlines()
    repaired, seen = [], set()
    for i, line in enumerate(lines):
        m = LINE.match(line.strip())
        if not m or m.group(1) not in seed or m.group(1) in seen:
            continue
        seen.add(m.group(1))
        want = "%s=%s" % (m.group(1), seed[m.group(1)])
        if line.strip() != want:
            lines[i] = want
            repaired.append(m.group(1))
    for key in seed:
        if key not in seen:
            lines.append("%s=%s" % (key, seed[key]))
            repaired.append(key)
    with open(env_path, "w") as f:
        f.write("\n".join(lines) + "\n")
    return repaired


def bad_proofs(criteria):
    return [line for line in criteria.splitlines() if SEARCH.search(line)]


def kind_conflict(kind, criteria):
    """Only KIND=code has a tests phase: a new test promised under another KIND is never written."""
    return kind != "code" and bool(NEW_TEST.search(criteria))


def main(argv):
    cmd = argv[0]
    if cmd == "restore":
        print(" ".join(restore(argv[1], argv[2])))
    elif cmd == "bad-proofs":
        print("\n".join(bad_proofs(sys.stdin.read())))
    elif cmd == "kind-conflict":
        return 1 if kind_conflict(argv[1], sys.stdin.read()) else 0
    else:
        print("unknown command: %s" % cmd, file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
