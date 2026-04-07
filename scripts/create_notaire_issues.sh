#!/bin/bash

# Create GitHub issues for notaire documentation wiki migration
# Run this from the notaire repository directory or specify --repo matiaspakua/notaire

REPO="matiaspakua/notaire"

echo "Creating GitHub issues for notaire documentation wiki migration..."

# Issue 1: README.md rewrite
gh issue create --repo $REPO \
  --title "README.md: Complete rewrite with project overview" \
  --body "**Priority: CRITICAL**

## Problem
Current README.md contains only 'Updated test' and provides zero context for new contributors and users.

## Solution
Rewrite README.md to include:
- Project purpose and mission (2-3 sentences)
- Key features and capabilities
- Quick start guide (dev setup)
- Architecture overview diagram link
- Links to main documentation (wiki, API docs, ops guide)
- Contributing guidelines link
- Technology stack summary

## Acceptance Criteria
- [ ] README is comprehensive but under 300 words
- [ ] Contains quick start for local dev setup
- [ ] Links to all major documentation
- [ ] Has architecture diagram reference
- [ ] Tested: new contributor can understand project in 5 minutes

## Estimate
2-3 hours" \
  --label "documentation,critical,wiki-migration,readme"

# Issue 2: Operations & Deployment Runbook
gh issue create --repo $REPO \
  --title "Operations Runbook: Deployment, monitoring, and incident response" \
  --body "**Priority: CRITICAL**

## Problem
Zero documentation for running notaire in production. Missing procedures for:
- Deployment pipeline and release process
- Health checks and monitoring setup
- Alerting configuration
- Backup and restore procedures
- Disaster recovery
- Incident response playbooks

## Solution
Create comprehensive ops runbook covering:
- Prerequisites (Java, PostgreSQL, environment variables)
- Deployment procedure (Docker, K8s, or standalone)
- Health check endpoints
- Monitoring stack integration
- Backup schedule and restore process
- Common incidents and resolution steps
- Scaling considerations
- Log aggregation setup

## Acceptance Criteria
- [ ] Covers all deployment scenarios
- [ ] Includes health check procedures
- [ ] Backup/restore tested
- [ ] At least 3 incident scenarios with resolution
- [ ] Ready for ops team handoff

## Estimate
6-8 hours

## Depends On
- README.md must reference this guide" \
  --label "documentation,critical,operations,wiki-migration,deployment"

# Issue 3: Service Layer Documentation
gh issue create --repo $REPO \
  --title "Document service layer: business logic, transaction boundaries, interactions" \
  --body "**Priority: HIGH**

## Problem
Service layer architecture is not documented. Missing context on:
- Service responsibilities and boundaries
- Transaction handling strategy
- Inter-service communication patterns
- DTO conversion strategy
- Error handling and exception mapping

## Solution
Create architecture documentation for service layer:
- High-level service diagram (PlantUML)
- Service descriptions and responsibilities
- Key service interfaces
- Transaction boundaries and propagation rules
- DTO mapping strategy
- Exception hierarchy and mapping
- Service examples (1-2 detailed walkthroughs)

## Acceptance Criteria
- [ ] All services have documented boundaries
- [ ] Transaction strategy is clear
- [ ] DTO mapping is explained
- [ ] At least 2 service examples with flow diagrams
- [ ] Code examples for key patterns

## Location
docs/wiki/architecture/service-layer.md

## Estimate
4-5 hours

## Depends On
- README.md with architecture overview" \
  --label "documentation,architecture,wiki-migration,service-layer"

# Issue 4: Security Documentation Expansion
gh issue create --repo $REPO \
  --title "Expand SECURITY.md: authentication, authorization, secrets management" \
  --body "**Priority: HIGH**

## Problem
SECURITY.md exists but is shallow. Missing critical details:
- API authentication and authorization strategy
- Secrets management (environment variables, vaults)
- Vulnerability disclosure process
- Security headers and CORS configuration
- Password policies
- Session management
- Audit logging

## Solution
Expand SECURITY.md to cover:
- API auth architecture (JWT, OAuth, etc.)
- Bearer token handling and refresh
- Role-based access control (RBAC) configuration
- Secrets management best practices
- Environment variable requirements (passwords, API keys)
- Vulnerability reporting process
- Security headers (HSTS, CSP, X-Frame-Options, etc.)
- Sensitive data handling
- Audit logging strategy

