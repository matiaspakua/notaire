"""Issue intake lint (#2257): the issue text an agent starts from is checked before it starts.

Reads the issue through the profile's `[tracker] fetch` command (or `--file`) and lints it. Every
check is `pass`, `fail` or `no-evaluable`; a check that cannot run is never a pass (axiom 3).
Exit codes: 0 all pass, 1 any fail, 3 the issue could not be evaluated.

Foreman claim refuse and the clean-board report live here too: enforcement is raise-only via
`[intake] required` after a clean board over open issues.
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
BOARD_NOTE = (
    "raise intake.required only after a clean report over the open board "
    "(raise-only; do not flip enforcement without evidence)"
)


@dataclass(frozen=True)
class IntakeConfig:
    sections: tuple[str, ...] = DEFAULT_SECTIONS
    label_prefixes: tuple[str, ...] = DEFAULT_LABEL_PREFIXES
    placeholders: tuple[str, ...] = DEFAULT_PLACEHOLDERS
    require_checkbox: bool = True
    max_links: int = DEFAULT_MAX_LINKS
    required: bool = False


Resolver = Callable[[int], bool]


def config_from(ctx: Context) -> IntakeConfig:
    table = ctx.profile.intake
    return IntakeConfig(
        sections=tuple(table.get("sections", DEFAULT_SECTIONS)),
        label_prefixes=tuple(table.get("label_prefixes", DEFAULT_LABEL_PREFIXES)),
        placeholders=tuple(table.get("placeholders", DEFAULT_PLACEHOLDERS)),
        require_checkbox=table.get("require_checkbox", True),
        max_links=table.get("max_links", DEFAULT_MAX_LINKS),
        required=bool(table.get("required", False)),
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


def evaluate(ctx: Context, issue: str, file: str | None) -> dict:
    """Return intake status payload without printing (shared by check / claim / board)."""
    config = config_from(ctx)
    data, reason = _load(ctx, issue, file)
    if data is None:
        return {
            "issue": issue,
            "status": "no-evaluable",
            "reason": reason,
            "checks": [],
            "required": config.required,
        }
    results = lint(data, config, _resolver(ctx))
    return {
        "issue": issue,
        "status": overall(results),
        "checks": results,
        "required": config.required,
    }


def claim_decision(status: str, required: bool) -> dict:
    """Foreman claim protocol: refuse only when enforced and status is not pass."""
    would_refuse = status != "pass"
    return {
        "intake_status": status,
        "required": required,
        "would_refuse": would_refuse,
        "dispatch_allowed": (not would_refuse) or (not required),
    }


def check(ctx: Context, issue: str, file: str | None, as_json: bool) -> int:
    payload = evaluate(ctx, issue, file)
    status = payload["status"]
    if status == "no-evaluable" and not payload.get("checks"):
        out = {
            "issue": issue,
            "status": "no-evaluable",
            "reason": payload.get("reason", ""),
            "checks": [],
        }
        print(json.dumps(out, indent=2) if as_json else f"intake #{issue}: no-evaluable — {out['reason']}")
        return NO_EVALUABLE_EXIT
    if as_json:
        print(json.dumps({"issue": issue, "status": status, "checks": payload.get("checks", [])}, indent=2, sort_keys=True))
    else:
        for row in payload.get("checks") or []:
            detail = f" — {row['detail']}" if row["detail"] else ""
            print(f"  {row['status'].upper():<13} {row['name']}{detail}")
        print(f"intake #{issue}: {status}")
    return {"pass": 0, "fail": 1}.get(status, NO_EVALUABLE_EXIT)


def _load_board(ctx: Context, file: str | None) -> tuple[list | None, str]:
    if file:
        try:
            data = json.loads(Path(file).read_text(encoding="utf-8"))
        except (OSError, ValueError) as exc:
            return None, f"cannot read {file}: {exc}"
        return (data, "") if isinstance(data, list) else (None, "board file is not a JSON array")
    if not ctx.profile.list:
        return None, "no [tracker] list command in the profile and no --file"
    outcome = run_shell(ctx.expand(ctx.profile.list, {}), ctx.root, timeout=120)
    if outcome.exit != 0:
        return None, f"list failed (exit {outcome.exit})"
    try:
        data = json.loads("\n".join(outcome.tail))
    except ValueError:
        return None, "list output is not a JSON array"
    return (data, "") if isinstance(data, list) else (None, "list output is not a JSON array")


def _issue_id(issue: dict, index: int) -> str:
    for key in ("number", "id"):
        value = issue.get(key)
        if value is not None:
            return str(value)
    return str(index + 1)


def board_report(issues: list, config: IntakeConfig, resolver: Resolver | None) -> dict:
    rows = []
    counts = {"pass": 0, "fail": 0, "no-evaluable": 0}
    for index, issue in enumerate(issues):
        if not isinstance(issue, dict):
            status = "no-evaluable"
            entry = {"issue": str(index + 1), "status": status, "reason": "board entry is not an object"}
        else:
            results = lint(issue, config, resolver)
            status = overall(results)
            entry = {"issue": _issue_id(issue, index), "status": status, "checks": results}
        counts[status] = counts.get(status, 0) + 1
        rows.append(entry)
    clean = bool(issues) and counts["fail"] == 0 and counts["no-evaluable"] == 0
    if not issues:
        clean = True
    if counts["fail"]:
        status = "fail"
    elif counts["no-evaluable"]:
        status = "no-evaluable"
    else:
        status = "pass"
    return {
        "status": status,
        "clean": clean,
        "required": config.required,
        "counts": counts,
        "issues": rows,
        "note": BOARD_NOTE,
    }


def board(ctx: Context, file: str | None, as_json: bool) -> int:
    config = config_from(ctx)
    issues, reason = _load_board(ctx, file)
    if issues is None:
        payload = {
            "status": "no-evaluable",
            "clean": False,
            "required": config.required,
            "counts": {"pass": 0, "fail": 0, "no-evaluable": 0},
            "issues": [],
            "reason": reason,
            "note": BOARD_NOTE,
        }
        if as_json:
            print(json.dumps(payload, indent=2, sort_keys=True))
        else:
            print(f"intake board: no-evaluable — {reason}")
        return NO_EVALUABLE_EXIT
    payload = board_report(issues, config, _resolver(ctx))
    if as_json:
        print(json.dumps(payload, indent=2, sort_keys=True))
    else:
        print(
            f"intake board: {payload['status']} clean={str(payload['clean']).lower()} "
            f"pass={payload['counts']['pass']} fail={payload['counts']['fail']} "
            f"no-evaluable={payload['counts']['no-evaluable']}"
        )
        print(f"  note: {payload['note']}")
        for row in payload["issues"]:
            print(f"  {row['status'].upper():<13} #{row['issue']}")
        if payload["status"] == "fail" and not config.required:
            print("intake board: FAIL (advisory — set intake.required = true only after clean=true)")
    if not config.required:
        return 0
    if payload["status"] == "fail":
        return 1
    if payload["status"] == "no-evaluable":
        return NO_EVALUABLE_EXIT
    return 0
