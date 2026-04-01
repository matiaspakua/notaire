#!/usr/bin/env python3
"""
Update markdown files with GitHub issue IDs
"""

import json
import os
import re
import glob

MAPPING_FILE = "cu_issues_mapping.json"
CU_DIR = "docs/business/03_CU - Casos de Uso"

# Load mapping
with open(MAPPING_FILE, "r", encoding="utf-8") as f:
    issues_map = json.load(f)

def normalize_key(key):
    """Normalize CU name to match mapping keys"""
    # Remove extra spaces and normalize dash
    key = key.strip()
    key = key.replace(" – ", " – ").replace(" - ", " - ")
    return key

# Build normalized mapping
normalized_map = {}
for k, v in issues_map.items():
    # Normalize the key
    normalized = normalize_key(k)
    normalized_map[normalized] = v

# Find all markdown files
md_files = glob.glob(os.path.join(CU_DIR, "CU*.md"))

print(f"Found {len(md_files)} markdown files")

# Update each file
for md_file in md_files:
    filename = os.path.basename(md_file)
    
    # Extract CU number from filename (e.g., "CU01 – Preparar Presupuesto.md")
    # Match pattern: CU## – or CU## - followed by name
    match = re.match(r'(CU\d+)\s*[-–]\s*(.+)\.md', filename)
    if not match:
        print(f"Skipping (no match): {filename}")
        continue
    
    cu_num = match.group(1)
    cu_name_pattern = match.group(2).strip()
    
    # Try to find matching key in mapping
    issue_num = None
    
    # Try different patterns
    for key in normalized_map.keys():
        if cu_num in key:
            issue_num = normalized_map[key]
            print(f"Matched {filename} -> #{issue_num} ({key})")
            break
    
    if not issue_num:
        print(f"No mapping found for: {filename}")
        continue
    
    # Read the file
    with open(md_file, "r", encoding="utf-8") as f:
        content = f.read()
    
    # Check if GitHub_ID already exists
    if "GitHub_ID" in content or f"github.com/matiaspakua/notaire/issues" in content:
        print(f"  Already has GitHub reference, skipping")
        continue
    
    # Add GitHub_ID row at the end of the first table (before </tbody></table>)
    # Look for the first </tbody></table> and add before it
    github_row = f'''<tr class="odd">
<td><strong>GitHub_ID:</strong></td>
<td><a href="https://github.com/matiaspakua/notaire/issues/{issue_num}">#{issue_num}</a></td>
</tr>'''
    
    # Find the position to insert (after last </tr> before </tbody>)
    # Use regex to find the last </tr> before </tbody>
    pattern = r'(</tr>\s*</tbody>)'
    match = re.search(pattern, content)
    
    if match:
        content = content[:match.start()] + github_row + "\n" + content[match.start():]
        
        # Write back
        with open(md_file, "w", encoding="utf-8") as f:
            f.write(content)
        
        print(f"  Updated: {filename} -> #{issue_num}")
    else:
        print(f"  Could not find insertion point in: {filename}")

print("\nDone!")