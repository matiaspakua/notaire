#!/usr/bin/env python3
"""Repair the Markdown lint errors `markdownlint --fix` leaves: md_repair.py FILE...

  MD040  a bare opening code fence gets the language `text` (a closing fence stays bare)
  MD055  a table row without its trailing pipe gets one

The #1049 gpt-oss spec failed its last attempt on exactly these two.
"""
import re
import sys

FENCE = re.compile(r"^(\s*)(`{3,}|~{3,})(.*)$")


def repair(text):
    lines = text.split("\n")
    fence = None
    for i, line in enumerate(lines):
        m = FENCE.match(line)
        if m:
            if fence is None:
                fence = m.group(2)
                if not m.group(3).strip():
                    lines[i] = f"{m.group(1)}{m.group(2)}text"
            elif m.group(2).startswith(fence) and not m.group(3).strip():
                fence = None
            continue
        if fence is None and line.lstrip().startswith("|") and not line.rstrip().endswith("|"):
            lines[i] = line.rstrip() + " |"
    return "\n".join(lines)


if __name__ == "__main__":
    for path in sys.argv[1:]:
        text = open(path).read()
        fixed = repair(text)
        if fixed != text:
            open(path, "w").write(fixed)
