# Security Policy

## Supported Versions

We actively support and provide security updates for the following versions:

| Version | Supported          | Notes |
| ------- | ------------------ | ----- |
| 1.x     | :white_check_mark: | Current stable release |

## Reporting a Vulnerability

If you discover a security vulnerability within Notaire, please send an e-mail to the maintainer. All security vulnerabilities will be promptly addressed.

Please include the following information:

- Type of vulnerability
- Full paths of source file(s) related to the vulnerability
- Location of the affected source code (tag/branch/commit or direct URL)
- Any special configuration required to reproduce the issue
- Step-by-step instructions to reproduce the issue
- Proof-of-concept or exploit code (if possible)
- Impact of the issue, including how an attacker might exploit it

## Security Features

### Implemented Security Measures

1. **Docker Security**
   - Non-root user execution
   - Minimal base image (Alpine Linux)
   - Read-only file system recommended
   - Security scanning with Trivy

2. **Application Security**
   - Input validation with Spring Boot Validation
   - SQL injection prevention via JPA/Hibernate
   - Secure password storage (bcrypt)
   - CORS configuration

3. **CI/CD Security**
   - Dependency vulnerability scanning (Trivy)
   - SAST scanning (SpotBugs)
   - Docker image scanning
   - SBOM generation

4. **GitHub Security Features**
   - Dependency Graph
   - Dependabot alerts
   - Secret scanning
   - Code scanning alerts

## Dependencies Security

This project uses the following security tools:

- **Trivy**: Vulnerability scanner for containers and dependencies
- **SpotBugs**: Static analysis for Java bytecode
- **Dependabot**: Automated dependency updates
- **GitHub Code Scanning**: SAST integration

## Security Best Practices

When deploying Notaire:

1. **Environment Variables**
   - Never commit secrets to version control
   - Use Docker secrets or environment variables
   - Rotate database credentials regularly

2. **Database**
   - Use strong passwords
   - Enable SSL connections
   - Restrict network access

3. **API Security**
   - JWT authentication on every `/api/**` endpoint (see [API Authentication Guide](API-AUTHENTICATION-GUIDE.md))
   - Login brute-force lockout per username (`LoginAttemptService`, in-memory, single-instance)
   - Enable HTTPS in production
   - General API rate limiting (not yet implemented — only login attempts are throttled)

4. **Docker**
   - Run containers as non-root
   - Use read-only volumes where possible
   - Scan images before deployment
