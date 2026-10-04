# ERD artifacts

Generated from the schema Flyway builds ([ADR-007](../../202-ADR/ADR-007-database-schema-versioning-flyway.md)); do not edit by hand.

| File | Content |
|------|---------|
| `ERD-Escribania_completo.puml` / `ERD_Escribania_completo.svg` | All tables, all columns, foreign-key relationships |
| `ERD.puml` / `ERD_Escribania.svg` | Entities grouped by package, with relationships |
| `Modelo Relacional Escribania - Entidades.csv` | One row per column (type, key, nullability, description) |

## Regenerate

With a PostgreSQL migrated by Flyway (for example the running stack), set the libpq variables and run:

```bash
export PGHOST=localhost PGUSER=notaire PGPASSWORD=... PGDATABASE=notaire
python3 scripts/generate_erd.py --render   # needs plantuml (or PLANTUML_JAR=/path/plantuml.jar) and graphviz
```

`scripts/test_erd_current_schema.py` (collected by CI) fails when an artifact uses a retired Spanish table name
or the three artifacts disagree. A new table needs a package in `PACKAGES` inside the generator.
