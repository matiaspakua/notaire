# AI SDLC framework kit

Stack-agnostic kit extracted from Notaire's evidence (#1958). Copy this tree into
another repository, add a profile under `profiles/<name>/`, and point agents at
your Constitution.

## Layout

| Path | Role |
|------|------|
| `run_gates.py` | Profile-driven gate runner (list / dry-run / execute) |
| `profiles/<name>/profile.yaml` | Adopter-specific gate commands and metadata |
| `templates/` | Neutral artifact starters (fill from your schema) |
| `tests/` | Kit unit tests |

Notaire's adopter profile is `profiles/notaire/`. The runner itself must not
import or hardcode product paths (`backend-api/`, `frontend/`, use-case IDs).

## Quick start

```bash
# List gates for the Notaire profile (from the monorepo root)
python3 framework/ai-sdlc/run_gates.py --profile notaire --list

# Dry-run (print only)
python3 framework/ai-sdlc/run_gates.py --profile notaire --dry-run
```

## Adopting in another product

1. Copy `framework/ai-sdlc/` into the new repo.
2. Write `profiles/<your-product>/profile.yaml` with gate `id` + `command` pairs
   that invoke *your* scripts.
3. Keep a single Constitution (or equivalent policy) as the source of truth;
   this kit does not replace it.
4. Read `ai_sdlc_framework.md` at the repository root for principles, roles,
   gates, cost notes, and how the kit was derived from Notaire evidence.
5. Run `python3 -m unittest discover -s framework/ai-sdlc/tests`.

## Related

- Report: [`ai_sdlc_framework.md`](../../ai_sdlc_framework.md)
- Notaire cloud fleet docs: `docs/300-development/304-ai-sdlc-cloud/`
- Case study (GitHub Pages): `/ai-sdlc/case-study/`
