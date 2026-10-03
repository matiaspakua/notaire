#!/usr/bin/env bash
#
# Black-box database V&V: empty PostgreSQL -> Flyway CLI migrate -> SQL checks.
# Uses only Docker, the Flyway CLI and SQL; no Spring context. The migrations are the
# application's own, mounted read-only and never edited (Constitution P6).

set -euo pipefail

DB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTING_DIR="$(cd "$DB_DIR/.." && pwd)"

MIGRATIONS_DIR="${MIGRATIONS_DIR:-$TESTING_DIR/../backend-api/src/main/resources/db/migration}" # TESTING_APP_SEAM
case "$MIGRATIONS_DIR" in /*) ;; *) MIGRATIONS_DIR="$TESTING_DIR/$MIGRATIONS_DIR" ;; esac
MIGRATIONS_DIR="$(cd "$MIGRATIONS_DIR" && pwd)"
export MIGRATIONS_DIR

EXPORTER_USER="${POSTGRES_EXPORTER_USER:-notaire_exporter}"
EXPORTER_PASSWORD="${POSTGRES_EXPORTER_PASSWORD:-vv-exporter-throwaway}"
# Migrations Flyway must ignore on purpose (manual rollback scripts), space separated.
EXPECTED_IGNORED="${EXPECTED_IGNORED:-R14__restore_presupuestos_fk_id_tramite.sql}"

PROJECT="${DB_VV_PROJECT:-notaire-db-vv-$$}"
COMPOSE=(docker compose -p "$PROJECT" -f "$DB_DIR/docker-compose.yml")
WORK="$(mktemp -d "$DB_DIR/.work.XXXXXX")"

RESULTS="$WORK/results"

cleanup() {
    "${COMPOSE[@]}" down -v --remove-orphans > /dev/null 2>&1 || true
    rm -rf "$WORK"
}
trap cleanup EXIT

# Counters live in a file because `cmd | record` runs record in a subshell (and macOS bash 3.2
# has no lastpipe).
record() {
    local line
    while IFS= read -r line; do
        [ -n "$line" ] || continue
        echo "  $line"
        echo "$line" >> "$RESULTS"
    done
}

passed_count() { grep -c '^PASS' "$RESULTS" || true; }
failed_count() { grep -vc '^PASS' "$RESULTS" || true; }

flyway() {
    "${COMPOSE[@]}" run --rm -T flyway \
        -placeholders.exporterUsername="$EXPORTER_USER" \
        -placeholders.exporterPassword="$EXPORTER_PASSWORD" "$@"
}

# stdin is /dev/null: `exec -T` would otherwise consume the input of a surrounding `while read` loop.
psql_query() {
    "${COMPOSE[@]}" exec -T postgres psql -U vv -d notaire -v ON_ERROR_STOP=1 -tA "$@" < /dev/null
}

versioned_files() {
    ls "$MIGRATIONS_DIR" | grep -E '^V[0-9]+__.+\.sql$' || true
}

ignored_files() {
    ls "$MIGRATIONS_DIR" | grep -E '\.sql$' | grep -vE '^V[0-9]+__' | sort || true
}

check_migrate_empty_database() {
    echo "== migrate an empty database"
    if flyway migrate > "$WORK/migrate.log" 2>&1; then
        echo "PASS flyway migrate succeeded" | record
    else
        cat "$WORK/migrate.log" >&2
        echo "FAIL flyway migrate failed (see output above)" | record
        return 1
    fi
    if flyway validate > "$WORK/validate.log" 2>&1; then
        echo "PASS flyway validate is clean after migrate" | record
    else
        cat "$WORK/validate.log" >&2
        echo "FAIL flyway validate failed after migrate" | record
    fi
}

check_files_against_history() {
    echo "== migration files versus Flyway history"
    local files applied
    files="$(versioned_files | wc -l | tr -d ' ')"
    applied="$(psql_query -c "SELECT count(*) FROM flyway_schema_history WHERE version IS NOT NULL AND success")"
    if [ "$files" = "$applied" ]; then
        echo "PASS all $files versioned migration files are applied" | record
    else
        echo "FAIL $files versioned files but $applied applied" | record
    fi
    local ignored expected
    ignored="$(ignored_files | tr '\n' ' ' | sed 's/ $//')"
    expected="$(printf '%s\n' $EXPECTED_IGNORED | sort | tr '\n' ' ' | sed 's/ $//')"
    if [ "$ignored" = "$expected" ]; then
        echo "PASS the only ignored SQL file is the documented manual rollback ($ignored)" | record
    else
        echo "FAIL ignored SQL files are [$ignored], expected [$expected]" | record
    fi
}

check_second_migrate_is_noop() {
    echo "== second migrate is a no-op"
    local before after
    before="$(psql_query -c "SELECT count(*) FROM flyway_schema_history")"
    flyway migrate > "$WORK/migrate2.log" 2>&1 || true
    after="$(psql_query -c "SELECT count(*) FROM flyway_schema_history")"
    if grep -q "up to date" "$WORK/migrate2.log" && [ "$before" = "$after" ]; then
        echo "PASS second migrate applied nothing (history stays at $after rows)" | record
    else
        echo "FAIL second migrate changed the database (history $before -> $after)" | record
    fi
}

check_edited_migration_is_detected() {
    echo "== an edited migration is detected"
    local copy="$WORK/tampered"
    mkdir -p "$copy"
    cp "$MIGRATIONS_DIR"/V*.sql "$copy"/
    local first
    first="$(ls "$copy" | grep -E '^V1__' | head -1)"
    echo "-- tampered by the V&V suite" >> "$copy/$first"
    if MIGRATIONS_DIR="$copy" flyway validate > "$WORK/tamper.log" 2>&1; then
        echo "FAIL flyway validate accepted an edited migration ($first)" | record
    elif grep -qi "checksum" "$WORK/tamper.log"; then
        echo "PASS flyway validate rejects an edited migration with a checksum mismatch" | record
    else
        echo "FAIL validate failed but not on a checksum mismatch" | record
    fi
}

check_renamed_tables_are_gone() {
    echo "== tables renamed by migrations no longer exist under the old name"
    local old new list
    list="$(cat "$MIGRATIONS_DIR"/V*.sql \
        | grep -iE '^[[:space:]]*ALTER TABLE( IF EXISTS)?[[:space:]]+[a-z_]+[[:space:]]+RENAME TO[[:space:]]+[a-z_]+' \
        | sed -E 's/^[[:space:]]*[Aa][Ll][Tt][Ee][Rr] [Tt][Aa][Bb][Ll][Ee]( [Ii][Ff] [Ee][Xx][Ii][Ss][Tt][Ss])?[[:space:]]+([a-z_]+)[[:space:]]+[Rr][Ee][Nn][Aa][Mm][Ee] [Tt][Oo][[:space:]]+([a-z_]+).*/\2 \3/')"
    local total=0 gone=0 renames
    renames="$(printf '%s\n' "$list" | grep -c . || true)"
    while read -r old new; do
        [ -n "$old" ] || continue
        # a name that another rename reuses as its target is legitimately present
        if printf '%s\n' "$list" | awk '{print $2}' | grep -qx "$old"; then continue; fi
        total=$((total + 1))
        if [ "$(psql_query -c "SELECT to_regclass('public.$old') IS NULL")" = "t" ]; then
            gone=$((gone + 1))
        else
            echo "FAIL table $old still exists after being renamed to $new" | record
        fi
    done <<< "$list"
    if [ "$gone" = "$total" ] && [ "$total" -gt 0 ]; then
        echo "PASS all $total renamed tables are gone under their old names ($renames rename statements read)" | record
    elif [ "$total" -eq 0 ]; then
        echo "FAIL no table renames were found to verify" | record
    fi
}

run_sql_checks() {
    echo "== SQL checks"
    local file
    for file in "$DB_DIR"/checks/*.sql; do
        echo "-- $(basename "$file")"
        "${COMPOSE[@]}" exec -T postgres psql -U vv -d notaire -v ON_ERROR_STOP=1 -tA \
            -v exporter="$EXPORTER_USER" -f - < "$file" | record
    done
}

echo "Database V&V — migrations: $MIGRATIONS_DIR"
"${COMPOSE[@]}" up -d --wait postgres > /dev/null

: > "$RESULTS"
check_migrate_empty_database || true
if [ "$(failed_count)" -eq 0 ]; then
    check_files_against_history
    run_sql_checks
    check_renamed_tables_are_gone
    check_second_migrate_is_noop
    check_edited_migration_is_detected
fi

echo
echo "Database V&V: $(passed_count) passed, $(failed_count) failed"
[ "$(failed_count)" -eq 0 ]
