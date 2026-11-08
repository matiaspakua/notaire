"""Lint the skills catalog: every skill is present, described and uniquely named, and the skills a
project calls generic carry none of its project-specific patterns."""
from __future__ import annotations

import re
from pathlib import Path

from aisdlc.context import Context

KEY = re.compile(r"^([A-Za-z_][\w-]*):\s*(.*)$")
MAX_SCANNED_BYTES = 1_000_000


def frontmatter(text: str) -> dict[str, str]:
    """Top-level `key: value` pairs of a leading `---` block; folded/indented values are joined."""
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        return {}
    data: dict[str, str] = {}
    key = None
    for line in lines[1:]:
        if line.strip() == "---":
            break
        match = KEY.match(line)
        if match:
            key = match.group(1)
            value = match.group(2).strip()
            data[key] = "" if value in (">", "|", ">-", "|-") else value.strip("'\"")
        elif key and line.startswith((" ", "\t")):
            data[key] = (data[key] + " " + line.strip()).strip()
    return data


def lint(ctx: Context) -> int:
    if ctx.profile.skills_dir is None:
        print("no skills catalog configured ([skills] dir)")
        return 0
    catalog = ctx.path(ctx.profile.skills_dir)
    if not catalog.is_dir():
        print(f"error: skills dir {ctx.profile.skills_dir} does not exist")
        return 1
    errors: list[str] = []
    names: dict[str, list[str]] = {}
    folders = sorted(p for p in catalog.iterdir() if p.is_dir() and not p.name.startswith("."))
    for folder in folders:
        rel = f"{ctx.profile.skills_dir.rstrip('/')}/{folder.name}"
        skill_md = folder / "SKILL.md"
        if not skill_md.is_file():
            errors.append(f"{rel}: no SKILL.md")
            continue
        meta = frontmatter(skill_md.read_text(encoding="utf-8"))
        if not meta.get("description"):
            errors.append(f"{rel}/SKILL.md: frontmatter has no description (agents select skills by it)")
        names.setdefault(meta.get("name") or folder.name, []).append(rel)
    for name, owners in sorted(names.items()):
        if len(owners) > 1:
            errors.append(f"skill name {name!r} is declared by {' and '.join(owners)}")
    errors += _generic_leaks(ctx, catalog)
    for error in errors:
        print(f"  FAIL  {error}")
    print(f"skills: {len(folders)} in catalog, {len(ctx.profile.skills_generic)} generic — "
          f"{'FAILED' if errors else 'PASSED'}")
    return 1 if errors else 0


def _generic_leaks(ctx: Context, catalog: Path) -> list[str]:
    patterns = [re.compile(p, re.I) for p in ctx.profile.skills_forbid]
    errors = []
    for name in ctx.profile.skills_generic:
        folder = catalog / name
        if not (folder / "SKILL.md").is_file():
            errors.append(f"generic skill {name!r} is listed but {folder.relative_to(ctx.root)}/SKILL.md is missing")
            continue
        for path in sorted(p for p in folder.rglob("*") if p.is_file() and p.stat().st_size <= MAX_SCANNED_BYTES):
            try:
                lines = path.read_text(encoding="utf-8").splitlines()
            except UnicodeDecodeError:
                continue
            for number, line in enumerate(lines, 1):
                hit = next((p.pattern for p in patterns if p.search(line)), None)
                if hit:
                    errors.append(f"{path.relative_to(ctx.root)}:{number}: generic skill mentions {hit!r}")
    return errors
