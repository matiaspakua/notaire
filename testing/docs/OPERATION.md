# Operating the QA suites

Commands run from the repository root while `testing/` is co-located. Prerequisites:
[PREPARATION](PREPARATION.md); variables: [CONFIGURATION](CONFIGURATION.md).

## Run

```bash
bash testing/scripts/run.sh --list          # integration, database, e2e
bash testing/scripts/run.sh database         # Docker only, about 10 seconds
bash testing/scripts/run.sh integration      # against a running stack
bash testing/scripts/run.sh e2e              # Playwright UI suite; extra arguments go to playwright (e.g. --project=smoke)
bash testing/scripts/test.sh                 # same as integration; the stable entry point
```

The database suite prints one `PASS` or `FAIL` line per check, ends with
`Database V&V: N passed, M failed`, and exits non-zero on any failure.

Point the integration suite at another environment:

```bash
BASE_URL=https://staging.example.org bash testing/scripts/run.sh integration
```

## Read a failure

| Output | Meaning | Action |
|--------|---------|--------|
| `FAIL flyway migrate failed` and Flyway's message above it | A migration does not apply to an empty database | Fix the migration in the application repository (a new `V<n>`, never an edit) |
| `No value provided for placeholder: ${…}` | A migration needs a Flyway placeholder the suite does not supply | Add it to `.env.example`, `database/run.sh` and CONFIGURATION |
| `FAIL … versioned files but … applied` | A `V<n>` file was not applied (bad name or gap) | Check the file name and version sequence |
| `FAIL ignored SQL files are […]` | An SQL file Flyway skips that is not the documented rollback | Rename it correctly or document it in `EXPECTED_IGNORED` |
| `FAIL table … still exists after being renamed` | A rename did not take effect | Inspect the migration that renames it |
| `FAIL seed … has at least …` | Seed data changed or a seed migration broke | Compare with V2 and V10; update the expectation only if the change is intended |
| `FAIL flyway validate accepted an edited migration` | Checksum protection is off | Check Flyway settings; this must never pass |

## Add a database check

1. Add `database/checks/NN_name.sql` (the number sets the order).
2. Print one row per assertion, starting with `PASS` or `FAIL`:

   ```sql
   SELECT CASE WHEN count(*) = 1 THEN 'PASS' ELSE 'FAIL' END || ' description'
   FROM some_table WHERE …;
   ```

3. Use `:'exporter'` if you need the exporter role name (the runner passes `-v exporter=…`).
4. Run the suite once with the condition inverted to see the check fail, then restore it.

## CI

`.github/workflows/database-vv.yml` runs the database suite on pull requests and pushes that touch
the migrations or `testing/database`. Locally, `bash scripts/preflight.sh --full` runs the same
suite ("database v&v suite"). The integration suite needs a running stack and runs locally and
through `preflight.sh --full`.

## Clean up

A run removes its containers, network and scratch directory on exit. If a run was killed:

```bash
docker ps -a --format '{{.Names}}' | grep notaire-db-vv      # leftovers
docker compose -p <project> -f testing/database/docker-compose.yml down -v
rm -rf testing/database/.work.*
```

## Rollback

Revert the pull request that changed `testing/`. The suites hold no state: the database suite uses
throwaway containers and the integration suite only reads from the stack under test (it creates and
changes a few sample records through the API, as the cURL scripts always did).
