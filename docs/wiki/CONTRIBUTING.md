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

1. Create a feature branch from `main`
2. Make your changes
3. Run tests locally:
   ```bash
   mvn clean test -pl backend-api
   ```
4. Ensure coverage is above 80%
5. Push and create PR
6. Wait for CI workflows to pass
7. Request review from code owners

### 4. CI/CD Pipeline

All PRs must pass:

- [ ] Build compilation
- [ ] Unit tests
- [ ] Integration tests
- [ ] Code coverage (≥80%)
- [ ] Security scan (Trivy)
- [ ] Code quality (SpotBugs)

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
