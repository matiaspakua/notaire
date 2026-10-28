"""Built-in change records: one Markdown file per change, with the profile's required sections and
an issue reference that the tracker must be able to resolve (an invented number fails Gate 1).

Projects that use a spec engine (OpenSpec, Spec Kit, ...) keep it and call its validator from a
`plan` gate instead.
"""
from __future__ import annotations

import re
from pathlib import Path

from aisdlc.context import Context, run_shell

NAME = re.compile(r"^[a-z0-9][a-z0-9-]*$")
COMMENT = re.compile(r"<!--.*?-->", re.S)
HINTS = {
    "Why": "The problem and who has it. Link the issue; do not restate it.",
    "What changes": "The behaviour that changes, in a few bullets. Nothing else changes.",
    "Acceptance scenarios": "WHEN … THEN … scenarios. Each one becomes at least one test.",
    "Test plan": "Which tests prove each scenario, at which level, and that they failed first.",
    "Traceability": "Issue → scenarios → tests → commits → PR → release evidence.",
}


def new(ctx: Context, name: str, issue: str, title: str | None) -> int:
    if not NAME.match(name):
        print(f"error: change name {name!r} must be kebab-case")
        return 2
    path = ctx.path(ctx.profile.spec_dir) / f"{name}.md"
    if path.exists():
        print(f"error: {path.relative_to(ctx.root)} already exists")
        return 2
    path.parent.mkdir(parents=True, exist_ok=True)
    body = [f"# {title or name}", "", f"Issue: #{issue}", ""]
    for section in ctx.profile.sections:
        body += [f"## {section}", "", f"<!-- {HINTS.get(section, 'Fill in this section.')} -->", ""]
    path.write_text("\n".join(body), encoding="utf-8")
    print(f"created {path.relative_to(ctx.root)}")
    return 0


def _records(ctx: Context, names: list[str]) -> list[Path]:
    folder = ctx.path(ctx.profile.spec_dir)
    if names:
        return [folder / f"{n}.md" for n in names]
    if not folder.is_dir():
        return []
    return sorted(p for p in folder.glob("*.md") if p.name.lower() != "readme.md")


def check(ctx: Context, names: list[str]) -> int:
    paths = _records(ctx, names)
    if not paths:
        print("no change records to check")
        return 0
    errors = []
    for path in paths:
        errors += [f"{path.relative_to(ctx.root)}: {e}" for e in _problems(ctx, path)]
    for error in errors:
        print(f"  FAIL  {error}")
    print(f"changes: {len(paths)} checked — {'FAILED' if errors else 'PASSED'}")
    return 1 if errors else 0


def _problems(ctx: Context, path: Path) -> list[str]:
    if not path.is_file():
        return ["does not exist"]
    text = path.read_text(encoding="utf-8")
    sections = _sections(text)
    problems = []
    for required in ctx.profile.sections:
        content = sections.get(required.lower())
        if content is None:
            problems.append(f"missing section '## {required}'")
        elif not COMMENT.sub("", content).strip():
            problems.append(f"section '{required}' is still empty (only template comments)")
    match = re.search(ctx.profile.issue_pattern, COMMENT.sub("", text))
    if not match:
        problems.append(f"no issue reference matching {ctx.profile.issue_pattern}")
    elif ctx.profile.verify:
        issue = match.group(1) if match.groups() else match.group(0)
        outcome = run_shell(ctx.expand(ctx.profile.verify, {"issue": issue}), ctx.root, timeout=60)
        if outcome.exit != 0:
            problems.append(f"issue {issue} could not be verified by the tracker (exit {outcome.exit})")
    return problems


def _sections(text: str) -> dict[str, str]:
    sections: dict[str, str] = {}
    current = None
    for line in text.splitlines():
        heading = re.match(r"^##\s+(.+?)\s*$", line)
        if heading:
            current = heading.group(1).lower()
            sections[current] = ""
        elif current is not None:
            sections[current] += line + "\n"
    return sections
