# Second-adopter sample (shop)

Non-product sample that proves a second project can adopt the AI SDLC kit **via the
wheel** (#2256 / CU76). This is not the Notaire sandbox; kit code and templates stay
free of adopter product paths.

## Quickstart (target ≤ 15 minutes)

```bash
# From a kit checkout that already built a wheel, or a GitHub Release asset:
python3 -m venv .venv && . .venv/bin/activate
python3 -m pip install path/to/aisdlc-*.whl
mkdir shop && cd shop && git init -b main
aisdlc install --path tools/ai-sdlc --name shop
# Offline tracker stub for Gate 1 without a forge:
printf '\n[tracker]\nverify = "true"\n' >> aisdlc.toml
aisdlc doctor
aisdlc change new quickstart --issue 1 --title "Quickstart"
# Fill the change record sections, then:
aisdlc gate plan
```

Recorded budget: see `QUICKSTART-TIME.txt` (`max_seconds=900`).

## PyPI

Not published by the kit release workflow. PyPI remains an Owner decision; install from
the GitHub Release wheel/sdist (or a locally built wheel) until then.
