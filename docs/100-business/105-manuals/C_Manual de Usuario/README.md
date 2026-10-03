# Manual de Usuario (PDF)

The historical **Manual de Usuario Notaire** PDF (~13 MB) is **not** stored as
an ordinary git blob (issue [#1050](https://github.com/matiaspakua/notaire/issues/1050),
CU76, [ADR-022](../../../200-architecture/202-ADR/ADR-022-git-history-rewrite-and-large-binaries.md)).

## Markdown companion (in-tree)

The searchable text companion remains here:

- [`Manual de Usuario Notaire.doc.md`](./Manual%20de%20Usuario%20Notaire.doc.md)

## Obtain the PDF

Publish / download via the `docs-manuals` GitHub Release asset
`Manual-de-Usuario-Notaire.doc.pdf`:

```bash
bash scripts/fetch-user-manual.sh
```

Or manually:

```bash
gh release download docs-manuals \
  --repo matiaspakua/notaire \
  --pattern 'Manual-de-Usuario-Notaire.doc.pdf' \
  --dir 'docs/100-business/105-manuals/C_Manual de Usuario'
```

Override tag/asset with `NOTAIRE_MANUAL_RELEASE_TAG` /
`NOTAIRE_MANUAL_ASSET_NAME` if needed.

> After cloning, run the fetch script once if you need the binary PDF locally.
> The PDF path is gitignored as a local download (see root `.gitignore`).
