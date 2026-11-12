# Upgrading the AI SDLC kit

## Versioning

- Kit version lives in `aisdlc/__version__` and is independent of the Notaire product tag.
- Release tags use the `aisdlc-vX.Y.Z` form. GitHub Release assets: wheel, sdist, CycloneDX SBOM, provenance attestation.

## Profile `schema_version`

- `[project] schema_version` is a positive integer (current: `1`).
- Absent key → treated as `1` for compatibility.
- `aisdlc init` / `aisdlc install` write the current value.

## Deprecation window

Breaking profile or CLI behaviour is deprecated for **one minor version** before removal.
Example: a field removed in `1.2.0` must have been warned as deprecated since at least `1.1.0`.
Document removals in this file and in `CHANGELOG.md`.

## PyPI

Publishing `aisdlc` to PyPI is an **Owner decision / non-goal** until explicitly asked
(design record #2251 §12). Install from a GitHub Release asset or a local wheel:

```bash
python -m pip install path/to/aisdlc-X.Y.Z-py3-none-any.whl
aisdlc install --path tools/ai-sdlc --name your-product
```

## kit_version skew

Optional `[project] kit_version` records the kit release the profile was written for.
`aisdlc doctor` prints INFO on minor/patch skew and WARN on major skew; it never fails the run for skew alone.
