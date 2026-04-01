#!/usr/bin/env python3
"""
Create GitHub Issues for all Use Cases and add to project board
"""

import csv
import subprocess
import time
import json
import re

CSV_FILE = "docs/business/03_CU - Casos de Uso/Progreso Sistema - CASOS DE USO.csv"
PROJECT_NUMBER = 3
OWNER = "matiaspakua"

def run_gh_command(args):
    """Run a gh command and return output"""
    cmd = ["gh"] + args
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"Error: {result.stderr}")
        return None
    return result.stdout.strip()

def create_issue(cu_name, modulo, grado, estado, progreso, observaciones):
    """Create a GitHub issue for a CU"""
    body = f"""**Caso de Uso:** {cu_name}

**Módulo:** {modulo}
**Grado:** {grado}
**Estado:** {estado}
**Progreso:** {progreso}%

**Referencias:** {observaciones if observaciones else 'N/A'}

---
*Generated from CSV - Full documentation in docs/business/03_CU - Casos de Uso/*"""
    
    output = run_gh_command([
        "issue", "create",
        "--repo", f"{OWNER}/notaire",
        "--title", cu_name,
        "--body", body
    ])
    
    if output and "github.com" in output:
        # Extract issue number from URL
        match = re.search(r'/issues/(\d+)$', output)
        if match:
            return int(match.group(1))
    return None

def add_to_project(issue_number):
    """Add issue to project board"""
    # Get project ID first
    project_info = run_gh_command(["project", "view", str(PROJECT_NUMBER), "--owner", OWNER, "--json", "id"])
    if not project_info:
        return False
    
    try:
        project_data = json.loads(project_info)
        project_id = project_data.get("id")
    except:
        print(f"Could not parse project info: {project_info}")
        return False
    
    # Add item to project
    result = run_gh_command([
        "project", "item-add",
        str(PROJECT_NUMBER),
        "--owner", OWNER,
        "--issue-id", f"PAI_{issue_number}"  # This format may vary
    ])
    
    # Alternative: use GraphQL
    if not result:
        # Try using gh api directly
        query = f'''
        mutation {{
          addProjectV2ItemById(input: {{projectId: "{project_id}", contentId: "I_ADOPTED_ISSUE_{issue_number}"}}) {{
            item {{
              id
            }}
          }}
        }}
        '''
        result = run_gh_command(["api", "graphql", "-f", f"query={query}"])
    
    return result is not None

def main():
    # Read CSV
    issues_map = {}
    
    with open(CSV_FILE, 'r', encoding='utf-8') as f:
        reader = csv.reader(f)
        header = next(reader)  # Skip header
        
        for row in reader:
            if len(row) < 6:
                continue
            
            cu = row[0].strip()
            modulo = row[1].strip()
            grado = row[2].strip()
            estado = row[3].strip()
            progreso = row[4].strip()
            observaciones = row[-1].strip() if len(row) > 10 else ""
            
            print(f"Processing: {cu}")
            
            # Create issue
            issue_num = create_issue(cu, modulo, grado, estado, progreso, observaciones)
            
            if issue_num:
                print(f"  Created issue #{issue_num}")
                issues_map[cu] = issue_num
                
                # Add to project
                time.sleep(0.5)  # Rate limiting
            else:
                print(f"  Failed to create issue")
            
            time.sleep(1)  # Rate limiting
    
    print("\n=== Summary ===")
    for cu, issue_num in issues_map.items():
        print(f"{cu}: #{issue_num}")
    
    # Save mapping for later use
    with open("cu_issues_mapping.json", "w") as f:
        json.dump(issues_map, f, indent=2)
    
    print(f"\nMapping saved to cu_issues_mapping.json")
    print(f"Total issues created: {len(issues_map)}")

if __name__ == "__main__":
    main()