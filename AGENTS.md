# AGENTS.md - Agent Coding Guidelines for Notaire

This file provides essential information for agentic coding agents operating in the Notaire repository.

## Project Overview

Multi-module Maven project refactoring a Java Swing monolith to microservices:
- **backend-api**: Spring Boot REST API (Java 21, PostgreSQL)
- **frontend-swing**: Swing GUI client (REST client only)
- **notaire-shared**: Shared DTOs and common code

## Build Commands

```bash
# Build entire project
mvn clean install

# Build specific module with dependencies (-am builds dependencies too)
mvn clean install -pl backend-api -am

# Package for deployment
mvn clean package
```

## Testing Commands

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl backend-api
mvn test -pl frontend-swing

# Single test class
mvn test -Dtest=PresupuestoEntityTest

# Single test method
mvn test -Dtest=PresupuestoEntityTest#shouldCreatePresupuestoWithRequiredFields

# Test pattern matching
mvn test -Dtest="*ControllerTest"
mvn test -Dtest="*ServiceTest,*RepositoryTest"

# Run unit tests only
mvn test -Dtest="**/unit/*"

# Run integration tests only
mvn test -Dtest="**/integration/*"

# Check JaCoCo coverage (80% minimum required)
mvn jacoco:check -pl backend-api
mvn jacoco:report -pl backend-api

# HTTP API integration tests (requires running API)
bash scripts/test.sh
```

## Code Quality & Linting

```bash
# Run all checks (tests + static analysis)
mvn verify -pl backend-api

# Checkstyle
mvn checkstyle:check -pl backend-api

# SpotBugs (requires Java 21 locally)
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false

# Full site generation with all reports
mvn site -pl backend-api
```

## Application Commands

```bash
# Start application and database
bash scripts/start.sh

# Stop application
bash scripts/stop.sh

# View logs
bash scripts/logs.sh

# Run backend directly
cd backend-api && mvn spring-boot:run

# Access Swagger UI: http://localhost:8080/swagger-ui.html
```

## Java Code Style Guidelines

### General Conventions
- **Java Version**: 21
- **Indentation**: 4 spaces (no tabs)
- **Line Limit**: 120 characters
- **Braces**: Same line, always use braces for control blocks
- **Spacing**: Space after keywords (`if ()`, `while ()`, `for ()`), spaces around operators

### Naming Conventions
| Element | Convention | Example |
|---------|-----------|---------|
| Classes | PascalCase | `UsuarioController`, `PresupuestoService` |
| Methods/variables | camelCase | `isLoading`, `hasError`, `getActiveUsers` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Packages | lowercase | `com.licensis.notaire.api` |
| DTOs | DtoEntityName | `DtoUsuario`, `DtoPersona` |
| Tests | *Test suffix | `PresupuestoEntityTest` |
| Test methods | shouldXxxYyy | `shouldCreatePresupuestoWithRequiredFields` |

### Imports
- **No wildcard imports** (e.g., `import java.util.*`)
- **Import order**: java → javax → third-party → own packages

### Critical Java Pitfalls (MUST AVOID)
- `==` compares references, not content — use `.equals()` for strings
- Override `equals()` must also override `hashCode()` — HashMap/HashSet break
- `Optional.get()` throws if empty — use `orElse()`, `orElseGet()`, `ifPresent()`
- Modifying while iterating throws `ConcurrentModificationException` — use Iterator.remove()
- Unboxing null throws NPE — `Integer i = null; int x = i;` crashes
- `Integer == Integer` uses reference for values outside -128 to 127 — use `.equals()`
- Try-with-resources for AutoCloseable — implement `AutoCloseable`, Java 7+

### Error Handling
- Return `ResponseEntity` with appropriate status codes (200, 201, 400, 404, 500)
- Use SLF4J `Logger`, parameterized logging (`log.info("msg {}", var)`)
- **Never ignore exceptions silently**
- Return empty collections, not null (`Collections.emptyList()`)
- Use `Optional<T>` for nullable returns

### Architecture Packages
- **Backend**: `com.licensis.notaire.{api,service,jpa,negocio,dto}`
- **Frontend**: `com.licensis.notaire.gui` (REST client only, no business logic)

### REST API Design
- **URL**: `/api/v1/resource` (plural nouns)
- **HTTP methods**: GET (read), POST (create), PUT (update), DELETE (delete)
- Use `@Operation` and `@Tag` from springdoc-openapi

### Testing Guidelines
- **Pattern**: AAA (Arrange-Act-Assert)
- **Assertions**: AssertJ fluent API (`assertThat(...).isEqualTo(...)`)
- **Organization**: Use `@Nested` classes for related tests
- **Naming**: `@DisplayName` + descriptive method names
- **Coverage**: Minimum 80% (JaCoCo enforces this)

### Database
- **Engine**: PostgreSQL 15 in Docker
- **ORM**: Spring Data JPA with EclipseLink
- **Entities**: Implement `equals()`/`hashCode()` based on ID

## Frontend Design System (Next.js/React)

**MANDATORY**: All frontend forms must follow the centralized Apple-inspired design system.

### Design System Files

- `frontend/src/theme/tokens.ts` — Single source of truth for colors, spacing, typography, shadows
- `frontend/src/theme/index.ts` — Theme utilities and hooks
- `frontend/src/theme/form-patterns.tsx` — Reusable form component patterns
- `docs/02-architecture/03-design/DESIGN-SYSTEM.md` — Complete design system documentation

### Form Component Pattern

Every form must follow this structure:

```tsx
import { 
  FormContainer, 
  FormField, 
  FormSection, 
  FormActions,
  FormHeader 
} from "@/theme/form-patterns";

