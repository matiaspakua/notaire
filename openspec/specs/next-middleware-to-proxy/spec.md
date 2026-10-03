# next-middleware-to-proxy Specification

## Purpose
Migrate the Next.js 16 edge request interceptor from the deprecated
`middleware.ts` convention to `proxy.ts` with `export function proxy`, while
preserving CU84 login route guards and non-regression of cookie-based session
auth. Source: #1056; owner CU84 – Login al sistema.
## Requirements
### Requirement: Edge interceptor uses Next 16 proxy convention

The frontend MUST expose the edge request interceptor as `frontend/src/proxy.ts`
exporting `proxy` (not `middleware`). The deprecated `frontend/src/middleware.ts`
file MUST NOT remain in the tree after the change. The migration SHOULD be
performed with `npx @next/codemod middleware-to-proxy` (or an equivalent manual
transform that yields the same end state if the codemod cannot run).

#### Scenario: Codemod yields proxy.ts with export proxy

- **WHEN** the migration is applied on the frontend package
- **THEN** `frontend/src/proxy.ts` exists and exports a function named `proxy`
  that performs the former middleware route-guard logic

#### Scenario: Deprecated middleware.ts is removed

- **WHEN** the migration is complete
- **THEN** `frontend/src/middleware.ts` MUST NOT exist in the repository

### Requirement: Route-guard semantics are preserved

Unauthenticated browser navigations to protected app routes MUST redirect to
`/login`. Authenticated sessions (UX status cookie present) visiting `/login`
MUST redirect to `/dashboard`. Non-admin roles MUST continue to be denied
`/dashboard/administracion/**` per #1052 helpers. Paths under `/api/**` MUST
still bypass edge redirects so the rewrite/BFF can forward credentials
(including HttpOnly JWT after #1051).

#### Scenario: Unauthenticated protected route redirects to login

- **WHEN** a browser requests a protected dashboard path without the auth status
  cookie
- **THEN** the response is a redirect to `/login`

#### Scenario: Authenticated login path redirects to dashboard

- **WHEN** a browser with a valid auth status cookie requests `/login`
- **THEN** the response is a redirect to `/dashboard`

#### Scenario: Non-admin is denied admin routes at the edge

- **WHEN** an authenticated non-admin role cookie is present and the path is
  under `/dashboard/administracion`
- **THEN** the response redirects away from administración (forbidden dashboard
  path) as today

#### Scenario: API proxy paths skip edge auth redirects

- **WHEN** a request targets a path under `/api/`
- **THEN** the edge `proxy` MUST NOT redirect it to `/login` (request proceeds
  to the Next rewrite/BFF)

### Requirement: Build is free of middleware deprecation warnings

Production frontend builds MUST NOT emit Next.js deprecation warnings about the
`middleware` file convention.

#### Scenario: next build has no middleware deprecation warning

- **WHEN** `next build` (or `npm run build` in `frontend/`) completes on the
  migrated tree
- **THEN** the build log MUST NOT contain a deprecation warning about the
  `middleware` file convention

### Requirement: Auth E2E remains green

Login, logout, and auth-dependent Playwright coverage MUST pass after the
migration. The change MUST NOT regress HttpOnly cookie session auth introduced
or in-flight under #1051 (no requirement to re-introduce `localStorage` JWT).

#### Scenario: Auth E2E suite stays green

- **WHEN** the Playwright auth-related suite (including login/logout golden
  paths) runs against the migrated frontend
- **THEN** those tests pass without weakening assertions

#### Scenario: HttpOnly cookie auth path is not regressed

- **WHEN** #1051 cookie-session behavior is present on the base branch (or lands
  before/with this change)
- **THEN** browser login still authenticates API calls via the cookie/BFF path
  and the edge `proxy` continues to honor UX status/role cookies without
  requiring a script-readable JWT

