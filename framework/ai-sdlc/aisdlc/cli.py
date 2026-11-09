"""`aisdlc` command line. Exit codes: 0 passed, 1 a gate or check failed, 2 usage or configuration error."""
from __future__ import annotations

import argparse
from pathlib import Path

import json
import os

from aisdlc import (
    __version__,
    adopt,
    catalogs,
    change,
    checks,
    diffchecks,
    evidence,
    gates,
    harness,
    lifecycle,
    metrics,
    ratchet,
    repo,
    skills,
)
from aisdlc.telemetry import get_telemetry
from aisdlc.context import Context
from aisdlc.factory import (
    check_inventory,
    config_view,
    list_plugins,
    method_plan,
    resolve_max_open_prs,
    validate_factory,
)
from aisdlc.harness import HarnessError
from aisdlc.lifecycle import DEFAULT_PATH
from aisdlc.profile import ProfileError, find_profile, load_profile


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="aisdlc", description="Stack-agnostic AI SDLC gates, ratchets and evidence.")
    parser.add_argument("--version", action="version", version=f"aisdlc {__version__}")
    parser.add_argument("--root", type=Path, help="project root (default: git top level, else the current directory)")
    parser.add_argument("--profile", type=Path, help="profile path (default: aisdlc.toml or .aisdlc/aisdlc.toml)")
    sub = parser.add_subparsers(dest="command", required=True)

    p = sub.add_parser("init", help="scaffold policy, profile, agent card and role agents")
    p.add_argument("--name", required=True)
    p.add_argument("--level", type=int, default=1, help="adoption level 1-4")
    p.add_argument("--force", action="store_true")

    p = sub.add_parser("install", help="copy the kit into the repo and scaffold a profile (#2135)")
    p.add_argument("--path", default=DEFAULT_PATH, help=f"kit destination (default: {DEFAULT_PATH})")
    p.add_argument("--name", required=True, help="project name for the scaffolded profile")
    p.add_argument("--level", type=int, default=1, help="adoption level 1-4")
    p.add_argument("--force", action="store_true", help="replace an existing kit tree and overwrite scaffold files")

    p = sub.add_parser("update", help="re-copy the kit idempotently; leave adopter profile/evidence alone")
    p.add_argument("--path", help=f"kit destination (default: manifest or {DEFAULT_PATH})")

    p = sub.add_parser("uninstall", help="remove kit-managed paths; --purge also drops scaffold and evidence")
    p.add_argument("--path", help=f"kit destination (default: manifest or {DEFAULT_PATH})")
    p.add_argument("--purge", action="store_true", help="also remove scaffolded policy/profile and evidence")

    sub.add_parser("doctor", help="validate the profile and report what its level still lacks")

    p = sub.add_parser("contract", help="factory control OpenAPI 3.1.0 contract (#2136)")
    contract_sub = p.add_subparsers(dest="action", required=True)
    contract_sub.add_parser("show", help="print the path of the factory OpenAPI document")
    contract_sub.add_parser("check", help="validate OpenAPI 3.1.0 structure and required paths")

    p = sub.add_parser("config", help="validate or print factory configuration")
    config_sub = p.add_subparsers(dest="action", required=True)
    show = config_sub.add_parser("show", help="print the resolved factory config")
    show.add_argument("--redacted", action="store_true", help="required: refuse a raw dump")
    show.add_argument("--json", action="store_true")
    show.add_argument("--max-open-prs", type=int, help="override the open pull-request cap for this print")
    config_sub.add_parser("validate", help="check factory config, plugins and the migration inventory")

    p = sub.add_parser("plugin", help="list trusted plugins without executing them")
    plugin_sub = p.add_subparsers(dest="action", required=True)
    listed = plugin_sub.add_parser("list")
    listed.add_argument("--json", action="store_true")
    caps = plugin_sub.add_parser("capabilities")
    caps.add_argument("--json", action="store_true")
    plan = plugin_sub.add_parser("plan", help="print a plugin command without running it")
    plan.add_argument("--id", required=True)
    plan.add_argument("--var", action="append", default=[], metavar="KEY=VALUE")

    p = sub.add_parser("inventory", help="check the migration inventory")
    p.add_argument("action", choices=("check",))

    p = sub.add_parser("harness", help="list or show agent-runtime adapters (not a commercial Harness product)")
    harness_sub = p.add_subparsers(dest="action", required=True)
    listed = harness_sub.add_parser("list", help="list adapters declared in the profile")
    listed.add_argument("--json", action="store_true")
    shown = harness_sub.add_parser("show", help="read one adapter source in place")
    shown.add_argument("--id", required=True)
    shown.add_argument("--json", action="store_true")

    p = sub.add_parser("gate", help="run every gate of a stage")
    p.add_argument("stage", nargs="?")
    p.add_argument("--list", action="store_true", help="list stages and gates")
    p.add_argument("--base", help="base revision for path scoping and {base}")
    p.add_argument("--var", action="append", default=[], metavar="KEY=VALUE", help="fill a {KEY} placeholder")
    p.add_argument("--stream", action="store_true", help="stream gate output live")

    p = sub.add_parser("ratchet", help="baselines only shrink, floors only rise")
    p.add_argument("--base")
    p.add_argument("--name")

    sub.add_parser("skills", help="lint the skills catalog")
    sub.add_parser("budget", help="check the always-loaded agent context budget")
    for name, text in (("commits", "check commit subjects since the base"),
                       ("tdd", "production changes must come with test changes")):
        sub.add_parser(name, help=text).add_argument("--base")
    sub.add_parser("protected", help="owner-only paths changed since the base").add_argument("--base")
    p = sub.add_parser("size", help="changed lines since the base stay within the review budget")
    p.add_argument("--base")
    p.add_argument("--limit", type=int, help="override [size] limit")
    p = sub.add_parser("scope", help="changed paths stay inside the change record's write set")
    p.add_argument("--base")
    p.add_argument("--strict", action="store_true", help="fail when no change record declares a write set")

    p = sub.add_parser("evidence", help="evidence ledger integrity")
    q = p.add_subparsers(dest="action", required=True).add_parser("verify", help="check the hash chain of the ledger")
    q.add_argument("--anchor", action="append", default=[], metavar="DIGEST",
                   help="a head digest recorded earlier; fails unless that record is still in the ledger")

    p = sub.add_parser("change", help="built-in change records")
    change_sub = p.add_subparsers(dest="action", required=True)
    q = change_sub.add_parser("new")
    q.add_argument("name")
    q.add_argument("--issue", required=True)
    q.add_argument("--title")
    q = change_sub.add_parser("check")
    q.add_argument("names", nargs="*")

    p = sub.add_parser("report", help="summarize the evidence ledger and costs")
    p.add_argument("--json", action="store_true")
    p.add_argument("--since", help="ISO date, e.g. 2026-10-01")
    p.add_argument("--html", type=Path, help="write a static HTML monitor snapshot (#2142)")
    p.add_argument(
        "--lead-time-fixture",
        type=Path,
        help="optional forge sample JSON to embed lead time in --html",
    )

    p = sub.add_parser("metrics", help="lead-time and stage duration metrics (#2138)")
    metrics_sub = p.add_subparsers(dest="action", required=True)
    lead = metrics_sub.add_parser(
        "lead-time",
        help="issue.created_at → earliest linked PR checks_green_at (fixture or sample JSON)",
    )
    lead.add_argument("--fixture", type=Path, required=True, help="JSON sample with issue + pull_requests")
    lead.add_argument("--json", action="store_true")
    stages = metrics_sub.add_parser("stages", help="stage durations from the evidence ledger")
    stages.add_argument("--json", action="store_true")
    stages.add_argument("--since", help="ISO date, e.g. 2026-10-01")

    p = sub.add_parser("cost", help="record the token cost of an agent run")
    p.add_argument("--agent", required=True)
    p.add_argument("--model", required=True)
    p.add_argument("--input", type=int, required=True, help="input tokens")
    p.add_argument("--output", type=int, required=True, help="output tokens")
    p.add_argument("--usd", type=float, help="actual cost; computed from [cost.prices] when omitted")
    p.add_argument("--issue")

    p = sub.add_parser("catalog", help="CRUD for skills/MCP/agents/memory catalogs (#2139)")
    cat_sub = p.add_subparsers(dest="action", required=True)
    for action, help_text in (
        ("list", "list catalog entries (never executes manifests)"),
        ("validate", "check digests and skill refs (read-only)"),
    ):
        q = cat_sub.add_parser(action, help=help_text)
        q.add_argument("--kind", choices=list(catalogs.KINDS), help="limit to one kind")
        if action == "list":
            q.add_argument("--json", action="store_true")
    for action in ("add", "update", "remove"):
        q = cat_sub.add_parser(action)
        q.add_argument("--kind", required=True, choices=list(catalogs.KINDS))
        q.add_argument("--id", required=True)
        if action != "remove":
            q.add_argument("--ref", help="path or logical reference")
            q.add_argument("--summary", help="short description")
            if action == "add":
                q.set_defaults(summary="")
    return parser


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    root = (args.root or repo.toplevel(Path.cwd()) or Path.cwd()).resolve()
    if args.command == "init":
        return adopt.init(root, args.name, args.level, args.force)
    if args.command == "install":
        try:
            return lifecycle.install(root, args.path, args.name, args.level, args.force)
        except ValueError as exc:
            print(f"error: {exc}")
            return 2
    if args.command == "update":
        try:
            return lifecycle.update(root, args.path)
        except ValueError as exc:
            print(f"error: {exc}")
            return 2
    if args.command == "uninstall":
        try:
            return lifecycle.uninstall(root, args.path, args.purge)
        except ValueError as exc:
            print(f"error: {exc}")
            return 2
    if args.command == "contract":
        from aisdlc import contract

        if args.action == "show":
            print(contract.contract_path())
            return 0
        try:
            return contract.check()
        except FileNotFoundError as exc:
            print(f"error: {exc}")
            return 2
    try:
        return dispatch(args, load_context(root, args.profile))
    except (ProfileError, repo.GitError) as exc:
        print(f"error: {exc}")
        return 2


