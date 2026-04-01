#!/usr/bin/env python3
"""
Add GitHub issues to project board #3 using GraphQL API
"""

import subprocess
import json
import time

PROJECT_ID = "PVT_kwHOATJKDs4BSlEC"
ISSUES = list(range(154, 222))  # 154 to 221

def run_gh_api(query):
    """Run GitHub GraphQL API"""
    result = subprocess.run(
        ["gh", "api", "graphql", "-f", f"query={query}"],
        capture_output=True,
        text=True
    )
    return result.stdout

# Add issues to project
added = []
failed = []

for issue_num in ISSUES:
    issue_node_id = f"I_ADOPTED_ISSUE_{issue_num}"  # Need to get actual node ID
    
    # Actually, we need to use the issue's node ID, not the database ID
    # Let's query for the issue first to get its node ID
    get_issue_query = f'''
    {{
      repository(owner: "matiaspakua", name: "notaire") {{
        issue(number: {issue_num}) {{
          id
        }}
      }}
    }}
    '''
    
    result = run_gh_api(get_issue_query)
    try:
        data = json.loads(result)
        issue_id = data['data']['repository']['issue']['id']
        
        # Add to project
        add_query = f'''
        mutation {{
          addProjectV2ItemById(input: {{projectId: "{PROJECT_ID}", contentId: "{issue_id}"}}) {{
            projectItem {{
              id
            }}
          }}
        }}
        '''
        
        result = run_gh_api(add_query)
        print(f"Added issue #{issue_num}")
        added.append(issue_num)
        time.sleep(0.5)  # Rate limiting
        
    except Exception as e:
        print(f"Failed for #{issue_num}: {e}")
        failed.append(issue_num)

print(f"\n=== Summary ===")
print(f"Added: {len(added)}")
print(f"Failed: {len(failed)}")
if failed:
    print(f"Failed issues: {failed}")