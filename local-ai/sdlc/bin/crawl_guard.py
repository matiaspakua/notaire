#!/usr/bin/env python3
"""grep/find shims for the worker's shell: recursive crawls skip dependency and build trees.

Linked as bin/shims/grep and bin/shims/find; runs the real tool with the tree pruned.
The worker ignores "use git grep" (#1049: 30 of 40 searches were find/grep -r), and a crawl
of node_modules/ fills the 32K window with minified code, after which the model loops.
"""
import os
import sys

SKIP = ("node_modules", "target", ".git", ".next")
FIND_ACTIONS = {"-print", "-print0", "-exec", "-execdir", "-ok", "-okdir", "-delete", "-ls",
                "-printf", "-fprint", "-quit"}
FIND_LEADING = {"-H", "-L", "-P", "-E", "-X", "-d", "-s", "-x", "-f"}


def is_recursive(arg):
    return arg in ("--recursive", "--dereference-recursive") or (
        arg.startswith("-") and not arg.startswith("--") and any(c in "rR" for c in arg[1:]))


def grep_args(args):
    if not any(is_recursive(a) for a in args):
        return list(args)
    return ["--exclude-dir=%s" % d for d in SKIP] + list(args)


def find_args(args):
    i = 0
    while i < len(args) and args[i] in FIND_LEADING:
        i += 1
    lead = list(args[:i])
    j = i
    while j < len(args) and not args[j].startswith("-") and args[j] not in ("(", "!"):
        j += 1
    paths, expr = list(args[i:j]) or ["."], list(args[j:])
    prune = ["("]
    for n, d in enumerate(SKIP):
        prune += (["-o"] if n else []) + ["-name", d]
    prune += [")", "-prune", "-o"]
    tail = ["(", *expr, ")"] if expr else []
    if not FIND_ACTIONS.intersection(expr):
        tail.append("-print")
    return lead + paths + prune + tail


def real_tool(name, here):
    for d in os.environ.get("PATH", "").split(os.pathsep):
        p = os.path.join(d, name)
        if os.path.realpath(d) != here and os.access(p, os.X_OK):
            return p
    sys.exit("crawl_guard: no real %s on PATH" % name)


def main(argv):
    name = os.path.basename(argv[0])
    rewrite = {"grep": grep_args, "find": find_args}[name]
    tool = real_tool(name, os.path.realpath(os.path.dirname(os.path.abspath(argv[0]))))
    os.execv(tool, [name] + rewrite(argv[1:]))


if __name__ == "__main__":
    main(sys.argv)