def load_context(root: Path, profile_arg: Path | None) -> Context:
    path = profile_arg.resolve() if profile_arg else find_profile(root)
    profile = load_profile(path)
    from aisdlc.flags import bind_flags
    from aisdlc.telemetry import bind_telemetry

    bind_telemetry(profile, env=dict(os.environ))
    bind_flags(root, profile.flags)
    return Context(root=root, profile=profile, profile_path=path)


def dispatch(args: argparse.Namespace, ctx: Context) -> int:
    if args.command == "gate":
        if args.list or not args.stage:
            return gates.list_gates(ctx)
        return gates.run_stage(ctx, args.stage, args.base, parse_vars(args.var), args.stream)
    if args.command == "change":
        if args.action == "new":
            return change.new(ctx, args.name, args.issue, args.title)
        return change.check(ctx, args.names)
    if args.command == "cost":
        return evidence.add_cost(ctx, args.agent, args.model, args.input, args.output, args.usd, args.issue)
    simple = {
        "doctor": lambda: adopt.doctor(ctx),
        "ratchet": lambda: ratchet.run(ctx, args.base, args.name),
        "skills": lambda: skills.lint(ctx),
        "budget": lambda: checks.budget(ctx),
        "commits": lambda: checks.commits(ctx, args.base),
        "tdd": lambda: checks.tdd(ctx, args.base),
        "report": lambda: evidence.report(
            ctx,
            args.json,
            args.since,
            html_path=getattr(args, "html", None),
            lead_time_fixture=getattr(args, "lead_time_fixture", None),
        ),
        "metrics": lambda: _metrics(ctx, args),
        "catalog": lambda: _catalog(ctx, args),
        "protected": lambda: diffchecks.protected(ctx, args.base),
        "size": lambda: diffchecks.size(ctx, args.base, args.limit),
        "scope": lambda: diffchecks.scope(ctx, args.base, args.strict),
        "evidence": lambda: evidence.verify(ctx, args.anchor),
        "config": lambda: _config(ctx, args),
        "plugin": lambda: _plugin(ctx, args),
        "inventory": lambda: _inventory(ctx),
        "harness": lambda: _harness(ctx, args),
    }
    return simple[args.command]()