## Acceptance Criteria
- [ ] All auth flows documented
- [ ] Secrets management guide complete
- [ ] Vulnerability disclosure process defined
- [ ] Code examples for common auth patterns
- [ ] Ready for security audit

## Estimate
3-4 hours

## Depends On
- Operations runbook for environment variable section" \
  --label "documentation,security,wiki-migration"

# Issue 5: API Error Handling Guide
gh issue create --repo $REPO \
  --title "Document API error handling: exception hierarchy, HTTP status mapping, error responses" \
  --body "**Priority: MEDIUM**

## Problem
API error handling is not documented. Missing:
- Exception hierarchy and meaning
- HTTP status code mapping
- Error response schema
- How clients should handle different errors
- Validation error formatting

## Solution
Create error handling guide:
- Exception hierarchy diagram
- HTTP status code mapping table (400, 401, 403, 404, 409, 500, etc.)
- Standard error response format (JSON schema)
- Validation error response examples
- How to handle each error category
- Retry strategies for transient errors
- Logging error context

## Acceptance Criteria
- [ ] All exception types mapped to HTTP status
- [ ] Error response schema defined
- [ ] Code examples for each error type
- [ ] Client error handling guide included
- [ ] Validation error examples for common cases

## Location
docs/wiki/api/error-handling.md

## Estimate
2-3 hours

## Depends On
- API documentation (existing)" \
  --label "documentation,api,wiki-migration,error-handling"

# Issue 6: JasperReports Configuration Guide
gh issue create --repo $REPO \
  --title "Document JasperReports: parameters, triggers, output formats, examples" \
  --body "**Priority: MEDIUM**

## Problem
JasperReports feature is undocumented and undiscoverable. Missing:
- How to trigger each report
- Report parameters and valid values
- Output formats (PDF, Excel, etc.)
- Report configuration
- Adding new reports
- Scheduling/automation

## Solution
Create JasperReports guide:
- Report inventory (9 files identified)
- Parameter reference for each report
- API endpoint to trigger reports
- Output format options
- Example: trigger and download a report
- How to add a new report (step-by-step)
- Scheduling automation (if applicable)

## Acceptance Criteria
- [ ] All 9 reports documented
- [ ] Parameters explained with examples
- [ ] Step-by-step: add new report
- [ ] cURL examples for each report
- [ ] Screenshot of sample outputs

## Location
docs/wiki/features/reports.md

## Estimate
3-4 hours

## Depends On
- API testing guide (existing reference)" \
  --label "documentation,features,wiki-migration,reports"

# Issue 7: Architecture Decision Records (ADRs)
gh issue create --repo $REPO \
  --title "Create ADRs: key design decisions (Hibernate, DTO strategy, legacy cleanup)" \
  --body "**Priority: MEDIUM**

## Problem
Key design decisions are not documented. No record of:
- Why Hibernate + JPA pattern
- DTO naming and mapping strategy
- Decision to remove legacy 'jpa' module and timeline
- Async/batch processing approach
- Caching strategy

## Solution
Create 3-5 Architecture Decision Records (ADRs):
1. ADR-001: ORM Strategy (Hibernate vs Raw SQL)
2. ADR-002: DTO Naming and Mapping Pattern
3. ADR-003: Legacy Module Cleanup Timeline
4. ADR-004: Async Processing Strategy (if applicable)
5. ADR-005: Caching Strategy

## Acceptance Criteria
- [ ] Each ADR follows standard format
- [ ] Status, date, and decision maker documented
- [ ] Trade-offs and consequences clear
- [ ] Timeline for any planned changes
- [ ] Stored in docs/wiki/adr/

## Format
Use standard ADR template (Status, Context, Decision, Consequences, etc.)

## Location
docs/wiki/adr/

## Estimate
2-3 hours

## Depends On
- Service layer documentation for context" \
  --label "documentation,architecture,wiki-migration,adr"

# Issue 8: Set up GitHub Wiki structure
gh issue create --repo $REPO \
  --title "Set up GitHub Wiki: structure, sidebar, organization" \
  --body "**Priority: HIGH**

## Problem
GitHub Wiki needs structure before content migration.

## Solution
Create wiki structure:
- Home page with navigation index
- _Sidebar.md with organized menu
- Folders/sections:
  - Getting Started (setup, quick start)
  - Architecture (design, services, ADRs)
  - API (endpoints, testing, error handling)
  - Features (reports, use cases)
  - Operations (deployment, monitoring, incidents)
  - Security (auth, secrets, audit)
  - Contributing (development, testing)

