# ADR-008: Security & Authentication

## Status
Accepted

## Context
The project is migrating from a standalone Swing monolith to a client-server architecture. We need a secure way to authenticate users from both the legacy Swing client and the new Next.js frontend, and authorize their actions on the REST API.

## Decision
We will implement **JWT (JSON Web Token)** based authentication using **Spring Security**.

### Key implementation details:
1.  **Authentication Provider**: Spring Security with custom `UserDetailsService`.
2.  **Token Generation**: Upon successful login, the server issues a signed JWT.
3.  **Token Storage**:
    *   Swing Client: In-memory session.
    *   Next.js: Secure HTTP-only cookies.
4.  **Authorization**: Role-Based Access Control (RBAC) using `@PreAuthorize` annotations on controllers.
5.  **Audit**: Integration with `RegistroAuditoria` to track login/logout events.

## Options Considered
-   **Session-based (JSESSIONID)**: Difficult to scale and problematic for cross-domain (Next.js).
-   **OAuth2 / OpenID Connect**: Powerful but adds complexity that is currently unnecessary for this internal project.

## Consequences
-   **Pros**: Stateless, easy to scale, works with multiple client types.
-   **Cons**: Token revocation is more complex (requires blacklisting or short TTLs).