def _catalog(ctx: Context, args: argparse.Namespace) -> int:
    try:
        if args.action == "list":
            data = catalogs.list_entries(ctx, getattr(args, "kind", None))
            if args.json:
                print(json.dumps(data, indent=2, sort_keys=True))
            else:
                blocks = [data] if getattr(args, "kind", None) else data.values()
                for block in blocks:
                    print(f"[{block['kind']}] {len(block['entries'])} entries")
                    for entry in block["entries"]:
                        print(f"  - {entry['id']}" + (f" · {entry.get('summary')}" if entry.get("summary") else ""))
            return 0
        if args.action == "validate":
            return catalogs.validate(ctx, getattr(args, "kind", None))
        if args.action == "add":
            return catalogs.add(ctx, args.kind, args.id, ref=args.ref, summary=args.summary)
        if args.action == "update":
            return catalogs.update(ctx, args.kind, args.id, ref=args.ref, summary=args.summary)
        return catalogs.remove(ctx, args.kind, args.id)
    except ProfileError as exc:
        print(f"error: {exc}")
        return 2


def _metrics(ctx: Context, args: argparse.Namespace) -> int:
    if args.action == "lead-time":
        path = args.fixture
        if not path.is_file():
            print(f"error: fixture not found: {path}")
            return 2
        result = metrics.load_lead_time_fixture(path)
        metrics.emit_lead_time(get_telemetry(), result)
        if args.json:
            print(json.dumps(result, indent=2, sort_keys=True))
        else:
            if result["status"] != "ok":
                print(f"lead-time: no-evaluable (issue {result.get('issue')})")
                return 1
            print(
                f"lead-time: issue #{result['issue']} → PR #{result['pull_request']} "
                f"green in {result['seconds']:.0f}s "
                f"({result['start_event']} → {result['end_event']})"
            )
        return 0 if result["status"] == "ok" else 1
    # stages
    data = evidence.summarize(ctx, args.since)
    stages = data.get("stages") or []
    metrics.emit_stage_durations(get_telemetry(), stages)
    if args.json:
        print(json.dumps({"stages": stages, "event_pair": metrics.EVENT_PAIR}, indent=2, sort_keys=True))
        return 0
    print("| Stage | Runs | OK | Median s | Total s |")
    print("|-------|-----:|---:|---------:|--------:|")
    for stage in stages:
        print(
            f"| {stage['stage']} | {stage['runs']} | {stage['ok_runs']} | "
            f"{stage['median_seconds']:.1f} | {stage['total_seconds']:.1f} |"
        )
    return 0