## Acceptance Criteria
- [ ] _Sidebar.md reflects all sections
- [ ] Home page has clear navigation
- [ ] Folder structure created
- [ ] No orphaned pages
- [ ] Mobile-friendly navigation

## Estimate
2 hours

## Depends On
- All content should be ready before this is marked done" \
  --label "wiki,organization,high-priority"

# Issue 9: Migrate business requirements to wiki
gh issue create --repo $REPO \
  --title "Migrate business requirements: 68+ use cases to GitHub Wiki" \
  --body "**Priority: MEDIUM**

## Problem
Business requirements are in docs/business/ directory (68 files). Need to organize in wiki.

## Solution
Migrate and organize use cases:
- Categorize use cases by business domain/feature
- Create index/matrix for quick reference
- Format consistently in wiki
- Create cross-references
- Link from feature documentation

## Acceptance Criteria
- [ ] All 68 use cases accessible in wiki
- [ ] Organized by domain/feature (not just numbered)
- [ ] Index/quick reference created
- [ ] Formatting consistent
- [ ] Links working

## Location
docs/wiki/requirements/

## Estimate
4-5 hours (mostly organization, less creation)

## Depends On
- Wiki structure must exist first" \
  --label "documentation,wiki-migration,requirements"

# Issue 10: Migrate technical guides to wiki
gh issue create --repo $REPO \
  --title "Migrate technical guides: wiki, API testing, setup to GitHub Wiki" \
  --body "**Priority: MEDIUM**

## Problem
Technical guides scattered across docs/wiki/ and other locations. Need centralized organization.

## Solution
Migrate technical documentation:
- Development setup guide (existing)
- API testing guide (527 lines, existing)
- DevSecOps/CI-CD guide (existing)
- Claude Code integration (existing)
- Database schema and migrations (new?)
- Testing strategies (new section)

## Acceptance Criteria
- [ ] All existing guides migrated
- [ ] Cross-references updated
- [ ] Search-friendly organization
- [ ] Links to code examples working
- [ ] No duplicate content

## Location
docs/wiki/development/, docs/wiki/deployment/

## Estimate
3-4 hours

## Depends On
- Wiki structure must exist" \
  --label "documentation,wiki-migration,technical"

# Issue 11: Fix documentation typos and cleanup
gh issue create --repo $REPO \
  --title "Documentation housekeeping: fix typos and organize files" \
  --body "**Priority: LOW**

## Problem
Minor documentation issues:
- Typo: docs/business/09_templaes/ should be docs/business/09_templates/
- Mixed language docs (Spanish/English) need clearer organization
- Python tooling scripts (convert_usecases.py, etc.) lack documentation
- Legacy files (config.properties, log4j.properties) need cleanup notes

## Solution
- Rename 09_templaes folder
- Add language tags to docs (or separate by language)
- Document Python tooling scripts
- Add deprecation notes to legacy config files

## Acceptance Criteria
- [ ] Typos fixed
- [ ] Language organization clear
- [ ] Python scripts documented
- [ ] Legacy files have deprecation status

## Estimate
1-2 hours" \
  --label "documentation,cleanup,low-priority"

# Issue 12: Document Python tooling scripts
gh issue create --repo $REPO \
  --title "Document Python tooling: convert_usecases.py, create_cu_issues.py, etc." \
  --body "**Priority: LOW**

## Problem
Helper scripts in scripts/ directory lack documentation:
- convert_usecases.py - converts business docs to markdown?
- create_cu_issues.py - creates GitHub issues from use cases?
- Other utility scripts

## Solution
Create docs/wiki/tools/ with:
- Purpose of each script
- How to run each one
- Parameters and examples
- When to use which script
- Maintenance notes

## Acceptance Criteria
- [ ] All scripts documented
- [ ] Usage examples provided
- [ ] Dependencies listed
- [ ] Maintenance owner assigned

## Location
docs/wiki/tools/scripts.md

## Estimate
1-2 hours" \
  --label "documentation,tools,low-priority"

echo "✓ All GitHub issues created successfully!"
echo ""
echo "Next steps:"
echo "1. Review issues in GitHub"
echo "2. Assign to team members"
echo "3. Start with critical priority issues (README, Operations, Wiki structure)"
echo "4. Link dependent issues"
