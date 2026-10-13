# Release asset backfill runbook

Use this runbook to attach the five documented release assets to an existing
`vX.Y.Z` GitHub Release **without** changing the tag, release notes,
`publishedAt`, `targetCommitish`, or published image digests. See
[`RELEASES.md`](305-project-management/RELEASES.md) and [`SBOM.md`](SBOM.md) for
the normal cut path.

## Preconditions

- Owner approval for the target tag (default repair tag: `v0.2.0`).
- `gh` authenticated with `contents: write` (and read access to GHCR).
- `docker` / `buildx` with `imagetools` for additive semver tags on existing digests.
- `trivy` for CycloneDX image bills against a **digest**, not a rebuild.
- This repository checkout; no PAT required when using `gh` interactively.

## 1. Read-only audit (do not mutate)

```bash
TAG=v0.2.0
gh release view "$TAG" --json tagName,targetCommitish,publishedAt,assets > before-release.json
git rev-list -n 1 "$TAG" > before-tag-commit.txt
```

Abort if the tag commit is not the commit you expect.

## 2. Worktree at the tag

```bash
git worktree add ../notaire-at-tag "$TAG"
cd ../notaire-at-tag
```

## 3. Coherence check at the tag

```bash
python3 workspace/sdlc/check-product-version-coherence.py --root . --tag "$TAG"
```

## 4. Generate product and database bills from the tagged tree

```bash
mkdir -p /tmp/release-assets
python3 workspace/release/product_release.py --root . --write-sbom /tmp/release-assets
cp release/product-release.json /tmp/release-assets/
```

## 5. Image bills from published digests

Resolve the digest already published for the tag commit SHA (do **not** rebuild):

```bash
SHA=$(git rev-parse "$TAG")
# Example: gh api …/packages/container/backend/versions | jq …
docker buildx imagetools inspect "ghcr.io/matiaspakua/notaire/backend:$SHA"
```

Generate CycloneDX with Trivy against `image@digest` for backend and frontend.
Save as `sbom-backend.cdx.json` and `sbom-frontend.cdx.json` under
`/tmp/release-assets/`.

Optionally add the semver tag (`0.2.0`) to the same digest (additive only):

```bash
docker buildx imagetools create \
  --tag ghcr.io/matiaspakua/notaire/backend:0.2.0 \
  ghcr.io/matiaspakua/notaire/backend@sha256:…
```

Repeat for frontend.

## 6. Upload **missing** assets only

```bash
for file in sbom-backend.cdx.json sbom-frontend.cdx.json sbom-database.cdx.json \
  sbom-notaire.cdx.json product-release.json; do
  if ! gh release view "$TAG" --json assets --jq '.assets[].name' | grep -qx "$file"; then
    gh release upload "$TAG" "/tmp/release-assets/$file"
  fi
done
```

You must not pass `--clobber`. Never delete the Release or recreate it.

## 7. Verify and capture after state

```bash
gh release view "$TAG" --json tagName,targetCommitish,publishedAt,assets > after-release.json
python3 workspace/sdlc/check-product-version-coherence.py
```

Abort and escalate if `tagName`, `targetCommitish`, `publishedAt`, or the tag
commit differ between before and after captures.

## Legacy tags (`notaire-v0.1.1`, `notaire-v0.1.2`)

| Tag | Product bill | Image bill | Database bill | Notes |
|-----|--------------|------------|---------------|-------|
| `notaire-v0.1.1` | omit | only if an image exists for the tag commit | from tagged tree | Maven was `1.0-SNAPSHOT`; do not fabricate a product record |
| `notaire-v0.1.2` | omit | only if an image exists for the tag commit | from tagged tree | same policy |

Document any omitted asset in the PR or Issue comment; do not invent coherence
that never existed.

## Abort rules

- Any before/after field on the Release object changes.
- A digest would change (rebuild, re-sign, or re-tag to a new digest).
- The upload would replace an existing asset (`--clobber`).
