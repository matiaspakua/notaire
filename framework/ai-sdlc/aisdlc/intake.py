"""Issue intake lint (#2257): the issue text an agent starts from is checked before it starts.

Reads the issue through the profile's `[tracker] fetch` command (or `--file`) and lints it. Every
check is `pass`, `fail` or `no-evaluable`; a check that cannot run is never a pass (axiom 3).
Exit codes: 0 all pass, 1 any fail, 3 the issue could not be evaluated.
"""
from __future__ import annotations

import json
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Callable

from aisdlc.context import Context, run_shell

DEFAULT_SECTIONS = ("Use Case", "Acceptance criteria", "Out of scope", "Test plan", "Size")
DEFAULT_LABEL_PREFIXES = ("area:", "type:")
DEFAULT_PLACEHOLDERS = ("#TBD", "TBD", "TODO", "XXX")
DEFAULT_MAX_LINKS = 20
NO_EVALUABLE_EXIT = 3
HEADING = re.compile(r"^#{1,6}\s+(.+?)\s*#*\s*$")
CHECKBOX = re.compile(r"^\s*[-*]\s+\[[ xX]\]\s+\S", re.M)
LINK = re.compile(r"(?<![\w/])#(\d+)\b")
COMMENT = re.compile(r"<!--.*?-->", re.DOTALL)


@dataclass(frozen=True)
class IntakeConfig:
    sections: tuple[str, ...] = DEFAULT_SECTIONS
    label_prefixes: tuple[str, ...] = DEFAULT_LABEL_PREFIXES
    placeholders: tuple[str, ...] = DEFAULT_PLACEHOLDERS
    require_checkbox: bool = True
    max_links: int = DEFAULT_MAX_LINKS


Resolver = Callable[[int], bool]


def config_from(ctx: Context) -> IntakeConfig:
    table = ctx.profile.intake
    return IntakeConfig(
        sections=tuple(table.get("sections", DEFAULT_SECTIONS)),
        label_prefixes=tuple(table.get("label_prefixes", DEFAULT_LABEL_PREFIXES)),
        placeholders=tuple(table.get("placeholders", DEFAULT_PLACEHOLDERS)),
        require_checkbox=table.get("require_checkbox", True),
        max_links=table.get("max_links", DEFAULT_MAX_LINKS),
    )


def _row(name: str, status: str, detail: str = "") -> dict:
    return {"name": name, "status": status, "detail": detail}


def _sections(body: str) -> list[tuple[str, str]]:
    """Heading text and the content under it, comments removed."""
    found: list[tuple[str, str]] = []
    for line in COMMENT.sub("", body).splitlines():
        heading = HEADING.match(line)
        if heading:
            found.append((heading.group(1).strip().lower(), ""))
        elif found:
            found[-1] = (found[-1][0], found[-1][1] + line + "\n")
    return found


def _label_names(labels) -> list[str]:
    names = []
    for label in labels or []:
        name = label.get("name") if isinstance(label, dict) else label
        if isinstance(name, str):
            names.append(name)
    return names


def lint(issue: dict, config: IntakeConfig, resolver: Resolver | None) -> list[dict]:
    body = issue.get("body") or ""
    sections = _sections(body)
    results = []
    for required in config.sections:
        wanted = required.lower()
        contents = [text for heading, text in sections if heading.startswith(wanted)]
        if not contents:
            results.append(_row(f"section:{wanted}", "fail", f"missing section '{required}'"))
        elif not any(text.strip() for text in contents):
            results.append(_row(f"section:{wanted}", "fail", f"section '{required}' is empty"))
        else:
            results.append(_row(f"section:{wanted}", "pass"))
    if config.require_checkbox:
        found = bool(CHECKBOX.search(body))
        results.append(_row("checkbox", "pass" if found else "fail", "" if found else "no acceptance checkbox"))
    hits = [word for word in config.placeholders if re.search(rf"(?<![\w]){re.escape(word)}(?![\w])", body)]
    results.append(_row("placeholder", "fail" if hits else "pass", ", ".join(hits)))
    results.append(_links(body, config, resolver))
    names = _label_names(issue.get("labels"))
    for prefix in config.label_prefixes:
        found = any(name.startswith(prefix) for name in names)
        results.append(_row(f"label:{prefix}", "pass" if found else "fail", "" if found else f"no '{prefix}' label"))
    return results


def _links(body: str, config: IntakeConfig, resolver: Resolver | None) -> dict:
    numbers = sorted({int(n) for n in LINK.findall(COMMENT.sub("", body))})[: config.max_links]
    if not numbers:
        return _row("links", "pass", "no links")
    if resolver is None:
        return _row("links", "no-evaluable", "no tracker verify command to resolve links")
    dangling = [f"#{n}" for n in numbers if not resolver(n)]
    return _row("links", "fail" if dangling else "pass", ", ".join(dangling))


def overall(results: list[dict]) -> str:
    statuses = {row["status"] for row in results}
    if "fail" in statuses:
        return "fail"
    return "no-evaluable" if "no-evaluable" in statuses else "pass"


def _load(ctx: Context, issue: str, file: str | None) -> tuple[dict | None, str]:
    if file:
        try:
            return json.loads(Path(file).read_text(encoding="utf-8")), ""
        except (OSError, ValueError) as exc:
            return None, f"cannot read {file}: {exc}"
    if not ctx.profile.fetch:
        return None, "no [tracker] fetch command in the profile and no --file"
    outcome = run_shell(ctx.expand(ctx.profile.fetch, {"issue": issue}), ctx.root, timeout=60)
    if outcome.exit != 0:
        return None, f"fetch failed (exit {outcome.exit})"
    try:
        data = json.loads("\n".join(outcome.tail))
    except ValueError:
        return None, "fetch output is not one JSON object"
    return (data, "") if isinstance(data, dict) else (None, "fetch output is not a JSON object")


def _resolver(ctx: Context) -> Resolver | None:
    if not ctx.profile.verify:
        return None

    def resolve(number: int) -> bool:
        return run_shell(ctx.expand(ctx.profile.verify, {"issue": str(number)}), ctx.root, timeout=60).exit == 0

    return resolve


def check(ctx: Context, issue: str, file: str | None, as_json: bool) -> int:
    data, reason = _load(ctx, issue, file)
    if data is None:
        payload = {"issue": issue, "status": "no-evaluable", "reason": reason, "checks": []}
        print(json.dumps(payload, indent=2) if as_json else f"intake #{issue}: no-evaluable — {reason}")
        return NO_EVALUABLE_EXIT
    results = lint(data, config_from(ctx), _resolver(ctx))
    status = overall(results)
    if as_json:
        print(json.dumps({"issue": issue, "status": status, "checks": results}, indent=2, sort_keys=True))
    else:
        for row in results:
            detail = f" — {row['detail']}" if row["detail"] else ""
            print(f"  {row['status'].upper():<13} {row['name']}{detail}")
        print(f"intake #{issue}: {status}")
    return {"pass": 0, "fail": 1}.get(status, NO_EVALUABLE_EXIT)
