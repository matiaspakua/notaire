#!/usr/bin/env python3
"""
Analyze and trace CU to RF requirements - Fixed version
"""

import csv
import json
import re
import glob
import os

CU_DIR = "docs/business/03_CU - Casos de Uso"
RF_CSV = "docs/business/01_RF - Requerimientos Funcionales/requerimientos.csv"

# Load RF requirements
rf_map = {}
with open(RF_CSV, 'r', encoding='utf-8') as f:
    reader = csv.reader(f)
    header = next(reader)
    for row in reader:
        if len(row) >= 2:
            rf_id = row[0].strip()
            rf_title = row[1].strip()
            rf_map[rf_id] = {'id': rf_id, 'title': rf_title, 'description': row[3].strip() if len(row) > 3 else ''}

print(f"Loaded {len(rf_map)} RF requirements")

# Mapping from legacy RF references to GitHub issue numbers
rf_legacy_mapping = {
    "1.1": "#4", "1.1.1": "#5", "1.1.2": "#6", "1.1.3": "#7", "1.1.4": "#8", "1.1.5": "#9",
    "1.2": "#10", "1.2.1": "#11", "1.2.2": "#12", "1.2.3": "#13",
    "1.3": "#14", "1.3.1": "#15", "1.3.2": "#16", "1.3.3": "#17", "1.3.4": "#18", "1.3.5": "#19",
    "1.4": "#20", "1.4.1": "#21", "1.4.2": "#22", "1.4.3": "#23",
    "1.5": "#24", "1.5.1": "#25", "1.5.2": "#26",
    "1.6": "#27", "1.6.1": "#28", "1.6.2": "#29", "1.6.3": "#30", "1.6.4": "#31",
    "1.7": "#32", "1.7.1": "#33", "1.7.2": "#34", "1.7.3": "#35",
    "1.8": "#36", "1.8.1": "#37",
    "2": "#38", "2.1": "#39", "2.2": "#40", "2.3": "#41", "2.4": "#42",
    "3": "#43", "3.1": "#44", "3.2": "#45", "3.3": "#46", "3.4": "#47",
    "4": "#48", "4.1": "#49", "4.2": "#50",
    "5": "#51", "5.1": "#52", "5.2": "#53", "5.3": "#54",
    "6": "#55", "6.1": "#56", "6.2": "#57", "6.3": "#58", "6.4": "#59", "6.5": "#60",
    "7": "#61", "7.1": "#62", "7.1.1": "#63", "7.1.2": "#64", "7.1.3": "#65",
    "7.2": "#66", "7.2.1": "#67", "7.2.2": "#68", "7.2.3": "#69",
    "8": "#70", "9": "#71", "10": "#72", "11": "#73", "12": "#74",
    "13": "#75", "14": "#76", "15": "#77", "16": "#78", "17": "#79", "18": "#80",
    "19": "#81", "20": "#82", "21": "#83", "22": "#84", "23": "#85",
    "24": "#86", "25": "#87", "26": "#88", "27": "#89", "28": "#90", "29": "#91", "30": "#92", "31": "#93",
    "1.9": "#94", "1.9.1": "#95", "1.9.2": "#96", "1.9.3": "#97", "1.9.4": "#98", "1.9.5": "#99",
    "1.10": "#100", "1.10.1": "#101", "1.10.2": "#102", "1.10.3": "#103",
    "1.11": "#104", "1.11.1": "#105", "1.11.2": "#106",
    "1.12": "#107", "1.12.1": "#108", "1.12.2": "#109", "1.12.3": "#110",
    "1.13": "#111", "1.13.1": "#112",
    "1.14": "#113", "1.14.1": "#114", "1.14.2": "#115", "1.14.3": "#116",
    "1.15": "#117", "1.16": "#118", "1.17": "#119", "1.18": "#120", "1.19": "#121",
}

# Analyze CU markdown files
cu_refs = {}
md_files = glob.glob(os.path.join(CU_DIR, "CU*.md"))

for md_file in md_files:
    filename = os.path.basename(md_file)
    with open(md_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Fixed pattern - match the exact structure
    ref_match = re.search(r'<td><strong>Referencias Cruzadas:</strong></td>\s*<td>([^<]+)</td>', content)
    
    if ref_match:
        refs_text = ref_match.group(1).strip()
        rf_refs = re.findall(r'RF\s*([0-9.]+)', refs_text, re.IGNORECASE)
        cu_refs_match = re.findall(r'CU(\d+)', refs_text, re.IGNORECASE)
        cu_refs[filename] = {'rf_refs': rf_refs, 'cu_refs': cu_refs_match}

print(f"\n=== CU Cross-References ({len(cu_refs)} files) ===")
for cu, refs in list(cu_refs.items())[:15]:
    print(f"{cu}: RFs={refs['rf_refs']}, CUs={refs['cu_refs']}")

# Generate traceability
traces = []
for cu, refs in cu_refs.items():
    for rf_ref in refs['rf_refs']:
        if rf_ref in rf_legacy_mapping:
            rf_id = rf_legacy_mapping[rf_ref]
            if rf_id in rf_map:
                traces.append({
                    'cu_file': cu,
                    'rf_ref': rf_ref,
                    'rf_id': rf_id,
                    'rf_title': rf_map[rf_id]['title']
                })

print(f"\n=== Found {len(traces)} CU->RF traces ===")
for t in traces[:25]:
    print(f"  {t['cu_file']} -> {t['rf_id']}: {t['rf_title']}")

# Save
with open("cu_rf_traceability.json", "w", encoding="utf-8") as f:
    json.dump({'traces': traces, 'summary': {'total_cu_with_refs': len(cu_refs), 'total_traces': len(traces)}}, f, indent=2)

print(f"\n=== Summary ===")
print(f"CUs with RF references: {len(cu_refs)}")
print(f"Total traces: {len(traces)}")