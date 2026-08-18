# Manual de Instalación

> **Aviso:** el manual de instalación original (2012) describía la instalación del monolito legacy sobre
> **WampServer + MySQL + JRE**. Esa pila tecnológica fue reemplazada por **Docker + PostgreSQL 16 + Spring
> Boot 4.1.0 (Java 21)**; el documento original quedó archivado por su valor histórico en
> [`docs/000-archive/100-business/105-manuals/`](../../../000-archive/100-business/105-manuals/) y no debe
> usarse como referencia de instalación actual.

## Instalación actual

La guía de instalación y puesta en marcha del entorno vigente se mantiene en un único lugar para evitar
duplicación:

**→ [`docs/300-development/301-setup/README.md`](../../../300-development/301-setup/README.md)**

Esa guía cubre: requisitos previos, clonado del repositorio, configuración de `.env`, arranque de la pila
Docker (`scripts/start.sh`), compilación Maven, estructura de módulos, comandos de test y troubleshooting.
