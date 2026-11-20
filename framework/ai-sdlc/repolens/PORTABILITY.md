# Portability

The installable package is `repolens` and `repolens.extractors` (`pyproject.toml`).
Benchmark task files and published result JSON are adopter measurements. They are not
in the wheel.

Runners under `bench/*.py` take task paths and provider flags. They do not import the
AI SDLC factory. Agent sessions use `bench/providers.py`: Anthropic when `claude` can
authenticate, otherwise the Cursor agent CLI. See `aisdlc agent probe` in a factory
checkout for the same decision, configured separately so neither tree imports the other.

Copy this directory out of a monorepo and run `python3 -m pytest -q` and
`python3 -m repolens --help` from that copy.
