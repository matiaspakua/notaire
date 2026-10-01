#!/usr/bin/env python3
"""Read the project adapter (.aisdlc/project.yml), the harness's only project-specific input.

  adapter.py [--file F] validate                            exit 2 listing every required key the adapter lacks
  adapter.py [--file F] get <dotted.key> [name=value ...]   value with {name} filled; lists one item per line
  adapter.py [--file F] surfaces [--fallback S]             file paths on stdin -> "backend,frontend" | S | "none"
  adapter.py [--file F] suite <surfaces>                    full-suite command for those surfaces
  adapter.py [--file F] check-test-cmd <surfaces> <cmd>     exit 1 + expected form if cmd is not a test_one command
"""
import os
import sys

import yaml

DEFAULT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "..", ".aisdlc", "project.yml")
# every key foreman.sh reads: a self-test checks the real adapter has them all
REQUIRED = (
    "paths.worktree", "paths.runs", "backend.profile", "backend.agent", "backend.opencode_model", "spec.schema", "spec.tasks_template", "spec.validate",
    "spec.plan_check", "surfaces", "source_roots", "test_files", "db_migrations", "gates.docs_lint",
    "gates.docs_lint_fix", "gates.preflight", "gates.pipeline", "gates.start", "gates.health_url", "gates.main_workflows",
    "compose_project", "guards.forbidden",
)


def load(path):
    with open(path) as f:
        return yaml.safe_load(f) or {}


def _lookup(cfg, key):
    node = cfg
    for part in key.split("."):
        if not isinstance(node, dict) or part not in node:
            raise KeyError(key)
        node = node[part]
    return node


def missing_keys(cfg):
    missing = []
    for key in REQUIRED:
        try:
            _lookup(cfg, key)
        except KeyError:
            missing.append(key)
    return missing


def _fill(text, subs):
    for name, value in (subs or {}).items():
        text = text.replace("{%s}" % name, value)
    return text


def get(cfg, key, subs=None):
    value = _lookup(cfg, key)
    if isinstance(value, list):
        return "\n".join(_fill(str(v), subs) for v in value)
    return _fill(str(value), subs)


def _named(cfg, names):
    all_surfaces = cfg["surfaces"]
    wanted = list(all_surfaces) if names == "both" else [n for n in names.split(",") if n and n != "none"]
    return [all_surfaces[n] for n in wanted]


def surfaces(cfg, paths, fallback=None):
    """Surfaces whose root holds a path; else fallback (triage's TEST_SURFACE) if the adapter defines it."""
    hit = [n for n, s in cfg["surfaces"].items() if any(p.startswith(s["root"]) for p in paths)]
    if not hit and fallback in cfg["surfaces"]:
        hit = [fallback]
    return ",".join(hit) or "none"


def suite(cfg, names):
    cmds = ["(%s)" % s["suite"] for s in _named(cfg, names)]
    return " && ".join(cmds) or "true"


def check_test_cmd(cfg, names, cmd):
    """None when cmd starts with a surface's test_one prefix, else a message with the expected forms."""
    forms = [s["test_one"] for s in _named(cfg, names)]
    if any(cmd.startswith(f.split("{test}")[0]) for f in forms):
        return None
    expected = "\n".join("  TEST_CMD=" + f.replace("{test}", "<TestClass>") for f in forms)
    return "TEST_CMD must use the single-test command of the change's surface (%s):\n%s\nGot: %s" % (names, expected, cmd)


def main(argv):
    path = DEFAULT
    if argv[:1] == ["--file"]:
        path, argv = argv[1], argv[2:]
    cfg = load(path)
    cmd, args = argv[0], argv[1:]
    try:
        if cmd == "validate":
            missing = missing_keys(cfg)
            if missing:
                print("adapter %s lacks: %s" % (os.path.abspath(path), ", ".join(missing)), file=sys.stderr)
                return 2
        elif cmd == "get":
            subs = dict(a.split("=", 1) for a in args[1:])
            print(get(cfg, args[0], subs))
        elif cmd == "surfaces":
            fallback = args[1] if args[:1] == ["--fallback"] else None
            print(surfaces(cfg, [line.strip() for line in sys.stdin if line.strip()], fallback))
        elif cmd == "suite":
            print(suite(cfg, args[0]))
        elif cmd == "check-test-cmd":
            err = check_test_cmd(cfg, args[0], args[1])
            if err:
                print(err)
                return 1
        else:
            print("unknown command: %s" % cmd, file=sys.stderr)
            return 2
    except KeyError as e:
        print("adapter: missing key %s in %s" % (e.args[0], os.path.abspath(path)), file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
