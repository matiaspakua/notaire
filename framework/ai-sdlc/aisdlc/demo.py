"""Kit-neutral factory adopter demo seed (#2243 Phase E).

Seeds catalog agents/skills and one approved workflow so backoffice / runtime
UIs have something real to show. Never executes agents or MCP. MCP refs stay
empty (no vanity tools). Kit code names no adopter product paths.
"""
from __future__ import annotations

from aisdlc import catalogs, migrate, workflows
from aisdlc.context import Context
from aisdlc.errors import KitError
from aisdlc.factory import redact

DEFAULT_WORKFLOW_ID = "factory-feature"
DEFAULT_AGENTS = ("planner", "implementer", "reviewer")
DEFAULT_SKILLS = ("propose", "apply", "steward")
OWNER_REASON = "Owner Phase E demo seed (approvals_live=false)"
WAIVE_REASON = "Phase E demo dry-run validation"


def _stage_ids(ctx: Context) -> tuple[list[str], list[str]]:
    agents: list[str] = []
    skills: list[str] = []
    seen_a: set[str] = set()
    seen_s: set[str] = set()
    for stage in ctx.profile.stage_profiles or ():
        for entry_id in stage.agents or ():
            if entry_id and entry_id not in seen_a:
                seen_a.add(entry_id)
                agents.append(entry_id)
        for entry_id in stage.skills or ():
            if entry_id and entry_id not in seen_s:
                seen_s.add(entry_id)
                skills.append(entry_id)
    return agents, skills


def _catalogs_configured(ctx: Context) -> bool:
    return bool(getattr(ctx.profile, "catalogs_dir", None))


def _skill_ref(ctx: Context, entry_id: str, *, scaffold: bool) -> str | None:
    """Prefer an existing skills-dir folder; optionally scaffold under .aisdlc/skills."""
    skills_dir = getattr(ctx.profile, "skills_dir", None)
    if skills_dir:
        candidate = ctx.path(f"{str(skills_dir).rstrip('/')}/{entry_id}")
        if (candidate / "SKILL.md").is_file():
            return f"{str(skills_dir).rstrip('/')}/{entry_id}"
    rel = f".aisdlc/skills/{entry_id}"
    skill_md = ctx.path(rel) / "SKILL.md"
    if skill_md.is_file():
        return rel
    if not scaffold:
        return None
    skill_md.parent.mkdir(parents=True, exist_ok=True)
    skill_md.write_text(
        f"---\nname: {entry_id}\ndescription: Demo skill (declare-only)\n---\n# {entry_id}\n",
        encoding="utf-8",
    )
    return rel


def _ensure_entry(ctx: Context, kind: str, entry_id: str, *, scaffold_skills: bool) -> dict | None:
    """Ensure a catalog entry exists (add only; leave existing revisions alone)."""
    try:
        return catalogs.load_entry(ctx, kind, entry_id)
    except KitError:
        pass
    if kind == "skills":
        ref = _skill_ref(ctx, entry_id, scaffold=scaffold_skills)
        if ref is None:
            return None
    else:
        ref = f".aisdlc/{kind}/{entry_id}"
    return catalogs.add(
        ctx,
        kind,
        entry_id,
        ref=ref,
        summary=f"Demo {kind} entry {entry_id} (declare-only; never executed)",
    )


def _already_approved(ctx: Context, workflow_id: str) -> dict | None:
    try:
        data = catalogs.load_entry(ctx, workflows.KIND, workflow_id)
    except KitError:
        return None
    if data.get("governance") == "approved":
        return redact("", data)
    return None


def seed(
    ctx: Context,
    *,
    workflow_id: str = DEFAULT_WORKFLOW_ID,
    from_stages: bool = False,
    reason: str | None = None,
) -> dict:
    """Seed catalogs + one approved workflow. Idempotent when already approved."""
    if not _catalogs_configured(ctx):
        raise KitError("no_catalogs", "no catalogs configured ([catalogs] dir)")

    existing = _already_approved(ctx, workflow_id)
    if existing is not None:
        return existing

    if from_stages:
        agents, skills = _stage_ids(ctx)
        if not agents or not skills:
            raise KitError(
                "no_stages",
                "demo seed --from-stages needs [[stages]] with agents and skills",
            )
    else:
        agents, skills = list(DEFAULT_AGENTS), list(DEFAULT_SKILLS)

    # Scaffold stub skills for generic demos. When --from-stages and a skills-dir
    # is configured, only pin skills that already exist on disk (no vanity stubs).
    scaffold_skills = not (from_stages and getattr(ctx.profile, "skills_dir", None))
    resolved_agents: list[str] = []
    for entry_id in agents:
        if _ensure_entry(ctx, "agents", entry_id, scaffold_skills=scaffold_skills) is not None:
            resolved_agents.append(entry_id)
    resolved_skills: list[str] = []
    for entry_id in skills:
        if _ensure_entry(ctx, "skills", entry_id, scaffold_skills=scaffold_skills) is not None:
            resolved_skills.append(entry_id)
    if not resolved_agents or not resolved_skills:
        raise KitError(
            "refs_unresolved",
            "demo seed needs at least one resolvable agent and skill catalog entry",
        )
    agents, skills = resolved_agents, resolved_skills

    try:
        catalogs.load_entry(ctx, workflows.KIND, workflow_id)
    except KitError:
        workflows.create(
            ctx,
            workflow_id,
            summary="Factory demo feature workflow (pinned refs; never auto-executed)",
            agents=agents,
            skills=skills,
            mcp=[],
            providers=[],
        )
    else:
        # Reset body toward draft with the desired refs when not yet approved.
        data = catalogs.load_entry(ctx, workflows.KIND, workflow_id)
        if data.get("governance") == "approved":
            return redact("", data)
        workflows.new_version(
            ctx,
            workflow_id,
            summary="Factory demo feature workflow (pinned refs; never auto-executed)",
            agents=agents,
            skills=skills,
            mcp=[],
            providers=[],
        )

    migrate.migrate_workflows(ctx)
    workflows.transition(ctx, workflow_id, "define")
    workflows.transition(
        ctx,
        workflow_id,
        "request_validation",
        waive_reason=WAIVE_REASON,
    )
    approved = workflows.transition(
        ctx,
        workflow_id,
        "approve",
        owner_override=True,
        reason=(reason or OWNER_REASON).strip(),
    )
    return approved
