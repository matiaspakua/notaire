#!/usr/bin/env python3
"""Read the harness KEY=value files (triage.env, tests.env).

One parser for foreman.sh's kv() and for the prompt renderer, so the two can
never disagree. A value loses a trailing ' # comment' and one pair of
surrounding quotes: the worker often writes TEST_CMD="mvn ...", and a quoted
value run through bash -c would be one command name (exit 127, #1063).

    envfile.py <file> <KEY>   print the value (empty if missing)
"""
import re
import shlex
import sys

LINE = re.compile(r"^([A-Z_][A-Z0-9_]*)=(.*)$")


def parse_value(raw):
    raw = raw.strip()
    if raw[:1] in ('"', "'"):
        try:
            words = shlex.split(raw, comments=True)
        except ValueError:
            words = []
        if len(words) == 1:
            return words[0]
    return re.sub(r"\s+#.*$", "", raw).strip()


def read_env(path):
    env = {}
    try:
        with open(path) as f:
            for line in f:
                m = LINE.match(line.strip())
                if m and m.group(1) not in env:
                    env[m.group(1)] = parse_value(m.group(2))
    except FileNotFoundError:
        pass
    return env


if __name__ == "__main__":
    print(read_env(sys.argv[1]).get(sys.argv[2], ""))
