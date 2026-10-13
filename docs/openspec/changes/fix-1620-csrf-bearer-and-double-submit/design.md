> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

PR #1618 enabled CSRF on the `/api/**` chain with the double-submit cookie
pattern and PR #1619 tightened the CORS allow-list, but the resulting policy only
works for a client that sends the *masked* token in `X-XSRF-TOKEN`. Neither
shipped client does:

- `frontend/src/lib/api-client.ts` `buildHeaders()` reads the `XSRF-TOKEN` cookie
  from `document.cookie` and echoes it verbatim — the documented SPA flow.
- Bruno API tests and the Playwright seed helpers send `Authorization: Bearer …`
  and no CSRF token at all.

`main` is therefore red in `Playwright E2E — Full Suite`: every mutating request
in the Bruno suite and in all three UI shards returns 403. Three distinct defects
sit behind that one symptom, and they must be fixed together:

1. **Bearer clients have no exemption.** A credential the client must attach
   explicitly cannot be replayed by a third-party site, so CSRF does not apply.
2. **The header token is XOR-decoded.** The default
   `XorCsrfTokenRequestAttributeHandler` expects the mask that
   `CsrfFilter` renders into server-side forms; the raw cookie value fails to
   resolve, so the browser flow is always 403.
3. **The cookie is not delivered where the browser needs it, and it is rotated.**
   `POST /api/v1/users/login` and `/logout` write their `Set-Cookie` through the
   `ResponseEntity` header map, which is applied with *set* semantics and wipes
   the token cookie the security chain had just added. Independently, Spring
   Security's `CsrfAuthenticationStrategy` re-issues the token on every
   authenticated request.

Constraints: no client signature may change; the frontend must not start holding
a credential; Spring Security here is 7.1.1 (Spring Boot 4.1.1), so
`CsrfConfigurer.spa()`, `CsrfTokenRequestHandler` and
`sessionAuthenticationStrategy(…)` are all first-class APIs.

## Goals / Non-Goals

**Goals:**

- Both supported clients work against the enforced policy: cookie-authenticated
  browsers with the double-submit nonce, and Bearer-token API clients without it.
- The `XSRF-TOKEN` nonce is delivered on the responses that create the session
  and is stable while the session is unchanged, so a client that reads the cookie
  and then sends a request cannot be raced by its own parallel traffic.
- The Playwright E2E suite (Bruno + UI shards) is green again without a workflow
  edit.

**Non-Goals:**

- No change to the CORS allow-list, to the JWT/cookie session design, or to any
  endpoint signature.
