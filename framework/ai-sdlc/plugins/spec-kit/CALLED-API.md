# Spec Kit extensions API — call record (#2123)

This file records the **pinned CLI** and the **extensions API that was actually
called** while implementing the adapter. It is not a sandbox claim and it is not
an OpenSpec replacement.

## Pin

| Item | Value |
|------|-------|
| Package | `specify-cli` |
| Version | **1.3.2** |
| Install | `uv tool install 'specify-cli==1.3.2'` |
| CLI | `specify --version` → `specify 1.3.2` |

## API called (CLI)

From a Spec Kit project root (after `specify init … --non-interactive`):

```bash
specify extension search git
```

Observed: catalog search returned matching extensions (e.g. `git` v1.0.4,
GitHub Integration). Requires a `.specify/` directory (or the command errors
with “Not a Spec Kit project”).

## API called (Python)

Documented in upstream `docs/extensions/api.md` (Extension Catalog):

```python
from pathlib import Path
from specify_cli.extensions import ExtensionCatalog

catalog = ExtensionCatalog(Path(".").resolve())
hits = catalog.search("git")
```

Observed against specify-cli 1.3.2 via **ExtensionCatalog.search**: `hits` is a
`list` (23 entries for query `git`); first entry id `git`.

## Factory dry plan

The adapter prints argv only (same contract as OpenSpec):

```text
specify extension search <query>
```

`plugin list` / `config validate` do not execute `specify`. The Notaire profile
keeps `github-spec-kit` **out of** `[plugins] allow` until an Owner opts in
after config validate and a dry plan pass on a profile that includes it.
