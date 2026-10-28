# {{project}} — Agent Card

> Always-loaded digest of [`CONSTITUTION.md`](CONSTITUTION.md); the full Constitution prevails.
> Keep this card under 3 KB: detail belongs in the Constitution, commands in `aisdlc.toml`.

## Non-negotiables

1. Issue + change record before code (`aisdlc gate plan`).
2. Failing test first, then the code (`aisdlc gate red`).
3. Gates are commands: run them, never claim them (`aisdlc gate pre-push`).
4. Baselines only shrink; floors only rise (`aisdlc ratchet`).
5. Push the branch at creation and after each stage; never commit to the default branch.
6. Docs change in the same pull request; nothing is duplicated.
7. Only the merge rule merges; only the Owner grants exceptions.

## Where things are

| Need | Where |
|------|-------|
| Process | `CONSTITUTION.md` |
| Commands, gates, ratchets, roles | `aisdlc.toml` (`aisdlc gate --list`) |
| Change records | `.aisdlc/changes/` |
| Role agents | `.aisdlc/agents/` |
| Evidence and cost | `aisdlc report` |
