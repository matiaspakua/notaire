#!/usr/bin/env python3
"""Patch the installed oMLX for gpt-oss tool calls (#1099). Idempotent; an oMLX update undoes it.

  patch_omlx.py [--check | --restore] [OMLX_PACKAGE_DIR]

OMLX_PACKAGE_DIR defaults to the app bundle's `omlx` package. Patches replace
exact oMLX 0.7.0 snippets of `adapter/harmony.py`; a snippet that is gone means
upstream changed the code, so that patch is skipped, never applied blindly.
The first change keeps `adapter/harmony.py.orig`; --restore puts it back.
Exit status: 0 when every patch is applied (or already was), 1 otherwise.
"""
import os
import shutil
import sys

DEFAULT_DIR = "/Applications/oMLX.app/Contents/Resources/omlx"
HERE = os.path.dirname(os.path.abspath(__file__))
REPAIR_MODULE = "notaire_harmony_repair.py"

PATCHES = [
    # the completion is prefixed with <|start|>assistant, and role=ASSISTANT makes the parser read
    # that header a second time: a completion that opens with a tool call fails and the call is lost
    ("harmony-header",
     """        messages = encoding.parse_messages_from_completion_tokens(
            full_token_ids,
            role=Role.ASSISTANT,
            strict=False,
        )""",
     """        has_header = full_token_ids[: len(start_tokens)] == start_tokens
        messages = encoding.parse_messages_from_completion_tokens(
            full_token_ids,
            role=None if has_header else Role.ASSISTANT,
            strict=False,
        )"""),
    ("repair-import",
     "import json\nimport logging\n",
     "import json\nimport logging\n\nfrom .notaire_harmony_repair import recovery_call, repair_tool_call, tool_call_lost\n"),
    ("repair-parsed-call",
     """                name = msg.recipient[10:]  # Remove "functions." prefix
                tool_calls.append(
                    {"name": name, "arguments": _message_content_text(msg)}
                )""",
     """                name, arguments = repair_tool_call(msg.recipient[10:], _message_content_text(msg))
                tool_calls.append({"name": name, "arguments": arguments})"""),
    # an analysis-channel call counts only when its JSON parses: check the repaired arguments
    ("repair-analysis-check",
     """    try:
        return isinstance(json.loads(_message_content_text(msg)), dict)
    except ValueError:
        return False""",
     """    _, arguments = repair_tool_call(recipient[10:], _message_content_text(msg))
    try:
        return isinstance(json.loads(arguments), dict)
    except ValueError:
        return False"""),
    # a lost call ends the Codex turn empty; a recovery call tells the model and keeps the turn going.
    # Not an exception: this runs inside the scheduler step shared by every request.
    ("recover-lost-call",
     """        return output_text, analysis_text, tool_calls

    except Exception as e:
        logger.warning(f"Error parsing tool calls from tokens: {e}")
        return "", "", []""",
     """        if tool_call_lost(decoded_text, tool_calls, output_text):
            tool_calls = [recovery_call()]
        return output_text, analysis_text, tool_calls

    except Exception as e:
        logger.warning(f"Error parsing tool calls from tokens: {e}")
        if tool_call_lost(locals().get("decoded_text", ""), [], ""):
            return "", "", [recovery_call()]
        return "", "", []"""),
    ("repair-streamed-call",
     """\n                tool_calls.append({"name": name, "arguments": content})\n""",
     """\n                name, content = repair_tool_call(name, content)
                tool_calls.append({"name": name, "arguments": content})\n"""),
]


def _paths(omlx_dir):
    adapter = os.path.join(omlx_dir, "adapter")
    return os.path.join(adapter, "harmony.py"), os.path.join(adapter, REPAIR_MODULE)


def _status(text, old, new):
    if new in text:
        return "already"
    return "pending" if text.count(old) == 1 else "skipped"


def check(omlx_dir):
    harmony, repair = _paths(omlx_dir)
    text = open(harmony).read()
    report = {name: _status(text, old, new) for name, old, new in PATCHES}
    source = os.path.join(HERE, "harmony_repair.py")
    current = os.path.exists(repair) and open(repair).read() == open(source).read()
    report["repair-module"] = "already" if current else "pending"
    return report


def apply(omlx_dir):
    """Rebuild harmony.py from its pristine copy, so a changed patch set upgrades an old patch cleanly."""
    harmony, repair = _paths(omlx_dir)
    if not os.path.exists(harmony + ".orig"):
        shutil.copy2(harmony, harmony + ".orig")
    current, text = open(harmony).read(), open(harmony + ".orig").read()
    found = {}
    for name, old, new in PATCHES:
        found[name] = text.count(old) == 1
        if found[name]:
            text = text.replace(old, new)
    changed = text != current
    if changed:
        with open(harmony, "w") as f:
            f.write(text)
    report = {name: ("applied" if changed else "already") if ok else "skipped" for name, ok in found.items()}
    report["repair-module"] = "skipped"
    if report["repair-import"] in ("applied", "already"):
        report["repair-module"] = _sync(os.path.join(HERE, "harmony_repair.py"), repair)
    return report


def _sync(source, target):
    """Copy SOURCE over TARGET; 'applied' when the content changed, so the caller restarts oMLX."""
    if os.path.exists(target) and open(target).read() == open(source).read():
        return "already"
    shutil.copy2(source, target)
    return "applied"


def restore(omlx_dir):
    harmony, repair = _paths(omlx_dir)
    if os.path.exists(harmony + ".orig"):
        shutil.move(harmony + ".orig", harmony)
    if os.path.exists(repair):
        os.remove(repair)


def main(argv):
    flags = [a for a in argv if a.startswith("--")]
    dirs = [a for a in argv if not a.startswith("--")]
    omlx_dir = dirs[0] if dirs else DEFAULT_DIR
    if "--restore" in flags:
        restore(omlx_dir)
        print(f"restored {omlx_dir}/adapter/harmony.py")
        return 0
    report = check(omlx_dir) if "--check" in flags else apply(omlx_dir)
    for name, status in report.items():
        print(f"  {name}: {status}")
    return 0 if set(report.values()) <= {"applied", "already"} else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
