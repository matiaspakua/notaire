# Diagrama de Clases — Legacy (archivado)

Los diagramas de clases originales (`dto.jpg`, `gui.jpg`, `jpa.jpg`, `negocio.jpg`, `servicios.jpg`) documentaban
la estructura de clases del monolito Java Swing legacy. Se archivaron en
[`docs/000-archive/200-architecture/204-diagrams/Diagrama de Clases/`](../../../000-archive/200-architecture/204-diagrams/Diagrama%20de%20Clases/)
por dos motivos:

- **`dto.jpg` y `negocio.jpg`** son ilegibles a cualquier resolución disponible (exportación de una
  herramienta de modelado antigua con texto denso); su contenido ya no puede transcribirse de forma confiable.
- **`gui.jpg`** documenta el módulo `frontend-swing`, que es **legacy/deprecado** (ver `CLAUDE.md`): excluido
  del reactor Maven raíz, no recibe nuevas features y no debe usarse como referencia de arquitectura vigente.
- **`jpa.jpg` y `servicios.jpg`** documentaban los paquetes `jpa`/`service` del monolito original, previos a la
  migración a Spring Boot.

La estructura de clases **vigente** del backend está documentada, con datos verificados contra el código
fuente actual, en:

- [`backend-package-structure.puml`](../backend-package-structure.puml) — estructura de paquetes de `backend-api`
- [SAD § 5.5 — Core Domain Model](../../201-SAD/sad.md#55-core-domain-model) — entidades de dominio y sus relaciones
- [SAD § 5.2 — Building Block View: backend-api](../../201-SAD/sad.md#52-level-2--backend-api-internal-structure)