def _config(ctx: Context, args: argparse.Namespace) -> int:
    if args.action == "validate":
        errors = validate_factory(ctx.root, ctx.profile)
        if errors:
            print("factory config invalid:")
            for error in errors:
                print(f"  - {error}")
            return 1
        print("factory config valid")
        return 0
    if not args.redacted:
        print("error: config show requires --redacted")
        return 2
    from aisdlc.flags import get_flags

    limit = resolve_max_open_prs(ctx.profile, os.environ, args.max_open_prs, flag_provider=get_flags())
    view = config_view(ctx.profile, limit=limit)
    print(json.dumps(view, indent=2, sort_keys=True) if args.json else _plain(view))
    return 0


def _plugin(ctx: Context, args: argparse.Namespace) -> int:
    if args.action == "plan":
        try:
            print(method_plan(ctx.root, ctx.profile, args.id, parse_vars(args.var)))
        except ProfileError as exc:
            print(f"error: {exc}")
            return 2
        return 0
    rows = list_plugins(ctx.root, ctx.profile)
    if args.action == "capabilities":
        rows = [row for row in rows if row["available"]]
    if args.json:
        print(json.dumps(rows, indent=2))
    else:
        for row in rows:
            caps = ",".join(row["capabilities"]) or "-"
            print(f"{row['id']}\t{row['kind']}\t{row['status']}\tavailable={row['available']}\t{caps}")
    return 0


def _inventory(ctx: Context) -> int:
    if not ctx.profile.inventory:
        print("error: [factory] inventory is not set")
        return 2
    errors = check_inventory(ctx.root / ctx.profile.inventory)
    if errors:
        print("migration inventory invalid:")
        for error in errors:
            print(f"  - {error}")
        return 1
    print("migration inventory valid")
    return 0


def _harness(ctx: Context, args: argparse.Namespace) -> int:
    if args.action == "list":
        rows = harness.list_adapters(ctx.root, ctx.profile)
        if args.json:
            print(json.dumps(rows, indent=2))
        else:
            for row in rows:
                print(
                    f"{row['id']}\t{row['runtime']}\treadable={row['readable']}\t"
                    f"sandbox={row['sandbox']}\t{row['source']}"
                )
        return 0
    try:
        view = harness.describe(ctx.root, ctx.profile, args.id)
    except ProfileError as exc:
        print(f"error: {exc}")
        return 2
    except HarnessError as exc:
        print(f"error: {exc}")
        return 1
    if args.json:
        print(json.dumps(view, indent=2, sort_keys=True))
    else:
        print(_plain(view))
    return 0


def _plain(view: dict) -> str:
    return "\n".join(f"{key}={value}" for key, value in view.items())


def parse_vars(pairs: list[str]) -> dict[str, str]:
    values = {}
    for pair in pairs:
        key, sep, value = pair.partition("=")
        if not sep or not key:
            raise ProfileError(f"--var expects KEY=VALUE, got {pair!r}")
        values[key] = value
    return values
