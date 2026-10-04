#!/usr/bin/env python3
"""
Regenerate the structural parts of the Diccionario de Datos (#1222 / CU76) from the Flyway schema.

    python3 scripts/generate_data_dictionary.py

Rewrites the table index (section 2), the per-table column tables (section 4) and the referential
matrix (section 5). Human-written text is read from the existing file and kept: table summaries,
package and entity type, and column descriptions. Anything without one is written as `TODO`, which
scripts/test_data_dictionary_sync.py rejects, so a migration forces a description.

Connection: the standard libpq variables (PGHOST, PGPORT, PGUSER, PGPASSWORD, PGDATABASE), against a
database migrated by Flyway. The database is never changed.
"""
import re
from collections import OrderedDict

from generate_erd import DICTIONARY, load_schema

TODO = "TODO"
ON_DELETE = {"a": "NO ACTION", "r": "RESTRICT", "c": "CASCADE", "n": "SET NULL", "d": "SET DEFAULT"}
COLUMN_HEADER = ("| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |\n"
                 "|---|---|---|---|---|---|---|---|")
INDEX_HEADER = ("| Nº | Tabla | Paquete / Módulo | Tipo Entidad | Descripción |\n|---|---|---|---|---|")
MATRIX_HEADER = ("| Tabla Origen | Columna FK | Tabla Destino | Columna PK | Acción ON DELETE |\n"
                 "|---|---|---|---|---|")


def split_sections(text):
    parts, current = OrderedDict(), None
    for line in text.split("\n"):
        match = re.match(r"^##\s+(\d+)\.", line)
        if match:
            current = int(match.group(1))
        parts.setdefault(current, []).append(line)
    return parts


def read_human_text(sections):
    index = OrderedDict()
    for line in sections[2]:
        match = re.match(r"\|\s*\d+\s*\|\s*\[(\w+)\]\([^)]*\)\s*\|(.*)\|\s*$", line)
        if match:
            index[match.group(1)] = [c.strip() for c in match.group(2).split("|")]
    summaries, descriptions, table, expect_summary = {}, {}, None, False
    for line in sections[4]:
        heading = re.match(r"###\s+\d+\.\s+`(\w+)`", line)
        if heading:
            table, expect_summary = heading.group(1), True
            continue
        row = re.match(r"\|\s*`(\w+)`\s*\|(.*)\|\s*$", line)
        if table and row:
            descriptions[(table, row.group(1))] = row.group(2).split("|")[-1].strip()
        elif table and expect_summary and line.strip():
            summaries[table], expect_summary = line.strip(), False
    return index, summaries, descriptions


def sql_type(column):
    name, length = column["data_type"], column.get("length")
    if column["generated"]:
        return "SERIAL (INT)"
    if name == "character varying":
        return f"VARCHAR({length})"
    if name == "numeric":
        return f"NUMERIC({column['precision']},{column['scale']})"
    return {"integer": "INTEGER", "timestamp without time zone": "TIMESTAMP"}.get(name, name.upper())


def default_text(column):
    if column["generated"]:
        return "Auto"
    default = column.get("default")
    if default is None:
        return "NULL" if column["nullable"] else "—"
    return re.sub(r"::[a-z ]+$", "", default)


def column_row(table, name, column, pks, fks, descriptions):
    reference = fks.get((table, name))
    cells = [f"`{name}`", sql_type(column), "Sí" if name in pks.get(table, []) else "No",
             "Sí" if reference else "No", "No" if column["nullable"] else "Sí", default_text(column),
             f"`{reference['ref_table']}({reference['ref_columns'][0]})`" if reference else "—",
             descriptions.get((table, name)) or TODO]
    return "| " + " | ".join(cells) + " |"


def ordered_columns(columns, pk):
    first = [c for c in columns if c in pk] + (["version"] if "version" in columns and "version" not in pk else [])
    return first + [c for c in columns if c not in first]


def table_order(tables, index):
    known = [t for t in index if t in tables]
    return known + sorted(set(tables) - set(known))


def render_index(order, index):
    rows = [f"| {n} | [{t}](#{n}-{t}) | " + " | ".join(index.get(t, [TODO, TODO, TODO])) + " |"
            for n, t in enumerate(order, 1)]
    return [INDEX_HEADER, *rows]


def render_tables(order, tables, pks, fks, summaries, descriptions):
    out = []
    for number, table in enumerate(order, 1):
        out += [f"### {number}. `{table}`", "", summaries.get(table, f"{TODO}: describir la tabla."), "", COLUMN_HEADER]
        out += [column_row(table, name, tables[table][name], pks, fks, descriptions)
                for name in ordered_columns(tables[table], pks.get(table, []))]
        out += ["", "---", ""]
    return out


def render_matrix(order, tables, fk_list, pks):
    position = {t: i for i, t in enumerate(order)}
    ordered = sorted(fk_list, key=lambda f: (position[f["table_name"]], list(tables[f["table_name"]]).index(
        f["columns"][0])))
    rows = [f"| `{f['table_name']}` | `{f['columns'][0]}` | `{f['ref_table']}` | `{f['ref_columns'][0]}` | "
            f"{ON_DELETE[f['on_delete']]} |" for f in ordered]
    return [MATRIX_HEADER, *rows]


def replace_table(lines, new_table):
    first = next(i for i, line in enumerate(lines) if line.startswith("|"))
    last = max(i for i, line in enumerate(lines) if line.startswith("|"))
    return lines[:first] + new_table + lines[last + 1:]


def main():
    tables, pks, fks, _ = load_schema()
    text = DICTIONARY.read_text(encoding="utf-8")
    sections = split_sections(text)
    index, summaries, descriptions = read_human_text(sections)
    order = table_order(tables, index)
    fk_by_column = {(f["table_name"], f["columns"][0]): f for f in fks}
    sections[2] = replace_table(sections[2], render_index(order, index))
    sections[2] = [re.sub(r"\(\d+ tablas,", f"({len(order)} tablas,", line) for line in sections[2]]
    sections[4] = sections[4][:next(i for i, l in enumerate(sections[4]) if l.startswith("### "))] + \
        render_tables(order, tables, pks, fk_by_column, summaries, descriptions)
    sections[5] = replace_table(sections[5], render_matrix(order, tables, fks, pks))
    DICTIONARY.write_text("\n".join(line for part in sections.values() for line in part), encoding="utf-8")
    print(f"{len(order)} tables, {len(fks)} foreign keys")


if __name__ == "__main__":
    main()
