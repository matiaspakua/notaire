#!/usr/bin/env python3
"""
Regenerate the ERD artifacts (#1021 / CU76) from the schema Flyway builds.

    python3 scripts/generate_erd.py            # rewrite .puml and .csv from the database
    python3 scripts/generate_erd.py --render   # also render both SVGs (needs plantuml + dot)

Connection: the standard libpq variables (PGHOST, PGPORT, PGUSER, PGPASSWORD, PGDATABASE).
The database must be migrated by Flyway; this script never changes it. Rendering uses the
`plantuml` command or the jar named in PLANTUML_JAR.
"""
import csv
import json
import os
import re
import shutil
import subprocess
import sys
from collections import OrderedDict
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
DATA_MODEL = REPO_ROOT / "docs" / "200-architecture" / "205-data-model"
ERD_DIR = DATA_MODEL / "ERD"
DICTIONARY = DATA_MODEL / "Diccionario de Datos.md"
FULL_PUML = ERD_DIR / "ERD-Escribania_completo.puml"
SIMPLE_PUML = ERD_DIR / "ERD.puml"
CSV_FILE = ERD_DIR / "Modelo Relacional Escribania - Entidades.csv"

PACKAGES = OrderedDict([
    ("Subjects and Security", ["people", "identification_types", "substitutions", "users", "roles", "role_modules",
                               "audit_records"]),
    ("Budgeting and Payments", ["budgets", "concepts", "budget_templates", "items", "payments"]),
    ("Deed Management and Workflows", ["deed_managements", "management_statuses", "history", "procedure_types",
                                       "procedures", "person_procedures", "procedure_folders", "properties",
                                       "workflow_definition", "workflow_node", "workflow_transition"]),
    ("Documentation and Certificates", ["document_types", "procedure_templates", "document_cost_templates",
                                        "submitted_documents"]),
    ("Protocols, Deeds and Registration", ["deeds", "notebooks", "folio_types", "folios", "testimonies",
                                           "testimony_movements", "copies", "folio_copies", "registration_drafts"]),
])
TYPE_MAP = {
    "integer": ("INT", "Entero"), "bigint": ("BIGINT", "Entero"), "smallint": ("INT", "Entero"),
    "text": ("TEXT", "Texto"), "character varying": ("TEXT", "Texto"), "character": ("TEXT", "Texto"),
    "date": ("DATE", "Fecha"), "boolean": ("BOOLEAN", "Lógico"),
    "timestamp without time zone": ("TIMESTAMP", "Fecha/Hora"), "timestamp with time zone": ("TIMESTAMP", "Fecha/Hora"),
    "real": ("REAL", "Real"), "double precision": ("REAL", "Real"), "numeric": ("NUMERIC", "Real"),
}
ENTITY_TYPES = {"Fuerte", "Débil", "Asociativa"}

QUERY = """
select json_build_object(
  'columns', (select json_agg(c order by c.table_name, c.ordinal_position) from (
      select table_name, column_name, data_type, is_nullable = 'YES' as nullable,
             coalesce(column_default like 'nextval%%', false) as generated, ordinal_position
      from information_schema.columns
      where table_schema = 'public' and table_name <> 'flyway_schema_history'
        and table_name in (select table_name from information_schema.tables
                           where table_schema = 'public' and table_type = 'BASE TABLE')) c),
  'constraints', (select json_agg(k) from (
      select rel.relname as table_name, con.contype as kind, con.conname as name,
             (select json_agg(a.attname order by u.ord) from unnest(con.conkey) with ordinality u(attnum, ord)
              join pg_attribute a on a.attrelid = con.conrelid and a.attnum = u.attnum) as columns,
             fr.relname as ref_table,
             (select json_agg(a.attname order by u.ord) from unnest(con.confkey) with ordinality u(attnum, ord)
              join pg_attribute a on a.attrelid = con.confrelid and a.attnum = u.attnum) as ref_columns
      from pg_constraint con
      join pg_class rel on rel.oid = con.conrelid
      join pg_namespace ns on ns.oid = rel.relnamespace and ns.nspname = 'public'
      left join pg_class fr on fr.oid = con.confrelid
      where con.contype in ('p', 'f', 'u')) k)
)
"""


