"""Repair a gpt-oss tool call before oMLX hands it to Codex.

gpt-oss was trained on Codex's classic tools: `shell` with an argv list and a
separate `apply_patch`. Codex 0.159 offers a local model only `exec_command`,
whose `cmd` is one string, so gpt-oss mixes the two shapes. Codex rejects the
call and ends the turn (#1099: 2 of 8 coding runs). Only shapes whose intent is
unambiguous are repaired; anything else passes through unchanged.

Copied into oMLX as `omlx/adapter/notaire_harmony_repair.py` by patch_omlx.py,
so it must stay standard-library only (oMLX bundles Python 3.11).
"""
import json
import os
import re
import shlex

SHELL_TOOL = "exec_command"
SHELLS = {"bash", "sh", "zsh"}
NAME = re.compile(r"[A-Za-z0-9_.\-]+")
STRAY_BRACKET = re.compile(r'"\s*\]\s*\}\s*$')
UNTERMINATED = re.compile(r'([^"\s])\s*\}\s*$')
INVALID_ESCAPE = re.compile(r'(?<!\\)((?:\\\\)*)\\(?=[^"\\/bfnrtu])')


def _script(argv):
    if len(argv) >= 3 and os.path.basename(argv[0]) in SHELLS and argv[1] in ("-c", "-lc"):
        return argv[2]
    return shlex.join(argv)


def _parse(arguments):
    args = _parse_strict(arguments)
    if args is not None:
        return args
    # a regex's \s written raw in the string: JSON allows only \" \\ \/ \b \f \n \r \t \uXXXX
    arguments = INVALID_ESCAPE.sub(r"\1\\\\", arguments)
    # a heredoc closed as if cmd were a list: {"cmd": "...EOF"]}, or a string never closed: {"cmd": "ls}
    return (_parse_strict(arguments) or _parse_strict(STRAY_BRACKET.sub('"}', arguments))
            or _parse_strict(UNTERMINATED.sub(r'\1"}', arguments)))


def _repair_arguments(arguments):
    args = _parse(arguments)
    if not isinstance(args, dict):
        return arguments
    changed = args != _parse_strict(arguments)
    if "cmd" not in args and "command" in args:
        args["cmd"] = args.pop("command")
        changed = True
    cmd = args.get("cmd")
    if isinstance(cmd, list) and all(isinstance(part, str) for part in cmd):
        args["cmd"] = _script(cmd)
        changed = True
    return json.dumps(args) if changed else arguments


def _parse_strict(arguments):
    try:
        return json.loads(arguments)
    except ValueError:
        return None


def repair_tool_call(name, arguments):
    """Return (name, arguments) with header tokens stripped from NAME and exec_command arguments repaired."""
    m = NAME.match(name or "")
    if m:
        name = m.group(0)
    if name == SHELL_TOOL and isinstance(arguments, str):
        arguments = _repair_arguments(arguments)
    return name, arguments