export function MyForm() {
  return (
    <FormContainer>
      <FormHeader title="Title" description="Description" />
      
      <FormSection title="Section 1">
        <FormField label="Label" required>
          <Input placeholder="..." />
        </FormField>
      </FormSection>

      <FormActions align="right">
        <Button variant="secondary">Cancel</Button>
        <Button variant="default">Submit</Button>
      </FormActions>
    </FormContainer>
  );
}
```

### Theme Token Usage

Always use theme tokens, never hardcode values:

```typescript
import { theme } from "@/theme/tokens";

// Colors
theme.colors.primary[600]           // Apple blue
theme.colors.neutral[900]           // Dark gray (text)
theme.semantic.form.inputBorder     // Form input border

// Spacing
theme.spacing[4]                    // 16px (form field gap)
theme.spacing[8]                    // 32px (card padding)

// Typography
theme.typography.fontSize.base      // 16px
theme.typography.fontWeight.semibold // 600
theme.borderRadius.lg               // 16px

// Shadows
theme.shadows.sm                    // Subtle shadow
theme.shadows.md                    // Card shadow
```

### UI Component Standards

| Component | Height | Border Radius | Spacing |
|-----------|--------|---------------|---------|
| Input | 48px | 12px | 16px padding |
| Button | 40px (md) / 48px (lg) | 12px | 12-20px padding |
| Card | Auto | 28px | 32px padding |
| Form Section Gap | N/A | N/A | 24px between sections |

### Key Rules

- ✅ Use semantic colors: `theme.semantic.form.*`, `theme.semantic.button.*`
- ✅ All inputs must have labels (above, not placeholder)
- ✅ Form fields must be grouped in `FormSection` components
- ✅ Button actions must be in `FormActions` with proper alignment
- ✅ Show validation errors below inputs with red color
- ✅ Disable button during submission to prevent duplicates
- ✅ Test responsive: 320px (mobile), 768px (tablet), 1024px (desktop)
- ✅ Verify color contrast (4.5:1 minimum) and keyboard navigation
- ❌ Never hardcode colors, spacing, or dimensions
- ❌ Never use placeholder as form label
- ❌ Never mix component patterns
- ❌ Don't add custom styling; use theme tokens

### References

- **UI/UX Rules**: `@.claude/rules/ui-ux-design.md`
- **Frontend Design Skill**: `@.claude/skills/frontend-design/SKILL.md`
- **Design System Doc**: `docs/02-architecture/03-design/DESIGN-SYSTEM.md`

## Prohibited Patterns
- Backend: Swing dependencies, direct database access from controllers
- Frontend: Direct JDBC connections, SQL queries, business logic in event handlers, hardcoded colors/spacing
- General: Hardcoded credentials, ignored exceptions, wildcard imports

## Git Workflow

1. Ensure branch is clean before editing
2. Create feature branch: `git checkout -b <TASK-ID>/[feat/fix/add]/<short-task-name>`
3. Never commit directly to main/master
4. Use conventional commits with issue reference: `<ISSUE-ID>/<type>: <description>`
   - Format: `[#<ISSUE-NUMBER>] <type>: <description>`
   - Examples: `223/feat: add CI/CD reports`, `223/fix: resolve workflow errors`
5. Run test suite and static analysis before committing
6. Verify build succeeds: `mvn test` or `mvn package`
7. Create PR with title: `[#<ISSUE-NUMBER>] <type>: <description>` and description: "Fixes #ISSUE-NUMBER"

## Issue-PR Traceability Rules

When creating or updating issues and PRs, always maintain traceability:

### PR Title Format (Conventional Commits)
- Use standard conventional commit format: `<type>: <description>`
- Example: `feat: add CI/CD markdown reports`
- Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`
- Note: Issue reference goes in PR description (e.g., "Fixes #223"), not in title

### PR Description
- Always reference the associated issue in the first line: "Fixes #ISSUE-NUMBER" or "Implements #ISSUE-NUMBER"
- Add section with labels used and their purpose

### Issue Labels for Traceability
- Add appropriate type labels: `MEJORAS`, `BUG`, `DOCUMENTACION`, etc.
- Add component labels: `BACKEND`, `FRONTEND`, `DEVOPS`, `DB`, etc.
- Add status labels: `in-progress`, `ready-for-dev`, `blocked`, etc.

### Git Commits
- Use conventional commits with issue reference: `<ISSUE-ID>/<type>: <description>`
- Example: `223/feat: add CI/CD reports to GitHub Pages`

### Workflow Changes
- When modifying GitHub Actions workflows, ensure:
  - Jobs use `continue-on-error: true` for non-critical steps
  - Test reporters have `only-if` conditions checking for report files
  - All jobs complete successfully without failing the workflow

## Best Practices for CI/CD

1. **Never skip tests** - Use test patterns with `-DfailIfNoTests=false` when filtering
2. **Handle missing reports** - Use `only-if` for test reporters and conditional steps
3. **External services** - Avoid dependence on GitHub Code Scanning, use local reports
4. **Report format** - Generate Markdown reports and publish to GitHub Pages
