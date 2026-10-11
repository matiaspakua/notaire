# Class diagrams — legacy (archived)

The original class diagrams (`dto.jpg`, `gui.jpg`, `jpa.jpg`, `negocio.jpg`, `servicios.jpg`) documented
the class structure of the legacy Java Swing monolith. They were archived under
[`docs/000-archive/200-architecture/204-diagrams/Diagrama de Clases/`](../../../000-archive/200-architecture/204-diagrams/Diagrama%20de%20Clases/)
for two reasons:

- **`dto.jpg` and `negocio.jpg`** are unreadable at every available resolution (an export from an
  old modeling tool with dense text); their content can no longer be transcribed reliably.
- **`gui.jpg`** documents the `frontend-swing` module, which is **legacy/deprecated** (see `CLAUDE.md`): excluded
  from the root Maven reactor, it receives no new features and must not be used as the current architecture reference.
- **`jpa.jpg` and `servicios.jpg`** documented the original monolith's `jpa` and `service` packages, from before the
  migration to Spring Boot.

The **current** backend class structure, checked against the source, is documented in:

- [`backend-package-structure.puml`](../backend-package-structure.puml) — package structure of `backend-api`
- [SAD § 5.5 — Core Domain Model](../../201-SAD/sad.md#55-core-domain-model) — domain entities and their relationships
- [SAD § 5.2 — Building Block View: backend-api](../../201-SAD/sad.md#52-level-2--backend-api-internal-structure)