- Not archiving the stale OpenSpec backlog (#1265) — reported separately.
- Not touching `csrf(withDefaults())` on the actuator and default chains: their
  reachable surface is GET/`permitAll` only, so the SPA flow does not apply.
- Not adding an SSR guard to `document.cookie` in `api-client.ts`: every consumer
  is a `"use client"` component with no SSR query execution (latent-only risk).

## Decisions

**1. Use `CsrfConfigurer.spa()` instead of a local copy of the recipe.**
The Spring Security reference documentation's `SpaCsrfTokenRequestHandler` is
shipped by this version as `spa()` — cookie repository plus a handler that
resolves a header-supplied value verbatim and keeps the XOR mask for
server-rendered tokens. Writing our own copy was the first attempt; the framework
API is less code to own and is the supported extension point. *Alternative
rejected:* a blanket plain `CsrfTokenRequestAttributeHandler`, which would drop
the mask for every case, including the server-rendered ones that still need it.

**2. Exempt Bearer requests with a request-matcher lambda, not a second chain.**
`ignoringRequestMatchers(request -> …)` keeps one filter chain and one place where
the policy is legible. The exemption is by header presence, so an *invalid* Bearer
token also skips CSRF — acceptable: it is not a cookie, so a cross-site page
cannot make the browser attach it, and the request still has to pass
authentication and authorization. *Alternative rejected:* splitting the API into
two chains by header, which Spring Security cannot express declaratively.

**3. Materialize the token before the chain, and keep appending cookies.**
`CsrfCookieFilter` forces `csrfToken.getToken()` before `filterChain.doFilter`, so
the cookie is written even when the response is a large body (the PDF downloads
served by `apiGetBytes`). Materializing *after* the chain is the other order seen
in the wild, but it depends on the response not being committed yet. That alone is
not enough on login/logout, whose controller wrote `Set-Cookie` through the
`ResponseEntity` header map — the entity path applies headers with *set* semantics
(`HttpEntityMethodProcessor` → `HttpServletResponse.setHeader`), which deletes
every `Set-Cookie` already on the response. `UserController` therefore appends the
session cookie with `HttpServletResponse.addHeader`, which is additive.

**4. Do not rotate the nonce (`sessionAuthenticationStrategy` no-op).**
`CsrfAuthenticationStrategy` deletes and re-issues the cookie on every
authenticated request. That protects a session-scoped token against fixation
(an attacker planting a token before login). Here the token is a stateless
double-submit nonce: the server accepts any value that the cookie and the header
agree on, so rotation adds no protection but makes the browser's read-then-send
racy — a dashboard that fires six parallel requests rotates the cookie six times,
and any response landing between the client's `document.cookie` read and its
request being sent leaves the header and the cookie disagreeing (403 on the next
write). `CsrfConfigurer.sessionAuthenticationStrategy(…)` is the documented hook
for this, and the double-submit check itself is untouched. *Alternative
rejected:* keeping rotation and documenting the flake.

**5. Keep the session-bootstrap exemption for `login`/`logout`.**
A session must be obtainable before a token exists, and logout must be possible
with a revoked token. Both are already `permitAll`; the exemption makes the CSRF
matcher agree with the authorization policy.

## Riesgos / Trade-offs

- [Bearer exemption is header-based, not token-based] → An attacker cannot make a
  victim's browser attach `Authorization`, so the CSRF risk is unchanged; the
  request still needs a valid token to pass authorization. Covered by a test that
  the *cookie* flow is still rejected without the header.
- [Disabling rotation weakens fixation defence] → The nonce is not bound to a
  session and never authorizes anything by itself; a planted value still has to be
  echoed by the victim's own client on every request. Documented here and in the
  configuration Javadoc.
- [A non-HttpOnly cookie is readable by XSS] → It already was, by design of the
  double-submit pattern; the credential (JWT) stays HttpOnly in
  `notaire-auth-token`, and the API sets no `innerHTML`.
- [The `spa()` handler leaves the token unmaterialized on safe methods] → The
  chain's `CsrfCookieFilter` materializes it; verified by the "safe request issues
  the token cookie" scenario.
- [E2E tests cannot all be exercised locally] → The stack (Postgres + backend jar
  + built frontend) was run locally and `TS-0044` plus the three new tests pass;
  the full sharded suite is verified in CI.

## Testing Strategy

TDD (Constitution P1): every scenario below was written and observed failing
before the implementation, then green after.

| Scenario (delta spec) | Test |
|-----------------------|------|
| A safe request issues the token cookie | `CsrfProtectionIntegrationTest#browserCookieDoubleSubmitShouldBeAccepted` (step 1) |
| Login returns the token cookie beside the session cookie | `CsrfProtectionIntegrationTest#loginShouldIssueTheXsrfCookieBesideTheSessionCookie` (observed failing: login returned only the session cookie) |
| The nonce stays stable while the session is unchanged | `CsrfProtectionIntegrationTest#authenticatedRequestShouldNotRotateTheXsrfCookie` (observed failing: two `Set-Cookie` headers, a delete plus a new value) |
| Browser double-submit is accepted | `CsrfProtectionIntegrationTest#browserCookieDoubleSubmitShouldBeAccepted` + `TS-0044` "browser double-submit — the XSRF-TOKEN cookie echoed as X-XSRF-TOKEN is accepted" |
| Cookie-authenticated request without the header is rejected | `CsrfProtectionIntegrationTest#cookieAuthenticatedRequestWithoutXsrfHeaderShouldBeRejected` + `TS-0044` "state-changing cookie request without X-XSRF-TOKEN is rejected with 403" |
| Bearer-token write succeeds without an XSRF token | `CsrfProtectionIntegrationTest#bearerTokenRequestShouldBeExemptFromCsrf` + `TS-0044` "Bearer-token client is exempt from CSRF" |
| The exemption does not weaken the cookie flow | `CsrfProtectionIntegrationTest#cookieAuthenticatedRequestWithoutXsrfHeaderShouldBeRejected` |
| Logout succeeds without a CSRF token | `CsrfProtectionIntegrationTest#loginEndpointShouldBeExemptFromCsrf` (login path) + existing logout coverage in `RbacIntegrationTest`/`TS-0006` |

## Regression Strategy

- `CsrfProtectionIntegrationTest` asserts the contract against the real filter
  chain (cookies and headers, no `SecurityMockMvcRequestPostProcessors.csrf()`).
- `SecurityMockMvcRequestPostProcessors.csrf()` was removed from
  `RbacIntegrationTest`, `JwtAuthIntegrationTest`,
  `HistoryWriteAuthorizationIntegrationTest` and `CsrfProtectionIntegrationTest`:
  it reflectively replaces the shared `CsrfFilter`'s token repository, which
  disables enforcement for the rest of the JVM and *hides* this regression.
- The full backend suite (2356 tests) stays green; no test was deleted.

## Playwright Strategy

`testing/e2e/tests/TS-0044-security-csrf-cors-qa.spec.ts` gains three cases
(double-submit accepted, header missing → 403, Bearer exempt) and its header
comment is corrected to describe the enforced posture. `TS-0044` is the owning
spec for CU78's CSRF/CORS posture; no other spec asserts it.

## Deployment Strategy

No new configuration, migration or secret: the fix ships with the normal image
build (`.github/workflows/cd.yml` → GHCR) and is live for the API as soon as the
container restarts. Staging smoke: `POST /api/v1/users/login` must return both
`XSRF-TOKEN` and `notaire-auth-token`, and a Bruno/`page.request` Bearer write
must return 201 without a CSRF header.

## Rollback Strategy

Revert the PR. The change is confined to the API chain's CSRF policy, two cookie
writes in `UserController`, tests and docs; the database schema is untouched, so
no migration rollback is needed and the previous (broken) policy returns with the
revert.