def load_schema():
    out = subprocess.run(["psql", "-X", "-At", "-c", QUERY], capture_output=True, text=True, check=True).stdout
    raw = json.loads(out)
    tables = OrderedDict()
    for col in raw["columns"]:
        tables.setdefault(col["table_name"], OrderedDict())[col["column_name"]] = col
    pks, fks, uniques = {}, [], {}
    for con in raw["constraints"]:
        if con["kind"] == "p":
            pks[con["table_name"]] = con["columns"]
        elif con["kind"] == "u" and len(con["columns"]) == 1:
            uniques.setdefault(con["table_name"], set()).add(con["columns"][0])
        elif con["kind"] == "f" and len(con["columns"]) == 1:
            fks.append(con)
    return tables, pks, fks, uniques


def parse_dictionary():
    text = DICTIONARY.read_text(encoding="utf-8")
    entity_types, descriptions = {}, {}
    for line in text.splitlines():
        match = re.match(r"\|\s*\d+\s*\|\s*\[(\w+)\]\([^)]*\)\s*\|[^|]*\|\s*([^|]+?)\s*\|", line)
        if match and match.group(2) in ENTITY_TYPES:
            entity_types[match.group(1)] = match.group(2)
    table = None
    for line in text.splitlines():
        heading = re.match(r"###\s+\d+\.\s+`(\w+)`", line)
        if heading:
            table = heading.group(1)
            continue
        row = re.match(r"\|\s*`(\w+)`\s*\|(.*)\|\s*$", line)
        if table and row:
            cells = [c.strip() for c in row.group(2).split("|")]
            descriptions[(table, row.group(1))] = cells[-1] if cells else ""
    return entity_types, descriptions


def puml_type(column):
    return TYPE_MAP.get(column["data_type"], (column["data_type"].upper(), "Texto"))[0]


def entity_type(table, columns, pk, fk_columns, dictionary_types):
    if table in dictionary_types:
        return dictionary_types[table]
    if pk and all(c in fk_columns for c in pk):
        return "Asociativa"
    return "Fuerte"


def full_entity(table, columns, pk, fk_columns, uniques):
    lines = [f'  entity "{table}" as {table} {{']
    ordered = [c for c in columns if c in pk] + [c for c in columns if c not in pk and c != "version"]
    for index, name in enumerate(ordered):
        column = columns[name]
        marks = []
        if name in pk:
            marks.append("PK")
            if column["generated"]:
                marks.append("generated")
        if name in fk_columns:
            marks.append("FK")
        if name in uniques.get(table, ()):
            marks.append("UNIQUE")
        star = "* " if not column["nullable"] else ""
        suffix = f" <<{', '.join(marks)}>>" if marks else ""
        lines.append(f"    {star}{name} : {puml_type(column)}{suffix}")
        if index + 1 == len(pk) and pk:
            lines.append("    --")
    if "version" in columns:
        lines.append("    version : INT")
    lines.append("  }")
    return "\n".join(lines)


def relationship(fk, columns_by_table, uniques, pk_by_table):
    child, parent = fk["table_name"], fk["ref_table"]
    column = fk["columns"][0]
    nullable = columns_by_table[child][column]["nullable"]
    is_one = column in uniques.get(child, ()) or pk_by_table.get(child) == [column]
    left = "|o" if nullable else "||"
    right = "o|" if is_one else "o{"
    return f'{parent} {left}--{right} {child} : "{column}"'


def header(name, title):
    return f"""@startuml {name}
!theme plain
top to bottom direction
skinparam linetype ortho
skinparam nodesep 90
skinparam ranksep 90
skinparam packagePadding 35
skinparam minClassWidth 135
skinparam roundcorner 6
skinparam shadowing false
skinparam packageStyle rectangle

skinparam class {{
    BackgroundColor #FFFFFF
    ArrowColor #2C3E50
    BorderColor #34495E
    FontSize 12
}}

skinparam package {{
    BackgroundColor #F8F9FA
    BorderColor #BDC3C7
    FontColor #2C3E50
    FontStyle bold
    FontSize 13
}}

' =============================================================================
' {title}
' Generated by scripts/generate_erd.py from the schema Flyway builds (ADR-007).
' Do not edit by hand: change a migration and regenerate.
' =============================================================================
"""


