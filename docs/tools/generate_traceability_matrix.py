#!/usr/bin/env python3
"""
Regenerate the requirement <-> use-case traceability matrix (#2067 / CU76).

    python3 docs/tools/generate_traceability_matrix.py           # rewrite the matrix
    python3 docs/tools/generate_traceability_matrix.py --check   # exit 1 when it is stale

The matrix is derived, never hand-edited: every `RF #N` in a use case's "Referencias Cruzadas" row is
a link, and requerimientos.csv names each requirement. The November 2026 documentation audit found the
committed matrix stopped at CU82 although CU83-CU88 existed, because nothing regenerated it;
docs/tests/test_traceability_matrix_current.py now runs --check.
"""
import csv
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BUSINESS = ROOT / "docs" / "100-business"
REQUIREMENTS = BUSINESS / "101-requirements" / "requerimientos.csv"
USE_CASES = BUSINESS / "102-use-cases"
MATRIX = BUSINESS / "104-traceability" / "Matriz de Trazabilidad Requerimientos-Casos de Uso.md"

CROSS_REFERENCES = re.compile(r"^\|\s*\*\*Referencias Cruzadas\*\*\s*\|(.*)\|\s*$", re.MULTILINE)
RF = re.compile(r"RF\s*#(\d+)")
RNF = re.compile(r"\bRNF[- ]?(\d+)")
USE_CASE = re.compile(r"^(CU\d+)\s*[–-]\s*(.+)\.md$")


def requirements():
    with open(REQUIREMENTS, encoding="utf-8", newline="") as handle:
        rows = list(csv.reader(handle))[1:]
    return {int(row[0].lstrip("#")): row[1] for row in rows}


def use_cases():
    found = []
    for path in sorted(USE_CASES.glob("CU*.md")):
        match = USE_CASE.match(path.name)
        if not match:
            continue
        text = path.read_text(encoding="utf-8")
        refs = CROSS_REFERENCES.search(text)
        cell = refs.group(1) if refs else ""
        rf = sorted({int(n) for n in RF.findall(cell)})
        rnf = sorted({int(n) for n in RNF.findall(cell)})
        found.append((match.group(1), f"{match.group(1)} – {match.group(2)}", rf, rnf))
    return sorted(found, key=lambda item: int(item[0][2:]))


def render():
    catalog = requirements()
    cases = use_cases()
    by_requirement = {n: [] for n in catalog}
    dangling = []
    for code, _title, rf, _rnf in cases:
        for n in rf:
            if n in by_requirement:
                by_requirement[n].append(code)
            else:
                dangling.append(f"{code} → RF #{n}")
    uncovered = [n for n, codes in by_requirement.items() if not codes]
    lines = [
        "# Matriz de Trazabilidad Requerimiento <-> Caso de Uso",
        "",
        "Generada por `docs/tools/generate_traceability_matrix.py` a partir del campo **Referencias Cruzadas**",
        f"(`RF #N`) de los {len(cases)} archivos `CU*.md` (`docs/100-business/102-use-cases/`) y del catálogo",
        "`docs/100-business/101-requirements/requerimientos.csv`. No se edita a mano:",
        "`docs/tests/test_traceability_matrix_current.py` falla si queda desactualizada.",
        "",
        f"**Cobertura:** {len(catalog) - len(uncovered)} de {len(catalog)} requisitos del catálogo están referenciados",
        f"por al menos un caso de uso; {len(dangling)} referencias `RF #N` apuntan a un requisito inexistente.",
    ]
    if uncovered:
        lines += ["", "Requisitos sin caso de uso: " + ", ".join(f"#{n}" for n in uncovered) + "."]
    if dangling:
        lines += ["", "Referencias colgantes: " + ", ".join(dangling) + "."]
    no_rf = [code for code, _t, rf, _r in cases if not rf]
    if no_rf:
        lines += ["", "Casos de uso sin `RF #N` (citan solo requisitos no funcionales u otros casos de uso): " + ", ".join(no_rf) + "."]
    lines += ["", "## Requisito -> Casos de Uso", "", "| Requisito | Título | Casos de Uso |", "|---|---|---|"]
    for n in sorted(catalog):
        lines.append(f"| #{n} | {catalog[n]} | {', '.join(by_requirement[n]) or '—'} |")
    lines += ["", "## Caso de Uso -> Requisitos", "", "| Caso de Uso | Requisitos | No funcionales |", "|---|---|---|"]
    for _code, title, rf, rnf in cases:
        lines.append(f"| {title} | {', '.join(f'#{n}' for n in rf) or '—'} | {', '.join(f'RNF-{n}' for n in rnf) or '—'} |")
    return "\n".join(lines) + "\n"


def main(argv):
    text = render()
    if "--check" in argv:
        current = MATRIX.read_text(encoding="utf-8") if MATRIX.exists() else ""
        if current != text:
            print(f"stale: regenerate with python3 docs/tools/generate_traceability_matrix.py ({MATRIX.relative_to(ROOT)})")
            return 1
        print("traceability matrix is current")
        return 0
    MATRIX.write_text(text, encoding="utf-8")
    print(f"wrote {MATRIX.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
