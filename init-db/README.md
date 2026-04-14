# Database Initialization - Notaire

`init-db` contiene los scripts de PostgreSQL usados para inicializar la base de datos del proyecto.

- `01-schema.sql` — definición del esquema y constraints
- `02-data.sql` — datos semilla
- `migrate.load` — archivo de carga/migración

## Uso

Este directorio se utiliza desde Docker Compose y `scripts/start.sh`.

```bash
bash scripts/start.sh
```

## Documentación relevante

- `/docs/02-architecture/01-adr/ADR-004-database-migration.md`
- `/docs/04-operations/02-deployment/`
