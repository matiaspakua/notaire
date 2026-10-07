# Manuales

Manuales de instalación, sistema y usuario del producto Notaire.

| Carpeta | Contenido |
|---------|-----------|
| [`A_Manual de Instalación/`](A_Manual%20de%20Instalaci%C3%B3n/) | Instalación vigente (apunta al setup actual) + aviso del manual 2012 archivado |
| [`B_Manual de Sistema/`](B_Manual%20de%20Sistema/) | Manual de sistema (markdown) |
| [`C_Manual de Usuario/`](C_Manual%20de%20Usuario/) | Manual de usuario (markdown in-tree; **PDF via GitHub Release**) |

## User-manual PDF (#1050 / ADR-022)

The ~13 MB `Manual de Usuario Notaire.doc.pdf` is **not** an ordinary git blob.
Obtain it with:

```bash
bash docs/tools/fetch-user-manual.sh
```

Details: [`C_Manual de Usuario/README.md`](C_Manual%20de%20Usuario/README.md) and
[ADR-022](../../200-architecture/202-ADR/ADR-022-git-history-rewrite-and-large-binaries.md).
