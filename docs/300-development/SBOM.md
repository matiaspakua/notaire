# Software Bill of Materials

Notaire’s SBOM is **CycloneDX 1.6**. A release attaches five files. They
answer different questions and are not substitutes for each other.

| Asset | Question it answers | Produced by |
|-------|---------------------|-------------|
| `sbom-notaire.cdx.json` | What is this product cut? | `workspace/release/product_release.py` |
| `sbom-database.cdx.json` | Which Flyway migrations shipped? | the same script |
| `sbom-backend.cdx.json` | Which packages are inside the backend image? | Trivy, on the published image |
| `sbom-frontend.cdx.json` | Which packages are inside the frontend image? | Trivy, on the published image |
| `product-release.json` | The same three components, without CycloneDX packaging | the committed record |

`sbom-notaire` is the **product** bill. Its components are:

- `notaire-backend` — `pkg:maven/com.licensis/notaire-root@<semver>`
- `notaire-frontend` — `pkg:npm/notaire-frontend@<semver>`
- `notaire-database` — `pkg:generic/notaire-database@<flyway version>`

Metadata properties name the delivery train (`notaire:release-train`), the
git tag, and the sibling asset filenames. The train title comes from
[RELEASES.md](305-project-management/RELEASES.md). A 0.x cut records
`2026-11 · Release blockers` because that is its release gate train, not
because the milestone closed.

`sbom-database` lists every versioned migration file (`V*__*.sql`). Repeatable
migrations are not a schema version. The component version is the highest
Flyway id. That id is not the product semver.

The image bills are dependency depth for the containers (OS packages and
libraries). They stay on the digest cosign attests. They do not list the
schema, because the schema is not a layer of either image.

## When they are written

CD’s `release` job runs only on `v*` and legacy `notaire-v*` tags, after both
images are published and the `release-identity` coherence check passes (#1591).
It downloads the Trivy bills, generates the product and database bills from the
tagged tree, asserts all five files exist, uploads with
`fail_on_unmatched_files: true`, and verifies the Release asset set equals
`RELEASE_ASSETS` exactly. It attaches to the GitHub Release that release-please
already created and does not rewrite the release notes.

Local preview:

```bash
python3 workspace/release/product_release.py --write-sbom /tmp/notaire-sbom
```

The committed source of truth for versions is `release/product-release.json`.
The CycloneDX documents are generated from it at release time so a
release-please version bump cannot leave a stale bill on `main`.

## Alignment

| Concern | How the SBOM follows it |
|---------|-------------------------|
| Milestones | `notaire:release-train` is the train title, derived from the semver |
| Board | The SBOM does not change issue status. Done work is already closed |
| AI SDLC | The foreman does not edit release-please branches. A release change is its own issue, lane `lane:devops` when that label exists |
| Version display | The product component version is the same string as actuator info, OpenAPI, and the sidebar |

Supply-chain controls on the images (cosign sign, cosign attest of the image
bill) stay as described in
[208-devsecops](../200-architecture/208-devsecops/README.md).