def grouped(tables):
    known = {t for members in PACKAGES.values() for t in members}
    missing = sorted(set(tables) - known)
    if missing:
        raise SystemExit(f"tables without a package, add them to PACKAGES: {missing}")
    return [(name, [t for t in members if t in tables]) for name, members in PACKAGES.items()]


def write_full(tables, pks, fks, uniques):
    fk_by_table = {}
    for fk in fks:
        fk_by_table.setdefault(fk["table_name"], set()).add(fk["columns"][0])
    parts = [header("ERD_Escribania_completo", "NOTAIRE — FULL ERD (ALL TABLES, ALL COLUMNS)")]
    for package, members in grouped(tables):
        parts.append(f'package "{package}" {{')
        for table in members:
            parts.append(full_entity(table, tables[table], pks.get(table, []), fk_by_table.get(table, set()), uniques))
            parts.append("")
        parts[-1] = "}"
        parts.append("")
    parts.append("' RELATIONSHIPS AND CARDINALITIES (label = foreign-key column)")
    parts.extend(relationship(fk, tables, uniques, pks) for fk in sorted(fks, key=lambda f: (f["table_name"], f["columns"][0])))
    parts.append("\n@enduml")
    FULL_PUML.write_text("\n".join(parts) + "\n", encoding="utf-8")


def write_simple(tables, pks, fks, uniques):
    parts = [header("ERD_Escribania", "NOTAIRE — SIMPLIFIED ERD (ENTITIES AND RELATIONSHIPS)")]
    for package, members in grouped(tables):
        parts.append(f'package "{package}" {{')
        parts.extend(f'  entity "{table}" as {table}' for table in members)
        parts.append("}\n")
    parts.append("' RELATIONSHIPS AND CARDINALITIES")
    parts.extend(relationship(fk, tables, uniques, pks).rsplit(" : ", 1)[0] for fk in
                 sorted(fks, key=lambda f: (f["table_name"], f["columns"][0])))
    parts.append("\n@enduml")
    SIMPLE_PUML.write_text("\n".join(parts) + "\n", encoding="utf-8")


def write_csv(tables, pks, fks, uniques):
    dictionary_types, descriptions = parse_dictionary()
    fk_columns = {(fk["table_name"], fk["columns"][0]) for fk in fks}
    rows = [["Nombre Entidad", "Tipo Entidad", "Atributo", "Clave", "Null?", "Tipo_Dato", "PK", "FK", "NULL",
             "Observaciones"]]
    for _, members in grouped(tables):
        for table in members:
            pk = pks.get(table, [])
            kind = entity_type(table, tables[table], pk, {c for t, c in fk_columns if t == table}, dictionary_types)
            for name, column in tables[table].items():
                is_pk, is_fk = name in pk, (table, name) in fk_columns
                key = "PK" if is_pk else ("FK" if is_fk else "")
                data_type = TYPE_MAP.get(column["data_type"], ("", "Texto"))[1]
                rows.append([table, kind, name, key, "si" if column["nullable"] else "no", data_type,
                             "x" if is_pk else "", "x" if is_fk else "", "x" if column["nullable"] else "",
                             descriptions.get((table, name), "")])
    with open(CSV_FILE, "w", encoding="utf-8", newline="") as handle:
        csv.writer(handle, lineterminator="\n").writerows(rows)


def render():
    command = shutil.which("plantuml")
    runner = [command] if command else ["java", "-jar", os.environ.get("PLANTUML_JAR", "plantuml.jar")]
    for source in (FULL_PUML, SIMPLE_PUML):
        subprocess.run([*runner, "-tsvg", "-charset", "UTF-8", str(source)], check=True)


def main():
    tables, pks, fks, uniques = load_schema()
    write_full(tables, pks, fks, uniques)
    write_simple(tables, pks, fks, uniques)
    write_csv(tables, pks, fks, uniques)
    if "--render" in sys.argv:
        render()
    print(f"{len(tables)} tables, {len(fks)} foreign keys")


if __name__ == "__main__":
    main()
