# Contributing to Notaire

## Development Workflow

### 1. Branch Naming

Use conventional branch names:

```
feature/<issue-number>-<description>
bugfix/<issue-number>-<description>
hotfix/<issue-number>-<description>
release/<version>
```

### 2. Commit Messages

Follow conventional commits:

```
feat: add new client endpoint
fix: resolve null pointer in budget service
docs: update API documentation
test: add integration tests for reports
ci: add new workflow
refactor: simplify payment calculation
```

### 3. Pull Request Process

1. Install the git hooks once per clone:
   ```bash
   bash scripts/install-git-hooks.sh
   ```
2. Create a feature branch from `main`
3. Make your changes
4. Run the CI gates locally before pushing:
   ```bash
   bash scripts/preflight.sh --fix
   ```
5. Push and create PR — the `pre-push` hook re-runs the gates and blocks the
   push if any fail
6. Wait for CI workflows to pass
7. Request review from code owners

### 4. CI/CD Pipeline

**Validate locally first.** `scripts/preflight.sh` runs the same gates CI runs,
so a push that passes it passes CI. This matters because not every CI gate is
reachable from the usual commands — notably, Spotless is not bound to `mvn
verify`, so `mvn verify` can be green while CI's "Code Lint" job fails. See
[CI-PREFLIGHT.md](./CI-PREFLIGHT.md).

All PRs must pass:

- [ ] Build compilation
- [ ] Unit tests
- [ ] Integration tests (H2 + Testcontainers/PostgreSQL)
- [ ] Coverage gate (JaCoCo ratchet floor; 80% is the target)
- [ ] Code Lint — Spotless format check (blocking) + Checkstyle (advisory)
- [ ] Frontend: TypeScript, ESLint, Vitest, Next.js build
- [ ] E2E: Playwright + Bruno API suite
- [ ] Security scan (Trivy)

### 5. Building Docker Image Locally

```bash
# Build image
docker build -t notary-backend .

# Run container
docker run -p 8080:8080 notary-backend
```

### 6. Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=DocumentServiceTest

# Run with coverage
mvn test -pl backend-api
mvn jacoco:report -pl backend-api

# View coverage report
open backend-api/target/site/jacoco/index.html
```

### 7. Code Style

Follow the project's Java conventions:

- Java 21
- 4 spaces indentation
- 120 character line limit
- No wildcard imports
- Use SLF4J for logging

### 8. Security

- Never commit secrets
- Use environment variables
- Run security scans locally:
  ```bash
  trivy fs .
  ```

### 9. Dependencies

- Use Dependabot for updates
- Review dependency changes in PRs
- Keep test dependencies separate

## Questions?

Open an issue for questions about contributing.
